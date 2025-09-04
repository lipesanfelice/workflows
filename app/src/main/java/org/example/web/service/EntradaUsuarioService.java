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
            limparConteudo(base); // <— limpa TUDO localmente antes de gravar o novo envio
            for (var e : arquivosRelativosParaConteudo.entrySet()) {
                Path alvo = base.resolve(e.getKey());
                Files.createDirectories(alvo.getParent());
                Files.writeString(alvo, e.getValue());
            }
            enviarParaRepositorio(base); // <— vai limpar no clone e commitar
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Path salvarConteudo(String caminhoRelativo, String conteudo) {
        try {
            Path base = resolverBase();
            limparConteudo(base); // <— limpa TUDO localmente
            Path alvo = base.resolve(caminhoRelativo);
            Files.createDirectories(alvo.getParent());
            Files.writeString(alvo, conteudo);
            enviarParaRepositorio(base);
            return alvo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // compatível com seu controller
    public Path salvarCodigo(String codigo) {
        String nome = "java/CodigoUsuario.java";
        return salvarConteudo(nome, codigo);
    }

    // compatível com seu controller
    public Path salvarArquivo(File arquivo) {
        try {
            Path base = resolverBase();
            limparConteudo(base); // <— limpa TUDO localmente
            Path destino = base.resolve(arquivo.getName());
            Files.createDirectories(destino.getParent());
            Files.copy(arquivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            enviarParaRepositorio(base);
            return destino;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // compatível com seu controller
    public Path salvarProjetoZip(File zip) {
        try {
            Path base = resolverBase();
            limparConteudo(base); // <— limpa TUDO localmente
            unzip(zip, base);
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

            // 1) limpa TUDO no clone, para refletir estado novo
            limparConteudo(destino);

            // 2) copia TUDO do envio atual (sem exceções)
            try (var walk = Files.walk(pastaEntrada)) {
                walk.forEach(p -> {
                    try {
                        Path rel = pastaEntrada.relativize(p);
                        Path alvo = destino.resolve(rel.toString()).normalize();
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
        // add -A garante que deleções são versionadas
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

    private static void unzip(File zipFile, Path targetBase) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path alvo = targetBase.resolve(entry.getName()).normalize();
                if (!alvo.startsWith(targetBase)) { zis.closeEntry(); continue; }
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
