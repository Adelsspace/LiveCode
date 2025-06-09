import React, { useState, useRef, useEffect } from "react";
import styles from "./AddUserButton.module.scss";
import { UserPlusIcon } from "../icons";
import { CopyUrl } from "../CopyUrl/CopyUrl";

const AddUserButton: React.FC = () => {
  const [isDropdownOpen, setDropdownOpen] = useState(false);

  const dropdownRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);

  const handleButtonClick = () => {
    setDropdownOpen((prev) => !prev);
  };

  const handleClickOutside = (event: MouseEvent) => {
    if (
      dropdownRef.current &&
      !dropdownRef.current.contains(event.target as Node) &&
      buttonRef.current &&
      !buttonRef.current.contains(event.target as Node)
    ) {
      setDropdownOpen(false);
    }
  };

  useEffect(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div>
      <button
        ref={buttonRef}
        className={`${styles.addButton} ${isDropdownOpen ? styles.active : ""}`}
        onClick={handleButtonClick}
      >
        <UserPlusIcon className={styles.icon} />
      </button>

      {isDropdownOpen && (
        <div className={styles.dropdown} ref={dropdownRef}>
          <div className={styles.dropdown_title}>
            Скопируйте и отправьте ссылку
          </div>

          <div className={styles.linkContainer}>
            <CopyUrl isAdmin={false} />
            <CopyUrl isAdmin={true} />
          </div>
        </div>
      )}
    </div>
  );
};

export default AddUserButton;
