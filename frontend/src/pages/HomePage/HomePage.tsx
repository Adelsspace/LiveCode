import { useNavigate } from "react-router-dom";
import styles from "./HomePage.module.scss";
import ThemeToggle from "../../components/ThemeToggle/ThemeToggle";

const HomePage = () => {
  const navigate = useNavigate();

  const handleCreateRoom = () => {
    const roomId = crypto.randomUUID();
    navigate(`/room/${roomId}`);
  };

  return (
    <>
      <header className={styles.header_container}>
        <div className={styles.logo}> Blokshnote</div> <ThemeToggle />
      </header>
      <section className={styles.main_container}>
        <h1 className={styles.title}>
          Проводи технические интервью на новом уровне
        </h1>

        <button
          className={`${styles.button} ${styles.button}`}
          onClick={handleCreateRoom}
        >
          Создать комнату
        </button>
      </section>
    </>
  );
};

export default HomePage;
