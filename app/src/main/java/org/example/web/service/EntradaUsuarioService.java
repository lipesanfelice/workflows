package org.example.web.service;

import org.example.web.git.GitServico;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.Map;
import java.io.*;
import java.util.zip.*;
import java.util.Comparator;

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
            limparConteudo(base);
            for (var e : arquivosRelativosParaConteudo.entrySet()) {
                Path alvo = base.resolve(e.getKey());
                if (!alvo.toString().endsWith(".java")) continue; // só .java
                Files.createDirectories(alvo.getParent());
                Files.writeString(alvo, e.getValue());
            }
            enviarParaRepositorio(base);
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Path salvarConteudo(String caminhoRelativo, String conteudo) {
        try {
            Path base = resolverBase();
            limparConteudo(base);
            if (!caminhoRelativo.endsWith(".java")) {
                caminhoRelativo = caminhoRelativo + ".java";
            }
            Path alvo = base.resolve(caminhoRelativo);
            Files.createDirectories(alvo.getParent());
            Files.writeString(alvo, conteudo);
            enviarParaRepositorio(base);
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
            limparConteudo(base);
            if (arquivo.getName().endsWith(".java")) {
                Path destino = base.resolve(arquivo.getName());
                Files.createDirectories(destino.getParent());
                Files.copy(arquivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            }
            enviarParaRepositorio(base);
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Path salvarProjetoZip(File zip) {
        try {
            Path base = resolverBase();
            limparConteudo(base);
            unzipSomenteJava(zip, base); // <-- apenas .java entram aqui
            enviarParaRepositorio(base);
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path resolverBase() {
        try {
            Path base = Path.of(diretorioEntrada);
            if (!base.isAbsolute()) {
                Path cwd = Path.of("").toAbsolutePath();
                Path candApp = cwd.resolve("app").resolve(diretorioEntrada);
                Path candAqui = cwd.resolve(diretorioEntrada);
                base = Files.exists(candApp.getParent()) ? candApp : candAqui;
            }
            Files.createDirectories(base);
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void enviarParaRepositorio(Path pastaEntrada) {
        if (pastaEntrada == null) throw new IllegalArgumentException("pastaEntrada nula");

        File clone = Paths.get(System.getProperty("java.io.tmpdir"), "repo-workflows").toFile();
        GitServico git = new GitServico(clone, repositorioGit);
        git.garantirClone();
        git.sincronizarMain();

        try {
            Path destino = clone.toPath().resolve("app").resolve("entrada-usuario");
            Files.createDirectories(destino);

            // 1) limpa tudo no clone
            limparConteudo(destino);

            // 2) copia SOMENTE arquivos .java do envio atual
            try (var walk = Files.walk(pastaEntrada)) {
                walk.forEach(p -> {
                    try {
                        if (Files.isDirectory(p)) return;
                        if (!p.toString().endsWith(".java")) return; // só .java
                        Path rel = pastaEntrada.relativize(p);
                        Path alvo = destino.resolve(rel.toString()).normalize();
                        Files.createDirectories(alvo.getParent());
                        Files.copy(p, alvo, StandardCopyOption.REPLACE_EXISTING);
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

    private static void limparConteudo(Path dir) {
        if (dir == null || !Files.exists(dir)) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                .filter(p -> !p.equals(dir))
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
        } catch (IOException ignored) {}
    }

    private static void unzipSomenteJava(File zipFile, Path targetBase) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String nome = entry.getName();
                if (entry.isDirectory()) { zis.closeEntry(); continue; }
                if (nome == null || !nome.endsWith(".java")) { zis.closeEntry(); continue; } // só .java
                Path alvo = targetBase.resolve(nome).normalize();
                if (!alvo.startsWith(targetBase)) { zis.closeEntry(); continue; }
                Files.createDirectories(alvo.getParent());
                try (OutputStream os = Files.newOutputStream(alvo, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    zis.transferTo(os);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
