package dev.robocode.tankroyale.ui.desktop.util;

import java.net.InetAddress
import java.net.URI
import java.net.UnknownHostException

object InetAddressUtil {

    fun isLocalAddress(nameOrIpAddr: String): Boolean {
        return try {
            val inetAddr = InetAddress.getByName(nameOrIpAddr)
            inetAddr.isLoopbackAddress || inetAddr.isSiteLocalAddress || inetAddr.isLinkLocalAddress
        } catch (ex: UnknownHostException) {
            false
        }
    }
}
