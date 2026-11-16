import java.nio.file.Files;
import java.nio.file.Path;

/** Writes its arguments, sleeps so a Ctrl+C can arrive, writes that it survived, and exits with the code in its last argument. */
public class CtrlcArgs {
    public static void main(String[] a) throws Exception {
        Path dir = Path.of(System.getProperty("probe.dir"));
        Files.writeString(dir.resolve("args.txt"), String.join("|", a));
        Thread.sleep(9000);
        Files.writeString(dir.resolve("child-alive.txt"), "alive");
        System.exit(Integer.parseInt(a[a.length - 1]));
    }
}
