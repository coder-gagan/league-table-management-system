package com.cqrs.tableretriever.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.tableretriever.listener.ScoreEventRetriever;
import com.cqrs.tableretriever.model.PointsTable;
import com.cqrs.tableretriever.support.MatchEventFixtures;
import com.cqrs.tableretriever.util.ReconstructTableUtil;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TableRetrieveServiceTest {

  @Mock private DatastoreTableService datastoreTableService;
  @Mock private ScoreEventRetriever scoreEventRetriever;

  private final ReconstructTableUtil reconstructTableUtil = new ReconstructTableUtil();
  private TableRetrieveService tableRetrieveService;

  @BeforeEach
  void setUp() {
    tableRetrieveService = new TableRetrieveService();
    ReflectionTestUtils.setField(
        tableRetrieveService, "datastoreTableService", datastoreTableService);
    ReflectionTestUtils.setField(
        tableRetrieveService, "scoreEventRetriever", scoreEventRetriever);
    ReflectionTestUtils.setField(
        tableRetrieveService, "reconstructTableUtil", reconstructTableUtil);
  }

  @Test
  void getLatestPointsTableFromDatastore_returnsSnapshotWhenPresent() {
    PointsTable snapshot = new PointsTable();
    snapshot.setTableId("league-1");
    when(datastoreTableService.getPointsTableById("league-1")).thenReturn(Optional.of(snapshot));

    Optional<PointsTable> result =
        tableRetrieveService.getLatestPointsTableFromDatastore("league-1");

    assertThat(result).contains(snapshot);
  }

  @Test
  void getPointsTableUptoMatchday_replaysKafkaEventsIntoStandings() {
    List<UpdatePointsEvent> events =
        List.of(MatchEventFixtures.homeWin("league-1", 1, "Arsenal", "Chelsea"));
    when(scoreEventRetriever.getEventsUptoMatchday("league-1", 1)).thenReturn(events);

    PointsTable table = tableRetrieveService.getPointsTableUptoMatchday("league-1", 1);

    assertThat(table.getTableId()).isEqualTo("league-1");
    assertThat(table.getStandings()).hasSize(2);
    verify(scoreEventRetriever).getEventsUptoMatchday("league-1", 1);
  }

  @Test
  void getPointsTableAtInstant_replaysEventsBeforeTimestamp() {
    ZonedDateTime instant = ZonedDateTime.of(2025, 3, 1, 15, 0, 0, 0, ZoneId.of("UTC"));
    List<UpdatePointsEvent> events =
        List.of(MatchEventFixtures.homeWin("league-1", 1, "Arsenal", "Chelsea"));
    when(scoreEventRetriever.getEventsPriorToTimestamp("league-1", instant))
        .thenReturn(events);

    PointsTable table = tableRetrieveService.getPointsTableAtInstant("league-1", instant);

    assertThat(table.getStandings())
        .extracting(com.cqrs.tableretriever.model.Standing::getTeamName)
        .contains("Arsenal", "Chelsea");
    verify(scoreEventRetriever).getEventsPriorToTimestamp("league-1", instant);
  }
}
