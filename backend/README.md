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


## Инструкция по запуску backend

1. Должен быть установлен и запущен docker
2. Запустить две команды ниже


## Сборка образа с кодом

```bash
./mvnw spring-boot:build-image
```

## Запуск контейнера с базой

```bash
docker-compose up -d --build
```
