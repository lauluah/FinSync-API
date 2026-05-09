# FinSync API

API REST de gerenciamento financeiro pessoal com análise de dados por IA (Gemini), documentação Swagger e pipeline de ingestão de transações externas.

---

## Tecnologias

- Java 21
- Spring Boot 4.0.6
- Spring Security
- Spring Data JPA
- Spring AI (Google Gemini)
- PostgreSQL (Supabase)
- SpringDoc OpenAPI (Swagger UI)

---

## Funcionalidades

- Cadastro e autenticação de usuários
- CRUD de transações financeiras (receitas e despesas)
- Categorização automática de transações
- Ingestão de transações de fontes externas com deduplicação via Virtual Threads
- Insights financeiros gerados por IA (Gemini 2.0 Flash)
- Resumo dos últimos 30 dias com recomendações
- Análise de gastos por categoria
- Documentação interativa via Swagger UI

---

## Configuração

### Pré-requisitos

- Java 21+
- Maven
- PostgreSQL (ou conta no Supabase)
- Chave de API do Google AI Studio

### application.properties

```properties
server.port=8080
spring.application.name=finsync

# PostgreSQL
spring.datasource.url=jdbc:postgresql://<host>:5432/postgres
spring.datasource.username=<usuario>
spring.datasource.password=<senha>
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# Google Gemini
spring.ai.model.chat=google-genai
spring.ai.google.genai.api-key=SUA_CHAVE_AQUI
spring.ai.google.genai.chat.options.model=gemini-2.0-flash

# Swagger
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
```

> A chave do Gemini pode ser obtida gratuitamente em [aistudio.google.com](https://aistudio.google.com)

### Executando

```bash
mvn spring-boot:run
```

---

## Documentação da API

### Postman

A documentação completa da API com todos os endpoints, exemplos de requisição e resposta está disponível no Postman:

[![Postman](https://img.shields.io/badge/Postman-Documentação-orange?style=for-the-badge&logo=postman)](https://documenter.getpostman.com/view/39388795/2sBXqNky4U)

> [https://documenter.getpostman.com/view/39388795/2sBXqNky4U](https://documenter.getpostman.com/view/39388795/2sBXqNky4U)

### Swagger UI

Após subir a aplicação localmente, acesse a documentação interativa:

```
http://localhost:8080/swagger-ui/index.html
```

A spec OpenAPI em JSON está disponível em:

```
http://localhost:8080/v3/api-docs
```

> No Swagger UI é possível testar os endpoints diretamente pelo navegador clicando em **Authorize** e informando o Bearer Token.

---

## Endpoints

### Autenticação — `/auth`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/register` | Registrar novo usuário |
| POST | `/auth/login` | Login |

### Transações — `/api/transactions`

> Requer autenticação Bearer Token

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/transactions` | Criar transação |
| GET | `/api/transactions` | Listar todas as transações |
| GET | `/api/transactions/last30days` | Transações dos últimos 30 dias |
| GET | `/api/transactions/summary` | Resumo por categoria |

### IA Financeira — `/api/ai`

> Requer autenticação Bearer Token

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/ai/insights` | Pergunta livre sobre suas finanças |
| GET | `/api/ai/summary/last30days` | Resumo automático dos últimos 30 dias |
| GET | `/api/ai/insights/categories` | Análise de gastos por categoria |

### Ingestão — `/api/ingest`

> Requer autenticação Bearer Token

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/ingest` | Importar transações de fontes externas |

---

## Exceções

A API retorna erros padronizados no formato:

```json
{
  "timestamp": "2026-05-08T21:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuário não encontrado: email@exemplo.com"
}
```

| Exception | Status HTTP | Quando é lançada |
|-----------|-------------|-----------------|
| `UserNotFoundException` | 404 | Usuário não encontrado por email ou ID |
| `UserAlreadyExistsException` | 409 | Tentativa de cadastro com e-mail já existente |
| `InvalidCredentialsException` | 401 | Email ou senha incorretos no login |
| `TransactionNotFoundException` | 404 | Transação não encontrada por ID |
| `InvalidTransactionException` | 400 | Dados inválidos na criação de transação |
| `AiInsightException` | 500 | Pergunta vazia ou erro na chamada ao Gemini |

---

## Categorias disponíveis

| Categoria | Descrição |
|-----------|-----------|
| `FOOD` | Alimentação e bebidas |
| `TRANSPORT` | Transporte e mobilidade |
| `ENTERTAINMENT` | Lazer e entretenimento |
| `SALARY` | Salário |
| `BONUS` | Bônus e gratificações |
| `INVESTMENT` | Investimentos |
| `OTHER` | Outros |

---

## Segurança

- Senhas armazenadas com BCrypt
- Endpoints protegidos via Spring Security
- Swagger UI acessível sem autenticação
- Autenticação via Bearer Token nos endpoints da API

---

## Estrutura do projeto

```
src/main/java/com/financas/tema1/
├── ai/               # Records de request/response da IA
├── application/      # Estratégias de categorização e normalização
├── config/           # Configurações (Security, OpenAPI)
├── controller/       # Controllers REST
├── domain/           # Entidades JPA
├── DTO/              # Data Transfer Objects
├── exception/        # Exceptions customizadas e GlobalExceptionHandler
├── repository/       # Repositórios Spring Data
├── service/          # Serviços de negócio
└── transaction/      # Enum TransactionType
```