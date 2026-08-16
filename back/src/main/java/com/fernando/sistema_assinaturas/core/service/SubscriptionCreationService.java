package com.fernando.sistema_assinaturas.core.service;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.CreateSubscriptionParam;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionCreationService {

	private final Clock clock;

	public SubscriptionCreationService() {
		this(Clock.systemUTC());
	}

	public SubscriptionCreationService(Clock clock) {
		this.clock = clock;
	}

	public Subscription create(CreateSubscriptionParam param) {
		if (param == null) {
			throw new IllegalArgumentException("Subscription data is required");
		}
		UUID userId = param.userId();
		Plan plan = param.plan();
		return Subscription.createPendingPayment(
			UUID.randomUUID(),
			userId,
			plan,
			LocalDate.now(clock.withZone(ZoneOffset.UTC))
		);
	}
}
