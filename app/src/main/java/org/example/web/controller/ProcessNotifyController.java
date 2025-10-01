package org.example.web.controller;

import org.example.web.process.ProcessTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook simples para o GitHub Actions avisar término da pipeline.
 * Chame com: POST /api/process/notify { "status": "success" | "error" }
 * (Opcional: adicione um token simples via header para segurança)
 */
@RestController
@RequestMapping("/api/process")
public class ProcessNotifyController {

    private final ProcessTracker tracker;

    public ProcessNotifyController(ProcessTracker tracker) {
        this.tracker = tracker;
    }

    public static class NotifyBody {
        public String status;
    }

    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> notifyFinish(@RequestBody NotifyBody body) {
        String status = body != null ? String.valueOf(body.status).toLowerCase() : "";
        switch (status) {
            case "success":
            case "ok":
            case "completed":
                tracker.markSuccess();
                break;
            case "error":
            case "failure":
            case "failed":
                tracker.markError();
                break;
            default:
                // se vier vazio, não mudo nada para evitar falso negativo
                return ResponseEntity.badRequest().body(Map.of(
                    "accepted", false,
                    "reason", "status must be success|error"
                ));
        }
        return ResponseEntity.ok(Map.of(
            "accepted", true,
            "running", tracker.isRunning(),
            "ok", tracker.wasLastOk(),
            "lastChangeAt", tracker.lastChangeAt()
        ));
    }
}
