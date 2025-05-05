import { useState, useEffect } from "react";
import Editor from "@monaco-editor/react";
import styles from "./MonacoEditor.module.scss";
import {
  EditorFontSize,
  EditorTheme,
  EditorLanguage,
} from "../../types/shared.types";

type CodeEditorProps = {
  initialCode?: string;
  editorLanguage?: EditorLanguage;
  editorTheme?: EditorTheme;
  editorFontSize?: EditorFontSize;
  onCodeChange?: (code: string) => void;
};

const MonacoEditor = ({
  initialCode = "// Начните писать код\n",
  editorLanguage = "javascript",
  editorTheme = "vs-dark",
  editorFontSize = 14,
  onCodeChange,
}: CodeEditorProps) => {
  const [localCode, setLocalCode] = useState(initialCode);

  useEffect(() => {
    setLocalCode(initialCode);
  }, [initialCode]);

  const handleEditorChange = (value?: string) => {
    const newCode = value || "";
    setLocalCode(newCode);
    onCodeChange?.(newCode);
  };

  return (
    <div className={styles.editorContainer}>
      <Editor
        height="80vh"
        width="90vw"
        language={editorLanguage}
        theme={editorTheme}
        value={localCode}
        onChange={handleEditorChange}
        options={{
          minimap: { enabled: false },
          fontSize: editorFontSize,
          lineNumbers: "on",
          roundedSelection: false,
          scrollBeyondLastLine: false,
        }}
      />
    </div>
  );
};

export default MonacoEditor;
