import { createRouter, createWebHistory } from 'vue-router'
import { auth, authInitialized } from '../lib/firebase'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    /**
     * Login com import estático (não lazy): após deploy no Vercel, um index.html em cache
     * ainda apontava para um chunk LoginView-*.js antigo → "Failed to fetch dynamically imported module"
     * ao dar logout e carregar /login. O bundle principal e o login ficam alinhados.
     */
    { path: '/login', name: 'login', component: LoginView },
    { path: '/rooms', name: 'rooms', component: () => import('../views/RoomsView.vue'), meta: { requiresAuth: true } },
    { path: '/bookings', name: 'bookings', component: () => import('../views/BookingsView.vue'), meta: { requiresAuth: true } },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/booking/confirm',
      name: 'booking-confirm',
      component: () => import('../views/BookingConfirmView.vue'),
      meta: { requiresAuth: true },
    },
    { path: '/', redirect: '/rooms' },
  ],
})

router.beforeEach(async (to) => {
  await authInitialized
  await auth.authStateReady()
  const u = auth.currentUser
  if (to.meta.requiresAuth && !u) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && u) {
    return { name: 'rooms' }
  }
  if (to.meta.requiresAdmin) {
    if (!u) return { name: 'login' }
    const token = await u.getIdTokenResult()
    if (token.claims.admin !== true) {
      return { name: 'rooms' }
    }
  }
  return true
})

export default router
