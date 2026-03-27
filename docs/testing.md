# Testes — como rodar localmente e no CI

Este repositório tem dois pacotes com testes automatizados: **backend** (Spring Boot + JUnit) e **frontend** (Vitest + Vue Test Utils).

## Pré-requisitos

| Pacote | Requisito |
|--------|-----------|
| Backend | JDK **17** (mesma versão do CI) |
| Frontend | **Node.js 20+** — veja [`studyroom-frontend/.nvmrc`](../studyroom-frontend/.nvmrc) e `engines` em [`package.json`](../studyroom-frontend/package.json) |

Com [nvm](https://github.com/nvm-sh/nvm):

```bash
cd studyroom-frontend
nvm use
```

---

## Backend (`studyroom-backend`)

Na raiz do backend:

```bash
cd studyroom-backend
./mvnw test
```

- Compila o projeto e executa todos os testes unitários e de integração leve (por exemplo `MockMvc` standalone, serviços com Mockito).
- Para rodar **uma classe** só:

  ```bash
  ./mvnw test -Dtest=UserControllerMvcTest
  ```

- O job de CI usa **`./mvnw verify`**, que inclui `test` e fases adicionais do Maven (por exemplo verificação de empacotamento, conforme o `pom.xml`).

Não é necessário Firebase/Firestore rodando para a suíte de testes atual: dependências externas costumam ser mockadas.

---

## Frontend (`studyroom-frontend`)

```bash
cd studyroom-frontend
npm ci
npm test
```

O script `test` em [`package.json`](../studyroom-frontend/package.json) executa **`vitest run`** (uma execução, sem modo *watch*).

Para modo interativo com *watch* (útil no desenvolvimento), use o binário do Vitest diretamente:

```bash
npx vitest
```

---

## CI (GitHub Actions)

O workflow [`.github/workflows/ci-cd.yml`](../.github/workflows/ci-cd.yml) roda:

- **Backend:** `working-directory: studyroom-backend` → `./mvnw verify` e build da imagem Docker.
- **Frontend:** Node 20, `npm ci`, `npm run test`, `npm run build`.

Push ou pull request para `main` ou `dev` disparam esses jobs.
