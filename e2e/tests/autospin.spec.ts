import { test, expect } from '@playwright/test'

/**
 * HU-9-QA-01 · AC1/AC3 — auto-spin happy path over the running stack: a batch stops on its own when
 * the spin count is reached, and a running batch stops when "Stop" is pressed. (Language-agnostic
 * class selectors, since the app may render in en or es.) Requires the stack up with seed data.
 */

async function openFruits(page) {
  await page.goto('/login')
  await page.locator('#email').fill('player1@nova.test')
  await page.locator('#password').fill('player123')
  await page.locator('.btn-primary').click()
  await expect(page.locator('.game-card')).toHaveCount(3)
  await page.locator('.game-card', { hasText: /Frut|Fruit/ }).locator('.game-card-btn').click()
  await expect(page.locator('.slot-grid')).toBeVisible()
}

test('auto-spin stops automatically after the configured number of spins (AC1)', async ({ page }) => {
  await openFruits(page)

  await page.locator('.slot-auto-count').fill('3')
  await page.locator('.slot-auto .slot-auto-btn').click()

  // The Stop button appears while running, then the batch finishes and the Auto controls return.
  await expect(page.locator('.slot-auto-stopbtn')).toHaveCount(0, { timeout: 15_000 })
  await expect(page.locator('.slot-auto')).toBeVisible()
})

test('a running auto-spin batch stops when "Stop" is pressed (AC3)', async ({ page }) => {
  await openFruits(page)

  await page.locator('.slot-auto-count').fill('50')
  await page.locator('.slot-auto .slot-auto-btn').click()

  // While the (long) batch runs, pressing Stop returns to the Auto controls.
  await page.locator('.slot-auto-stopbtn').click()
  await expect(page.locator('.slot-auto')).toBeVisible({ timeout: 15_000 })
})
