package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionResponse(
	UUID id,
	UUID userId,
	Plan plan,
	int monthlyPriceCents,
	LocalDate startDate,
	LocalDate expirationDate,
	SubscriptionStatus status
) {

	public static SubscriptionResponse from(Subscription subscription) {
		return new SubscriptionResponse(
			subscription.getId(),
			subscription.getUserId(),
			subscription.getPlan(),
			subscription.getPlan().monthlyPriceCents(),
			subscription.getStartDate(),
			subscription.getExpirationDate(),
			subscription.getStatus()
		);
	}
}
