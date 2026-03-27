<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import AppShell from "../components/AppShell.vue";
import { api } from "../lib/api";
import { useAuth } from "../composables/useAuth";
import { useRouter } from "vue-router";
import type { Room, Reservation, RoomStatus } from "../types/api";

const { refreshClaims, logout } = useAuth();
const router = useRouter();

const rooms = ref<Room[]>([]);
const statusRows = ref<RoomStatus[]>([]);
const reservations = ref<Reservation[]>([]);
const loading = ref(true);
const err = ref("");

const newName = ref("");
const newCapacity = ref(4);
const selectedAmenities = ref<string[]>([]);
const amenityOptions = ["Quadro branco", "Projetor", "Zona silenciosa"];
const promoteUid = ref("");
const busyRoom = ref(false);
const busyPromote = ref(false);
const msg = ref("");
const inventorySearch = ref("");
const deletingId = ref<string | null>(null);

async function loadAll() {
  loading.value = true;
  err.value = "";
  try {
    const [roomsRes, statusRes, resRes] = await Promise.all([
      api.get<Room[]>("/api/rooms"),
      api.get<RoomStatus[]>("/api/rooms/status"),
      api.get<Reservation[]>("/api/reservations"),
    ]);
    rooms.value = roomsRes.data;
    statusRows.value = statusRes.data;
    reservations.value = resRes.data;
  } catch {
    err.value =
      "Falha ao carregar dados admin. Confirme se você tem permissão de administrador.";
  } finally {
    loading.value = false;
  }
}

function toggleAmenity(a: string) {
  const i = selectedAmenities.value.indexOf(a);
  if (i >= 0) selectedAmenities.value.splice(i, 1);
  else selectedAmenities.value.push(a);
}

async function createRoom() {
  busyRoom.value = true;
  msg.value = "";
  try {
    await api.post("/api/rooms", {
      name: newName.value.trim(),
      capacity: Number(newCapacity.value),
      active: true,
      resources: [...selectedAmenities.value],
    });
    newName.value = "";
    msg.value = "Sala publicada com sucesso.";
    await loadAll();
  } catch {
    msg.value = "Erro ao criar sala.";
  } finally {
    busyRoom.value = false;
  }
}

async function deleteRoom(roomId: string, roomName: string) {
  if (
    !confirm(
      `Apagar a sala "${roomName}"? Não é possível se houver reservas futuras ativas.`,
    )
  ) {
    return;
  }
  deletingId.value = roomId;
  msg.value = "";
  try {
    await api.delete(`/api/rooms/${encodeURIComponent(roomId)}`);
    msg.value = "Sala removida.";
    await loadAll();
  } catch {
    msg.value =
      "Não foi possível apagar (verifique reservas futuras ou tente de novo).";
  } finally {
    deletingId.value = null;
  }
}

async function promote() {
  const uid = promoteUid.value.trim();
  if (!uid) return;
  busyPromote.value = true;
  msg.value = "";
  try {
    await api.post(`/api/users/${encodeURIComponent(uid)}/promote`);
    msg.value =
      "Usuário promovido. Ele precisa obter um novo token (sair e entrar).";
    promoteUid.value = "";
    await refreshClaims();
  } catch {
    msg.value = "Falha na promoção (permissão ou UID inválido).";
  } finally {
    busyPromote.value = false;
  }
}

function scrollTo(id: string) {
  document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
}

function statusForRoom(room: Room): {
  label: string;
  dot: "green" | "orange" | "secondary";
} {
  if (room.active === false) {
    return { label: "Manutenção", dot: "secondary" };
  }
  const s = statusRows.value.find((x) => x.roomId === room.id);
  if (s?.occupied) return { label: "Em uso agora", dot: "orange" };
  return { label: "Livre agora", dot: "green" };
}

const filteredRooms = computed(() => {
  const q = inventorySearch.value.trim().toLowerCase();
  if (!q) return rooms.value;
  return rooms.value.filter(
    (r) =>
      r.name.toLowerCase().includes(q) ||
      (r.resources || []).some((x) => x.toLowerCase().includes(q)),
  );
});

const utilizationPct = computed(() => {
  if (!statusRows.value.length) return 0;
  const occ = statusRows.value.filter((s) => s.occupied).length;
  return Math.round((occ / statusRows.value.length) * 100);
});

