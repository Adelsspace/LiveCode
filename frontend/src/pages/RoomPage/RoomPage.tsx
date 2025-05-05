import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import { CodeEditor, RoomAuthModal, UsersList } from "../../components";
import { useWebSocket } from "../../hooks/useWebSocket";
import styles from "./RoomPage.module.scss";
import { Participant } from "../../types/shared.types";

const RoomPage = () => {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [showPopup, setShowPopup] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [roomStatus, setRoomStatus] = useState<"LOADING" | "EXISTS">("LOADING");
  const [isAdmin, setIsAdmin] = useState(false);
  const [username, setUsername] = useState<string | null>(null);

  const { editorContent, users, sendMessage } = useWebSocket(
    roomId,
    username ?? undefined
  );

  const handleBeforeUnload = useCallback((e: BeforeUnloadEvent) => {
    if (sessionStorage.getItem("participant")) {
      e.preventDefault();
    }
  }, []);

  useEffect(() => {
    const checkRoom = async () => {
      try {
        await axios.get(`/api/rooms/${roomId}`);
        setRoomStatus("EXISTS");

        const savedParticipant = sessionStorage.getItem("participant");
        if (savedParticipant) {
          const participant: Participant = JSON.parse(savedParticipant);
          if (participant.roomId === roomId) {
            setUsername(participant.username);
            setIsAdmin(!!participant.adminToken);
            setShowPopup(false);
            window.addEventListener("beforeunload", handleBeforeUnload);
            return;
          }
        }
        setShowPopup(true);
      } catch (error) {
        if (axios.isAxiosError(error) && error.response?.status === 404) {
          navigate("/pageNotFound");
        } else {
          navigate("/error");
        }
      }
    };

    checkRoom();
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [roomId, navigate, handleBeforeUnload]);

  const handleAdminRequest = async (username: string) => {
    try {
      const adminToken = searchParams.get("adminToken");
      const { data } = await axios.post(
        `/api/rooms/${roomId}/admin`,
        { username },
        { params: { adminToken } }
      );
      return { success: true, data, isAdmin: true };
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 403) {
        return { success: false };
      }
      throw error;
    }
  };

  const handleUserRequest = async (username: string) => {
    const { data } = await axios.post(`/api/rooms/${roomId}/users`, {
      username,
    });
    return { success: true, data, isAdmin: false };
  };

  const handleSubmit = async (usernameInput: string) => {
    if (!usernameInput.trim() || isSubmitting) return;

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      let result;
      if (searchParams.has("adminToken")) {
        result = await handleAdminRequest(usernameInput);
        if (!result.success) {
          searchParams.delete("adminToken");
          setSearchParams(searchParams);
          result = await handleUserRequest(usernameInput);
        }
      } else {
        result = await handleUserRequest(usernameInput);
      }

      const participant: Participant = {
        roomId: roomId!,
        username: usernameInput,
        adminToken:
          result.data.adminToken || searchParams.get("adminToken") || undefined,
      };

      sessionStorage.setItem("participant", JSON.stringify(participant));
      setIsAdmin(result.isAdmin ?? false);
      setUsername(usernameInput);
      setShowPopup(false);
    } catch (error) {
      console.error("Authentication failed:", error);
      setErrorMessage(
        axios.isAxiosError(error)
          ? error.response?.data?.message || "Authentication failed"
          : "Unknown error occurred"
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCodeChange = useCallback(
    (newCode: string) => sendMessage(newCode),
    [sendMessage]
  );

  if (roomStatus === "LOADING") {
    return <div className={styles.loading}>Загрузка...</div>;
  }

  return (
    <div className={styles.container}>
      <CodeEditor
        isAdmin={isAdmin}
        onCodeChange={handleCodeChange}
        initialCode={editorContent}
      />
      <UsersList users={users} />

      {showPopup && (
        <RoomAuthModal
          isAdmin={searchParams.has("adminToken")}
          errorMessage={errorMessage}
          isSubmitting={isSubmitting}
          onSubmit={handleSubmit}
        />
      )}
    </div>
  );
};

export default RoomPage;
