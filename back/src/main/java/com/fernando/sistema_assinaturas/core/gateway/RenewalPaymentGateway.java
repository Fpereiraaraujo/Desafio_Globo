package com.fernando.sistema_assinaturas.core.gateway;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;

public interface RenewalPaymentGateway {

	PaymentStatus charge(Subscription subscription, String idempotencyKey);
}
