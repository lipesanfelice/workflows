package org.example.web.git;

import java.io.File;
import java.io.IOException;

public class GitServico {
    private final File pastaRepo;
    private final String urlRemota;

    public GitServico(File pastaRepo, String urlRemota) {
        this.pastaRepo = pastaRepo;
        this.urlRemota = urlRemota;
    }

    public void garantirClone() {
        if (pastaRepo.exists() && pastaRepo.isDirectory() && new File(pastaRepo, ".git").exists()) return;
        pastaRepo.mkdirs();
        exec(null, "git", "clone", urlRemota, pastaRepo.getAbsolutePath());
    }

    public void sincronizarMain() {
        exec(pastaRepo, "git", "config", "--global", "--add", "safe.directory", pastaRepo.getAbsolutePath());
        execSilencioso(pastaRepo, "git", "stash", "push", "--include-untracked", "--all", "-m", "autostash");
        exec(pastaRepo, "git", "fetch", "origin", "main", "--prune");
        if (exec(pastaRepo, "git", "rev-parse", "--verify", "main") != 0) {
            exec(pastaRepo, "git", "checkout", "-b", "main");
        } else {
            exec(pastaRepo, "git", "checkout", "main");
        }
        exec(pastaRepo, "git", "reset", "--hard", "origin/main");
        execSilencioso(pastaRepo, "git", "stash", "pop");
    }

    public void configurarIdentidade(String nome, String email) {
        exec(pastaRepo, "git", "config", "user.name", nome);
        exec(pastaRepo, "git", "config", "user.email", email);
    }

    public void adicionarCommitarEmpurrar(String caminhoRelativo, String mensagem) {
        exec(pastaRepo, "git", "add", "-A", caminhoRelativo);
        if (exec(pastaRepo, "git", "diff", "--cached", "--quiet") == 0) return;
        exec(pastaRepo, "git", "commit", "-m", mensagem);
        if (exec(pastaRepo, "git", "push", "origin", "HEAD:main") != 0) {
            execSilencioso(pastaRepo, "git", "pull", "--rebase", "--autostash", "origin", "main");
            if (exec(pastaRepo, "git", "push", "origin", "HEAD:main") != 0) {
                exec(pastaRepo, "git", "push", "--force-with-lease", "origin", "HEAD:main");
            }
        }
    }

    private int exec(File dir, String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (dir != null) pb.directory(dir);
            pb.inheritIO();
            Process p = pb.start();
            return p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void execSilencioso(File dir, String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (dir != null) pb.directory(dir);
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
