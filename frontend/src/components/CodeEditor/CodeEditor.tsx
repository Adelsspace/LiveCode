import { useRef, useState } from "react";
import { changeEditorFontSize, getSettings } from "../../utils";
import { EditorFontSize } from "../../types/shared.types";
import { EditorControls } from "../EditorControls/EditorControls";
import MonacoEditor from "../MonacoEditor/MonacoEditor";
import { Logo } from "../Logo/Logo";
import { UsersList } from "../UsersList/UsersList";
import styles from "./CodeEditor.module.scss";
import { useAppSelector } from "../../hooks/reduxHooks";
import { Chat } from "../Chat/Chat";
import AddUserButton from "../AddUserButton/AddUserButton";
import ThemeToggle from "../ThemeToggle/ThemeToggle";

interface CodeEditorProps {
  isAdmin: boolean;
}

export const CodeEditor = ({ isAdmin }: CodeEditorProps) => {
  const initialSettings = getSettings();
  const [editorFontSize, setEditorFontSize] = useState(
    initialSettings.editorFontSize
  );
  const { text, language } = useAppSelector((state) => state.room.editorState);

  const [editorWidth, setEditorWidth] = useState(67);
  const containerRef = useRef<HTMLDivElement>(null);
  const [isChatVisible, setIsChatVisible] = useState(true);

  const handleFontSizeChange = (newFontSize: EditorFontSize) => {
    setEditorFontSize(newFontSize);
    changeEditorFontSize(newFontSize);
  };

  const handleMouseMove = (event: MouseEvent) => {
    if (containerRef.current) {
      const containerWidth = containerRef.current.clientWidth;
      const newWidth =
        ((event.clientX - containerRef.current.getBoundingClientRect().left) /
          containerWidth) *
        100;

      if (newWidth < 29) {
        setEditorWidth(29);
      } else if (newWidth > 71) {
        setEditorWidth(71);
      } else {
        setEditorWidth(newWidth);
      }
    }
  };

  const handleMouseUp = () => {
    document.removeEventListener("mousemove", handleMouseMove);
    document.removeEventListener("mouseup", handleMouseUp);
  };

  const handleMouseDown = () => {
    document.addEventListener("mousemove", handleMouseMove);
    document.addEventListener("mouseup", handleMouseUp);
  };

  return (
    <div className={styles.appContainer}>
      <header className={styles.header_container}>
        <div className={styles.header}>
          <Logo />
          <UsersList />
          {isAdmin && <AddUserButton />}
        </div>
        <ThemeToggle />
      </header>

      <main className={styles.mainContent}>
        <div
          className={styles.container}
          ref={containerRef}
          style={{
            gridTemplateColumns:
              isAdmin && isChatVisible
                ? `${editorWidth}% 5px ${100 - editorWidth}%`
                : "100%",
          }}
        >
          <div className={styles.editor}>
            <MonacoEditor
              initialCode={text}
              editorLanguage={language}
              editorFontSize={editorFontSize}
            />
          </div>

          {isAdmin && isChatVisible && (
            <>
              <div className={styles.separator} onMouseDown={handleMouseDown}>
                <div className={styles.dots}>
                  <div className={styles.dot}></div>
                  <div className={styles.dot}></div>
                  <div className={styles.dot}></div>
                </div>
              </div>
              <div className={styles.chat}>
                <Chat />
              </div>
            </>
          )}
        </div>
      </main>
      <footer className={styles.footer}>
        <EditorControls
          editorFontSize={editorFontSize}
          onFontSizeChange={handleFontSizeChange}
          isAdmin={isAdmin}
          isChatVisible={isChatVisible}
          toggleChatVisibility={() => setIsChatVisible((prev) => !prev)}
        />
      </footer>
    </div>
  );
};
