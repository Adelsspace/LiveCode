import "./App.css";

import { Outlet } from "react-router-dom";
import { initializeSettings } from "./utils";
import "./styles/global.scss";

export default function App() {
  initializeSettings();
  return (
    <div className="app">
      <main>
        <Outlet />
      </main>
    </div>
  );
}
