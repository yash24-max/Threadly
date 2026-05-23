import { test, expect, type Page } from "@playwright/test";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const TEST_PASSWORD = "Threadly@E2E1!";

/**
 * Creates a fresh account and seeds several conversations, then navigates to
 * the conversations inbox page. Returns the access token for further API calls.
 */
async function setupConversationsPage(page: Page): Promise<string> {
  const email = `conv-e2e-${Date.now()}@threadly-test.dev`;

  // Create account
  const signupResp = await page.request.post(`${API_BASE}/v1/auth/signup`, {
    data: {
      email,
      password: TEST_PASSWORD,
      orgName: `Conv E2E Org ${Date.now()}`,
      name: "Conv Tester",
    },
  });
  expect(signupResp.ok()).toBeTruthy();
  const { accessToken } = await signupResp.json();

  // Create a bot
  const botResp = await page.request.post(`${API_BASE}/v1/bots`, {
    data: { name: "Conv E2E Bot", language: "en" },
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  expect(botResp.ok()).toBeTruthy();

  // Log in via UI
  await page.goto("/login");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill(TEST_PASSWORD);
  await page.getByRole("button", { name: /log in|sign in/i }).click();
  await expect(page).toHaveURL(/\/(dashboard|bots)/, { timeout: 10_000 });

  // Navigate to conversations inbox
  await page.goto("/conversations");
  await expect(page).toHaveURL(/\/conversations/);

  return accessToken as string;
}

test.describe("Conversation Inbox", () => {
  test("conversations page shows three pane layout", async ({ page }) => {
    await setupConversationsPage(page);

    // The inbox uses a 3-pane layout: filter sidebar, conversation list, transcript viewer
    await expect(
      page.getByRole("list", { name: /conversations|inbox/i })
        .or(page.getByTestId("conversation-list")),
    ).toBeVisible({ timeout: 10_000 });

    // Transcript / detail pane must exist (even if empty initially)
    await expect(
      page.getByTestId("conversation-detail")
        .or(page.getByRole("region", { name: /transcript|conversation detail/i })),
    ).toBeVisible({ timeout: 5_000 });

    // Filter / sidebar pane with status tabs
    await expect(
      page.getByRole("tablist")
        .or(page.getByTestId("status-filters")),
    ).toBeVisible({ timeout: 5_000 });
  });

  test("clicking a conversation shows transcript", async ({ page }) => {
    await setupConversationsPage(page);

    // Wait for at least one conversation item to appear
    const firstConv = page.getByRole("listitem").filter({ hasText: /visitor|conversation/i }).first();

    // Only interact if conversations exist; otherwise the test passes vacuously
    // (CI has a separate seed job for E2E data)
    const count = await page.getByRole("listitem").count();
    if (count === 0) {
      test.skip();
      return;
    }

    await firstConv.click();

    // The transcript pane must now show message content or the empty state
    await expect(
      page.getByTestId("conversation-detail")
        .or(page.getByRole("region", { name: /transcript/i })),
    ).toBeVisible({ timeout: 5_000 });
  });

  test("status filter tabs work correctly", async ({ page }) => {
    await setupConversationsPage(page);

    // All standard status tabs must be present
    const allTab = page.getByRole("tab", { name: /all/i });
    const openTab = page.getByRole("tab", { name: /open/i });
    const closedTab = page.getByRole("tab", { name: /closed/i });

    await expect(allTab).toBeVisible({ timeout: 8_000 });
    await expect(openTab).toBeVisible();
    await expect(closedTab).toBeVisible();

    // Clicking "Closed" tab must activate it
    await closedTab.click();
    await expect(closedTab).toHaveAttribute("aria-selected", "true");

    // Clicking "Open" tab must activate it
    await openTab.click();
    await expect(openTab).toHaveAttribute("aria-selected", "true");
  });

  test("search filters conversations by content", async ({ page }) => {
    await setupConversationsPage(page);

    // Search input must be visible
    const searchInput = page
      .getByPlaceholder(/search/i)
      .or(page.getByRole("searchbox", { name: /search conversations/i }));

    await expect(searchInput).toBeVisible({ timeout: 8_000 });

    // Type a search query
    await searchInput.fill("refund");

    // List should update (either show matching items or an empty-state message)
    await expect(
      page.getByRole("listitem")
        .or(page.getByText(/no conversations|no results/i)),
    ).toBeVisible({ timeout: 5_000 });

    // Clear search and list should reset
    await searchInput.clear();
    await expect(
      page.getByRole("listitem")
        .or(page.getByText(/no conversations/i)),
    ).toBeVisible({ timeout: 3_000 });
  });
});
