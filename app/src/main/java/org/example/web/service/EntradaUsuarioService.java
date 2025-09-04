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

    // ===== APIs usadas pelo controller =====

    public Path salvarArquivos(Map<String, String> arquivosRelativosParaConteudo) {
        try {
            Path base = resolverBase();
            limparConteudo(base);

            for (var e : arquivosRelativosParaConteudo.entrySet()) {
                String nome = soNome(e.getKey());
                if (nome == null || !nome.endsWith(".java")) continue; // só .java
                Path alvo = base.resolve(nome);
                Files.createDirectories(alvo.getParent());
                Files.writeString(alvo, e.getValue()); // último vence (sobrescreve o anterior do mesmo nome)
            }

            enviarParaRepositorio(base);
            return base;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Path salvarConteudo(String caminhoRelativo, String conteudo) {
        try {
            Path base = resolverBase();
            limparConteudo(base);

            String nome = soNome(caminhoRelativo);
            if (nome == null || nome.isBlank()) nome = "Arquivo.java";
            if (!nome.endsWith(".java")) nome = nome + ".java";

            Path alvo = base.resolve(nome);
            Files.createDirectories(alvo.getParent());
            Files.writeString(alvo, conteudo); // último vence

            enviarParaRepositorio(base);
            return alvo;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Path salvarCodigo(String codigo) {
        return salvarConteudo("CodigoUsuario.java", codigo);
    }

    public Path salvarArquivo(File arquivo) {
        try {
            Path base = resolverBase();
            limparConteudo(base);
            if (arquivo.getName().endsWith(".java")) {
                String nome = soNome(arquivo.getName());
                Path alvo = base.resolve(nome);
                Files.createDirectories(alvo.getParent());
                Files.copy(arquivo.toPath(), alvo, StandardCopyOption.REPLACE_EXISTING); // último vence
            }
            enviarParaRepositorio(base);
            return base;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Path salvarProjetoZip(File zip) {
        try {
            Path base = resolverBase();
            limparConteudo(base);
            unzipSomenteJavaAchatarOverwrite(zip, base); // só .java, achatado, último vence
            enviarParaRepositorio(base);
            return base;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // ===== infra =====

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

            // limpa tudo no clone
            limparConteudo(destino);

            // copia SOMENTE .java, achatando; se repetir nome, o último sobrescreve
            try (var walk = Files.walk(pastaEntrada)) {
                walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            String nome = p.getFileName().toString();
                            Path alvo = destino.resolve(nome).normalize();
                            Files.createDirectories(alvo.getParent());
                            Files.copy(p, alvo, StandardCopyOption.REPLACE_EXISTING); // último vence
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
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
        } catch (IOException ignored) {}
    }

    // extrai apenas .java e grava no targetBase com o NOME DO ARQUIVO (sem pastas);
    // se aparecerem dois com o mesmo nome, o último do ZIP sobrescreve o anterior
    private static void unzipSomenteJavaAchatarOverwrite(File zipFile, Path targetBase) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) { zis.closeEntry(); continue; }
                String nome = entry.getName();
                if (nome == null || !nome.endsWith(".java")) { zis.closeEntry(); continue; }
                String baseName = Paths.get(nome).getFileName().toString();
                Path alvo = targetBase.resolve(baseName).normalize();
                Files.createDirectories(alvo.getParent());
                try (OutputStream os = Files.newOutputStream(alvo, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    zis.transferTo(os); // último vence
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String soNome(String caminho) {
        if (caminho == null || caminho.isBlank()) return "Arquivo.java";
        return Paths.get(caminho).getFileName().toString();
    }
}
