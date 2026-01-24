package dev.robocode.tankroyale.common.util

import dev.robocode.tankroyale.common.APPLICATION_NAME
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File

@Suppress("unused") // Test class is discovered by Kotest via reflection
class UserDataDirectoryTest : FunSpec({

    context("UserDataDirectory") {

        test("get() returns a valid directory") {
            val dir = UserDataDirectory.get("Test App")
            dir shouldNotBe null
            dir.isAbsolute shouldBe true
        }

        test("get() creates directory if it doesn't exist") {
            val testAppName = "TestApp-${System.currentTimeMillis()}"
            val dir = UserDataDirectory.get(testAppName)

            dir.exists() shouldBe true
            dir.isDirectory shouldBe true

            // Clean up
            dir.deleteRecursively()
        }

        test("Windows: uses LOCALAPPDATA or APPDATA") {
            if (Platform.operatingSystemType == Platform.PlatformType.Windows) {
                val dir = UserDataDirectory.get("Test App")
                val path = dir.absolutePath

                val localAppData = System.getenv("LOCALAPPDATA")
                val appData = System.getenv("APPDATA")

                val shouldContainPath = localAppData ?: appData ?: File(
                    System.getProperty("user.home"),
                    "AppData${File.separator}Local"
                ).absolutePath

                path shouldContain shouldContainPath
                path shouldContain "Test App"
            }
        }

        test("macOS: uses Library/Application Support") {
            if (Platform.operatingSystemType == Platform.PlatformType.Mac) {
                val dir = UserDataDirectory.get("Test App")
                val path = dir.absolutePath

                path shouldContain "Library"
                path shouldContain "Application Support"
                path shouldContain "Test App"
            }
        }

        test("Linux: uses .config with normalized name") {
            if (Platform.operatingSystemType == Platform.PlatformType.Linux) {
                val dir = UserDataDirectory.get("Test App")
                val path = dir.absolutePath

                // Should use .config (unless XDG_CONFIG_HOME is set)
                val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
                if (xdgConfigHome.isNullOrBlank()) {
                    path shouldContain ".config"
                } else {
                    path shouldContain xdgConfigHome
                }

                // App name should be normalized to lowercase with hyphens
                path shouldContain "test-app"
                path shouldNotContain "Test App"
            }
        }

        test("Robocode Tank Royale uses correct directories") {
            val dir = UserDataDirectory.get()

            when (Platform.operatingSystemType) {
                Platform.PlatformType.Windows -> {
                    dir.name shouldBe APPLICATION_NAME
                }

                Platform.PlatformType.Mac -> {
                    dir.name shouldBe APPLICATION_NAME
                    dir.absolutePath shouldContain "Application Support"
                }

                Platform.PlatformType.Linux -> {
                    dir.name shouldBe "robocode-tank-royale"
                }

                else -> {
                    // Other platforms fall back to Linux behavior
                    dir.name shouldBe "robocode-tank-royale"
                }
            }
        }

        test("getSubdir() creates subdirectory within user data directory") {
            val testAppName = "TestApp-${System.currentTimeMillis()}"
            val subdirName = "test-subdir"
            val subdir = UserDataDirectory.getSubdir(subdirName, testAppName)

            subdir.exists() shouldBe true
            subdir.isDirectory shouldBe true
            subdir.name shouldBe subdirName
            subdir.parentFile.name shouldBe when (Platform.operatingSystemType) {
                Platform.PlatformType.Linux -> testAppName.lowercase().replace(" ", "-")
                else -> testAppName
            }

            // Clean up
            subdir.parentFile.deleteRecursively()
        }

        test("getSubdir() handles nested subdirectories") {
            val testAppName = "TestApp-${System.currentTimeMillis()}"
            val subdirPath = "level1${File.separator}level2"
            val subdir = UserDataDirectory.getSubdir(subdirPath, testAppName)

            subdir.exists() shouldBe true
            subdir.isDirectory shouldBe true

            // Clean up
            val rootDir = UserDataDirectory.get(testAppName)
            rootDir.deleteRecursively()
        }
    }
})
