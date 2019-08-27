package dev.robocode.tankroyale.server.server;

import java.util.TimerTask;

@SuppressWarnings("unused")
class Timer {

  private java.util.Timer timer;
  private final long delay;
  private final Runnable task;

  Timer(long intervalMicroseconds, Runnable task) {
    this.delay = intervalMicroseconds / 1000;
    this.task = task;
  }

  void start() {
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

  void stop() {
    if (timer != null) {
      timer.cancel();
    }
    timer = null;
  }
}
