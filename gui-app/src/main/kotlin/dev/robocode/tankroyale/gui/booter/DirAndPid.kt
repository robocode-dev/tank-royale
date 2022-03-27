package dev.robocode.tankroyale.gui.booter

import java.util.*

class DirAndPid(
    val dir: String,
    val pid: Long

) : Comparable<DirAndPid> {

    override fun compareTo(other: DirAndPid): Int {
        val cmp = dir.compareTo(other.dir)
        return if (cmp != 0) cmp else (pid - other.pid).toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DirAndPid

        if (dir != other.dir) return false
        if (pid != other.pid) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(dir, pid)
    }

    override fun toString(): String {
        return "$dir $pid"
    }
}