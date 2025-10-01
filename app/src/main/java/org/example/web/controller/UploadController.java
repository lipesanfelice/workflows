package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.web.dto.ProcessamentoRequest;
import org.example.web.dto.ResultadoProcessamento;
import org.example.web.service.ProcessamentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.web.process.ProcessTracker; // << pacote padronizado

import java.util.Map; // << faltava

@RestController
@RequestMapping("/api")
public class UploadController {

    private final ProcessamentoService service;
    private final ProcessTracker tracker;

    public UploadController(ProcessamentoService service, ProcessTracker tracker) {
        this.service = service;
        this.tracker = tracker;
    }

    /** Endpoint que o loading.html consulta */
    @GetMapping("/process/running")
    public Map<String, Object> status() {
        return Map.of(
            "running", tracker.isRunning(),
            "ok",      tracker.wasLastOk(),
            "lastChangeAt", tracker.lastChangeAt()
        );
    }

    @PostMapping(value = "/codigo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultadoProcessamento> codigo(@Valid @RequestBody ProcessamentoRequest body) throws Exception {
        tracker.markStarted();                 // liga o “estamos rodando”
        ResultadoProcessamento r = service.processarTrechoCodigo(body.getCodigo());
        // NÃO desliga aqui! (pipeline real segue no GitHub Actions)
        return ResponseEntity.ok(r);
    }

    @PostMapping(value = "/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> arquivo(@RequestPart("file") MultipartFile file) throws Exception {
        tracker.markStarted();
        ResultadoProcessamento r = service.processarArquivo(file);
        return ResponseEntity.ok(r);
    }

    @PostMapping(value = "/projeto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> projeto(@RequestPart("file") MultipartFile zip) throws Exception {
        tracker.markStarted();
        ResultadoProcessamento r = service.processarProjetoZip(zip);
        return ResponseEntity.ok(r);
    }


    @GetMapping("/health")
    public String health() { return "ok"; }
}
