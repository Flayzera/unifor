<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppShell from '../components/AppShell.vue'
import BookingSummaryCard from '../components/BookingSummaryCard.vue'

const route = useRoute()
const id = computed(() => (route.query.id as string) || '')
const roomLabel = computed(() => {
  const name = route.query.roomName as string | undefined
  const idQ = route.query.room as string | undefined
  return name || idQ || 'Sala reservada'
})
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
    return new Date(iso).toLocaleString('pt-BR')
  } catch {
    return iso
  }
}
</script>

<template>
  <AppShell>
    <div class="max-w-lg mx-auto px-6 py-12 sm:py-16 text-center">
      <div
        class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-100 text-green-900 mb-6"
        role="img"
        aria-label="Sucesso"
      >
        <span class="material-symbols-outlined text-4xl">check_circle</span>
      </div>
      <h1 class="text-3xl font-extrabold font-headline mb-3 text-on-surface">
        Reserva confirmada
      </h1>
      <p class="text-on-surface-variant text-lg mb-8 leading-relaxed">
        Sua reserva foi registrada com sucesso. Você pode ver todos os detalhes em
        <strong>Minhas reservas</strong>.
      </p>

      <BookingSummaryCard
        v-if="start && end"
        class="text-left mb-8"
        :room-name="roomLabel"
        :start-label="fmt(start)"
        :end-label="fmt(end)"
        :party-size="partySize"
      />
      <p v-else-if="id" class="text-base text-on-surface-variant mb-8">
        Código da reserva: <strong>{{ id }}</strong>
      </p>

      <div class="flex flex-col sm:flex-row gap-3 justify-center">
        <RouterLink
          to="/bookings"
          class="a11y-btn-primary inline-block primary-gradient text-white font-bold px-8 py-4 rounded-lg"
        >
          Ver minhas reservas
        </RouterLink>
        <RouterLink
          to="/rooms"
          class="a11y-touch-target inline-block border-2 border-primary text-primary font-bold px-8 py-4 rounded-lg hover:bg-primary/5"
        >
          Voltar às salas
        </RouterLink>
      </div>
    </div>
  </AppShell>
</template>
