import { defineConfig, devices } from '@playwright/test'

/**
 * Suite E2E de NovaCasino Studio.
 * Arranca el stack completo con docker-compose antes de ejecutar.
 * Wired as the 'e2e' CI job in ticket HU-1-QA-01.
 */
export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  retries: 1,
  reporter: [['html', { outputFolder: 'playwright-report' }]],

  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
