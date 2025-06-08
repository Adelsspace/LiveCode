import { configureStore } from "@reduxjs/toolkit";
import { roomApi } from "./api/roomApi";
import roomReducer from "./slices/roomSlice";
import { socketMiddleware } from "./middleware/socketMiddleware";
import type { Store, ThunkDispatch, UnknownAction } from "@reduxjs/toolkit";
import { commentApi } from "./api/commentApi";

export const store: Store = configureStore({
  reducer: {
    [roomApi.reducerPath]: roomApi.reducer,
    [commentApi.reducerPath]: commentApi.reducer,
    room: roomReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(roomApi.middleware)
      .concat(commentApi.middleware)
      .concat(socketMiddleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = ThunkDispatch<RootState, unknown, UnknownAction>;
