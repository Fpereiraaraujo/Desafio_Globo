package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.param.CreateSubscriptionParam;
import com.fernando.sistema_assinaturas.core.service.SubscriptionCreationService;
import com.fernando.sistema_assinaturas.core.usecase.CreateSubscriptionUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.UserRepository;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.BusinessException;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSubscriptionUseCaseImp implements CreateSubscriptionUseCase {

	private final UserRepository userRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final SubscriptionCreationService subscriptionCreationService;

	@Override
	@Transactional
	public Subscription execute(CreateSubscriptionParam param) {
		UUID userId = param == null ? null : param.userId();
		if (userId == null || !userRepository.existsById(userId)) {
			throw new ResourceNotFoundException("User not found");
		}
		if (subscriptionRepository.existsByUserIdAndStatusActive(userId)
			|| subscriptionRepository.existsByUserIdAndStatus(
				userId,
				SubscriptionStatus.PENDING_PAYMENT
			)) {
			throw new BusinessException("User already has an active subscription");
		}

		Subscription subscription = subscriptionCreationService.create(param);
		return SubscriptionDatabaseMapper.toDomain(
			subscriptionRepository.save(SubscriptionDatabaseMapper.toEntity(subscription))
		);
	}
}
