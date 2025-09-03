package dev.robocode.tankroyale.gui.util

import java.net.InetAddress
import java.net.Inet6Address
import java.net.URI
import java.net.UnknownHostException

fun isRemoteEndpoint(endpoint: String) = !isLocalEndpoint(endpoint)

fun isLocalEndpoint(endpoint: String): Boolean {
    val cleanEndpoint = endpoint.lowercase().trim()

    // Handle special cases
    if (cleanEndpoint == "localhost" || cleanEndpoint.startsWith("localhost:")) {
        return true
    }

    // Try to parse as URI first
    val uri = try {
        URI(cleanEndpoint)
    } catch (_: Exception) {
        // If it's not a valid URI, try to parse it as an IP address or hostname
        return isLocalIpAddressWithOptionalPort(cleanEndpoint)
    }

    // Extract host from URI
    val host = uri.host ?: return isLocalIpAddressWithOptionalPort(cleanEndpoint)

    return isLocalIpAddressWithOptionalPort(host)
}

private fun isLocalIpAddressWithOptionalPort(ipWithOptionalPort: String): Boolean {
    val parts = ipWithOptionalPort.split(":")
    val ip = when {
        parts.size == 2 -> parts[0] // IPv4 with port
        ipWithOptionalPort.startsWith("[") && ipWithOptionalPort.contains("]:") ->
            ipWithOptionalPort.substringBefore("]").substring(1) // IPv6 with port
        else -> ipWithOptionalPort // IP without port
    }
    return isLocalIpAddress(ip)
}

private fun isLocalIpAddress(ip: String): Boolean =
    try {
        val strippedIp = ip.split("%")[0].replace("[", "").replace("]", "")
        val addr = InetAddress.getByName(strippedIp)
        when {
            addr.isLoopbackAddress -> true
            addr.isSiteLocalAddress -> true
            addr.isLinkLocalAddress -> true
            addr.isAnyLocalAddress -> true
            addr is Inet6Address -> isLocalIpv6(addr)
            else -> false
        }
    } catch (_: UnknownHostException) {
        false // If we can't resolve the host, assume it's not local
    }

private fun isLocalIpv6(addr: Inet6Address): Boolean {
    val bytes = addr.address
    // Check for fc00::/7 (Unique Local IPv6 Unicast Addresses)
    return (bytes[0].toInt() and 0xfe) == 0xfc
}
