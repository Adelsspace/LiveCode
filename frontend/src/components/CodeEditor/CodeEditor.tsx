import { useState } from "react";
import Editor from "@monaco-editor/react";
import styles from "./CodeEditor.module.scss";
import { EditorTheme, Language } from "../../types/shared.types";

type CodeEditorProps = {
  initialCode?: string;
  language?: Language;
  theme?: EditorTheme;
  onChange?: (code: string) => void;
};

const CodeEditor = ({
  initialCode = "// Начните писать код\n",
  language = "javascript",
  theme = "vs-dark",
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
        height="60vh"
        width="90vw"
        language={language}
        theme={theme}
        value={code}
        onChange={handleCodeChange}
        options={{
          minimap: { enabled: false },
          fontSize: 20,
          lineNumbers: "on",
          roundedSelection: false,
          scrollBeyondLastLine: false,
        }}
      />
    </div>
  );
};

export default CodeEditor;
