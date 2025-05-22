import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import {
  AdminTokenDto,
  CreateRoomRequest,
  RoomDto,
  UserDto,
  WebSocketUrlDto,
} from "../../types/api.types";

// const BASE_URL = "http://localhost:8080/api"; // для локальной разработки

export const roomApi = createApi({
  reducerPath: "roomApi",
  // baseQuery: fetchBaseQuery({ baseUrl: BASE_URL }),  // для локальной разработки
  baseQuery: fetchBaseQuery({ baseUrl: "/api" }),
  endpoints: (builder) => ({
    createRoom: builder.mutation<AdminTokenDto, CreateRoomRequest>({
      query: (body) => ({
        url: "/rooms",
        method: "POST",
        body,
      }),
    }),
    getWebSocketUrl: builder.query<WebSocketUrlDto, string>({
      query: (uuid) => `/rooms/${uuid}/url`,
    }),
    getRoom: builder.query<RoomDto, string>({
      query: (uuid) => `/rooms/${uuid}`,
    }),
    addUser: builder.mutation<UserDto, { uuid: string; username: string }>({
      query: ({ uuid, ...body }) => ({
        url: `/rooms/${uuid}/users`,
        method: "POST",
        body,
      }),
    }),
    addAdmin: builder.mutation<
      UserDto,
      { uuid: string; adminToken: string; username: string }
    >({
      query: ({ uuid, adminToken, ...body }) => ({
        url: `/rooms/${uuid}/admin`,
        method: "POST",
        params: { adminToken },
        body,
      }),
    }),
  }),
});

export const {
  useCreateRoomMutation,
  useGetWebSocketUrlQuery,
  useAddUserMutation,
  useGetRoomQuery,
  useAddAdminMutation,
} = roomApi;
