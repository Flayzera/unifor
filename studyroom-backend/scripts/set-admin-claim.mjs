#!/usr/bin/env node
/**
 * Define custom claim `admin: true` no Firebase Auth.
 *
 * Recomendado: Node.js 18+ (ou 16 com firebase-admin 11 abaixo).
 *
 *   cd studyroom-backend/scripts && npm install
 *   node set-admin-claim.mjs SEU_UID_FIREBASE
 *
 * Opcional: 2º argumento = caminho absoluto do JSON da conta de serviço.
 */

import admin from "firebase-admin";
import { readFileSync, existsSync } from "fs";
import { dirname, resolve } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));

const uid = process.argv[2];
const pathFromArg = process.argv[3];
const pathFromEnv = process.env.GOOGLE_APPLICATION_CREDENTIALS?.trim();
const defaultRelative = resolve(
  __dirname,
  "../src/main/resources/study-room-firebase-adminsdk.json",
);

const credPath = pathFromArg || pathFromEnv || defaultRelative;

if (!uid) {
  console.error("Uso: node set-admin-claim.mjs <UID_DO_FIREBASE_AUTH> [caminho-do-json-opcional]");
  process.exit(1);
}

if (!existsSync(credPath)) {
  console.error(
    "Não achei o JSON da conta de serviço em:\n  " +
      credPath +
      "\n\nPasse o caminho absoluto como 2º argumento ou defina GOOGLE_APPLICATION_CREDENTIALS.\n" +
      "Arquivo esperado pelo backend: src/main/resources/study-room-firebase-adminsdk.json",
  );
  process.exit(1);
}

const json = JSON.parse(readFileSync(credPath, "utf8"));
const projectId = json.project_id;
if (!projectId) {
  console.error("O JSON não contém project_id.");
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(json),
  projectId,
});

await admin.auth().setCustomUserClaims(uid, { admin: true });
console.log(`OK: usuário ${uid} agora tem claim admin (projeto ${projectId}). Faça logout/login no front.`);
