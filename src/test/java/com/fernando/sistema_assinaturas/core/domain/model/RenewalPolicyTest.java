package com.fernando.sistema_assinaturas.core.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RenewalPolicyTest {

	@Test
	void defaultPolicyAllowsThreeAttempts() {
		assertThat(RenewalPolicy.defaultPolicy().maxAttempts()).isEqualTo(3);
	}

	@Test
	void rejectsNonPositiveMaximumAttempts() {
		assertThatThrownBy(() -> new RenewalPolicy(0))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Maximum renewal attempts must be positive");
	}

	@Test
	void recognizesFinalFailedAttempt() {
		RenewalAttempt attempt = RenewalAttempt.builder()
			.id(UUID.randomUUID())
			.subscriptionId(UUID.randomUUID())
			.renewalDate(LocalDate.of(2026, 8, 12))
			.attemptNumber(3)
			.status(RenewalAttemptStatus.FAILED)
			.idempotencyKey("renewal-key")
			.attemptedAt(Instant.parse("2026-08-12T12:00:00Z"))
			.build();

		assertThat(attempt.isFinalFailure(3)).isTrue();
	}
}
