package ru.hh.blokshnote.unittesting;

import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.hh.blokshnote.config.KafkaConfig;

@EmbeddedKafka(
    topics = {
        KafkaConfig.WEBSOCKET_TOPIC
    }
)
public class WithKafkaAbstractTest extends AbstractIntegrationTest {
}
