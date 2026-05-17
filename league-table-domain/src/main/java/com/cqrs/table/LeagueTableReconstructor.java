package com.cqrs.table;

import com.cqrs.events.Record;
import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.table.model.PointsTable;
import com.cqrs.table.model.Standing;
import com.cqrs.table.ranking.TableRanking;
import java.util.List;
import java.util.Optional;

public class LeagueTableReconstructor {

  public PointsTable reconstructTableUsingEvents(List<UpdatePointsEvent> events) {
    PointsTable pointsTable = new PointsTable();
    events.forEach(event -> updateTableForEvent(pointsTable, event));
    return pointsTable;
  }

  public void updateTableForEvent(PointsTable pointsTable, UpdatePointsEvent event) {
    applyTeamResult(pointsTable, event.getHomeTeamDetails());
    applyTeamResult(pointsTable, event.getAwayTeamDetails());
    TableRanking.sortAndAssignRanks(pointsTable.getStandings());
    pointsTable.setTableId(event.getTableId());
  }

  private void applyTeamResult(PointsTable pointsTable, Record teamRecord) {
    Optional<Standing> existing =
        pointsTable.getStandings().stream()
            .filter(s -> s.getTeamName().equals(teamRecord.getTeamName()))
            .findFirst();

    if (existing.isPresent()) {
      mergeIntoStanding(existing.get(), teamRecord);
    } else {
      pointsTable.getStandings().add(newStandingFrom(teamRecord));
    }
  }

  private static void mergeIntoStanding(Standing standing, Record teamRecord) {
    standing.setPoints(standing.getPoints() + teamRecord.getPoints());
    standing.setGoalsScored(standing.getGoalsScored() + teamRecord.getGoalsScored());
    standing.setGoalsConceded(standing.getGoalsConceded() + teamRecord.getGoalsConceded());
    applyResultType(standing, teamRecord.getPoints(), true);
    standing.setGoalDifference(standing.getGoalsScored() - standing.getGoalsConceded());
  }

  private static Standing newStandingFrom(Record teamRecord) {
    Standing standing = new Standing();
    standing.setTeamName(teamRecord.getTeamName());
    standing.setPoints(teamRecord.getPoints());
    standing.setGoalsScored(teamRecord.getGoalsScored());
    standing.setGoalsConceded(teamRecord.getGoalsConceded());
    applyResultType(standing, teamRecord.getPoints(), false);
    standing.setGoalDifference(standing.getGoalsScored() - standing.getGoalsConceded());
    return standing;
  }

  private static void applyResultType(Standing standing, int matchPoints, boolean merge) {
    if (merge) {
      switch (matchPoints) {
        case 3 -> standing.setWins(standing.getWins() + 1);
        case 1 -> standing.setDraws(standing.getDraws() + 1);
        case 0 -> standing.setLosses(standing.getLosses() + 1);
        default -> { }
      }
    } else {
      switch (matchPoints) {
        case 3 -> standing.setWins(1);
        case 1 -> standing.setDraws(1);
        case 0 -> standing.setLosses(1);
        default -> { }
      }
    }
  }
}
