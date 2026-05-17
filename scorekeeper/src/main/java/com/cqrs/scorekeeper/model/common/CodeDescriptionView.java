package com.cqrs.scorekeeper.model.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CodeDescriptionView {
	String code;
	String description;
}
