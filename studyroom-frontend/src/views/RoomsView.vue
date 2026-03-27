<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import AppShell from "../components/AppShell.vue";
import { api } from "../lib/api";
import { auth } from "../lib/firebase";
import type { Reservation, Room } from "../types/api";

const router = useRouter();
const route = useRoute();

const reservations = ref<Reservation[]>([]);
const loading = ref(true);
const loadingReservations = ref(false);
const err = ref("");

const bookingRoom = ref<Room | null>(null);
const startLocal = ref("");
const endLocal = ref("");
const partySize = ref(1);
const bookingBusy = ref(false);
const bookingErr = ref("");

/** Todas as salas; disponibilidade no intervalo atual vem de {@link availableIds}. */
const allRooms = ref<Room[]>([]);
const availableIds = ref<Set<string>>(new Set());

/** yyyy-mm-dd */
const reservationDate = ref("");
const startTime = ref("09:00");
type DurationKey = "1" | "2" | "3" | "8";
const durationKey = ref<DurationKey>("2");

const DURATION_OPTIONS: { key: DurationKey; label: string; hours: number }[] = [
  { key: "1", label: "1 hora", hours: 1 },
  { key: "2", label: "2 horas", hours: 2 },
  { key: "3", label: "3 horas", hours: 3 },
  { key: "8", label: "Dia inteiro (8 h)", hours: 8 },
];

function pad(n: number) {
  return String(n).padStart(2, "0");
}

const timeSlotOptions = computed(() => {
  const slots: { value: string; label: string }[] = [];
  for (let h = 8; h <= 21; h++) {
    for (const m of [0, 30]) {
      if (h === 21 && m === 30) break;
      const value = `${pad(h)}:${pad(m)}`;
      const d = new Date();
      d.setHours(h, m, 0, 0);
      const label = d.toLocaleTimeString("pt-BR", {
        hour: "2-digit",
        minute: "2-digit",
      });
      slots.push({ value, label });
    }
  }
  return slots;
});

function todayStr() {
  const n = new Date();
  return `${n.getFullYear()}-${pad(n.getMonth() + 1)}-${pad(n.getDate())}`;
}

function nextSlotFromNow(): string {
  const now = new Date();
  let total = now.getHours() * 60 + now.getMinutes();
  total = Math.ceil(total / 30) * 30;
  let h = Math.floor(total / 60);
  let m = total % 60;
  if (h > 21 || (h === 21 && m > 0)) return "09:00";
  return `${pad(h)}:${pad(m)}`;
}

function toBackendIso(d: Date): string {
  return d.toISOString().replace(/\.\d{3}Z$/, ".000Z");
}

/** Build local Date from date (yyyy-mm-dd) + HH:mm */
function combineDateAndTime(dateStr: string, timeStr: string): Date {
  const [y, mo, da] = dateStr.split("-").map(Number);
  const [hh, mm] = timeStr.split(":").map(Number);
  return new Date(y, mo - 1, da, hh, mm, 0, 0);
}

const interval = computed(() => {
  const hours =
    DURATION_OPTIONS.find((d) => d.key === durationKey.value)?.hours ?? 2;
  const start = combineDateAndTime(reservationDate.value, startTime.value);
  const end = new Date(start.getTime() + hours * 60 * 60 * 1000);
  return { start, end, hours };
});

function dateToDatetimeLocal(d: Date): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function roomImageUrl(room: Room): string {
  const seed = encodeURIComponent(room.id || room.name || "room");
  return `https://picsum.photos/seed/${seed}/800/480`;
}

const visibleRooms = computed(() =>
  allRooms.value.filter((r) => r.active !== false),
);

const freeInSlotCount = computed(
  () => visibleRooms.value.filter((r) => availableIds.value.has(r.id)).length,
);

function slotFree(room: Room): boolean {
  return availableIds.value.has(room.id);
}

const modalPartyOptions = computed(() => {
  const cap = bookingRoom.value ? Math.max(1, bookingRoom.value.capacity) : 1;
  return Array.from({ length: cap }, (_, i) => i + 1);
});

