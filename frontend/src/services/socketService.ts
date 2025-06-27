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
  setCommentsUpdated,
  setLlmIsAvailable,
} from "../store/slices/roomSlice";
import { roomApi } from "../store/api/roomApi";
import type { ThunkDispatch, UnknownAction } from "@reduxjs/toolkit";
import {
  CursorPosition,
  TextSelection,
  TextUpdate,
} from "../types/shared.types";

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
    this.socket.on("NEW_COMMENT", () => {
      dispatch(setCommentsUpdated(true));
    });
    this.socket.on("TEXT_SELECTION", (payload: TextSelection) => {
      dispatch(setTextSelection(payload));
    });
    this.socket.on("CURSOR_POSITION", (payload: CursorPosition) => {
      dispatch(setCursorPosition(payload));
    });
    this.socket.on("LLM_STATUS", (payload: { isAvailable: boolean }) => {
      dispatch(setLlmIsAvailable(payload.isAvailable));
    });

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

    this.socket.on("TEXT_UPDATE", (payload: TextUpdate) => {
      dispatch(setTextUpdate(payload));
    });

    this.socket.on("connect_error", (err: Error) => {
      console.error("Socket error:", err.message);
    });
  }

  sendNewEditorState(text: string, language: string) {
    const payload = { text, language };
    this.socket?.emit("NEW_EDITOR_STATE", payload);
  }

  sendTextSelection(payload: TextSelection) {
    this.socket?.emit("TEXT_SELECTION", payload);
  }

  sendCursorPosition(payload: CursorPosition) {
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

  sendTextUpdate(payload: TextUpdate) {
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
