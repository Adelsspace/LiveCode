import { createBrowserRouter } from "react-router-dom";
import App from "./App";
import HomePage from "./pages/HomePage/HomePage";
import RoomPage from "./pages/RoomPage/RoomPage";
import NotFoundPage from "./pages/NotFoundPage/NotFoundPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    errorElement: <NotFoundPage />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: "room/:roomId",
        element: <RoomPage />,
      },
    ],
  },
]);
