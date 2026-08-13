package com.fernando.sistema_assinaturas.core.usecase.imp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.CreateSubscriptionParam;
import com.fernando.sistema_assinaturas.core.service.SubscriptionCreationService;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSubscriptionUseCaseImpTest {

	private static final UUID USER_ID = UUID.randomUUID();

	@Mock private UserRepository userRepository;
	@Mock private SubscriptionRepository subscriptionRepository;

	private CreateSubscriptionUseCaseImp useCase;

	@BeforeEach
	void setUp() {
		useCase = new CreateSubscriptionUseCaseImp(
			userRepository,
			subscriptionRepository,
			new SubscriptionCreationService(Clock.fixed(Instant.parse("2026-08-12T12:00:00Z"), ZoneOffset.UTC))
		);
	}

	@Test
	void createsSubscriptionForExistingUserWithoutActiveSubscription() {
		when(userRepository.existsById(USER_ID)).thenReturn(true);
		when(subscriptionRepository.existsByUserIdAndStatusActive(USER_ID)).thenReturn(false);
		when(subscriptionRepository.save(any(SubscriptionJpaEntity.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		Subscription result = useCase.execute(new CreateSubscriptionParam(USER_ID, Plan.PREMIUM));

		assertThat(result.getUserId()).isEqualTo(USER_ID);
		assertThat(result.getPlan()).isEqualTo(Plan.PREMIUM);
		verify(subscriptionRepository).save(any(SubscriptionJpaEntity.class));
	}

	@Test
	void rejectsSecondActiveSubscriptionForUser() {
		when(userRepository.existsById(USER_ID)).thenReturn(true);
		when(subscriptionRepository.existsByUserIdAndStatusActive(USER_ID)).thenReturn(true);

		assertThatThrownBy(() -> useCase.execute(new CreateSubscriptionParam(USER_ID, Plan.BASICO)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("User already has an active subscription");
	}
}
