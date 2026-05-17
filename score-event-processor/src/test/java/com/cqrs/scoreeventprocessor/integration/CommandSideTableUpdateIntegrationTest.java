package com.cqrs.scoreeventprocessor.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cqrs.scoreeventprocessor.listener.ScoreEventBatchListener;
import com.cqrs.table.model.PointsTable;
import com.cqrs.table.model.Standing;
import com.cqrs.scoreeventprocessor.processor.ScoreEventsProcessor;
import com.cqrs.scoreeventprocessor.repository.PointsTableRepository;
import com.cqrs.scoreeventprocessor.service.DatastoreTableService;
import com.cqrs.scoreeventprocessor.support.MatchEventFixtures;
import com.cqrs.scoreeventprocessor.util.ReconstructTableUtil;
import com.cqrs.events.UpdatePointsEvent;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommandSideTableUpdateIntegrationTest {

  @Mock private PointsTableRepository repository;

  private DatastoreTableService datastoreTableService;
  private ScoreEventsProcessor processor;
  private ScoreEventBatchListener listener;

  @BeforeEach
  void wireCommandStack() {
    datastoreTableService = new DatastoreTableService(repository);
    ReconstructTableUtil reconstructTableUtil = new ReconstructTableUtil();
    processor = new ScoreEventsProcessor();
    ReflectionTestUtils.setField(processor, "datastoreTableService", datastoreTableService);
    ReflectionTestUtils.setField(processor, "reconstructTableUtil", reconstructTableUtil);
    listener = new ScoreEventBatchListener(processor);
  }

  @Test
  void newLeague_firstMatchResult_persistedWithCorrectStandings() {
    UpdatePointsEvent event = MatchEventFixtures.homeWin("premier-2025", 1, "Arsenal", "Chelsea");
    when(repository.findByTableId("premier-2025")).thenReturn(Optional.empty());
    when(repository.save(any(PointsTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

    listener.listen(event);

    ArgumentCaptor<PointsTable> saved = ArgumentCaptor.forClass(PointsTable.class);
    verify(repository).save(saved.capture());

    PointsTable table = saved.getValue();
    assertThat(table.getTableId()).isEqualTo("premier-2025");
    assertThat(table.getStandings()).hasSize(2);
    assertThat(leader(table).getTeamName()).isEqualTo("Arsenal");
    assertThat(leader(table).getPoints()).isEqualTo(3);
    assertThat(findTeam(table, "Chelsea").getPoints()).isEqualTo(0);
  }

  @Test
  void existingTable_secondMatchResult_mergesIntoPersistedSnapshot() {
    PointsTable existing = new PointsTable();
    existing.setTableId("premier-2025");
    Standing arsenal = standing("Arsenal", 3, 1, 0, 0, 2, 1, 1, 1);
    existing.getStandings().add(arsenal);

    UpdatePointsEvent event = MatchEventFixtures.homeWin("premier-2025", 2, "Arsenal", "Liverpool");
    when(repository.findByTableId("premier-2025")).thenReturn(Optional.of(existing));
    when(repository.save(any(PointsTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

    listener.listen(event);

    ArgumentCaptor<PointsTable> saved = ArgumentCaptor.forClass(PointsTable.class);
    verify(repository).save(saved.capture());

    assertThat(findTeam(saved.getValue(), "Arsenal").getPoints()).isEqualTo(6);
    assertThat(findTeam(saved.getValue(), "Arsenal").getWins()).isEqualTo(2);
    assertThat(findTeam(saved.getValue(), "Liverpool").getPoints()).isEqualTo(0);
  }

  private static Standing leader(PointsTable table) {
    return table.getStandings().stream()
        .filter(s -> s.getRank() == 1)
        .findFirst()
        .orElseThrow();
  }

  private static Standing findTeam(PointsTable table, String name) {
    return table.getStandings().stream()
        .filter(s -> s.getTeamName().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private static Standing standing(
      String name,
      int points,
      int wins,
      int draws,
      int losses,
      int gs,
      int gc,
      int gd,
      int rank) {
    Standing s = new Standing();
    s.setTeamName(name);
    s.setPoints(points);
    s.setWins(wins);
    s.setDraws(draws);
    s.setLosses(losses);
    s.setGoalsScored(gs);
    s.setGoalsConceded(gc);
    s.setGoalDifference(gd);
    s.setRank(rank);
    return s;
  }
}
