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

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/gh")
public class GithubProxyController {

    @Value("${GH_TOKEN:}")
    private String tokenAmbiente;

    @Bean
    public RestTemplate restTemplate() {
        // Simples e suficiente para este caso; se quiser timeouts explícitos, use HttpClient custom
        return new RestTemplate();
    }

    /** Lê token do header Authorization: Bearer ..., ou X-GH-TOKEN, ou do ambiente como fallback. */
    private String resolverToken(HttpServletRequest req) {
        if (req != null) {
            String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && auth.toLowerCase().startsWith("bearer ")) {
                return auth.substring(7).trim();
            }
            String x = req.getHeader("X-GH-TOKEN");
            if (x != null && !x.isBlank()) return x.trim();
        }
        return tokenAmbiente;
    }

    private HttpHeaders cabecalhos(String tokenEfetivo) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
        h.set("X-GitHub-Api-Version", "2022-11-28");
        h.set(HttpHeaders.USER_AGENT, "CodeAnalyzer/1.0 (+http://localhost)");
        if (tokenEfetivo != null && !tokenEfetivo.isBlank()) {
            h.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenEfetivo);
        }
        return h;
    }

    private ResponseEntity<String> chamar(RestTemplate rt, String url, HttpHeaders headers) {
        try {
            ResponseEntity<String> r = rt.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return ResponseEntity.status(r.getStatusCode())
                    .headers(r.getHeaders())
                    .body(r.getBody());
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

    @GetMapping("/me")
    public ResponseEntity<String> me(RestTemplate rt, HttpServletRequest req) {
        String token = resolverToken(req);
        HttpHeaders h = cabecalhos(token);
        return chamar(rt, "https://api.github.com/user", h);
    }

    @GetMapping("/rate_limit")
    public ResponseEntity<String> rate(RestTemplate rt, HttpServletRequest req) {
        String token = resolverToken(req);
        HttpHeaders h = cabecalhos(token);
        return chamar(rt, "https://api.github.com/rate_limit", h);
    }

    @GetMapping("/repos/{owner}/{repo}/actions/runs")
    public ResponseEntity<String> runs(
            RestTemplate rt,
            HttpServletRequest req,
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam Map<String,String> q
    ) {
        String token = resolverToken(req);
        HttpHeaders h = cabecalhos(token);

        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl("https://api.github.com/repos/{owner}/{repo}/actions/runs")
                .encode()
                .uriVariables(Map.of("owner", owner, "repo", repo));
        if (q != null) q.forEach(b::queryParam);

        return chamar(rt, b.toUriString(), h);
    }

    @GetMapping("/repos/{owner}/{repo}/actions/runs/{id}")
    public ResponseEntity<String> runById(
            RestTemplate rt,
            HttpServletRequest req,
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String id
    ) {
        String token = resolverToken(req);
        HttpHeaders h = cabecalhos(token);

        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.github.com/repos/{owner}/{repo}/actions/runs/{id}")
                .encode()
                .buildAndExpand(owner, repo, id)
                .toUriString();

        return chamar(rt, url, h);
    }

    @GetMapping("/raw/{owner}/{repo}/{branch}/**")
    public ResponseEntity<String> raw(
            RestTemplate rt,
            HttpServletRequest req,
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String branch
    ) {
        String token = resolverToken(req); // não é necessário para raw, mas ok manter
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.USER_AGENT, "CodeAnalyzer/1.0 (+http://localhost)");
        h.setCacheControl(CacheControl.noStore());

        // Reconstrói o caminho após /raw/{owner}/{repo}/{branch}/...
        String uri = req.getRequestURI(); // ex: /gh/raw/owner/repo/branch/path/to/file
        String ctx = req.getContextPath() == null ? "" : req.getContextPath();
        String prefix = (ctx + "/gh/raw/" + owner + "/" + repo + "/" + branch + "/").replaceAll("//+", "/");
        String resto = uri.startsWith(prefix) ? uri.substring(prefix.length()) : "";

        String url = UriComponentsBuilder
                .fromHttpUrl("https://raw.githubusercontent.com/{owner}/{repo}/{branch}/{path}")
                .encode()
                .buildAndExpand(owner, repo, branch, resto)
                .toUriString();

        return chamar(rt, url, h);
    }
}
