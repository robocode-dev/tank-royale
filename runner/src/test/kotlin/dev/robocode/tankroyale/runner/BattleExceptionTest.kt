package dev.robocode.tankroyale.runner

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Unit tests for [BattleException] and error scenario preconditions.
 */
class BattleExceptionTest {

    @Tag("Unit")
    @Test
    fun `BattleException is a RuntimeException`() {
        val ex = BattleException("test")
        assertThat(ex).isInstanceOf(RuntimeException::class.java)
        assertThat(ex.message).isEqualTo("test")
        assertThat(ex.cause).isNull()
    }

    @Tag("Unit")
    @Test
    fun `BattleException with cause`() {
        val cause = IllegalStateException("root")
        val ex = BattleException("wrapper", cause)
        assertThat(ex.message).isEqualTo("wrapper")
        assertThat(ex.cause).isSameAs(cause)
    }
}
