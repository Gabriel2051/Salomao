# SALOMÃO — biblioteca pessoal digital

Sistema web completo para criação, organização e gerenciamento de conteúdos narrativos:
**anotações, personagens, poderes, histórias e mapas mentais** — todos privados por usuário,
com pesquisa global, editor rico com autosave, menções `@Personagem` e mapas interativos.

Identidade visual própria dark premium (preto profundo `#080808` + vermelho `#B5121B`).

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1 (MVC, Security, Data JPA, Validation, Thymeleaf) |
| Banco | PostgreSQL (`salomao`) em runtime · H2 em testes |
| Frontend | Thymeleaf + CSS/JS próprios (sem dependência de CDN) |
| Sanitização | Jsoup (anti-XSS em todo HTML rico) |
| Senhas | BCrypt (custo 12) |
| Build | Gradle Wrapper |

## Arquitetura

```
com.biblioteca.salomao
├── config/        SecurityConfig (sessão, CSRF, CSP, headers), WebConfig, DevSeedConfig (profile seed)
├── security/      CustomUserDetails(Service), RateLimitFilter (login/registro), OwnershipService (anti-IDOR)
├── domain/        User, Note, Character, Power, Story, StoryMention,
│                  MentalMap, MindMapNode, MindMapEdge, Tag, EntityTag, ActivityLog
├── repository/    Spring Data JPA — TODAS as consultas filtram por user_id
├── service/       UserService (registro/senha forte), LibraryService (regras + sanitização),
│                  ActivityService (atividades recentes)
├── web/           AuthController, DashboardController, Note/Character/Power/Story/MindMapController,
│                  GlobalExceptionHandler (mensagens amigáveis; 500 nunca vaza detalhe)
└── util/          Sanitizer (Jsoup)
```

Frontend em `src/main/resources/{templates,static/{css,js}}` com layout sidebar,
paleta global (`Ctrl+K`), editor rico, autosave com debounce e editor de mapa mental em SVG.
Schema canônico em `src/main/resources/db/migration/V1__init.sql`.

## Segurança / isolamento (multi-tenancy por `user_id`)

- Todo conteúdo pertence a um `User`; **nenhum endpoint aceita `user_id` do frontend** —
  o dono é sempre extraído da sessão validada no backend (`@AuthenticationPrincipal`).
- Cada leitura/escrita valida a propriedade (`findByIdAndUserId`, `OwnershipService`).
- Recurso alheio retorna **404** (não revela existência) — bloqueia IDOR horizontal,
  inclusive em associações (poder alheio não associa), menções e referências de mapa.
- BCrypt + validação de senha forte + confirmação; sem senha em texto puro.
- Rate limiting (5 tentativas/10min por IP) em `/login` e `/registro` (anti brute-force).
- CSRF ativo, cookies `HttpOnly` + `SameSite=lax` (+ `Secure` via `APP_SECURE_COOKIE=true` em produção),
  fixação de sessão mitigada (`migrateSession`), logout com invalidação.
- Headers: CSP restritiva, `frame-ancestors 'none'`, `X-Frame-Options: DENY`, referrer policy.
- Sanitização server-side de todo HTML (Jsoup) e de tags/textos; validação Bean Validation.
- Testes automatizados de isolamento: `SecurityIsolationTest` (A × B em todos os módulos).

## Pré-requisitos

- JDK 21+ · PostgreSQL 14+ · (opcional) Docker

## Configuração do PostgreSQL

```sql
CREATE DATABASE salomao;
CREATE USER salomao WITH PASSWORD 'troque-esta-senha';
GRANT ALL PRIVILEGES ON DATABASE salomao TO salomao;
```

Copie `.env.example` para `.env` (ou exporte as variáveis) e ajuste:

| Variável | Padrão | Descrição |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5433/salomao` | JDBC do banco (neste ambiente, cluster PostgreSQL 17) |
| `DATABASE_USER` / `DATABASE_PASSWORD` | `salomao` / `salomao` | credenciais |
| `AUTH_SECRET` | — | segredo (≥32 chars em produção) |
| `APP_URL` | `http://localhost:8080` | URL pública |
| `PORT` | `8080` | porta |
| `APP_SECURE_COOKIE` | `false` | `true` com HTTPS em produção |

Com Docker (alternativa rápida):

```bash
docker run --name salomao-pg -e POSTGRES_DB=salomao -e POSTGRES_USER=salomao \
  -e POSTGRES_PASSWORD=salomao -p 5433:5432 -d postgres:16
# (porta 5433 no host para não conflitar com outro PostgreSQL local na 5432)
```

## Execução local

```bash
./gradlew bootRun
# Windows:
gradlew.bat bootRun
```

Acesse `http://localhost:8080` → crie a conta em **Criar conta** → use o dashboard.

### Problemas comuns ao subir

