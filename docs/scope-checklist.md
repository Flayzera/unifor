# Scope Checklist — Status atual

> Atualize os itens **fora do repositório** (equipe, vídeo, board do GitHub) conforme a entrega da disciplina.

## 1) Planejamento e organização

- [ ] Equipe definida com papéis formais (fora do código)
- [ ] Repositório GitHub **público** (confirmar na organização da disciplina)
- [x] README detalhado — raiz [`README.md`](../README.md), backend [`studyroom-backend/README.md`](../studyroom-backend/README.md), frontend [`studyroom-frontend/README.md`](../studyroom-frontend/README.md)
- [x] Padrão de branch + commits semânticos — seção **Contribuindo** no [`README.md`](../README.md#contribuindo)
- [ ] GitHub Projects / Issues em uso para tarefas (fora do código)

## 2) Requisitos funcionais da aplicação web

- [x] Autenticação e autorização (admin vs usuário autenticado)
- [x] API REST com rotas principais documentadas em OpenAPI
- [x] Operações de CRUD nas entidades expostas (salas, reservas, perfil de usuário)
- [x] Validação no backend (Bean Validation + regras de negócio)
- [x] Logs de acesso (filtro de request) e de erro (`GlobalExceptionHandler`)
- [x] Contrato da API — [`studyroom-backend/openapi.yaml`](../studyroom-backend/openapi.yaml)

## 3) Arquitetura técnica

- [x] Frontend com framework moderno (Vue 3)
- [x] Frontend implantável em nuvem (ex.: Vercel) — ver [`studyroom-frontend/README.md`](../studyroom-frontend/README.md) (`vercel.json`, `VITE_*`)
- [x] Backend containerizado — [`studyroom-backend/Dockerfile`](../studyroom-backend/Dockerfile)
- [x] Backend implantável em nuvem (ex.: Render) — ver [`studyroom-backend/README.md`](../studyroom-backend/README.md)
- [x] Banco gerenciado (Firebase Firestore + Auth)
- [x] Persistência fora do container (Firestore)

## 4) DevOps e CI/CD

- [x] Docker no backend
- [x] Pipeline GitHub Actions — [`.github/workflows/ci-cd.yml`](../.github/workflows/ci-cd.yml)
- [x] Suporte a deploy automático via *hooks* (secrets opcionais)
- [ ] Secrets `BACKEND_DEPLOY_HOOK_URL` / `FRONTEND_DEPLOY_HOOK_URL` configurados no GitHub (opcional se o deploy for só manual na Vercel/Render)

## 5) Segurança e boas práticas

- [x] Credenciais via `.env` / secrets / variáveis de ambiente (sem commit de segredos)
- [x] Rotas protegidas no front e no back
- [x] Tratamento centralizado de erros
- [x] Separação `dev` / `prod` (`application-*.properties`)

## 6) Testes e qualidade

- [x] Backend — testes automatizados (JUnit, contexto Spring)
- [x] Frontend — pelo menos um teste de componente (Vitest + `StatusBadge.spec.ts`)
- [ ] Relatório de cobertura publicado no CI (melhoria opcional)

## 7) Entregáveis

- [x] Código organizado + Dockerfile do backend
- [x] Modelo de relatório — [`technical-report-template.md`](technical-report-template.md)
- [ ] Relatório técnico final (conteúdo da equipe, máx. 6 páginas)
- [ ] Vídeo de demonstração (até 7 min) — roteiro sugerido: [`roteiro-video-7min.md`](roteiro-video-7min.md)

## 8) Diferenciais (bônus)

- [ ] Cache (Redis, Cloudflare, etc.)
- [ ] Monitoramento (métricas / dashboards)
- [x] Feature flag — `feature.external-fact.enabled` (ex.: `application-prod.properties`)
- [x] Integração com API pública externa — `GET /api/integrations/random-fact` (catfact.ninja)
