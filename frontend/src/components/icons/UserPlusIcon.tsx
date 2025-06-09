import React from "react";

interface IconProps {
  size?: number;
  color?: string;
  className?: string;
}

export const UserPlusIcon: React.FC<IconProps> = ({
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
      stroke-linecap="round"
      className={className}
    >
      <circle cx="12" cy="8" r="4" />
      <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
      <line x1="19" y1="10" x2="19" y2="4" />
      <line x1="16" y1="7" x2="22" y2="7" />
    </svg>
  );
};
