package com.cqrs.tableretriever.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.tableretriever.config.redis.RedisCacheConfig;
import com.cqrs.tableretriever.support.MatchEventFixtures;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.ConsumerFactory;

@ExtendWith(MockitoExtension.class)
class ScoreEventRetrieverTest {

  private static final TopicPartition PARTITION = new TopicPartition("scoreUpdate", 0);

  @Mock private RedisCacheConfig redisCacheConfig;
  @Mock private ConsumerFactory<String, UpdatePointsEvent> consumerFactory;
  @Mock private Consumer<String, UpdatePointsEvent> consumer;

  private ScoreEventRetriever retriever;

  @BeforeEach
  void setUp() {
    when(consumerFactory.createConsumer(anyString())).thenReturn(consumer);
    when(consumer.assignment()).thenReturn(Set.of(PARTITION));
    retriever = new ScoreEventRetriever(redisCacheConfig, consumerFactory);
  }

  @Test
  void getEventsUptoMatchday_keepsOnlyRequestedTableAndGameweek() {
    UpdatePointsEvent inScope =
        MatchEventFixtures.homeWin("league-a", 3, "Home", "Away");
    UpdatePointsEvent futureGameweek =
        MatchEventFixtures.homeWin("league-a", 8, "X", "Y");
    UpdatePointsEvent otherLeague =
        MatchEventFixtures.homeWin("league-b", 2, "P", "Q");

    when(consumer.poll(any(Duration.class)))
        .thenReturn(
            ConsumerRecords.empty(),
            records(inScope, futureGameweek, otherLeague),
            ConsumerRecords.empty(),
            ConsumerRecords.empty(),
            ConsumerRecords.empty(),
            ConsumerRecords.empty(),
            ConsumerRecords.empty());

    List<UpdatePointsEvent> result = retriever.getEventsUptoMatchday("league-a", 5);

    assertThat(result).containsExactly(inScope);
    verify(consumer).subscribe(List.of("scoreUpdate"));
    verify(consumer).seekToBeginning(Set.of(PARTITION));
    verify(consumer).close();
  }

  @Test
  void getEventsPriorToTimestamp_stopsAtCutoffAndFiltersByTable() {
    Instant cutoff = Instant.parse("2025-03-01T15:00:00Z");
    UpdatePointsEvent before =
        MatchEventFixtures.atInstant(
            "league-a", 1, cutoff.minusSeconds(3600), "Home", 3, "Away", 0);
    UpdatePointsEvent atCutoff =
        MatchEventFixtures.atInstant("league-a", 2, cutoff, "X", 1, "Y", 1);
    UpdatePointsEvent otherLeague =
        MatchEventFixtures.atInstant(
            "league-b", 1, cutoff.minusSeconds(7200), "P", 3, "Q", 0);

    when(consumer.poll(any(Duration.class)))
        .thenReturn(
            ConsumerRecords.empty(),
            records(before, atCutoff, otherLeague));

    List<UpdatePointsEvent> result =
        retriever.getEventsPriorToTimestamp(
            "league-a", ZonedDateTime.ofInstant(cutoff, ZoneId.of("UTC")));

    assertThat(result).containsExactly(before);
    verify(consumer).close();
  }

  @SafeVarargs
  private static ConsumerRecords<String, UpdatePointsEvent> records(
      UpdatePointsEvent... events) {
    List<ConsumerRecord<String, UpdatePointsEvent>> records =
        Arrays.stream(events)
            .map(e -> new ConsumerRecord<>("scoreUpdate", 0, 0L, e.getTableId(), e))
            .toList();
    return new ConsumerRecords<>(Map.of(PARTITION, records));
  }
}
