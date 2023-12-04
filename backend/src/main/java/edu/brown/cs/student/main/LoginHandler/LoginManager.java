package edu.brown.cs.student.main.LoginHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LoginManager {

  private Map<String, Integer> userCredentials;

  public LoginManager() {
    this.userCredentials = new HashMap<>();
  }

  public void registerUser(String username, String password) {
    int hashedPassword = this.hashPassword(password);
    userCredentials.put(username, hashedPassword);
  }

  public boolean authenticateUser(String username, String password) {
    if (userCredentials.containsKey(username)) {
      int storedHashedPassword = userCredentials.get(username);
      int enteredHashedPassword = this.hashPassword(password);

      return storedHashedPassword == enteredHashedPassword;
    }
    return false;
  }

  private int hashPassword(String password) {
    return password.hashCode();
  }
}