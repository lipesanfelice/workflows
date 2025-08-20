package org.example.web.controller;

import org.example.web.service.EntradaUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/entrada")
public class EntradaUsuarioController {

    @Autowired
    private EntradaUsuarioService entradaUsuarioService;

    @PostMapping("/codigo")
    @ResponseBody
    public String salvarCodigo(@RequestParam("codigo") String codigo) {
        entradaUsuarioService.salvarCodigo(codigo); // agora só passa o código, como esperado
        return "Código recebido com sucesso!";
    }

    @PostMapping("/arquivo")
    @ResponseBody
    public String salvarArquivo(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            return "Nenhum arquivo enviado.";
        }

        File arquivoConvertido = new File(System.getProperty("java.io.tmpdir") + "/" + arquivo.getOriginalFilename());
        arquivo.transferTo(arquivoConvertido);

        entradaUsuarioService.salvarArquivo(arquivoConvertido); // agora passa um File
        return "Arquivo recebido com sucesso!";
    }

    @PostMapping("/projeto")
    @ResponseBody
    public String salvarProjetoZip(@RequestParam("projetoZip") MultipartFile projetoZip) throws IOException {
        if (projetoZip.isEmpty()) {
            return "Nenhum projeto zip enviado.";
        }

        File zipConvertido = new File(System.getProperty("java.io.tmpdir") + "/" + projetoZip.getOriginalFilename());
        projetoZip.transferTo(zipConvertido);

        entradaUsuarioService.salvarProjetoZip(zipConvertido); // agora passa um File
        return "Projeto zip recebido com sucesso!";
    }
}
