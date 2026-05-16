package com.cqrs.scoreeventprocessor.listener;

import com.cqrs.scoreeventprocessor.processor.ScoreEventsProcessor;
import java.time.LocalDateTime;
import com.cqrs.events.UpdatePointsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreEventBatchListener {

   private final ScoreEventsProcessor scoreProcessor;

  @Autowired
   public ScoreEventBatchListener(ScoreEventsProcessor scoreProcessor) {
    this.scoreProcessor = scoreProcessor;
  }

  @KafkaListener(topics = "scoreUpdate", groupId = "scoreGroup", containerFactory = "kafkaListenerContainerFactory")
  public void listen(UpdatePointsEvent updatePointsEvent) {
    System.out.println("Received list of events: " + updatePointsEvent + " at " + LocalDateTime.now());
    scoreProcessor.updateTableUsingListenedEvents(updatePointsEvent);
    System.out.println("Processed " + updatePointsEvent + " events at: " + LocalDateTime.now());
  }
}

