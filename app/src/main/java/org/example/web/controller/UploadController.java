package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.web.dto.ProcessamentoRequest;
import org.example.web.dto.ResultadoProcessamento;
import org.example.web.service.ProcessamentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.web.exec.ProcessTracker; // <— importe

@RestController
@RequestMapping("/api")
public class UploadController {

    private final ProcessamentoService service;
    private final ProcessTracker tracker; // <— injete

    public UploadController(ProcessamentoService service, ProcessTracker tracker) {
        this.service = service;
        this.tracker = tracker;
    }

    // ===== endpoint de status bem simples =====
    @GetMapping("/process/running")
    public ResponseEntity<?> running() {
        // você pode incluir mais campos se quiser
        return ResponseEntity.ok(new java.util.HashMap<>() {{
            put("running", tracker.isRunning());
            put("ok", tracker.wasLastOk());
            put("lastChangeAt", tracker.lastChangeAt());
        }});
    }

    // ===== seus endpoints, agora marcando início/fim =====
    @PostMapping(value = "/codigo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultadoProcessamento> codigo(@Valid @RequestBody ProcessamentoRequest body) throws Exception {
        tracker.markStarted();
        try {
            ResultadoProcessamento r = service.processarTrechoCodigo(body.getCodigo());
            tracker.markSuccess();
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            tracker.markError();
            throw e;
        }
    }

    @PostMapping(value = "/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> arquivo(@RequestPart("file") MultipartFile file) throws Exception {
        tracker.markStarted();
        try {
            ResultadoProcessamento r = service.processarArquivo(file);
            tracker.markSuccess();
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            tracker.markError();
            throw e;
        }
    }

    @PostMapping(value = "/projeto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> projeto(@RequestPart("file") MultipartFile zip) throws Exception {
        tracker.markStarted();
        try {
            ResultadoProcessamento r = service.processarProjetoZip(zip);
            tracker.markSuccess();
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            tracker.markError();
            throw e;
        }
    }

    // Endpoint que o loading.html consulta sem execId
    @GetMapping("/process/status")
    public Map<String, Object> status() {
        return Map.of(
            "running", tracker.isRunning(),
            "done",    tracker.isDone(),
            "error",   tracker.hasError(),
            "status",  tracker.getStatus()
        );
    }

    @GetMapping("/health")
    public String health() { return "ok"; }
}
