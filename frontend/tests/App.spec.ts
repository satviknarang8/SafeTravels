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

test("after entering valid login credentials and clicking Login, the user should be redirected", async ({ page }) => {
  await page.goto("http://localhost:5173/");
  await page.fill("#loginUsername", "test1");
  await page.fill("#loginPassword", "test1");
  await page.click("#loginButton");

  // Assuming redirection logic waits for a while before redirecting.
  await page.waitForTimeout(2000); // Wait for 2 seconds

  await expect(page.locator("#generateSafest")).toBeVisible();
});

test("after entering valid register credentials and clicking Register, a success message should appear", async ({
  page,
}) => {
  await page.goto("http://localhost:5173/");
  const randomSuffix = Math.ceil(Math.random() * 100);

  // Enter valid register credentials
  await page.fill("#registerUsername", "newUsername" + randomSuffix);
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

  await page.fill("#registerUsername", "newUsername");
  await page.fill("#registerPassword", "newPassword");

  await page.click("#registerButton");
  await page.fill("#registerUsername", "newUsername");
  await page.fill("#registerPassword", "newPassword");

  await page.click("#registerButton");

  await expect(page.locator(".custom-message")).toHaveText(
    "Registration unsuccessful, username already in use."
  );
});

test("when 'Report A Hazard' button is clicked, the hazard dropdown should be visible", async ({
  page,
}) => {
  await page.goto("http://localhost:5173/");

  await page.fill("#loginUsername", "test1");
  await page.fill("#loginPassword", "test1");

  await page.click("#loginButton");

  await page.click("#reportHazard");

  await expect(page.locator("#hazardDropdown")).toBeVisible();
});

test("on login, the 'Report A Hazard' button and 'Access Previously Viewed Routes' button should be visible", async ({
  page,
}) => {
  await page.goto("http://localhost:5173/");

  await page.fill("#loginUsername", "test1");
  await page.fill("#loginPassword", "test1");

  await page.click("#loginButton");

  await page.click("#reportHazard");

  await expect(page.locator("#reportHazard")).toBeVisible();

  await expect(page.locator("#accessPreviously")).toBeVisible();
});

test("when 'Access Previously Viewed Routes' button is clicked, the dropdown should be visible", async ({
  page,
}) => {
  await page.goto("http://localhost:5173/");

  await page.fill("#loginUsername", "test1");
  await page.fill("#loginPassword", "test1");

  await page.click("#loginButton");

  await page.click("#accessPreviously");

  await expect(page.locator("#previouslyViewedRoutesDropdown")).toBeVisible();
});

test("on page load, the hazard reporting form is not visible", async ({ page }) => {
  await page.goto("http://localhost:5173/");
  await expect(page.locator("#reportHazardForm")).not.toBeVisible();
});

test("on page load, the access routes dropdown is not visible", async ({ page }) => {
  await page.goto("http://localhost:5173/");
  await expect(page.locator("#accessRoutesDropdown")).not.toBeVisible();
});

test.describe('SafetyHandler Integration Tests', () => {

      test('successful safety rating request', async ({ request }) => {
          // Define the API endpoint with query parameters
          const endpoint = 'http://localhost:3232/safestroute?start=867%20Fenimore%20Road%2C%20NY&end=1%20E%20161%20St%2C%20Bronx%2C%20NY%2010451%2C%20United%20States';
          
          // Make the request
          const response = await request.get(endpoint);
          
          // Check if the response status is 200 OK
          expect(response.status()).toBe(200);
          
          // Further validation of response body
          const responseBody = await response.json();
          expect(responseBody).toHaveProperty('type', 'success');
          // Additional assertions can be added here based on expected response format
      });
});
