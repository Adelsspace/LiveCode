package ru.hh.blokshnote.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

@Service
public class WebsocketMessagesListener {
  private static final Logger log = LoggerFactory.getLogger(WebsocketMessagesListener.class);

  @KafkaListener(topicPartitions = @TopicPartition(topic = "websocket-messages", partitions = "#{@finder.partitions('websocket-messages')}"))
  public void onMessage() {
    log.info("Got new message from kafka websocket-messages");
  }
}
