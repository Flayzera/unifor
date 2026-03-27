# Roteiro — Vídeo de demonstração do StudyRoom (~7 minutos)

Este documento é um **guia de apresentação** para gravar o vídeo da disciplina: explica o projeto de ponta a ponta, com **sugestão de tempos**, **texto-base** (adaptável à sua fala) e **o que mostrar em tela**. Ajuste nomes da equipe, URLs de deploy e detalhes da interface conforme a versão final.

---

## 1. Objetivo do vídeo

- **Mostrar** o sistema **StudyRoom** funcionando (reserva de salas de estudo).
- **Explicar**, em alto nível, **arquitetura**, **segurança**, **persistência**, **DevOps** e **qualidade** (testes / documentação).
- Manter **clareza** e **ritmo**: ~7 minutos costuma ser apertado — priorize **1 demo fluida** + **2 ou 3 slides** (ou browser + IDE) em vez de entrar em todo o código-fonte.

**Público:** professor e colegas que precisam entender *o quê* o sistema faz e *como* está estruturado, sem necessidade de ser um tutorial passo a passo de instalação.

---

## 2. Mapa de tempo sugerido (total ~7:00)

| Bloco | Tempo aprox. | Foco |
|-------|----------------|------|
| A — Abertura e problema | 0:45 | Contexto + objetivo do sistema |
| B — Stack e arquitetura | 1:15 | Vue, Spring Boot, Firebase; fluxo token → API → Firestore |
| C — Demonstração (happy path) | 2:45 | Login, salas, reserva, (opcional) visão admin |
| D — API e contrato | 0:50 | OpenAPI, REST, autenticação Bearer |
| E — Segurança e dados | 0:45 | Papéis, Firestore, validação e erros |
| F — DevOps e qualidade | 0:50 | Docker, GitHub Actions, testes |
| G — Encerramento | 0:10 | Recapitular + link do repositório |

> **Dica:** ensaie uma vez cronometrando. Se passar de 7 min, corte primeiro **detalhes extras** no bloco B ou reduza **uma** funcionalidade na demo (ex.: não mostrar promote admin).

---

## 3. Roteiro falado + o que mostrar na tela

### Bloco A — Abertura e problema (~0:00 – ~0:45)

**O que dizer (texto-base):**

- Cumprimentar e apresentar o nome do projeto: **StudyRoom**.
- Explicar o **problema**: em ambientes acadêmicos, é comum precisar **organizar o uso de salas de estudo** — evitar conflitos de horário, saber quais salas estão livres e **quem reservou**.
- Dizer que a solução é uma **aplicação web full stack**: o usuário se autentica, consulta salas, cria e gerencia **reservas**, e há perfis **estudante** e **administrador** com permissões diferentes.

**O que mostrar:**

- Tela inicial da aplicação (landing ou dashboard) **ou** slide simples com logo/nome do projeto + uma frase do problema.
- Se gravar tela do navegador, deixe a janela **maximizada** e zoom confortável (100–125%).

**Transição:** “A seguir, resumo como isso foi implementado tecnicamente e depois mostro funcionando.”

---

### Bloco B — Stack e arquitetura (~0:45 – ~2:00)

**O que dizer (texto-base):**

- O repositório é um **monorepo** com duas pastas principais:
  - **`studyroom-frontend`**: **Vue 3**, **TypeScript**, **Vite**, **Tailwind** — interface responsiva e chamadas HTTP à API.
  - **`studyroom-backend`**: **Java 17**, **Spring Boot**, **REST** — regras de negócio, validação e integração com Firebase.
- **Autenticação**: **Firebase Authentication** (ex.: login com Google no front). O front obtém um **ID Token JWT**.
- **Chamadas à API**: o frontend envia `Authorization: Bearer <token>`. O backend usa o **Firebase Admin SDK** para **validar** o token e montar o contexto de segurança do Spring (**roles** `ROLE_STUDENT` / `ROLE_ADMIN` a partir de **custom claims**, por exemplo `admin: true`).
- **Dados**: **Cloud Firestore** — usuários, salas e reservas persistidos fora do container; o backend acessa via SDK/admin conforme a modelagem do projeto.
- Opcional (uma frase): perfil do usuário pode ser **sincronizado** com o backend após o login (`POST /api/users`), e o **perfil atual** via `GET /api/users/me`.

