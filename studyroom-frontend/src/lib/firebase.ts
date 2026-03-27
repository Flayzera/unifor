import { initializeApp, type FirebaseOptions } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";

function readEnv(key: keyof ImportMetaEnv): string {
  const v = import.meta.env[key];
  if (typeof v !== "string" || !v.trim()) {
    throw new Error(
      `[Firebase] Variável ${String(key)} ausente ou vazia. ` +
        "Crie o arquivo .env na pasta studyroom-frontend (copie de .env.example) e preencha com o objeto firebaseConfig do Console (app Web). " +
        "Reinicie o terminal com npm run dev após salvar o .env.",
    );
  }
  return v.trim();
}

const firebaseConfig: FirebaseOptions = {
  apiKey: readEnv("VITE_FIREBASE_API_KEY"),
  authDomain: readEnv("VITE_FIREBASE_AUTH_DOMAIN"),
  projectId: readEnv("VITE_FIREBASE_PROJECT_ID"),
  storageBucket: readEnv("VITE_FIREBASE_STORAGE_BUCKET"),
  messagingSenderId: readEnv("VITE_FIREBASE_MESSAGING_SENDER_ID"),
  appId: readEnv("VITE_FIREBASE_APP_ID"),
};

export const firebaseApp = initializeApp(firebaseConfig);
export const auth = getAuth(firebaseApp);
export const googleProvider = new GoogleAuthProvider();

/**
 * Aguarda o Auth hidratar (persistência / estado inicial).
 * Só o primeiro `onAuthStateChanged` costuma disparar com `null` antes da sessão
 * ser restaurada do IndexedDB — o router via `currentUser === null` e o login parecia falhar até dar F5.
 */
export const authInitialized = auth.authStateReady();
