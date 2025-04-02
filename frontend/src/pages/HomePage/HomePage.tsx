import { useNavigate } from "react-router-dom";

export default function HomePage() {
  const navigate = useNavigate();

  const handleCreateRoom = () => {
    const roomId = crypto.randomUUID();
    navigate(`/room/${roomId}`);
  };

  return (
    <div>
      <h1>Проводи технические интервью на новом уровне</h1>
      <button onClick={handleCreateRoom}>Создать комнату</button>
    </div>
  );
}
