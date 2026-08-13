package com.fernando.sistema_assinaturas.core.usecase.imp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.GetUserSubscriptionParam;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserSubscriptionUseCaseImpTest {

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@Test
	void returnsActiveSubscriptionForUser() {
		UUID userId = UUID.randomUUID();
		Subscription subscription = Subscription.create(
			UUID.randomUUID(), userId, Plan.BASICO, LocalDate.of(2026, 8, 12)
		);
		SubscriptionJpaEntity entity = SubscriptionDatabaseMapper.toEntity(subscription);
		when(subscriptionRepository.findByUserIdAndStatus(userId, com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus.ACTIVE))
			.thenReturn(Optional.of(entity));

		Subscription result = new GetUserSubscriptionUseCaseImp(subscriptionRepository)
			.execute(new GetUserSubscriptionParam(userId));

		assertThat(result.getUserId()).isEqualTo(userId);
		assertThat(result.getPlan()).isEqualTo(Plan.BASICO);
	}

	@Test
	void rejectsWhenActiveSubscriptionDoesNotExist() {
		UUID userId = UUID.randomUUID();
		when(subscriptionRepository.findByUserIdAndStatus(userId, com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus.ACTIVE))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> new GetUserSubscriptionUseCaseImp(subscriptionRepository)
			.execute(new GetUserSubscriptionParam(userId)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Active subscription not found");
	}
}
