package com.cqrs.scorekeeper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cqrs.table.model.PointsTable;
import com.cqrs.events.UpdatePointsEvent;
import com.cqrs.scorekeeper.model.request.RecordScoreRequestView;
import com.cqrs.scorekeeper.util.ReconstructTableUtil;
import com.cqrs.scorekeeper.util.UpdateEventsTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
public class ScoreEventsProcessorTest {

	@Mock
	private DatastoreTableService datastoreTableService;

	@Mock
	private ReconstructTableUtil reconstructTableUtil;

	@InjectMocks
	private ScoreEventsProcessor scoreEventsProcessor;

	@Captor
	private ArgumentCaptor<PointsTable> pointsTableCaptor;

	private RecordScoreRequestView recordScoreRequestView;
	private UpdatePointsEvent updatePointsEvent1;
	private UpdatePointsEvent updatePointsEvent2;
	private List<UpdatePointsEvent> updatePointsEvents;

	@BeforeEach
	void setUp() throws IOException {
		// Setup test data
		// Read JSON file
		String json = new String(Files.readAllBytes(Paths.get("src/test/resources/recordScoreRequestView.json")));

		// Use ObjectMapper to convert JSON to object
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		recordScoreRequestView = mapper.readValue(json, RecordScoreRequestView.class);

		updatePointsEvent1 = new UpdatePointsEvent();
		updatePointsEvent1.setTableId("table1");

		updatePointsEvent2 = new UpdatePointsEvent();
		updatePointsEvent2.setTableId("table2");

		updatePointsEvents = List.of(updatePointsEvent1, updatePointsEvent2);

		// Setup mock for static method using MockedStatic (requires Mockito 3.4.0+)
		try (MockedStatic<UpdateEventsTransformer> mockedTransformer = mockStatic(UpdateEventsTransformer.class)) {
			mockedTransformer
					.when(() -> UpdateEventsTransformer.generateUpdateEvents(any(RecordScoreRequestView.class)))
					.thenReturn(updatePointsEvents);
		}
	}

	@Test
	void testPersistUpdateEvents() {
		// Execute
		scoreEventsProcessor.persistUpdateEvents(recordScoreRequestView);

		// Verify
		verify(datastoreTableService, times(2)).getPointsTableById(anyString());
		verify(reconstructTableUtil, times(2)).updateTableForEvent(any(PointsTable.class),
				any(UpdatePointsEvent.class));
		verify(datastoreTableService, times(2)).savePointsTable(any(PointsTable.class));
	}

	@Test
	void testUpdateTableUsingListenedEvents_TableExists() {
		// Setup
		PointsTable existingTable = new PointsTable();
		when(datastoreTableService.getPointsTableById("table1")).thenReturn(Optional.of(existingTable));

		// Execute
		scoreEventsProcessor.updateTableUsingListenedEvents(updatePointsEvent1);

		// Verify
		verify(datastoreTableService).getPointsTableById("table1");
		verify(reconstructTableUtil).updateTableForEvent(eq(existingTable), eq(updatePointsEvent1));
		verify(datastoreTableService).savePointsTable(existingTable);
	}

	@Test
	void testUpdateTableUsingListenedEvents_TableDoesNotExist() {
		// Setup
		when(datastoreTableService.getPointsTableById("table2")).thenReturn(Optional.empty());

		// Execute
		scoreEventsProcessor.updateTableUsingListenedEvents(updatePointsEvent2);

		// Verify
		verify(datastoreTableService).getPointsTableById("table2");
		verify(reconstructTableUtil).updateTableForEvent(pointsTableCaptor.capture(), eq(updatePointsEvent2));
		verify(datastoreTableService).savePointsTable(pointsTableCaptor.getValue());

		// Verify a new PointsTable was created
		PointsTable capturedTable = pointsTableCaptor.getValue();
		assert capturedTable != null;
	}
}