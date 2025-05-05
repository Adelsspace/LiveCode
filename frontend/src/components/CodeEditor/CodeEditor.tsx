import { useState } from "react";
import {
  changeEditorFontSize,
  changeEditorLanguage,
  changeEditorTheme,
  getSettings,
} from "../../utils";
import {
  EditorFontSize,
  EditorLanguage,
  EditorTheme,
} from "../../types/shared.types";
import { EditorControls } from "../EditorControls/EditorControls";
import MonacoEditor from "../MonacoEditor/MonacoEditor";

interface CodeEditorProps {
  isAdmin: boolean;
  initialCode?: string;
  onCodeChange?: (code: string) => void;
}

export const CodeEditor = ({
  isAdmin,
  initialCode = "",
  onCodeChange,
}: CodeEditorProps) => {
  const initialSettings = getSettings();
  const [editorLanguage, setEditorLanguage] = useState(
    initialSettings.editorLanguage
  );
  const [editorTheme, setEditorTheme] = useState(initialSettings.editorTheme);
  const [editorFontSize, setEditorFontSize] = useState(
    initialSettings.editorFontSize
  );
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
      <EditorControls
        editorLanguage={editorLanguage}
        editorTheme={editorTheme}
        editorFontSize={editorFontSize}
        onLanguageChange={handleLanguageChange}
        onThemeChange={handleThemeChange}
        onFontSizeChange={handleFontSizeChange}
        isAdmin={isAdmin}
      />
      <MonacoEditor
        initialCode={initialCode}
        editorLanguage={editorLanguage}
        editorTheme={editorTheme}
        editorFontSize={editorFontSize}
        onCodeChange={onCodeChange}
      />
    </div>
  );
};
