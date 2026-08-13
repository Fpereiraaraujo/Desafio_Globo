package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.param.CreateCheckoutParam;

public interface CreateCheckoutUseCase {

	CheckoutResult execute(CreateCheckoutParam param);
}
