import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { CommentDto, CommentListResponse } from "../../types/api.types";

// const BASE_URL = "http://localhost/api"; // для локальной разработки

export const commentApi = createApi({
  reducerPath: "commentApi",
  // baseQuery: fetchBaseQuery({ baseUrl: BASE_URL }), // для локальной разработки

  baseQuery: fetchBaseQuery({ baseUrl: "/api" }),
  endpoints: (builder) => ({
    getComments: builder.query<
      CommentListResponse,
      { roomId: string; adminToken: string }
    >({
      query: ({ roomId, adminToken }) =>
        `/rooms/${roomId}/comments?adminToken=${adminToken}`,
    }),
    postComment: builder.mutation<
      CommentDto,
      { roomId: string; adminToken: string; content: string; author: string }
    >({
      query: ({ roomId, adminToken, ...body }) => ({
        url: `/rooms/${roomId}/comments`,
        method: "POST",
        params: { adminToken },
        body,
      }),
    }),
    askGpt: builder.mutation<
      CommentDto,
      { roomId: string; adminToken: string; prompt: string }
    >({
      query: ({ roomId, adminToken, prompt }) => ({
        url: `/rooms/${roomId}/review`,
        method: "POST",
        params: { adminToken },
        body: { prompt },
      }),
    }),
  }),
});

export const {
  useGetCommentsQuery,
  usePostCommentMutation,
  useAskGptMutation,
} = commentApi;
