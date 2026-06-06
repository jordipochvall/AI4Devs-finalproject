import { test, expect } from '@playwright/test'

/**
 * HU-5-QA-01 · AC3 — login → the lobby shows the seed games and the balance →
 * click a cover → navigates to the game screen.
 * Requires the stack running (docker compose up) with the seed data.
 *
 * Note: the game grid (`<SlotGame>`) is delivered by HU-1-FE-01; here we verify up to the
 * navigation to /play/{id}.
 */
test('login → lobby with games and balance → navigates to the game', async ({ page }) => {
  // Login with a seed player (fields by id, language-independent)
  await page.goto('/login')
  await page.locator('#email').fill('player1@nova.test')
  await page.locator('#password').fill('player123')
  await page.locator('.btn-primary').click()

  // Lands in the lobby with the 3 seed games
  await expect(page).toHaveURL(/\/$|\/$/)
  await expect(page.locator('.game-card')).toHaveCount(3)

  // The balance is visible (1,000.00 € seed; thousands separator per ICU)
  await expect(page.locator('.lobby-balance')).toContainText('€')

  // Click the first cover → navigates to /play/{id}
  await page.locator('.game-card-btn').first().click()
  await expect(page).toHaveURL(/\/play\/\d+/)
})
