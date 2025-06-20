import React from "react";

interface IconProps {
  size?: number;
  color?: string;
  className?: string;
}

export const SendIcon: React.FC<IconProps> = ({
  size = 24,
  color = "currentColor",
  className = "",
}) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke={color}
      strokeWidth="2"
      stroke-linecap="round"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <path d="M12 4L12 20" />
      <path d="M6 10L12 4L18 10" />
    </svg>
  );
};
