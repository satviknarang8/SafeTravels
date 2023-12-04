package edu.brown.cs.student.main.tests.testLogin;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import edu.brown.cs.student.main.LoginHandler.LoginManager;
import org.junit.jupiter.api.BeforeAll;
import org.testng.annotations.Test;

public class TestLoginHandler {

  private LoginManager loginManager;

  @Test
  public void testRegisterUserAndAuthenticateUser() {
    this.loginManager = new LoginManager();
    String username = "test_user";
    String password = "test_password";

    // Register a user
    loginManager.registerUser(username, password);

    // Authenticate the registered user
    assertTrue(loginManager.authenticateUser(username, password));
  }

  @Test
  public void testAuthenticateUserInvalidPassword() {
    this.loginManager = new LoginManager();

    String username = "test_user";
    String password = "test_password";

    // Register a user
    loginManager.registerUser(username, password);

    // Try to authenticate with an incorrect password
    assertFalse(loginManager.authenticateUser(username, "wrong_password"));
  }

  @Test
  public void testAuthenticateUserNonexistentUser() {
    this.loginManager = new LoginManager();
    // Try to authenticate a user that does not exist
    assertFalse(loginManager.authenticateUser("nonexistent_user", "password123"));
  }

  @Test
  public void testHashPassword() {
    this.loginManager = new LoginManager();

    String password = "test_password";

    // Hash the password using the hashPassword method
    int hashedPassword = loginManager.hashPassword(password);

    // Manually compute the hashCode to compare
    int expectedHash = password.hashCode();

    assertEquals(expectedHash, hashedPassword);
  }
}

