package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.CancelSubscriptionParam;

public interface CancelSubscriptionUseCase {

	Subscription execute(CancelSubscriptionParam param);
}
