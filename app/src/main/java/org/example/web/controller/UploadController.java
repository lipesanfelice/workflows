package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.web.dto.ProcessamentoRequest;
import org.example.web.dto.ResultadoProcessamento;
import org.example.web.service.ProcessamentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.web.process.ProcessTracker;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final ProcessamentoService service;
    private final ProcessTracker tracker;

    public UploadController(ProcessamentoService service, ProcessTracker tracker) {
        this.service = service;
        this.tracker = tracker;
    }

    /** Submissão de trecho de código JSON */
    @PostMapping(value = "/codigo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultadoProcessamento> codigo(@Valid @RequestBody ProcessamentoRequest body) throws Exception {
        // marca que o processo foi iniciado (loading mostra “em execução”)
        tracker.markStarted();

        ResultadoProcessamento res = service.processarTrechoCodigo(body.getCodigo());
        // Atenção: NÃO marcar success aqui; quem fecha é o /api/process/notify

        return ResponseEntity.ok(res);
    }

    /** Submissão de um arquivo .java */
    @PostMapping(value = "/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> arquivo(@RequestPart("file") MultipartFile file) throws Exception {
        tracker.markStarted();

        ResultadoProcessamento res = service.processarArquivo(file);

        return ResponseEntity.ok(res);
    }

    /** Submissão de um projeto .zip */
    @PostMapping(value = "/projeto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> projeto(@RequestPart("file") MultipartFile zip) throws Exception {
        tracker.markStarted();

        ResultadoProcessamento res = service.processarProjetoZip(zip);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/health")
    public String health() { return "ok"; }
}
