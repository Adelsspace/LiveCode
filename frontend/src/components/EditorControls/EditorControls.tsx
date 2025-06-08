import { Button, Dropdown } from "../../components/index";
import {
  EditorFontSize,
  EditorTheme,
  EditorLanguage,
} from "../../types/shared.types";
import ThemeToggle from "../../components/ThemeToggle/ThemeToggle";
import styles from "./EditorControls.module.scss";
import { socketService } from "../../services/socketService";
import { useAppDispatch, useAppSelector } from "../../hooks/reduxHooks";
import { setLanguageChange } from "../../store/slices/roomSlice";

interface EditorControlsProps {
  editorTheme: EditorTheme;
  editorFontSize: EditorFontSize;
  onThemeChange: (theme: EditorTheme) => void;
  onFontSizeChange: (size: EditorFontSize) => void;
  isAdmin?: boolean;
  isChatVisible: boolean;
  toggleChatVisibility: () => void;
}

const editorLanguages: EditorLanguage[] = [
  "javascript",
  "typescript",
  "python",
  "java",
];

const editorThemes: EditorTheme[] = ["vs-dark", "light", "hc-black"];
const editorFontSizes: EditorFontSize[] = [12, 14, 16, 18, 20, 22, 24];

export const EditorControls = ({
  editorTheme,
  editorFontSize,
  onThemeChange,
  onFontSizeChange,
  isAdmin,
  isChatVisible,
  toggleChatVisibility,
}: EditorControlsProps) => {
  const dispatch = useAppDispatch();

  const defaultLanguage = useAppSelector(
    (state) => state.room.editorState?.language
  );
  const editorLanguage = useAppSelector(
    (state) => state.room.languageChange?.language
  );

  const name = useAppSelector((state) => state.room.name);
  const handleLanguageChange = (newLanguage: EditorLanguage) => {
    socketService.sendLanguageChange(newLanguage, name);
    dispatch(
      setLanguageChange({
        language: newLanguage,
        username: name,
      })
    );
  };

  return (
    <div className={styles.controls}>
      <Dropdown
        options={editorLanguages}
        defaultValue={editorLanguage || defaultLanguage || "javascript"}
        onSelect={handleLanguageChange}
      />
      <Dropdown
        options={editorThemes}
        defaultValue={editorTheme}
        onSelect={onThemeChange}
      />
      <Dropdown
        options={editorFontSizes}
        defaultValue={editorFontSize}
        onSelect={onFontSizeChange}
      />
      <ThemeToggle />

      {isAdmin && (
        <Button
          toggle={isChatVisible}
          label={"Показать комментарии"}
          toggleLabel={"Скрыть комментарии"}
          onClick={toggleChatVisibility}
        />
      )}
    </div>
  );
};
