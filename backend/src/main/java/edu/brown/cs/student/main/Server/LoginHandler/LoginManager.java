package edu.brown.cs.student.main.Server.LoginHandler;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles login and registration requests.
 */
public class LoginManager implements Route {

  private static Map<String, Integer> userCredentials;

  /**
   * Constructor initializes user credentials with a default test user.
   */
  public LoginManager() {
    userCredentials = new HashMap<>();
    userCredentials.put("test1", "test1".hashCode());
  }

  /**
   * Handles incoming requests.
   *
   * @param request  The HTTP request object.
   * @param response The HTTP response object.
   * @return The response object based on the request type.
   * @throws Exception Throws an exception if there's an error processing the request.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    String path = request.pathInfo();

    // Check the path to determine the type of request
    if ("/login".equals(path)) {
      return handleLogin(request);
    } else if ("/register".equals(path)) {
      // Consider both "/register" and "/register/" as valid paths for registration
      System.out.println("here");
      return handleRegister(request);
    } else {
      // Handle other paths or return an error response
      response.status(404);
      System.out.println("here!");
      return "Not Found";
    }
  }

  /**
   * Handles the login process.
   *
   * @param request The HTTP request object containing user credentials.
   * @return A response based on the success or failure of the login attempt.
   */
  private Object handleLogin(Request request) {
    String username = request.queryParams("username");
    String password = request.queryParams("password");

    try {
      authenticateUser(username, password);

      // Authentication successful
      Map<String, Object> successResponse = new HashMap<>();
      successResponse.put("type", "success");
      successResponse.put("message", "Login successful");
      return toJson(successResponse);
    } catch (AuthenticationException e) {
      // Handle the case where the user is not found
      if ("User not found".equals(e.getMessage())) {
        Map<String, Object> notFoundResponse = new HashMap<>();
        notFoundResponse.put("type", "fail");
        notFoundResponse.put("error", "User not found");
        return toJson(notFoundResponse);
      } else {
        // Handle other authentication errors
        Map<String, Object> failResponse = new HashMap<>();
        failResponse.put("type", "fail");
        failResponse.put("error", e.getMessage());
        return toJson(failResponse);
      }
    }
  }

  /**
   * Handles the registration process.
   *
   * @param request The HTTP request object containing user credentials.
   * @return A response based on the success or failure of the registration attempt.
   */
  private Object handleRegister(Request request) {
    String username = request.queryParams("username");
    String password = request.queryParams("password");

    try {
      registerUser(username, password);

      // Registration successful
      Map<String, Object> successResponse = new HashMap<>();
      successResponse.put("type", "success");
      successResponse.put("message", "Registration successful");
      return toJson(successResponse);
    } catch (AuthenticationException e) {
      // Registration failed
      Map<String, Object> failResponse = new HashMap<>();
      failResponse.put("type", "fail");
      failResponse.put("error", e.getMessage());
      return toJson(failResponse);
    }
  }

  /**
   * Converts a map to its JSON string representation.
   *
   * @param responseMap The map to convert.
   * @return The JSON string representation of the map.
   */
  private String toJson(Map<String, Object> responseMap) {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    return adapter.toJson(responseMap);
  }
  /**
   * Registers a new user.
   *
   * @param username The username of the new user.
   * @param password The password of the new user.
   * @throws AuthenticationException If the username already exists.
   */

  public void registerUser(String username, String password) throws AuthenticationException {
    if (userCredentials.containsKey(username)) {
      throw new AuthenticationException("Username already exists");
    }
    System.out.println(username);
    System.out.println(password);
    int hashedPassword = hashPassword(password);
    userCredentials.put(username, hashedPassword);
    System.out.println(userCredentials);
  }

  /**
   * Authenticates a user.
   *
   * @param username The username of the user to authenticate.
   * @param password The password of the user to authenticate.
   * @throws AuthenticationException If the authentication fails.
   */
  public void authenticateUser(String username, String password) throws AuthenticationException {
    System.out.println(username);
    System.out.println(userCredentials);
    if (userCredentials.containsKey(username)) {
      int storedHashedPassword = userCredentials.get(username);
      int enteredHashedPassword = hashPassword(password);

      if (storedHashedPassword != enteredHashedPassword) {
        throw new AuthenticationException("Invalid username or password");
      }
    } else {
      throw new AuthenticationException("User not found");
    }
  }

  /**
   * Hashes a password using its hash code.
   *
   * @param password The password to hash.
   * @return The hash code of the password.
   */
  public int hashPassword(String password) {
    return password.hashCode();
  }

  /**
   * Custom exception for authentication issues.
   */
  public static class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
      super(message);
    }
  }
}
