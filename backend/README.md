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
- Liquibase

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
mvn clean test -Dtest=OpenApiGenerationTest
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
Пример ссылки:

```
ws://localhost:8080/ws/room/connect?user=John&roomUuid=82cf90c2-b024-4ad1-899a-ab929849df03
```
### События

- `NEW_EDITOR_STATE`  
  Пример тела сообщения:
```json
{
  "text": "public static",
  "language": "Java"
}
```
- `TEXT_SELECTION`  
  Пример тела сообщения:
```json
{
  "selection": {
    "startLineNumber": 5,
    "startColumn": 3,
    "endLineNumber": 7,
    "endColumn": 12
  },
  "username": "John"
}
```

- `CURSOR_POSITION`  
  Пример тела сообщения:
```json
{
  "position": {
    "lineNumber": 5,
    "column": 15
  },
  "username": "John"
}
```

- `USER_ACTIVITY`  
  Пример тела сообщения:
```json
{
  "isActive": false,
  "username": "John"
}
```

- `LANGUAGE_CHANGE`  
  Пример тела сообщения:
```json
{
  "language": "typescript",
  "username": "John"
}
```
- `TEXT_UPDATE`  
  Пример тела сообщения:
```json
{
  "changes": [
    {
      "range": {
        "startLineNumber": 3,
        "startColumn": 1,
        "endLineNumber": 3,
        "endColumn": 5
      },
      "text": "hello world",
      "forceMoveMarkers": false,
      "version": 2
    }
  ],
  "username": "John"
}
```

- `USERS_UPDATE`  
  Пример тела сообщения:
```json
{
  "usersStates": [
    {
      "username": "John",
      "isActive": true,
      "isAdmin": true,
      "color": "#f58231"
    },
    {
      "username": "Jane",
      "isActive": true,
      "isAdmin": false,
      "color": "#008080"
    }
  ]
}
```

- `NEW_COMMENT`
 - После сохранения комментария в бд рассылается всем админам комнаты, что есть новый комментарий

При подключении пользователя, ему отправляется событие `NEW_EDITOR_STATE` и всем пользователям комнаты рассылается событие `USERS_UPDATE`.
При отключении пользователя всем пользователям комнаты рассылается событие `USERS_UPDATE`.

## Создание и управление миграциями

## Управление миграциями БД в Docker

Liquibase не сможет применить миграции к БД с существующими сущностями (попытается создать их снова),
поэтому разумнее на этапе разработки просто удалить базу при выключении docker-compose,
поднять снова и позволить Liquibase применить миграции.
Эта команда удалит volume с БД

```bash
docker-compose down -v
```

### Просмотр примененных миграций

```bash
docker-compose run --rm liquibase history
```

### Откат примененной миграции

Liquibase будет откатывать самый последний changeSet, в том порядке в котором они указаны в db.changelog-master.yaml

```bash
docker-compose run --rm liquibase rollbackCount 1
```

### Применение миграций

Spring автоматически при старте приложение применит/проверит что были применены все миграции из master файла.
Но если нужно повторить этот процесс уже в запущенном приложении

```bash
docker-compose run --rm liquibase update

```

## Локальное управление при разработке

### Создание новой миграции

После внесения изменений в структуру entities в ru.hh.blokshnote.entity
выполнить команду

```bash
mvn clean install liquibase:diff -DskipTests
```
В папке resources/db/changelog/migrations появится файл {timestamp}-outputChangeLog.yaml.
В файл нужно добавить (это нужно для корректного учета уже примененных миграций)

```
- logicalFilePath: db/changelog/migrations/{timestamp}-outputChangeLog.yaml
```

!!! Следует проверить файл миграции на правильность вносимых изменений, тк maven плагин для 
генерации несовершенен и может работать некорректно в редких случаях !!!

Добавить файл с миграцией в master файл - db.changelog-master.yaml, это делается добавлением
в него следующих строк

```
  - include:
      file: migrations/{timestamp}-outputChangeLog.yaml
      relativeToChangelogFile: true
```

После этого нужно применить миграции к БД.
Это происходит автоматически при запуске приложения, можно отключить изменив параметр в application.properties

```
spring.liquibase.enabled=true\false
```

Или можно вызвать команду, запускать приложение не придется

```bash
mvn liquibase:update
```

### Просмотр примененных миграций

Для того чтобы просмотреть историю примененных миграций вызови команду

```bash
mvn liquibase:history
```

Или можно просмотреть историю используя инструменты для БД например DBeaver.
У миграций есть своя отдельная табличка в БД, где хранится история

```postgres-psql
SELECT * FROM databasechangelog
```

### Откат миграции

Если миграция БД неудачна, ее можно откатить используя следующую команду

```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

Для windows

```bash
mvn liquibase:rollback "-Dliquibase.rollbackCount=1"
```

Число в -Dliquibase.rollbackCount=NUM, это число changeSet считая от текущего, которые нужно откатить
В одной миграции может быть несколько changeSet, поэтому если требуется откатить всю миграцию нужно указывать число changeSet в ней
(Например в init миграции 4 changeSet, если я выполню -Dliquibase.rollbackCount=1 откатится только последний из них,
 а 3 остальных останутся).

Также БД можно откатить до какой-то даты

```bash
mvn liquibase:rollback "-Dliquibase.rollbackDate=Jun 03, 2017"
```

База данных откатится до выбранного состояния.
После этого из master файла требуется убрать "- include:" с миграцией,
затем можно удалить файл миграции
