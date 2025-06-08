import React, { useState } from "react";
import {
  useAddUserMutation,
  useAddAdminMutation,
} from "../../store/api/roomApi";
import { RoomAuthModal } from "../../components";
import { useAppDispatch } from "../../hooks/reduxHooks";
import {
  setAdminToken,
  setName,
  setRoomId,
} from "../../store/slices/roomSlice";
import { useSearchParams } from "react-router-dom";

interface RoomAuthProps {
  roomId: string;
  onAuthSuccess: (username: string, isAdmin: boolean) => void;
}

interface ErrorData {
  error: string;
}

interface Error {
  status: number;
  data: ErrorData;
}

export const RoomAuth: React.FC<RoomAuthProps> = ({
  roomId,
  onAuthSuccess,
}) => {
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const dispatch = useAppDispatch();
  const [addUser] = useAddUserMutation();
  const [addAdmin] = useAddAdminMutation();
  const [searchParams, setSearchParams] = useSearchParams();

  const handleSubmit = async (usernameInput: string) => {
    if (!usernameInput.trim() || isSubmitting || !roomId) return;

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      let result;
      const adminToken = searchParams.get("adminToken");

      if (adminToken) {
        try {
          const adminResult = await addAdmin({
            uuid: roomId,
            adminToken,
            username: usernameInput,
          }).unwrap();

          result = {
            data: adminResult,
            isAdmin: true,
          };
        } catch (adminError) {
          if ((adminError as { status: number }).status === 403) {
            searchParams.delete("adminToken");
            setSearchParams(searchParams);
            const userResult = await addUser({
              uuid: roomId,
              username: usernameInput,
            }).unwrap();

            result = {
              data: userResult,
              isAdmin: false,
            };
          } else {
            throw adminError;
          }
        }
      } else {
        const userResult = await addUser({
          uuid: roomId,
          username: usernameInput,
        }).unwrap();

        result = {
          data: userResult,
          isAdmin: false,
        };
      }

      const participant = {
        roomId,
        username: usernameInput,
        adminToken: adminToken || undefined,
      };

      sessionStorage.setItem("participant", JSON.stringify(participant));
      dispatch(setName(usernameInput));
      dispatch(setName(participant.username));
      dispatch(setRoomId(roomId));
      if (adminToken) dispatch(setAdminToken(adminToken));
      onAuthSuccess(usernameInput, result.isAdmin);
    } catch (error) {
      console.error("Аутентификация не удалась:", error);
      if (error && typeof error === "object" && "status" in error) {
        const apiError = error as Error;
        if (apiError.status === 409) {
          setErrorMessage("Логин занят, попробуйте другой");
        } else {
          setErrorMessage(
            apiError.data?.error || "Произошла неизвестная ошибка"
          );
        }
      } else {
        setErrorMessage("Произошла неизвестная ошибка");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <RoomAuthModal
      isAdmin={searchParams.has("adminToken")}
      errorMessage={errorMessage}
      isSubmitting={isSubmitting}
      onSubmit={handleSubmit}
    />
  );
};
