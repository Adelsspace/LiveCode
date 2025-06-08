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

interface RoomState {
  roomId: string | null;
  name: string;
  adminToken: string | null;
  users: User[];
  content: string;
  status: "idle" | "loading" | "connected" | "error";
  version: number;
  editorState: EditorState | null;
  textSelection: TextSelection | null;
  cursorPosition: CursorPosition | null;
  userActivity: UserActivity | null;
  languageChange: LanguageChange | null;
  textUpdate: TextUpdate | null;
  commentsUpdated: boolean;
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
        textSelection: null,
        cursorPosition: null,
        userActivity: null,
        languageChange: null,
        textUpdate: null,
        commentsUpdated: false,
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
    textSelection: null,
    cursorPosition: null,
    userActivity: null,
    languageChange: null,
    textUpdate: null,
    commentsUpdated: false,
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
      state.users = action.payload;
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
      state.textSelection = action.payload;
    },
    setCursorPosition: (state, action: PayloadAction<CursorPosition>) => {
      state.cursorPosition = action.payload;
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
  setUserActivity,
  setLanguageChange,
  setTextUpdate,
  setUserActivityByUsername,
  setVersionChange,
  setCommentsUpdated,
} = roomSlice.actions;

export default roomSlice.reducer;
