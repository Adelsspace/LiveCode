import { useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "./HomePage.module.scss";
import ThemeToggle from "../../components/ThemeToggle/ThemeToggle";
import { Logo } from "../../components";

const HomePage = () => {
  const navigate = useNavigate();

  const handleCreateRoom = async () => {
    try {
      const roomId = crypto.randomUUID();
      const response = await axios.post("/api/rooms", {
        username: "Host",
        uuid: roomId,
      });

      navigate(`/room/${roomId}?adminToken=${response.data.adminToken}`);
    } catch (error) {
      console.error("Ошибка создания комнаты:", error);
      navigate("/error");
    }
  };

  return (
    <>
      <header className={styles.header_container}>
        <Logo />
        <ThemeToggle />
      </header>
      <section className={styles.main_container}>
        <h1 className={styles.title}>
          Проводи технические интервью на новом уровне
        </h1>
        <button className={styles.button} onClick={handleCreateRoom}>
          Создать комнату
        </button>
      </section>
    </>
  );
};

export default HomePage;
