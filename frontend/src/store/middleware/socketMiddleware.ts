import type { Middleware } from "@reduxjs/toolkit";
import type { RootState, AppDispatch } from "../store";
import { socketService } from "../../services/socketService";

type SocketConnectAction = {
  type: "socket/connect";
  payload: { roomId: string; username: string };
};

type SocketDisconnectAction = {
  type: "socket/disconnect";
};

type SocketAction = SocketConnectAction | SocketDisconnectAction;

const isSocketAction = (action: unknown): action is SocketAction => {
  return (
    typeof action === "object" &&
    action !== null &&
    "type" in action &&
    (action.type === "socket/connect" || action.type === "socket/disconnect")
  );
};

export const socketMiddleware: Middleware<object, RootState, AppDispatch> =
  (store) => (next) => (action) => {
    if (!isSocketAction(action)) return next(action);

    switch (action.type) {
      case "socket/connect": {
        const { roomId, username } = action.payload;
        socketService.init(roomId, username, store.dispatch);
        break;
      }

      case "socket/disconnect": {
        socketService.disconnect();
        break;
      }
    }

    return next(action);
  };
