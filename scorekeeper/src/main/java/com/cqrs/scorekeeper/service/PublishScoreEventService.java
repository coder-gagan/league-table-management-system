package com.cqrs.scorekeeper.service;

import static com.cqrs.scorekeeper.util.UpdateEventsTransformer.generateUpdateEvents;

import com.cqrs.scorekeeper.producer.KafkaProducer;
import com.cqrs.scorekeeper.model.common.CodeDescriptionView;
import com.cqrs.scorekeeper.model.common.StatusView;
import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.scorekeeper.model.request.RecordScoreRequestView;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublishScoreEventService {

	@Autowired
	private KafkaProducer<UpdatePointsEvent> kafkaProducer;

	public void publishUpdateEvents(RecordScoreRequestView recordScoreRequestView) {
		// Transform incoming request to update events
		List<UpdatePointsEvent> updatePointsEvents = generateUpdateEvents(recordScoreRequestView);
		// push events to kafka event store
		updatePointsEvents.forEach(updatePointsEvent -> kafkaProducer.sendMessage("scoreUpdate", updatePointsEvent));
	}
}
