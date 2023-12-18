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

  const containerStyle: React.CSSProperties = {
    fontFamily: "Arial, sans-serif",
    maxWidth: "400px",
    margin: "auto",
    padding: "20px",
    borderRadius: "8px",
    boxShadow: "0 0 10px rgba(0, 0, 0, 0.1)",
    backgroundColor: "#f0f8ff",
  };

  const formStyle: React.CSSProperties = {
    display: "flex",
    flexDirection: "column",
  };

  const inputStyle: React.CSSProperties = {
    margin: "8px 0",
    padding: "12px",
    borderRadius: "6px",
    border: "1px solid #4682b4",
    fontSize: "16px",
    outline: "none",
  };

  const buttonStyle: React.CSSProperties = {
    backgroundColor: "#4169e1",
    color: "#fff",
    padding: "12px",
    borderRadius: "6px",
    border: "none",
    cursor: "pointer",
    fontSize: "16px",
  };

  const messageStyle: React.CSSProperties = {
    marginTop: "12px",
    textAlign: "center",
    color: "#2e8b57",
    fontSize: "14px",
  };

  const handleLogin = async () => {
    try {
      await onLogin(loginUsername, loginPassword);
      setGeneralMessage(null);
    } catch (error) {
      console.log("HERE!");
      setGeneralMessage("Login failed. Please try again.");
    }
    setLoginUsername("");
    setLoginPassword("");
  };

  const handleRegister = async () => {
    try {
      await onRegister(registerUsername, registerPassword);
      setGeneralMessage("Registration successful!");
    } catch (error) {
      setGeneralMessage(`Registration unsuccessful, username already in use.`);
    }

    setRegisterUsername("");
    setRegisterPassword("");
  };

  return (
    <div style={containerStyle}>
      <h1 style={{ textAlign: "center", color: "#2e8b57" }}>
        Welcome to SafeTravels!
      </h1>
      <h2 style={{ color: "#4169e1" }}>Login</h2>
      <form id="loginForm" style={formStyle}>
        <label htmlFor="loginUsername">Username:</label>
        <input
          type="text"
          id="loginUsername"
          name="loginUsername"
          value={loginUsername}
          onChange={(e) => setLoginUsername(e.target.value)}
          style={inputStyle}
          required
        />

        <label htmlFor="loginPassword">Password:</label>
        <input
          type="password"
          id="loginPassword"
          name="loginPassword"
          value={loginPassword}
          onChange={(e) => setLoginPassword(e.target.value)}
          style={inputStyle}
          required
        />

        <button
          type="button"
          onClick={handleLogin}
          style={buttonStyle}
          id="loginButton"
        >
          Login
        </button>
      </form>

      <h2 style={{ color: "#4169e1" }}>Register</h2>
      <form id="registerForm" style={formStyle}>
        <label htmlFor="registerUsername">Username:</label>
        <input
          type="text"
          id="registerUsername"
          name="registerUsername"
          value={registerUsername}
          onChange={(e) => setRegisterUsername(e.target.value)}
          style={inputStyle}
          required
        />

        <label htmlFor="registerPassword">Password:</label>
        <input
          type="password"
          id="registerPassword"
          name="registerPassword"
          value={registerPassword}
          onChange={(e) => setRegisterPassword(e.target.value)}
          style={inputStyle}
          required
        />

        <button
          type="button"
          onClick={handleRegister}
          style={buttonStyle}
          id="registerButton"
        >
          Register
        </button>
        <p style={messageStyle}>
          {generalMessage && (
            <div className="custom-message">{generalMessage}</div>
          )}
        </p>
      </form>
    </div>
  );
};

export default Login;
