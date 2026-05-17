package com.cqrs.scorekeeper.util;

import com.cqrs.events.Record;
import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.scorekeeper.model.library.ScoreRecord;
import com.cqrs.scorekeeper.model.request.RecordScoreRequestView;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateEventsTransformer {

	public static List<UpdatePointsEvent> generateUpdateEvents(RecordScoreRequestView recordScoreRequestView) {
		List<UpdatePointsEvent> updatePointsEvents = new ArrayList<>();
		recordScoreRequestView.getScoreRecords().forEach(scoreRecord -> {
			updatePointsEvents.add(UpdatePointsEvent.builder()
					.timeStamp(ZonedDateTime.now(ZoneId.of("UTC")).toInstant()).tableId(scoreRecord.getTableId())
					.gameweek(scoreRecord.getGameweek()).homeTeamDetails(populateTeamDetails(scoreRecord, true))
					.awayTeamDetails(populateTeamDetails(scoreRecord, false)).build());
		});
		// Transform input request to consolidated points update event
		return updatePointsEvents;
	}

	private static Record populateTeamDetails(ScoreRecord scoreRecord, boolean isHomeTeam) {

		int points = 0;
		int selfScore = isHomeTeam ? scoreRecord.getHomeTeamGoals() : scoreRecord.getAwayTeamGoals();
		int opposingScore = isHomeTeam ? scoreRecord.getAwayTeamGoals() : scoreRecord.getHomeTeamGoals();
		if (selfScore > opposingScore) {
			points = 3;
		} else if (Objects.equals(selfScore, opposingScore)) {
			points = 1;
		}

		return Record.builder().teamName(isHomeTeam ? scoreRecord.getHomeTeam() : scoreRecord.getAwayTeam())
				.goalsScored(isHomeTeam ? scoreRecord.getHomeTeamGoals() : scoreRecord.getAwayTeamGoals())
				.goalsConceded(isHomeTeam ? scoreRecord.getAwayTeamGoals() : scoreRecord.getHomeTeamGoals())
				.points(points).isHomeGame(isHomeTeam)
				.matchNumber(isHomeTeam ? scoreRecord.getHomeTeamMatchNumber() : scoreRecord.getAwayTeamMatchNumber())
				.build();
	}
}