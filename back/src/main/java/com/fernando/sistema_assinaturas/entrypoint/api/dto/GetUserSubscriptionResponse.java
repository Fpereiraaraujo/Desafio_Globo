package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GetUserSubscriptionResponse(
	UUID id,
	UUID userId,
	String plan,
	int monthlyPriceCents,
	LocalDate startDate,
	LocalDate expirationDate,
	SubscriptionStatus status,
	Instant canceledAt
) {

	public static GetUserSubscriptionResponse from(Subscription subscription) {
		return new GetUserSubscriptionResponse(
			subscription.getId(),
			subscription.getUserId(),
			subscription.getPlan().name(),
			subscription.getPlan().monthlyPriceCents(),
			subscription.getStartDate(),
			subscription.getExpirationDate(),
			subscription.getStatus(),
			subscription.getCanceledAt()
		);
	}
}
