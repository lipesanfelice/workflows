// src/main/java/org/example/web/controller/UploadController.java
package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.web.dto.ProcessamentoRequest;
import org.example.web.dto.ResultadoProcessamento;
import org.example.web.service.ProcessamentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.web.process.ProcessTracker;   // <--- importe

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final ProcessamentoService service;
    private final ProcessTracker tracker;        // <--- adicione

    public UploadController(ProcessamentoService service, ProcessTracker tracker) {
        this.service = service;
        this.tracker = tracker;
    }

    @PostMapping(value = "/codigo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultadoProcessamento> codigo(@Valid @RequestBody ProcessamentoRequest body) throws Exception {
        return ResponseEntity.ok(service.processarTrechoCodigo(body.getCodigo()));
    }

    @PostMapping(value = "/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> arquivo(@RequestPart("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(service.processarArquivo(file));
    }

    @PostMapping(value = "/projeto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> projeto(@RequestPart("file") MultipartFile zip) throws Exception {
        return ResponseEntity.ok(service.processarProjetoZip(zip));
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
