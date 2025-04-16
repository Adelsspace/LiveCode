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
