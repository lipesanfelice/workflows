package org.example.web.process;

import org.springframework.stereotype.Component;

@Component
public class ProcessTracker {
    private volatile boolean started = false;
    private volatile boolean done = false;
    private volatile boolean error = false;
    private volatile String  status = "Aguardando…";

    public synchronized void markStarted() {
        this.started = true;
        this.done = false;
        this.error = false;
        this.status = "Pipeline em execução…";
    }
    public synchronized void markSuccess() {
        this.done = true;
        this.error = false;
        this.status = "Concluído";
    }
    public synchronized void markError() {
        this.done = true;
        this.error = true;
        this.status = "Falhou";
    }
    public synchronized void reset() {
        this.started = false;
        this.done = false;
        this.error = false;
        this.status = "Aguardando…";
    }

    public boolean isStarted() { return started; }
    public boolean isDone()     { return done; }
    public boolean hasError()   { return error; }
    public String  getStatus()  { return status; }
}
