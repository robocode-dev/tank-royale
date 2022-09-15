package dev.robocode.tankroyale.gui.booter

import java.util.*

class DirAndBootId(
    val dir: String,
    val bootId: Long

) : Comparable<DirAndBootId> {

    override fun compareTo(other: DirAndBootId): Int {
        val cmp = dir.compareTo(other.dir)
        return if (cmp != 0) cmp else (bootId - other.bootId).toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DirAndBootId

        if (dir != other.dir) return false
        if (bootId != other.bootId) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(dir, bootId)
    }

    override fun toString(): String {
        return "$dir $bootId"
    }
}