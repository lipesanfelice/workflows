package org.example.web.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class EntradaUsuarioService {

    private static final String PASTA_ENTRADA = "entrada-usuario";

    public EntradaUsuarioService() throws IOException {
        inicializarPasta();
    }

    private void inicializarPasta() throws IOException {
        Path pasta = Paths.get(PASTA_ENTRADA);
        if (!Files.exists(pasta)) {
            Files.createDirectories(pasta);
        }
    }

    public void limparPastaEntrada() throws IOException {
        Path pasta = Paths.get(PASTA_ENTRADA);
        if (Files.exists(pasta)) {
            Files.walk(pasta)
                    .sorted((a, b) -> b.compareTo(a)) // primeiro arquivos, depois diretórios
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(pasta);
    }

    public void salvarCodigo(String codigo) throws IOException {
        limparPastaEntrada();
        Path arquivo = Paths.get(PASTA_ENTRADA, "EntradaUsuario.java");
        Files.writeString(arquivo, codigo);
        comitarEntrada();
    }

    public void salvarArquivo(File arquivo) throws IOException {
        limparPastaEntrada();
        Path destino = Paths.get(PASTA_ENTRADA, arquivo.getName());
        Files.copy(arquivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        comitarEntrada();
    }

    public void salvarProjetoZip(File zipFile) throws IOException {
        limparPastaEntrada();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                    Path destino = Paths.get(PASTA_ENTRADA, Paths.get(entry.getName()).getFileName().toString());
                    Files.copy(zis, destino, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        comitarEntrada();
    }

    private void comitarEntrada() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "git", "add", PASTA_ENTRADA
            );
            pb.inheritIO().start().waitFor();

            pb = new ProcessBuilder(
                    "git", "commit", "-m", "Nova entrada do usuário"
            );
            pb.inheritIO().start().waitFor();

            pb = new ProcessBuilder(
                    "git", "push"
            );
            pb.inheritIO().start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
