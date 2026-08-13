package com.fernando.sistema_assinaturas.core.gateway;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;

public interface PaymentGateway {

	CheckoutResult createCheckout(Subscription subscription, String orderNsu);

}
