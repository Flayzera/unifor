import { describe, expect, it, beforeEach } from 'vitest'
import { initAccessibility, useAccessibility } from './useAccessibility'

describe('useAccessibility', () => {
  beforeEach(() => {
    localStorage.clear()
    document.documentElement.classList.remove('a11y-large-text', 'a11y-high-contrast')
    initAccessibility()
  })

  it('liga texto maior e alto contraste juntos no modo fácil', () => {
    const { toggleAccessibleMode, enabled } = useAccessibility()
    expect(enabled.value).toBe(false)
    toggleAccessibleMode()
    expect(enabled.value).toBe(true)
    expect(document.documentElement.classList.contains('a11y-large-text')).toBe(true)
    expect(document.documentElement.classList.contains('a11y-high-contrast')).toBe(true)
    toggleAccessibleMode()
    expect(enabled.value).toBe(false)
    expect(document.documentElement.classList.contains('a11y-large-text')).toBe(false)
  })
})
