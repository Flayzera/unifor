# StudyRoom — Visão geral (monorepo)

Sistema web para **gestão e reserva de salas de estudo**: autenticação com **Firebase Auth**, API **REST** em **Spring Boot** e persistência em **Firestore**.

| Camada | Tecnologia | Pasta |
|--------|------------|--------|
| Frontend | Vue 3, TypeScript, Vite, Tailwind | `studyroom-frontend/` |
| Backend | Java 17, Spring Boot, Firebase Admin SDK | `studyroom-backend/` |

Documentação detalhada por pasta:

- [Backend — `studyroom-backend/README.md`](studyroom-backend/README.md)
- [Frontend — `studyroom-frontend/README.md`](studyroom-frontend/README.md)

---

## Escopo acadêmico — referência rápida

Este monorepo cobre os **requisitos técnicos** típicos de um trabalho full stack em nuvem (organização da equipe, GitHub Projects, texto do relatório e gravação do vídeo ficam **fora** do código).

| Área | Onde está no repositório |
|------|---------------------------|
| **Planejamento (git)** | README detalhado (esta página + por pasta); [padrão de branches e commits](#contribuindo) (Conventional Commits). *Issues/Projects: use no GitHub conforme a disciplina.* |
| **App** | Autenticação/autorização **Firebase** + papéis **admin** / **estudante**; CRUD de **usuários, salas e reservas** descrito nas tabelas do [README do backend](studyroom-backend/README.md). |
| **API documentada** | Contrato **OpenAPI 3** em [`studyroom-backend/openapi.yaml`](studyroom-backend/openapi.yaml) (import no **Swagger Editor** ou **Postman**; não há Swagger UI embutida no servidor). |
| **Validação e logs** | Bean Validation e regras no Spring; **logs de requisição** (filtro) e **erros centralizados** (`GlobalExceptionHandler` no código). |
| **Arquitetura** | **Vue 3** + deploy **Vercel**; API **Spring Boot** em **Docker** + deploy **Render** (ou similar); **Firestore** como banco gerenciado, dados fora do container. |
| **CI/CD** | [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml): **build**, **testes** (`./mvnw verify`, `npm run test`) e **imagem Docker** do backend. **Deploy automático** opcional via *webhooks* (`BACKEND_DEPLOY_HOOK_URL` / `FRONTEND_DEPLOY_HOOK_URL`); configure os *hooks* na Vercel/Render se a disciplina exigir publicação automática a cada push. |
| **Segurança** | Credenciais por **variáveis de ambiente** / secret files; rotas protegidas no front e no back; perfis **dev** / **prod** (`application-*.properties`). |
| **Testes** | Guia de execução local e CI: [`docs/testing.md`](docs/testing.md) (JUnit/Mockito no backend, Vitest no frontend). *Cobertura publicada no CI: opcional.* |
| **Bônus** | *Feature flag* `feature.external-fact.enabled` + [`GET /api/integrations/random-fact`](studyroom-backend/openapi.yaml) (API externa). |

Checklist item a item (marcar o que a equipe já cumpriu): [`docs/scope-checklist.md`](docs/scope-checklist.md). Modelo do relatório técnico: [`docs/technical-report-template.md`](docs/technical-report-template.md).

---

## Arquitetura (resumo)

1. O usuário faz login no **frontend** com Firebase (ex.: Google).
2. O frontend envia o **ID Token** nas chamadas: `Authorization: Bearer <token>`.
3. O **backend** valida o token com o Firebase Admin e aplica papéis (`ROLE_ADMIN` / `ROLE_STUDENT`) a partir das **custom claims** (`admin: true` no Firebase Auth).
4. Dados de negócio (usuários, salas, reservas) ficam no **Firestore**.

---

## Pré-requisitos

- **Node.js 20+** e npm (frontend e ferramentas JS do backend em `scripts/`)
- **Java 17** e Maven (ou `./mvnw` na pasta do backend)
- Projeto **Firebase** com **Authentication** e **Firestore** habilitados
- Conta de serviço (JSON) do Firebase para o backend

---

## Configuração rápida (desenvolvimento local)

### Backend

1. Baixe a chave JSON em Firebase → Configurações → Contas de serviço → **Gerar nova chave privada**.
2. Salve como `studyroom-backend/src/main/resources/study-room-firebase-adminsdk.json` (não commite este arquivo).
3. Execute: `cd studyroom-backend && chmod +x mvnw && ./mvnw spring-boot:run`  
   API em `http://localhost:8080`.

### Frontend

1. `cd studyroom-frontend && cp .env.example .env`
2. Preencha as variáveis `VITE_FIREBASE_*` (mesmo projeto Firebase do backend).
3. `npm install && npm run dev` → em geral `http://localhost:5173` (proxy `/api` → `8080`).

---

## Deploy em nuvem (referência)

| Serviço | Uso típico | Observação |
|---------|------------|------------|
| **Render** | Backend (Docker) | Root directory: `studyroom-backend`. Variáveis: `SPRING_PROFILES_ACTIVE=prod`, credencial Firebase (`GOOGLE_APPLICATION_CREDENTIALS` ou `FIREBASE_SERVICE_ACCOUNT_JSON`). |
| **Vercel** | Frontend estático | Root directory: `studyroom-frontend`. `VITE_API_URL=https://seu-backend.onrender.com` (com `https://`). Arquivo `vercel.json` garante rotas SPA. |
| **Firebase** | Auth + Firestore | Em Authentication → **Authorized domains**, inclua o domínio da Vercel (ex.: `*.vercel.app`). |

---

## Docker (somente backend)

```bash
cd studyroom-backend
docker build -t studyroom-backend .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e GOOGLE_APPLICATION_CREDENTIALS=/run/secrets/firebase.json \
  ...
```

Na raiz do repositório:

```bash
docker build -t studyroom-backend -f studyroom-backend/Dockerfile studyroom-backend
```

---

## Testes e build

```bash
# Backend
cd studyroom-backend && ./mvnw verify

# Frontend
cd studyroom-frontend && npm test && npm run build
```

---

## Contrato da API

- Arquivo OpenAPI: [`studyroom-backend/openapi.yaml`](studyroom-backend/openapi.yaml)  
  Importe no Swagger Editor ou Postman.

---

## CI/CD

Workflow: [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml)

- Build/teste do backend, imagem Docker, testes e build do frontend.
- Deploy automático via *hooks* opcionais (secrets `BACKEND_DEPLOY_HOOK_URL`, `FRONTEND_DEPLOY_HOOK_URL`).

---

## Contribuindo

Leia também os READMEs por pacote (links no início deste arquivo) antes de propor mudanças.

### Branches

Use um branch por feature ou correção:

- `feature/<descrição-curta>`
- `fix/<descrição-curta>`
- `docs/<descrição-curta>`

### Commits (Conventional Commits)

Prefira mensagens semânticas, por exemplo:

- `feat: add room status endpoint`
- `fix: validate reservation dates`
- `docs: update deployment instructions`
- `test: add StatusBadge component test`
- `chore: configure ci workflow`

### Pull request

Antes de abrir o PR:

- [ ] Branch atualizado com `main` ou `dev`
- [ ] Testes automatizados passando localmente (ver [Testes e build](#testes-e-build) e [`docs/testing.md`](docs/testing.md))
- [ ] README ou docs atualizados quando fizer sentido
- [ ] Nenhum segredo (`.env`, JSON de serviço) no commit

---

## Documentação da equipe e entrega

- Guia de testes (local e CI): [`docs/testing.md`](docs/testing.md)
- Roteiro para vídeo de demonstração (~7 min): [`docs/roteiro-video-7min.md`](docs/roteiro-video-7min.md)
- Checklist do escopo do trabalho: [`docs/scope-checklist.md`](docs/scope-checklist.md)
- Modelo de relatório técnico: [`docs/technical-report-template.md`](docs/technical-report-template.md)

---

## Segurança

- Não commite `.env` real nem JSON de conta de serviço.
- Mantenha o mesmo **projeto Firebase** entre frontend (`VITE_*`) e backend (credencial Admin).

---

## Funcionalidade extra (bônus)

- `GET /api/integrations/random-fact` — integração com API pública; controlada por `feature.external-fact.enabled` (em **produção** costuma estar `false`; ligue no Render se quiser ativar).
