import axios from "axios";
import { auth } from "./firebase";

/**
 * Com `npm run dev`, sempre usa baseURL vazia → mesma origem do Vite (:5173) e o proxy
 * encaminha `/api` para o backend (sem CORS no navegador).
 * Em build de produção, use VITE_API_URL com a URL pública da API.
 */
function apiBaseUrl(): string {
  if (import.meta.env.DEV) {
    return "";
  }
  const fromEnv = import.meta.env.VITE_API_URL?.trim();
  if (fromEnv) {
    return fromEnv;
  }
  return "";
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
