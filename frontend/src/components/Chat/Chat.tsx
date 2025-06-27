import React, { useState, useEffect, useRef } from "react";
import { useAppSelector } from "../../hooks/reduxHooks";
import {
  useGetCommentsQuery,
  usePostCommentMutation,
  useAskGptMutation,
} from "../../store/api/commentApi";
import { CommentItem } from "../CommentItem/CommentItem";
import styles from "./Chat.module.scss";
import { GptModal } from "../GptModal/GptModal";
import { Button } from "../Button/Button";
import { useDispatch } from "react-redux";
import { setCommentsUpdated } from "../../store/slices/roomSlice";
import { SendIcon } from "../icons";
import DotLoader from "../DotLoader/DotLoader";

export const Chat: React.FC = () => {
  const [message, setMessage] = useState<string>("");
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const textareaRef = useRef<HTMLTextAreaElement | null>(null);
  const dispatch = useDispatch();
  const commentsUpdated = useAppSelector((state) => state.room.commentsUpdated);
  const messagesRef = useRef<HTMLDivElement | null>(null);

  const { roomId, adminToken, name } = useAppSelector((state) => state.room);
  const currentUser = useAppSelector((state) => state.room.name);
  const llmIsAvailable = useAppSelector((state) => state.room.llmIsAvailable);
  const { data, isLoading, isError, refetch, isFetching } = useGetCommentsQuery(
    { roomId: roomId!, adminToken: adminToken! },
    { skip: !roomId || !adminToken }
  );

  const [postComment] = usePostCommentMutation();
  const [askGpt] = useAskGptMutation();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!message.trim() || !roomId || !adminToken || !currentUser) return;

    try {
      await postComment({
        roomId,
        adminToken,
        content: message,
        author: name,
      }).unwrap();
      setMessage("");
      await refetch();
      scrollToBottom();
    } catch (err) {
      console.error("Не удалось отправить комментарий:", err);
    }
  };

  const handleAskGpt = async (prompt: string) => {
    if (!roomId || !adminToken) return;

    try {
      await askGpt({ roomId, adminToken, prompt }).unwrap();
      await refetch();
    } catch (err) {
      console.error("Не удалось спросить LLM:", err);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setMessage(e.target.value);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e as unknown as React.FormEvent<HTMLFormElement>);
    }
  };

  const scrollToBottom = () => {
    requestAnimationFrame(() => {
      if (messagesRef.current) {
        messagesRef.current.scrollTop = messagesRef.current.scrollHeight;
      }
    });
  };

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height = `${Math.min(
        textareaRef.current.scrollHeight,
        150
      )}px`;
    }
  }, [message]);

  useEffect(() => {
    if (commentsUpdated) {
      refetch();
      dispatch(setCommentsUpdated(false));
    }
  }, [commentsUpdated, refetch, dispatch]);

  if (isLoading)
    return <div className={styles.loading}>Loading comments...</div>;
  if (isError)
    return <div className={styles.error}>Error loading comments!</div>;

  return (
    <div className={styles.chatContainer}>
      <div className={styles.messages} ref={messagesRef}>
        {data?.items.map((comment) => (
          <CommentItem
            key={`${comment.createdAt}-${comment.author}`}
            comment={comment}
          />
        ))}
      </div>

      <form onSubmit={handleSubmit} className={styles.inputArea}>
        <textarea
          ref={textareaRef}
          value={message}
          onChange={handleInputChange}
          placeholder="Комментарий..."
          className={styles.input}
          onKeyDown={handleKeyDown}
          disabled={isFetching}
          rows={1}
          style={{ overflowY: "auto", resize: "none" }}
        />
        <Button
          type="submit"
          icon={<SendIcon />}
          disabled={isFetching || !message.trim()}
          className={styles.submitButton}
        />
        {llmIsAvailable ? (
          <Button
            type="button"
            onClick={() => setIsModalOpen(true)}
            label={"Анализ кода"}
          />
        ) : (
          <DotLoader />
        )}
      </form>

      <GptModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleAskGpt}
      />
    </div>
  );
};
