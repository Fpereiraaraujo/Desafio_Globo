package com.fernando.sistema_assinaturas.dataprovider.payment;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class UnconfiguredPaymentGateway implements PaymentGateway {

	@Override
	public PaymentResult charge(Subscription subscription, String idempotencyKey) {
		return new PaymentResult(
			PaymentStatus.UNKNOWN,
			null,
			"Payment gateway is not configured"
		);
	}
}
