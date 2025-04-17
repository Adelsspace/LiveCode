import { Dropdown, Logo } from "../../components/index";
import {
  EditorFontSize,
  EditorTheme,
  EditorLanguage,
} from "../../types/shared.types";
import ThemeToggle from "../../components/ThemeToggle/ThemeToggle";
import styles from "./EditorControls.module.scss";

interface EditorControlsProps {
  editorLanguage: EditorLanguage;
  editorTheme: EditorTheme;
  editorFontSize: EditorFontSize;
  onLanguageChange: (lang: EditorLanguage) => void;
  onThemeChange: (theme: EditorTheme) => void;
  onFontSizeChange: (size: EditorFontSize) => void;
  isAdmin?: boolean;
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
  editorLanguage,
  editorTheme,
  editorFontSize,
  onLanguageChange,
  onThemeChange,
  onFontSizeChange,
  isAdmin,
}: EditorControlsProps) => (
  <div className={styles.controls}>
    <Logo />
    <Dropdown
      options={editorLanguages}
      defaultValue={editorLanguage}
      onSelect={onLanguageChange}
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
    {isAdmin && <div className={styles.adminBadge}>Режим администратора</div>}
  </div>
);