**`FATAL: autenticação do tipo senha falhou para o usuário "salomao"`**
(a mensagem pode aparecer com caracteres trocados — é só encoding do console).
Significa: o app chegou ao PostgreSQL, mas a senha (ou o usuário) não confere.
Os erros seguintes (`Unable to determine Dialect...`) são só consequência —
somem assim que a conexão funcionar. Resolva de uma destas formas:

```sql
-- Opção A (recomendada): alinhe o banco aos padrões do app.
-- Conecte com o superusuário (ex.: SQL Shell do PostgreSQL) e rode:
ALTER USER salomao WITH PASSWORD 'salomao';
-- Se o role/banco ainda não existirem:
-- CREATE USER salomao WITH PASSWORD 'salomao';
-- CREATE DATABASE salomao OWNER salomao;
-- GRANT ALL PRIVILEGES ON DATABASE salomao TO salomao;
```

```text
# Opção B: alinhe o app ao banco (sem tocar no PostgreSQL).
# No IntelliJ: Run > Edit Configurations > sua classe Application >
# Environment variables:
DATABASE_USER=salomao
DATABASE_PASSWORD=<a-senha-real>
# (Se o banco estiver em outro host/porta: DATABASE_URL=jdbc:postgresql://localhost:5433/salomao)
```

```text
# Opção C: pré-visualizar a interface sem PostgreSQL (dados em memória, somem ao parar).
# VM options da Run Configuration no IntelliJ:
-Dspring.datasource.url=jdbc:h2:mem:salomaoboot -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password=
```

> Atenção se você tem **mais de um PostgreSQL instalado** (ex.: 17 e 18):
> cada um roda em uma porta (5433, 5432...). Este projeto usa por padrão a **5433**
> (cluster 17). Crie o role/banco no cluster certo ou ajuste `DATABASE_URL`
> com a porta correta.

Seed demo (somente desenvolvimento):

```bash
./gradlew bootRun --args="--spring.profiles.active=seed"
# login: demo / Demo1234
```

## Testes

```bash
./gradlew test
```

Cobrem: registro/validação/hash (`SecurityIsolationTest`), bloqueio A×B em
GET/PUT/DELETE/pesquisa/associações/menções, dashboard isolado, CRUD de todos os
módulos (`LibraryCrudTest`), grafo do mapa, sanitização XSS e pesquisa global.

## Migrações

O DDL de runtime é gerenciado pelo Hibernate (`ddl-auto=update`). O schema
canônico versionado está em `src/main/resources/db/migration/V1__init.sql`
(tabelas, FKs, índices, uniques e checks) — use-o para criar o banco manualmente
ou como base ao ativar Flyway em produção (ver "Próximas melhorias").

```bash
psql -U salomao -d salomao -f src/main/resources/db/migration/V1__init.sql
```

## Build

```bash
./gradlew build        # compila + testes + jar
ls build/libs/*.jar
```

## Deploy

1. PostgreSQL acessível + variáveis de ambiente (`DATABASE_URL`, `DATABASE_USER`,
   `DATABASE_PASSWORD`, `AUTH_SECRET` com ≥32 chars, `APP_URL`).
2. `APP_SECURE_COOKIE=true` (HTTPS obrigatório).
3. Executar: `java -jar build/libs/salomao-*.jar` (porta via `PORT`).
4. Recomendado: proxy reverso (TLS), usuário de banco com privilégios mínimos,
   backups do PostgreSQL e logs centralizados.

## Funcionalidades

- Registro/login/logout, sessões com expiração, troca de senha, perfil
- Dashboard (contadores + atividades recentes + continuar editando + criação rápida)
- Anotações (CRUD, busca, tags/categorias, favoritos, arquivamento, grade/lista)
- Personagens (ficha completa: básicas, aparência, personalidade, história, habilidades)
- Poderes (tokens clicáveis → modal/painel, personagens associados)
- Personagem ↔ poderes (N:N, mesma regra de dono nos dois lados)
- Histórias (editor rico, status, menções `@` com autocomplete e vínculo real no banco)
- Editor rico (negrito/itálico/sublinhado/títulos/listas/citação/link) + autosave com indicador
- Mapas mentais interativos (nós arrastáveis, tipos, conexões, zoom/pan/centralizar, autosave)
- Pesquisa global (página + paleta `Ctrl+K` com navegação por teclado)
- Responsivo (desktop/notebook/tablet/celular), acessibilidade (foco, labels, ARIA, teclado)
- Tratamento de erros amigável, confirmações em exclusões, estados loading/sucesso/erro/vazio

## Próximas melhorias recomendadas

1. Flyway ativo com a `V1__init.sql` + perfis `dev/prod` e `ddl-auto=validate` em produção
2. Upload de retratos (com validação de tipo/tamanho e armazenamento fora do banco)
3. Exportação (PDF/Markdown) de histórias e fichas
4. Compartilhamento somente-leitura via link assinado (sem quebrar o isolamento padrão)
5. Busca full-text (tsvector) + paginação infinita e cache de dashboard
