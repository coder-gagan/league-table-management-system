package com.cqrs.scorekeeper.model.common;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class HeaderView {
	String requestId;
	ZonedDateTime timestamp;
}
