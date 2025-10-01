package org.example.web.controller;

import org.example.web.process.ProcessTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/process")
public class ProcessNotifyController {

    private final ProcessTracker tracker;

    public ProcessNotifyController(ProcessTracker tracker) {
        this.tracker = tracker;
    }

    /** Loading consulta isso periodicamente */
    @GetMapping("/state")
    public Map<String, Object> state() {
        return Map.of(
                "started", tracker.isStarted(),
                "done",    tracker.isDone(),
                "error",   tracker.hasError(),
                "status",  tracker.getStatus()
        );
    }

    /** Chame antes de cada submissão (no index.html) para limpar estado anterior */
    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        tracker.reset();
        return ResponseEntity.ok().build();
    }

    /** Chame quando a pipeline terminar: body {"status":"success"} ou {"status":"error"} */
    @PostMapping("/notify")
    public ResponseEntity<Void> notifyDone(@RequestBody Map<String, String> body) {
        String status = (body.getOrDefault("status", "") + "").toLowerCase();
        if ("success".equals(status)) tracker.markSuccess();
        else                          tracker.markError();
        return ResponseEntity.ok().build();
    }
}
