import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Throwaway feasibility check: can one Java-only credential file design work the same on Windows, macOS and Linux?
 *
 * Run (Java 11 or newer, nothing to build):
 *   java CredentialFileSpike.java --write    writes the file and keeps it, to test across separate runs
 *   java CredentialFileSpike.java --verify   in a NEW process reads what --write left, then deletes it
 *   java CredentialFileSpike.java            does everything in one process and cleans up
 *
 * The file holds only a FAKE token. Everything it creates lives in one folder that is removed again.
 */
public class CredentialFileSpike {

    private static final String APP_NAME = "Robocode Tank Royale";
    private static final String FAKE_TOKEN = "{\"refreshToken\":\"ghr_FAKE_TOKEN_FOR_THE_SPIKE_ONLY\"}";
    private static final byte[] MAGIC = "TRC1".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] APP_SALT = "robocode-tank-royale/credential-file/v1".getBytes(StandardCharsets.UTF_8);

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "--all";
        Path folder = userDataDir().resolve("credential-spike");
        Path file = folder.resolve("signin-test.bin");

        section("Environment");
        info("java", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        info("os", System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        info("user.name", System.getProperty("user.name"));
        info("data folder", folder.toString());

        Files.createDirectories(folder);
        try {
            section("File system");
            fileSystemFacts(folder);

            if (mode.equals("--verify")) {
                verifyFromEarlierRun(file);
            } else {
                writeAndCheck(folder, file, mode.equals("--write"));
            }
        } finally {
            if (!mode.equals("--write")) {
                cleanUp(folder);
            }
        }

        section("Summary");
        System.out.println((failed == 0 ? "RESULT: ALL " + passed + " CHECKS PASSED" : "RESULT: " + failed + " FAILED, " + passed + " passed")
                + " | " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " | java " + System.getProperty("java.version"));
        System.exit(failed == 0 ? 0 : 1);
    }

    // ---- the design under test ----------------------------------------------------------------------------------

    private static void writeAndCheck(Path folder, Path file, boolean keep) throws Exception {
        section("1. File that only the owner may use");
        Path temp = folder.resolve("signin-test.bin.tmp");
        Files.deleteIfExists(temp);
        String how = createOwnerOnly(temp);
        check("owner-only file could be created (" + how + ")", !how.startsWith("none"));
        check("permissions read back as owner-only", ownerOnlyReadBack(temp));

        section("2. Key derived from the user and the machine, same Java code everywhere");
        long t0 = System.nanoTime();
        SecretKey key = deriveKey(identity());
        long ms = (System.nanoTime() - t0) / 1_000_000;
        check("PBKDF2 + AES-256 are available in this Java (" + ms + " ms)", key.getEncoded().length == 32);
        check("deriving the key twice gives the same key", Arrays.equals(key.getEncoded(), deriveKey(identity()).getEncoded()));

        section("3. Encrypt, write atomically, read back");
        byte[] blob = encrypt(key, FAKE_TOKEN.getBytes(StandardCharsets.UTF_8));
        Files.write(temp, blob);
        String moved = moveIntoPlace(temp, file);
        check("atomic replace worked (" + moved + ")", !moved.startsWith("FAILED"));
        check("file kept its owner-only permissions after the move", ownerOnlyReadBack(file));
        byte[] stored = Files.readAllBytes(file);
        check("file holds no readable token text", !new String(stored, StandardCharsets.ISO_8859_1).contains("FAKE_TOKEN"));
        check("decrypting returns exactly what was saved", FAKE_TOKEN.equals(new String(decrypt(key, stored), StandardCharsets.UTF_8)));

        section("4. Refusals: damaged file, other user, other machine");
        byte[] damaged = stored.clone();
        damaged[damaged.length - 3] ^= 0x55;
        check("a changed byte is detected and refused", refuses(key, damaged));
        check("a different user name cannot open it", refuses(deriveKey("someone-else|" + System.getProperty("user.home") + "|" + System.getProperty("os.name")), stored));
        check("a different home folder cannot open it", refuses(deriveKey(System.getProperty("user.name") + "|/other/home|" + System.getProperty("os.name")), stored));
        check("a truncated file is refused, not crashed on", refuses(key, Arrays.copyOf(stored, 10)));
        check("an empty file is refused, not crashed on", refuses(key, new byte[0]));

        section("5. Sign out");
        if (keep) {
            info("kept", "the file is left for --verify, which must be run in a separate process");
        } else {
            Files.delete(file);
            check("sign out removes the file", !Files.exists(file));
        }
    }

    private static void verifyFromEarlierRun(Path file) throws Exception {
        section("Next run: can a NEW process open what the earlier process saved?");
        check("the file from the earlier run is there", Files.exists(file));
        if (!Files.exists(file)) return;
        byte[] stored = Files.readAllBytes(file);
        SecretKey key = deriveKey(identity());
        check("the new process derives the same key and decrypts it", FAKE_TOKEN.equals(new String(decrypt(key, stored), StandardCharsets.UTF_8)));
        check("permissions are still owner-only", ownerOnlyReadBack(file));
    }

    // ---- building blocks ----------------------------------------------------------------------------------------

    /** The same three folders as UserDataDirectory in lib:common. */
    private static Path userDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            String base = System.getenv("LOCALAPPDATA");
            if (base == null) base = System.getenv("APPDATA");
            if (base == null) base = Paths.get(home, "AppData", "Local").toString();
            return Paths.get(base, APP_NAME);
        }
        if (os.contains("mac")) {
            return Paths.get(home, "Library", "Application Support", APP_NAME);
        }
        String xdg = System.getenv("XDG_CONFIG_HOME");
        Path base = (xdg == null || xdg.trim().isEmpty()) ? Paths.get(home, ".config") : Paths.get(xdg);
        return base.resolve(APP_NAME.toLowerCase().replace(' ', '-'));
    }

    /** What ties the key to this user and this machine. Deliberately no host name: it changes with the network. */
    private static String identity() {
        return System.getProperty("user.name") + "|" + System.getProperty("user.home") + "|" + System.getProperty("os.name");
    }

    private static SecretKey deriveKey(String identity) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] bytes = factory.generateSecret(new PBEKeySpec(identity.toCharArray(), APP_SALT, 100_000, 256)).getEncoded();
        return new SecretKeySpec(bytes, "AES");
    }

    private static byte[] encrypt(SecretKey key, byte[] plain) throws GeneralSecurityException {
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
        cipher.updateAAD(MAGIC);
        byte[] encrypted = cipher.doFinal(plain);
        return ByteBuffer.allocate(MAGIC.length + nonce.length + encrypted.length).put(MAGIC).put(nonce).put(encrypted).array();
    }

    private static byte[] decrypt(SecretKey key, byte[] file) throws GeneralSecurityException {
        if (file.length < MAGIC.length + 12 + 16) throw new GeneralSecurityException("file too short");
        for (int i = 0; i < MAGIC.length; i++) if (file[i] != MAGIC[i]) throw new GeneralSecurityException("not a credential file");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, file, MAGIC.length, 12));
        cipher.updateAAD(MAGIC);
        return cipher.doFinal(file, MAGIC.length + 12, file.length - MAGIC.length - 12);
    }

    private static boolean refuses(SecretKey key, byte[] file) {
        try {
            decrypt(key, file);
            return false;
        } catch (GeneralSecurityException e) {
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Creates an empty file only the owner can use, with whatever the file system offers. Returns how it was done. */
    private static String createOwnerOnly(Path path) throws IOException {
        Set<String> views = path.getFileSystem().supportedFileAttributeViews();
        if (views.contains("posix")) {
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
            Files.createFile(path, attr);
            return "POSIX rw-------";
        }
        if (views.contains("acl")) {
            Files.createFile(path);
            AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
            UserPrincipal owner = view.getOwner();
            AclEntry entry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(owner)
                    .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA, AclEntryPermission.APPEND_DATA,
                            AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.WRITE_ATTRIBUTES, AclEntryPermission.READ_NAMED_ATTRS,
                            AclEntryPermission.WRITE_NAMED_ATTRS, AclEntryPermission.READ_ACL, AclEntryPermission.WRITE_ACL,
                            AclEntryPermission.DELETE, AclEntryPermission.SYNCHRONIZE)
                    .build();
            view.setAcl(Collections.singletonList(entry));
            return "ACL for owner " + owner.getName();
        }
        Files.createFile(path);
        return "none: this file system has no permission control (views: " + views + ")";
    }

    private static boolean ownerOnlyReadBack(Path path) throws IOException {
        Set<String> views = path.getFileSystem().supportedFileAttributeViews();
        if (views.contains("posix")) {
            String perms = PosixFilePermissions.toString(Files.getPosixFilePermissions(path));
            info("permissions", perms);
            return perms.equals("rw-------");
        }
        if (views.contains("acl")) {
            AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
            List<AclEntry> acl = view.getAcl();
            String owner = view.getOwner().getName();
            boolean ok = true;
            for (AclEntry e : acl) {
                info("acl", e.type() + " " + e.principal().getName() + " " + e.permissions().size() + " rights");
                if (e.type() == AclEntryType.ALLOW && !e.principal().getName().equals(owner)) ok = false;
            }
            return ok && !acl.isEmpty();
        }
        return false;
    }

    private static String moveIntoPlace(Path temp, Path target) throws IOException {
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return "ATOMIC_MOVE";
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            return "REPLACE_EXISTING (atomic move not supported here)";
        } catch (IOException e) {
            return "FAILED: " + e;
        }
    }

    private static void fileSystemFacts(Path folder) throws IOException {
        info("file system type", Files.getFileStore(folder).type());
        info("attribute views", String.valueOf(folder.getFileSystem().supportedFileAttributeViews()));
        check("the data folder is writable", Files.isWritable(folder));
    }

    private static void cleanUp(Path folder) throws IOException {
        try (java.util.stream.Stream<Path> paths = Files.walk(folder)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
        check("everything the spike created has been removed", !Files.exists(folder));
    }

    // ---- reporting ----------------------------------------------------------------------------------------------

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }

    private static void info(String name, String detail) {
        System.out.println("   info  " + name + ": " + detail);
    }

    private static void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        System.out.println("   " + (ok ? "PASS  " : "FAIL  ") + name);
    }
}
