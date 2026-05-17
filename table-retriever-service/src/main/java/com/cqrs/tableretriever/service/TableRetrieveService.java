package com.cqrs.tableretriever.service;

import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.tableretriever.listener.ScoreEventRetriever;
import com.cqrs.table.model.PointsTable;
import com.cqrs.tableretriever.util.ReconstructTableUtil;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TableRetrieveService {

  @Autowired
  private DatastoreTableService datastoreTableService;

  @Autowired
  private ScoreEventRetriever scoreEventRetriever;

  @Autowired
  private ReconstructTableUtil reconstructTableUtil;

  public Optional<PointsTable> getLatestPointsTableFromDatastore(String tableId) {
    return datastoreTableService.getPointsTableById(tableId);
  }

  @Cacheable(value = "tables", key = "#tableId + '-' + #matchday", condition = "@redisCacheConfig.isCacheEnabled()")
  public PointsTable getPointsTableUptoMatchday(String tableId, int matchday) {
    log.info("Uncached");
    List<UpdatePointsEvent> events = scoreEventRetriever.getEventsUptoMatchday(tableId, matchday);
    return reconstructTableUtil.reconstructTableUsingEvents(events);
  }

  @Cacheable(value = "tables", condition = "@redisCacheConfig.isCacheEnabled()")
  public PointsTable getPointsTableAtInstant(String tableId, ZonedDateTime zonedDateTime) {
    List<UpdatePointsEvent> events = scoreEventRetriever.getEventsPriorToTimestamp(tableId, zonedDateTime);
    return reconstructTableUtil.reconstructTableUsingEvents(events);
  }
}