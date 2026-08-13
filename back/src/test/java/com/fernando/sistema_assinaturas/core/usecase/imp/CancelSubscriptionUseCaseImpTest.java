package com.fernando.sistema_assinaturas.core.usecase.imp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.CancelSubscriptionParam;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelSubscriptionUseCaseImpTest {

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@Test
	void cancelsSubscriptionAndPreservesExpirationDate() {
		UUID subscriptionId = UUID.randomUUID();
		SubscriptionJpaEntity entity = entity(subscriptionId);
		when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(entity));
		when(subscriptionRepository.save(any(SubscriptionJpaEntity.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		Subscription result = new CancelSubscriptionUseCaseImp(subscriptionRepository)
			.execute(new CancelSubscriptionParam(subscriptionId));

		assertThat(result.getStatus().name()).isEqualTo("CANCELED");
		assertThat(result.getExpirationDate()).isEqualTo(LocalDate.of(2026, 9, 12));
		assertThat(result.getCanceledAt()).isNotNull();
		verify(subscriptionRepository).save(any(SubscriptionJpaEntity.class));
	}

	@Test
	void rejectsMissingSubscription() {
		UUID subscriptionId = UUID.randomUUID();
		when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new CancelSubscriptionUseCaseImp(subscriptionRepository)
			.execute(new CancelSubscriptionParam(subscriptionId)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Subscription not found");
	}

	private SubscriptionJpaEntity entity(UUID id) {
		Subscription subscription = Subscription.create(
			id, UUID.randomUUID(), Plan.PREMIUM, LocalDate.of(2026, 8, 12)
		);
		return com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper.toEntity(subscription);
	}
}
