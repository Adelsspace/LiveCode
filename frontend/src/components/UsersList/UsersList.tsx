import { useAppSelector } from "../../hooks/reduxHooks";
import styles from "./UsersList.module.scss";

interface UsersListProps {
  users: {
    username: string;
    isActive: boolean;
    isAdmin: boolean;
    color: string;
  }[];
}

export const UsersList = () => {
  const { users }: UsersListProps = useAppSelector((state) => state.room);

  return (
    <div className={styles.usersList}>
      {users && (
        <ul>
          {users.map(({ username, isActive, isAdmin, color }) => (
            <li
              key={username}
              className={styles.userItem}
              style={{ backgroundColor: color }}
            >
              {isAdmin && <span className={styles.adminStar}>*</span>}
              <span>{username}</span>
              <span
                className={styles.statusIndicator}
                style={{ backgroundColor: isActive ? "green" : "gray" }}
              />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};
