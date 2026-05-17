package com.cqrs.tableretriever.integration;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cqrs.tableretriever.controller.TableDetailsController;
import com.cqrs.table.model.PointsTable;
import com.cqrs.table.model.Standing;
import com.cqrs.tableretriever.service.TableRetrieveService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TableDetailsController.class)
class HttpToTableReplayIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TableRetrieveService tableRetrieveService;

  @Test
  void latestTable_returnsStandingsJson() throws Exception {
    PointsTable table = new PointsTable();
    table.setTableId("league-1");
    Standing leader = new Standing();
    leader.setTeamName("Arsenal");
    leader.setRank(1);
    leader.setPoints(6);
    table.getStandings().add(leader);
    when(tableRetrieveService.getLatestPointsTableFromDatastore("league-1"))
        .thenReturn(Optional.of(table));

    mockMvc
        .perform(get("/table/league-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tableId").value("league-1"))
        .andExpect(jsonPath("$.standings[0].teamName").value("Arsenal"))
        .andExpect(jsonPath("$.standings[0].points").value(6));
  }

  @Test
  void unknownTable_returnsNoContent() throws Exception {
    when(tableRetrieveService.getLatestPointsTableFromDatastore("missing"))
        .thenReturn(Optional.empty());

    mockMvc.perform(get("/table/missing")).andExpect(status().isNoContent());
  }

  @Test
  void matchdayEndpoint_delegatesToReplayService() throws Exception {
    PointsTable replayed = new PointsTable();
    replayed.setTableId("league-1");
    when(tableRetrieveService.getPointsTableUptoMatchday("league-1", 5)).thenReturn(replayed);

    mockMvc.perform(get("/table/league-1/matchday/5")).andExpect(status().isOk());

    verify(tableRetrieveService).getPointsTableUptoMatchday(eq("league-1"), eq(5));
  }
}
