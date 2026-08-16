package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import java.util.UUID;

public interface GetPaymentUseCase {

	PaymentTransaction execute(UUID paymentId);
}
