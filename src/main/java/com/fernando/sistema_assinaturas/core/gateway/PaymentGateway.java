package com.fernando.sistema_assinaturas.core.gateway;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentResult;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;

public interface PaymentGateway {

	PaymentResult charge(Subscription subscription, String idempotencyKey);
}
