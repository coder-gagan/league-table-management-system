package com.cqrs.scoreeventprocessor.listener;

import static org.mockito.Mockito.verify;

import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.scoreeventprocessor.processor.ScoreEventsProcessor;
import com.cqrs.scoreeventprocessor.support.MatchEventFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoreEventBatchListenerTest {

  @Mock private ScoreEventsProcessor scoreProcessor;

  @InjectMocks private ScoreEventBatchListener listener;

  @Test
  void listen_forwardsScoreUpdateToProcessor() {
    UpdatePointsEvent event = MatchEventFixtures.homeWin("league-1", 1, "HomeFC", "AwayFC");

    listener.listen(event);

    verify(scoreProcessor).updateTableUsingListenedEvents(event);
  }
}
