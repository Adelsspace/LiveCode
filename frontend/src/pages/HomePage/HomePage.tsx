import { useNavigate } from "react-router-dom";
import { useCreateRoomMutation } from "../../store/api/roomApi";
import styles from "./HomePage.module.scss";
import ThemeToggle from "../../components/ThemeToggle/ThemeToggle";
import { Button, Logo } from "../../components";

const HomePage = () => {
  const navigate = useNavigate();
  const [createRoom] = useCreateRoomMutation();

  const handleCreateRoom = async () => {
    try {
      const roomId = crypto.randomUUID();
      const { adminToken } = await createRoom({
        username: "Host",
        uuid: roomId,
      }).unwrap();

      navigate(`/room/${roomId}?adminToken=${adminToken}`);
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
        <Button label={"Создать комнату"} onClick={handleCreateRoom} />
      </section>
    </>
  );
};

export default HomePage;
