import styles from "./UsersList.module.scss";

interface UsersListProps {
  users: string[];
}

export const UsersList = ({ users }: UsersListProps) => {
  return (
    <div className={styles.usersList}>
      <h3>Участники:</h3>
      <ul>
        {users.map((user) => (
          <li key={user} className={styles.userItem}>
            {user}
          </li>
        ))}
      </ul>
    </div>
  );
};
