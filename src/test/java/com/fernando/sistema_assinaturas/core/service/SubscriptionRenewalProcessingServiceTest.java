package com.fernando.sistema_assinaturas.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttemptStatus;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalPolicy;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.gateway.PaymentGateway;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionRenewalProcessingServiceTest {

	@Mock
	private PaymentGateway paymentGateway;

	private SubscriptionRenewalProcessingService service;

	@BeforeEach
	void setUp() {
		service = new SubscriptionRenewalProcessingService(
			paymentGateway,
			Clock.fixed(Instant.parse("2026-09-12T12:00:00Z"), ZoneOffset.UTC),
			RenewalPolicy.defaultPolicy()
		);
	}

	@Test
	void approvesPaymentAndMovesSubscriptionToNextCycle() {
		Subscription subscription = subscription();
		when(paymentGateway.createCheckout(subscription, subscription.getId() + ":2026-09-12"))
			.thenReturn(new CheckoutResult(PaymentStatus.APPROVED, "https://checkout.test/1", "order-1", "tx-1", "approved"));

		var result = service.process(subscription, 1);

		assertThat(result.succeeded()).isTrue();
		assertThat(result.attempt().getStatus()).isEqualTo(RenewalAttemptStatus.SUCCEEDED);
		assertThat(result.subscription().getExpirationDate()).isEqualTo(LocalDate.of(2026, 10, 12));
		verify(paymentGateway).createCheckout(subscription, subscription.getId() + ":2026-09-12");
	}

	@Test
	void keepsSubscriptionOnPaymentFailure() {
		Subscription subscription = subscription();
		when(paymentGateway.createCheckout(subscription, subscription.getId() + ":2026-09-12"))
			.thenReturn(new CheckoutResult(PaymentStatus.DECLINED, null, "order-1", null, "declined"));

		var result = service.process(subscription, 3);

		assertThat(result.succeeded()).isFalse();
		assertThat(result.attempt().getStatus()).isEqualTo(RenewalAttemptStatus.FAILED);
		assertThat(result.attempt().isFinalFailure(3)).isTrue();
		assertThat(result.subscription().getExpirationDate()).isEqualTo(LocalDate.of(2026, 9, 12));
	}

	private Subscription subscription() {
		return Subscription.create(UUID.randomUUID(), UUID.randomUUID(), Plan.PREMIUM, LocalDate.of(2026, 8, 12));
	}
}
