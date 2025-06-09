import React, { useEffect, useState } from "react";
import styles from "./Tooltip.module.scss";

interface TooltipProps {
  message: string;
  targetRef: React.RefObject<HTMLDivElement | null>;
}

const Tooltip: React.FC<TooltipProps> = ({ message, targetRef }) => {
  const [position, setPosition] = useState({ top: 0, left: 0 });

  useEffect(() => {
    const updatePosition = () => {
      if (targetRef.current) {
        const rect = targetRef.current.getBoundingClientRect();
        setPosition({
          top: rect.top - 40,
          left: rect.left + rect.width / 2,
        });
      }
    };

    updatePosition();
    window.addEventListener("resize", updatePosition);

    return () => {
      window.removeEventListener("resize", updatePosition);
    };
  }, [targetRef]);

  return (
    <div
      className={styles.tooltip}
      style={{
        top: `${position.top}px`,
        left: `${position.left}px`,
        transform: "translateX(-50%)",
      }}
    >
      {message}
    </div>
  );
};

export default Tooltip;
