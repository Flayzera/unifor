import { computed, ref } from 'vue'
import { onAuthStateChanged, signOut, type User } from 'firebase/auth'
import axios from 'axios'
import { auth } from '../lib/firebase'

const user = ref<User | null>(null)
const isAdmin = ref(false)

onAuthStateChanged(auth, async (u) => {
  user.value = u
  if (u) {
    const token = await u.getIdTokenResult()
    isAdmin.value = token.claims.admin === true
  } else {
    isAdmin.value = false
  }
})

export function useAuth() {
  const displayName = computed(
    () => user.value?.displayName || user.value?.email?.split('@')[0] || 'Usuário'
  )

  async function logout() {
    await signOut(auth)
  }

  async function refreshClaims() {
    if (user.value) {
      const token = await user.value.getIdTokenResult(true)
      isAdmin.value = token.claims.admin === true
    }
  }

  return { user, isAdmin, displayName, logout, refreshClaims }
}

/** Após login, garante documento em Firestore via API. */
export async function syncBackendProfile() {
  const u = auth.currentUser
  if (!u) return
  const { api } = await import('../lib/api')
  try {
    await api.get('/api/users/me')
    return
  } catch (e) {
    if (axios.isAxiosError(e) && e.response?.status === 401) {
      console.warn('[studyroom] Backend recusou o token (401). Confira se o backend está no mesmo projeto Firebase.')
      return
    }
    // 404 = perfil ainda não existe no Firestore; demais erros no GET também seguem para tentar POST
  }
  try {
    const name = u.displayName || u.email?.split('@')[0] || 'Usuário'
    const email = u.email || ''
    await api.post('/api/users', { name, email })
  } catch (e) {
    console.warn('[studyroom] Não foi possível criar o perfil na API.', e)
  }
}
