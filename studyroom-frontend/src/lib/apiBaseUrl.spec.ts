import { describe, expect, it, vi } from "vitest"
import { resolveApiBaseUrl } from "./apiBaseUrl"

describe("resolveApiBaseUrl", () => {
  it("returns empty string in dev (proxy)", () => {
    expect(resolveApiBaseUrl({ viteApiUrl: "anything", isDev: true })).toBe("")
  })

  it("prefixes https when host has no scheme", () => {
    expect(
      resolveApiBaseUrl({
        viteApiUrl: "api.example.com",
        isDev: false,
        logError: () => {},
      }),
    ).toBe("https://api.example.com")
  })

  it("keeps https URL and strips trailing slash", () => {
    expect(
      resolveApiBaseUrl({
        viteApiUrl: "https://api.example.com/",
        isDev: false,
        logError: () => {},
      }),
    ).toBe("https://api.example.com")
  })

  it("returns empty and logs when missing env in prod", () => {
    const log = vi.fn()
    expect(resolveApiBaseUrl({ viteApiUrl: "", isDev: false, logError: log })).toBe("")
    expect(log).toHaveBeenCalled()
  })

  it("returns empty when localhost in prod", () => {
    const log = vi.fn()
    expect(
      resolveApiBaseUrl({
        viteApiUrl: "http://localhost:8080",
        isDev: false,
        logError: log,
      }),
    ).toBe("")
    expect(log).toHaveBeenCalled()
  })
})
