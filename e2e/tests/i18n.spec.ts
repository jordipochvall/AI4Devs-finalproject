import { test, expect } from '@playwright/test'

/**
 * HU-11-QA-01 · AC1 — conmutación de idioma en caliente y persistencia.
 * Requiere el stack levantado (docker compose up); baseURL = http://localhost:5173.
 */
test.describe('i18n — conmutación y persistencia', () => {

  test('cambia el idioma sin recargar y persiste tras recargar', async ({ page }) => {
    await page.goto('/login')

    // Forzamos ES de forma explícita (no dependemos del idioma del navegador)
    await page.getByRole('button', { name: 'ES' }).click()
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Iniciar sesión')

    // Conmutación a EN: el texto cambia al instante, sin recarga (AC2 de HU-11-FE)
    await page.getByRole('button', { name: 'EN' }).click()
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Sign in')

    // Persistencia: tras recargar sigue en EN (AC3 de HU-11-FE)
    await page.reload()
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Sign in')
  })

  test('no quedan claves i18n sin resolver en la pantalla de login', async ({ page }) => {
    await page.goto('/login')
    await page.getByRole('button', { name: 'EN' }).click()

    const bodyText = await page.locator('body').innerText()
    // Una clave sin resolver aparecería como "auth.login.title" o "shared:..." en pantalla
    expect(bodyText).not.toMatch(/auth\.login\./)
    expect(bodyText).not.toMatch(/[a-z]+:[a-z]+\./)
    // Y el texto traducido debe estar presente
    expect(bodyText).toContain('Sign in')
  })
})
