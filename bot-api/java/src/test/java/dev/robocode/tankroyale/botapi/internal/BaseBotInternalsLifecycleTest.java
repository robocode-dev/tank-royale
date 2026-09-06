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

import static org.assertj.core.api.Assertions.assertThat;

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
