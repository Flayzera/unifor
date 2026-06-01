<script setup lang="ts">
defineProps<{
  roomName: string
  startLabel: string
  endLabel: string
  partySize?: number | null
}>()
</script>

<template>
  <div
    class="rounded-xl border-2 border-outline-variant/25 bg-surface-container-low p-4 sm:p-5 space-y-3"
    role="region"
    aria-label="Resumo da reserva"
  >
    <h3 class="font-headline font-bold text-lg text-on-surface">Resumo da reserva</h3>
    <dl class="space-y-2 text-base">
      <div class="flex flex-col sm:flex-row sm:gap-2">
        <dt class="font-semibold text-on-surface min-w-28">Sala</dt>
        <dd class="text-on-surface">{{ roomName }}</dd>
      </div>
      <div class="flex flex-col sm:flex-row sm:gap-2">
        <dt class="font-semibold text-on-surface min-w-28">Início</dt>
        <dd class="text-on-surface">{{ startLabel }}</dd>
      </div>
      <div class="flex flex-col sm:flex-row sm:gap-2">
        <dt class="font-semibold text-on-surface min-w-28">Fim</dt>
        <dd class="text-on-surface">{{ endLabel }}</dd>
      </div>
      <div v-if="partySize != null" class="flex flex-col sm:flex-row sm:gap-2">
        <dt class="font-semibold text-on-surface min-w-28">Pessoas</dt>
        <dd class="text-on-surface">{{ partySize }}</dd>
      </div>
    </dl>
  </div>
</template>
