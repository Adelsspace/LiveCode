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
        uuid: roomId,
      }).unwrap();

      navigate(`/room/${roomId}?adminToken=${adminToken}`);
    } catch (error) {
      console.error("Ошибка создания комнаты:", error);
      navigate("/error");
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header_container}>
        <Logo />
        <ThemeToggle />
      </header>
      <section className={styles.main_container}>
        <h1 className={styles.title}>
          Платформа для проведения технических собеседований с анализом кода при
          помощью ИИ
        </h1>
        <Button label={"Создать комнату"} onClick={handleCreateRoom} />
      </section>
      <section className={styles.advantages}>
        <ul>
          <ol>
            <span className={styles.dash}>—</span> Без регистрации
          </ol>
          <ol>
            <span className={styles.dash}>—</span> Совместное редактирование
            кода в реальном времени
          </ol>

          <ol>
            <span className={styles.dash}>—</span> Чат для админов для удобного
            общения и заметок
          </ol>
          <ol>
            <span className={styles.dash}>—</span> Анализ кода с помощью ИИ с
            возможностью дополнительного промта
          </ol>
          <ol>
            <span className={styles.dash}>—</span> Отслеживание активности
            кандидата для точной оценки
          </ol>
          <ol>
            <span className={styles.dash}>—</span> Подсветка синтаксиса для
            JavaScript, TypeScript, Python, Java, Kotlin, Go, Plain Text, C++,
            C#, SQL, Ruby, PHP и Markdown
          </ol>
        </ul>
      </section>
    </div>
  );
};

export default HomePage;
