package com.fernando.sistema_assinaturas.core.domain.param;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import java.util.UUID;

public record UpdatePaymentStatusParam(
	UUID paymentId,
	PaymentStatus status,
	String providerTransactionId,
	String failureReason
) {
}
