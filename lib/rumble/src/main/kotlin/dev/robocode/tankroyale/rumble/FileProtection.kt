package dev.robocode.tankroyale.rumble

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.AclEntry
import java.nio.file.attribute.AclEntryPermission
import java.nio.file.attribute.AclEntryType
import java.nio.file.attribute.AclFileAttributeView
import java.nio.file.attribute.PosixFilePermissions

/** Makes a file usable by its owner only, and checks whether a file is. */
interface FileProtection {
    /** Creates an empty file that only its owner may use, if the file system can do that. */
    fun createOwnerOnly(file: Path)

    /** True only when the file's rights, read back from the file system, let nobody but the owner use it. */
    fun isOwnerOnly(file: Path): Boolean
}

/**
 * Owner-only files with standard Java on every operating system: POSIX rights where the file system has them
 * (Linux, macOS), and an access list that holds only the owner where it has those instead (Windows). The rights are
 * read back to confirm, because some file systems, such as a Windows drive mounted into Linux, accept the call but
 * do not restrict anything.
 */
object JavaFileProtection : FileProtection {
    private const val OWNER_ONLY = "rw-------"

    override fun createOwnerOnly(file: Path) {
        val views = file.fileSystem.supportedFileAttributeViews()
        when {
            "posix" in views -> Files.createFile(
                file,
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(OWNER_ONLY)),
            )
            "acl" in views -> {
                Files.createFile(file)
                val view = Files.getFileAttributeView(file, AclFileAttributeView::class.java)
                val entry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(view.owner)
                    .setPermissions(
                        AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA, AclEntryPermission.APPEND_DATA,
                        AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.WRITE_ATTRIBUTES,
                        AclEntryPermission.READ_NAMED_ATTRS, AclEntryPermission.WRITE_NAMED_ATTRS,
                        AclEntryPermission.READ_ACL, AclEntryPermission.WRITE_ACL, AclEntryPermission.DELETE,
                        AclEntryPermission.SYNCHRONIZE,
                    )
                    .build()
                view.acl = listOf(entry)
            }
            else -> Files.createFile(file)
        }
    }

    override fun isOwnerOnly(file: Path): Boolean {
        val views = file.fileSystem.supportedFileAttributeViews()
        return try {
            when {
                "posix" in views -> PosixFilePermissions.toString(Files.getPosixFilePermissions(file)) == OWNER_ONLY
                "acl" in views -> {
                    val view = Files.getFileAttributeView(file, AclFileAttributeView::class.java)
                    val acl = view.acl
                    acl.isNotEmpty() && acl.none { it.type() == AclEntryType.ALLOW && it.principal() != view.owner }
                }
                else -> false
            }
        } catch (e: IOException) {
            false
        }
    }
}
