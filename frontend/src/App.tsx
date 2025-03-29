import "./App.css";

import { Outlet } from "react-router-dom";

export default function App() {
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
