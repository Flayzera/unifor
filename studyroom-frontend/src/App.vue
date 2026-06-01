<script setup lang="ts">
import { RouterView } from 'vue-router'
import AccessibilityToggle from './components/AccessibilityToggle.vue'
</script>

<template>
  <a href="#main-content" class="a11y-skip-link">Ir para o conteúdo principal</a>
  <AccessibilityToggle />
  <div id="main-content">
    <RouterView />
  </div>
</template>
