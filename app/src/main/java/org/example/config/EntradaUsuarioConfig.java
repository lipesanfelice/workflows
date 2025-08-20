package org.example.config;

import org.example.web.service.EntradaUsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntradaUsuarioConfig {

    @Bean
    public EntradaUsuarioService entradaUsuarioService() {
        // Caminho da pasta onde os arquivos do usuário serão commitados
        String diretorioEntradaUsuario = "entrada-usuario";
        
        // URL do repositório Git remoto
        String repositorioGit = "https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git";

        // Cria o service já configurado
        EntradaUsuarioService service = new EntradaUsuarioService(diretorioEntradaUsuario, repositorioGit);

        // Limpa a pasta no startup para evitar conflitos
        service.limparEntradaUsuario();

        return service;
    }
}
