package com.cqrs.scorekeeper.producer;

import com.cqrs.scorekeeper.kafka.serializer.UpdatePointsEventSerializer;
import com.cqrs.events.UpdatePointsEvent;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaProducer<T> {

	@Autowired
	private KafkaTemplate<String, UpdatePointsEvent> kafkaTemplate;

	public void sendMessage(String topicName, UpdatePointsEvent updatePointsEvent) {
		kafkaTemplate.send(topicName, updatePointsEvent);
		log.info("Message sent to Kafka topic: " + updatePointsEvent);
	}
}