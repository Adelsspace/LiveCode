import { useEffect, useRef, useState } from "react";
import styles from "./Dropdown.module.scss";

interface DropdownProps<T> {
  options: T[];
  defaultValue?: T;
  onSelect: (option: T) => void;
}

export const Dropdown = <T extends string | number>({
  options,
  defaultValue,
  onSelect,
}: DropdownProps<T>) => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [selectedOption, setSelectedOption] = useState<T | undefined>(
    defaultValue
  );
  useEffect(() => {
    setSelectedOption(defaultValue);
  }, [defaultValue]);

  const dropdownRef = useRef<HTMLDivElement>(null);

  const handleClickOutside = (event: MouseEvent) => {
    const target = event.target as Node;
    if (dropdownRef.current && !dropdownRef.current.contains(target)) {
      setIsOpen(false);
    }
  };
  useEffect(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const handleSelect = (option: T) => {
    setSelectedOption(option);
    setIsOpen(false);
    onSelect(option);
  };

  return (
    <div className={styles.dropdown} ref={dropdownRef}>
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
              onClick={() => handleSelect(option)}
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
