/**
 * Calcula a base URL da API em produção (build Vite).
 * Em desenvolvimento o Vite usa proxy; o caller deve passar isDev=true.
 */
export function resolveApiBaseUrl(options: {
  viteApiUrl: string | undefined
  isDev: boolean
  logError?: (msg: string) => void
}): string {
  const { viteApiUrl, isDev, logError = console.error } = options

  if (isDev) {
    return ""
  }

  const raw = viteApiUrl?.trim() ?? ""
  if (!raw) {
    logError(
      "[studyroom] VITE_API_URL ausente no build de produção. Na Vercel → Settings → Environment Variables, defina VITE_API_URL=https://seu-backend.onrender.com (sem barra no final). Depois faça redeploy.",
    )
    return ""
  }
  if (/localhost|127\.0\.0\.1/i.test(raw)) {
    logError(
      "[studyroom] VITE_API_URL não pode ser localhost na Vercel (o navegador do usuário não acessa sua máquina). Troque pela URL pública do backend no Render e redeploy.",
    )
    return ""
  }

  let base = raw.replace(/\/$/, "")
  if (!/^https?:\/\//i.test(base)) {
    base = `https://${base}`
  }
  return base
}
