package org.example.web.process;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Flag global bem simples para o loading.html:
 * - markStarted(): pipeline começou
 * - markSuccess()/markError(): pipeline terminou (com sucesso/erro)
 * - isRunning(): usado pelo front; quando false, redireciona para results.html
 */
@Service
public class ProcessTracker {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean lastOk  = new AtomicBoolean(false);
    private final AtomicLong lastChange = new AtomicLong(0L);

    public void markStarted() {
        running.set(true);
        lastChange.set(System.currentTimeMillis());
        System.out.println("[ProcessTracker] markStarted()");
    }

    public void markSuccess() {
        lastOk.set(true);
        running.set(false);
        lastChange.set(System.currentTimeMillis());
        System.out.println("[ProcessTracker] markSuccess()");
    }

    public void markError() {
        lastOk.set(false);
        running.set(false);
        lastChange.set(System.currentTimeMillis());
        System.out.println("[ProcessTracker] markError()");
    }

    /** Front usa este! */
    public boolean isRunning() { return running.get(); }

    /** Opcional para debug/telemetria */
    public boolean wasLastOk() { return lastOk.get(); }

    /** Opcional para debug/telemetria */
    public long lastChangeAt() { return lastChange.get(); }
}
