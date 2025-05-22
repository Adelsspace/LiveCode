import io, { Socket } from "socket.io-client";
import type { AppDispatch, RootState } from "../store/store";
import {
  setUsers,
  setEditorState,
  setTextSelection,
  setCursorPosition,
  setLanguageChange,
  setTextUpdate,
  setUserActivityByUsername,
} from "../store/slices/roomSlice";
import { roomApi } from "../store/api/roomApi";
import type { ThunkDispatch, UnknownAction } from "@reduxjs/toolkit";
import { CursorPosition, TextSelection } from "../types/shared.types";

class SocketService {
  private socket: typeof Socket | null = null;
  private dispatch: ThunkDispatch<RootState, unknown, UnknownAction> | null =
    null;

  async init(roomId: string, username: string, dispatch: AppDispatch) {
    try {
      this.dispatch = dispatch;

      const result = await dispatch(
        roomApi.endpoints.getWebSocketUrl.initiate(roomId)
      );

      if ("data" in result) {
        const { data } = result;
        if (!data?.wsConnectUrl) throw new Error("WebSocket URL not found");
        this.socket = io(data.wsConnectUrl, {
          query: {
            roomUuid: roomId,
            user: username,
          },
          transports: ["websocket"],
        });

        this.setupListeners();

        this.socket.on(
          "NEW_EDITOR_STATE",
          (editorState: { text: string; language: string }) => {
            dispatch(setEditorState(editorState));
          }
        );
      }

      if ("error" in result) {
        throw new Error("Failed to get WebSocket URL");
      }
    } catch (error) {
      console.error("Socket connection error:", error);
      throw error;
    }
  }

  private setupListeners() {
    if (!this.socket || !this.dispatch) return;

    const dispatch = this.dispatch;

    this.socket.on(
      "USERS_UPDATE",
      (data: {
        usersStates: {
          username: string;
          isActive: boolean;
          isAdmin: boolean;
          color: string;
        }[];
      }) => {
        dispatch(setUsers(data.usersStates));
      }
    );

    this.socket.on(
      "NEW_EDITOR_STATE",
      (data: { text: string; language: string }) => {
        dispatch(setEditorState(data));
      }
    );

    this.socket.on(
      "TEXT_SELECTION",
      (data: {
        selection: {
          startLineNumber: number;
          startColumn: number;
          endLineNumber: number;
          endColumn: number;
        };
        username: string;
      }) => {
        const textSelection: TextSelection = {
          startLineNumber: data.selection.startLineNumber,
          startColumn: data.selection.startColumn,
          endLineNumber: data.selection.endLineNumber,
          endColumn: data.selection.endColumn,
          username: data.username,
        };
        dispatch(setTextSelection(textSelection));
      }
    );

    this.socket.on(
      "CURSOR_POSITION",
      (data: {
        position: { lineNumber: number; column: number };
        username: string;
      }) => {
        const cursorPosition: CursorPosition = {
          lineNumber: data.position.lineNumber,
          column: data.position.column,
          username: data.username,
        };
        dispatch(setCursorPosition(cursorPosition));
      }
    );

    this.socket.on(
      "USER_ACTIVITY",
      (data: { isActive: boolean; username: string }) => {
        dispatch(setUserActivityByUsername({ ...data }));
      }
    );

    this.socket.on(
      "LANGUAGE_CHANGE",
      (data: { language: string; username: string }) => {
        dispatch(setLanguageChange(data));
      }
    );

    this.socket.on(
      "TEXT_UPDATE",
      (data: {
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
      }) => {
        dispatch(setTextUpdate(data));
      }
    );

    this.socket.on("connect_error", (err: Error) => {
      console.error("Socket error:", err.message);
    });
  }

  sendNewEditorState(text: string, language: string) {
    const payload = { text, language };
    this.socket?.emit("NEW_EDITOR_STATE", payload);
  }

  sendTextSelection(
    selection: {
      startLineNumber: number;
      startColumn: number;
      endLineNumber: number;
      endColumn: number;
    },
    username: string
  ) {
    const payload = { selection, username };
    this.socket?.emit("TEXT_SELECTION", payload);
  }

  sendCursorPosition(
    position: { lineNumber: number; column: number },
    username: string
  ) {
    const payload = { position, username };
    this.socket?.emit("CURSOR_POSITION", payload);
  }

  sendUserActivity(isActive: boolean, username: string) {
    const payload = { isActive, username };
    this.socket?.emit("USER_ACTIVITY", payload);
  }

  sendLanguageChange(language: string, username: string) {
    const payload = { language, username };
    this.socket?.emit("LANGUAGE_CHANGE", payload);
  }

  sendTextUpdate(
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
    }[],
    username: string
  ) {
    const payload = { changes, username };
    this.socket?.emit("TEXT_UPDATE", payload);
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }
}

export const socketService = new SocketService();
