package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.GetSubscriptionParam;
import com.fernando.sistema_assinaturas.core.usecase.GetSubscriptionUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class GetSubscriptionUseCaseImp implements GetSubscriptionUseCase {

	private final SubscriptionRepository subscriptionRepository;

	@Override
	@Cacheable(cacheNames = "subscriptionsById", key = "#param.subscriptionId()", cacheManager = "redisCacheManager")
	public Subscription execute(GetSubscriptionParam param) {
		UUID subscriptionId = param == null ? null : param.subscriptionId();
		if (subscriptionId == null) {
			throw new IllegalArgumentException("Subscription id is required");
		}

		return subscriptionRepository.findById(subscriptionId)
			.map(SubscriptionDatabaseMapper::toDomain)
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
	}
}
