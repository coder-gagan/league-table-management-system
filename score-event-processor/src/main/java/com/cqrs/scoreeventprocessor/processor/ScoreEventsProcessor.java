package com.cqrs.scoreeventprocessor.processor;

import com.cqrs.table.model.PointsTable;
import com.cqrs.scoreeventprocessor.service.DatastoreTableService;
import com.cqrs.scoreeventprocessor.util.ReconstructTableUtil;
import java.util.Optional;
import com.cqrs.events.UpdatePointsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScoreEventsProcessor {

  @Autowired private DatastoreTableService datastoreTableService;

  @Autowired private ReconstructTableUtil reconstructTableUtil;

  public void updateTableUsingListenedEvents(UpdatePointsEvent event) {
    Optional<PointsTable> pointsTableOptional = datastoreTableService.getPointsTableById(event.getTableId());// Get from MongoDB
    if(pointsTableOptional.isEmpty()){
      pointsTableOptional = Optional.of(new PointsTable());
    }
    PointsTable pointsTable = pointsTableOptional.get();
    reconstructTableUtil.updateTableForEvent(pointsTable, event);
    datastoreTableService.savePointsTable(pointsTable);
  }
}
