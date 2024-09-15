package dev.robocode.tankroyale.gui.util

import java.net.InetAddress
import java.net.Inet4Address
import java.net.Inet6Address

fun isLocalAddress(ipAddress: String): Boolean {
    return try {
        val addr = InetAddress.getByName(ipAddress)
        when (addr) {
            is Inet4Address -> isLocalIPv4(addr)
            is Inet6Address -> isLocalIPv6(addr)
            else -> false
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        false
    }
}

private fun isLocalIPv4(addr: Inet4Address): Boolean {
    return addr.isLoopbackAddress ||
            addr.isLinkLocalAddress ||
            addr.isSiteLocalAddress ||
            addr.hostAddress.startsWith("169.254.") // IPv4 link-local
}

private fun isLocalIPv6(addr: Inet6Address): Boolean {
    return addr.isLoopbackAddress ||
            addr.isLinkLocalAddress ||
            addr.isSiteLocalAddress ||
            addr.hostAddress.lowercase().startsWith("fc") || // ULA
            addr.hostAddress.lowercase().startsWith("fd")    // ULA
}
