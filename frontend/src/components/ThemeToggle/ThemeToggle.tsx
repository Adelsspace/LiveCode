import { useEffect, useState } from "react";
import styles from "./ThemeToggle.module.scss";
import { changeEditorTheme, changeTheme, getSettings } from "../../utils";
import { useAppDispatch } from "../../hooks/reduxHooks";
import { setEditorTheme } from "../../store/slices/roomSlice";

const ThemeToggle = () => {
  const dispatch = useAppDispatch();
  const [theme, setTheme] = useState<"light" | "dark">(() => {
    const currentSettings = getSettings();
    return currentSettings.theme;
  });

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    changeTheme(theme);

    const editorTheme = theme === "light" ? "light" : "vs-dark";
    changeEditorTheme(editorTheme);

    dispatch(setEditorTheme(editorTheme));
  }, [theme, dispatch]);

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
