package com.fernando.sistema_assinaturas.dataprovider.infinitepay;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
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
		var response = client.createLinkWithResilience(
			orderNsu,
			subscription.getPlan().monthlyPriceCents(),
			"Assinatura " + subscription.getPlan().name()
		);
		boolean available = response != null && response.url() != null && !response.url().isBlank();
		return new CheckoutResult(
			PaymentStatus.PENDING,
			available ? response.url() : "mock://infinitepay/checkout",
			orderNsu,
			null,
			available ? "Checkout created" : "Demonstration checkout created"
		);
	}

}
