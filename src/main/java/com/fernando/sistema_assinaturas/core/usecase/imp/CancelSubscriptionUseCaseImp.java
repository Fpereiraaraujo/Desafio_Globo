package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.CancelSubscriptionParam;
import com.fernando.sistema_assinaturas.core.usecase.CancelSubscriptionUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.util.UUID;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelSubscriptionUseCaseImp implements CancelSubscriptionUseCase {

	private final SubscriptionRepository subscriptionRepository;

	@Override
	@Transactional
	public Subscription execute(CancelSubscriptionParam param) {
		UUID subscriptionId = param == null ? null : param.subscriptionId();
		if (subscriptionId == null) {
			throw new IllegalArgumentException("Subscription id is required");
		}

		var entity = subscriptionRepository.findById(subscriptionId)
			.orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
		Subscription canceled = SubscriptionDatabaseMapper.toDomain(entity).cancel(Instant.now());
		return SubscriptionDatabaseMapper.toDomain(
			subscriptionRepository.save(SubscriptionDatabaseMapper.toEntity(canceled))
		);
	}
}
