<script setup lang="ts">
import { useAuth } from "../composables/useAuth";
import { supportPhone, supportWhatsApp } from "../composables/useAccessibility";
import { useRoute, useRouter } from "vue-router";

const { isAdmin, displayName, logout } = useAuth();
const router = useRouter();
const current = useRoute();

const supportHref = supportWhatsApp
  ? `https://wa.me/${supportWhatsApp.replace(/\D/g, "")}`
  : supportPhone
    ? `tel:${supportPhone.replace(/\s/g, "")}`
    : "";

function navClass(path: string) {
  const active = current.path === path || current.path.startsWith(path + "/");
  return active
    ? "text-orange-700 font-semibold border-b-2 border-orange-600 pb-1 a11y-touch-target"
    : "text-stone-500 hover:text-orange-600 transition-colors a11y-touch-target";
}
</script>

<template>
  <nav
    class="fixed top-0 w-full z-50 glass-nav border-b border-orange-100/20 shadow-sm"
    aria-label="Menu principal"
  >
    <div
      class="flex justify-between items-center px-6 sm:px-8 h-16 w-full mx-auto max-w-7xl"
    >
      <div class="flex items-center gap-6 sm:gap-8">
        <RouterLink
          to="/rooms"
          class="text-xl font-bold tracking-tight text-stone-900 font-headline a11y-touch-target"
        >
          Study Room
        </RouterLink>
        <div class="hidden md:flex items-center gap-5 text-sm">
          <RouterLink
            :class="navClass('/rooms')"
            to="/rooms"
            :aria-current="current.path === '/rooms' ? 'page' : undefined"
            >Salas</RouterLink
          >
          <RouterLink
            :class="navClass('/bookings')"
            to="/bookings"
            :aria-current="
              current.path.startsWith('/bookings') ? 'page' : undefined
            "
            >Minhas reservas</RouterLink
          >
          <RouterLink
            :class="navClass('/como-usar')"
            to="/como-usar"
            :aria-current="current.path === '/como-usar' ? 'page' : undefined"
            >Como usar</RouterLink
          >
          <RouterLink
            v-if="isAdmin"
            :class="navClass('/admin')"
            to="/admin"
            :aria-current="
              current.path.startsWith('/admin') ? 'page' : undefined
            "
            >Admin</RouterLink
          >
        </div>
      </div>
      <div class="flex items-center gap-3 text-sm text-stone-600">
        <span class="hidden sm:inline max-w-40 truncate" :title="displayName">{{
          displayName
        }}</span>
        <button
          type="button"
          class="a11y-touch-target rounded-lg border border-stone-200 px-3 py-1.5 text-stone-600 hover:bg-orange-50 focus-visible:outline focus-visible:outline-primary"
          @click="logout().then(() => router.push('/login'))"
        >
          Sair
        </button>
      </div>
    </div>
  </nav>
  <div class="pt-16 min-h-screen pb-24 md:pb-8">
    <slot />
    <footer
      v-if="supportHref"
      class="max-w-7xl mx-auto px-6 sm:px-8 py-6 mt-8 border-t border-outline-variant/15 text-center text-base text-on-surface-variant"
    >
      <p class="font-semibold text-on-surface mb-1">
        Precisa de ajuda para usar o sistema?
      </p>
      <a
        :href="supportHref"
        class="text-primary font-bold underline a11y-touch-target inline-block mt-1"
        :target="supportWhatsApp ? '_blank' : undefined"
        :rel="supportWhatsApp ? 'noopener noreferrer' : undefined"
      >
        {{ supportWhatsApp ? "Falar no WhatsApp" : "Ligar para suporte" }}
      </a>
    </footer>
  </div>
  <nav
    class="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-md border-t border-stone-200 flex justify-around items-center py-2.5"
    aria-label="Menu inferior"
  >
    <RouterLink
      to="/rooms"
      class="flex flex-col items-center gap-0.5 min-w-14 a11y-touch-target"
      :class="
        current.path === '/rooms' || current.path === '/'
          ? 'text-orange-700'
          : 'text-stone-500'
      "
      :aria-current="
        current.path === '/rooms' || current.path === '/' ? 'page' : undefined
      "
    >
      <span
        class="material-symbols-outlined text-[22px]"
        :style="
          current.path === '/rooms' || current.path === '/'
            ? 'font-variation-settings: \'FILL\' 1'
            : undefined
        "
        >dashboard</span
      >
      <span class="text-[0.65rem] font-medium">Salas</span>
    </RouterLink>
    <RouterLink
      to="/bookings"
      class="flex flex-col items-center gap-0.5 min-w-14 a11y-touch-target"
      :class="
        current.path.startsWith('/bookings')
          ? 'text-orange-700'
          : 'text-stone-500'
      "
      :aria-current="current.path.startsWith('/bookings') ? 'page' : undefined"
    >
      <span
        class="material-symbols-outlined text-[22px]"
        :style="
          current.path.startsWith('/bookings')
            ? 'font-variation-settings: \'FILL\' 1'
            : undefined
        "
        >event_available</span
      >
      <span class="text-[0.65rem] font-medium">Reservas</span>
    </RouterLink>
    <RouterLink
      to="/como-usar"
      class="flex flex-col items-center gap-0.5 min-w-14 a11y-touch-target"
      :class="
        current.path === '/como-usar' ? 'text-orange-700' : 'text-stone-500'
      "
      :aria-current="current.path === '/como-usar' ? 'page' : undefined"
    >
      <span class="material-symbols-outlined text-[22px]">help</span>
      <span class="text-[0.65rem] font-medium">Ajuda</span>
    </RouterLink>
    <RouterLink
      v-if="isAdmin"
      to="/admin"
      class="flex flex-col items-center gap-0.5 min-w-14 a11y-touch-target"
      :class="
        current.path.startsWith('/admin') ? 'text-orange-700' : 'text-stone-500'
      "
      :aria-current="current.path.startsWith('/admin') ? 'page' : undefined"
    >
      <span
        class="material-symbols-outlined text-[22px]"
        :style="
          current.path.startsWith('/admin')
            ? 'font-variation-settings: \'FILL\' 1'
            : undefined
        "
        >settings</span
      >
      <span class="text-[0.65rem] font-medium">Admin</span>
    </RouterLink>
  </nav>
</template>
