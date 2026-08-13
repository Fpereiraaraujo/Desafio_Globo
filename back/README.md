# Sistema de Assinaturas — Backend

API REST para gerenciar usuários e assinaturas mensais. O projeto foi desenvolvido para o desafio técnico da Globo, com foco em regras de negócio claras, consistência dos dados e uma arquitetura simples de manter.

## O que a aplicação resolve

- Cadastro de usuários e contratação de planos.
- Garantia de somente uma assinatura ativa por usuário.
- Cancelamento sem perda de acesso antes do fim do ciclo vigente.
- Renovação automática no dia do vencimento.
- Até três tentativas de renovação; após a terceira falha, a assinatura é suspensa.
- Geração demonstrativa de checkout via InfinitePay.

## Stack

| Categoria | Tecnologias |
| --- | --- |
| Linguagem e framework | Java 21, Spring Boot 3 |
| Persistência | Spring Data JPA, PostgreSQL, Flyway |
| Cache | Redis para consultas de assinatura; Caffeine para planos |
| Resiliência | Resilience4j (retry e circuit breaker no checkout) |
| Documentação e operação | Swagger/OpenAPI, Actuator, Docker Compose |
| Qualidade | JUnit 5, Mockito e AssertJ |

## Arquitetura

O projeto usa uma Clean Architecture simplificada. O domínio não depende da API ou do banco, enquanto os detalhes de infraestrutura ficam nas bordas.

```text
src/main/java/com/fernando/sistema_assinaturas
├── core
│   ├── domain       # Entidades, enums e parâmetros dos casos de uso
│   ├── service      # Regras de domínio
│   ├── usecase      # Orquestração dos fluxos
│   ├── gateway      # Contratos para integrações externas
│   └── scheduler    # Renovação automática
├── dataprovider
│   ├── database     # Entidades JPA, mappers e repositories
│   ├── infinitepay  # Cliente de checkout demonstrativo
│   └── payment      # Gateway de renovação mockado
├── entrypoint/api   # Controllers, DTOs, mappers e tratamento de erros
└── config           # Cache, CORS, OpenAPI e propriedades
```

## Regras de negócio

| Regra | Implementação |
| --- | --- |
| Um usuário possui uma assinatura ativa por vez | Validação no caso de uso e índice único parcial no PostgreSQL |
| Ciclo mensal | A expiração é calculada com um mês a partir da data de início ou da última expiração |
| Cancelamento | O status vira `CANCELED`; a data de expiração é preservada |
| Renovação | O scheduler seleciona somente assinaturas `ACTIVE` cuja expiração é o dia corrente |
| Falha de pagamento | São feitas até três tentativas sequenciais; a terceira falha muda o status para `SUSPENDED` |

### Consistência na renovação

O fluxo de renovação foi pensado para evitar duplicidade em reprocessamentos:

1. A assinatura é bloqueada com lock pessimista no banco durante o processamento.
2. Cada tentativa recebe uma chave idempotente no formato `subscriptionId:dataDeVencimento:attempt:n`.
3. A tentativa e a transação de pagamento são persistidas na mesma transação local.
4. Uma repetição da mesma tentativa devolve o registro existente, sem disparar uma nova cobrança.

Essa solução é adequada ao gateway mockado do desafio. Uma integração financeira real também exigiria conciliação por consulta/webhook do provedor para lidar com falhas de rede ambíguas.

## Planos

| Plano | Valor mensal |
| --- | ---: |
| `BASICO` | R$ 19,90 |
| `PREMIUM` | R$ 39,90 |
| `FAMILIA` | R$ 59,90 |

## Executando localmente

Pré-requisitos: Java 21 e Docker.

```bash
docker compose up -d
./gradlew bootRun
```

Serviços disponíveis:

| Serviço | Endereço |
| --- | --- |
| API | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Health check | `http://localhost:8080/actuator/health` |
| PostgreSQL | `localhost:5433` |
| Redis | `localhost:6379` |

Para parar a infraestrutura:

```bash
docker compose down
```

## Endpoints

| Método | Endpoint | Descrição |
| --- | --- | --- |
| POST | `/api/v1/users` | Cadastra um usuário |
| POST | `/api/v1/subscriptions` | Cria uma assinatura |
| GET | `/api/v1/subscriptions/{id}` | Busca uma assinatura por id |
| GET | `/api/v1/subscriptions/users/{userId}` | Busca a assinatura ativa de um usuário |
| POST | `/api/v1/subscriptions/{id}/cancel` | Cancela a renovação da assinatura |
| POST | `/api/v1/subscriptions/{id}/checkout` | Cria um link demonstrativo de checkout |
| GET | `/api/v1/plans` | Lista os planos |

### Exemplo de fluxo

Crie um usuário:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Fernando Pereira","email":"fernando@example.com"}'
```

Use o `id` retornado para criar a assinatura:

```bash
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H 'Content-Type: application/json' \
  -d '{"userId":"<uuid-do-usuario>","plan":"PREMIUM"}'
```

Resposta esperada:

```json
{
  "id": "<uuid>",
  "userId": "<uuid>",
  "plan": "PREMIUM",
  "monthlyPriceCents": 3990,
  "startDate": "2026-08-13",
  "expirationDate": "2026-09-13",
  "status": "ACTIVE"
}
```

## Cache, CORS e integração externa

- `plans`: cache local Caffeine, com duração de uma hora.
- `subscriptionsById` e `subscriptionsByUser`: cache Redis, invalidado ao cancelar ou renovar.
- CORS libera, por padrão, `http://localhost:3000` e `http://localhost:5173`.
- A InfinitePay é apenas uma vitrine de integração: o endpoint devolve a URL de checkout. Retry e circuit breaker protegem a chamada HTTP externa. Se a API externa estiver indisponível, a resposta usa uma URL `mock://` que o frontend converte em uma tela demonstrativa de checkout; ela não processa pagamento real.

## Configurações por ambiente

| Variável | Padrão | Uso |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5433/assinaturas` | URL do PostgreSQL |
| `DATABASE_USERNAME` | `assinaturas` | Usuário do banco |
| `DATABASE_PASSWORD` | `assinaturas` | Senha do banco |
| `REDIS_HOST` | `localhost` | Host do Redis |
| `REDIS_PORT` | `6379` | Porta do Redis |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Origens permitidas |
| `INFINITEPAY_HANDLE` | `fernando-de-9n6` | Handle usado no checkout demonstrativo |
| `SUBSCRIPTIONS_RENEWAL_CRON` | `0 0 2 * * *` | Agendamento da renovação |

## Testes

```bash
./gradlew test
```

Os testes cobrem regras de domínio, casos de uso, scheduler e controllers. O perfil de testes usa H2, portanto não é necessário subir Docker para executá-los.
