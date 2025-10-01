// src/main/java/org/example/web/process/ProcessTracker.java
package org.example.web.process;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ProcessTracker {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean done    = new AtomicBoolean(false);
    private final AtomicBoolean error   = new AtomicBoolean(false);
    private volatile String status = "idle"; // "queued", "in_progress", "done", "error", etc.

    public void markStarted() {
        running.set(true);
        done.set(false);
        error.set(false);
        status = "in_progress";
    }

    public void markSuccess() {
        running.set(false);
        done.set(true);
        error.set(false);
        status = "done";
    }

    public void markError() {
        running.set(false);
        done.set(true);
        error.set(true);
        status = "error";
    }

    public boolean isRunning() { return running.get(); }
    public boolean isDone()    { return done.get(); }
    public boolean hasError()  { return error.get(); }
    public String  getStatus() { return status; }
}

