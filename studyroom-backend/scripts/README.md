# Scripts Firebase (Admin)

## Primeiro administrador (`admin: true`)

Quando ainda não existe nenhum admin no sistema, você precisa definir a **custom claim** `admin` no Firebase Auth para o seu usuário.

### Pré-requisitos

- Node.js 16+ (recomendado 18+)
- Arquivo JSON da **conta de serviço** do mesmo projeto Firebase do backend (o mesmo que você usa no Render/local).

### Passos

```bash
cd studyroom-backend/scripts
npm install
node set-admin-claim.mjs SEU_UID_DO_FIREBASE
```

O **UID** aparece no Firebase Console → Authentication → Users (coluna User UID).

Opcional — caminho explícito do JSON:

```bash
node set-admin-claim.mjs SEU_UID /caminho/absoluto/para/serviceAccount.json
```

Ou defina a variável (como no Render):

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/caminho/para/serviceAccount.json
node set-admin-claim.mjs SEU_UID
```

Depois: **logout e login de novo** no app para o token trazer `claims.admin === true`.

### Depois do primeiro admin

Use a API (como admin autenticado):

`POST /api/users/{uid}/promote`

para promover outros usuários.
