import { test, expect } from '@playwright/test'

/**
 * HU-12-QA-01 · AC1/AC2 — the DGOJ seal and "+18" notice are present, and the
 * "Responsible gaming" link opens its information. Checked on the login screen (no auth).
 * Requires the stack running (docker compose up).
 *
 * Note: presence on the lobby and game screens, and the loss-threshold notice (AC3),
 * are covered once play exists (HU-1-FE / HU-9).
 */
test('login shows the DGOJ seal and +18, and opens the responsible gaming dialog', async ({ page }) => {
  await page.goto('/login')

  await expect(page.locator('.compliance-banner')).toContainText('DGOJ')
  await expect(page.locator('.compliance-banner')).toContainText('+18')

  await page.getByRole('button', { name: /Juego responsable|Responsible gaming/ }).click()
  await expect(page.locator('.rg-dialog')).toBeVisible()
})
