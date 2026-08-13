package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetUserSubscriptionResponseTest {

	@Test
	void mapsSubscriptionDetailsForUserQuery() {
		Subscription subscription = Subscription.create(
			UUID.randomUUID(), UUID.randomUUID(), Plan.FAMILIA, LocalDate.of(2026, 8, 12)
		);

		GetUserSubscriptionResponse response = GetUserSubscriptionResponse.from(subscription);

		assertThat(response.plan()).isEqualTo("FAMILIA");
		assertThat(response.monthlyPriceCents()).isEqualTo(5_990);
		assertThat(response.status()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(response.canceledAt()).isNull();
	}
}
