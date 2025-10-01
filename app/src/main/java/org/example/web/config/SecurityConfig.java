// src/main/java/.../SecurityConfig.java
package org.example.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      // Habilita CORS (fonte será definida no CorsConfigurationSource)
      .cors(Customizer.withDefaults())

      // CSRF: GET não precisa, mas se quiser simplificar, pode desabilitar
      .csrf(csrf -> csrf.disable())

      .authorizeHttpRequests(auth -> auth
        // libera o status da pipeline
        .requestMatchers(HttpMethod.GET, "/api/process/status").permitAll()

        // libera o preflight CORS em toda a API (opcional, mas útil)
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

        // o restante você decide:
        .anyRequest().authenticated()
      )

      // Exemplo simples de auth HTTP Basic (ajuste conforme seu projeto)
      .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}
