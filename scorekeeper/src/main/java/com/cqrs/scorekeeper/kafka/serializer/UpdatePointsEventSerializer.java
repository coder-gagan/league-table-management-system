package com.cqrs.scorekeeper.kafka.serializer;

import com.cqrs.events.UpdatePointsEvent;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import org.apache.kafka.common.serialization.Serializer;

public class UpdatePointsEventSerializer implements Serializer<UpdatePointsEvent> {
	@Override
	public byte[] serialize(String topic, UpdatePointsEvent data) {
		// Convert UpdatePointsEvent to byte array
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(data);
			return bos.toByteArray();
		} catch (Exception e) {
			// Handle serialization error
			e.printStackTrace();
			return null;
		}
	}
}