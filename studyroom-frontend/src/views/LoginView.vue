<script setup lang="ts">
import { nextTick, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  signInWithEmailAndPassword,
  signInWithPopup,
  createUserWithEmailAndPassword,
} from "firebase/auth";
import { auth, googleProvider } from "../lib/firebase";
import { syncBackendProfile } from "../composables/useAuth";

const router = useRouter();
const route = useRoute();

const email = ref("");
const password = ref("");
const error = ref("");
const busy = ref(false);
const modeRegister = ref(false);

/**
 * Entra na área logada antes de sincronizar com a API.
 * Se o backend estiver lento ou offline, o sync não bloqueia a navegação.
 */
async function afterAuth() {
  await auth.authStateReady();
  const u = auth.currentUser;
  if (u) await u.getIdToken();
  const redirect = (route.query.redirect as string) || "/rooms";
  await nextTick();
  await router.replace(redirect);
  try {
    await syncBackendProfile();
  } catch {
    /* best-effort; login já concluiu no Firebase */
  }
}

async function onGoogle() {
  error.value = "";
  busy.value = true;
  try {
    await signInWithPopup(auth, googleProvider);
    await auth.authStateReady();
    await afterAuth();
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : "Falha no login com Google";
  } finally {
    busy.value = false;
  }
}

async function onEmailSubmit() {
  error.value = "";
  busy.value = true;
  try {
    if (modeRegister.value) {
      await createUserWithEmailAndPassword(
        auth,
        email.value.trim(),
        password.value,
      );
    } else {
      await signInWithEmailAndPassword(
        auth,
        email.value.trim(),
        password.value,
      );
    }
    await afterAuth();
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : "E-mail ou senha inválidos";
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <main
    class="bg-surface text-on-surface min-h-screen flex flex-col selection:bg-[#ffdbd1] selection:text-on-surface"
  >
    <div
      class="grow flex items-center justify-center p-6 sm:p-12 relative overflow-hidden"
    >
      <div class="absolute top-0 left-0 w-full h-full pointer-events-none z-0">
        <div
          class="absolute -top-[10%] -left-[5%] w-[40%] h-[40%] rounded-full bg-primary-container/10 blur-[120px]"
        />
        <div
          class="absolute bottom-[10%] right-[0%] w-[30%] h-[30%] rounded-full bg-secondary-container/10 blur-[100px]"
        />
      </div>

      <div class="w-full max-w-lg z-10">
        <div class="text-center mb-10">
          <div
            class="inline-flex items-center justify-center w-16 h-16 rounded-xl signature-gradient text-white mb-6 shadow-xl shadow-primary/20"
          >
            <span class="material-symbols-outlined text-3xl">school</span>
          </div>
          <h1
            class="text-3xl font-extrabold tracking-tight text-on-surface mb-2 font-headline"
          >
            Study Room System
          </h1>
          <p class="text-on-surface-variant max-w-xs mx-auto leading-relaxed">
            Acesse o ambiente acadêmico e gerencie suas salas de estudo.
          </p>
        </div>

        <div
          class="glass-panel border border-outline-variant/15 rounded-2xl p-8 sm:p-10 shadow-[0px_20px_40px_rgba(39,24,19,0.06)]"
        >
          <p
            v-if="error"
            class="mb-4 text-sm text-red-700 bg-red-50 rounded-lg px-3 py-2"
          >
            {{ error }}
          </p>

          <form class="space-y-6" @submit.prevent="onEmailSubmit">
            <button
              type="button"
              class="w-full flex items-center justify-center gap-3 py-3.5 px-4 bg-surface-container-lowest border border-outline-variant/30 rounded-lg font-semibold text-on-surface hover:bg-surface transition-all active:scale-[0.98]"
              :disabled="busy"
              @click="onGoogle"
            >
              <img
                alt="Google"
                class="w-5 h-5"
                src="https://www.google.com/favicon.ico"
              />
              <span>Continuar com Google</span>
            </button>

            <div class="relative flex items-center py-2">
              <div class="grow border-t border-outline-variant/20" />
              <span
                class="shrink mx-4 text-xs text-on-surface-variant/60 font-medium uppercase tracking-widest"
                >Ou e-mail</span
              >
              <div class="grow border-t border-outline-variant/20" />
            </div>

            <div class="space-y-4">
              <div>
                <label
                  class="block text-sm font-semibold text-on-surface-variant mb-1.5 ml-1"
                  for="email"
                  >E-mail</label
                >
                <div class="relative">
                  <span
                    class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant/50 text-xl"
                    >mail</span
                  >
                  <input
                    id="email"
                    v-model="email"
                    class="w-full pl-12 pr-4 py-3.5 bg-surface-container-highest/50 border-none rounded-lg focus:ring-2 focus:ring-primary/25 placeholder:text-on-surface-variant/40"
                    placeholder="nome@universidade.edu"
                    type="email"
                    required
                    autocomplete="email"
                  />
                </div>
              </div>
              <div>
                <label
                  class="block text-sm font-semibold text-on-surface-variant mb-1.5 ml-1"
                  for="password"
                  >Senha</label
                >
                <div class="relative">
                  <span
                    class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant/50 text-xl"
                    >lock</span
                  >
                  <input
                    id="password"
                    v-model="password"
                    class="w-full pl-12 pr-4 py-3.5 bg-surface-container-highest/50 border-none rounded-lg focus:ring-2 focus:ring-primary/25 placeholder:text-on-surface-variant/40"
                    type="password"
                    required
                    autocomplete="current-password"
                  />
                </div>
              </div>
            </div>

            <button
              type="submit"
              class="w-full py-4 px-6 signature-gradient text-white rounded-lg font-bold text-lg shadow-lg shadow-primary/20 active:scale-[0.97] transition-all disabled:opacity-60"
              :disabled="busy"
            >
              {{ modeRegister ? "Criar conta" : "Entrar no portal" }}
            </button>
          </form>

          <div class="mt-6 text-center">
            <button
              type="button"
              class="text-sm text-primary font-bold hover:underline"
              @click="modeRegister = !modeRegister"
            >
              {{
                modeRegister
                  ? "Já tenho conta — entrar"
                  : "Novo na plataforma — criar conta"
              }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>
