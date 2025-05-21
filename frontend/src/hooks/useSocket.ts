import { useEffect } from "react";
import { useDispatch } from "react-redux";

export const useSocket = (roomId?: string, username?: string) => {
  const dispatch = useDispatch();

  useEffect(() => {
    if (roomId && username) {
      dispatch({
        type: "socket/connect",
        payload: { roomId, username },
      });
    }

    return () => {
      dispatch({ type: "socket/disconnect" });
    };
  }, [roomId, username, dispatch]);
};
