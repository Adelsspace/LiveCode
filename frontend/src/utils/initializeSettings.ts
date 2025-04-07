import { saveSettings, getSettings, Theme } from "./localStorageUtils";

function detectDefaultTheme(): Theme {
  if (
    window.matchMedia &&
    window.matchMedia("(prefers-color-scheme: dark)").matches
  ) {
    return "dark";
  }
  return "light";
}

export function initializeSettings(): void {
  const currentSettings = getSettings();
  if (!currentSettings.theme) {
    const detectedTheme = detectDefaultTheme();
    const updatedSettings = { ...currentSettings, theme: detectedTheme };
    saveSettings(updatedSettings);
  }
}
