package com.cqrs.tableretriever.controller;

import com.cqrs.tableretriever.model.PointsTable;
import com.cqrs.tableretriever.service.TableRetrieveService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class TableDetailsController {

  @Autowired private TableRetrieveService tableRetrieveService;

  @GetMapping("/ping")
  public String ping() {
    return "table-retriever service is up and running @" + LocalDateTime.now();
  }

  @GetMapping("/table/{tableId}")
  public ResponseEntity<PointsTable> retrieveTable(
      @PathVariable String tableId,
      @RequestParam(required = false) String instant) {
    if (instant != null && !instant.isBlank()) {
      Instant epoch = Instant.ofEpochSecond(Long.parseLong(instant));
      return ResponseEntity.ok(
          tableRetrieveService.getPointsTableAtInstant(
              tableId, ZonedDateTime.ofInstant(epoch, ZoneId.of("UTC"))));
    }
    Optional<PointsTable> latestPointsTable =
        tableRetrieveService.getLatestPointsTableFromDatastore(tableId);
    return latestPointsTable
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/table/{tableId}/matchday/{matchday}")
  public ResponseEntity<PointsTable> retrieveTableUptoMatchday(@PathVariable String tableId, @PathVariable String matchday){
    ZonedDateTime startTimeStamp = ZonedDateTime.now();
    ResponseEntity<PointsTable> result = ResponseEntity.ok(tableRetrieveService.getPointsTableUptoMatchday(tableId, Integer.parseInt(matchday)));
    log.info("Checkpoint 2: " + Duration.between(startTimeStamp, ZonedDateTime.now()));
    return result;
  }
}