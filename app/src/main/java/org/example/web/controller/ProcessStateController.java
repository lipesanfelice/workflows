// src/main/java/org/example/web/controller/ProcessStateController.java
package org.example.web.controller;

import org.example.web.process.ProcessTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/process")
public class ProcessStateController {

    private final ProcessTracker tracker;

    public ProcessStateController(ProcessTracker tracker) {
        this.tracker = tracker;
    }

    @GetMapping("/state")
    public Map<String,Object> state() {
        return Map.of(
            "started", tracker.isStarted(),
            "done",    tracker.isDone(),
            "error",   tracker.hasError(),
            "status",  tracker.getStatus()
        );
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        tracker.reset();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify")
    public ResponseEntity<Void> notifyDone(@RequestBody Map<String,String> body) {
        String status = (body.getOrDefault("status","") + "").toLowerCase();
        if ("success".equals(status)) tracker.markSuccess();
        else                           tracker.markError();
        return ResponseEntity.ok().build();
    }
}
