package com.fernando.sistema_assinaturas.dataprovider.database.mapper;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.PaymentTransactionJpaEntity;

public final class PaymentTransactionDatabaseMapper {

	private PaymentTransactionDatabaseMapper() {
	}

	public static PaymentTransactionJpaEntity toEntity(PaymentTransaction transaction) {
		return PaymentTransactionJpaEntity.builder()
			.id(transaction.getId())
			.subscriptionId(transaction.getSubscriptionId())
			.paymentType(transaction.getPaymentType())
			.attemptNumber(transaction.getAttemptNumber())
			.idempotencyKey(transaction.getIdempotencyKey())
			.amountCents(transaction.getAmountCents())
			.status(transaction.getStatus())
			.checkoutUrl(transaction.getCheckoutUrl())
			.providerTransactionId(transaction.getProviderTransactionId())
			.failureReason(transaction.getFailureReason())
			.createdAt(transaction.getCreatedAt())
			.completedAt(transaction.getCompletedAt())
			.build();
	}

	public static PaymentTransaction toDomain(PaymentTransactionJpaEntity entity) {
		return PaymentTransaction.builder()
			.id(entity.getId())
			.subscriptionId(entity.getSubscriptionId())
			.paymentType(entity.getPaymentType())
			.attemptNumber(entity.getAttemptNumber())
			.idempotencyKey(entity.getIdempotencyKey())
			.amountCents(entity.getAmountCents())
			.status(entity.getStatus())
			.checkoutUrl(entity.getCheckoutUrl())
			.providerTransactionId(entity.getProviderTransactionId())
			.failureReason(entity.getFailureReason())
			.createdAt(entity.getCreatedAt())
			.completedAt(entity.getCompletedAt())
			.build();
	}
}
