package com.cqrs.scorekeeper.model.common;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class RequestView<U, T> {
	U header;
	T body;
}
