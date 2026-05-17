package com.cqrs.scorekeeper.model.library;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ScoreRecord {
	String tableId;
	String homeTeam;
	Integer homeTeamMatchNumber;
	String awayTeam;
	Integer awayTeamMatchNumber;
	Integer homeTeamGoals;
	Integer awayTeamGoals;
	ZonedDateTime timeStamp;
	Integer gameweek;
}
