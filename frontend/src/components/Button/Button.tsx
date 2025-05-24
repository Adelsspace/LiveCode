import React from "react";
import styles from "./Button.module.scss";

interface ButtonProps {
  label: string;
  toggleLabel?: string;
  onClick?: () => void;
  toggle?: boolean;
  disabled?: boolean;
  type?: "button" | "submit" | "reset";
  icon?: React.ReactNode;
  loading?: boolean;
  size?: "small" | "medium" | "large";
  className?: string;
}

export const Button: React.FC<ButtonProps> = ({
  label,
  toggleLabel,
  onClick,
  toggle,
  disabled,
  type = "button",
  icon,
  loading,
  size = "medium",
  className,
}) => {
  return (
    <button
      className={`${styles.button} ${styles[size]} ${className}`}
      onClick={onClick}
      disabled={disabled || loading}
      type={type}
    >
      {loading ? (
        <span className={styles.loader}></span>
      ) : (
        <>
          {icon && <span className={styles.icon}>{icon}</span>}
          {toggle && toggleLabel ? toggleLabel : label}
        </>
      )}
    </button>
  );
};
