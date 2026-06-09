import { test, expect } from '@playwright/test'

/**
 * HU-1-QA-01 · AC4 — happy path of the spin: login → open a game → press Spin → the round resolves
 * (the grid renders and the spin button returns to its idle state, with no error).
 * Requires the stack running (docker compose up) with the seed data.
 *
 * The "Frutas" 3x3 game is used because it has no free spins (a bounded, quick round).
 */
test('login → open game → spin resolves and updates the screen', async ({ page }) => {
  // Login with a seed player (fields by id, language-independent).
  await page.goto('/login')
  await page.locator('#email').fill('player1@nova.test')
  await page.locator('#password').fill('player123')
  await page.locator('.btn-primary').click()

  // Open the Fruits game from the lobby.
  await expect(page.locator('.game-card')).toHaveCount(3)
  await page.locator('.game-card', { hasText: /Frut|Fruit/ }).locator('.game-card-btn').click()
  await expect(page).toHaveURL(/\/play\/\d+/)

  // The data-driven grid renders.
  await expect(page.locator('.slot-grid')).toBeVisible()

  // Spin and wait for the round to resolve (button leaves the "spinning" state).
  const spinButton = page.locator('.slot-spin')
  await expect(spinButton).toBeEnabled()
  await spinButton.click()
  await expect(spinButton).toBeEnabled() // re-enabled once the request completes

  // The round succeeded: no error banner is shown and the grid is still there.
  await expect(page.locator('.slot-error')).toHaveCount(0)
  await expect(page.locator('.slot-grid')).toBeVisible()
})
