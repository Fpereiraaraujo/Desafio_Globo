package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.param.UpdatePaymentStatusParam;

public interface UpdatePaymentStatusUseCase {

	PaymentTransaction execute(UpdatePaymentStatusParam param);
}
