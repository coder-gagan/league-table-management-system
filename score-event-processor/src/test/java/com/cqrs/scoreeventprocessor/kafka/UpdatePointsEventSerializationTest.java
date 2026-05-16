package com.cqrs.scoreeventprocessor.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.cqrs.scoreeventprocessor.kafka.deserializer.UpdatePointsEventDeserializer;
import com.cqrs.scoreeventprocessor.kafka.serializer.UpdatePointsEventSerializer;
import java.time.Instant;
import com.cqrs.events.Record;
import com.cqrs.events.UpdatePointsEvent;
import org.junit.jupiter.api.Test;

class UpdatePointsEventSerializationTest {

  private final UpdatePointsEventSerializer serializer = new UpdatePointsEventSerializer();
  private final UpdatePointsEventDeserializer deserializer = new UpdatePointsEventDeserializer();

  @Test
  void roundTrip_preservesEventFields() {
    UpdatePointsEvent original =
        UpdatePointsEvent.builder()
            .tableId("league-a")
            .gameweek(4)
            .timeStamp(Instant.parse("2024-01-15T20:00:00Z"))
            .homeTeamDetails(
                Record.builder()
                    .teamName("Alpha")
                    .matchNumber(2)
                    .isHomeGame(true)
                    .points(3)
                    .goalsScored(2)
                    .goalsConceded(0)
                    .build())
            .awayTeamDetails(
                Record.builder()
                    .teamName("Beta")
                    .matchNumber(2)
                    .isHomeGame(false)
                    .points(0)
                    .goalsScored(0)
                    .goalsConceded(2)
                    .build())
            .build();

    byte[] bytes = serializer.serialize("scoreUpdate", original);
    UpdatePointsEvent restored = deserializer.deserialize("scoreUpdate", bytes);

    assertThat(restored).isNotNull();
    assertThat(restored.getTableId()).isEqualTo("league-a");
    assertThat(restored.getGameweek()).isEqualTo(4);
    assertThat(restored.getTimeStamp()).isEqualTo(Instant.parse("2024-01-15T20:00:00Z"));
    assertThat(restored.getHomeTeamDetails().getTeamName()).isEqualTo("Alpha");
    assertThat(restored.getAwayTeamDetails().getTeamName()).isEqualTo("Beta");
    assertThat(restored.getHomeTeamDetails().getPoints()).isEqualTo(3);
    assertThat(restored.getAwayTeamDetails().getPoints()).isEqualTo(0);
  }

  @Test
  void deserialize_invalidPayload_returnsNull() {
    assertThat(deserializer.deserialize("scoreUpdate", new byte[] {0x00, 0x01, 0x02}))
        .isNull();
  }
}
