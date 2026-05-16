package com.cqrs.scoreeventprocessor.listener;

import com.cqrs.events.UpdatePointsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
public class KafkaMessageConsumer {

  private final ConcurrentMessageListenerContainer<String, List<UpdatePointsEvent>> messageListenerContainer;

  @Autowired
  public KafkaMessageConsumer(ConcurrentMessageListenerContainer<String, List<UpdatePointsEvent>> messageListenerContainer) {
    this.messageListenerContainer = messageListenerContainer;
  }

  public void startConsuming() {
    messageListenerContainer.start();
  }

  public void stopConsuming() {
    messageListenerContainer.stop();
  }

  @KafkaListener(topics = "scoreUpdate", groupId = "scoreGroup", containerFactory = "kafkaListenerContainerFactory")
  public void listen(List<UpdatePointsEvent> updateEvents) {
    // Process the received list of UpdateEvent objects
    for (UpdatePointsEvent event : updateEvents) {
      System.out.println("Received UpdateEvent: " + event);
      // Process each event as needed
    }
  }
}

