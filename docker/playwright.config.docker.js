// @ts-check
const { defineConfig, devices } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './e2e-docker/',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 1,
  workers: 1,
  reporter: [
    ['html', { outputFolder: '../test-documentation/playwright-report' }],
    ['json', { outputFile: '../test-documentation/test-results.json' }]
  ],
  
  use: {
    baseURL: 'http://localhost',
    trace: 'on',
    screenshot: 'on',
    video: 'retain-on-failure',
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },

  projects: [
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        viewport: { width: 1280, height: 720 }
      },
    },
  ],

  webServer: {
    command: 'echo "Services should be running via docker-compose"',
    url: 'http://localhost',
    reuseExistingServer: true,
    timeout: 5000,
  },
});