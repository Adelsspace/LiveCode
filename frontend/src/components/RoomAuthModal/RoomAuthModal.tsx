import { useState } from "react";
import styles from "./RoomAuthModal.module.scss";
import { Button } from "../Button/Button";

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
          <Button
            type="submit"
            disabled={!username.trim() || isSubmitting}
            loading={isSubmitting}
            label="Присоединиться"
            className={styles.button}
          />
        </form>
      </div>
    </div>
  );
};
