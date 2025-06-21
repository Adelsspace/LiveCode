package ru.hh.blokshnote.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class KafkaConfig {
  public static final String WEBSOCKET_TOPIC = "websocket-messages";

  @Bean
  public PartitionFinder finder(ConsumerFactory<String, String> consumerFactory) {
    return new PartitionFinder(consumerFactory);
  }

  public static class PartitionFinder {

    private final ConsumerFactory<String, String> consumerFactory;

    public PartitionFinder(ConsumerFactory<String, String> consumerFactory) {
      this.consumerFactory = consumerFactory;
    }

    public String[] partitions(String topic) {
      try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
        return consumer.partitionsFor(topic).stream()
            .map(pi -> "" + pi.partition())
            .toArray(String[]::new);
      }
    }
  }
}
