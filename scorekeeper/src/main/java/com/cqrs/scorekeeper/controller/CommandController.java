package com.cqrs.scorekeeper.controller;

import static com.cqrs.scorekeeper.util.UpdateEventsTransformer.generateUpdateEvents;

import com.cqrs.scorekeeper.config.FeatureConfig;
import com.cqrs.scorekeeper.model.common.CodeDescriptionView;
import com.cqrs.scorekeeper.model.common.HeaderView;
import com.cqrs.scorekeeper.model.common.RequestView;
import com.cqrs.scorekeeper.model.common.ResponseView;
import com.cqrs.scorekeeper.model.common.StatusView;
import com.cqrs.scorekeeper.model.request.RecordScoreRequestView;
import com.cqrs.scorekeeper.service.PublishScoreEventService;
import com.cqrs.scorekeeper.service.ScoreEventsProcessor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommandController {

	@Autowired
	private final PublishScoreEventService publishScoreEventService;

	@Autowired
	private final ScoreEventsProcessor scoreEventsProcessor;

	@Autowired
	private final FeatureConfig featureConfig;

	public CommandController(PublishScoreEventService publishScoreEventService,
			ScoreEventsProcessor scoreEventsProcessor, FeatureConfig featureConfig) {
		this.publishScoreEventService = publishScoreEventService;
		this.scoreEventsProcessor = scoreEventsProcessor;
		this.featureConfig = featureConfig;
	}

	@PostMapping(path = "score", consumes = "application/json", produces = "application/json")
	public ResponseView<HeaderView, StatusView> recordScore(
			@RequestBody RequestView<HeaderView, RecordScoreRequestView> requestView) {
		StatusView responseBody;
		try {
			if (featureConfig.isCqrsmode()) {
				publishScoreEventService.publishUpdateEvents(requestView.getBody());
			} else {
				scoreEventsProcessor.persistUpdateEvents(requestView.getBody());
			}
			responseBody = StatusView.builder().statusCode(200).build();
		} catch (Exception e) {
			responseBody = StatusView.builder().statusCode(500).errors(getErrors(e)).build();
		}

		return ResponseView.<HeaderView, StatusView>builder().headerView(requestView.getHeader()).bodyView(responseBody)
				.build();
	}

	private List<CodeDescriptionView> getErrors(Exception e) {
		List<CodeDescriptionView> errors = new ArrayList<>();
		errors.add(CodeDescriptionView.builder().description(e.getMessage()).build());
		return errors;
	}

	@GetMapping("/ping")
	public String ping() {
		return "scorekeeper service is up and running @ " + LocalDateTime.now();
	}

}
