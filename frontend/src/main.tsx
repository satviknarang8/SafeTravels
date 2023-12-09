import React, { useState } from "react";
import ReactDOM from "react-dom/client";
import App from "./components/App";
import Login from "./components/Login";
import "./style/index.css";

const Root: React.FC = () => {
  const [isLoggedIn, setLoggedIn] = useState(false);

  const handleLogin = async (username: string, password: string) => {
    try {
      const response = await fetch(
        `http://localhost:3233/login?username=${username}&password=${password}`
      );

      const data = await response.json();

      if (data.type === "success") {
        // If login is successful, update the login state
        setLoggedIn(true);
      } else {
        // Handle login failure
        throw new Error(data.error);
      }
    } catch (error) {
      throw new Error("Unknown error during login.");
    }
  };

  const handleRegister = async (username: string, password: string) => {
    try {
      const response = await fetch(
        `http://localhost:3233/register?username=${username}&password=${password}`
      );
      const data = await response.json();
      if (data.type === "fail") {
        throw new Error("Username already in use.");
      }
    } catch (error) {
      throw new Error("Error during registration.");
    }
  };

  return (
    <React.StrictMode>
      {/* Conditionally render either the Login or App component based on the login state */}
      {isLoggedIn ? (
        <App />
      ) : (
        <Login
          onLogin={(username: string, password: string) =>
            handleLogin(username, password)
          }
          onRegister={(username: string, password: string) =>
            handleRegister(username, password)
          }
        />
      )}
    </React.StrictMode>
  );
};

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <Root />
);
