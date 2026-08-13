package com.fernando.sistema_assinaturas.core.gateway;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;

public interface PaymentGateway {

	CheckoutResult createCheckout(Subscription subscription, String orderNsu);

	PaymentResult verifyPayment(PaymentTransaction transaction);
}
