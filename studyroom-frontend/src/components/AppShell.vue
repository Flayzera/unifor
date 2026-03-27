<script setup lang="ts">
import { useAuth } from "../composables/useAuth";
import { useRoute, useRouter } from "vue-router";

const { isAdmin, displayName, logout } = useAuth();
const router = useRouter();
const current = useRoute();

function navClass(path: string) {
  const active = current.path === path || current.path.startsWith(path + "/");
  return active
    ? "text-orange-700 font-semibold border-b-2 border-orange-600 pb-1"
    : "text-stone-500 hover:text-orange-600 transition-colors";
}
</script>

<template>
  <nav
    class="fixed top-0 w-full z-50 glass-nav border-b border-orange-100/20 shadow-sm"
  >
    <div
      class="flex justify-between items-center px-6 sm:px-8 h-16 w-full mx-auto max-w-7xl"
    >
      <div class="flex items-center gap-8">
        <RouterLink
          to="/rooms"
          class="text-xl font-bold tracking-tight text-stone-900 font-headline"
        >
          Study Room
        </RouterLink>
        <div class="hidden md:flex items-center gap-6 text-sm">
          <RouterLink :class="navClass('/rooms')" to="/rooms">Salas</RouterLink>
          <RouterLink :class="navClass('/bookings')" to="/bookings"
            >Minhas reservas</RouterLink
          >
          <RouterLink v-if="isAdmin" :class="navClass('/admin')" to="/admin"
            >Admin</RouterLink
          >
        </div>
      </div>
      <div class="flex items-center gap-3 text-sm text-stone-600">
        <span class="hidden sm:inline max-w-40 truncate">{{
          displayName
        }}</span>
        <button
          type="button"
          class="rounded-lg border border-stone-200 px-3 py-1.5 text-stone-600 hover:bg-orange-50"
          @click="logout().then(() => router.push('/login'))"
        >
          Sair
        </button>
      </div>
    </div>
  </nav>
  <div class="pt-16 min-h-screen pb-20 md:pb-0">
    <slot />
  </div>
  <nav
    class="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-md border-t border-stone-200 flex justify-around items-center py-2.5"
    aria-label="Primary"
  >
    <RouterLink
      to="/rooms"
      class="flex flex-col items-center gap-0.5 min-w-14"
      :class="
        current.path === '/rooms' || current.path === '/'
          ? 'text-orange-700'
          : 'text-stone-500'
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
      class="flex flex-col items-center gap-0.5 min-w-14"
      :class="
        current.path.startsWith('/bookings')
          ? 'text-orange-700'
          : 'text-stone-500'
      "
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
      v-if="isAdmin"
      to="/admin"
      class="flex flex-col items-center gap-0.5 min-w-14"
      :class="
        current.path.startsWith('/admin') ? 'text-orange-700' : 'text-stone-500'
      "
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
