package com.cqrs.tableretriever.util;

import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.tableretriever.model.PointsTable;
import com.cqrs.tableretriever.model.Standing;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class ReconstructTableUtil {

  public PointsTable reconstructTableUsingEvents(List<UpdatePointsEvent> events) {
    PointsTable pointsTable = new PointsTable();
    events.forEach(event -> updateTableForEvent(pointsTable, event));
    return pointsTable;
  }

  public void updateTableForEvent(PointsTable pointsTable, UpdatePointsEvent event) {

    Optional<Standing> homeTeamStanding = pointsTable.getStandings()
        .stream()
        .filter(standing -> standing.getTeamName().equals(event.getHomeTeamDetails().getTeamName()))
        .findFirst();

    if(homeTeamStanding.isPresent()){
      homeTeamStanding.get().setPoints(homeTeamStanding.get().getPoints() + event.getHomeTeamDetails().getPoints());
      homeTeamStanding.get().setGoalsScored(homeTeamStanding.get().getGoalsScored() + event.getHomeTeamDetails().getGoalsScored());
      homeTeamStanding.get().setGoalsConceded(homeTeamStanding.get().getGoalsConceded() + event.getHomeTeamDetails().getGoalsConceded());
      switch (event.getHomeTeamDetails().getPoints()) {
        case 3 -> homeTeamStanding.get().setWins(homeTeamStanding.get().getWins() + 1);
        case 1 -> homeTeamStanding.get().setDraws(homeTeamStanding.get().getDraws() + 1);
        case 0 -> homeTeamStanding.get().setLosses(homeTeamStanding.get().getLosses() + 1);
      }
      homeTeamStanding.get().setGoalDifference(homeTeamStanding.get().getGoalsScored() - homeTeamStanding.get().getGoalsConceded());
    } else {
      Standing teamStanding = new Standing();
      teamStanding.setTeamName(event.getHomeTeamDetails().getTeamName());
      teamStanding.setPoints(event.getHomeTeamDetails().getPoints());
      teamStanding.setGoalsScored(event.getHomeTeamDetails().getGoalsScored());
      teamStanding.setGoalsConceded(event.getHomeTeamDetails().getGoalsConceded());
      switch (event.getHomeTeamDetails().getPoints()) {
        case 3 -> teamStanding.setWins(1);
        case 1 -> teamStanding.setDraws(1);
        case 0 -> teamStanding.setLosses(1);
      }
      teamStanding.setGoalDifference(teamStanding.getGoalsScored() - teamStanding.getGoalsConceded());
      pointsTable.getStandings().add(teamStanding);
    }

    // Update away team standings
    Optional<Standing> awayTeamStanding = pointsTable.getStandings()
        .stream()
        .filter(standing -> standing.getTeamName().equals(event.getAwayTeamDetails().getTeamName()))
        .findFirst();

    if(awayTeamStanding.isPresent()){
      awayTeamStanding.get().setPoints(awayTeamStanding.get().getPoints() + event.getAwayTeamDetails().getPoints());
      awayTeamStanding.get().setGoalsScored(awayTeamStanding.get().getGoalsScored() + event.getAwayTeamDetails().getGoalsScored());
      awayTeamStanding.get().setGoalsConceded(awayTeamStanding.get().getGoalsConceded() + event.getAwayTeamDetails().getGoalsConceded());
      switch (event.getAwayTeamDetails().getPoints()) {
        case 3 -> awayTeamStanding.get().setWins(awayTeamStanding.get().getWins() + 1);
        case 1 -> awayTeamStanding.get().setDraws(awayTeamStanding.get().getDraws() + 1);
        case 0 -> awayTeamStanding.get().setLosses(awayTeamStanding.get().getLosses() + 1);
      }
      awayTeamStanding.get().setGoalDifference(awayTeamStanding.get().getGoalsScored() - awayTeamStanding.get().getGoalsConceded());
    } else {
      Standing teamStanding = new Standing();
      teamStanding.setTeamName(event.getAwayTeamDetails().getTeamName());
      teamStanding.setPoints(event.getAwayTeamDetails().getPoints());
      teamStanding.setGoalsScored(event.getAwayTeamDetails().getGoalsScored());
      teamStanding.setGoalsConceded(event.getAwayTeamDetails().getGoalsConceded());
      switch (event.getAwayTeamDetails().getPoints()) {
        case 3 -> teamStanding.setWins(1);
        case 1 -> teamStanding.setDraws(1);
        case 0 -> teamStanding.setLosses(1);
      }
      teamStanding.setGoalDifference(teamStanding.getGoalsScored() - teamStanding.getGoalsConceded());
      pointsTable.getStandings().add(teamStanding);
    }

    // Refresh table rankings — reverse each sort key independently
    pointsTable
        .getStandings()
        .sort(
            Comparator.<Standing>comparingInt(Standing::getPoints)
                .reversed()
                .thenComparing(Comparator.comparingInt(Standing::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(Standing::getGoalsScored).reversed())
                .thenComparingInt(Standing::getGoalsConceded));

    // Update ranking
    AtomicInteger ranking = new AtomicInteger();
    pointsTable.getStandings().forEach(
        standing -> standing.setRank(ranking.incrementAndGet())
    );

    pointsTable.setTableId(event.getTableId());
  }

}
