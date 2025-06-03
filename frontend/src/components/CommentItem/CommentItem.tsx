import React, { useState } from "react";
import styles from "./CommentItem.module.scss";
import { CommentDto } from "../../types/api.types";

interface CommentItemProps {
  comment: CommentDto;
}

export const CommentItem: React.FC<CommentItemProps> = ({ comment }) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const toggleExpand = () => setIsExpanded((prev) => !prev);

  const formattedContent = () => {
    if (isExpanded) return comment.content;
    return comment.content.length > 100
      ? `${comment.content.slice(0, 100)}...`
      : comment.content;
  };

  return (
    <div className={styles.comment}>
      <div className={styles.header}>
        <span className={styles.author} style={{ color: comment.color }}>
          {comment.author || "Deepseek"}
        </span>
        {comment.isLlm && <span className={styles.llmBadge}>AI Generated</span>}
      </div>
      <div className={styles.content}>
        {formattedContent()}
        {comment.content.length > 100 && (
          <button className={styles.toggleButton} onClick={toggleExpand}>
            {isExpanded ? "Свернуть" : "Развернуть"}
          </button>
        )}
      </div>
      <span className={styles.date}>
        {new Date(comment.createdAt).toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
        })}
      </span>
    </div>
  );
};
