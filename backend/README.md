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

## Сборка образа с кодом

```bash
./mvnw spring-boot:build-image
```

## Запуск контейнера с базой

```bash
docker-compose up -d --build
```
