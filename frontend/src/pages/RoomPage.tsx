import { useParams } from "react-router-dom";
import { Dropdown } from "../components/index";
import { useState } from "react";
import { Language } from "../types/shared.types";

export default function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const [language, setLanguage] = useState<Language>("javascript");

  const languages: Language[] = ["javascript", "typescript", "python", "java"];

  return (
    <div>
      <Dropdown
        options={languages}
        defaultValue="javascript"
        onSelect={setLanguage}
      />
      <h2>Комната: {roomId}</h2>
      TODO
    </div>
  );
}
