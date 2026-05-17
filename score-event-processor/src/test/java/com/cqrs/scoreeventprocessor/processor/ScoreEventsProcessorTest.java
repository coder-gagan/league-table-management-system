package com.cqrs.scoreeventprocessor.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cqrs.table.model.PointsTable;
import com.cqrs.table.model.Standing;
import com.cqrs.scoreeventprocessor.service.DatastoreTableService;
import com.cqrs.scoreeventprocessor.util.ReconstructTableUtil;
import java.util.Optional;
import com.cqrs.events.Record;
import com.cqrs.events.UpdatePointsEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoreEventsProcessorTest {

  @Mock private DatastoreTableService datastoreTableService;

  @Spy private ReconstructTableUtil reconstructTableUtil = new ReconstructTableUtil();

  @InjectMocks private ScoreEventsProcessor scoreEventsProcessor;

  @Test
  void updateTableUsingListenedEvents_createsNewTableWhenMissing() {
    UpdatePointsEvent event = sampleEvent("new-table");
    when(datastoreTableService.getPointsTableById("new-table")).thenReturn(Optional.empty());

    scoreEventsProcessor.updateTableUsingListenedEvents(event);

    ArgumentCaptor<PointsTable> captor = ArgumentCaptor.forClass(PointsTable.class);
    verify(datastoreTableService).savePointsTable(captor.capture());
    PointsTable saved = captor.getValue();

    assertThat(saved.getTableId()).isEqualTo("new-table");
    assertThat(saved.getStandings()).hasSize(2);
    assertThat(findTeam(saved, "HomeFC").getPoints()).isEqualTo(3);
    assertThat(findTeam(saved, "AwayFC").getPoints()).isEqualTo(0);
  }

  @Test
  void updateTableUsingListenedEvents_mergesIntoExistingTable() {
    PointsTable existing = new PointsTable();
    existing.setTableId("tid");
    Standing home = new Standing();
    home.setTeamName("HomeFC");
    home.setPoints(3);
    home.setWins(1);
    home.setDraws(0);
    home.setLosses(0);
    home.setGoalsScored(1);
    home.setGoalsConceded(0);
    home.setGoalDifference(1);
    home.setRank(1);
    existing.getStandings().add(home);

    UpdatePointsEvent event = sampleEvent("tid");
    when(datastoreTableService.getPointsTableById("tid")).thenReturn(Optional.of(existing));

    scoreEventsProcessor.updateTableUsingListenedEvents(event);

    ArgumentCaptor<PointsTable> captor = ArgumentCaptor.forClass(PointsTable.class);
    verify(datastoreTableService).savePointsTable(captor.capture());

    Standing mergedHome = findTeam(captor.getValue(), "HomeFC");
    assertThat(mergedHome.getPoints()).isEqualTo(6);
    assertThat(mergedHome.getWins()).isEqualTo(2);
    assertThat(findTeam(captor.getValue(), "AwayFC").getPoints()).isEqualTo(0);
  }

  private static Standing findTeam(PointsTable table, String name) {
    return table.getStandings().stream()
        .filter(s -> s.getTeamName().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private static UpdatePointsEvent sampleEvent(String tableId) {
    return UpdatePointsEvent.builder()
        .tableId(tableId)
        .homeTeamDetails(
            Record.builder()
                .teamName("HomeFC")
                .points(3)
                .goalsScored(2)
                .goalsConceded(1)
                .matchNumber(1)
                .isHomeGame(true)
                .build())
        .awayTeamDetails(
            Record.builder()
                .teamName("AwayFC")
                .points(0)
                .goalsScored(1)
                .goalsConceded(2)
                .matchNumber(1)
                .isHomeGame(false)
                .build())
        .build();
  }
}
