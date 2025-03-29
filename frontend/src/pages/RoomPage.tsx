import { useParams } from "react-router-dom";

export default function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();

  return (
    <div>
      <h2>Комната: {roomId}</h2>
      TODO
    </div>
  );
}
