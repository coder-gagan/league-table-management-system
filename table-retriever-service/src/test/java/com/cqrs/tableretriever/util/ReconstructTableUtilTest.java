package com.cqrs.tableretriever.util;

import static org.assertj.core.api.Assertions.assertThat;
import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.tableretriever.model.PointsTable;
import com.cqrs.tableretriever.model.Standing;
import com.cqrs.tableretriever.support.MatchEventFixtures;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReconstructTableUtilTest {

  private ReconstructTableUtil util;

  @BeforeEach
  void setUp() {
    util = new ReconstructTableUtil();
  }

  @Test
  void replayMatchdaySlice_appliesFixturesInOrder() {
    List<UpdatePointsEvent> events =
        List.of(
            MatchEventFixtures.homeWin("league-1", 1, "Arsenal", "Chelsea"),
            MatchEventFixtures.fixture("league-1", 2, "Liverpool", 3, 1, 0, "Arsenal", 0, 0, 1));

    PointsTable table = util.reconstructTableUsingEvents(events);

    assertThat(table.getTableId()).isEqualTo("league-1");
    assertThat(findTeam(table, "Arsenal").getPoints()).isEqualTo(3);
    assertThat(findTeam(table, "Liverpool").getPoints()).isEqualTo(3);
    assertThat(table.getStandings().get(0).getRank()).isEqualTo(1);
  }

  @Test
  void teamsLevelOnPoints_areOrderedByGoalDifference() {
    PointsTable table = new PointsTable();
    util.updateTableForEvent(
        table, MatchEventFixtures.fixture("t1", 1, "StrongGd", 3, 3, 0, "SideA", 0, 0, 3));
    util.updateTableForEvent(
        table, MatchEventFixtures.fixture("t1", 2, "WeakGd", 3, 1, 0, "SideB", 0, 0, 1));

    assertThat(findTeam(table, "StrongGd").getGoalDifference()).isEqualTo(3);
    assertThat(findTeam(table, "WeakGd").getGoalDifference()).isEqualTo(1);
    assertThat(table.getStandings().get(0).getTeamName()).isEqualTo("StrongGd");
    assertThat(table.getStandings().get(1).getTeamName()).isEqualTo("WeakGd");
  }

  private static Standing findTeam(PointsTable table, String name) {
    return table.getStandings().stream()
        .filter(s -> s.getTeamName().equals(name))
        .findFirst()
        .orElseThrow();
  }
}
