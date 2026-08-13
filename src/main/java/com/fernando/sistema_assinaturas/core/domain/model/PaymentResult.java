package com.fernando.sistema_assinaturas.core.domain.model;

public record PaymentResult(
	PaymentStatus status,
	String providerTransactionId,
	String message
) {

	public boolean isApproved() {
		return status == PaymentStatus.APPROVED;
	}
}