const upcomingReservations = computed(() => {
  const now = Date.now();
  return [...reservations.value]
    .filter((r) => new Date(r.endTime).getTime() > now)
    .sort(
      (a, b) =>
        new Date(a.startTime).getTime() - new Date(b.startTime).getTime(),
    )
    .slice(0, 4);
});

async function refreshRoomsBrowse() {
  loading.value = true;
  err.value = "";
  try {
    const { start, end } = interval.value;
    if (start >= end) {
      err.value = "O horário de fim deve ser depois do início.";
      allRooms.value = [];
      availableIds.value = new Set();
      return;
    }
    const params = {
      start: toBackendIso(start),
      end: toBackendIso(end),
    };
    const [allRes, availRes] = await Promise.all([
      api.get<Room[]>("/api/rooms"),
      api.get<Room[]>("/api/rooms/available", { params }),
    ]);
    allRooms.value = allRes.data;
    availableIds.value = new Set(availRes.data.map((r) => r.id));
  } catch {
    err.value =
      "Não foi possível carregar as salas. Verifique o login e a API.";
    allRooms.value = [];
    availableIds.value = new Set();
  } finally {
    loading.value = false;
  }
}

async function loadReservations() {
  const uid = auth.currentUser?.uid;
  if (!uid) return;
  loadingReservations.value = true;
  try {
    const { data } = await api.get<Reservation[]>(
      `/api/reservations/user/${uid}`,
    );
    reservations.value = data;
  } catch {
    reservations.value = [];
  } finally {
    loadingReservations.value = false;
  }
}

function openBook(r: Room) {
  if (!slotFree(r)) return;
  bookingRoom.value = r;
  bookingErr.value = "";
  const { start, end } = interval.value;
  startLocal.value = dateToDatetimeLocal(start);
  endLocal.value = dateToDatetimeLocal(end);
  partySize.value = Math.min(
    Math.max(1, partySize.value),
    Math.max(1, r.capacity),
  );
}

async function confirmBook() {
  if (!bookingRoom.value || !auth.currentUser) return;
  bookingBusy.value = true;
  bookingErr.value = "";
  try {
    const ps = Math.min(
      Math.max(1, partySize.value),
      Math.max(1, bookingRoom.value.capacity),
    );
    const body = {
      roomId: bookingRoom.value.id,
      startTime: toBackendIso(new Date(startLocal.value)),
      endTime: toBackendIso(new Date(endLocal.value)),
      partySize: ps,
    };
    const { data } = await api.post<string>("/api/reservations", body);
    const idMatch = String(data).match(/ID:\s*(\S+)/i);
    const id = idMatch?.[1] || "";
    bookingRoom.value = null;
    await loadReservations();
    await router.push({
      name: "booking-confirm",
      query: {
        room: body.roomId,
        start: body.startTime,
        end: body.endTime,
        id,
        partySize: String(ps),
      },
    });
  } catch (e: unknown) {
    bookingErr.value =
      e && typeof e === "object" && "response" in e
        ? JSON.stringify(
            (e as { response?: { data?: unknown } }).response?.data,
          )
        : "Falha ao reservar";
  } finally {
    bookingBusy.value = false;
  }
}

onMounted(async () => {
  reservationDate.value = todayStr();
  startTime.value =
    reservationDate.value === todayStr() ? nextSlotFromNow() : "09:00";
  await Promise.all([refreshRoomsBrowse(), loadReservations()]);
});

watch(reservationDate, (d) => {
  if (d === todayStr()) {
    startTime.value = nextSlotFromNow();
  }
});

/** Volta da confirmação: atualiza lista e disponibilidade no intervalo. */
watch(
  () => route.name,
  (name, oldName) => {
    if (name === "rooms" && oldName === "booking-confirm") {
      void refreshRoomsBrowse();
      void loadReservations();
    }
  },
);
</script>

