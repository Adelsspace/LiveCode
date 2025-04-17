## Стек технологий для бэкенда

- Java 17
- Maven
- Spring Boot Web
- WebSocket
- Spring Data JPA
- PostgreSQL
- Kafka/rabbit/redis
- JUnit5
- Testcontainers

## Установка JDK

* [Установщики для windows, linux, macos](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* Установка через cli:

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

## Инструкция по запуску backend

1. Должен быть установлен и запущен docker
2. Должен быть установлен jdk-17 или выше
3. Запустить две команды ниже

## Сборка образа с кодом

```bash
./mvnw spring-boot:build-image
```

## Запуск контейнера с базой

```bash
docker-compose up -d --build
```

## Взаимодействие с WebSocket

Для получения ссылки выполнить GET запрос на "/api/rooms/{uuid}/url", где {uuid} - UUID комнаты.  
Пример тела ответа:

```json
{
  "wsConnectUrl": "ws://localhost:8080/ws/room/connect"
}
```
Для подключения и обмена сообщениями ссылка должна содержать параметры "user" и "roomUuid", в которых передаются имя пользователя и UUID комнаты.
Для передачи в сообщении состояния комнаты ссылка должна содержать параметр "message_type=NEW_ROOM_STATE".
Пример ссылки:

```
ws://localhost:8080/ws/room/connect?user=John&roomUuid=82cf90c2-b024-4ad1-899a-ab929849df03&message_type=NEW_ROOM_STATE
```

Пример тела сообщения:
```json
{
  "editorText": "public static"
}
```
При подключении или отключении пользователя и приёме сообщения всем пользователям комнаты рассылается текущее состояние комнаты в виде:
```json
{
  "editorText": "public static",
  "users": [
    "John",
    "Jane"
  ]
}
```
