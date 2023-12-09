import React, { useState } from "react";

interface LoginProps {
  onLogin: (username: string, password: string) => void;
  onRegister: (username: string, password: string) => void;
}

const Login: React.FC<LoginProps> = ({ onLogin, onRegister }) => {
  const [loginUsername, setLoginUsername] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [registerUsername, setRegisterUsername] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [generalMessage, setGeneralMessage] = useState<string | null>(null);

  const handleLogin = async () => {
    // Validate loginUsername and loginPassword if needed
    try {
      await onLogin(loginUsername, loginPassword);
      // Clear any previous login error messages
      setGeneralMessage(null);
    } catch (error) {
      // Handle login error
      console.log("HERE!");
      setGeneralMessage("Login failed. Please try again.");
    }

    // Clear the text boxes after handling login
    setLoginUsername("");
    setLoginPassword("");
  };

  const handleRegister = async () => {
    // Validate registerUsername and registerPassword if needed
    try {
      await onRegister(registerUsername, registerPassword);
      setGeneralMessage(null);

      // Display success message
      setGeneralMessage("Registration successful!");
    } catch (error) {
      // Handle registration error
      setGeneralMessage(`Registration Unsuccessful.`);
    }

    // Clear the text boxes after handling registration
    setRegisterUsername("");
    setRegisterPassword("");
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      <form id="loginForm">
        <label htmlFor="loginUsername">Username:</label>
        <input
          type="text"
          id="loginUsername"
          name="loginUsername"
          value={loginUsername}
          onChange={(e) => setLoginUsername(e.target.value)}
          required
        />

        <label htmlFor="loginPassword">Password:</label>
        <input
          type="password"
          id="loginPassword"
          name="loginPassword"
          value={loginPassword}
          onChange={(e) => setLoginPassword(e.target.value)}
          required
        />

        <button type="button" onClick={handleLogin} id="loginButton">
          Login
        </button>
      </form>

      <h2>Register</h2>
      <form id="registerForm">
        <label htmlFor="registerUsername">Username:</label>
        <input
          type="text"
          id="registerUsername"
          name="registerUsername"
          value={registerUsername}
          onChange={(e) => setRegisterUsername(e.target.value)}
          required
        />

        <label htmlFor="registerPassword">Password:</label>
        <input
          type="password"
          id="registerPassword"
          name="registerPassword"
          value={registerPassword}
          onChange={(e) => setRegisterPassword(e.target.value)}
          required
        />

        <button type="button" onClick={handleRegister} id="registerButton">
          Register
        </button>
        <p>
          {generalMessage && (
            <div className="custom-message">{generalMessage}</div>
          )}
        </p>
      </form>
    </div>
  );
};

export default Login;