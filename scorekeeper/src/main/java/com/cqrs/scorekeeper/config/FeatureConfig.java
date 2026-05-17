package com.cqrs.scorekeeper.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class FeatureConfig {

	@Value("${feature.cqrsmode}")
	private boolean cqrsmode;

}
