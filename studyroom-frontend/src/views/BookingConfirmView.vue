<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppShell from '../components/AppShell.vue'

const route = useRoute()
const id = computed(() => (route.query.id as string) || '')
const start = computed(() => (route.query.start as string) || '')
const end = computed(() => (route.query.end as string) || '')
const partySize = computed(() => {
  const q = route.query.partySize
  const s = Array.isArray(q) ? q[0] : q
  const n = parseInt(String(s || ''), 10)
  return Number.isFinite(n) && n > 0 ? n : null
})

function fmt(iso: string) {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}
</script>

<template>
  <AppShell>
    <div class="max-w-lg mx-auto px-6 py-16 text-center">
      <div
        class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-100 text-green-800 mb-6"
      >
        <span class="material-symbols-outlined text-3xl">check_circle</span>
      </div>
      <h1 class="text-2xl font-extrabold font-headline mb-2">Reserva confirmada</h1>
      <p class="text-on-surface-variant text-sm mb-6">
        Sua solicitação foi registrada. Você pode gerenciar o agendamento em <strong>Minhas reservas</strong>.
      </p>
      <div class="rounded-xl border border-outline-variant/20 bg-surface-container-low text-left p-5 text-sm space-y-2">
        <p v-if="id"><span class="text-on-surface-variant">ID:</span> {{ id }}</p>
        <p v-if="start"><span class="text-on-surface-variant">Início:</span> {{ fmt(start) }}</p>
        <p v-if="end"><span class="text-on-surface-variant">Fim:</span> {{ fmt(end) }}</p>
        <p v-if="partySize != null">
          <span class="text-on-surface-variant">Pessoas:</span> {{ partySize }}
        </p>
      </div>
      <RouterLink
        to="/bookings"
        class="inline-block mt-8 primary-gradient text-white font-semibold px-6 py-3 rounded-lg"
      >
        Ver minhas reservas
      </RouterLink>
    </div>
  </AppShell>
</template>
