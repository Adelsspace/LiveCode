import { useEffect, useState } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import { CodeEditor } from "../../components";
import { RoomAuthModal } from "../../components";
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

  const handleBeforeUnload = (e: BeforeUnloadEvent) => {
    if (sessionStorage.getItem("participant")) {
      e.preventDefault();
    }
  };

  useEffect(() => {
    const checkRoom = async () => {
      try {
        await axios.get(`/api/rooms/${roomId}`);
        setRoomStatus("EXISTS");

        const savedParticipant = sessionStorage.getItem("participant");
        if (savedParticipant) {
          const participant: Participant = JSON.parse(savedParticipant);
          if (participant.roomId === roomId) {
            setShowPopup(false);
            setIsAdmin(!!participant.adminToken);
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

    window.addEventListener("beforeunload", handleBeforeUnload);
    checkRoom();

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [roomId, navigate]);

  const handleAdminRequest = async (username: string) => {
    try {
      const adminToken = searchParams.get("adminToken");
      const { data } = await axios.post(
        `/api/rooms/${roomId}/admin`,
        { username },
        { params: { adminToken } }
      );

      return {
        success: true,
        data,
        isAdmin: true,
      };
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
    return {
      success: true,
      data,
      isAdmin: false,
    };
  };

  const handleSubmit = async (username: string) => {
    if (!username.trim() || isSubmitting) return;

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      let result;
      if (searchParams.has("adminToken")) {
        result = await handleAdminRequest(username);
        if (!result.success) {
          searchParams.delete("adminToken");
          setSearchParams(searchParams);
          result = await handleUserRequest(username);
        }
      } else {
        result = await handleUserRequest(username);
      }

      const participant: Participant = {
        roomId: roomId!,
        username,
        adminToken:
          result.data.adminToken || searchParams.get("adminToken") || undefined,
      };

      sessionStorage.setItem("participant", JSON.stringify(participant));
      setIsAdmin(result.isAdmin ?? false);
      setShowPopup(false);
    } catch (error) {
      if (axios.isAxiosError(error)) {
        if (error.response?.status === 409) {
          setErrorMessage("Это имя уже занято");
        } else if (error.response?.status === 404) {
          navigate("/pageNotFound");
        } else {
          setErrorMessage("Ошибка сервера");
        }
      } else {
        setErrorMessage("Ошибка подключения");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (roomStatus === "LOADING")
    return <div className={styles.loading}>Загрузка...</div>;

  return (
    <div className={styles.container}>
      <CodeEditor isAdmin={isAdmin} />

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
