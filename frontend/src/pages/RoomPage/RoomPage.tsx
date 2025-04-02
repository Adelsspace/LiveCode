import { useParams } from "react-router-dom";
import { Dropdown } from "../../components/index";
import { useState } from "react";
import { EditorTheme, Language } from "../../types/shared.types";
import CodeEditor from "../../components/CodeEditor/CodeEditor";
import styles from "./RoomPage.module.scss";

export default function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const [language, setLanguage] = useState<Language>("javascript");
  const [editorTheme, setEditorTheme] = useState<EditorTheme>("vs-dark");

  const languages: Language[] = ["javascript", "typescript", "python", "java"];
  const editorThemes: EditorTheme[] = ["vs-dark", "light", "hc-black"];

  return (
    <div>
      <div className={styles.dropdownsContainer}>
        <Dropdown
          options={languages}
          defaultValue="javascript"
          onSelect={setLanguage}
        />
        <Dropdown
          options={editorThemes}
          defaultValue="vs-dark"
          onSelect={setEditorTheme}
        />
      </div>
      <h2>Комната: {roomId}</h2>
      <CodeEditor theme={editorTheme} language={language} />
    </div>
  );
}
