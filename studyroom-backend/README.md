# StudyRoom — Backend (API)

API REST em **Java 17** + **Spring Boot**, com **Firebase Admin SDK** para validar tokens (**Firebase Auth**) e acesso ao **Firestore**.

## Requisitos

- JDK 17
- Maven (ou use `./mvnw` na raiz desta pasta)
- Projeto Firebase com Firestore e Authentication
- Arquivo JSON da **conta de serviço** (Firebase Console → Configurações → Contas de serviço → Gerar nova chave privada)

---

## Credenciais Firebase

### Desenvolvimento local (classpath)

1. Renomeie o JSON baixado para `study-room-firebase-adminsdk.json`.
2. Coloque em: `src/main/resources/study-room-firebase-adminsdk.json`  
3. **Não commite** este arquivo (adicione ao `.gitignore` se necessário).

### Produção (Docker / Render / outro host)

O código aceita credenciais nesta ordem:

1. **`FIREBASE_SERVICE_ACCOUNT_JSON`** — conteúdo JSON completo da conta de serviço (uma linha ou variável de ambiente).
2. **`GOOGLE_APPLICATION_CREDENTIALS`** — caminho absoluto para um arquivo JSON no disco (ex.: secret montado em `/etc/secrets/firebase-admin.json`).
3. Fallback: recurso no classpath `study-room-firebase-adminsdk.json` (adequado ao dev local).

No **Render**, use *Secret File* + `GOOGLE_APPLICATION_CREDENTIALS` apontando para o caminho do arquivo montado.

---

## Perfis Spring

| Perfil | Arquivo | Uso |
|--------|---------|-----|
| default | `application.properties` | Base |
| `dev` | `application-dev.properties` | Logs de request mais verbosos |
| `prod` | `application-prod.properties` | Produção; `feature.external-fact.enabled=false` por padrão |

Exemplo:

```bash
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

---

## Como executar

```bash
chmod +x mvnw   # uma vez, se necessário
./mvnw spring-boot:run
```

Servidor HTTP padrão: **porta 8080**.

### Testes e JAR

```bash
./mvnw verify
./mvnw package
```

O JAR gerado fica em `target/`.

---

## Docker

```bash
docker build -t studyroom-backend .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e GOOGLE_APPLICATION_CREDENTIALS=/run/secrets/sa.json \
  -v /caminho/local/sa.json:/run/secrets/sa.json:ro \
  studyroom-backend
```

Na raiz do monorepo:

```bash
docker build -f studyroom-backend/Dockerfile -t studyroom-backend studyroom-backend
```

---

## Autenticação

Todas as rotas sob `/api/**` exigem usuário autenticado, salvo configuração explícita de segurança.

Envie o ID Token do Firebase:

```http
Authorization: Bearer <FIREBASE_ID_TOKEN>
```

Papéis:

- **`ROLE_ADMIN`** — quando a custom claim `admin: true` está no token Firebase.
- **`ROLE_STUDENT`** — demais usuários autenticados.

---

## Rotas principais

### Usuários

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/api/users` | Cria/sincroniza perfil do usuário logado (`name`, `email`) |
| `GET` | `/api/users/me` | Perfil do usuário logado (**404** se ainda não existir documento) |
| `POST` | `/api/users/{uid}/promote` | Promove usuário a admin (**só admin**) |

### Salas

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET` | `/api/rooms` | Lista salas |
| `GET` | `/api/rooms/available?start=...&end=...` | Salas livres no intervalo (ISO UTC) |
| `POST` | `/api/rooms` | Cria sala (**admin**) |
| `DELETE` | `/api/rooms/{id}` | Remove sala (**admin**) |
| `GET` | `/api/rooms/status` | Dashboard (**admin**) |

### Reservas

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/api/reservations` | Nova reserva |
| `GET` | `/api/reservations` | Todas (**admin**) |
| `GET` | `/api/reservations/user/{userId}` | Por usuário |
| `GET` | `/api/reservations/room/{roomID}` | Por sala (**admin**) |
| `GET` | `/api/reservations/timeframe?start=&end=` | Por período (**admin**) |
| `PATCH` | `/api/reservations/{id}/cancel` | Cancelar |
| `PATCH` | `/api/reservations/{id}/complete` | Concluir |
| `PATCH` | `/api/reservations/{id}/reschedule` | Remarcar |

Datas em **ISO UTC**, exemplo: `2026-03-12T14:00:00.000Z`.

### Integração opcional (bônus)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET` | `/api/integrations/random-fact` | Fato aleatório (API externa); pode retornar **503** se `feature.external-fact.enabled=false` |

---

## Contrato OpenAPI

Veja [`openapi.yaml`](openapi.yaml) na mesma pasta deste README.

---

## Primeiro administrador (sem outro admin no sistema)

Use o script Node que define a custom claim `admin: true` no Firebase Auth:

```bash
cd scripts
npm install
node set-admin-claim.mjs <UID_DO_USUARIO>
```

Instruções completas: [`scripts/README.md`](scripts/README.md).

Depois, faça **logout e login** no app para o token renovar.  
Demais promoções: `POST /api/users/{uid}/promote` com um admin autenticado.

---

## Documentação adicional

- Monorepo (visão geral): [`../README.md`](../README.md)