<template>
  <AppShell>
    <main class="pt-8 pb-16 md:pb-20 max-w-7xl mx-auto px-6 sm:px-8">
      <div class="grid grid-cols-1 gap-10 lg:grid-cols-12 lg:gap-8">
        <!-- Sidebar -->
        <aside class="hidden lg:block lg:col-span-2">
          <div class="sticky top-24 space-y-8">
            <div>
              <p
                class="text-[0.75rem] font-semibold uppercase tracking-widest text-on-surface-variant mb-4"
              >
                Status
              </p>
              <div
                class="flex items-center gap-2 text-primary font-medium text-sm"
              >
                <span
                  class="w-2 h-2 rounded-full bg-primary shrink-0 animate-pulse"
                />
                Consulta por intervalo
              </div>
            </div>
          </div>
        </aside>

        <div class="lg:col-span-10 space-y-12">
          <!-- Find your space -->
          <section
            id="search"
            class="bg-surface-container-low p-6 sm:p-8 rounded-xl border border-outline-variant/10"
          >
            <h2 class="font-headline text-2xl font-bold text-on-surface mb-2">
              Encontre seu espaço
            </h2>
            <p class="text-sm text-on-surface-variant mb-6 max-w-2xl">
              A lista abaixo responde só ao <strong>intervalo que você
                escolher</strong> (não é “agora” no relógio). Se estiver livre
              aqui mas ocupada às 18h, ela aparece livre até você buscar um
              horário que corte essa reserva.
            </p>

            <form
              class="grid grid-cols-1 md:grid-cols-3 gap-6"
              @submit.prevent="refreshRoomsBrowse"
            >
              <div class="space-y-2">
                <label
                  class="text-[0.75rem] font-semibold text-on-surface-variant px-1"
                  >Data</label
                >
                <input
                  v-model="reservationDate"
                  type="date"
                  class="w-full bg-surface-container-highest border border-transparent rounded-lg p-3 text-on-surface focus:ring-2 focus:ring-primary/25 outline-none transition-all"
                  required
                />
              </div>
              <div class="space-y-2">
                <label
                  class="text-[0.75rem] font-semibold text-on-surface-variant px-1"
                  >Início</label
                >
                <div class="relative">
                  <select
                    v-model="startTime"
                    class="w-full bg-surface-container-highest border border-transparent rounded-lg p-3 text-on-surface focus:ring-2 focus:ring-primary/25 outline-none appearance-none cursor-pointer pr-10"
                  >
                    <option
                      v-for="opt in timeSlotOptions"
                      :key="opt.value"
                      :value="opt.value"
                    >
                      {{ opt.label }}
                    </option>
                  </select>
                  <span
                    class="material-symbols-outlined pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-sm"
                    >expand_more</span
                  >
                </div>
              </div>
              <div class="space-y-2">
                <label
                  class="text-[0.75rem] font-semibold text-on-surface-variant px-1"
                  >Duração</label
                >
                <div class="relative">
                  <select
                    v-model="durationKey"
                    class="w-full bg-surface-container-highest border border-transparent rounded-lg p-3 text-on-surface focus:ring-2 focus:ring-primary/25 outline-none appearance-none cursor-pointer pr-10"
                  >
                    <option
                      v-for="opt in DURATION_OPTIONS"
                      :key="opt.key"
                      :value="opt.key"
                    >
                      {{ opt.label }}
                    </option>
                  </select>
                  <span
                    class="material-symbols-outlined pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-sm"
                    >expand_more</span
                  >
                </div>
              </div>
              <div class="md:col-span-3 flex justify-end">
                <button
                  type="submit"
                  class="primary-gradient text-white px-8 py-3 rounded-lg font-semibold flex items-center gap-2 hover:opacity-95 transition-all active:scale-[0.98] shadow-lg shadow-primary/15 disabled:opacity-50"
                  :disabled="loading"
                >
                  <span class="material-symbols-outlined text-[20px]"
                    >search</span
                  >
                  Atualizar disponibilidade
                </button>
              </div>
            </form>
          </section>

          <!-- Salas (todas + status no intervalo) -->
          <section id="available-rooms">
            <div
              class="flex flex-wrap items-baseline justify-between gap-4 mb-8"
            >
              <h2 class="font-headline text-3xl font-bold text-on-surface">
                Salas
              </h2>
              <span class="text-on-surface-variant text-sm">
                <template v-if="!loading">
                  {{ visibleRooms.length }} cadastradas ·
                  {{ freeInSlotCount }} livres no intervalo acima
                </template>
                <template v-else>…</template>
              </span>
            </div>

            <p v-if="err" class="text-red-700 text-sm mb-4">{{ err }}</p>
            <p v-if="loading" class="text-on-surface-variant py-12 text-center">
              Carregando salas…
            </p>

            <div
              v-else-if="!visibleRooms.length"
              class="rounded-xl border border-dashed border-outline-variant/40 bg-surface-container-low/50 p-10 text-center text-on-surface-variant"
            >
              Nenhuma sala cadastrada ainda.
            </div>

            <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-8">
              <article
                v-for="r in visibleRooms"
                :key="r.id"
                class="group bg-surface-container-lowest rounded-xl overflow-hidden transition-all duration-300 ring-1 ring-outline-variant/15 hover:shadow-2xl hover:shadow-stone-900/5"
                :class="
                  slotFree(r)
                    ? 'hover:ring-primary/20'
                    : 'opacity-95 ring-stone-200/60'
                "
              >
                <div
                  class="relative h-48 w-full overflow-hidden bg-surface-container-high"
                >
                  <img
                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                    :src="roomImageUrl(r)"
                    :alt="r.name"
                    loading="lazy"
                  />
                  <div
                    class="absolute top-4 right-4 bg-white/90 backdrop-blur px-3 py-1 rounded-full text-[0.7rem] font-bold flex items-center gap-1"
                    :class="
                      slotFree(r) ? 'text-emerald-800' : 'text-orange-800'
                    "
                  >
                    <span
                      class="w-1.5 h-1.5 rounded-full shrink-0"
                      :class="slotFree(r) ? 'bg-emerald-600' : 'bg-orange-600'"
                    />
                    {{
                      slotFree(r)
                        ? "Livre neste intervalo"
                        : "Ocupada neste intervalo"
                    }}
                  </div>
                </div>
                <div class="p-6">
                  <div class="flex justify-between items-start gap-3 mb-4">
                    <div>
                      <h3
                        class="font-headline text-xl font-bold text-on-surface"
                      >
                        {{ r.name }}
                      </h3>
                      <p class="text-on-surface-variant text-sm mt-0.5">
                        Até {{ r.capacity }} pessoas
                      </p>
                    </div>
                    <div
                      class="bg-tertiary-fixed text-on-tertiary-fixed-variant px-3 py-1 rounded text-[0.75rem] font-bold shrink-0"
                    >
                      {{ r.capacity }} LUGARES
                    </div>
                  </div>
                  <div
                    v-if="r.resources?.length"
                    class="flex flex-wrap gap-2 mb-6"
                  >
                    <span
                      v-for="tag in r.resources"
                      :key="tag"
                      class="bg-surface-container px-3 py-1 rounded-full text-[0.7rem] font-medium text-on-surface-variant"
                      >{{ tag }}</span
                    >
                  </div>
                  <button
                    type="button"
                    class="w-full font-bold py-3 rounded-lg transition-all active:scale-[0.98]"
                    :class="
                      slotFree(r)
                        ? 'border-2 border-primary text-primary hover:bg-primary hover:text-white'
                        : 'border-2 border-stone-200 text-stone-400 cursor-not-allowed bg-stone-50'
                    "
                    :disabled="!slotFree(r)"
                    @click="openBook(r)"
                  >
                    {{
                      slotFree(r)
                        ? "Reservar agora"
                        : "Indisponível neste intervalo"
                    }}
                  </button>
                </div>
              </article>
            </div>
          </section>

          <!-- Current reservations preview -->
          <section
            id="my-reservations"
            class="pt-8 border-t border-outline-variant/20"
          >
            <div class="flex flex-wrap items-center justify-between gap-4 mb-8">
              <h2 class="font-headline text-2xl font-bold text-on-surface">
                Próximas reservas
              </h2>
              <RouterLink
                to="/bookings"
                class="text-sm font-semibold text-primary hover:underline"
              >
                Ver todas
              </RouterLink>
            </div>

            <p
              v-if="loadingReservations"
              class="text-on-surface-variant text-sm"
            >
              Carregando…
            </p>
            <ul v-else-if="upcomingReservations.length" class="space-y-4">
              <li
                v-for="res in upcomingReservations"
                :key="res.id"
                class="bg-surface-container-low rounded-xl p-6 flex flex-col md:flex-row md:items-center justify-between gap-6"
              >
                <div class="flex items-center gap-6">
                  <div
                    class="w-12 h-12 rounded-lg bg-secondary-container flex items-center justify-center text-secondary shrink-0"
                  >
                    <span class="material-symbols-outlined text-[22px]"
                      >event_available</span
                    >
                  </div>
                  <div>
                    <h4 class="font-bold text-on-surface font-headline">
                      {{ res.roomName || res.roomId }}
                    </h4>
                    <p class="text-on-surface-variant text-sm">
                      {{ new Date(res.startTime).toLocaleString("pt-BR") }} —
                      {{ new Date(res.endTime).toLocaleString("pt-BR")
                      }}<template v-if="res.partySize != null">
                        · {{ res.partySize }} pessoa(s)</template
                      >
                    </p>
                  </div>
                </div>
                <div
                  class="flex items-center gap-2 px-3 py-1 rounded-full text-[0.7rem] font-bold w-fit"
                  :class="
                    res.status === 'CONFIRMED'
                      ? 'bg-emerald-100 text-emerald-800'
                      : 'bg-stone-200 text-stone-600'
                  "
                >
                  <span
                    v-if="res.status === 'CONFIRMED'"
                    class="w-1.5 h-1.5 rounded-full bg-emerald-600"
                  />
                  {{ res.status || "—" }}
                </div>
              </li>
            </ul>
            <p v-else class="text-on-surface-variant text-sm">
              Nenhuma reserva futura. Escolha uma sala acima para começar.
            </p>
          </section>
        </div>
      </div>

      <!-- Modal -->
      <div
        v-if="bookingRoom"
        class="fixed inset-0 z-60 flex items-center justify-center bg-black/45 p-4 pointer-events-auto"
        @click.self="bookingRoom = null"
      >
        <div
          class="bg-surface-container-lowest rounded-2xl max-w-md w-full p-6 shadow-xl border border-outline-variant/20"
        >
          <h3 class="font-bold text-lg font-headline mb-1">
            {{ bookingRoom.name }}
          </h3>
          <p class="text-sm text-on-surface-variant mb-4">
            Ajuste horários (local), quantidade de pessoas e confirme. A sala
            continua inteira para o intervalo; o número é só registro (máx.
            {{ bookingRoom.capacity }}).
          </p>
          <p v-if="bookingErr" class="text-sm text-red-700 mb-2">
            {{ bookingErr }}
          </p>
          <div class="space-y-3">
            <label class="block text-xs font-semibold text-on-surface-variant"
              >Início</label
            >
            <input
              v-model="startLocal"
              type="datetime-local"
              class="w-full rounded-lg border border-outline-variant/30 bg-surface-container-highest px-3 py-2.5 text-on-surface"
            />
            <label class="block text-xs font-semibold text-on-surface-variant"
              >Fim</label
            >
            <input
              v-model="endLocal"
              type="datetime-local"
              class="w-full rounded-lg border border-outline-variant/30 bg-surface-container-highest px-3 py-2.5 text-on-surface"
            />
            <label class="block text-xs font-semibold text-on-surface-variant"
              >Quantidade de pessoas</label
            >
            <select
              v-model.number="partySize"
              class="w-full rounded-lg border border-outline-variant/30 bg-surface-container-highest px-3 py-2.5 text-on-surface"
            >
              <option v-for="n in modalPartyOptions" :key="n" :value="n">
                {{ n }} {{ n === 1 ? "pessoa" : "pessoas" }}
              </option>
            </select>
          </div>
          <div class="flex gap-2 mt-6">
            <button
              type="button"
              class="flex-1 border border-outline-variant/40 rounded-lg py-2.5 font-medium text-on-surface hover:bg-surface-container-low"
              @click="bookingRoom = null"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="flex-1 primary-gradient text-white rounded-lg py-2.5 font-semibold disabled:opacity-50"
              :disabled="bookingBusy"
              @click="confirmBook"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </main>
  </AppShell>
</template>
