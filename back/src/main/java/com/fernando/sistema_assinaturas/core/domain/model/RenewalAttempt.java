package com.fernando.sistema_assinaturas.core.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class RenewalAttempt {

	UUID id;
	UUID subscriptionId;
	LocalDate renewalDate;
	int attemptNumber;
	RenewalAttemptStatus status;
	String idempotencyKey;
	String failureReason;
	Instant attemptedAt;

	public boolean isFinalFailure(int maxAttempts) {
		return status == RenewalAttemptStatus.FAILED && attemptNumber >= maxAttempts;
	}
}
