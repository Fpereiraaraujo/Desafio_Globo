package com.fernando.sistema_assinaturas.dataprovider.payment;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class UnconfiguredPaymentGateway implements PaymentGateway {

	@Override
	public CheckoutResult createCheckout(Subscription subscription, String orderNsu) {
		return new CheckoutResult(PaymentStatus.UNKNOWN, null, orderNsu, null, "Payment gateway is not configured");
	}

	@Override
	public PaymentResult verifyPayment(PaymentTransaction transaction) {
		return new PaymentResult(
			PaymentStatus.UNKNOWN,
			null,
			"Payment gateway is not configured"
		);
	}
}
