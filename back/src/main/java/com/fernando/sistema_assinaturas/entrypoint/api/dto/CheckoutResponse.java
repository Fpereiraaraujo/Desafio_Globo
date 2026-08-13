package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;

public record CheckoutResponse(
	PaymentStatus status,
	String checkoutUrl,
	String orderNsu
) {

	public static CheckoutResponse from(CheckoutResult result) {
		return new CheckoutResponse(result.status(), result.checkoutUrl(), result.orderNsu());
	}
}
