import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useGetRoomQuery } from "../../store/api/roomApi";
import { useSocket } from "../../hooks/useSocket";
import { socketService } from "../../services/socketService";
import { CodeEditor, RoomAuth } from "../../components";
import type { Participant } from "../../types/shared.types";
import styles from "./RoomPage.module.scss";
import { useAppDispatch } from "../../hooks/reduxHooks";
import { setUserActivityByUsername } from "../../store/slices/roomSlice";

const RoomPage = () => {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [showPopup, setShowPopup] = useState(false);

  const [isAdmin, setIsAdmin] = useState(false);
  const [username, setUsername] = useState<string>("");

  const { data: roomData, error: roomError } = useGetRoomQuery(roomId!, {
    skip: !roomId,
  });

  useSocket(roomId, username ?? undefined);

  const handleBeforeUnload = useCallback((e: BeforeUnloadEvent) => {
    if (sessionStorage.getItem("participant")) {
      e.preventDefault();
    }
  }, []);

  useEffect(() => {
    const checkRoom = () => {
      if (roomError) {
        if ("status" in roomError && roomError.status === 404) {
          navigate("/pageNotFound");
        } else {
          navigate("/error");
        }
        return;
      }

      if (roomData) {
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
      }
    };

    checkRoom();
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [roomData, roomError, roomId, navigate, handleBeforeUnload, dispatch]);

  useEffect(() => {
    if (!username) return;

    const updateUserActivity = (isActive: boolean) => {
      socketService.sendUserActivity(isActive, username);
      dispatch(setUserActivityByUsername({ isActive, username }));
    };

    const handleVisibilityChange = () => {
      updateUserActivity(document.visibilityState === "visible");
    };

    const handleBlur = () => updateUserActivity(false);
    const handleFocus = () => updateUserActivity(true);

    document.addEventListener("visibilitychange", handleVisibilityChange);
    window.addEventListener("blur", handleBlur);
    window.addEventListener("focus", handleFocus);

    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
      window.removeEventListener("blur", handleBlur);
      window.removeEventListener("focus", handleFocus);
    };
  }, [username, dispatch]);

  const handleAuthSuccess = (usernameInput: string, isAdmin: boolean) => {
    setUsername(usernameInput);
    setIsAdmin(isAdmin);
    setShowPopup(false);
  };

  if (!roomData && !roomError) {
    return <div className={styles.loading}>Загрузка...</div>;
  }

  return (
    <div className={styles.container}>
      {showPopup ? (
        <RoomAuth roomId={roomId!} onAuthSuccess={handleAuthSuccess} />
      ) : (
        <CodeEditor isAdmin={isAdmin} />
      )}
    </div>
  );
};

export default RoomPage;
