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
                if (!nome.endsWith(".java")) continue;
                Path alvo = nomeUnico(base, nome);
                Files.createDirectories(alvo.getParent());
                Files.writeString(alvo, e.getValue());
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
            if (!nome.endsWith(".java")) nome = nome + ".java";
            Path alvo = nomeUnico(base, nome);
            Files.createDirectories(alvo.getParent());
            Files.writeString(alvo, conteudo);
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
                Path alvo = nomeUnico(base, soNome(arquivo.getName()));
                Files.createDirectories(alvo.getParent());
                Files.copy(arquivo.toPath(), alvo, StandardCopyOption.REPLACE_EXISTING);
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
            unzipSomenteJavaAchatar(zip, base); // << só .java e sem pastas
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

            // copia SOMENTE arquivos .java, achatando (sem pastas)
            try (var walk = Files.walk(pastaEntrada)) {
                walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            String nome = p.getFileName().toString();
                            Path alvo = nomeUnico(destino, nome);
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
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
        } catch (IOException ignored) {}
    }

    // extrai apenas .java e grava no targetBase com o NOME DO ARQUIVO (sem pastas)
    private static void unzipSomenteJavaAchatar(File zipFile, Path targetBase) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) { zis.closeEntry(); continue; }
                String nome = entry.getName();
                if (nome == null || !nome.endsWith(".java")) { zis.closeEntry(); continue; }
                String baseName = Paths.get(nome).getFileName().toString();
                Path alvo = nomeUnico(targetBase, baseName);
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

    // util: pega só o nome do arquivo (sem diretórios)
    private static String soNome(String caminho) {
        if (caminho == null || caminho.isBlank()) return "Arquivo.java";
        return Paths.get(caminho).getFileName().toString();
    }

    // util: garante nome único para evitar sobrescritas (Classe.java, Classe_2.java, ...)
    private static Path nomeUnico(Path dir, String baseName) {
        String nome = baseName;
        int i = 1;
        Path alvo = dir.resolve(nome);
        String semExt = nome;
        String ext = "";
        int p = nome.lastIndexOf('.');
        if (p > 0) {
            semExt = nome.substring(0, p);
            ext = nome.substring(p);
        }
        while (Files.exists(alvo)) {
            i++;
            alvo = dir.resolve(semExt + "_" + i + ext);
        }
        return alvo;
    }
}
