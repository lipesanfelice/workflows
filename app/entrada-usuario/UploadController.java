package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.web.dto.ProcessamentoRequest;
import org.example.web.dto.ResultadoProcessamento;
import org.example.web.service.ProcessamentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final ProcessamentoService service;

    public UploadController(ProcessamentoService service) {
        this.service = service;
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

    @GetMapping("/health")
    public String health() { return "ok"; }
}
