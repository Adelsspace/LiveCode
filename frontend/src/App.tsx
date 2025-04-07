import "./App.css";

import { Outlet } from "react-router-dom";
import { initializeSettings } from "./utils";

export default function App() {
  initializeSettings();
  return (
    <div className="app">
      <header>Blokshnote общий хедер</header>

      <main>
        <Outlet />
      </main>

      <footer>общий футер</footer>
    </div>
  );
}
