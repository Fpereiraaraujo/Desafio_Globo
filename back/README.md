# Sistema de Assinaturas — Desafio Globo

API em Java 21 e Spring Boot 3 para cadastro de usuários, contratação, cancelamento e renovação automática de assinaturas.

## Decisões de arquitetura

- Organização em camadas: `core` concentra domínio e casos de uso; `dataprovider` contém persistência e integrações; `entrypoint` expõe a API.
- PostgreSQL com Flyway para versionar o schema; Redis para leituras de assinatura e Caffeine para o catálogo de planos.
- Uma restrição única no banco impede duas assinaturas `ACTIVE` para o mesmo usuário.
- O job processa somente assinaturas que vencem no dia corrente. Cada tentativa tem chave idempotente própria (`assinatura:data:attempt:n`), registro persistido e lock pessimista da assinatura para evitar processamento concorrente.
- Após três falhas de pagamento, a assinatura passa para `SUSPENDED`. Ao cancelar, o status muda para `CANCELED`, mas a data de expiração é preservada.
- InfinitePay é uma integração demonstrativa de checkout: cria e devolve a URL de redirecionamento. Retry e circuit breaker estão limitados a essa chamada externa.

## Tecnologias

Java 21, Spring Boot 3, Spring Data JPA, PostgreSQL, Flyway, Redis, Caffeine, Lombok, Resilience4j, OpenAPI/Swagger, JUnit 5 e Mockito.

## Executando localmente

Pré-requisitos: Java 21 e Docker (somente para PostgreSQL e Redis).

```bash
docker compose up -d
./gradlew bootRun
```

Serviços locais:

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Actuator: `http://localhost:8080/actuator/health`
- PostgreSQL: `localhost:5433`
- Redis: `localhost:6379`

Para executar os testes:

```bash
./gradlew test
```

## Endpoints principais

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| POST | `/api/v1/users` | Cadastra usuário |
| POST | `/api/v1/subscriptions` | Cria assinatura |
| GET | `/api/v1/subscriptions/{id}` | Consulta assinatura |
| GET | `/api/v1/subscriptions/users/{userId}` | Consulta assinatura ativa do usuário |
| POST | `/api/v1/subscriptions/{id}/cancel` | Cancela, preservando o acesso até o vencimento |
| POST | `/api/v1/subscriptions/{id}/checkout` | Retorna URL demonstrativa de checkout da InfinitePay |
| GET | `/api/v1/plans` | Lista planos e preços |

Exemplo de cadastro e contratação:

```json
POST /api/v1/users
{
  "name": "Fernando Pereira",
  "email": "fernando@example.com"
}
```

```json
POST /api/v1/subscriptions
{
  "userId": "<uuid-do-usuario>",
  "plan": "PREMIUM"
}
```

Planos disponíveis: `BASICO` (R$ 19,90), `PREMIUM` (R$ 39,90) e `FAMILIA` (R$ 59,90) por mês.

## Configurações úteis

As variáveis `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `CORS_ALLOWED_ORIGINS`, `INFINITEPAY_HANDLE` e `SUBSCRIPTIONS_RENEWAL_CRON` permitem adaptar o ambiente sem alterar o código.
