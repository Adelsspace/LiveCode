import { useParams } from "react-router-dom";
import { Dropdown } from "../components/index";

export default function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();

  return (
    <div>
      <Dropdown
        options={["JavaScript", "Java", "Python"]}
        defaultValue="JavaScript"
      />
      <h2>Комната: {roomId}</h2>
      TODO
    </div>
  );
}
