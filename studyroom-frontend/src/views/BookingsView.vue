<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { api } from '../lib/api'
import { auth } from '../lib/firebase'
import type { Reservation } from '../types/api'

const list = ref<Reservation[]>([])
const loading = ref(true)
const err = ref('')
const successMsg = ref('')

const uid = computed(() => auth.currentUser?.uid || '')

async function load() {
  if (!uid.value) return
  loading.value = true
  err.value = ''
  try {
    const { data } = await api.get<Reservation[]>(`/api/reservations/user/${uid.value}`)
    list.value = data
  } catch {
    err.value = 'Não foi possível carregar suas reservas. Verifique sua internet e tente de novo.'
  } finally {
    loading.value = false
  }
}

async function cancel(id: string, roomName: string) {
  const ok = confirm(
    `Deseja cancelar a reserva da sala "${roomName}"?\n\nEsta ação não pode ser desfeita pelo aplicativo.`,
  )
  if (!ok) return
  successMsg.value = ''
  try {
    await api.patch(`/api/reservations/${id}/cancel`)
    successMsg.value = 'Reserva cancelada com sucesso.'
    await load()
  } catch {
    err.value = 'Não foi possível cancelar. Tente novamente ou peça ajuda ao suporte.'
  }
}

function formatRange(s: string, e: string) {
  try {
    return `${new Date(s).toLocaleString('pt-BR')} — ${new Date(e).toLocaleString('pt-BR')}`
  } catch {
    return `${s} — ${e}`
  }
}

onMounted(load)
</script>

<template>
  <AppShell>
    <div class="max-w-4xl mx-auto px-6 sm:px-8 py-10">
      <header class="mb-8">
        <span class="text-primary font-semibold tracking-wider text-xs uppercase mb-2 block"
          >Reservas</span
        >
        <h1 class="text-3xl sm:text-4xl font-extrabold font-headline">Minhas reservas</h1>
        <p class="mt-2 text-lg text-on-surface-variant">
          Aqui você acompanha e pode cancelar agendamentos futuros.
        </p>
      </header>
      <p
        v-if="successMsg"
        class="a11y-alert text-green-900 bg-green-50 rounded-lg px-4 py-3 mb-4"
        role="status"
      >
        {{ successMsg }}
      </p>
      <p v-if="err" class="a11y-alert text-red-800 bg-red-50 rounded-lg px-4 py-3 mb-4" role="alert">
        {{ err }}
      </p>
      <p v-if="loading" class="text-on-surface-variant text-lg">Carregando suas reservas…</p>
      <ul v-else class="space-y-4">
        <li
          v-for="r in list"
          :key="r.id"
          class="rounded-xl border-2 border-outline-variant/15 bg-surface-container-low p-5 sm:p-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4"
        >
          <div>
            <p class="font-bold text-lg text-on-surface">{{ r.roomName || r.roomId }}</p>
            <p class="text-base text-on-surface-variant mt-1">{{ formatRange(r.startTime, r.endTime) }}</p>
            <p class="text-base mt-2 flex flex-wrap items-center gap-2">
              <span class="font-semibold">Status:</span>
              <StatusBadge
                v-if="r.status === 'CONFIRMED' || r.status === 'CANCELLED' || r.status === 'COMPLETED'"
                :status="r.status"
              />
              <span v-else>{{ r.status || '—' }}</span>
              <template v-if="r.partySize != null">
                · {{ r.partySize }} {{ r.partySize === 1 ? 'pessoa' : 'pessoas' }}</template
              >
            </p>
          </div>
          <div class="flex gap-2">
            <button
              v-if="r.id && r.status !== 'CANCELLED' && r.status !== 'COMPLETED'"
              type="button"
              class="a11y-btn-primary border-2 border-primary text-primary rounded-lg px-5 py-3 font-bold hover:bg-primary hover:text-white transition-colors"
              @click="cancel(r.id!, r.roomName || r.roomId)"
            >
              Cancelar reserva
            </button>
          </div>
        </li>
        <li
          v-if="!list.length"
          class="rounded-xl border border-dashed border-outline-variant/30 p-8 text-center text-lg text-on-surface-variant"
        >
          Você ainda não tem reservas. Vá em <RouterLink to="/rooms" class="text-primary font-bold underline"
            >Salas</RouterLink
          >
          para fazer a primeira.
        </li>
      </ul>
    </div>
  </AppShell>
</template>
