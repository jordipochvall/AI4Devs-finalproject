import { test, expect } from '@playwright/test'

/**
 * HU-11-QA-01 · AC1 — hot language switching and persistence.
 * Requires the stack running (docker compose up); baseURL = http://localhost:5173.
 */
test.describe('i18n — switching and persistence', () => {

  test('switches language without reload and persists after reload', async ({ page }) => {
    await page.goto('/login')

    // Force ES explicitly (do not depend on the browser language).
    await page.getByRole('button', { name: 'ES' }).click()
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Iniciar sesión')

    // Switch to EN: the text changes instantly, without reload (AC2 of HU-11-FE).
    await page.getByRole('button', { name: 'EN' }).click()
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Sign in')

    // Persistence: after a reload it is still EN (AC3 of HU-11-FE).
    await page.reload()
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Sign in')
  })

  test('no unresolved i18n keys remain on the login screen', async ({ page }) => {
    await page.goto('/login')
    await page.getByRole('button', { name: 'EN' }).click()

    const bodyText = await page.locator('body').innerText()
    // An unresolved key would show as "auth.login.title" or "shared:..." on screen.
    expect(bodyText).not.toMatch(/auth\.login\./)
    expect(bodyText).not.toMatch(/[a-z]+:[a-z]+\./)
    // And the translated text must be present.
    expect(bodyText).toContain('Sign in')
  })
})
