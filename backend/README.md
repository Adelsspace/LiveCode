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

## Инструкция по сборке бэкенда

1. Должен быть установлен и запущен docker
2. Должен быть установлен jdk-17 или выше
3. Запустить команду ниже

## Сборка образа с кодом

```bash
./mvnw spring-boot:build-image -DskipTests
```

## Обновление OpenAPI спецификации

```bash
mvn clean test
```
