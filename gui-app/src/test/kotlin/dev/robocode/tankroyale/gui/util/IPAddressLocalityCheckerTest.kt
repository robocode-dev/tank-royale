package dev.robocode.tankroyale.gui.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class IPAddressLocalityCheckerTest : StringSpec({
    "isLocalAddress should return true for local addresses" {
        val localAddresses = listOf(
            "127.0.0.1",
            "10.0.0.1",
            "192.168.1.1",
            "172.16.0.1",
            "::1",
            "fe80::1234:5678:9abc",
            "fc00::1",
            "fd00::1",
            "fe80:0:0:0:d4da:d362:c269:663f%ethernet_32769",
        )
        localAddresses.forEach {
            isLocalAddress(it) shouldBe true
        }
    }

    "isLocalAddress should return false for non-local valid addresses" {
        val nonLocalAddresses = listOf(
            "8.8.8.8",
            "2001:db8::1",
        )
        nonLocalAddresses.forEach {
            isLocalAddress(it) shouldBe false
        }
    }

    "isLocalAddress should return false for invalid addresses" {
        val nonLocalAddresses = listOf(
            "1",
            "1.2",
            "1.2.3",
            "1.2.3:1",
            "1:2:3:4:5:6:7:8",
            "::",
            "::2",
        )
        nonLocalAddresses.forEach {
            isLocalAddress(it) shouldBe false
        }
    }
})