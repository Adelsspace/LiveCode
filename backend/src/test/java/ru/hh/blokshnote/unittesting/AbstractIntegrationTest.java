package ru.hh.blokshnote.unittesting;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {
  static final String IMAGE = "postgres:latest";

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(IMAGE);
}
