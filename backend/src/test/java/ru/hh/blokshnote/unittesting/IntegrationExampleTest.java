package ru.hh.blokshnote.unittesting;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class IntegrationExampleTest extends AbstractIntegrationTest {
  @PersistenceContext
  private EntityManager entityManager;

  @BeforeEach
  public void cleanDb() {
    // Do smth to clean db
  }

  @Test
  public void testDbConnection() {
    Integer result = (Integer) entityManager.createNativeQuery("SELECT 1").getSingleResult();
    assertThat(result).isEqualTo(1);
  }
}
