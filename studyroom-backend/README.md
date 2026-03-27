# API StudyRoom

Backend desenvolvido em Java (Spring Boot) com banco de dados NoSQL (Firebase Firestore) e Autenticação (Firebase Auth).

## ⚠️ Configuração do Firebase:

Para rodar este projeto localmente, você precisa do arquivo de credenciais do Firebase Admin SDK.

Crie um projeto no Firebase.

Vá em Configurações > Contas de Serviço > Gerar nova chave privada.

Renomeie o arquivo baixado para study-room-firebase-adminsdk.json.

Coloque o arquivo dentro da pasta src/main/resources/.

## Autenticação

Todas as requisições (exceto rotas públicas, se houver) exigem o envio do ID Token do Firebase no header.

- **Header:** `Authorization: Bearer <SEU_FIREBASE_ID_TOKEN>`

## Fluxo de Usuário (User)

1. O Front-end faz o login usando Firebase Auth (Google, Email/Senha).
2. Imediatamente após pegar o `uid` e gerar o Token, faça um POST para `/api/users` enviando `{ "name": "...", "email": "..." }` para criar o perfil no Firestore.

- `GET /api/users/me` -> Retorna os dados do perfil logado.
- `POST /api/users/{uid}/promote` -> (Apenas ADM) Torna outro usuário administrador.

## Fluxo de Salas (Room)

- `GET /api/rooms` -> Retorna todas as salas.
- `GET /api/rooms/available?start=ISO&end=ISO` -> Retorna salas livres num período.
- `POST /api/rooms` -> (Apenas ADM) Cria nova sala `{ "name": "A1", "capacity": 10... }`.
- `GET /api/rooms/status` -> (Apenas ADM) Retorna dashboard de ocupação atual.

## Fluxo de Reservas (Reservation)

- Datas devem ser enviadas no formato ISO UTC: `2026-03-12T14:00:00.000Z`
- `POST /api/reservations` -> Cria reserva `{ "roomId": "id_da_sala", "startTime": "...", "endTime": "..." }`.
- `GET /api/reservations/user/{userId}` -> Retorna reservas de um aluno específico.
- `PATCH /api/reservations/{id}/cancel` -> Cancela a reserva (Obrigatório ser o dono ou ADM).
- `PATCH /api/reservations/{id}/reschedule` -> Remarca horário `{ "newStart": "...", "newEnd": "..." }`.

**Exclusivos do Administrador:**

- `GET /api/reservations` -> Histórico global.
- `GET /api/reservations/room/{roomId}` -> Visão por sala.
- `GET /api/reservations/timeframe?start=ISO&end=ISO` -> Visão por período (agenda).
