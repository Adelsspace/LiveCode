import { EditorFontSize } from "../types/shared.types";
import { Settings, saveSettings, getSettings } from "./localStorageUtils";

export function changeEditorFontSize(newFont: EditorFontSize): void {
  const currentSettings = getSettings();
  const updatedSettings: Settings = {
    ...currentSettings,
    editorFontSize: newFont,
  };
  saveSettings(updatedSettings);
}
