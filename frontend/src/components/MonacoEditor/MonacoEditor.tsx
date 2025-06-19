import { useEffect, useRef } from "react";
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
import {
  clearUserDecorations,
  setVersionChange,
} from "../../store/slices/roomSlice";
import type {
  TextSelection,
  CursorPosition,
  User,
  TextUpdate,
} from "../../types/shared.types";

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
  const dispatch = useAppDispatch();
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null);
  const decorationsCollectionRef =
    useRef<monaco.editor.IEditorDecorationsCollection | null>(null);
  const cursorLabelsRef = useRef<Record<string, HTMLDivElement>>({});
  const isRemoteRef = useRef(false);
  const removeTimersRef = useRef<Record<string, number>>({});
  const username = useAppSelector((state) => state.room.name);
  const users = useAppSelector((state) => state.room.users);
  const textSelections: Record<string, TextSelection> = useAppSelector(
    (state) => state.room.textSelections
  );
  const cursorPositions: Record<string, CursorPosition> = useAppSelector(
    (state) => state.room.cursorPositions
  );
  const textUpdate: TextUpdate = useAppSelector(
    (state) => state.room.textUpdate
  );
  const editorState = useAppSelector((state) => state.room.editorState);
  const stateLanguage = useAppSelector(
    (state) => state.room.languageChange?.language
  );
  const hasCalledUpdate = useRef(false);
  const hasRun = useRef(false);

  useEffect(() => {
    if (!editorRef.current || !textUpdate) return;

    const model = editorRef.current.getModel();
    if (!model || isRemoteRef.current) return;

    isRemoteRef.current = true;
    const edits = textUpdate.changes.map((change) => ({
      range: new monaco.Range(
        change.range.startLineNumber,
        change.range.startColumn,
        change.range.endLineNumber,
        change.range.endColumn
      ),
      text: change.text,
      forceMoveMarkers: false,
    }));

    model.applyEdits(edits);
    isRemoteRef.current = false;
  }, [textUpdate]);

  useEffect(() => {
    if (!editorRef.current) return;
    const editor = editorRef.current;
    const model = editor.getModel();
    if (!model) return;

    if (!decorationsCollectionRef.current) {
      decorationsCollectionRef.current = editor.createDecorationsCollection([]);
    }

    const newDecorations: monaco.editor.IModelDeltaDecoration[] = [];

    Object.entries(textSelections).forEach(([username, selection]) => {
      const user = users.find((u: User) => u.username === username);
      if (!user) return;

      newDecorations.push({
        range: new monaco.Range(
          selection.selection.startLineNumber,
          selection.selection.startColumn,
          selection.selection.endLineNumber,
          selection.selection.endColumn
        ),
        options: {
          className: `${styles.selection} ${styles[`selection-${user.color}`]}`,
          isWholeLine: false,
        },
      });
    });

    Object.entries(cursorPositions).forEach(([username, position]) => {
      const user = users.find((u: User) => u.username === username);
      if (!user) return;

      newDecorations.push({
        range: new monaco.Range(
          position.position.lineNumber,
          position.position.column,
          position.position.lineNumber,
          position.position.column + 1
        ),
        options: {
          className: `${styles.cursor} ${styles[`cursor-${user.color}`]}`,
          stickiness:
            monaco.editor.TrackedRangeStickiness.NeverGrowsWhenTypingAtEdges,
        },
      });
    });

    decorationsCollectionRef.current.set(newDecorations);

    Object.values(cursorLabelsRef.current).forEach((el) => el.remove());
    cursorLabelsRef.current = {};

    Object.values(removeTimersRef.current).forEach((timerId) =>
      clearTimeout(timerId)
    );
    removeTimersRef.current = {};

    Object.entries(cursorPositions).forEach(([username, position]) => {
      const user = users.find((u: User) => u.username === username);
      if (!user) return;
      const coords = editor.getScrolledVisiblePosition({
        lineNumber: position.position.lineNumber,
        column: position.position.column,
      });
      if (coords) {
        const container = editor.getDomNode();
        if (container) {
          const label = document.createElement("div");
          label.className = `${styles.cursorLabel} ${
            styles[`cursorLabel-${username}`]
          }`;
          label.textContent = username;
          label.style.position = "absolute";
          label.style.top = `${
            coords.top - (position.position.lineNumber <= 2 ? -40 : 0)
          }px`;
          label.style.left = `${coords.left}px`;
          label.style.zIndex = "100";
          label.style.opacity = "1";
          label.style.transition = "opacity 0.5s";
          label.style.backgroundColor = `#${user.color}`;

          container.appendChild(label);
          cursorLabelsRef.current[username] = label;
          removeTimersRef.current[username] = setTimeout(() => {
            label.style.opacity = "0";
            setTimeout(() => {
              label.remove();
              dispatch(clearUserDecorations(username));
              delete cursorLabelsRef.current[username];
              delete removeTimersRef.current[username];
            }, 300);
          }, 1000);
        }
      }
    });

    const updatePositions = () => {
      Object.entries(cursorPositions).forEach(([username, position]) => {
        const label = cursorLabelsRef.current[username];
        if (!label) return;

        const coords = editor.getScrolledVisiblePosition({
          lineNumber: position.position.lineNumber,
          column: position.position.column,
        });

        if (coords) {
          label.style.top = `${
            coords.top - (position.position.lineNumber <= 2 ? -20 : 20)
          }px`;
          label.style.left = `${coords.left}px`;
        }
      });
    };

    const scrollListener = editor.onDidScrollChange(updatePositions);
    const resizeListener = editor.onDidLayoutChange(updatePositions);

    return () => {
      scrollListener.dispose();
      resizeListener.dispose();
    };
  }, [textSelections, cursorPositions, users, dispatch]);

  useEffect(() => {
    if (!editorRef.current || !editorState || hasRun.current) return;

    const model = editorRef.current.getModel();
    if (!model || model.getValue() === editorState.text) return;
    model.setValue(editorState.text);
    hasRun.current = true;
  }, [editorState]);

  const handleEditorMount = (editor: monaco.editor.IStandaloneCodeEditor) => {
    editorRef.current = editor;
    decorationsCollectionRef.current = editor.createDecorationsCollection([]);

    const model = editor.getModel();

    if (model) {
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
          version: model.getVersionId(),
        }));

        dispatch(setVersionChange(changes[0].version));

        if (!hasCalledUpdate.current) {
          hasCalledUpdate.current = true;
        } else {
          socketService.sendTextUpdate({ changes, username });
        }

        const fullText = model.getValue();
        const language = editorLanguage;
        socketService.sendNewEditorState(fullText, language);
      });

      editor.onDidChangeCursorSelection((e) => {
        if (isRemoteRef.current || !username) return;

        const hasSelection = !(
          e.selection.startLineNumber === e.selection.endLineNumber &&
          e.selection.startColumn === e.selection.endColumn
        );

        if (hasSelection) {
          socketService.sendTextSelection({
            selection: {
              startLineNumber: e.selection.startLineNumber,
              startColumn: e.selection.startColumn,
              endLineNumber: e.selection.endLineNumber,
              endColumn: e.selection.endColumn,
            },
            username,
          });
        } else {
          socketService.sendTextSelection({
            selection: {
              startLineNumber: 0,
              startColumn: 0,
              endLineNumber: 0,
              endColumn: 0,
            },
            username,
          });
        }
      });

      editor.onDidChangeCursorPosition((e) => {
        if (isRemoteRef.current || !username) return;

        socketService.sendCursorPosition({
          position: {
            lineNumber: e.position.lineNumber,
            column: e.position.column,
          },
          username,
        });
      });
    }
  };

  useEffect(() => {
    return () => {
      Object.values(removeTimersRef.current).forEach((timerId) =>
        clearTimeout(timerId)
      );
      Object.values(cursorLabelsRef.current).forEach((label) => label.remove());
    };
  }, []);

  return (
    <div className={styles.editorContainer}>
      <Editor
        height="78vh"
        language={stateLanguage || editorLanguage}
        theme={editorTheme}
        defaultValue={initialCode}
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
