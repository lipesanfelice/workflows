package org.example.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/gh")
public class GithubProxyController {
    @Value("${GH_TOKEN:}")
    private String token;

    private HttpHeaders cabecalhos() {
        HttpHeaders h = new HttpHeaders();
        h.set("Accept", "application/vnd.github+json");
        h.set("X-GitHub-Api-Version", "2022-11-28");
        if (token != null && !token.isEmpty()) h.set("Authorization", "Bearer " + token);
        return h;
    }

    @GetMapping("/rate_limit")
    public ResponseEntity<String> rate() {
        RestTemplate rt = new RestTemplate();
        HttpEntity<Void> ent = new HttpEntity<>(cabecalhos());
        return rt.exchange("https://api.github.com/rate_limit", HttpMethod.GET, ent, String.class);
    }

    @GetMapping("/repos/{owner}/{repo}/actions/runs")
    public ResponseEntity<String> runs(@PathVariable String owner, @PathVariable String repo, @RequestParam Map<String,String> q) {
        RestTemplate rt = new RestTemplate();
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/actions/runs";
        if (!q.isEmpty()) url += "?" + q.entrySet().stream().map(e->e.getKey()+"="+e.getValue()).reduce((a,b)->a+"&"+b).orElse("");
        HttpEntity<Void> ent = new HttpEntity<>(cabecalhos());
        return rt.exchange(url, HttpMethod.GET, ent, String.class);
    }

    @GetMapping("/repos/{owner}/{repo}/actions/runs/{id}")
    public ResponseEntity<String> runById(@PathVariable String owner, @PathVariable String repo, @PathVariable String id) {
        RestTemplate rt = new RestTemplate();
        HttpEntity<Void> ent = new HttpEntity<>(cabecalhos());
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/actions/runs/" + id;
        return rt.exchange(url, HttpMethod.GET, ent, String.class);
    }

    @GetMapping("/raw/{owner}/{repo}/{branch}/**")
    public ResponseEntity<String> raw(@PathVariable String owner, @PathVariable String repo, @PathVariable String branch, @RequestHeader Map<String,String> headers) {
        RestTemplate rt = new RestTemplate();
        String path = headers.getOrDefault("x-original-uri", "");
        int i = path.indexOf("/raw/" + owner + "/" + repo + "/" + branch + "/");
        String resto = i>=0 ? path.substring(i + ("/raw/" + owner + "/" + repo + "/" + branch + "/").length()) : "";
        String url = "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + "/" + resto;
        HttpHeaders hh = new HttpHeaders();
        hh.set("Cache-Control","no-store");
        ResponseEntity<String> r = rt.exchange(url, HttpMethod.GET, new HttpEntity<>(hh), String.class);
        return ResponseEntity.status(r.getStatusCode()).headers(r.getHeaders()).body(r.getBody());
    }
}
