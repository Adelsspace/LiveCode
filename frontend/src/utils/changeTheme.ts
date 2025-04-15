import {
  Settings,
  saveSettings,
  getSettings,
  Theme,
} from "./localStorageUtils";

export function changeTheme(newTheme: Theme): void {
  const currentSettings = getSettings();
  const updatedSettings: Settings = { ...currentSettings, theme: newTheme };
  saveSettings(updatedSettings);
}
