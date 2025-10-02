package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/gh")
public class GithubProxyController {

    @Value("${GH_TOKEN:}")
    private String token;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private HttpHeaders cabecalhos() {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
        h.set("X-GitHub-Api-Version", "2022-11-28");
        h.set(HttpHeaders.USER_AGENT, "CodeAnalyzer/1.0 (+http://localhost)");
        if (token != null && !token.isEmpty()) h.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return h;
    }

    private ResponseEntity<String> chamar(RestTemplate rt, String url) {
        try {
            ResponseEntity<String> r = rt.exchange(url, HttpMethod.GET, new HttpEntity<>(cabecalhos()), String.class);
            return ResponseEntity.status(r.getStatusCode()).headers(r.getHeaders()).body(r.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Upstream error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Proxy error: " + e.getMessage());
        }
    }

    @GetMapping("/rate_limit")
    public ResponseEntity<String> rate(RestTemplate rt) {
        return chamar(rt, "https://api.github.com/rate_limit");
    }

    @GetMapping("/repos/{owner}/{repo}/actions/runs")
    public ResponseEntity<String> runs(
            RestTemplate rt,
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam Map<String,String> q
    ) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl("https://api.github.com/repos/" + owner + "/" + repo + "/actions/runs");
        if (q != null) q.forEach(b::queryParam);
        return chamar(rt, b.toUriString());
    }

    @GetMapping("/repos/{owner}/{repo}/actions/runs/{id}")
    public ResponseEntity<String> runById(
            RestTemplate rt,
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String id
    ) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/actions/runs/" + id;
        return chamar(rt, url);
    }

    @GetMapping("/raw/{owner}/{repo}/{branch}/**")
    public ResponseEntity<String> raw(
            RestTemplate rt,
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String branch,
            HttpServletRequest req
    ) {
        String uri = req.getRequestURI(); // /gh/raw/owner/repo/branch/path/to/file
        String prefix = "/gh/raw/" + owner + "/" + repo + "/" + branch + "/";
        String resto = uri.startsWith(prefix) ? uri.substring(prefix.length()) : "";
        String url = "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + "/" + resto;
        HttpHeaders hh = new HttpHeaders();
        hh.set("Cache-Control", "no-store");
        try {
            ResponseEntity<String> r = rt.exchange(url, HttpMethod.GET, new HttpEntity<>(hh), String.class);
            return ResponseEntity.status(r.getStatusCode()).headers(r.getHeaders()).body(r.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Upstream error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Proxy error: " + e.getMessage());
        }
    }
}
