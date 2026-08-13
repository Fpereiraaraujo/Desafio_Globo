package com.fernando.sistema_assinaturas.core.domain.param;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;

public record ProcessPaymentWebhookParam(
	String orderNsu,
	String transactionNsu,
	PaymentStatus status
) {
}
