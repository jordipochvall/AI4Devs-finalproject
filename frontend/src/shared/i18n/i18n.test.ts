import { describe, it, expect, beforeEach } from 'vitest'
import i18n, { SUPPORTED_LANGS } from './i18n'

// Recolecta recursivamente las rutas de claves "hoja" de un objeto anidado.
function leafKeys(obj: Record<string, unknown>, prefix = ''): string[] {
  return Object.entries(obj).flatMap(([k, v]) => {
    const path = prefix ? `${prefix}.${k}` : k
    return typeof v === 'object' && v !== null
      ? leafKeys(v as Record<string, unknown>, path)
      : [path]
  })
}

const NAMESPACES = ['shared', 'player', 'operator', 'math'] as const

describe('i18n setup', () => {
  beforeEach(async () => {
    await i18n.changeLanguage('es')
  })

  it('supports es and en', () => {
    expect(SUPPORTED_LANGS).toEqual(['es', 'en'])
  })

  it('defaults to the shared namespace', () => {
    expect(i18n.options.defaultNS).toBe('shared')
  })

  it('resolves a key in the default (shared) namespace', () => {
    expect(i18n.t('auth.login.title')).toBe('Iniciar sesión')
  })

  it('resolves keys in surface namespaces', () => {
    expect(i18n.t('player:lobby.title')).toBe('Sala de juegos')
    expect(i18n.t('operator:backoffice.title')).toBe('Backoffice del operador')
    expect(i18n.t('math:backoffice.title')).toBe('Backoffice matemático')
  })

  it('switches language at runtime (AC2)', async () => {
    expect(i18n.t('auth.login.title')).toBe('Iniciar sesión')
    await i18n.changeLanguage('en')
    expect(i18n.t('auth.login.title')).toBe('Sign in')
    expect(i18n.t('player:lobby.title')).toBe('Game lobby')
  })

  it('persists the chosen language to localStorage (AC3)', async () => {
    await i18n.changeLanguage('en')
    expect(localStorage.getItem('nova-lang')).toBe('en')
  })

  // AC4: ninguna clave sin traducir — paridad de claves entre es y en por namespace
  it('has identical key sets across es and en for every namespace', () => {
    for (const ns of NAMESPACES) {
      const es = i18n.getResourceBundle('es', ns) as Record<string, unknown>
      const en = i18n.getResourceBundle('en', ns) as Record<string, unknown>
      const esKeys = leafKeys(es).sort()
      const enKeys = leafKeys(en).sort()
      expect(enKeys, `namespace ${ns} difiere entre es/en`).toEqual(esKeys)
    }
  })
})
