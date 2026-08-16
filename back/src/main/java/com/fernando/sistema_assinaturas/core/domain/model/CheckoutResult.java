package com.fernando.sistema_assinaturas.core.domain.model;

import java.util.UUID;

public record CheckoutResult(
	PaymentStatus status,
	String checkoutUrl,
	String orderNsu,
	String providerTransactionId,
	String message,
	UUID paymentId
) {

	public CheckoutResult(
		PaymentStatus status,
		String checkoutUrl,
		String orderNsu,
		String providerTransactionId,
		String message
	) {
		this(status, checkoutUrl, orderNsu, providerTransactionId, message, null);
	}

	public boolean isPending() {
		return status == PaymentStatus.PENDING;
	}
}