**O que mostrar:**

- **Diagrama simples** (slide ou desenho rápido): Navegador → Firebase Auth → Front com token → API Spring → Firestore.
- **Ou** árvore de pastas no VS Code na **raiz do repositório** destacando `studyroom-frontend` e `studyroom-backend` (sem ficar mais de 20–30 s no código).

**Transição:** “Com essa arquitetura, vamos ver o fluxo na prática.”

---

### Bloco C — Demonstração (~2:00 – ~4:45)

Este é o **núcleo** do vídeo. Mostre um fluxo contínuo; evite pausas longas por erro de login.

**Roteiro sugerido (adaptar às telas reais):**

1. **Login** (~30–40 s)  
   - Abrir a URL do front (local ou deploy **Vercel**).  
   - Fazer login com **Firebase** (ex.: Google).  
   - **Dizer** que só usuários autenticados acessam as rotas principais.

2. **Salas e disponibilidade** (~40–50 s)  
   - Mostrar **lista de salas** ou fluxo de **busca de salas disponíveis** em um intervalo de tempo (conforme a UI).  
   - Mencionar que no backend existem endpoints como listagem de salas, salas **disponíveis** no período (`/api/rooms/available` com `start` e `end`), etc., conforme o [OpenAPI](../studyroom-backend/openapi.yaml).

3. **Criar reserva** (~50–60 s)  
   - Escolher sala, intervalo de tempo e confirmar.  
   - Enfatizar **regras de negócio** em uma frase: validação de datas, conflitos de horário, limites (ex.: tamanho do grupo), se isso estiver visível ou for citado como regra no back.

4. **Minhas reservas / ações** (~30–40 s)  
   - Mostrar **lista das reservas do usuário** (cancelar, reagendar ou concluir, se a UI expuser).  
   - Citualizar que existem rotas REST para **cancelar**, **concluir** e **reagendar** reserva no contrato da API.

5. **Administrador (opcional, ~40–50 s)** — *inclua só se couber no tempo e estiver estável.*  
   - Entrar com usuário **admin** (ou mostrar promoção se já tiverem feito o fluxo).  
   - Mostrar **CRUD de salas** (criar/excluir), **listagem de reservas** ou **dashboard de ocupação** (`/api/rooms/status`), alinhado ao que o front implementar.  
   - Dizer que operações sensíveis exigem **ROLE_ADMIN** no backend.

**O que mostrar:**

- Apenas o **navegador**, em uma **única aba** se possível; feche notificações e barra de favoritos poluída.
- Se algo falhar, tenha **segunda conta** ou **gravação de backup** (veja seção 6).

**Transição:** “Por baixo dos panos, isso tudo é exposto como API REST documentada.”

---

### Bloco D — API e contrato (~4:45 – ~5:35)

**O que dizer (texto-base):**

- A API é **REST**, com segurança **Bearer JWT** em rotas protegidas.
- O contrato está em **`studyroom-backend/openapi.yaml`**: pode ser aberto no **Swagger Editor** ou **Postman** para revisar paths de **usuários**, **salas**, **reservas** e a integração opcional **`/api/integrations/random-fact`** (fato aleatório de API externa, ligada por **feature flag** em produção).
- Mencionar **dois exemplos** de recurso: ex. `GET /api/users/me` para perfil atual; `POST /api/reservations` para criar reserva — sem ler todos os endpoints em voz alta.

**O que mostrar:**

- Abrir o arquivo **OpenAPI** no GitHub (renderizado) **ou** importação rápida no Postman (lista de endpoints colapsada, expandindo um exemplo).

**Transição:** “Como garantimos que só quem deve acessa cada coisa?”

---

### Bloco E — Segurança e dados (~5:35 – ~6:20)

**O que dizer (texto-base):**

- **Autorização**: o Spring Security aplica regras por **papel** (estudante vs admin); rotas administrativas protegidas com `hasRole('ADMIN')` onde aplicável.
- **Dados**: Firestore como banco gerenciado; credenciais do backend **não** vão para o repositório — uso de **variáveis de ambiente** ou arquivo de serviço fora do commit (como descrito no [README](../README.md)).
- **Erros e validação**: validação com **Bean Validation**; erros tratados de forma centralizada no **`GlobalExceptionHandler`** (incluindo validação de corpo de requisição com respostas HTTP adequadas).
- **Logs**: menção breve a **logs de requisição** e de erro para rastreabilidade.

