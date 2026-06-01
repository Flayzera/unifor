import { computed, ref } from 'vue'

const STORAGE_KEY = 'studyroom-a11y'

const accessibleMode = ref(false)

export const supportPhone = (import.meta.env.VITE_SUPPORT_PHONE || '').trim()
export const supportWhatsApp = (import.meta.env.VITE_SUPPORT_WHATSAPP || '').trim()

function loadPrefs(): boolean {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return false
    const p = JSON.parse(raw) as
      | boolean
      | { accessibleMode?: boolean; largeText?: boolean; highContrast?: boolean }
    if (typeof p === 'boolean') return p
    if (p.accessibleMode === true) return true
    return !!(p.largeText || p.highContrast)
  } catch {
    return false
  }
}

function savePrefs() {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({ accessibleMode: accessibleMode.value }),
  )
}

function applyDocumentClasses() {
  const on = accessibleMode.value
  const el = document.documentElement
  el.classList.toggle('a11y-large-text', on)
  el.classList.toggle('a11y-high-contrast', on)
}

export function initAccessibility() {
  accessibleMode.value = loadPrefs()
  applyDocumentClasses()
}

export function useAccessibility() {
  const enabled = computed(() => accessibleMode.value)

  function toggleAccessibleMode() {
    accessibleMode.value = !accessibleMode.value
    savePrefs()
    applyDocumentClasses()
  }

  /** @deprecated use toggleAccessibleMode — mantido para testes legados */
  function resetAccessibility() {
    accessibleMode.value = false
    savePrefs()
    applyDocumentClasses()
  }

  return {
    accessibleMode,
    enabled,
    toggleAccessibleMode,
    resetAccessibility,
  }
}
