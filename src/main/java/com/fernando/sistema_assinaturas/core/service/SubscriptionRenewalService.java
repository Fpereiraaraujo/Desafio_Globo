package com.fernando.sistema_assinaturas.core.service;

import com.fernando.sistema_assinaturas.core.domain.model.RenewalResult;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionRenewalService {

	public RenewalResult renew(Subscription subscription, LocalDate renewalDate) {
		if (subscription == null || renewalDate == null) {
			throw new IllegalArgumentException("Subscription and renewal date are required");
		}
		if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
			throw new IllegalStateException("Only active subscriptions can be renewed");
		}
		if (renewalDate.isBefore(subscription.getExpirationDate())) {
			throw new IllegalArgumentException("Subscription is not due for renewal");
		}

		LocalDate renewedUntil = subscription.getExpirationDate().plusMonths(1);
		Subscription renewed = subscription.toBuilder()
			.startDate(subscription.getExpirationDate())
			.expirationDate(renewedUntil)
			.build();
		return new RenewalResult(renewed, renewedUntil);
	}
}
