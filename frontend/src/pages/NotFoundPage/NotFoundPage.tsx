import { useNavigate } from "react-router-dom";
import stles from "./NotFoundPage.module.scss";

const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <div className={stles.container}>
      <div
        className={stles.logo}
        onClick={() => navigate("/")}
        title="На главную"
      >
        🚀 Blockshnote
      </div>
      <h1 className={stles.errorCode}>404</h1>
      <p className={stles.errorMsg}>Такой страницы нет</p>
    </div>
  );
};

export default NotFoundPage;
