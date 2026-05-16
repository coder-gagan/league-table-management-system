package com.cqrs.scoreeventprocessor.kafka.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import com.cqrs.events.UpdatePointsEvent;
import org.apache.kafka.common.serialization.Deserializer;

public class UpdatePointsEventDeserializer implements Deserializer<UpdatePointsEvent> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public UpdatePointsEvent deserialize(String topic, byte[] data) {
    // Convert byte array to UpdatePointsEvent
    try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis)) {
      return (UpdatePointsEvent) ois.readObject();
    } catch (Exception e) {
      // Handle deserialization error
      e.printStackTrace();
      return null;
    }
  }

  /*@Override
  public List<UpdatePointsEvent> deserialize(String topic, byte[] data) {
    // Convert byte array to UpdatePointsEvent
    try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis)) {
      return (List<UpdatePointsEvent>) ois.readObject();
    } catch (Exception e) {
      // Handle deserialization error
      e.printStackTrace();
      return null;
    }
  }*/
}
