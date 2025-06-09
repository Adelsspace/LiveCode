import React, { useState, useEffect, useRef } from "react";
import { CopyIcon } from "../icons";
import styles from "./CopyUrl.module.scss";
import Tooltip from "../Tooltip/Tooltip";
import { useAppSelector } from "../../hooks/reduxHooks";

interface LinkItemProps {
  isAdmin: boolean;
}

export const CopyUrl: React.FC<LinkItemProps> = ({ isAdmin }) => {
  const [showTooltip, setShowTooltip] = useState(false);
  const [tooltipMessage, setTooltipMessage] = useState("Копировать");
  const [isHovered, setIsHovered] = useState(false);
  const adminToken = useAppSelector((state) => state.room.adminToken);
  const [timeoutId, setTimeoutId] = useState<number | null>(null);
  const iconRef = useRef<HTMLDivElement>(null);

  const handleCopy = () => {
    const url = window.location.href;
    const copyUrl = isAdmin ? `${url}?adminToken=${adminToken}` : url;

    navigator.clipboard.writeText(copyUrl).then(() => {
      setShowTooltip(true);
      setTooltipMessage("Скопировано!");
      const id = setTimeout(() => {
        setShowTooltip(false);
        setTooltipMessage("Копировать");
      }, 1300);
      setTimeoutId(id);
    });
  };

  const handleMouseEnter = () => {
    setIsHovered(true);
  };

  const handleMouseLeave = () => {
    setIsHovered(false);
    setShowTooltip(false);
  };

  useEffect(() => {
    return () => {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    };
  }, [timeoutId]);

  return (
    <div className={styles.copyButton}>
      <span>{isAdmin ? "Добавить админа" : "Добавить пользователя"}:</span>
      <div
        className={styles.copyIconContainer}
        onClick={handleCopy}
        ref={iconRef}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
      >
        <CopyIcon className={styles.icon} />
        {(showTooltip || isHovered) && iconRef.current && (
          <Tooltip message={tooltipMessage} targetRef={iconRef} />
        )}
      </div>
    </div>
  );
};
