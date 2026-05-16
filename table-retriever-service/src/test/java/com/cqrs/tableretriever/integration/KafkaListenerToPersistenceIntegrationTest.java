package com.cqrs.tableretriever.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.tableretriever.listener.ScoreEventBatchListener;
import com.cqrs.tableretriever.model.PointsTable;
import com.cqrs.tableretriever.processor.ScoreEventsProcessor;
import com.cqrs.tableretriever.repository.PointsTableRepository;
import com.cqrs.tableretriever.service.DatastoreTableService;
import com.cqrs.tableretriever.support.MatchEventFixtures;
import com.cqrs.tableretriever.util.ReconstructTableUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class KafkaListenerToPersistenceIntegrationTest {

  @Mock private PointsTableRepository repository;

  private ScoreEventBatchListener listener;

  @BeforeEach
  void wireListenerToMongo() {
    DatastoreTableService datastore = new DatastoreTableService(repository);
    ScoreEventsProcessor processor = new ScoreEventsProcessor();
    ReflectionTestUtils.setField(processor, "datastoreTableService", datastore);
    ReflectionTestUtils.setField(processor, "reconstructTableUtil", new ReconstructTableUtil());
    listener = new ScoreEventBatchListener(processor);
  }

  @Test
  void kafkaMessage_updatesReadModelInMongo() {
    UpdatePointsEvent event = MatchEventFixtures.homeWin("league-1", 1, "Arsenal", "Chelsea");
    when(repository.findByTableId("league-1")).thenReturn(Optional.empty());
    when(repository.save(any(PointsTable.class))).thenAnswer(inv -> inv.getArgument(0));

    listener.listen(event);

    ArgumentCaptor<PointsTable> saved = ArgumentCaptor.forClass(PointsTable.class);
    verify(repository).save(saved.capture());
    assertThat(saved.getValue().getTableId()).isEqualTo("league-1");
    assertThat(saved.getValue().getStandings()).hasSize(2);
  }
}
