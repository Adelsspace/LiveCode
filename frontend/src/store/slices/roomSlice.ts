import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import {
  CursorPosition,
  EditorState,
  LanguageChange,
  TextSelection,
  TextUpdate,
  User,
  UserActivity,
} from "../../types/shared.types";
import { compareUserLists } from "../../utils";

interface RoomState {
  roomId: string | null;
  name: string;
  adminToken: string | null;
  users: User[];
  content: string;
  status: "idle" | "loading" | "connected" | "error";
  version: number;
  editorState: EditorState | null;
  textSelections: Record<string, TextSelection>;
  cursorPositions: Record<string, CursorPosition>;
  userActivity: UserActivity | null;
  languageChange: LanguageChange | null;
  textUpdate: TextUpdate | null;
  commentsUpdated: boolean;
  llmIsAvailable: boolean;
}

const getInitialState = (): RoomState => {
  const participantData = sessionStorage.getItem("participant");
  if (participantData) {
    try {
      const { roomId, username, adminToken } = JSON.parse(participantData);
      return {
        roomId: roomId || null,
        name: username || "",
        adminToken: adminToken || null,
        users: [],
        content: "",
        status: "idle",
        version: 0,
        editorState: { text: "", language: "javascript" },
        textSelections: {},
        cursorPositions: {},
        userActivity: null,
        languageChange: null,
        textUpdate: null,
        commentsUpdated: false,
        llmIsAvailable: true,
      };
    } catch (error) {
      console.error("Ошибка при парсинге participantData:", error);
    }
  }
  return {
    roomId: null,
    name: "",
    adminToken: null,
    users: [],
    content: "",
    status: "idle",
    version: 0,
    editorState: { text: "", language: "javascript" },
    textSelections: {},
    cursorPositions: {},
    userActivity: null,
    languageChange: null,
    textUpdate: null,
    commentsUpdated: false,
    llmIsAvailable: true,
  };
};

const initialState: RoomState = getInitialState();

const roomSlice = createSlice({
  name: "room",
  initialState,
  reducers: {
    setRoomId: (state, action: PayloadAction<string>) => {
      state.roomId = action.payload;
    },
    setName: (state, action: PayloadAction<string>) => {
      state.name = action.payload;
    },
    setAdminToken: (state, action: PayloadAction<string>) => {
      state.adminToken = action.payload;
    },
    setUsers: (state, action: PayloadAction<User[]>) => {
      const newUsers = action.payload;

      if (compareUserLists(state.users, newUsers)) return;

      const currentUsernames = newUsers.map((u) => u.username);

      Object.keys(state.textSelections).forEach((username) => {
        if (!currentUsernames.includes(username)) {
          delete state.textSelections[username];
        }
      });

      Object.keys(state.cursorPositions).forEach((username) => {
        if (!currentUsernames.includes(username)) {
          delete state.cursorPositions[username];
        }
      });

      state.users = newUsers;
    },
    setContent: (state, action: PayloadAction<string>) => {
      state.content = action.payload;
    },
    setStatus: (state, action: PayloadAction<RoomState["status"]>) => {
      state.status = action.payload;
    },
    setEditorState: (state, action: PayloadAction<EditorState>) => {
      state.editorState = action.payload;
    },
    setTextSelection: (state, action: PayloadAction<TextSelection>) => {
      const { username, selection } = action.payload;
      const isZeroSelection =
        selection.startLineNumber === 0 && selection.endLineNumber === 0;

      if (isZeroSelection) {
        delete state.textSelections[username];
      } else {
        state.textSelections[username] = action.payload;
      }
    },
    setCursorPosition: (state, action: PayloadAction<CursorPosition>) => {
      state.cursorPositions[action.payload.username] = action.payload;
    },
    clearUserDecorations: (state, action: PayloadAction<string>) => {
      const username = action.payload;
      delete state.textSelections[username];
      delete state.cursorPositions[username];
    },
    setUserActivity: (state, action: PayloadAction<UserActivity>) => {
      state.userActivity = action.payload;
    },
    setLanguageChange: (state, action: PayloadAction<LanguageChange>) => {
      state.languageChange = action.payload;
    },
    setVersionChange: (state, action: PayloadAction<number>) => {
      state.version = action.payload;
    },
    setTextUpdate: (state, action: PayloadAction<TextUpdate>) => {
      state.textUpdate = action.payload;
    },
    setUserActivityByUsername: (
      state,
      action: PayloadAction<{ username: string; isActive: boolean }>
    ) => {
      const { username, isActive } = action.payload;
      const user = state.users.find((user) => user.username === username);
      if (user) {
        user.isActive = isActive;
      }
    },
    setCommentsUpdated(state, action: PayloadAction<boolean>) {
      state.commentsUpdated = action.payload;
    },
    setLlmIsAvailable(state, action: PayloadAction<boolean>) {
      if (state.llmIsAvailable !== action.payload)
        state.llmIsAvailable = action.payload;
    },
  },
});

export const {
  setRoomId,
  setName,
  setAdminToken,
  setUsers,
  setContent,
  setStatus,
  setEditorState,
  setTextSelection,
  setCursorPosition,
  clearUserDecorations,
  setUserActivity,
  setLanguageChange,
  setTextUpdate,
  setUserActivityByUsername,
  setVersionChange,
  setCommentsUpdated,
  setLlmIsAvailable,
} = roomSlice.actions;

export default roomSlice.reducer;
