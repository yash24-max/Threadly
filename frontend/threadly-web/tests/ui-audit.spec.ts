import { test } from "@playwright/test";
import path from "path";

const SHOTS = path.resolve(__dirname, "../screenshots");
test.use({ viewport: { width: 1440, height: 900 } });

test("Landing full page", async ({ page }) => {
  await page.goto("/");
  await page.waitForLoadState("networkidle");
  await page.screenshot({ path: `${SHOTS}/01-landing-top.png`,      fullPage: false });
  await page.evaluate(() => window.scrollTo(0, 1400));
  await page.waitForTimeout(200);
  await page.screenshot({ path: `${SHOTS}/02-landing-features.png`, fullPage: false });
  await page.evaluate(() => window.scrollTo(0, 2800));
  await page.waitForTimeout(200);
  await page.screenshot({ path: `${SHOTS}/03-landing-usecases.png`, fullPage: false });
  await page.evaluate(() => window.scrollTo(0, 4400));
  await page.waitForTimeout(200);
  await page.screenshot({ path: `${SHOTS}/04-landing-pricing.png`,  fullPage: false });
  await page.screenshot({ path: `${SHOTS}/00-landing-full.png`,     fullPage: true  });
});

test("Login page", async ({ page }) => {
  await page.goto("/login");
  await page.waitForLoadState("networkidle");
  await page.screenshot({ path: `${SHOTS}/05-login.png`, fullPage: true });
});

test("Signup page", async ({ page }) => {
  await page.goto("/signup");
  await page.waitForLoadState("networkidle");
  await page.screenshot({ path: `${SHOTS}/06-signup.png`, fullPage: true });
});
