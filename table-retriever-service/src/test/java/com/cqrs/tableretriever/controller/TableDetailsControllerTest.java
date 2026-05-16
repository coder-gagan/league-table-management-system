package com.cqrs.tableretriever.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.cqrs.tableretriever.model.PointsTable;
import com.cqrs.tableretriever.service.TableRetrieveService;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TableDetailsController.class)
class TableDetailsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TableRetrieveService tableRetrieveService;

  @Test
  void retrieveTable_withoutInstant_readsFromDatastore() throws Exception {
    when(tableRetrieveService.getLatestPointsTableFromDatastore("league-1"))
        .thenReturn(Optional.of(new PointsTable()));

    mockMvc.perform(get("/table/league-1")).andExpect(status().isOk());

    verify(tableRetrieveService).getLatestPointsTableFromDatastore("league-1");
  }

  @Test
  void retrieveTable_withInstant_replaysAtTimestamp() throws Exception {
    when(tableRetrieveService.getPointsTableAtInstant(eq("league-1"), any(ZonedDateTime.class)))
        .thenReturn(new PointsTable());

    mockMvc.perform(get("/table/league-1").param("instant", "1715958000"))
        .andExpect(status().isOk());

    verify(tableRetrieveService)
        .getPointsTableAtInstant(
            eq("league-1"),
            eq(ZonedDateTime.ofInstant(java.time.Instant.ofEpochSecond(1715958000), ZoneId.of("UTC"))));
  }

  @Test
  void retrieveTable_whenUnknown_returnsNoContent() throws Exception {
    when(tableRetrieveService.getLatestPointsTableFromDatastore("unknown"))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(get("/table/unknown"))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
  }

  @Test
  void retrieveTableUptoMatchday() throws Exception {
    when(tableRetrieveService.getPointsTableUptoMatchday("league-1", 10))
        .thenReturn(new PointsTable());

    mockMvc.perform(get("/table/league-1/matchday/10")).andExpect(status().isOk());

    verify(tableRetrieveService).getPointsTableUptoMatchday("league-1", 10);
  }
}
