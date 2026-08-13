package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.GetUserSubscriptionParam;

public interface GetUserSubscriptionUseCase {

	Subscription execute(GetUserSubscriptionParam param);
}
