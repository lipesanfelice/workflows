package org.example.web.config;

import org.example.web.ia.ClienteIa;
import org.example.web.ia.ClienteIaGroq;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IaConfig {
    @Bean
    ClienteIa clienteIa() {
        var provedor = System.getenv().getOrDefault("IA_PROVEDOR","groq");
        if (provedor.equalsIgnoreCase("groq")) {
            var chave = System.getenv("GROQ_API_KEY");
            var modelo = System.getenv().getOrDefault("GROQ_MODELO","llama-3.1-8b-instant");
            return new ClienteIaGroq(chave, modelo);
        }
        throw new IllegalStateException("Provedor de IA inválido");
    }
}
