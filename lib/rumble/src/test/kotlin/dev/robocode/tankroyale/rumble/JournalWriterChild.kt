package dev.robocode.tankroyale.rumble

import java.nio.file.Path

/**
 * A program the journal tests start as a separate process: it adds records to a journal and prints one line for each
 * record the journal reported as kept, so that the test can stop it in the middle and compare.
 */
object JournalWriterChild {
    @JvmStatic
    fun main(args: Array<String>) {
        val journal = RankedJournal.open(RumbleEnvironments.PUBLIC, Path.of(args[0]))
        var number = 0
        while (true) {
            number++
            val record = JournalFixtures.record("child-$number")
            journal.add(record)
            println("kept child-$number")
            System.out.flush()
        }
    }
}
