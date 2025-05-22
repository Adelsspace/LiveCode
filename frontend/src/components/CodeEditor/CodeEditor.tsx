import { useState } from "react";
import {
  changeEditorFontSize,
  changeEditorTheme,
  getSettings,
} from "../../utils";
import { EditorFontSize, EditorTheme } from "../../types/shared.types";
import { EditorControls } from "../EditorControls/EditorControls";
import MonacoEditor from "../MonacoEditor/MonacoEditor";
import { Logo } from "../Logo/Logo";
import { UsersList } from "../UsersList/UsersList";
import styles from "./CodeEditor.module.scss";
import { useAppSelector } from "../../hooks/reduxHooks";

interface CodeEditorProps {
  isAdmin: boolean;
}

export const CodeEditor = ({ isAdmin }: CodeEditorProps) => {
  const initialSettings = getSettings();
  const [editorTheme, setEditorTheme] = useState(initialSettings.editorTheme);
  const [editorFontSize, setEditorFontSize] = useState(
    initialSettings.editorFontSize
  );
  const { text, language } = useAppSelector((state) => state.room.editorState);

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
      <header className={styles.header}>
        <Logo />
        <UsersList />
      </header>

      <MonacoEditor
        initialCode={text}
        editorLanguage={language}
        editorTheme={editorTheme}
        editorFontSize={editorFontSize}
      />
      <EditorControls
        editorTheme={editorTheme}
        editorFontSize={editorFontSize}
        onThemeChange={handleThemeChange}
        onFontSizeChange={handleFontSizeChange}
        isAdmin={isAdmin}
      />
    </div>
  );
};
