package com.cqrs.scoreeventprocessor.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.cqrs.scoreeventprocessor.model.PointsTable;
import com.cqrs.scoreeventprocessor.model.Standing;
import java.util.List;
import com.cqrs.events.Record;
import com.cqrs.events.UpdatePointsEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReconstructTableUtilTest {

  private ReconstructTableUtil util;

  @BeforeEach
  void setUp() {
    util = new ReconstructTableUtil();
  }

  @Test
  void updateTableForEvent_createsStandingsForNewTeams_homeWin() {
    UpdatePointsEvent event =
        match(
            "league-1",
            "Arsenal",
            3,
            2,
            1,
            "Chelsea",
            0,
            1,
            2);

    PointsTable table = new PointsTable();
    util.updateTableForEvent(table, event);

    assertThat(table.getTableId()).isEqualTo("league-1");
    assertThat(table.getStandings()).hasSize(2);

    Standing first = table.getStandings().get(0);
    Standing second = table.getStandings().get(1);

    assertThat(first.getRank()).isEqualTo(1);
    assertThat(first.getTeamName()).isEqualTo("Arsenal");
    assertThat(first.getPoints()).isEqualTo(3);
    assertThat(first.getWins()).isEqualTo(1);
    assertThat(first.getLosses()).isEqualTo(0);
    assertThat(first.getDraws()).isEqualTo(0);
    assertThat(first.getGoalsScored()).isEqualTo(2);
    assertThat(first.getGoalsConceded()).isEqualTo(1);
    assertThat(first.getGoalDifference()).isEqualTo(1);

    assertThat(second.getRank()).isEqualTo(2);
    assertThat(second.getTeamName()).isEqualTo("Chelsea");
    assertThat(second.getPoints()).isEqualTo(0);
    assertThat(second.getWins()).isEqualTo(0);
    assertThat(second.getLosses()).isEqualTo(1);
  }

  @Test
  void updateTableForEvent_draw_recordsDrawForBoth() {
    UpdatePointsEvent event =
        match("t1", "A", 1, 1, 1, "B", 1, 1, 1);

    PointsTable table = new PointsTable();
    util.updateTableForEvent(table, event);

    assertThat(table.getStandings()).hasSize(2);
    for (Standing s : table.getStandings()) {
      assertThat(s.getPoints()).isEqualTo(1);
      assertThat(s.getDraws()).isEqualTo(1);
      assertThat(s.getWins()).isEqualTo(0);
      assertThat(s.getLosses()).isEqualTo(0);
    }
  }

  @Test
  void updateTableForEvent_accumulatesSecondFixture() {
    PointsTable table = new PointsTable();
    util.updateTableForEvent(
        table,
        match("t1", "A", 3, 1, 0, "B", 0, 0, 1));
    util.updateTableForEvent(
        table,
        match("t1", "B", 3, 2, 0, "C", 0, 0, 2));

    assertThat(table.getStandings()).hasSize(3);

    Standing a = findStanding(table, "A");
    Standing b = findStanding(table, "B");
    Standing c = findStanding(table, "C");

    assertThat(a.getPoints()).isEqualTo(3);
    assertThat(a.getWins()).isEqualTo(1);

    assertThat(b.getPoints()).isEqualTo(3);
    assertThat(b.getWins()).isEqualTo(1);
    assertThat(b.getLosses()).isEqualTo(1);

    assertThat(c.getPoints()).isEqualTo(0);
    assertThat(c.getLosses()).isEqualTo(1);
  }

  @Test
  void updateTableForEvent_sortsByGoalDifferenceWhenPointsAreEqual() {
    PointsTable table = new PointsTable();
    util.updateTableForEvent(
        table,
        match("t1", "StrongGd", 3, 3, 0, "SideA", 0, 0, 3));
    util.updateTableForEvent(
        table,
        match("t1", "WeakGd", 3, 1, 0, "SideB", 0, 0, 1));

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

    PointsTable table = util.reconstructTableUsingEvents(events);

    assertThat(table.getTableId()).isEqualTo("lid");
    assertThat(table.getStandings()).hasSize(3);
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
