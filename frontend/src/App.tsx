import "./App.css";

import { Outlet } from "react-router-dom";
import { initializeSettings } from "./utils";
import "./styles/global.scss";
import axios from "axios";

export default function App() {
  axios.defaults.baseURL = "http://localhost:8080";
  initializeSettings();
  return (
    <div className="app">
      <main>
        <Outlet />
      </main>
    </div>
  );
}
