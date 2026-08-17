package com.fernando.sistema_assinaturas.dataprovider.infinitepay;

import com.fernando.sistema_assinaturas.config.InfinitePayProperties;
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
	private final InfinitePayProperties properties;

	public InfinitePayPaymentGateway(InfinitePayClient client, InfinitePayProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public CheckoutResult createCheckout(Subscription subscription, String orderNsu) {
		if (properties.demoMode()) {
			return new CheckoutResult(
				PaymentStatus.PENDING,
				"mock://infinitepay/checkout",
				orderNsu,
				null,
				"Demonstration checkout created"
			);
		}
		String configuredUrl = properties.configuredCheckoutUrl(subscription.getPlan());
		if (configuredUrl != null) {
			return new CheckoutResult(
				PaymentStatus.PENDING,
				configuredUrl,
				orderNsu,
				null,
				"Configured InfinitePay checkout"
			);
		}
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
