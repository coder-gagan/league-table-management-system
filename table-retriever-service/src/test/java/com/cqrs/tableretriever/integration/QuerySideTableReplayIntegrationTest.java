package com.cqrs.tableretriever.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.tableretriever.listener.ScoreEventRetriever;
import com.cqrs.table.model.PointsTable;
import com.cqrs.tableretriever.service.TableRetrieveService;
import com.cqrs.tableretriever.support.MatchEventFixtures;
import com.cqrs.tableretriever.util.ReconstructTableUtil;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QuerySideTableReplayIntegrationTest {

  @Mock private ScoreEventRetriever scoreEventRetriever;

  private TableRetrieveService tableRetrieveService;

  @BeforeEach
  void wireQueryReplayStack() {
    tableRetrieveService = new TableRetrieveService();
    ReflectionTestUtils.setField(
        tableRetrieveService, "scoreEventRetriever", scoreEventRetriever);
    ReflectionTestUtils.setField(
        tableRetrieveService, "reconstructTableUtil", new ReconstructTableUtil());
  }

  @Test
  void matchdayReplay_endToEndStandingsMatchKafkaHistory() {
    List<UpdatePointsEvent> history =
        List.of(
            MatchEventFixtures.homeWin("league-1", 1, "Arsenal", "Chelsea"),
            MatchEventFixtures.homeWin("league-1", 2, "Liverpool", "Arsenal"));
    when(scoreEventRetriever.getEventsUptoMatchday("league-1", 2)).thenReturn(history);

    PointsTable table = tableRetrieveService.getPointsTableUptoMatchday("league-1", 2);

    assertThat(table.getTableId()).isEqualTo("league-1");
    assertThat(table.getStandings()).hasSize(3);
    assertThat(teamPoints(table, "Arsenal")).isEqualTo(3);
    assertThat(teamPoints(table, "Liverpool")).isEqualTo(3);
    assertThat(teamPoints(table, "Chelsea")).isEqualTo(0);
  }

  @Test
  void instantReplay_excludesFixturesOnOrAfterCutoff() {
    ZonedDateTime cutoff = ZonedDateTime.of(2025, 2, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
    List<UpdatePointsEvent> history =
        List.of(MatchEventFixtures.homeWin("league-1", 1, "Arsenal", "Chelsea"));
    when(scoreEventRetriever.getEventsPriorToTimestamp("league-1", cutoff))
        .thenReturn(history);

    PointsTable table = tableRetrieveService.getPointsTableAtInstant("league-1", cutoff);

    assertThat(table.getStandings()).hasSize(2);
    assertThat(teamPoints(table, "Arsenal")).isEqualTo(3);
  }

  private static int teamPoints(PointsTable table, String team) {
    return table.getStandings().stream()
        .filter(s -> s.getTeamName().equals(team))
        .findFirst()
        .orElseThrow()
        .getPoints();
  }
}
