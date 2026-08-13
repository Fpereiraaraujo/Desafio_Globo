package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.CreateSubscriptionParam;

public interface CreateSubscriptionUseCase {

	Subscription execute(CreateSubscriptionParam param);
}
