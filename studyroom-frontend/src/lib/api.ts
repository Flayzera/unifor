import axios from "axios";
import { auth } from "./firebase";

/**
 * Com `npm run dev`, sempre usa baseURL vazia → mesma origem do Vite (:5173) e o proxy
 * encaminha `/api` para o backend (sem CORS no navegador).
 * Em build de produção, defina VITE_API_URL na Vercel com a URL pública do backend (ex.: Render).
 */
function apiBaseUrl(): string {
  if (import.meta.env.DEV) {
    return "";
  }
  const raw = import.meta.env.VITE_API_URL?.trim() ?? "";
  if (!raw) {
    console.error(
      "[studyroom] VITE_API_URL ausente no build de produção. Na Vercel → Settings → Environment Variables, defina VITE_API_URL=https://seu-backend.onrender.com (sem barra no final). Depois faça redeploy.",
    );
    return "";
  }
  if (/localhost|127\.0\.0\.1/i.test(raw)) {
    console.error(
      "[studyroom] VITE_API_URL não pode ser localhost na Vercel (o navegador do usuário não acessa sua máquina). Troque pela URL pública do backend no Render e redeploy.",
    );
    return "";
  }
  return raw.replace(/\/$/, "");
}

export const api = axios.create({
  baseURL: apiBaseUrl(),
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use(async (config) => {
  const user = auth.currentUser;
  if (user) {
    const token = await user.getIdToken();
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
