// components/App.tsx
import React from "react";
import MapBox from "./MapBox";
import "../style/App.css";

const App: React.FC = () => {
  return (
    <div className="App">
      <MapBox />
    </div>
  );
};

export default App;

