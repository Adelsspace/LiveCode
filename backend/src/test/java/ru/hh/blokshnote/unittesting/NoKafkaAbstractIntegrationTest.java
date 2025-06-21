package ru.hh.blokshnote.unittesting;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.hh.blokshnote.config.KafkaConfig;
import ru.hh.blokshnote.service.kafka.WebsocketMessagesListener;
import ru.hh.blokshnote.service.kafka.WebsocketMessagesProducer;

@SpringBootTest("spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
public abstract class NoKafkaAbstractIntegrationTest extends AbstractIntegrationTest {
  @MockitoBean
  private KafkaConfig.PartitionFinder partitionFinder;

  @MockitoBean
  private WebsocketMessagesProducer producer;

  @MockitoBean
  private WebsocketMessagesListener listener;
}
