package com.cqrs.scoreeventprocessor.listener;

import com.cqrs.scoreeventprocessor.listener.KafkaMessageConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
public class KafkaConsumerScheduler {

  private final KafkaMessageConsumer kafkaMessageConsumer;

  @Autowired
  public KafkaConsumerScheduler(KafkaMessageConsumer kafkaMessageConsumer) {
    this.kafkaMessageConsumer = kafkaMessageConsumer;
  }

  @Scheduled(fixedRate = 15000) // Run every 15 seconds
  public void consumeFromKafka() {
    kafkaMessageConsumer.startConsuming();
    // Simulate processing time
    try {
      Thread.sleep(5000); // Simulate processing time
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    kafkaMessageConsumer.stopConsuming();
  }
}

