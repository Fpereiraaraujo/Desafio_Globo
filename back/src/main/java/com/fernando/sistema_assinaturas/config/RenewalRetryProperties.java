package com.fernando.sistema_assinaturas.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "subscriptions.renewal.retry")
public record RenewalRetryProperties(
	Duration firstDelay,
	Duration secondDelay,
	Duration pendingTimeout
) {
	public RenewalRetryProperties {
		firstDelay = firstDelay == null ? Duration.ofMinutes(30) : firstDelay;
		secondDelay = secondDelay == null ? Duration.ofHours(24) : secondDelay;
		pendingTimeout = pendingTimeout == null ? Duration.ofMinutes(30) : pendingTimeout;
	}

	public Duration delayForFailure(int attemptNumber) {
		return attemptNumber == 1 ? firstDelay : secondDelay;
	}
}
