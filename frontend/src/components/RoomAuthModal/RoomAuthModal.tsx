import { useState } from "react";
import styles from "./RoomAuthModal.module.scss";

interface RoomAuthModalProps {
  isAdmin: boolean;
  errorMessage: string;
  isSubmitting: boolean;
  onSubmit: (username: string) => void;
}

export const RoomAuthModal = ({
  errorMessage,
  isSubmitting,
  onSubmit,
}: RoomAuthModalProps) => {
  const [username, setUsername] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(username.trim());
  };

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modal}>
        <h2 className={styles.modalTitle}>Введите имя для входа</h2>
        <form onSubmit={handleSubmit} className={styles.form}>
          <input
            type="text"
            placeholder="Имя"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={isSubmitting}
            className={styles.input}
            autoFocus
          />
          {errorMessage && <div className={styles.error}>{errorMessage}</div>}
          <button
            type="submit"
            disabled={!username.trim() || isSubmitting}
            className={styles.button}
          >
            {isSubmitting ? "Отправка..." : "Присоединиться"}
          </button>
        </form>
      </div>
    </div>
  );
};
