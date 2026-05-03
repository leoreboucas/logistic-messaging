# logistic-messaging

Microsserviço de mensageria em tempo real desenvolvido como extensão do projeto [logistic](https://github.com/leoreboucas/logistic-project). Permite que clientes, entregadores e empresas se comuniquem com suporte de um bot de triagem baseado em IA antes de serem encaminhados para atendimento humano.

---

## Visão geral

O serviço opera de forma independente do `logistic`, com seu próprio banco de dados, autenticação JWT e lógica de domínio. A integração entre os dois serviços é feita via endpoints internos autenticados por API Key, usando o CPF/CNPJ como chave de integração entre os domínios.

---

## Stack

- Java 21
- Spring Boot 4.0.5
- PostgreSQL
- JPA / Hibernate
- Spring Security + JWT (jjwt 0.12.6)
- WebSocket + STOMP + SockJS
- Redis (pub/sub para broadcasting de mensagens)
- Google Gemini API (google-genai 1.0.0)
- SpringDoc OpenAPI 3.0.1
- Maven

---

## Arquitetura

### Comunicação em tempo real
A troca de mensagens é feita via WebSocket com protocolo STOMP sobre SockJS. Cada conversa tem um tópico Redis dedicado (`conversation-{uuid}`), permitindo que múltiplas instâncias do serviço possam distribuir mensagens sem acoplamento direto.

### Autenticação
O serviço possui geração e validação de JWT próprios, independente do `logistic`. A autenticação WebSocket é validada no handshake STOMP via `JwtChannelInterceptor`, que popula o `Principal` da sessão para uso nos controllers de mensagem.

### Integração com o serviço logistic
O registro de um novo usuário no messaging valida a existência desse usuário no `logistic` antes de prosseguir. Dados contextuais como pedidos do cliente e entregas do entregador são buscados em tempo real a cada sessão de bot, garantindo que o contexto enviado à IA esteja sempre atualizado.

### Bot de triagem
O bot opera exclusivamente no status `TRIAGEM`. O fluxo de status de uma conversa é:

```
ABERTO → TRIAGEM → AGUARDANDO_ATENDIMENTO → EM_ATENDIMENTO → ENCERRADA
```

O histórico enviado ao Gemini é filtrado por `sessionId`, garantindo que cada reabertura de conversa inicie um contexto limpo. O prompt do bot é composto por uma base comum e um complemento específico por role do usuário (`CUSTOMER`, `DELIVERY_MAN`, `ENTERPRISE`), com dados contextuais injetados dinamicamente.

Quando o bot identifica informações suficientes para encaminhar, retorna um JSON estruturado que é detectado pelo serviço e dispara a transição de status automaticamente.

### Tratamento de erros
Exceções de domínio são mapeadas por um `GlobalExceptionHandler` centralizado com respostas padronizadas por tipo (`BusinessException` → 400, `NotFoundException` → 404, `AuthenticationException` → 401).

---

## Status do projeto

A aplicação está funcional e pronta para uso. As seguintes implementações estão planejadas:

- [ ] Testes unitários (JUnit 5 + Mockito)
- [ ] Testes de integração (Testcontainers)
- [ ] Docker Compose unificado (logistic + messaging + Redis + ambos os bancos)

---

## Endpoints principais

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/users` | Cadastro de usuário |
| POST | `/auth/login` | Autenticação, retorna JWT |
| POST | `/conversations` | Criar ou recuperar conversa existente |
| WS | `/ws` (STOMP) | Conexão WebSocket |
| SUB | `/topic/conversations/{id}` | Receber mensagens da conversa |
| PUB | `/app/conversation/{id}` | Enviar mensagem |

---

## Variáveis de ambiente

```yaml
spring.datasource.url: jdbc:postgresql://localhost:5432/logistic_messaging
jwt.secret: <mínimo 32 caracteres>
jwt.expiration: <milissegundos>
gemini.api_key: <chave da Google AI Studio>
logistic.base_url: <url do serviço logistic>
internal.api.key: <chave compartilhada com o logistic>
```
