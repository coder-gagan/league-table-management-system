package com.cqrs.scorekeeper.model.request;

import com.cqrs.scorekeeper.model.library.ScoreRecord;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RecordScoreRequestView {
	List<ScoreRecord> scoreRecords;
}