**O que mostrar (opcional):**

- Trecho curto do **`GlobalExceptionHandler`** ou do **`openapi.yaml`** na seção `security` — **no máximo 15–20 s**.

**Transição:** “Para fechar o aspecto de engenharia: deploy e qualidade.”

---

### Bloco F — DevOps e qualidade (~6:20 – ~7:10)

> Se o tempo já estiver no limite, fale este bloco em **~40 s** e pule detalhes.

**O que dizer (texto-base):**

- **Backend containerizado** com **Docker** (`Dockerfile` em `studyroom-backend`); deploy de referência em serviços como **Render**, com perfil **`prod`** e variáveis para Firebase.
- **Frontend** como site estático (build Vite), deploy em **Vercel** com `VITE_API_URL` apontando para o backend em **HTTPS**.
- **CI/CD**: pipeline em **GitHub Actions** (`.github/workflows/ci-cd.yml`) — testes e build do backend, imagem Docker, testes e build do frontend; opcionalmente *hooks* de deploy.
- **Testes**: backend com **JUnit** / **Mockito** / **MockMvc**; frontend com **Vitest** (ex.: componentes e utilitários). Referência: [`docs/testing.md`](testing.md).

**O que mostrar:**

- Página do **GitHub Actions** com último workflow **verde** **ou** terminal com `./mvnw test` e `npm test` já rodados (pode ser print pré-gravado).

---

### Bloco G — Encerramento (~últimos 10–15 s)

**O que dizer:**

- Recapitular em **uma frase**: sistema de reservas com **front moderno**, **API Spring Boot**, **Firebase Auth + Firestore**, **Docker + CI**.
- Informar onde está o código (**repositório GitHub público**) e agradecer.

**O que mostrar:**

- Volta para o **README** do repositório na tela **ou** slide final com **link** e **nomes da equipe**.

---

## 4. Checklist antes de gravar

- [ ] URL do frontend (produção ou local) testada **no mesmo dia**.
- [ ] Conta de usuário **estudante** e **admin** funcionando; senha/login conferidos.
- [ ] Backend no ar se a demo usar deploy (evitar “vou subir agora” no vídeo).
- [ ] Navegador em **modo anônimo** ou perfil limpo, se quiser evitar extensões e dados sensíveis.
- [ ] Áudio: microfone estável; ambiente **silencioso**; *opcional:* música de fundo **desligada** (distrai em avaliação acadêmica).
- [ ] Resolução mínima **720p**; preferível **1080p**.
- [ ] Nomes reais de terceiros e dados pessoais **fora** da gravação (use dados fictícios se necessário).

---

## 5. Dicas de ritmo e linguagem

- **Frases curtas**; evite ler o README inteiro em voz alta.
- **Um conceito por frase** no bloco de arquitetura (evita “empilhar” Firebase + Spring + Firestore numa única respiração).
- Se precisar cortar tempo: **priorize a demo (bloco C)** e **encerre com uma frase forte** sobre arquitetura + DevOps.

---

## 6. Plano B se algo falhar na gravação

- Ter **segundo vídeo** curto só da demo gravado antes (**“take seguro”**).
- **Screenshots** ou **GIF** da aplicação no relatório técnico como comprovação complementar (se a disciplina permitir).
- Mostrar **OpenAPI + GitHub Actions verde** mesmo se o login do Firebase falhar momentaneamente.

---

## 7. Referências rápidas no repositório

| Tema | Onde |
|------|------|
| Visão geral e setup | [`README.md`](../README.md) |
| Contrato REST | [`studyroom-backend/openapi.yaml`](../studyroom-backend/openapi.yaml) |
| Testes (comandos) | [`docs/testing.md`](testing.md) |
| Checklist de escopo da disciplina | [`docs/scope-checklist.md`](scope-checklist.md) |
| CI/CD | [`.github/workflows/ci-cd.yml`](../.github/workflows/ci-cd.yml) |

---

*Boa apresentação — use este roteiro como espinha dorsal e adapte a voz da equipe.*
