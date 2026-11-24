package dev.robocode.tankroyale.common.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** The Java that Tank Royale starts its other programs with: the one it runs on, by its full path. */
class JavaExecTest {

    @Test
    @Tag("RCL-165")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL165_UnitPositive_anotherProgramIsStartedWithTheFullPathOfTheJavaThisOneRunsOn(@TempDir dir: Path) {
        val windows = Files.createDirectories(dir.resolve("win/bin"))
        Files.createFile(windows.resolve("javaw.exe"))
        Files.createFile(windows.resolve("java.exe"))
        assertEquals(windows.resolve("javaw.exe").toString(), JavaExec.javaIn(dir.resolve("win").toString(), windows = true))

        // A Windows Java home without the console-less variant gives the other one.
        val plain = Files.createDirectories(dir.resolve("plain/bin"))
        Files.createFile(plain.resolve("java.exe"))
        assertEquals(plain.resolve("java.exe").toString(), JavaExec.javaIn(dir.resolve("plain").toString(), windows = true))

        val unix = Files.createDirectories(dir.resolve("unix/bin"))
        Files.createFile(unix.resolve("java"))
        assertEquals(unix.resolve("java").toString(), JavaExec.javaIn(dir.resolve("unix").toString(), windows = false))

        // The program's own Java is always a real file.
        assertEquals(true, Files.isRegularFile(Path.of(JavaExec.java())))
    }

    @Test
    @Tag("RCL-165")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL165_UnitNegative_aJavaHomeWithoutAJavaGivesTheBareNameFoundOnThePath(@TempDir dir: Path) {
        assertEquals("javaw", JavaExec.javaIn(dir.resolve("none").toString(), windows = true))
        assertEquals("java", JavaExec.javaIn(null, windows = false))
        assertEquals("java", JavaExec.javaIn("", windows = false))
    }
}
