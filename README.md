# Sistema de Assinaturas — Desafio Globo

Monorepo com uma API Spring Boot e uma interface demonstrativa em React.

| Pasta | Tecnologia | Responsabilidade |
| --- | --- | --- |
| [`back`](./back) | Java 21, Spring Boot, PostgreSQL e Redis | Regras de negócio, renovação, cache e checkout demonstrativo |
| [`front`](./front) | React, Vite e Tailwind CSS | Fluxo visual de contratação, consulta, cancelamento e checkout |

## Executando a demonstração

Em dois terminais:

```bash
cd back
docker compose up -d
./gradlew bootRun
```

```bash
cd front
npm install
npm run dev
```

Abra `http://localhost:5173`. A documentação interativa da API fica em `http://localhost:8080/swagger-ui.html`.
