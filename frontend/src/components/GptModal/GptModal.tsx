import React, { useCallback, useEffect, useState, useRef } from "react";
import styles from "./GptModal.module.scss";

interface GptModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (prompt: string) => void;
}

export const GptModal: React.FC<GptModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [prompt, setPrompt] = useState("");
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const handleSubmit = useCallback(
    (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      onSubmit(prompt);
      setPrompt("");
      onClose();
    },
    [onClose, onSubmit, prompt]
  );

  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
      } else if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        handleSubmit(e as unknown as React.FormEvent<HTMLFormElement>);
      }
    },
    [onClose, handleSubmit]
  );

  useEffect(() => {
    if (isOpen) {
      window.addEventListener("keydown", handleKeyDown);
      textareaRef.current?.focus();
    } else {
      window.removeEventListener("keydown", handleKeyDown);
    }

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, handleKeyDown]);

  if (!isOpen) return <></>;

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modal}>
        <header className={styles.header}>Анализ кода при помощи LLM</header>
        <form onSubmit={handleSubmit}>
          <textarea
            ref={textareaRef}
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder="Дополнительный промт..."
            className={styles.textarea}
            aria-label="Введите ваш промт"
          />
          <div className={styles.buttons}>
            <button type="submit" className={styles.submitButton}>
              Анализировать
            </button>
            <button
              type="button"
              className={styles.cancelButton}
              onClick={onClose}
              aria-label="Закрыть модальное окно"
            >
              Отмена
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
