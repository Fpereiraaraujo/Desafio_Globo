package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import java.util.UUID;

public record CheckoutResponse(
	PaymentStatus status,
	String checkoutUrl,
	String orderNsu,
	UUID paymentId
) {

	public static CheckoutResponse from(CheckoutResult result) {
		return new CheckoutResponse(result.status(), result.checkoutUrl(), result.orderNsu(), result.paymentId());
	}
}
