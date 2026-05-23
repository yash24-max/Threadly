import { test, expect } from "@playwright/test";

/**
 * Generates a unique email to avoid collision between parallel test runs.
 */
function uniqueEmail(): string {
  return `e2e-${Date.now()}@threadly-test.dev`;
}

const TEST_PASSWORD = "Threadly@E2E1!";

test.describe("Authentication", () => {
  test("signup creates account and redirects to dashboard", async ({ page }) => {
    const email = uniqueEmail();

    await page.goto("/signup");

    await page.getByLabel("Organisation name").fill("E2E Test Org");
    await page.getByLabel("Your name").fill("E2E Tester");
    await page.getByLabel("Email").fill(email);
    await page.getByLabel("Password").fill(TEST_PASSWORD);
    await page.getByRole("button", { name: /create account|sign up/i }).click();

    // After successful signup the user is redirected to the main dashboard
    await expect(page).toHaveURL(/\/(dashboard|bots)/, { timeout: 10_000 });
    // A welcome element confirms authentication state
    await expect(
      page.getByRole("heading", { name: /welcome|dashboard|bots/i }),
    ).toBeVisible({ timeout: 8_000 });
  });

  test("login with valid credentials redirects to dashboard", async ({ page }) => {
    // First create an account via the API so we have valid credentials
    const email = uniqueEmail();
    const signupResp = await page.request.post(
      `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/v1/auth/signup`,
      {
        data: {
          email,
          password: TEST_PASSWORD,
          orgName: "Login E2E Org",
          name: "Login E2E Tester",
        },
      },
    );
    expect(signupResp.ok()).toBeTruthy();

    await page.goto("/login");

    await page.getByLabel("Email").fill(email);
    await page.getByLabel("Password").fill(TEST_PASSWORD);
    await page.getByRole("button", { name: /log in|sign in/i }).click();

    await expect(page).toHaveURL(/\/(dashboard|bots)/, { timeout: 10_000 });
  });

  test("login with invalid password shows error toast", async ({ page }) => {
    const email = uniqueEmail();
    // Create the account first
    await page.request.post(
      `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/v1/auth/signup`,
      {
        data: {
          email,
          password: TEST_PASSWORD,
          orgName: "Wrong Pass Org",
          name: "Wrong Pass Tester",
        },
      },
    );

    await page.goto("/login");

    await page.getByLabel("Email").fill(email);
    await page.getByLabel("Password").fill("totally-wrong-password");
    await page.getByRole("button", { name: /log in|sign in/i }).click();

    // Error feedback must be visible — either an inline message or a toast
    await expect(
      page.getByRole("alert").or(page.getByText(/invalid|incorrect|wrong|unauthorized/i)),
    ).toBeVisible({ timeout: 5_000 });

    // Must NOT navigate away from the login page
    await expect(page).toHaveURL(/\/login/);
  });

  test("protected dashboard page redirects unauthenticated users to login", async ({ page }) => {
    // Navigate directly to a protected route without logging in
    await page.goto("/dashboard");

    // Should be redirected to the login page
    await expect(page).toHaveURL(/\/login/, { timeout: 8_000 });
  });

  test("logout clears session and redirects to login", async ({ page }) => {
    const email = uniqueEmail();
    // Create account
    await page.request.post(
      `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/v1/auth/signup`,
      {
        data: {
          email,
          password: TEST_PASSWORD,
          orgName: "Logout E2E Org",
          name: "Logout Tester",
        },
      },
    );

    // Log in via the UI
    await page.goto("/login");
    await page.getByLabel("Email").fill(email);
    await page.getByLabel("Password").fill(TEST_PASSWORD);
    await page.getByRole("button", { name: /log in|sign in/i }).click();
    await expect(page).toHaveURL(/\/(dashboard|bots)/, { timeout: 10_000 });

    // Open user menu and click logout
    await page.getByRole("button", { name: /user|account|profile/i }).click();
    await page.getByRole("menuitem", { name: /log out|sign out|logout/i }).click();

    // Must land on the login page after logout
    await expect(page).toHaveURL(/\/login/, { timeout: 8_000 });

    // Navigating to a protected page must redirect again — session is gone
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login/, { timeout: 5_000 });
  });
});
