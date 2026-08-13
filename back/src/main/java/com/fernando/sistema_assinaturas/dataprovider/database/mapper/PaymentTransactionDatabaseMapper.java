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
			.idempotencyKey(transaction.getIdempotencyKey())
			.amountCents(transaction.getAmountCents())
			.status(transaction.getStatus())
			.providerTransactionId(transaction.getProviderTransactionId())
			.createdAt(transaction.getCreatedAt())
			.completedAt(transaction.getCompletedAt())
			.build();
	}

	public static PaymentTransaction toDomain(PaymentTransactionJpaEntity entity) {
		return PaymentTransaction.builder()
			.id(entity.getId())
			.subscriptionId(entity.getSubscriptionId())
			.idempotencyKey(entity.getIdempotencyKey())
			.amountCents(entity.getAmountCents())
			.status(entity.getStatus())
			.providerTransactionId(entity.getProviderTransactionId())
			.createdAt(entity.getCreatedAt())
			.completedAt(entity.getCompletedAt())
			.build();
	}
}
