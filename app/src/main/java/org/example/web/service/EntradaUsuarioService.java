package org.example.web.service;

import org.example.web.git.GitServico;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.Map;
import java.util.Comparator;
import java.io.*;
import java.util.zip.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class EntradaUsuarioService {

    private static final ReentrantLock LOCK = new ReentrantLock();

    @Value("${app.entrada.diretorio:entrada-usuario}")
    private String diretorioEntrada;

    @Value("${app.entrada.repositorio-git:https://github.com/lipesanfelice/workflows.git}")
    private String repositorioGit;

    @Value("${app.entrada.mensagem-commit:Atualizar entrada do usuário}")
    private String mensagemCommit;

    // ===== APIs usadas pelo controller =====

    public Path salvarArquivos(Map<String, String> arquivosRelativosParaConteudo) {
        LOCK.lock();
        try {
            Path base = resolverBase();
            limparConteudo(base); // zera local

            for (var e : arquivosRelativosParaConteudo.entrySet()) {
                String nome = soNome(e.getKey());
                if (nome == null || !nome.endsWith(".java")) continue; // só .java
                Path alvo = nomeUnico(base, nome);                      // numera se já existir
                Files.createDirectories(alvo.getParent());
                Files.writeString(alvo, e.getValue());
            }

            enviarParaRepositorio(base); // limpa clone, copia mantendo nomes e commita
            return base;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            LOCK.unlock();
        }
    }

    public Path salvarConteudo(String caminhoRelativo, String conteudo) {
        LOCK.lock();
        try {
            Path base = resolverBase();
            limparConteudo(base);

            String nome = soNome(caminhoRelativo);
            if (nome == null || nome.isBlank()) nome = "Arquivo.java";
            if (!nome.endsWith(".java")) nome = nome + ".java";

            Path alvo = nomeUnico(base, nome);
            Files.createDirectories(alvo.getParent());
            Files.writeString(alvo, conteudo);

            enviarParaRepositorio(base);
            return alvo;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            LOCK.unlock();
        }
    }

    public Path salvarCodigo(String codigo) {
        return salvarConteudo("CodigoUsuario.java", codigo);
    }

    public Path salvarArquivo(File arquivo) {
        LOCK.lock();
        try {
            Path base = resolverBase();
            limparConteudo(base);

            if (arquivo.getName().endsWith(".java")) {
                String nome = soNome(arquivo.getName());
                Path alvo = nomeUnico(base, nome);
                Files.createDirectories(alvo.getParent());
                Files.copy(arquivo.toPath(), alvo, StandardCopyOption.REPLACE_EXISTING);
            }

            enviarParaRepositorio(base);
            return base;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            LOCK.unlock();
        }
    }

    public Path salvarProjetoZip(File zip) {
        LOCK.lock();
        try {
            Path base = resolverBase();
            limparConteudo(base);
            unzipSomenteJavaAchatarNumerando(zip, base); // só .java, achatado, numerando duplicados
            enviarParaRepositorio(base);
            return base;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            LOCK.unlock();
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

            // copia SOMENTE .java do envio atual, achatando e PRESERVANDO o nome (já numerado se preciso)
            try (var walk = Files.walk(pastaEntrada)) {
                walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            String nome = p.getFileName().toString(); // mantém o nome como está
                            Path alvo = destino.resolve(nome);
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

    // extrai apenas .java e grava no targetBase com o NOME DO ARQUIVO (sem pastas); numera se já existir
    private static void unzipSomenteJavaAchatarNumerando(File zipFile, Path targetBase) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) { zis.closeEntry(); continue; }
                String nome = entry.getName();
                if (nome == null || !nome.endsWith(".java")) { zis.closeEntry(); continue; }
                String baseName = Paths.get(nome).getFileName().toString();
                Path alvo = nomeUnico(targetBase, baseName); // numera se necessário
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

    // util: garante nome único no diretório (Classe.java, Classe_2.java, Classe_3.java, …)
    private static Path nomeUnico(Path dir, String baseName) {
        String semExt = baseName;
        String ext = "";
        int p = baseName.lastIndexOf('.');
        if (p > 0) {
            semExt = baseName.substring(0, p);
            ext = baseName.substring(p);
        }
        Path candidato = dir.resolve(baseName);
        int i = 2;
        while (Files.exists(candidato)) {
            candidato = dir.resolve(semExt + "_" + i + ext);
            i++;
        }
        return candidato;
    }
}
