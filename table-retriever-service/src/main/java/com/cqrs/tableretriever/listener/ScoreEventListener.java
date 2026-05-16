package com.cqrs.tableretriever.listener;

import com.cqrs.tableretriever.processor.ScoreEventsProcessor;
import org.springframework.beans.factory.annotation.Autowired;

//@Component
public class ScoreEventListener {

  private final ScoreEventsProcessor scoreProcessor;

  @Autowired
  public ScoreEventListener(
      ScoreEventsProcessor scoreProcessor) {
    this.scoreProcessor = scoreProcessor;
  }

  /*@KafkaListener(topics = "scoreUpdate", groupId = "scoreGroup", containerFactory = "kafkaListenerContainerFactory")
  public void listen(UpdatePointsEvent updatePointsEvent) {
    System.out.println("Received message: " + updatePointsEvent);
    // Process the received event (e.g., save to database, perform business logic)
    scoreProcessor.updateTableUsingEvents(updatePointsEvent);
  }*/
}
