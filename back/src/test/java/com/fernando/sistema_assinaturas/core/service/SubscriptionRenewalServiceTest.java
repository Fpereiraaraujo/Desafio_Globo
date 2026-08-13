package com.fernando.sistema_assinaturas.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SubscriptionRenewalServiceTest {

	private final SubscriptionRenewalService service = new SubscriptionRenewalService();

	@Test
	void renewsSubscriptionForItsDueDate() {
		Subscription subscription = Subscription.create(
			UUID.randomUUID(), UUID.randomUUID(), Plan.PREMIUM, LocalDate.of(2026, 8, 12)
		);

		var result = service.renew(subscription, LocalDate.of(2026, 9, 12));

		assertThat(result.renewedUntil()).isEqualTo(LocalDate.of(2026, 10, 12));
		assertThat(result.subscription().getExpirationDate()).isEqualTo(LocalDate.of(2026, 10, 12));
	}

	@Test
	void rejectsRenewalBeforeDueDate() {
		Subscription subscription = Subscription.create(
			UUID.randomUUID(), UUID.randomUUID(), Plan.BASICO, LocalDate.of(2026, 8, 12)
		);

		assertThatThrownBy(() -> service.renew(subscription, LocalDate.of(2026, 9, 11)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Subscription is not due for renewal");
	}
}
