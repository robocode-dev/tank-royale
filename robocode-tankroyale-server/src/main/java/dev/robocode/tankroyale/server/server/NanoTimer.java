package dev.robocode.tankroyale.server.server;

import java.util.concurrent.atomic.AtomicBoolean;

public class NanoTimer {

  private final Runnable job;
  private final long periodInNanos;
  private Thread thread;
  private final AtomicBoolean isRunning = new AtomicBoolean();
  private final AtomicBoolean isPaused = new AtomicBoolean();

  public NanoTimer(long periodInNanos, Runnable job) {
    this.periodInNanos = periodInNanos;
    this.job = job;
  }

  public void start() {
    thread = new Thread(NanoTimer.this::run);
    isRunning.set(true);
    thread.start();
  }

  public void stop() {
    isRunning.set(false);
    thread.interrupt();
  }

  public void pause() {
    isPaused.set(true);
  }

  public void resume() {
    isPaused.set(false);
  }

  public void reset() {
    lastTime = System.nanoTime();
  }

  volatile long lastTime = System.nanoTime();

  public void run() {
    while (isRunning.get() && !thread.isInterrupted()) {
      long now = System.nanoTime();
      if ((now - lastTime) >= periodInNanos) {
        lastTime = now;
        if (!isPaused.get()) {
          job.run();
        }
      }
    }
    thread = null;
  }
}
