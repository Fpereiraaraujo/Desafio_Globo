package com.fernando.sistema_assinaturas.entrypoint.api.mapper;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.GetUserSubscriptionResponse;

public final class SubscriptionQueryApiMapper {

	private SubscriptionQueryApiMapper() {
	}

	public static GetUserSubscriptionResponse toUserResponse(Subscription subscription) {
		return GetUserSubscriptionResponse.from(subscription);
	}
}