function reservationBlocksSlot(r: Reservation): boolean {
  const s = (r.status ?? "").trim().toUpperCase();
  if (!s) return true;
  return s !== "CANCELLED" && s !== "COMPLETED";
}

const activeReservationsNow = computed(() => {
  const t = Date.now();
  return reservations.value.filter(
    (r) =>
      reservationBlocksSlot(r) &&
      new Date(r.startTime).getTime() <= t &&
      new Date(r.endTime).getTime() > t,
  ).length;
});

const highDemand = computed(() => utilizationPct.value >= 70);

const reservationsSorted = computed(() =>
  [...reservations.value].sort(
    (a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime(),
  ),
);

const reservationsPreview = computed(() =>
  reservationsSorted.value.slice(0, 8),
);

function formatNextSlotLine(isoStart: string, isoEnd: string | undefined) {
  try {
    const d = {
      weekday: "short",
      day: "2-digit",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    } as const;
    const end = isoEnd ?? isoStart;
    return `${new Date(isoStart).toLocaleString("pt-BR", d)} — ${new Date(end).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}`;
  } catch {
    return isoStart;
  }
}

function formatRange(isoStart: string, isoEnd: string) {
  try {
    const o = { hour: "2-digit", minute: "2-digit" } as const;
    return `${new Date(isoStart).toLocaleTimeString("pt-BR", o)} — ${new Date(isoEnd).toLocaleTimeString("pt-BR", o)}`;
  } catch {
    return `${isoStart} — ${isoEnd}`;
  }
}

function relativeHint(iso: string) {
  const d = new Date(iso).getTime();
  const diff = Date.now() - d;
  const m = Math.floor(diff / 60000);
  if (m < 1) return "agora";
  if (m < 60) return `${m} min atrás`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h} h atrás`;
  const days = Math.floor(h / 24);
  if (days === 1) return "ontem";
  if (days < 7) return `${days} dias atrás`;
  return new Date(iso).toLocaleDateString("pt-BR");
}

function initials(name: string | undefined, fallback: string) {
  const s = (name || fallback).trim();
  const parts = s.split(/\s+/).filter(Boolean);
  if (!parts.length) return "?";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function exportReport() {
  const header = [
    "userId",
    "userName",
    "room",
    "start",
    "end",
    "status",
    "partySize",
  ];
  const lines = reservationsSorted.value.map((r) =>
    [
      r.userId || "",
      r.userName || "",
      r.roomName || r.roomId || "",
      r.startTime,
      r.endTime,
      r.status || "",
      r.partySize != null ? String(r.partySize) : "",
    ]
      .map((cell) => `"${String(cell).replace(/"/g, '""')}"`)
      .join(","),
  );
  const csv = [header.join(","), ...lines].join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `reservas-${new Date().toISOString().slice(0, 10)}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

async function doLogout() {
  await logout();
  await router.push("/login");
}

onMounted(loadAll);
</script>

<template>
  <AppShell>
    <div class="relative min-h-[calc(100vh-4rem)]">
      <!-- Sidebar (desktop) -->
      <aside
        class="hidden lg:flex flex-col fixed left-0 top-16 z-30 w-64 h-[calc(100vh-4rem)] bg-stone-50 border-r border-stone-200 py-6"
        aria-label="Admin"
      >
        <div class="px-6 mb-8">
          <p
            class="text-lg font-black text-orange-700 leading-tight font-headline"
          >
            Study Room
          </p>
          <p
            class="text-[0.65rem] text-stone-500 mt-1 uppercase tracking-widest font-semibold"
          >
            Console admin
          </p>
        </div>
        <nav class="flex-1 space-y-0.5 px-2">
          <button
            type="button"
            class="w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left text-orange-700 bg-orange-100/60 transition-transform duration-200 hover:translate-x-0.5"
            @click="scrollTo('admin-top')"
          >
            <span class="material-symbols-outlined text-[22px]">dashboard</span>
            <span class="font-medium text-sm">Visão geral</span>
          </button>
          <button
            type="button"
            class="w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left text-stone-600 hover:bg-stone-200/50 transition-transform duration-200 hover:translate-x-0.5"
            @click="scrollTo('admin-rooms')"
          >
            <span class="material-symbols-outlined text-[22px]"
              >meeting_room</span
            >
            <span class="font-medium text-sm">Salas</span>
          </button>
          <button
            type="button"
            class="w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left text-stone-600 hover:bg-stone-200/50 transition-transform duration-200 hover:translate-x-0.5"
            @click="scrollTo('admin-metrics')"
          >
            <span class="material-symbols-outlined text-[22px]">analytics</span>
            <span class="font-medium text-sm">Ocupação</span>
          </button>
          <button
            type="button"
            class="w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left text-stone-600 hover:bg-stone-200/50 transition-transform duration-200 hover:translate-x-0.5"
            @click="scrollTo('admin-history')"
          >
            <span class="material-symbols-outlined text-[22px]">history</span>
            <span class="font-medium text-sm">Histórico</span>
          </button>
          <button
            type="button"
            class="w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left text-stone-600 hover:bg-stone-200/50 transition-transform duration-200 hover:translate-x-0.5"
            @click="scrollTo('admin-access')"
          >
            <span class="material-symbols-outlined text-[22px]">group</span>
            <span class="font-medium text-sm">Acesso</span>
          </button>
        </nav>
        <div class="mt-auto px-2 space-y-0.5 border-t border-stone-200/80 pt-4">
          <RouterLink
            to="/rooms"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-stone-600 hover:bg-stone-200/50 text-sm"
          >
            <span class="material-symbols-outlined text-[20px]"
              >arrow_back</span
            >
            <span class="font-medium">Área do aluno</span>
          </RouterLink>
          <button
            type="button"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-stone-600 hover:bg-stone-200/50 text-sm"
            @click="doLogout"
          >
            <span class="material-symbols-outlined text-[20px]">logout</span>
            <span class="font-medium">Sair</span>
          </button>
        </div>
      </aside>

      <!-- Main -->
      <main
        id="admin-top"
        class="lg:ml-64 px-5 sm:px-8 pt-8 pb-24 lg:pb-14 max-w-[1600px] mx-auto"
      >
        <header class="mb-10 lg:mb-12 scroll-mt-24">
          <span
            class="text-primary font-semibold tracking-wider text-xs uppercase mb-2 block"
            >Visão administrativa</span
          >
          <h2
            class="font-headline text-3xl sm:text-4xl font-extrabold text-on-surface tracking-tight"
          >
            Infraestrutura do sistema
          </h2>
          <p
            class="text-on-surface-variant max-w-2xl mt-3 text-base sm:text-lg leading-relaxed"
          >
            Gerencie espaços acadêmicos, acompanhe ocupação em tempo quase real
            e controle permissões no ecossistema Curator.
          </p>
        </header>

        <div
          v-if="loading"
          class="rounded-xl border border-outline-variant/15 bg-surface-container-low/80 p-16 text-center text-on-surface-variant"
        >
          <span
            class="inline-block w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin mb-4"
          />
          <p class="text-sm font-medium">Carregando painel…</p>
        </div>

        <template v-else>
          <p v-if="err" class="text-red-700 text-sm mb-4">{{ err }}</p>
          <p
            v-if="msg"
            class="text-emerald-800 text-sm mb-6 bg-emerald-50 border border-emerald-100 rounded-lg px-4 py-3"
          >
            {{ msg }}
          </p>

          <!-- Bento: create + inventory -->
          <div
            id="admin-rooms"
            class="grid grid-cols-1 lg:grid-cols-12 gap-8 mb-14 lg:mb-16 scroll-mt-24"
          >
            <div
              class="lg:col-span-4 bg-surface-container-low p-6 sm:p-8 rounded-xl border border-outline-variant/10 shadow-sm"
            >
              <h3
                class="font-headline text-xl font-bold mb-6 flex items-center gap-2"
              >
                <span class="material-symbols-outlined text-primary"
                  >add_circle</span
                >
                Nova sala
              </h3>
              <form class="space-y-5" @submit.prevent="createRoom">
                <div>
                  <label
                    class="block text-[0.65rem] font-bold text-on-surface-variant uppercase tracking-tight mb-1.5"
                    >Nome da sala</label
                  >
                  <input
                    v-model="newName"
                    class="w-full bg-surface-container-highest border-none rounded-lg px-4 py-3 text-on-surface focus:ring-0 border-b-2 border-transparent focus:border-b-primary transition-all outline-none"
                    placeholder="Ex.: Pod de estudo 402"
                    required
                  />
                </div>
                <div>
                  <label
                    class="block text-[0.65rem] font-bold text-on-surface-variant uppercase tracking-tight mb-1.5"
                    >Capacidade</label
                  >
                  <input
                    v-model.number="newCapacity"
                    type="number"
                    min="1"
                    class="w-full bg-surface-container-highest border-none rounded-lg px-4 py-3 text-on-surface border-b-2 border-transparent focus:border-b-primary transition-all outline-none"
                  />
                </div>
                <div>
                  <label
                    class="block text-[0.65rem] font-bold text-on-surface-variant uppercase tracking-tight mb-2"
                    >Recursos</label
                  >
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-for="a in amenityOptions"
                      :key="a"
                      type="button"
                      class="bg-surface-container-high px-3 py-1.5 rounded-full text-xs font-medium cursor-pointer border transition-all"
                      :class="
                        selectedAmenities.includes(a)
                          ? 'border-primary bg-primary/10 text-primary'
                          : 'border-transparent hover:border-primary/40'
                      "
                      @click="toggleAmenity(a)"
                    >
                      {{ a }}
                    </button>
                  </div>
                </div>
                <button
                  type="submit"
                  class="w-full primary-gradient text-white py-3.5 rounded-lg cursor-pointer font-bold text-sm shadow-lg shadow-primary/15 hover:opacity-95 active:scale-[0.98] transition-all disabled:opacity-50"
                  :disabled="busyRoom"
                >
                  Criar Sala
                </button>
              </form>
            </div>

            <div
              class="lg:col-span-8 bg-surface-container-lowest border border-outline-variant/10 rounded-xl overflow-hidden shadow-sm"
            >
              <div
                class="px-6 sm:px-8 py-5 flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4 bg-surface-container-low/40 border-b border-outline-variant/10"
              >
                <h3 class="font-headline text-xl font-bold">
                  Inventário ativo
                </h3>
                <div class="flex flex-wrap items-center gap-2">
                  <span
                    class="bg-tertiary-fixed text-on-tertiary-fixed-variant px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider"
                    >Total: {{ rooms.length }} salas</span
                  >
                  <div class="relative flex-1 min-w-[180px] max-w-xs">
                    <span
                      class="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-stone-400 text-lg pointer-events-none"
                      >search</span
                    >
                    <input
                      v-model="inventorySearch"
                      type="search"
                      placeholder="Buscar salas ou recursos…"
                      class="w-full bg-surface-container-highest border-none rounded-lg pl-9 pr-3 py-2 text-sm text-on-surface focus:ring-2 focus:ring-primary/20 outline-none"
                    />
                  </div>
                </div>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full text-left min-w-[640px]">
                  <thead>
                    <tr
                      class="text-[10px] font-black uppercase text-on-surface-variant tracking-widest border-b border-outline-variant/10"
                    >
                      <th class="px-6 sm:px-8 py-4">Recurso</th>
                      <th class="px-6 sm:px-8 py-4">Atributos</th>
                      <th class="px-6 sm:px-8 py-4">Status</th>
                      <th class="px-6 sm:px-8 py-4 text-right w-28">Ações</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-outline-variant/5">
                    <tr
                      v-for="r in filteredRooms"
                      :key="r.id"
                      class="hover:bg-surface/60 transition-colors group"
                    >
                      <td class="px-6 sm:px-8 py-5">
                        <div class="font-bold text-on-surface">
                          {{ r.name }}
                        </div>
                        <div class="text-xs text-on-surface-variant mt-0.5">
                          ID {{ r.id?.slice(0, 8) }}…
                        </div>
                      </td>
                      <td class="px-6 sm:px-8 py-5">
                        <div class="flex flex-wrap gap-1.5">
                          <span
                            class="bg-surface-container text-on-surface-variant px-2 py-0.5 rounded text-[10px] font-medium"
                            >{{ r.capacity }} lugares</span
                          >
                          <span
                            v-for="tag in (r.resources || []).slice(0, 3)"
                            :key="tag"
                            class="bg-surface-container text-on-surface-variant px-2 py-0.5 rounded text-[10px] font-medium"
                            >{{ tag }}</span
                          >
                        </div>
                      </td>
                      <td class="px-6 sm:px-8 py-5">
                        <span
                          class="flex items-center gap-1.5 text-xs font-semibold w-fit"
                          :class="{
                            'text-emerald-700':
                              statusForRoom(r).dot === 'green',
                            'text-orange-700':
                              statusForRoom(r).dot === 'orange',
                            'text-secondary':
                              statusForRoom(r).dot === 'secondary',
                          }"
                        >
                          <span
                            class="w-1.5 h-1.5 rounded-full shrink-0"
                            :class="{
                              'bg-emerald-500':
                                statusForRoom(r).dot === 'green',
                              'bg-orange-500':
                                statusForRoom(r).dot === 'orange',
                              'bg-secondary':
                                statusForRoom(r).dot === 'secondary',
                            }"
                          />
                          {{ statusForRoom(r).label }}
                        </span>
                      </td>
                      <td class="px-6 sm:px-8 py-5 text-right">
                        <button
                          type="button"
                          class="inline-flex items-center justify-center p-2 rounded-lg text-red-700 hover:bg-red-50 transition-colors disabled:opacity-40"
                          title="Apagar sala"
                          :disabled="deletingId === r.id"
                          @click="deleteRoom(r.id, r.name)"
                        >
                          <span class="material-symbols-outlined text-lg"
                            >delete</span
                          >
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
                <p
                  v-if="!filteredRooms.length"
                  class="px-8 py-10 text-center text-sm text-on-surface-variant"
                >
                  Nenhuma sala corresponde à busca.
                </p>
              </div>
            </div>
          </div>

          <!-- Metrics -->
          <section id="admin-metrics" class="mb-14 lg:mb-16 scroll-mt-24">
            <div
              class="flex flex-col sm:flex-row sm:justify-between sm:items-end gap-4 mb-8"
            >
              <div>
                <h3 class="font-headline text-2xl font-bold tracking-tight">
                  Ocupação ao vivo
                </h3>
                <p class="text-on-surface-variant text-sm mt-1">
                  Reflete o momento atual: sala ocupada só enquanto há reserva
                  ativa (já começou e o término ainda não passou).
                  Canceladas/concluídas não contam.
                </p>
              </div>
              <button
                type="button"
                class="text-primary font-bold text-sm flex items-center gap-1 hover:underline self-start sm:self-auto"
                @click="exportReport"
              >
                Exportar relatório
                <span class="material-symbols-outlined text-lg">download</span>
              </button>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div
                class="bg-surface-container p-6 rounded-xl border border-outline-variant/20"
              >
                <div class="flex justify-between mb-4">
                  <span
                    class="material-symbols-outlined text-secondary bg-secondary-container/35 p-2 rounded-lg"
                    >sensor_door</span
                  >
                  <span
                    class="text-on-surface-variant text-[10px] font-bold uppercase"
                    >Ao vivo</span
                  >
                </div>
                <div class="text-3xl font-black font-headline text-on-surface">
                  {{ utilizationPct }}%
                </div>
                <div
                  class="text-[0.65rem] font-bold text-on-surface-variant uppercase tracking-widest mt-1"
                >
                  Utilização média
                </div>
              </div>
              <div
                class="bg-surface-container p-6 rounded-xl border border-outline-variant/20"
              >
                <div class="flex justify-between mb-4">
                  <span
                    class="material-symbols-outlined text-primary bg-primary/15 p-2 rounded-lg"
                    >event_busy</span
                  >
                  <span class="text-on-surface-variant text-xs font-bold"
                    >Agora</span
                  >
                </div>
                <div class="text-3xl font-black font-headline text-on-surface">
                  {{ activeReservationsNow }}
                </div>
                <div
                  class="text-[0.65rem] font-bold text-on-surface-variant uppercase tracking-widest mt-1"
                >
                  Reservas ativas
                </div>
              </div>
              <div
                class="md:col-span-2 bg-surface-container-high p-6 rounded-xl border border-outline-variant/20 relative overflow-hidden"
              >
                <div
                  class="relative z-10 flex flex-col justify-between gap-4 min-h-[140px]"
                >
                  <div>
                    <h4 class="font-bold text-lg mb-1 font-headline">
                      {{
                        highDemand
                          ? "Pico de demanda"
                          : "Operação dentro do esperado"
                      }}
                    </h4>
                    <p
                      class="text-xs text-on-surface-variant max-w-md leading-relaxed"
                    >
                      {{
                        highDemand
                          ? "Muitas salas ocupadas neste momento. Considere abrir novos horários ou salas."
                          : "Taxa de ocupação moderada. Métricas atualizadas ao recarregar o painel."
                      }}
                    </p>
                  </div>
                  <div class="flex gap-8 flex-wrap">
                    <div class="flex flex-col gap-0.5">
                      <span class="text-[10px] font-bold opacity-60 uppercase"
                        >Status</span
                      >
                      <span class="font-bold text-orange-800">{{
                        highDemand ? "Alta demanda" : "Normal"
                      }}</span>
                    </div>
                    <div class="flex flex-col gap-0.5">
                      <span class="text-[10px] font-bold opacity-60 uppercase"
                        >Salas</span
                      >
                      <span class="font-bold text-on-surface"
                        >{{ statusRows.filter((s) => s.occupied).length }} /
                        {{ statusRows.length }} ocupadas agora</span
                      >
                    </div>
                  </div>
                </div>
                <span
                  class="material-symbols-outlined absolute -right-4 -bottom-4 text-[7rem] opacity-[0.06] rotate-12 pointer-events-none text-orange-900"
                  >warning</span
                >
              </div>
            </div>

            <!-- Live room cards -->
            <div class="mt-8">
              <h4
                class="text-sm font-bold text-on-surface-variant uppercase tracking-wider mb-4"
              >
                Por sala
              </h4>
              <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
                <div
                  v-for="s in statusRows"
                  :key="s.roomId"
                  class="rounded-xl border border-outline-variant/15 p-4 bg-surface-container-low/80"
                >
                  <p class="font-bold text-on-surface">{{ s.roomName }}</p>
                  <p
                    class="text-xs mt-2 font-semibold"
                    :class="s.occupied ? 'text-orange-700' : 'text-emerald-700'"
                  >
                    {{
                      s.occupied
                        ? `Em uso agora${s.until ? ` — libera às ${new Date(s.until).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}` : ""}`
                        : s.nextReservationStart
                          ? `Livre agora · próxima: ${formatNextSlotLine(s.nextReservationStart, s.nextReservationEnd)}`
                          : "Livre agora (sem próxima reserva agendada)"
                    }}
                  </p>
                  <p
                    v-if="s.occupied && s.currentOccupant"
                    class="text-xs text-on-surface-variant mt-1 truncate"
                  >
                    {{ s.currentOccupant }}
                  </p>
                </div>
              </div>
            </div>
          </section>

          <!-- History + Access -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 pb-8">
            <div
              id="admin-history"
              class="lg:col-span-2 space-y-6 scroll-mt-24"
            >
              <h3 class="font-headline text-2xl font-bold tracking-tight">
                Reservas globais
              </h3>
              <div
                class="bg-surface-container-lowest rounded-xl overflow-hidden shadow-sm border border-outline-variant/10"
              >
                <div class="divide-y divide-outline-variant/5">
                  <div
                    v-for="r in reservationsPreview"
                    :key="r.id"
                    class="py-4 px-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 group hover:bg-surface-container-low/50 transition-colors"
                  >
                    <div class="flex items-center gap-4 min-w-0">
                      <div
                        class="w-10 h-10 rounded-full bg-stone-200 flex items-center justify-center text-xs font-bold text-stone-700 shrink-0"
                      >
                        {{ initials(r.userName, r.userId || "?") }}
                      </div>
                      <div class="min-w-0">
                        <div class="text-sm font-bold text-on-surface truncate">
                          {{ r.userName || r.userId || "Usuário" }}
                        </div>
                        <div
                          class="text-[10px] text-on-surface-variant flex flex-wrap gap-x-2 gap-y-0.5"
                        >
                          <span>{{ relativeHint(r.startTime) }}</span>
                          <span>•</span>
                          <span>{{ r.roomName || r.roomId }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="flex items-center gap-4 sm:justify-end">
                      <div class="text-right">
                        <div
                          class="text-[10px] font-bold uppercase tracking-tighter text-on-surface-variant"
                        >
                          Intervalo
                        </div>
                        <div class="text-xs font-semibold tabular-nums">
                          {{ formatRange(r.startTime, r.endTime) }}
                        </div>
                        <div class="text-[10px] text-on-surface-variant mt-0.5">
                          {{ r.status
                          }}<template v-if="r.partySize != null">
                            · {{ r.partySize }} pessoa(s)</template
                          >
                        </div>
                      </div>
                    </div>
                  </div>
                  <p
                    v-if="!reservations.length"
                    class="px-6 py-10 text-center text-sm text-on-surface-variant"
                  >
                    Nenhuma reserva registrada.
                  </p>
                </div>
                <button
                  v-if="reservations.length > 8"
                  type="button"
                  class="w-full py-4 text-xs font-bold text-primary bg-surface-container-low/60 hover:bg-surface-container-low transition-colors"
                  @click="exportReport"
                >
                  Exportar histórico completo (CSV)
                </button>
              </div>
            </div>

            <div id="admin-access" class="space-y-6 scroll-mt-24">
              <h3 class="font-headline text-2xl font-bold tracking-tight">
                Controle de acesso
              </h3>
              <div
                class="bg-surface-container-high p-8 rounded-xl border border-primary/10"
              >
                <div class="flex items-center gap-3 mb-4">
                  <span class="material-symbols-outlined text-primary text-2xl"
                    >verified_user</span
                  >
                  <h4 class="font-bold text-on-surface font-headline">
                    Elevar permissões
                  </h4>
                </div>
                <p class="text-xs text-on-surface-variant leading-relaxed mb-6">
                  Promova contas ao papel de administrador. O usuário passa a
                  gerenciar salas e ver reservas globais após obter um novo
                  token (sair e entrar).
                </p>
                <form class="space-y-4" @submit.prevent="promote">
                  <div>
                    <label
                      class="block text-[10px] font-black text-on-surface-variant uppercase tracking-widest mb-2"
                      >UID (Firebase)</label
                    >
                    <div class="relative">
                      <input
                        v-model="promoteUid"
                        class="w-full bg-surface-container-lowest border-none rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-primary/25 outline-none pr-10"
                        placeholder="cole o UID do usuário"
                        type="text"
                      />
                      <span
                        class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-stone-300 text-lg pointer-events-none"
                        >alternate_email</span
                      >
                    </div>
                  </div>
                  <div
                    class="bg-primary/5 p-4 rounded-lg flex gap-3 items-start border border-primary/10"
                  >
                    <span
                      class="material-symbols-outlined text-primary text-sm mt-0.5 shrink-0"
                      >info</span
                    >
                    <p
                      class="text-[10px] text-primary/90 font-medium leading-relaxed"
                    >
                      Em produção, restrinja promoções a contas verificadas e
                      audite alterações no Firebase Console.
                    </p>
                  </div>
                  <button
                    type="submit"
                    class="w-full border-2 border-primary text-primary py-3 rounded-lg font-bold text-xs uppercase tracking-widest hover:bg-primary hover:text-white transition-all active:scale-[0.98] disabled:opacity-50"
                    :disabled="busyPromote"
                  >
                    Confirmar promoção
                  </button>
                </form>
              </div>

              <div
                class="bg-surface-container-low p-6 rounded-xl border border-outline-variant/10"
              >
                <h4
                  class="text-xs font-black text-on-surface-variant uppercase tracking-widest mb-4"
                >
                  Eventos recentes
                </h4>
                <ul class="space-y-4">
                  <li class="flex gap-3">
                    <span
                      class="w-1.5 h-1.5 rounded-full bg-orange-500 mt-1.5 shrink-0"
                    />
                    <div>
                      <p class="text-[11px] font-bold text-on-surface">
                        Sincronização do painel
                      </p>
                      <p class="text-[10px] text-on-surface-variant">
                        Dados atualizados ao carregar esta página
                      </p>
                    </div>
                  </li>
                  <li class="flex gap-3">
                    <span
                      class="w-1.5 h-1.5 rounded-full bg-stone-300 mt-1.5 shrink-0"
                    />
                    <div>
                      <p class="text-[11px] font-bold text-on-surface">
                        Promoções via API
                      </p>
                      <p class="text-[10px] text-on-surface-variant">
                        Use UID válido do Authentication
                      </p>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </template>
      </main>

      <!-- Mobile FAB -->
      <button
        type="button"
        class="lg:hidden fixed bottom-24 right-5 z-40 w-14 h-14 primary-gradient text-white rounded-full shadow-2xl flex items-center justify-center active:scale-90 transition-transform"
        aria-label="Nova sala"
        @click="scrollTo('admin-rooms')"
      >
        <span class="material-symbols-outlined text-2xl">add</span>
      </button>
    </div>
  </AppShell>
</template>
