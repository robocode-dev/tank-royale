package dev.robocode.tankroyale.server.util

/** Invokes [action] for every unique unordered pair `(list[i], list[j])` where `i < j`. */
inline fun <T> forEachUniquePair(list: List<T>, action: (T, T) -> Unit) {
    for (i in list.indices) {
        for (j in i + 1 until list.size) {
            action(list[i], list[j])
        }
    }
}
