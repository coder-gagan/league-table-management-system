package com.cqrs.table.ranking;

import com.cqrs.table.model.Standing;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Football league table ordering: points, goal difference, goals scored, goals conceded. */
public final class TableRanking {

  private TableRanking() {}

  public static Comparator<Standing> comparator() {
    return Comparator.<Standing>comparingInt(Standing::getPoints)
        .reversed()
        .thenComparing(Comparator.comparingInt(Standing::getGoalDifference).reversed())
        .thenComparing(Comparator.comparingInt(Standing::getGoalsScored).reversed())
        .thenComparingInt(Standing::getGoalsConceded);
  }

  public static void sortAndAssignRanks(List<Standing> standings) {
    standings.sort(comparator());
    AtomicInteger rank = new AtomicInteger();
    standings.forEach(standing -> standing.setRank(rank.incrementAndGet()));
  }
}
