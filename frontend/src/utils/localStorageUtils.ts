import { EditorSettings } from "../types/shared.types";

export type Theme = "light" | "dark";

export type Settings = {
  theme: Theme;
} & EditorSettings;

const defaultSettings: Settings = {
  theme: "dark",
  editorLanguage: "javascript",
  editorTheme: "vs-dark",
  editorFontSize: 14,
};

export function saveSettings(settings: Settings): void {
  localStorage.setItem("userSettings", JSON.stringify(settings));
}

export function getSettings(): Settings {
  const settings = localStorage.getItem("userSettings");
  return settings ? JSON.parse(settings) : defaultSettings;
}
