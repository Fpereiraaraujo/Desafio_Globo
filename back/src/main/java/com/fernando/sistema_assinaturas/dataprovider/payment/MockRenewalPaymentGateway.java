package com.fernando.sistema_assinaturas.dataprovider.payment;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.gateway.RenewalPaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class MockRenewalPaymentGateway implements RenewalPaymentGateway {

	@Override
	public PaymentStatus charge(Subscription subscription, String idempotencyKey) {
		return PaymentStatus.APPROVED;
	}
}
