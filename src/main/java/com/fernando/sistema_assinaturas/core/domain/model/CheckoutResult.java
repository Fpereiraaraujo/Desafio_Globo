package com.fernando.sistema_assinaturas.core.domain.model;

public record CheckoutResult(
	PaymentStatus status,
	String checkoutUrl,
	String orderNsu,
	String providerTransactionId,
	String message
) {

	public boolean isPending() {
		return status == PaymentStatus.PENDING;
	}
}
