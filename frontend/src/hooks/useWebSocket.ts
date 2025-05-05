import { useState, useEffect, useCallback, useRef } from "react";
import axios from "axios";

export const useWebSocket = (roomId?: string, username?: string) => {
  const [editorContent, setEditorContent] = useState("");
  const [users, setUsers] = useState<string[]>([]);
  const ws = useRef<WebSocket | null>(null);

  const sendMessage = useCallback((newCode: string) => {
    if (ws.current?.readyState === WebSocket.OPEN) {
      ws.current.send(JSON.stringify({ editorText: newCode }));
    }
  }, []);

  useEffect(() => {
    let isMounted = true;
    const setupWebSocket = async () => {
      try {
        if (!roomId || !username) return;

        const response = await axios.get(`/api/rooms/${roomId}/url`);
        if (!isMounted) return;

        const baseWsUrl = response.data.wsConnectUrl;
        const wsUrl = new URL(baseWsUrl);

        wsUrl.searchParams.append("user", username);
        wsUrl.searchParams.append("roomUuid", roomId);
        wsUrl.searchParams.append("message_type", "NEW_ROOM_STATE");

        ws.current = new WebSocket(wsUrl.toString());

        ws.current.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            if (data.editorText !== undefined)
              setEditorContent(data.editorText);
            if (data.users) setUsers(data.users);
          } catch (error) {
            console.error("Error parsing WebSocket message:", error);
          }
        };

        ws.current.onerror = (error) =>
          console.error("WebSocket error:", error);

        ws.current.onclose = () => console.log("WebSocket disconnected");
      } catch (error) {
        console.error("Error setting up WebSocket:", error);
      }
    };

    setupWebSocket();

    return () => {
      isMounted = false;
      ws.current?.close();
    };
  }, [roomId, username]);

  return { editorContent, users, sendMessage };
};
