package com.fernando.sistema_assinaturas.core.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SubscriptionTest {

	@Test
	void createsActiveMonthlySubscriptionWithPlanPrice() {
		LocalDate startDate = LocalDate.of(2026, 8, 12);

		Subscription subscription = Subscription.create(
			UUID.randomUUID(), UUID.randomUUID(), Plan.PREMIUM, startDate
		);

		assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(subscription.getExpirationDate()).isEqualTo(LocalDate.of(2026, 9, 12));
		assertThat(subscription.getPlan().monthlyPriceCents()).isEqualTo(3_990);
	}

	@Test
	void cancellationKeepsTheEndOfTheCurrentCycle() {
		Subscription subscription = Subscription.create(
			UUID.randomUUID(), UUID.randomUUID(), Plan.BASICO, LocalDate.of(2026, 1, 31)
		);

		Subscription canceled = subscription.cancel();

		assertThat(canceled.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
		assertThat(canceled.getExpirationDate()).isEqualTo(LocalDate.of(2026, 2, 28));
	}

	@Test
	void rejectsCancellationOfNonActiveSubscription() {
		Subscription suspended = Subscription.create(
			UUID.randomUUID(), UUID.randomUUID(), Plan.FAMILIA, LocalDate.now()
		).toBuilder().status(SubscriptionStatus.SUSPENDED).build();

		assertThatThrownBy(suspended::cancel)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Only active subscriptions can be canceled");
	}
}
