package ru.hh.blokshnote.unittesting;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.hh.blokshnote.config.KafkaConfig;

@SpringBootTest("spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
public abstract class NoKafkaAbstractIntegrationTest extends AbstractIntegrationTest {
  @MockitoBean
  private KafkaConfig.PartitionFinder partitionFinder;
}
