package com.cqrs.scoreeventprocessor.support;

import com.cqrs.events.Record;
import com.cqrs.events.UpdatePointsEvent;
import java.time.Instant;

public final class MatchEventFixtures {

  private MatchEventFixtures() {}

  public static UpdatePointsEvent homeWin(
      String tableId, int gameweek, String home, String away) {
    return fixture(tableId, gameweek, home, 3, 2, 1, away, 0, 1, 2);
  }

  public static UpdatePointsEvent fixture(
      String tableId,
      int gameweek,
      String homeName,
      int homePoints,
      int homeGs,
      int homeGc,
      String awayName,
      int awayPoints,
      int awayGs,
      int awayGc) {
    Instant base = Instant.parse("2025-01-01T12:00:00Z");
    return UpdatePointsEvent.builder()
        .tableId(tableId)
        .gameweek(gameweek)
        .timeStamp(base.plusSeconds(gameweek * 3600L))
        .homeTeamDetails(
            Record.builder()
                .teamName(homeName)
                .points(homePoints)
                .goalsScored(homeGs)
                .goalsConceded(homeGc)
                .matchNumber(gameweek)
                .isHomeGame(true)
                .build())
        .awayTeamDetails(
            Record.builder()
                .teamName(awayName)
                .points(awayPoints)
                .goalsScored(awayGs)
                .goalsConceded(awayGc)
                .matchNumber(gameweek)
                .isHomeGame(false)
                .build())
        .build();
  }
}
