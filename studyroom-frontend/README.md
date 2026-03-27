# StudyRoom — Frontend

Interface web em **Vue 3**, **TypeScript**, **Vite** e **Tailwind CSS**, com autenticação via **Firebase Auth** (SDK web) e chamadas HTTP à API Spring Boot.

## Requisitos

- **Node.js 20+** (Vite 8 e dependências exigem versão recente)
- npm

Na raiz desta pasta há um [`.nvmrc`](.nvmrc) com `20`. Com [nvm](https://github.com/nvm-sh/nvm): `nvm use` antes de `npm install` / `npm run test`.

---

## Variáveis de ambiente

1. Copie o exemplo:

   ```bash
   cp .env.example .env
   ```

2. Preencha o objeto Firebase do app **Web** (Firebase Console → Configurações do projeto → Seus apps → app Web → `firebaseConfig`):

   - `VITE_FIREBASE_API_KEY`
   - `VITE_FIREBASE_AUTH_DOMAIN`
   - `VITE_FIREBASE_PROJECT_ID`
   - `VITE_FIREBASE_STORAGE_BUCKET`
   - `VITE_FIREBASE_MESSAGING_SENDER_ID`
   - `VITE_FIREBASE_APP_ID`

3. **Produção (build na Vercel ou outro host):** defina **`VITE_API_URL`** com a URL pública do backend, **com protocolo**:

   - Correto: `https://seu-app.onrender.com`
   - Também aceito: só o host `seu-app.onrender.com` (o código prefixa `https://` automaticamente)
   - Evite `http://localhost:8080` em produção

Em **`npm run dev`**, o `VITE_API_URL` é ignorado para a API: o Vite faz **proxy** de `/api` para `http://localhost:8080` (veja `vite.config.ts`).

---

## Scripts npm

| Comando | Descrição |
|---------|-----------|
| `npm run dev` | Servidor de desenvolvimento (hot reload) |
| `npm run build` | Checagem TypeScript (`vue-tsc`) + build de produção |
| `npm run preview` | Servir o build localmente |
| `npm run test` | Testes com Vitest (config em `vitest.config.ts`) |

---

## Rotas e proteção

Definidas em `src/router/index.ts`:

- `/login` — login
- `/rooms`, `/bookings`, `/booking/confirm` — exigem autenticação (`requiresAuth`)
- `/admin` — exige usuário com claim `admin` no token Firebase

---

## Deploy na Vercel (monorepo)

1. **Root Directory** do projeto: `studyroom-frontend`
2. Build: `npm run build` — saída: `dist`
3. Variáveis de ambiente: todas as `VITE_*` acima + `VITE_API_URL`
4. O arquivo **`vercel.json`** na raiz desta pasta faz **rewrite** para `index.html`, evitando **404** ao atualizar páginas como `/login` ou `/rooms` (SPA).

### Firebase — domínios autorizados

Em Firebase Console → Authentication → **Settings** → **Authorized domains**, inclua o domínio do deploy (ex.: `seu-projeto.vercel.app`).

---

## Estrutura útil

| Caminho | Função |
|---------|--------|
| `src/lib/api.ts` | Cliente Axios + token Firebase + base URL (dev vs produção) |
| `src/lib/firebase.ts` | Inicialização do Firebase |
| `src/composables/useAuth.ts` | Estado de auth + `syncBackendProfile()` após login |
| `src/views/` | Telas principais |

---

## Testes

- Vitest + Vue Test Utils; exemplo em `src/components/StatusBadge.spec.ts`
- Configuração separada em **`vitest.config.ts`** (o `vite.config.ts` permanece só para build/dev)

---

## Documentação adicional

- Monorepo (visão geral): [`../README.md`](../README.md)
- Backend (API): [`../studyroom-backend/README.md`](../studyroom-backend/README.md)
