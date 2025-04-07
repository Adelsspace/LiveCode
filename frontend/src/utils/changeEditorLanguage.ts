import { EditorLanguage } from "../types/shared.types";
import { Settings, saveSettings, getSettings } from "./localStorageUtils";

export function changeEditorLanguage(newEditroLanguage: EditorLanguage): void {
  const currentSettings = getSettings();
  const updatedSettings: Settings = {
    ...currentSettings,
    editorLanguage: newEditroLanguage,
  };
  saveSettings(updatedSettings);
}
