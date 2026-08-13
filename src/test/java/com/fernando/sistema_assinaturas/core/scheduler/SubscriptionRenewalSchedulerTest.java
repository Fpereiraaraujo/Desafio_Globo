package com.fernando.sistema_assinaturas.core.scheduler;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.usecase.ProcessRenewalUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.RenewalAttemptJpaEntity;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.RenewalAttemptRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionRenewalSchedulerTest {

	@Mock private SubscriptionRepository subscriptionRepository;
	@Mock private RenewalAttemptRepository renewalAttemptRepository;
	@Mock private ProcessRenewalUseCase processRenewalUseCase;

	@Test
	void sendsDueSubscriptionToNextRenewalAttempt() {
		UUID subscriptionId = UUID.randomUUID();
		LocalDate dueDate = LocalDate.now().minusDays(1);
		LocalDate today = LocalDate.of(2026, 9, 12);
		when(subscriptionRepository.findAllByStatusAndExpirationDateLessThanEqual(
			SubscriptionStatus.ACTIVE, today))
			.thenReturn(List.of(subscription(subscriptionId, dueDate)));
		when(renewalAttemptRepository.countBySubscriptionIdAndRenewalDate(subscriptionId, dueDate)).thenReturn(1);

		new SubscriptionRenewalScheduler(subscriptionRepository, renewalAttemptRepository, processRenewalUseCase,
			Clock.fixed(Instant.parse("2026-09-12T12:00:00Z"), ZoneOffset.UTC))
			.processDueSubscriptions();

		verify(processRenewalUseCase).execute(argThat(param ->
			param.subscriptionId().equals(subscriptionId) && param.attemptNumber() == 2 && param.renewalDate().equals(dueDate)
		));
	}

	@Test
	void skipsSubscriptionThatAlreadyReachedThreeAttempts() {
		UUID subscriptionId = UUID.randomUUID();
		LocalDate dueDate = LocalDate.now().minusDays(1);
		LocalDate today = LocalDate.of(2026, 9, 12);
		when(subscriptionRepository.findAllByStatusAndExpirationDateLessThanEqual(
			SubscriptionStatus.ACTIVE, today))
			.thenReturn(List.of(subscription(subscriptionId, dueDate)));
		when(renewalAttemptRepository.countBySubscriptionIdAndRenewalDate(subscriptionId, dueDate)).thenReturn(3);

		new SubscriptionRenewalScheduler(subscriptionRepository, renewalAttemptRepository, processRenewalUseCase,
			Clock.fixed(Instant.parse("2026-09-12T12:00:00Z"), ZoneOffset.UTC))
			.processDueSubscriptions();

		verify(processRenewalUseCase, never()).execute(org.mockito.ArgumentMatchers.any());
	}

	private SubscriptionJpaEntity subscription(UUID id, LocalDate expirationDate) {
		return SubscriptionJpaEntity.builder()
			.id(id)
			.userId(UUID.randomUUID())
			.startDate(expirationDate.minusMonths(1))
			.expirationDate(expirationDate)
			.status(SubscriptionStatus.ACTIVE)
			.build();
	}
}
