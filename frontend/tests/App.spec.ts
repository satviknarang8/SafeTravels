import { test, expect } from "@playwright/test";

// this tests confirms that a submit button is present upon page load.
test("on page load, i see a submit button", async ({ page }) => {
  await page.goto("http://localhost:5173/");
  await expect(page.getByRole("button", { name: "Login" })).toBeVisible();
});

// this tests confirms that a command button to display a dialogue of valid commands is present upon page load.
test("on page load, the login form is rendered", async ({ page }) => {
  await page.goto("http://localhost:5173/");

  // Check if the login form is rendered
  await expect(page.locator("#loginForm")).toBeVisible();

  // Check if the login inputs and button are rendered
  await expect(page.locator("#loginUsername")).toBeVisible();
  await expect(page.locator("#loginPassword")).toBeVisible();
  await expect(page.locator("#loginButton")).toBeVisible();
});

test("on page load, the register form is rendered", async ({ page }) => {
  await page.goto("http://localhost:5173/");

  // Check if the register form is rendered
  await expect(page.locator("#registerForm")).toBeVisible();

  // Check if the register inputs and button are rendered
  await expect(page.locator("#registerUsername")).toBeVisible();
  await expect(page.locator("#registerPassword")).toBeVisible();
  await expect(page.locator("#registerButton")).toBeVisible();
});

test("after entering valid login credentials and clicking Login, the user should be redirected", async ({
  page,
}) => {
  await page.goto("http://localhost:5173/");

  // Enter valid login credentials
  await page.fill("#loginUsername", "test1");
  await page.fill("#loginPassword", "test1");

  // Click the Login button
  await page.click("#loginButton");

  // Check if the user is redirected (modify this based on your actual redirection logic)

  // You might want to assert some condition on the redirected page
  await expect(page.locator("#generateSafestButton")).toBeVisible();
});

test("after entering valid register credentials and clicking Register, a success message should appear", async ({
  page,
}) => {
  await page.goto("http://localhost:5173/");

  // Enter valid register credentials
  await page.fill("#registerUsername", "newUsername");
  await page.fill("#registerPassword", "newPassword");

  // Click the Register button
  await page.click("#registerButton");

  // Check if the success message appears
  await expect(page.locator(".custom-message")).toHaveText(
    "Registration successful!"
  );
});

test("Upon running the above a second time, an error message should appear", async ({
  page,
}) => {
  await page.goto("http://localhost:5173/");

  // Enter valid register credentials
  await page.fill("#registerUsername", "newUsername");
  await page.fill("#registerPassword", "newPassword");

  // Click the Register button
  await page.click("#registerButton");

  // Check if the success message appears
  await expect(page.locator(".custom-message")).toHaveText(
    "Registration unsuccessful, this username is already in use."
  );
});
