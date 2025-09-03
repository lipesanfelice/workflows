package org.example.web.controller;

import org.example.web.service.EntradaUsuarioService;
import org.example.web.service.ProcessamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/entrada")
public class EntradaUsuarioController {

    private final EntradaUsuarioService entradaUsuarioService;
    private final ProcessamentoService processamentoService;

    public EntradaUsuarioController(EntradaUsuarioService entradaUsuarioService, 
                                  ProcessamentoService processamentoService) {
        this.entradaUsuarioService = entradaUsuarioService;
        this.processamentoService = processamentoService;
    }

    @PostMapping("/codigo")
    public ResponseEntity<String> salvarCodigo(@RequestParam("codigo") String codigo) throws IOException {
        entradaUsuarioService.salvarCodigo(codigo);
        return ResponseEntity.ok("Código salvo no GitHub com sucesso!");
    }

    @PostMapping("/arquivo")
    public ResponseEntity<String> salvarArquivo(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum arquivo enviado.");
        }

        File arquivoConvertido = new File(System.getProperty("java.io.tmpdir") + "/" + arquivo.getOriginalFilename());
        arquivo.transferTo(arquivoConvertido);

        entradaUsuarioService.salvarArquivo(arquivoConvertido);
        return ResponseEntity.ok("Arquivo salvo no GitHub com sucesso!");
    }

    @PostMapping("/projeto")
    public ResponseEntity<String> salvarProjetoZip(@RequestParam("projetoZip") MultipartFile projetoZip) throws IOException {
        if (projetoZip.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum projeto zip enviado.");
        }

        File zipConvertido = new File(System.getProperty("java.io.tmpdir") + "/" + projetoZip.getOriginalFilename());
        projetoZip.transferTo(zipConvertido);

        entradaUsuarioService.salvarProjetoZip(zipConvertido);
        return ResponseEntity.ok("Projeto zip salvo no GitHub com sucesso!");
    }
}