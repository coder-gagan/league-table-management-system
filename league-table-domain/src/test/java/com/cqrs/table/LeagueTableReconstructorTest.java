package com.cqrs.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.cqrs.events.Record;
import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.table.model.PointsTable;
import com.cqrs.table.model.Standing;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LeagueTableReconstructorTest {

  private LeagueTableReconstructor reconstructor;

  @BeforeEach
  void setUp() {
    reconstructor = new LeagueTableReconstructor();
  }

  @Test
  void updateTableForEvent_createsStandingsForNewTeams_homeWin() {
    UpdatePointsEvent event = match("league-1", "Arsenal", 3, 2, 1, "Chelsea", 0, 1, 2);

    PointsTable table = new PointsTable();
    reconstructor.updateTableForEvent(table, event);

    assertThat(table.getTableId()).isEqualTo("league-1");
    assertThat(table.getStandings()).hasSize(2);

    Standing first = table.getStandings().get(0);
    Standing second = table.getStandings().get(1);

    assertThat(first.getRank()).isEqualTo(1);
    assertThat(first.getTeamName()).isEqualTo("Arsenal");
    assertThat(first.getPoints()).isEqualTo(3);
    assertThat(first.getWins()).isEqualTo(1);
    assertThat(second.getTeamName()).isEqualTo("Chelsea");
    assertThat(second.getPoints()).isEqualTo(0);
    assertThat(second.getLosses()).isEqualTo(1);
  }

  @Test
  void updateTableForEvent_draw_recordsDrawForBoth() {
    PointsTable table = new PointsTable();
    reconstructor.updateTableForEvent(table, match("t1", "A", 1, 1, 1, "B", 1, 1, 1));

    assertThat(table.getStandings()).hasSize(2);
    for (Standing s : table.getStandings()) {
      assertThat(s.getPoints()).isEqualTo(1);
      assertThat(s.getDraws()).isEqualTo(1);
    }
  }

  @Test
  void updateTableForEvent_sortsByGoalDifferenceWhenPointsAreEqual() {
    PointsTable table = new PointsTable();
    reconstructor.updateTableForEvent(
        table, match("t1", "StrongGd", 3, 3, 0, "SideA", 0, 0, 3));
    reconstructor.updateTableForEvent(
        table, match("t1", "WeakGd", 3, 1, 0, "SideB", 0, 0, 1));

    assertThat(table.getStandings())
        .extracting(Standing::getTeamName, Standing::getPoints, Standing::getGoalDifference)
        .containsExactly(
            tuple("StrongGd", 3, 3),
            tuple("WeakGd", 3, 1),
            tuple("SideB", 0, -1),
            tuple("SideA", 0, -3));
  }

  @Test
  void reconstructTableUsingEvents_appliesEventsInOrder() {
    List<UpdatePointsEvent> events =
        List.of(
            match("lid", "X", 3, 1, 0, "Y", 0, 0, 1),
            match("lid", "Y", 3, 2, 0, "Z", 0, 0, 2));

    PointsTable table = reconstructor.reconstructTableUsingEvents(events);

    assertThat(table.getTableId()).isEqualTo("lid");
    assertThat(findStanding(table, "X").getPoints()).isEqualTo(3);
    assertThat(findStanding(table, "Y").getPoints()).isEqualTo(3);
    assertThat(findStanding(table, "Z").getPoints()).isEqualTo(0);
  }

  private static Standing findStanding(PointsTable table, String teamName) {
    return table.getStandings().stream()
        .filter(s -> s.getTeamName().equals(teamName))
        .findFirst()
        .orElseThrow();
  }

  private static UpdatePointsEvent match(
      String tableId,
      String homeName,
      int homePoints,
      int homeGs,
      int homeGc,
      String awayName,
      int awayPoints,
      int awayGs,
      int awayGc) {
    return UpdatePointsEvent.builder()
        .tableId(tableId)
        .homeTeamDetails(
            Record.builder()
                .teamName(homeName)
                .points(homePoints)
                .goalsScored(homeGs)
                .goalsConceded(homeGc)
                .matchNumber(0)
                .isHomeGame(true)
                .build())
        .awayTeamDetails(
            Record.builder()
                .teamName(awayName)
                .points(awayPoints)
                .goalsScored(awayGs)
                .goalsConceded(awayGc)
                .matchNumber(0)
                .isHomeGame(false)
                .build())
        .build();
  }
}
