<script setup lang="ts">
import { useAccessibility, supportPhone, supportWhatsApp } from '../composables/useAccessibility'

const { enabled, toggleAccessibleMode } = useAccessibility()

const supportHref = supportWhatsApp
  ? `https://wa.me/${supportWhatsApp.replace(/\D/g, '')}`
  : supportPhone
    ? `tel:${supportPhone.replace(/\s/g, '')}`
    : ''
</script>

<template>
  <div class="a11y-toggle-root" aria-live="polite">
    <a
      v-if="supportHref"
      :href="supportHref"
      class="a11y-help-chip"
      :target="supportWhatsApp ? '_blank' : undefined"
      :rel="supportWhatsApp ? 'noopener noreferrer' : undefined"
      title="Falar com a equipe de extensão"
    >
      <span class="material-symbols-outlined text-[20px]" aria-hidden="true">support_agent</span>
      <span class="hidden sm:inline">Ajuda</span>
    </a>

    <button
      type="button"
      class="a11y-fab"
      :class="enabled ? 'a11y-fab--on' : 'a11y-fab--off'"
      :aria-pressed="enabled"
      :aria-label="
        enabled
          ? 'Desativar modo fácil de ler e voltar à aparência padrão'
          : 'Ativar modo fácil de ler: texto maior e alto contraste'
      "
      @click="toggleAccessibleMode"
    >
      <span class="material-symbols-outlined text-[22px]" aria-hidden="true">
        {{ enabled ? 'close' : 'accessibility_new' }}
      </span>
      <span class="a11y-fab__label">{{ enabled ? 'Modo normal' : 'Modo fácil' }}</span>
    </button>
  </div>
</template>
