import { useParams } from "react-router-dom";
import { Dropdown } from "../../components/index";
import { useState } from "react";
import {
  EditorFontSize,
  EditorTheme,
  EditorLanguage,
} from "../../types/shared.types";
import CodeEditor from "../../components/CodeEditor/CodeEditor";
import styles from "./RoomPage.module.scss";
import {
  changeEditorFontSize,
  changeEditorLanguage,
  changeEditorTheme,
  getSettings,
} from "../../utils";
import ThemeToggle from "../../components/ThemeToggle/ThemeToggle";

export default function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const initialSettings = getSettings();
  const [editorLanguage, setEditorLanguage] = useState<EditorLanguage>(
    initialSettings.editorLanguage
  );
  const [editorTheme, setEditorTheme] = useState<EditorTheme>(
    initialSettings.editorTheme
  );
  const [editorFontSize, setEditorFontSize] = useState<EditorFontSize>(
    initialSettings.editorFontSize
  );

  const editorLanguages: EditorLanguage[] = [
    "javascript",
    "typescript",
    "python",
    "java",
  ];
  const editorThemes: EditorTheme[] = ["vs-dark", "light", "hc-black"];
  const editorFontSizes: EditorFontSize[] = [12, 14, 16, 18, 20, 22, 24];

  const handleLanguageChange = (newLanguage: EditorLanguage) => {
    setEditorLanguage(newLanguage);
    changeEditorLanguage(newLanguage);
  };

  const handleThemeChange = (newTheme: EditorTheme) => {
    setEditorTheme(newTheme);
    changeEditorTheme(newTheme);
  };

  const handleFontSizeChange = (newFontSize: EditorFontSize) => {
    setEditorFontSize(newFontSize);
    changeEditorFontSize(newFontSize);
  };

  return (
    <div>
      <div className={styles.dropdownsContainer}>
        <div>Blokshnote</div>
        <ThemeToggle />
        <Dropdown
          options={editorLanguages}
          defaultValue={editorLanguage}
          onSelect={handleLanguageChange}
        />
        <Dropdown
          options={editorThemes}
          defaultValue={editorTheme}
          onSelect={handleThemeChange}
        />
        <Dropdown
          options={editorFontSizes}
          defaultValue={editorFontSize}
          onSelect={handleFontSizeChange}
        />
      </div>
      <h2>Комната: {roomId}</h2>
      <CodeEditor
        editorTheme={editorTheme}
        editorLanguage={editorLanguage}
        editorFontSize={editorFontSize}
      />
    </div>
  );
}
