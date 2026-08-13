package com.fernando.sistema_assinaturas.dataprovider.database.mapper;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity;

public final class SubscriptionDatabaseMapper {

	private SubscriptionDatabaseMapper() {
	}

	public static SubscriptionJpaEntity toEntity(Subscription subscription) {
		return SubscriptionJpaEntity.builder()
			.id(subscription.getId())
			.userId(subscription.getUserId())
			.plan(subscription.getPlan())
			.startDate(subscription.getStartDate())
			.expirationDate(subscription.getExpirationDate())
			.status(subscription.getStatus())
			.build();
	}

	public static Subscription toDomain(SubscriptionJpaEntity entity) {
		return Subscription.builder()
			.id(entity.getId())
			.userId(entity.getUserId())
			.plan(entity.getPlan())
			.startDate(entity.getStartDate())
			.expirationDate(entity.getExpirationDate())
			.status(entity.getStatus())
			.build();
	}
}
