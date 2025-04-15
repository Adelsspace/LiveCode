import { EditorTheme } from "../types/shared.types";
import { Settings, saveSettings, getSettings } from "./localStorageUtils";

export function changeEditorTheme(newEditorTheme: EditorTheme): void {
  const currentSettings = getSettings();
  const updatedSettings: Settings = {
    ...currentSettings,
    editorTheme: newEditorTheme,
  };
  saveSettings(updatedSettings);
}
