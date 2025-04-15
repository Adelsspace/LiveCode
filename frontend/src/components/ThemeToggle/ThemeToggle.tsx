import { useEffect, useState } from "react";
import styles from "./ThemeToggle.module.scss";
import { changeTheme, getSettings } from "../../utils";

const ThemeToggle = () => {
  const [theme, setTheme] = useState<"light" | "dark">(() => {
    const currentSettings = getSettings();
    return currentSettings.theme;
  });

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    changeTheme(theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prevTheme) => {
      const newTheme = prevTheme === "light" ? "dark" : "light";
      return newTheme;
    });
  };

  return (
    <button
      className={styles.toggle}
      onClick={toggleTheme}
      aria-label={`Switch to ${theme === "light" ? "dark" : "light"} theme`}
    >
      {theme === "light" ? "🌙" : "☀️"}
    </button>
  );
};

export default ThemeToggle;
