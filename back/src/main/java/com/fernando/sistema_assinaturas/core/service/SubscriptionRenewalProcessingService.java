package com.fernando.sistema_assinaturas.core.service;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttempt;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttemptStatus;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalProcessingResult;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalPolicy;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.gateway.RenewalPaymentGateway;
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

	@Autowired
	public SubscriptionRenewalProcessingService(RenewalPaymentGateway paymentGateway) {
		this(paymentGateway, Clock.systemUTC(), RenewalPolicy.defaultPolicy());
	}

	public SubscriptionRenewalProcessingService(RenewalPaymentGateway paymentGateway, Clock clock, RenewalPolicy renewalPolicy) {
		this.paymentGateway = paymentGateway;
		this.clock = clock;
		this.renewalPolicy = renewalPolicy;
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
		boolean approved = paymentStatus == PaymentStatus.APPROVED;
		boolean pending = paymentStatus == PaymentStatus.PENDING;

		PaymentTransaction transaction = PaymentTransaction.builder()
			.id(UUID.randomUUID())
			.subscriptionId(subscription.getId())
			.idempotencyKey(idempotencyKey)
			.amountCents(subscription.getPlan().monthlyPriceCents())
			.status(paymentStatus)
			.providerTransactionId(null)
			.createdAt(now)
			.completedAt(approved ? now : null)
			.build();

		RenewalAttempt attempt = RenewalAttempt.builder()
			.id(UUID.randomUUID())
			.subscriptionId(subscription.getId())
			.renewalDate(renewalDate)
			.attemptNumber(attemptNumber)
			.status(approved ? RenewalAttemptStatus.SUCCEEDED : pending ? RenewalAttemptStatus.PENDING : RenewalAttemptStatus.FAILED)
			.idempotencyKey(idempotencyKey)
			.failureReason(approved || pending ? null : "Renewal payment failed")
			.attemptedAt(now)
			.build();

		Subscription renewed = approved
			? new SubscriptionRenewalService().renew(subscription, renewalDate).subscription()
			: subscription;

		return new RenewalProcessingResult(attempt, transaction, renewed);
	}
}
