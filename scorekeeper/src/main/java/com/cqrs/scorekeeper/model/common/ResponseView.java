package com.cqrs.scorekeeper.model.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ResponseView<U, T> {
	U headerView;
	T bodyView;
}
