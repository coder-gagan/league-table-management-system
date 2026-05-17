package com.cqrs.scorekeeper.model.common;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class StatusView {
	private Integer statusCode;
	private List<CodeDescriptionView> errors;
	private List<CodeDescriptionView> warnings;
}
