package org.example.web.exec;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProcessTracker {
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean lastOk  = new AtomicBoolean(false);
  private final AtomicLong lastChange = new AtomicLong(0L);

  public void markStarted() {
    running.set(true);
    lastChange.set(System.currentTimeMillis());
  }
  public void markSuccess() {
    lastOk.set(true);
    running.set(false);
    lastChange.set(System.currentTimeMillis());
  }
  public void markError() {
    lastOk.set(false);
    running.set(false);
    lastChange.set(System.currentTimeMillis());
  }

  public boolean isRunning() { return running.get(); }
  public boolean wasLastOk() { return lastOk.get(); }
  public long lastChangeAt() { return lastChange.get(); }
}
