import { useState } from "react";
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
  onChange?: (code: string) => void;
};

const MonacoEditor = ({
  initialCode = "// Начните писать код\n",
  editorLanguage = "javascript",
  editorTheme = "vs-dark",
  editorFontSize = 14,
  onChange,
}: CodeEditorProps) => {
  const [code, setCode] = useState(initialCode);

  const handleCodeChange = (value: string | undefined) => {
    const newCode = value || "";
    setCode(newCode);
    onChange?.(newCode);
  };

  return (
    <div className={styles.editorContainer}>
      <Editor
        height="80vh"
        width="90vw"
        language={editorLanguage}
        theme={editorTheme}
        value={code}
        onChange={handleCodeChange}
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
