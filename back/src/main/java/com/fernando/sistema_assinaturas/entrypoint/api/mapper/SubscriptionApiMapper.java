package com.fernando.sistema_assinaturas.entrypoint.api.mapper;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.CreateSubscriptionParam;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.CreateSubscriptionRequest;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.SubscriptionResponse;

public final class SubscriptionApiMapper {

	private SubscriptionApiMapper() {
	}

	public static CreateSubscriptionParam toParam(CreateSubscriptionRequest request) {
		return new CreateSubscriptionParam(request.userId(), request.plan());
	}

	public static SubscriptionResponse toResponse(Subscription subscription) {
		return SubscriptionResponse.from(subscription);
	}
}
