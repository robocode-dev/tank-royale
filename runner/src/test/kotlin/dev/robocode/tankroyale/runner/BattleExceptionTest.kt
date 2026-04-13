package dev.robocode.tankroyale.runner

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * Unit tests for [BattleException] and error scenario preconditions.
 */
class BattleExceptionTest {

    @Test
    fun `BattleException is a RuntimeException`() {
        val ex = BattleException("test")
        assertThat(ex).isInstanceOf(RuntimeException::class.java)
        assertThat(ex.message).isEqualTo("test")
        assertThat(ex.cause).isNull()
    }

    @Test
    fun `BattleException with cause`() {
        val cause = IllegalStateException("root")
        val ex = BattleException("wrapper", cause)
        assertThat(ex.message).isEqualTo("wrapper")
        assertThat(ex.cause).isSameAs(cause)
    }
}
