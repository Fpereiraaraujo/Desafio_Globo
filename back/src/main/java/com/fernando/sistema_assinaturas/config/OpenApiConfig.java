package com.fernando.sistema_assinaturas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI sistemaAssinaturasOpenApi() {
		return new OpenAPI().info(new Info()
			.title("Sistema de Assinaturas API")
			.description("API para gestão de usuários, planos, assinaturas e checkout")
			.version("v1"));
	}
}
