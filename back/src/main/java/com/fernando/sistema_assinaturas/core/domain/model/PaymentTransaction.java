package com.fernando.sistema_assinaturas.core.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PaymentTransaction {

	UUID id;
	UUID subscriptionId;
	PaymentType paymentType;
	int attemptNumber;
	String idempotencyKey;
	int amountCents;
	PaymentStatus status;
	String checkoutUrl;
	String providerTransactionId;
	String failureReason;
	Instant createdAt;
	Instant completedAt;

	public static PaymentTransaction pending(
		UUID id,
		UUID subscriptionId,
		PaymentType paymentType,
		int attemptNumber,
		String idempotencyKey,
		int amountCents,
		Instant createdAt
	) {
		if (id == null || subscriptionId == null || paymentType == null || idempotencyKey == null || createdAt == null) {
			throw new IllegalArgumentException("Payment data is required");
		}
		if (attemptNumber < 1 || amountCents < 1) {
			throw new IllegalArgumentException("Payment attempt and amount must be positive");
		}
		return PaymentTransaction.builder()
			.id(id)
			.subscriptionId(subscriptionId)
			.paymentType(paymentType)
			.attemptNumber(attemptNumber)
			.idempotencyKey(idempotencyKey)
			.amountCents(amountCents)
			.status(PaymentStatus.PENDING)
			.createdAt(createdAt)
			.build();
	}

	public PaymentTransaction applyProviderStatus(
		PaymentStatus newStatus,
		String newProviderTransactionId,
		String newFailureReason,
		Instant completedAt
	) {
		if (newStatus == null) {
			throw new IllegalArgumentException("Payment status is required");
		}
		if (status == newStatus) {
			return toBuilder()
				.providerTransactionId(newProviderTransactionId != null ? newProviderTransactionId : providerTransactionId)
				.failureReason(newFailureReason != null ? newFailureReason : failureReason)
				.completedAt(completedAt != null ? completedAt : this.completedAt)
				.build();
		}
		if (status != PaymentStatus.PENDING && status != PaymentStatus.UNKNOWN) {
			throw new IllegalStateException("A final payment status cannot be changed");
		}
		return toBuilder()
			.status(newStatus)
			.providerTransactionId(newProviderTransactionId)
			.failureReason(newFailureReason)
			.completedAt(newStatus.isFinal() ? completedAt : null)
			.build();
	}
}
