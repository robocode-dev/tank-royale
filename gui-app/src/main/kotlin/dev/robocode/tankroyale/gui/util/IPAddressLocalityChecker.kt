package dev.robocode.tankroyale.gui.util

import java.net.InetAddress
import java.net.Inet4Address
import java.net.Inet6Address

fun isLocalEndpoint(endpoint: String): Boolean {
    val (ipAddress, _) = parseEndpoint(endpoint)
    return isLocalAddress(ipAddress)
}

private fun isLocalAddress(ipAddress: String): Boolean {
    return try {
        val addr = InetAddress.getByName(ipAddress.split("%")[0]) // Remove interface identifier if present
        val cleanIpAddress = addr.hostAddress // This gives the IP without the leading slash
        when (addr) {
            is Inet4Address -> isLocalIPv4(addr, cleanIpAddress)
            is Inet6Address -> isLocalIPv6(addr, cleanIpAddress)
            else -> false
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        false
    }
}

private fun parseEndpoint(endpoint: String): Pair<String, Int?> {
    // Handle IPv6 addresses with square brackets
    if (endpoint.startsWith("[")) {
        val closingBracketIndex = endpoint.lastIndexOf("]")
        if (closingBracketIndex != -1) {
            val ipv6Address = endpoint.substring(1, closingBracketIndex)
            val port = if (closingBracketIndex < endpoint.length - 1 && endpoint[closingBracketIndex + 1] == ':') {
                endpoint.substring(closingBracketIndex + 2).toIntOrNull()
            } else null
            return Pair(ipv6Address, port)
        }
    }

    // Handle compressed IPv6 addresses (like ::1)
    if (endpoint.contains("::") || endpoint.count { it == ':' } > 2) {
        return Pair(endpoint, null)
    }

    // Handle other cases (IPv4 or IPv6 without brackets)
    val parts = endpoint.split(":")
    return when {
        parts.size == 1 -> Pair(parts[0], null)
        parts.size == 2 -> Pair(parts[0], parts[1].toIntOrNull())
        else -> Pair(endpoint, null)
    }
}

private fun isLocalIPv4(addr: Inet4Address, cleanIpAddress: String): Boolean {
    return addr.isLoopbackAddress ||
            addr.isLinkLocalAddress ||
            addr.isSiteLocalAddress ||
            cleanIpAddress.startsWith("169.254.") // IPv4 link-local
}

private fun isLocalIPv6(addr: Inet6Address, cleanIpAddress: String): Boolean {
    return addr.isLoopbackAddress ||
            addr.isLinkLocalAddress ||
            addr.isSiteLocalAddress ||
            cleanIpAddress == "0:0:0:0:0:0:0:1" || // Uncompressed ::1
            cleanIpAddress == "::1" || // Compressed ::1
            cleanIpAddress.lowercase().startsWith("fc") || // ULA
            cleanIpAddress.lowercase().startsWith("fd")    // ULA
}