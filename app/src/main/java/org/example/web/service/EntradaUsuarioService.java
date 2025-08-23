package org.example.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class EntradaUsuarioService {

    private final String pastaEntrada;
    private final String repositorioGit;
    private final String mensagemCommit;

    public EntradaUsuarioService(
            @Value("${app.entrada.diretorio:entrada-usuario}") String pastaEntrada,
            @Value("${app.entrada.repositorio-git:https://github.com/lipesanfelice/workflows.git}") String repositorioGit,
            @Value("${app.entrada.mensagem-commit:Nova entrada do usuario}") String mensagemCommit) throws IOException {
        
        this.pastaEntrada = pastaEntrada;
        this.repositorioGit = repositorioGit;
        this.mensagemCommit = mensagemCommit;
        
        inicializarPasta();
    }

    private void inicializarPasta() throws IOException {
        Path pasta = Paths.get(pastaEntrada);
        if (!Files.exists(pasta)) {
            Files.createDirectories(pasta);
        }
    }

    public void limparPastaEntrada() throws IOException {
        Path pasta = Paths.get(pastaEntrada);
        if (Files.exists(pasta)) {
            Files.walk(pasta)
                    .sorted((a, b) -> b.compareTo(a))
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(pasta);
    }

    public void salvarCodigo(String codigo) throws IOException {
        limparPastaEntrada();
        Path arquivo = Paths.get(pastaEntrada, "EntradaUsuario.java");
        Files.writeString(arquivo, codigo);
        comitarEntrada();
    }

    public void salvarArquivo(File arquivo) throws IOException {
        limparPastaEntrada();
        Path destino = Paths.get(pastaEntrada, arquivo.getName());
        Files.copy(arquivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        comitarEntrada();
    }

    public void salvarProjetoZip(File zipFile) throws IOException {
        limparPastaEntrada();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                    Path destino = Paths.get(pastaEntrada, Paths.get(entry.getName()).getFileName().toString());
                    Files.copy(zis, destino, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        comitarEntrada();
    }

    private void comitarEntrada() {
        try {
            // Configurar o repositório remoto se não estiver configurado
            ProcessBuilder pb = new ProcessBuilder(
                    "git", "remote", "get-url", "origin"
            );
            Process process = pb.start();
            if (process.waitFor() != 0) {
                // Se não tiver remote, adicionar
                pb = new ProcessBuilder(
                        "git", "remote", "add", "origin", repositorioGit
                );
                pb.inheritIO().start().waitFor();
            }

            // Fazer commit e push
            pb = new ProcessBuilder(
                    "git", "add", pastaEntrada
            );
            pb.inheritIO().start().waitFor();

            pb = new ProcessBuilder(
                    "git", "commit", "-m", mensagemCommit
            );
            pb.inheritIO().start().waitFor();

            pb = new ProcessBuilder(
                    "git", "push", "origin", "HEAD:main"
            );
            pb.inheritIO().start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters para acesso às configurações
    public String getPastaEntrada() {
        return pastaEntrada;
    }

    public String getRepositorioGit() {
        return repositorioGit;
    }

    public String getMensagemCommit() {
        return mensagemCommit;
    }
}