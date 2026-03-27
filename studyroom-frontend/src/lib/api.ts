import axios from "axios";
import { auth } from "./firebase";
import { resolveApiBaseUrl } from "./apiBaseUrl";

/**
 * Com `npm run dev`, sempre usa baseURL vazia → mesma origem do Vite (:5173) e o proxy
 * encaminha `/api` para o backend (sem CORS no navegador).
 * Em build de produção, defina VITE_API_URL na Vercel com a URL pública do backend (ex.: Render).
 */
function apiBaseUrl(): string {
  return resolveApiBaseUrl({
    viteApiUrl: import.meta.env.VITE_API_URL,
    isDev: import.meta.env.DEV,
  });
}

export const api = axios.create({
  baseURL: apiBaseUrl(),
  headers: { "Content-Type": "application/json" },
  timeout: 20000,
});

api.interceptors.request.use(async (config) => {
  const user = auth.currentUser;
  if (user) {
    const token = await user.getIdToken();
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
