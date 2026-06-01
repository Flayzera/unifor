<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import HowToUseGuide from '../components/HowToUseGuide.vue'
import { auth, authInitialized } from '../lib/firebase'

const authed = ref(false)

onMounted(async () => {
  await authInitialized
  authed.value = !!auth.currentUser
})
</script>

<template>
  <AppShell v-if="authed">
    <HowToUseGuide />
    <p class="max-w-3xl mx-auto px-6 sm:px-8 pb-10 text-base text-on-surface-variant">
      <RouterLink to="/rooms" class="text-primary font-semibold hover:underline"
        >Voltar para as salas</RouterLink
      >
    </p>
  </AppShell>
  <div v-else>
    <HowToUseGuide />
    <p class="max-w-3xl mx-auto px-6 sm:px-8 pb-10 text-base text-on-surface-variant">
      <RouterLink to="/login" class="text-primary font-semibold hover:underline"
        >Voltar ao login</RouterLink
      >
    </p>
  </div>
</template>
