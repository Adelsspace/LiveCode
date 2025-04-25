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

## Создание и управление миграциями

### Создание новой миграции

После внесения изменений в структуру entities в ru.hh.blokshnote.entity
выполнить команду

```bash
mvn clean install liquibase:diff -DskipTests
```
В папке resources/db/changelog/migrations появится файл {timestamp}-outputChangeLog.yaml
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

Или можно просмотреть историю используя инструменты для БД например DBeaver
У миграций есть своя отдельная табличка в БД, где хранится история

```postgres-psql
SELECT * FROM databasechangelog
```

### Откат миграции

Если миграция БД неудача, ее можно откатить используя следующую команду
Число в -Dliquibase.rollbackCount=NUM, это число changeSet считая от текущего, которые нужно откатить
В одной миграции может быть несколько changeSet, поэтому если требуется откатить всю миграцию нужно указывать число changeSet в ней
(Например в init миграции 4 changeSet, если я выполню -Dliquibase.rollbackCount=1 откатится только последний из них,
 а 3 остальных останутся)

```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

Для windows

```bash
mvn liquibase:rollback "-Dliquibase.rollbackCount=1"
```

Также БД можно откатить до какой-то даты

```bash
mvn liquibase:rollback "-Dliquibase.rollbackDate=Jun 03, 2017"
```

База данных откатится до выбранного состояния
После этого из master файла требуется убрать "- include:" с миграцией,
затем можно удалить файл миграции