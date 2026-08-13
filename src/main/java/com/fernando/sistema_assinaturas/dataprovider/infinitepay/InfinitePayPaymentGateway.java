package com.fernando.sistema_assinaturas.dataprovider.infinitepay;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.gateway.PaymentGateway;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class InfinitePayPaymentGateway implements PaymentGateway {

	private final InfinitePayClient client;

	public InfinitePayPaymentGateway(InfinitePayClient client) {
		this.client = client;
	}

	@Override
	public CheckoutResult createCheckout(Subscription subscription, String orderNsu) {
		var response = client.createLink(
			orderNsu,
			subscription.getPlan().monthlyPriceCents(),
			"Assinatura " + subscription.getPlan().name()
		);
		return new CheckoutResult(PaymentStatus.PENDING, response != null ? response.url() : null, orderNsu, null, "Checkout created");
	}

	@Override
	public PaymentResult verifyPayment(PaymentTransaction transaction) {
		var response = client.checkPayment(transaction.getIdempotencyKey(), transaction.getProviderTransactionId(), null);
		if (response == null || !response.success()) {
			return new PaymentResult(PaymentStatus.UNKNOWN, null, "Payment verification failed");
		}
		return new PaymentResult(
			response.paid() ? PaymentStatus.APPROVED : PaymentStatus.PENDING,
			response.transactionNsu(),
			response.paid() ? "Payment approved" : "Payment pending"
		);
	}
}
