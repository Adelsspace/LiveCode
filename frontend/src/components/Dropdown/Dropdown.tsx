import React, { useState } from "react";
import styles from "./Dropdown.module.scss";

interface DropdownProps {
  options: string[];
  defaultValue?: string;
}

export const Dropdown: React.FC<DropdownProps> = ({
  options,
  defaultValue,
}) => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [selectedOption, setSelectedOption] = useState<string>(
    defaultValue || options[0]
  );

  return (
    <div className={styles.dropdown}>
      <button
        className={`${styles.dropdownToggle} ${isOpen ? styles.open : ""}`}
        onClick={() => setIsOpen(!isOpen)}
      >
        {selectedOption}

        <svg
          className={styles.arrow}
          viewBox="0 0 16 16"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M4 6L8 10L12 6"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>

      {isOpen && (
        <ul className={styles.dropdownMenu}>
          {options.map((option) => (
            <li
              key={option}
              className={styles.dropdownItem}
              onClick={() => {
                setSelectedOption(option);
                setIsOpen(false);
              }}
            >
              {option}
              {option === selectedOption && (
                <span className={styles.checkmark}>✓</span>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};
