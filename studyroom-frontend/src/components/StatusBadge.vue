<template>
  <span
    class="inline-flex items-center rounded-full px-2 py-1 text-xs font-semibold"
    :class="colorClass"
    data-testid="status-badge"
  >
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: 'CONFIRMED' | 'CANCELLED' | 'COMPLETED'
}>()

const label = computed(() => {
  if (props.status === 'CONFIRMED') return 'confirmada'
  if (props.status === 'CANCELLED') return 'cancelada'
  return 'concluída'
})

const colorClass = computed(() => {
  if (props.status === 'CONFIRMED') return 'bg-green-100 text-green-700'
  if (props.status === 'CANCELLED') return 'bg-red-100 text-red-700'
  return 'bg-blue-100 text-blue-700'
})
</script>
