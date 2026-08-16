package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(
	@NotNull(message = "status is required")
	PaymentStatus status,
	String providerTransactionId,
	String failureReason
) {
}
