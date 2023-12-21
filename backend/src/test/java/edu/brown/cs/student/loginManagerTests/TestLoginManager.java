package edu.brown.cs.student.loginManagerTests;

import edu.brown.cs.student.main.Server.LoginHandler.LoginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestLoginManager {

    @Mock
    private Request mockRequest;
    @Mock
    private Response mockResponse;

    private LoginManager loginManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        loginManager = new LoginManager();
    }

    // Test method for successful login
    @Test
    public void testSuccessfulLogin() throws Exception {
        when(mockRequest.queryParams("username")).thenReturn("test1");
        when(mockRequest.queryParams("password")).thenReturn("test1");
        when(mockRequest.pathInfo()).thenReturn("/login");

        String result = (String) loginManager.handle(mockRequest, mockResponse);
        assertTrue(result.contains("Login successful"));
    }


    // Test method for failed login due to incorrect credentials
    @Test
    public void testFailedLoginIncorrectCredentials() throws Exception {
        when(mockRequest.queryParams("username")).thenReturn("test1");
        when(mockRequest.queryParams("password")).thenReturn("wrongpassword");
        when(mockRequest.pathInfo()).thenReturn("/login");

        String result = (String) loginManager.handle(mockRequest, mockResponse);
        assertTrue(result.contains("Invalid username or password"));
    }


    // Test method for failed login due to non-existent user
    @Test
    public void testFailedLoginNonExistentUser() throws Exception {
        when(mockRequest.queryParams("username")).thenReturn("nonexistent");
        when(mockRequest.queryParams("password")).thenReturn("test1");
        when(mockRequest.pathInfo()).thenReturn("/login");

        String result = (String) loginManager.handle(mockRequest, mockResponse);
        assertTrue(result.contains("User not found"));
    }

    // Test method for successful registration
    @Test
    public void testSuccessfulRegistration() throws Exception {
        when(mockRequest.queryParams("username")).thenReturn("newuser");
        when(mockRequest.queryParams("password")).thenReturn("newpassword");
        when(mockRequest.pathInfo()).thenReturn("/register");

        String result = (String) loginManager.handle(mockRequest, mockResponse);
        assertTrue(result.contains("Registration successful"));
    }

    // Test method for failed registration due to existing user
    @Test
    public void testFailedRegistrationUserExists() throws Exception {
        when(mockRequest.queryParams("username")).thenReturn("test1");
        when(mockRequest.queryParams("password")).thenReturn("test1");
        when(mockRequest.pathInfo()).thenReturn("/register");

        String result = (String) loginManager.handle(mockRequest, mockResponse);
        assertTrue(result.contains("Username already exists"));
    }
}
