package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessPaymentWebhookParam;

public interface ProcessPaymentWebhookUseCase {

	PaymentTransaction execute(ProcessPaymentWebhookParam param);
}
