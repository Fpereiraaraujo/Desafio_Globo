package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentType;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
	UUID id,
	UUID subscriptionId,
	PaymentType paymentType,
	int attemptNumber,
	String idempotencyKey,
	int amountCents,
	PaymentStatus status,
	String checkoutUrl,
	String providerTransactionId,
	String failureReason,
	Instant createdAt,
	Instant completedAt
) {

	public static PaymentResponse from(PaymentTransaction payment) {
		return new PaymentResponse(
			payment.getId(),
			payment.getSubscriptionId(),
			payment.getPaymentType(),
			payment.getAttemptNumber(),
			payment.getIdempotencyKey(),
			payment.getAmountCents(),
			payment.getStatus(),
			payment.getCheckoutUrl(),
			payment.getProviderTransactionId(),
			payment.getFailureReason(),
			payment.getCreatedAt(),
			payment.getCompletedAt()
		);
	}
}
