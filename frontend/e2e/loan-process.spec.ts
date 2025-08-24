import { test, expect } from '@playwright/test';

test.describe('Loan Process E2E Tests', () => {
  
  test.beforeEach(async ({ page }) => {
    // Navigate to the app
    await page.goto('/');
  });

  test('should redirect to login page when not authenticated', async ({ page }) => {
    await expect(page).toHaveURL('/login');
    await expect(page.locator('h2')).toContainText('Login');
  });

  test('should login successfully with valid credentials', async ({ page }) => {
    // Fill login form
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    
    // Click login button
    await page.click('button[type="submit"]');
    
    // Should redirect to dashboard
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('h1')).toContainText('Loan Process Dashboard');
    await expect(page.locator('.user-info span')).toContainText('Welcome, testuser!');
  });

  test('should show error with invalid credentials', async ({ page }) => {
    // Fill login form with invalid credentials
    await page.fill('#username', 'wronguser');
    await page.fill('#password', 'wrongpassword');
    
    // Click login button
    await page.click('button[type="submit"]');
    
    // Should show error message
    await expect(page.locator('.error')).toContainText('Invalid credentials');
    await expect(page).toHaveURL('/login');
  });

  test('should process loan application with high credit score (auto-approval)', async ({ page }) => {
    // Login first
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    
    // Wait for dashboard to load
    await expect(page).toHaveURL('/dashboard');
    
    // Fill loan application form with high credit score
    await page.fill('#creditScore', '750');
    
    // Submit loan application
    await page.click('button:has-text("Start Loan Process")');
    
    // Wait for result to appear
    await expect(page.locator('.result-card')).toBeVisible();
    await expect(page.locator('.result-card h3')).toContainText('Process Started Successfully!');
    
    // Check process details
    await expect(page.locator('.result-details')).toContainText('Process Instance ID:');
    await expect(page.locator('.result-details')).toContainText('loanApprovalProcess');
    await expect(page.locator('.result-details')).toContainText('Credit Score: 750');
    
    // Check status message for auto-approval
    await expect(page.locator('.status-info')).toContainText('automatically approved');
  });

  test('should process loan application with low credit score (manual review)', async ({ page }) => {
    // Login first
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    
    // Wait for dashboard to load
    await expect(page).toHaveURL('/dashboard');
    
    // Fill loan application form with low credit score
    await page.fill('#creditScore', '650');
    
    // Submit loan application
    await page.click('button:has-text("Start Loan Process")');
    
    // Wait for result to appear
    await expect(page.locator('.result-card')).toBeVisible();
    await expect(page.locator('.result-card h3')).toContainText('Process Started Successfully!');
    
    // Check process details
    await expect(page.locator('.result-details')).toContainText('Process Instance ID:');
    await expect(page.locator('.result-details')).toContainText('loanApprovalProcess');
    await expect(page.locator('.result-details')).toContainText('Credit Score: 650');
    
    // Check status message for manual review
    await expect(page.locator('.status-info')).toContainText('requires manual review');
  });

  test('should validate credit score input', async ({ page }) => {
    // Login first
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    
    // Wait for dashboard to load
    await expect(page).toHaveURL('/dashboard');
    
    // Try to submit without credit score
    await page.click('button:has-text("Start Loan Process")');
    
    // Should show validation error
    await expect(page.locator('.error')).toContainText('Credit score is required');
    
    // Try with invalid credit score (too low)
    await page.fill('#creditScore', '200');
    await page.blur('#creditScore');
    await expect(page.locator('.error')).toContainText('Credit score must be between 300 and 850');
    
    // Try with invalid credit score (too high)
    await page.fill('#creditScore', '900');
    await page.blur('#creditScore');
    await expect(page.locator('.error')).toContainText('Credit score must be between 300 and 850');
  });

  test('should display loan process workflow steps', async ({ page }) => {
    // Login first
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    
    // Wait for dashboard to load
    await expect(page).toHaveURL('/dashboard');
    
    // Check that process steps are displayed
    await expect(page.locator('.process-info h3')).toContainText('Loan Approval Process');
    
    const steps = page.locator('.step');
    await expect(steps).toHaveCount(4);
    
    await expect(steps.nth(0)).toContainText('1. Document Validation');
    await expect(steps.nth(1)).toContainText('2. Credit Score Check');
    await expect(steps.nth(2)).toContainText('3. Auto Approve (Score ≥ 700) or Manual Review (Score < 700)');
    await expect(steps.nth(3)).toContainText('4. Process Complete');
  });

  test('should logout successfully', async ({ page }) => {
    // Login first
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    
    // Wait for dashboard to load
    await expect(page).toHaveURL('/dashboard');
    
    // Click logout button
    await page.click('button:has-text("Logout")');
    
    // Should redirect to login page
    await expect(page).toHaveURL('/login');
    await expect(page.locator('h2')).toContainText('Login');
  });

  test('should handle network errors gracefully', async ({ page }) => {
    // Login first
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    
    // Wait for dashboard to load
    await expect(page).toHaveURL('/dashboard');
    
    // Simulate network failure by blocking the API endpoint
    await page.route('**/process/loan/start', route => route.abort());
    
    // Try to submit loan application
    await page.fill('#creditScore', '700');
    await page.click('button:has-text("Start Loan Process")');
    
    // Should show error message
    await expect(page.locator('.error')).toContainText('Failed to start loan process');
  });

  test('should maintain authentication state on page refresh', async ({ page }) => {
    // Login first
    await page.fill('#username', 'testuser');
    await page.fill('#password', 'demo123');
    await page.click('button[type="submit"]');
    
    // Wait for dashboard to load
    await expect(page).toHaveURL('/dashboard');
    
    // Refresh the page
    await page.reload();
    
    // Should still be authenticated and on dashboard
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('h1')).toContainText('Loan Process Dashboard');
  });
});