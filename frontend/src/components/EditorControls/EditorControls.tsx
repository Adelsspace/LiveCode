import { Button, Dropdown } from "../../components/index";
import { EditorFontSize, EditorLanguage } from "../../types/shared.types";
import styles from "./EditorControls.module.scss";
import { socketService } from "../../services/socketService";
import { useAppDispatch, useAppSelector } from "../../hooks/reduxHooks";
import { setLanguageChange } from "../../store/slices/roomSlice";

interface EditorControlsProps {
  editorFontSize: EditorFontSize;
  onFontSizeChange: (size: EditorFontSize) => void;
  isAdmin?: boolean;
  isChatVisible: boolean;
  toggleChatVisibility: () => void;
}

const languageOptions = [
  { label: "JavaScript", value: "javascript" },
  { label: "TypeScript", value: "typescript" },
  { label: "Python", value: "python" },
  { label: "Java", value: "java" },
  { label: "Kotlin", value: "kotlin" },
  { label: "Go", value: "go" },
  { label: "C++", value: "cpp" },
  { label: "C#", value: "csharp" },
  { label: "SQL", value: "sql" },
  { label: "Ruby", value: "ruby" },
  { label: "PHP", value: "php" },
  { label: "Plain Text", value: "plaintext" },
  { label: "Markdown", value: "markdown" },
] as const;

const editorFontSizes: EditorFontSize[] = [12, 14, 16, 18, 20, 22, 24];

const fontSizeOptions = editorFontSizes.map((size) => ({
  label: `${size}px`,
  value: size,
}));
export const EditorControls = ({
  editorFontSize,
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
        options={languageOptions}
        defaultValue={editorLanguage || defaultLanguage || "javascript"}
        onSelect={handleLanguageChange}
      />
      <Dropdown
        options={fontSizeOptions}
        defaultValue={editorFontSize}
        onSelect={onFontSizeChange}
      />

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
