package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.param.GetUserSubscriptionParam;
import com.fernando.sistema_assinaturas.core.usecase.GetUserSubscriptionUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserSubscriptionUseCaseImp implements GetUserSubscriptionUseCase {

	private final SubscriptionRepository subscriptionRepository;

	@Override
	public Subscription execute(GetUserSubscriptionParam param) {
		UUID userId = param == null ? null : param.userId();
		if (userId == null) {
			throw new IllegalArgumentException("User id is required");
		}

		return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
			.map(SubscriptionDatabaseMapper::toDomain)
			.orElseThrow(() -> new IllegalArgumentException("Active subscription not found"));
	}
}
