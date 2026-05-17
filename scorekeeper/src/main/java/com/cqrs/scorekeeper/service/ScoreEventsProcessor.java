package com.cqrs.scorekeeper.service;

import static com.cqrs.scorekeeper.util.UpdateEventsTransformer.generateUpdateEvents;

import com.cqrs.table.model.PointsTable;
import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.scorekeeper.model.request.RecordScoreRequestView;
import com.cqrs.scorekeeper.util.ReconstructTableUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScoreEventsProcessor {

	@Autowired
	private DatastoreTableService datastoreTableService;

	@Autowired
	private ReconstructTableUtil reconstructTableUtil;

	public void persistUpdateEvents(RecordScoreRequestView recordScoreRequestView) {
		// Transform incoming request to update events
		List<UpdatePointsEvent> updatePointsEvents = generateUpdateEvents(recordScoreRequestView);
		// push events to kafka event store
		updatePointsEvents.forEach(this::updateTableUsingListenedEvents);
	}

	public void updateTableUsingListenedEvents(UpdatePointsEvent event) {
		Optional<PointsTable> pointsTableOptional = datastoreTableService.getPointsTableById(event.getTableId());// Get
																													// from
																													// MongoDB
		if (pointsTableOptional.isEmpty()) {
			pointsTableOptional = Optional.of(new PointsTable());
		}
		PointsTable pointsTable = pointsTableOptional.get();
		reconstructTableUtil.updateTableForEvent(pointsTable, event);
		datastoreTableService.savePointsTable(pointsTable);
	}
}
