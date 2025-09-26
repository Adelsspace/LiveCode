# Collaborative Code Editor

Платформа для проведения технических собеседований с LLM-анализом кода. веб-интерфейс для совместного редактирования кода в реальном времени (Monaco), с чатом, списком участников, комментариями и интеграциями с LLM через бэкенд и WebSocket.

**Авторство:** Весь фронтенд от идеи заказчика до MVP (проектирование, дизайн, архитектура, реализация) выполнен одним разработчиком — мной [@adelsspace](https://github.com/adelsspace)

> Код можно посмотреть в папке frontend

## Стек технологий на фронте

- **React 19 + TypeScript**
- **Vite** — сборка и dev-сервер
- **Redux Toolkit + RTK Query** — глобальное состояние и API-интеграция
- **socket.io-client** — realtime через WebSocket
- **@monaco-editor/react (Monaco Editor)** — редактор кода (подсветка, курсоры, выделения)
- **Sass (scss)** — стили
- **ESLint** — линтинг
- **Docker + Nginx** — production Dockerfile для сборки и отдачи /dist через nginx
- Прочие: `react-router-dom`,

## Стек технологий на бэкенде

- Java 17
- Maven
- Spring Boot Web
- WebSocket
- Spring Data JPA
- PostgreSQL
- Kafka/rabbit/redis
- JUnit5
- Testcontainers
- Liquibase

---

## Фичи

- Совместное редактирование кода в реальном времени (синхронизация текста, курсоров и выделений).
- Подсветка синтаксиса для многих языков (JS/TS, Python, Java и т.д.).
- Встроенный чат для участников комнаты.
- Список участников и индикация активности.
- Комментарии/аннотации.
- Создание комнаты (генерация UUID + возврат `adminToken`).
- Авторизация администратора комнаты (через admin token / RoomAuth).
- Переключение темы (light/dark), управление шрифтом редактора.
- Модальное окно для отправки запросов к LLM (GptModal) — интеграция реализована через бэкенд.
- Кнопки управления редактором, копирование URL, базовые настройки UX.

---

## Быстрый старт (локально)

### Предпосылки

- Node.js (рекомендуется LTS — Node 18+)
- npm (или yarn/pnpm)

> **Бэкенд API** (локально или удалённо) для обработки `/api` роутов и выдачи WebSocket URL.
>
> 1. Собрать образ бэкенда по [инструкции](backend/README.md)
> 2. Запустить команду:
>
> ```bash
> docker-compose up -d --build
> ```

### Установка

```bash
# из корня проекта (где package.json)
npm install
```

### Запуск в режиме разработки

```bash
npm run dev
```

Откройте `http://localhost:5173` (или адрес, который выдаст Vite).

> По умолчанию фронтенд делает запросы к `/api`. Для работы в development обеспечьте:
>
> - локальный бэкенд, обслуживающий `/api`, или
> - прокси/настройку `VITE_API_BASE_URL` / изменение `baseUrl` в `src/store/api/roomApi.ts`.

### Сборка и preview

```bash
npm run build
npm run preview
```

---

## Docker (production)

В репозитории есть `Dockerfile`, который собирает приложение и отдаёт `/dist` через nginx (порт в образе — 3000).

Примеры:

```bash
docker build -t frontend:latest .
docker run -p 3000:3000 frontend:latest
```

---

## Конфигурация / взаимодействие с бэкендом

- RTK Query настроен с `baseQuery: fetchBaseQuery({ baseUrl: "/api" })` (файл: `src/store/api/roomApi.ts`).
- `socketService` запрашивает `getWebSocketUrl`, затем подключается к `io(wsConnectUrl, ...)`. Бэкенд должен возвращать корректный `wsConnectUrl`.
- Если бэкенд на другом хосте — используйте переменные окружения (Vite `VITE_API_BASE_URL`) или proxy.

### Основные эндпоинты, которые ожидает фронтенд

- `createRoom` — создание комнаты (возвращает `adminToken`).
- `getWebSocketUrl` — получение WebSocket URL.
- `addUser` — регистрация пользователя в комнате.
- `getRoom` — получение информации о комнате.
- `addAdmin` — привязка admin token.
- Комментарии: отдельный `commentApi`.

Смотpите `src/types/api.types.ts` для ожидаемых DTO.

---

## Структура проекта (корень `src/`)

- `src/pages` — страницы (Home, Room, NotFound)
- `src/components` — UI-компоненты (CodeEditor, Chat, UsersList, GptModal и пр.)
- `src/store` — Redux store, slices, RTK Query api
- `src/services` — socketService и т.п.
- `src/hooks` — кастомные хуки
- `src/styles` — SCSS и переменные
- `src/utils` — утилиты
- `public` — статические файлы

---

## Скрипты (package.json)

- `npm run dev` — dev server
- `npm run build` — production build
- `npm run preview` — preview собранного билда
- `npm run lint` — ESLint

---

## Контакты / авторство

- Все этапы фронтенда — от идеи до MVP — выполнены одним разработчиком (проектирование, дизайн, архитектура, реализация) — [@adelsspace](https://github.com/adelsspace)
- Для вопросов по коду и сборке — укажите контакт/issue в репозитории.

---
