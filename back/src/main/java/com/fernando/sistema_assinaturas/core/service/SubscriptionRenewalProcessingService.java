package com.fernando.sistema_assinaturas.core.service;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentType;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttempt;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttemptStatus;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalProcessingResult;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalPolicy;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.gateway.RenewalPaymentGateway;
import com.fernando.sistema_assinaturas.config.RenewalRetryProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SubscriptionRenewalProcessingService {

	private final RenewalPaymentGateway paymentGateway;
	private final Clock clock;
	private final RenewalPolicy renewalPolicy;
	private final SubscriptionRenewalService subscriptionRenewalService;
	private final RenewalRetryProperties retryProperties;

	@Autowired
	public SubscriptionRenewalProcessingService(
		RenewalPaymentGateway paymentGateway,
		SubscriptionRenewalService subscriptionRenewalService,
		RenewalRetryProperties retryProperties
	) {
		this(paymentGateway, Clock.systemUTC(), RenewalPolicy.defaultPolicy(), subscriptionRenewalService, retryProperties);
	}

	public SubscriptionRenewalProcessingService(RenewalPaymentGateway paymentGateway, Clock clock, RenewalPolicy renewalPolicy) {
		this(paymentGateway, clock, renewalPolicy, new SubscriptionRenewalService(), new RenewalRetryProperties(null, null, null));
	}

	public SubscriptionRenewalProcessingService(
		RenewalPaymentGateway paymentGateway,
		Clock clock,
		RenewalPolicy renewalPolicy,
		SubscriptionRenewalService subscriptionRenewalService,
		RenewalRetryProperties retryProperties
	) {
		this.paymentGateway = paymentGateway;
		this.clock = clock;
		this.renewalPolicy = renewalPolicy;
		this.subscriptionRenewalService = subscriptionRenewalService;
		this.retryProperties = retryProperties;
	}

	public RenewalProcessingResult process(Subscription subscription, int attemptNumber) {
		if (subscription == null) {
			throw new IllegalArgumentException("Subscription is required");
		}
		if (attemptNumber < 1 || attemptNumber > renewalPolicy.maxAttempts()) {
			throw new IllegalArgumentException("Invalid renewal attempt number");
		}

		Instant now = Instant.now(clock);
		LocalDate renewalDate = subscription.getExpirationDate();
		String idempotencyKey = subscription.getId() + ":" + renewalDate + ":attempt:" + attemptNumber;
		PaymentStatus paymentStatus;
		try {
			paymentStatus = paymentGateway.charge(subscription, idempotencyKey);
		} catch (RuntimeException exception) {
			paymentStatus = PaymentStatus.FAILED;
		}
		if (paymentStatus == null) {
			paymentStatus = PaymentStatus.FAILED;
		}
		boolean approved = paymentStatus == PaymentStatus.APPROVED;
		boolean pending = paymentStatus == PaymentStatus.PENDING;
		boolean retryScheduled = !approved && !pending && attemptNumber < renewalPolicy.maxAttempts();

		PaymentTransaction transaction = PaymentTransaction.pending(
			UUID.randomUUID(),
			subscription.getId(),
			PaymentType.RENEWAL,
			attemptNumber,
			idempotencyKey,
			subscription.getPlan().monthlyPriceCents(),
			now
		).applyProviderStatus(
			paymentStatus,
			null,
			approved || pending ? null : "Renewal payment failed",
			paymentStatus.isFinal() ? now : null
		);

		RenewalAttempt attempt = RenewalAttempt.builder()
			.id(UUID.randomUUID())
			.subscriptionId(subscription.getId())
			.renewalDate(renewalDate)
			.attemptNumber(attemptNumber)
			.status(approved ? RenewalAttemptStatus.SUCCEEDED : pending ? RenewalAttemptStatus.PENDING
				: retryScheduled ? RenewalAttemptStatus.WAITING_RETRY : RenewalAttemptStatus.FAILED)
			.idempotencyKey(idempotencyKey)
			.failureReason(approved || pending ? null : "Renewal payment failed")
			.attemptedAt(now)
			.nextRetryAt(retryScheduled ? now.plus(retryProperties.delayForFailure(attemptNumber)) : null)
			.build();

		Subscription renewed = approved
			? subscriptionRenewalService.renew(subscription, renewalDate).subscription()
			: subscription;

		return new RenewalProcessingResult(attempt, transaction, renewed);
	}
}
