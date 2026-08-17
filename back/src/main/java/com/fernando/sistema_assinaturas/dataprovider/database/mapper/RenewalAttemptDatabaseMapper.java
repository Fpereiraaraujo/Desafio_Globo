package com.fernando.sistema_assinaturas.dataprovider.database.mapper;

import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttempt;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.RenewalAttemptJpaEntity;

public final class RenewalAttemptDatabaseMapper {

	private RenewalAttemptDatabaseMapper() {
	}

	public static RenewalAttemptJpaEntity toEntity(RenewalAttempt attempt) {
		return RenewalAttemptJpaEntity.builder()
			.id(attempt.getId())
			.subscriptionId(attempt.getSubscriptionId())
			.renewalDate(attempt.getRenewalDate())
			.attemptNumber(attempt.getAttemptNumber())
			.status(attempt.getStatus())
			.idempotencyKey(attempt.getIdempotencyKey())
			.failureReason(attempt.getFailureReason())
			.attemptedAt(attempt.getAttemptedAt())
			.nextRetryAt(attempt.getNextRetryAt())
			.build();
}

	public static RenewalAttempt toDomain(RenewalAttemptJpaEntity entity) {
		return RenewalAttempt.builder()
			.id(entity.getId())
			.subscriptionId(entity.getSubscriptionId())
			.renewalDate(entity.getRenewalDate())
			.attemptNumber(entity.getAttemptNumber())
			.status(entity.getStatus())
			.idempotencyKey(entity.getIdempotencyKey())
			.failureReason(entity.getFailureReason())
			.attemptedAt(entity.getAttemptedAt())
			.nextRetryAt(entity.getNextRetryAt())
			.build();
	}
}
