package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.IBot;
import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.events.TickEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseBotInternalsLifecycleTest {

    @Test
    void stale_bot_thread_cannot_dispatch_after_next_round_takes_ownership() throws Exception {
        var internals = new BaseBotInternals(proxy(IBaseBot.class), botInfo(), null, null);
        internals.setTickEvent(new TickEvent(1, 1, null, List.of(), List.of()));

        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var staleDispatchBlocked = new AtomicBoolean(false);
        var oldBot = botProxy((method, args) -> {
            if (method.getName().equals("run")) {
                entered.countDown();
                while (release.getCount() != 0) {
                    try {
                        release.await(20, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignored) {
                        // Keep the legacy bot alive until the test releases it.
                    }
                }
                try {
                    internals.dispatchEvents(1);
                } catch (ThreadInterruptedException expected) {
                    staleDispatchBlocked.set(true);
                }
            }
            return defaultValue(method.getReturnType());
        });

        internals.startThread(oldBot);
        assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();

        internals.stopThread();
        internals.startThread(botProxy((method, args) -> defaultValue(method.getReturnType())));
        long runningDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (!internals.isRunning() && System.nanoTime() < runningDeadline) {
            Thread.sleep(5);
        }
        release.countDown();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (!staleDispatchBlocked.get() && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        internals.stopThread();

        assertThat(staleDispatchBlocked).isTrue();
    }

    @Test
    void final_tick_events_are_flushed_after_the_bot_thread_loses_ownership() {
        var dispatchedTicks = new AtomicInteger();
        var baseBot = (IBaseBot) Proxy.newProxyInstance(
                IBaseBot.class.getClassLoader(),
                new Class<?>[]{IBaseBot.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("onTick")) {
                        dispatchedTicks.incrementAndGet();
                    }
                    return defaultValue(method.getReturnType());
                });

        var internals = new BaseBotInternals(baseBot, botInfo(), null, null);
        var tick = new TickEvent(1, 1, null, List.of(), List.of());
        internals.setTickEvent(tick);
        internals.addEventsFromTick(tick);

        // stopThread() invalidates bot-thread ownership, so the bot thread can no longer drain
        // the queue itself. The WebSocket thread must still be able to — this is what the
        // game-ended, game-aborted and disconnected paths rely on.
        internals.stopThread();
        internals.flushFinalTurnEvents();

        assertThat(dispatchedTicks).hasValue(1);
    }

    @Test
    void wait_for_next_turn_unwinds_when_no_thread_owns_the_round() throws Exception {
        var internals = new BaseBotInternals(proxy(IBaseBot.class), botInfo(), null, null);
        internals.setTickEvent(new TickEvent(1, 1, null, List.of(), List.of()));

        // A BaseBot with no run() loop owns no thread, so the blocking wait unwinds right after
        // the intent was sent by execute(). Mirrored by the .NET and Python lifecycle tests.
        var waitForNextTurn = BaseBotInternals.class.getDeclaredMethod("waitForNextTurn", int.class);
        waitForNextTurn.setAccessible(true);

        assertThatThrownBy(() -> waitForNextTurn.invoke(internals, 1))
                .hasCauseInstanceOf(ThreadInterruptedException.class);
    }

    @Test
    void unexpected_error_from_run_still_drains_final_turn_events() throws Exception {
        var dispatchedTicks = new AtomicInteger();
        var baseBot = (IBaseBot) Proxy.newProxyInstance(
                IBaseBot.class.getClassLoader(),
                new Class<?>[]{IBaseBot.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("onTick")) {
                        dispatchedTicks.incrementAndGet();
                    }
                    return defaultValue(method.getReturnType());
                });

        var internals = new BaseBotInternals(baseBot, botInfo(), null, null);
        var tick = new TickEvent(1, 1, null, List.of(), List.of());
        internals.setTickEvent(tick);
        internals.addEventsFromTick(tick);

        // run() blowing up must not cost the bot its final-turn events.
        internals.startThread(botProxy((method, args) -> {
            if (method.getName().equals("run")) {
                throw new IllegalStateException("boom");
            }
            // End the post-run() pre-warm loop on the first go(), the way a stopped bot does.
            // Letting it spin burns CPU and allocates for the whole wait below.
            if (method.getName().equals("go")) {
                throw new ThreadInterruptedException();
            }
            return defaultValue(method.getReturnType());
        }));

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (dispatchedTicks.get() == 0 && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        internals.stopThread();

        assertThat(dispatchedTicks.get()).isGreaterThanOrEqualTo(1);
    }

    private static BotInfo botInfo() {
        return BotInfo.builder()
                .setName("LifecycleTest")
                .setVersion("1.0")
                .addAuthor("Test")
                .addGameType("classic")
                .build();
    }

    private static <T> T proxy(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> defaultValue(method.getReturnType())));
    }

    private static IBot botProxy(Invocation invocation) {
        return IBot.class.cast(Proxy.newProxyInstance(
                IBot.class.getClassLoader(),
                new Class<?>[]{IBot.class},
                (proxy, method, args) -> invocation.call(method, args)));
    }

    private interface Invocation {
        Object call(java.lang.reflect.Method method, Object[] args);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }
}
