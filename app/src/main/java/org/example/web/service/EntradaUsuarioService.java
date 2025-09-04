package org.example.web.service;

import org.example.web.git.GitServico;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.Map;
import java.io.*;
import java.util.zip.*;

@Service
public class EntradaUsuarioService {

    @Value("${app.entrada.diretorio:entrada-usuario}")
    private String diretorioEntrada;

    @Value("${app.entrada.repositorio-git:https://github.com/lipesanfelice/workflows.git}")
    private String repositorioGit;

    @Value("${app.entrada.mensagem-commit:Atualizar entrada do usuário}")
    private String mensagemCommit;

    public Path salvarArquivos(Map<String, String> arquivosRelativosParaConteudo) {
        try {
            Path base = resolverBase();
            for (var e : arquivosRelativosParaConteudo.entrySet()) {
                Path alvo = base.resolve(e.getKey());
                Files.createDirectories(alvo.getParent());
                Files.writeString(alvo, e.getValue());
            }
            enviarParaRepositorio(base.getParent());
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Path salvarConteudo(String caminhoRelativo, String conteudo) {
        try {
            Path base = resolverBase();
            Path alvo = base.resolve(caminhoRelativo);
            Files.createDirectories(alvo.getParent());
            Files.writeString(alvo, conteudo);
            enviarParaRepositorio(base.getParent());
            return alvo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Path salvarCodigo(String codigo) {
        String nome = "java/CodigoUsuario.java";
        return salvarConteudo(nome, codigo);
    }

    public Path salvarArquivo(File arquivo) {
        try {
            Path base = resolverBase();
            Path destino = base.resolve(arquivo.getName());
            Files.createDirectories(destino.getParent());
            Files.copy(arquivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            enviarParaRepositorio(base.getParent());
            return destino;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Path salvarProjetoZip(File zip) {
        try {
            Path base = resolverBase();
            unzip(zip, base);
            enviarParaRepositorio(base.getParent());
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path resolverBase() {
        Path base = Path.of(diretorioEntrada);
        if (!base.isAbsolute()) {
            Path cand = Path.of("app").resolve(diretorioEntrada);
            base = Files.exists(cand.getParent()) ? cand : base;
        }
        try {
            Files.createDirectories(base);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return base.resolve("");
    }

    private void enviarParaRepositorio(Path pastaEntrada) {
        File clone = Paths.get(System.getProperty("java.io.tmpdir"), "repo-workflows").toFile();
        GitServico git = new GitServico(clone, repositorioGit);
        git.garantirClone();
        git.sincronizarMain();
        try {
            Path destino = clone.toPath().resolve("app").resolve("entrada-usuario");
            Files.createDirectories(destino);
            try (var walk = Files.walk(pastaEntrada)) {
                walk.forEach(p -> {
                    try {
                        Path rel = pastaEntrada.relativize(p);
                        Path alvo = destino.resolve(rel.toString());
                        if (Files.isDirectory(p)) {
                            Files.createDirectories(alvo);
                        } else {
                            Files.createDirectories(alvo.getParent());
                            Files.copy(p, alvo, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        git.configurarIdentidade("github-actions[bot]", "github-actions[bot]@users.noreply.github.com");
        git.adicionarCommitarEmpurrar("app/entrada-usuario", mensagemCommit);
    }

    private static void unzip(File zipFile, Path targetBase) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path alvo = targetBase.resolve(entry.getName()).normalize();
                if (!alvo.startsWith(targetBase)) {
                    zis.closeEntry();
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(alvo);
                } else {
                    Files.createDirectories(alvo.getParent());
                    try (OutputStream os = Files.newOutputStream(alvo, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        zis.transferTo(os);
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
