package com.fernando.sistema_assinaturas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SistemaAssinaturasApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaAssinaturasApplication.class, args);
	}

}
