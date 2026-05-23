import { test, expect, type Page } from "@playwright/test";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const TEST_PASSWORD = "Threadly@E2E1!";

/**
 * Creates an account, logs in via the UI, creates a bot, and navigates to
 * the flow builder for that bot. Returns the bot ID.
 */
async function loginAndOpenBuilder(page: Page): Promise<string> {
  const email = `builder-${Date.now()}@threadly-test.dev`;

  // Create account via API (faster than full UI signup for fixture setup)
  const signupResp = await page.request.post(`${API_BASE}/v1/auth/signup`, {
    data: {
      email,
      password: TEST_PASSWORD,
      orgName: `Builder E2E Org ${Date.now()}`,
      name: "Builder Tester",
    },
  });
  expect(signupResp.ok()).toBeTruthy();
  const { accessToken } = await signupResp.json();

  // Create a bot via API
  const botResp = await page.request.post(`${API_BASE}/v1/bots`, {
    data: { name: "E2E Flow Bot", description: "Playwright test bot", language: "en" },
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  expect(botResp.ok()).toBeTruthy();
  const bot = await botResp.json();

  // Log in via the UI so the browser session is authenticated
  await page.goto("/login");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill(TEST_PASSWORD);
  await page.getByRole("button", { name: /log in|sign in/i }).click();
  await expect(page).toHaveURL(/\/(dashboard|bots)/, { timeout: 10_000 });

  // Navigate to the builder for this specific bot
  await page.goto(`/builder/${bot.id}`);

  return bot.id as string;
}

test.describe("Flow Builder", () => {
  test("builder page loads with flow canvas", async ({ page }) => {
    await loginAndOpenBuilder(page);

    // The React Flow canvas renders as a div with data-testid or role="presentation"
    await expect(
      page.locator(".react-flow").or(page.getByTestId("flow-canvas")),
    ).toBeVisible({ timeout: 15_000 });
  });

  test("node panel shows all 5 categories", async ({ page }) => {
    await loginAndOpenBuilder(page);

    // Node palette / sidebar must display at least these 5 category headings
    const expectedCategories = [
      /message|messages/i,
      /logic|condition/i,
      /ai|intelligence/i,
      /integration|api/i,
      /input|collect/i,
    ];

    for (const category of expectedCategories) {
      await expect(page.getByRole("heading", { name: category })).toBeVisible({
        timeout: 8_000,
      });
    }
  });

  test("search in node panel filters nodes", async ({ page }) => {
    await loginAndOpenBuilder(page);

    const searchInput = page
      .getByPlaceholder(/search nodes|search blocks/i)
      .or(page.getByRole("searchbox", { name: /node|block/i }));

    await searchInput.fill("message");

    // After typing "message" the panel should show message-type nodes
    await expect(
      page.getByRole("button", { name: /message/i }).first(),
    ).toBeVisible({ timeout: 5_000 });

    // Non-matching categories should be hidden
    await expect(
      page.getByRole("button", { name: /^delay$/i }),
    ).toBeHidden({ timeout: 3_000 });
  });

  test("clicking a node adds it to canvas", async ({ page }) => {
    await loginAndOpenBuilder(page);

    // Count existing nodes before adding
    const nodesBefore = await page.locator(".react-flow__node").count();

    // Click the first available node type in the palette
    await page.getByRole("button", { name: /message/i }).first().click();

    // A new node should appear on the canvas
    await expect(page.locator(".react-flow__node")).toHaveCount(nodesBefore + 1, {
      timeout: 5_000,
    });
  });

  test("saved indicator shows after node change", async ({ page }) => {
    await loginAndOpenBuilder(page);

    // Add a node to trigger a save
    await page.getByRole("button", { name: /message/i }).first().click();

    // The UI must acknowledge the save with a status indicator
    await expect(
      page
        .getByText(/saved|auto.?saved|changes saved/i)
        .or(page.getByRole("status", { name: /saved/i })),
    ).toBeVisible({ timeout: 8_000 });
  });

  test("undo with Cmd+Z removes last change", async ({ page }) => {
    await loginAndOpenBuilder(page);

    const nodesBefore = await page.locator(".react-flow__node").count();

    // Add a node
    await page.getByRole("button", { name: /message/i }).first().click();
    await expect(page.locator(".react-flow__node")).toHaveCount(
      nodesBefore + 1,
      { timeout: 4_000 },
    );

    // Undo — use Meta+Z on macOS, Control+Z everywhere else
    await page.keyboard.press("Meta+Z");

    // The node count must revert to the original
    await expect(page.locator(".react-flow__node")).toHaveCount(nodesBefore, {
      timeout: 5_000,
    });
  });

  test("publish button becomes enabled after edit", async ({ page }) => {
    await loginAndOpenBuilder(page);

    const publishBtn = page.getByRole("button", { name: /publish/i });

    // Initially disabled (no unsaved changes on a fresh flow)
    // After adding a node the publish button should become active
    await page.getByRole("button", { name: /message/i }).first().click();

    await expect(publishBtn).toBeEnabled({ timeout: 8_000 });
  });
});
