package dev.robocode.tankroyale.gui.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EndpointCheckerTest : FunSpec({

    context("isLocalEndpoint") {
        test("should identify local endpoints correctly") {
            val localEndpoints = listOf(
                "localhost",
                "localhost:8080",
                "127.0.0.1",
                "127.0.0.1:12345",
                "ws://127.0.0.1",
                "wss://127.0.0.1",
                "10.0.0.1",
                "192.168.1.1",
                "172.16.0.1",
                "::1",
                "[::1]:1",
                "fe80::1234:5678:9abc",
                "fc00::1",
                "fd00::1",
                "fe80:0:0:0:d4da:d362:c269:663f%ethernet_32769",
                "[fe80:0:0:0:d4da:d362:c269:663f%ethernet_32769]:7654",
                "ws://[fe80:0:0:0:d4da:d362:c269:663f%ethernet_32769]:7654",
            )

            localEndpoints.forEach { endpoint ->
                isLocalEndpoint(endpoint) shouldBe true
            }
        }

        test("should identify non-local endpoints correctly") {
            val nonLocalEndpoints = listOf(
                "example.com",
                "https://example.com",
                "8.8.8.8",
                "8.8.8.8:8",
                "2001:db8::1",
                "[2001:db8::1]:7913",
            )

            nonLocalEndpoints.forEach { endpoint ->
                isLocalEndpoint(endpoint) shouldBe false
            }
        }
    }
})