package ru.hh.blokshnote.unittesting;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {
  static final String IMAGE = "postgres:17.4";

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(IMAGE);
}
