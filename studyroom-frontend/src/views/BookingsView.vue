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

const uid = computed(() => auth.currentUser?.uid || '')

async function load() {
  if (!uid.value) return
  loading.value = true
  err.value = ''
  try {
    const { data } = await api.get<Reservation[]>(`/api/reservations/user/${uid.value}`)
    list.value = data
  } catch {
    err.value = 'Não foi possível carregar suas reservas.'
  } finally {
    loading.value = false
  }
}

async function cancel(id: string) {
  if (!confirm('Cancelar esta reserva?')) return
  try {
    await api.patch(`/api/reservations/${id}/cancel`)
    await load()
  } catch {
    err.value = 'Falha ao cancelar.'
  }
}

function formatRange(s: string, e: string) {
  try {
    return `${new Date(s).toLocaleString()} — ${new Date(e).toLocaleString()}`
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
        <span class="text-primary font-semibold tracking-wider text-xs uppercase mb-2 block">Reservas</span>
        <h2 class="text-3xl font-extrabold font-headline">Minhas reservas</h2>
      </header>
      <p v-if="err" class="text-red-700 text-sm mb-4">{{ err }}</p>
      <p v-if="loading" class="text-on-surface-variant">Carregando…</p>
      <ul v-else class="space-y-3">
        <li
          v-for="r in list"
          :key="r.id"
          class="rounded-xl border border-outline-variant/15 bg-surface-container-low p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3"
        >
          <div>
            <p class="font-semibold">{{ r.roomName || r.roomId }}</p>
            <p class="text-sm text-on-surface-variant">{{ formatRange(r.startTime, r.endTime) }}</p>
            <p class="text-xs mt-1 text-stone-500 flex flex-wrap items-center gap-2">
              <span>Status:</span>
              <StatusBadge
                v-if="r.status === 'CONFIRMED' || r.status === 'CANCELLED' || r.status === 'COMPLETED'"
                :status="r.status"
              />
              <span v-else>{{ r.status || '—' }}</span>
              <template v-if="r.partySize != null"> · {{ r.partySize }} pessoa(s)</template>
            </p>
          </div>
          <div class="flex gap-2">
            <button
              v-if="r.id && r.status !== 'CANCELLED' && r.status !== 'COMPLETED'"
              type="button"
              class="text-sm border border-primary text-primary rounded-lg px-3 py-1.5 hover:bg-primary hover:text-white transition-colors"
              @click="cancel(r.id!)"
            >
              Cancelar
            </button>
          </div>
        </li>
        <li v-if="!list.length" class="text-on-surface-variant text-sm">Nenhuma reserva ainda.</li>
      </ul>
    </div>
  </AppShell>
</template>
