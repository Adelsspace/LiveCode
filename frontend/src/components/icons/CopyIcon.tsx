import React from "react";

interface IconProps {
  size?: number;
  color?: string;
  className?: string;
}

export const CopyIcon: React.FC<IconProps> = ({
  size = 24,
  color = "currentColor",
  className = "",
}) => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke={color}
      stroke-width="2"
      className={className}
    >
      <rect x="9" y="9" width="13" height="13" rx="2" stroke-linejoin="round" />
      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
    </svg>
  );
};
