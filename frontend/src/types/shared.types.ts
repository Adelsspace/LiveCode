export type EditorLanguage = "javascript" | "typescript" | "python" | "java";
export type EditorTheme = "vs-dark" | "light" | "hc-black";
export type EditorFontSize = 12 | 14 | 16 | 18 | 20 | 22 | 24;

export type EditorSettings = {
  editorLanguage: EditorLanguage;
  editorTheme: EditorTheme;
  editorFontSize: EditorFontSize;
};

export type Participant = {
  roomId: string;
  adminToken?: string;
  username: string;
};

export interface User {
  username: string;
  isActive: boolean;
  isAdmin: boolean;
}

export interface EditorState {
  text: string;
  language: string;
}

export interface TextSelection {
  startLineNumber: number;
  startColumn: number;
  endLineNumber: number;
  endColumn: number;
  username: string;
}

export interface CursorPosition {
  lineNumber: number;
  column: number;
  username: string;
}

export interface UserActivity {
  isActive: boolean;
  username: string;
}

export interface LanguageChange {
  language: string;
  username: string;
}

export interface TextUpdate {
  changes: {
    range: {
      startLineNumber: number;
      startColumn: number;
      endLineNumber: number;
      endColumn: number;
    };
    text: string;
    forceMoveMarkers: boolean;
    version: number;
  }[];
  username: string;
}
