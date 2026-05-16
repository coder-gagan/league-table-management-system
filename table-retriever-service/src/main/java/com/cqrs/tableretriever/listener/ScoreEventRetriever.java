package com.cqrs.tableretriever.listener;

import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.tableretriever.config.redis.RedisCacheConfig;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScoreEventRetriever {

  private final RedisCacheConfig redisCacheConfig;

  private final ConsumerFactory<String, UpdatePointsEvent> consumerFactory;

  @Autowired
  public ScoreEventRetriever(RedisCacheConfig redisCacheConfig, @Qualifier("scoreOnDemandConsumerFactory") ConsumerFactory<String, UpdatePointsEvent> consumerFactory) {
    this.redisCacheConfig = redisCacheConfig;
    this.consumerFactory = consumerFactory;
  }

  public List<UpdatePointsEvent> getEventsUptoMatchday(String tableId, int matchday) {
    Consumer<String, UpdatePointsEvent> consumer = consumerFactory.createConsumer("score-retriever-" + UUID.randomUUID());
    consumer.subscribe(Collections.singletonList("scoreUpdate"));

    // Wait until partitions are assigned before proceeding
    consumer.poll(Duration.ofMillis(100)); // Keep polling to trigger assignment

    // Ensure we start from the earliest offset
    consumer.seekToBeginning(consumer.assignment());

    List<UpdatePointsEvent> events = new ArrayList<>();
    int emptyPollCount = 0;
    int maxEmptyPolls = 5; // Allow some retries before stopping

    while (emptyPollCount < maxEmptyPolls) {
      ConsumerRecords<String, UpdatePointsEvent> records = consumer.poll(Duration.ofMillis(500)); // Increase timeout

      if (records.isEmpty()) {
        emptyPollCount++;
        continue;
      }

      for (ConsumerRecord<String, UpdatePointsEvent> record : records) {
        if (record.value().getGameweek() <= matchday) {
          events.add(record.value());
        }
      }
    }

    consumer.close();

    // Filter by tableId
    events = events.stream()
        .filter(event -> tableId.equals(event.getTableId()))
        .sorted(Comparator.comparing(UpdatePointsEvent::getTimeStamp)) // Sort by timestamp
        .collect(Collectors.toList());

    return events;
  }

  public List<UpdatePointsEvent> getEventsPriorToTimestamp(String tableId, ZonedDateTime zonedDateTime) {
    Instant timestamp = zonedDateTime.toInstant();

    Consumer<String, UpdatePointsEvent> consumer = consumerFactory.createConsumer("score-retriever-" + UUID.randomUUID());
    consumer.subscribe(Collections.singletonList("scoreUpdate"));

    // Seek to the beginning of each partition to start fetching from the earliest offset
    consumer.poll(Duration.ofMillis(0));
    consumer.seekToBeginning(consumer.assignment());

    List<UpdatePointsEvent> events = new ArrayList<>();

    boolean stopFetching = false;

    while (!stopFetching) {
      ConsumerRecords<String, UpdatePointsEvent> records = consumer.poll(Duration.ofMillis(100));
      boolean foundRecordAfterTimestamp = false;

      for (ConsumerRecord<String, UpdatePointsEvent> record : records) {
        // Check if the event's timestamp is less than the desired timestamp
        if (record.value().getTimeStamp().isBefore(timestamp)) {
          events.add(record.value());
        } else {
          // If an event with timestamp >= desired timestamp is found, set flag
          foundRecordAfterTimestamp = true;
        }
      }

      // Exit the loop if no more records or if we found a record with timestamp >= desired timestamp
      if (records.isEmpty() || foundRecordAfterTimestamp) {
        stopFetching = true;
      }
    }

    consumer.close();

    // Filter events that belong to the tableId
    events = events.stream().filter(event -> tableId.equals(event.getTableId())).collect(Collectors.toList());

    // Sort events based on timestamp (optional, depending on your requirements)
    events.sort(Comparator.comparing(UpdatePointsEvent::getTimeStamp));

    return events;
  }
}
