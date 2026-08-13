package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import java.time.Instant;
import java.util.UUID;

public record PaymentWebhookResponse(
	UUID transactionId,
	String orderNsu,
	PaymentStatus status,
	String providerTransactionId,
	Instant completedAt
) {

	public static PaymentWebhookResponse from(PaymentTransaction transaction) {
		return new PaymentWebhookResponse(
			transaction.getId(),
			transaction.getIdempotencyKey(),
			transaction.getStatus(),
			transaction.getProviderTransactionId(),
			transaction.getCompletedAt()
		);
	}
}
