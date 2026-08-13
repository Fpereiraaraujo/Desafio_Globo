package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.GetSubscriptionParam;

public interface GetSubscriptionUseCase {

	Subscription execute(GetSubscriptionParam param);
}
