package dev.robocode.tankroyale.server.server;

import java.util.TimerTask;

public class Timer {

  private java.util.Timer timer;
  private final long delay;
  private final Runnable task;

  public Timer(long intervalMicroseconds, Runnable task) {
    this.delay = intervalMicroseconds / 1000;
    this.task = task;
  }

  public void start() {
    stop();
    timer = new java.util.Timer();
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            task.run();
          }
        },
        delay);
  }

  public void stop() {
    if (timer != null) {
      timer.cancel();
    }
    timer = null;
  }
}
