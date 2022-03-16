package test_utils;

import java.util.concurrent.TimeUnit;

public final class Await {

    private Await() { // hide
    }

    public static void await(Condition condition, int timeoutMillis) {
        final long startTime = System.currentTimeMillis();
        do {
            if (condition.test()) {
                return;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ignore) {
            }
        } while (System.currentTimeMillis() - startTime < timeoutMillis);
    }

    @FunctionalInterface
    public interface Condition {
        boolean test();
    }
}
