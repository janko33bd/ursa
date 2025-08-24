const { test, expect } = require('@playwright/test');

test.describe('Docker Integration Tests - Login and Camunda Process', () => {
  
  test.beforeEach(async ({ page }) => {
    // Navigate to the containerized app
    await page.goto('http://localhost:80');
    // Wait for page to load
    await page.waitForLoadState('networkidle');
  });

  test('should redirect unauthenticated users to login page', async ({ page }) => {
    await expect(page).toHaveURL(/.*login/);
    await expect(page.locator('h2')).toContainText('Login');
    await expect(page.locator('#username')).toBeVisible();
    await expect(page.locator('#password')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('should authenticate user and access dashboard', async ({ page }) => {
    console.log('🔐 Testing authentication with testuser...');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    await expect(page).toHaveURL(/.*dashboard/);
    await expect(page.locator('h1')).toContainText('Loan Process Dashboard');
    await expect(page.locator('.user-info')).toContainText('Welcome, testuser');
    console.log('✅ Authentication successful');
  });

  test('should display loan process form and workflow info', async ({ page }) => {
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    await expect(page.locator('#creditScore')).toBeVisible();
    await expect(page.locator('button:has-text("Start Loan Process")')).toBeVisible();
    await expect(page.locator('.process-info')).toBeVisible();
    await expect(page.locator('.process-info h3')).toContainText('Loan Approval Process');
    const steps = page.locator('.step');
    await expect(steps).toHaveCount(4);
    await expect(steps.nth(0)).toContainText('1. Document Validation');
    await expect(steps.nth(1)).toContainText('2. Credit Score Check');
    await expect(steps.nth(2)).toContainText('3. Auto Approve');
    await expect(steps.nth(3)).toContainText('4. Process Complete');
  });

  test('should start high credit score loan process (auto-approval path)', async ({ page }) => {
    console.log('🏦 Testing high credit score loan process...');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    await page.fill('#creditScore', '750');
    await page.click('button:has-text("Start Loan Process")');
    await expect(page.locator('.result-card')).toBeVisible({ timeout: 20000 });
    await expect(page.locator('.result-card h3')).toContainText('Process Started Successfully!');
    await expect(page.locator('.result-details')).toContainText('Credit Score: 750');
    await expect(page.locator('.status-info')).toContainText('processed automatically');
    console.log('✅ High credit score process completed successfully');
  });

  test('should start low credit score loan process (manual review path)', async ({ page }) => {
    console.log('🏦 Testing low credit score loan process...');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    await page.fill('#creditScore', '650');
    await page.click('button:has-text("Start Loan Process")');
    await expect(page.locator('.result-card')).toBeVisible({ timeout: 20000 });
    await expect(page.locator('.result-details')).toContainText('Credit Score: 650');
    await expect(page.locator('.status-info')).toContainText('requires manual review');
    console.log('✅ Low credit score process completed successfully');
  });

  test('should validate credit score input', async ({ page }) => {
    console.log('🔍 Testing credit score validation...');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    await expect(page.locator('button:has-text("Start Loan Process")')).toBeDisabled();
    await page.fill('#creditScore', '200');
    await page.locator('#creditScore').blur();
    await expect(page.locator('button:has-text("Start Loan Process")')).toBeDisabled();
    await page.fill('#creditScore', '900');
    await page.locator('#creditScore').blur();
    await expect(page.locator('button:has-text("Start Loan Process")')).toBeDisabled();
    await page.fill('#creditScore', '700');
    await page.locator('#creditScore').blur();
    await expect(page.locator('button:has-text("Start Loan Process")')).toBeEnabled();
    console.log('✅ Credit score validation working correctly');
  });

  test('should handle authentication errors', async ({ page }) => {
    console.log('🚫 Testing authentication error handling...');
    await page.fill('#username', 'wronguser');
    await page.fill('#password', 'wrongpassword');
    await page.click('button[type="submit"]');
    await expect(page.locator('.error-message')).toContainText('Invalid credentials');
    await expect(page).toHaveURL(/.*login/);
    console.log('✅ Authentication error handling working correctly');
  });

  test('should maintain authentication state on page refresh', async ({ page }) => {
    console.log('🔄 Testing authentication persistence...');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('Loan Process Dashboard');
    await page.reload();
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/.*dashboard/);
    await expect(page.locator('h1')).toContainText('Loan Process Dashboard');
    console.log('✅ Authentication persistence working correctly');
  });

  test('should logout successfully', async ({ page }) => {
    console.log('🚪 Testing logout functionality...');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    await page.click('button:has-text("Logout")');
    await expect(page).toHaveURL(/.*login/);
    await expect(page.locator('h2')).toContainText('Login');
    console.log('✅ Logout working correctly');
  });

  test('should test multiple loan processes in sequence', async ({ page }) => {
    console.log('🔄 Testing multiple sequential loan processes...');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    await page.fill('#creditScore', '800');
    await page.click('button:has-text("Start Loan Process")');
    await expect(page.locator('.result-card')).toBeVisible({ timeout: 20000 });
    await page.goto('http://localhost:80/dashboard');
    await page.waitForLoadState('networkidle');
    await page.fill('#creditScore', '680');
    await page.click('button:has-text("Start Loan Process")');
    await expect(page.locator('.result-card')).toBeVisible({ timeout: 20000 });
    console.log('✅ Multiple sequential processes completed');
  });

  test('should verify backend API endpoints are accessible', async ({ page }) => {
    console.log('🔗 Testing backend API accessibility...');
    const healthResponse = await page.request.get('http://localhost:8080/actuator/health');
    expect(healthResponse.ok()).toBeTruthy();
    const healthData = await healthResponse.json();
    expect(healthData.status).toBe('UP');
    console.log('✅ Backend health endpoint accessible');
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*dashboard/, { timeout: 15000 });
    console.log('✅ Backend integration test completed');
  });
});
