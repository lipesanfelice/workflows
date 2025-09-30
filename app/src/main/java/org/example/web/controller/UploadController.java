package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.web.dto.ProcessamentoRequest;
import org.example.web.dto.ResultadoProcessamento;
import org.example.web.Exec.ExecRegistry; // <-- pacote correto em minúsculo
import org.example.web.service.ProcessamentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final ProcessamentoService service;
    private final ExecRegistry execRegistry;

    public UploadController(ProcessamentoService service, ExecRegistry execRegistry) {
        this.service = service;
        this.execRegistry = execRegistry;
    }

    @PostMapping(value = "/codigo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultadoProcessamento> codigo(@Valid @RequestBody ProcessamentoRequest body) throws Exception {
        ResultadoProcessamento res = service.processarTrechoCodigo(body.getCodigo());

        // vincula execId -> último SHA pushado por /entrada/*
        if (res != null && res.getIdExecucao() != null) {
            execRegistry.bindExecToLatest(res.getIdExecucao());
        }

        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> arquivo(@RequestPart("file") MultipartFile file) throws Exception {
        ResultadoProcessamento res = service.processarArquivo(file);

        // vincula execId -> último SHA pushado por /entrada/*
        if (res != null && res.getIdExecucao() != null) {
            execRegistry.bindExecToLatest(res.getIdExecucao());
        }

        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/projeto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoProcessamento> projeto(@RequestPart("file") MultipartFile zip) throws Exception {
        ResultadoProcessamento res = service.processarProjetoZip(zip);

        // vincula execId -> último SHA pushado por /entrada/*
        if (res != null && res.getIdExecucao() != null) {
            execRegistry.bindExecToLatest(res.getIdExecucao());
        }

        return ResponseEntity.ok(res);
    }

    @GetMapping("/health")
    public String health() { 
        return "ok"; 
    }
}
