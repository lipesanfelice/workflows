package org.example.web.Exec;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ExecRegistry {

    // execId -> commitSha
    private final ConcurrentMap<String, String> execToSha = new ConcurrentHashMap<>();
    // último commit empurrado pelas rotas /entrada/*
    private volatile String latestPushedSha;

    public void setLatestPushedSha(String sha) {
        this.latestPushedSha = sha;
    }
    public String getLatestPushedSha() {
        return latestPushedSha;
    }

    public void bindExecToLatest(String execId) {
        String sha = this.latestPushedSha;
        if (sha != null && execId != null && !execId.isBlank()) {
            execToSha.put(execId, sha);
        }
    }

    public void bind(String execId, String sha) {
        if (execId != null && sha != null) execToSha.put(execId, sha);
    }

    public String getSha(String execId) {
        return execToSha.get(execId);
    }
}
