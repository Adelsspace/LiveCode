import { useState, useEffect, useRef } from "react";
import Editor from "@monaco-editor/react";
import styles from "./MonacoEditor.module.scss";
import {
  EditorFontSize,
  EditorLanguage,
  EditorTheme,
} from "../../types/shared.types";

import * as monaco from "monaco-editor";
import { socketService } from "../../services/socketService";
import { useAppDispatch, useAppSelector } from "../../hooks/reduxHooks";
import { setVersionChange } from "../../store/slices/roomSlice";

type CodeEditorProps = {
  initialCode?: string;
  editorLanguage?: EditorLanguage;
  editorTheme?: EditorTheme;
  editorFontSize?: EditorFontSize;
};

const MonacoEditor = ({
  initialCode = "// Начните писать код\n",
  editorLanguage = "javascript",
  editorTheme = "vs-dark",
  editorFontSize = 14,
}: CodeEditorProps) => {
  const [localCode, setLocalCode] = useState(initialCode);
  const dispatch = useAppDispatch();

  useEffect(() => {
    setLocalCode(initialCode);
  }, [initialCode]);

  const stateLanguage = useAppSelector(
    (state) => state.room.languageChange?.language
  );

  const [editorModel, setEditorModel] =
    useState<monaco.editor.ITextModel | null>(null);
  const isRemoteRef = useRef(false);
  const isFirstMountRef = useRef(true);
  const username = useAppSelector((state) => state.room.name);
  const textUpdate = useAppSelector((state) => state.room.textUpdate);

  useEffect(() => {
    if (textUpdate && editorModel) {
      isRemoteRef.current = true;
      // @ts-expect-error todo
      const edits = textUpdate.changes.map((change) => ({
        range: new monaco.Range(
          change.range.startLineNumber,
          change.range.startColumn,
          change.range.endLineNumber,
          change.range.endColumn
        ),
        text: change.text,
        forceMoveMarkers: change.forceMoveMarkers,
      }));
      editorModel.applyEdits(edits);
      setLocalCode(editorModel.getValue());
      isRemoteRef.current = false;
    }
  }, [textUpdate, editorModel]);

  const handleEditorMount = (editor: monaco.editor.IStandaloneCodeEditor) => {
    const model = editor.getModel();
    if (model) {
      setEditorModel(model);
      model.onDidChangeContent((event) => {
        if (isRemoteRef.current || !username) return;

        const changes = event.changes.map((change) => ({
          range: {
            startLineNumber: change.range.startLineNumber,
            startColumn: change.range.startColumn,
            endLineNumber: change.range.endLineNumber,
            endColumn: change.range.endColumn,
          },
          text: change.text,
          // @ts-expect-error todo
          forceMoveMarkers: change.forceMoveMarkers,
          version: model.getVersionId(),
        }));
        if (isFirstMountRef.current) {
          isFirstMountRef.current = false;
          return;
        }
        dispatch(setVersionChange(changes[0].version));
        socketService.sendTextUpdate(changes, username);
      });
    }
  };
  const handleEditorChange = (value?: string) => {
    if (!isRemoteRef.current) {
      const newCode = value || "";
      setLocalCode(newCode);
      socketService.sendNewEditorState(newCode, editorLanguage);
    }
  };
  return (
    <div className={styles.editorContainer}>
      <Editor
        height="75vh"
        width="90vw"
        language={stateLanguage || editorLanguage}
        theme={editorTheme}
        value={localCode}
        onChange={handleEditorChange}
        onMount={handleEditorMount}
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
