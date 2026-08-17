package com.fernando.sistema_assinaturas.core.scheduler;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttempt;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttemptStatus;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalProcessingResult;
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
		LocalDate today = LocalDate.of(2026, 9, 12);
		LocalDate dueDate = today;
		when(subscriptionRepository.findAllByStatusAndExpirationDate(
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
		LocalDate today = LocalDate.of(2026, 9, 12);
		LocalDate dueDate = today;
		when(subscriptionRepository.findAllByStatusAndExpirationDate(
			SubscriptionStatus.ACTIVE, today))
			.thenReturn(List.of(subscription(subscriptionId, dueDate)));
		when(renewalAttemptRepository.countBySubscriptionIdAndRenewalDate(subscriptionId, dueDate)).thenReturn(3);

		new SubscriptionRenewalScheduler(subscriptionRepository, renewalAttemptRepository, processRenewalUseCase,
			Clock.fixed(Instant.parse("2026-09-12T12:00:00Z"), ZoneOffset.UTC))
			.processDueSubscriptions();

		verify(processRenewalUseCase, never()).execute(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void executesOnlyOneAttemptPerSchedulerRun() {
		UUID subscriptionId = UUID.randomUUID();
		LocalDate dueDate = LocalDate.of(2026, 9, 12);
		when(subscriptionRepository.findAllByStatusAndExpirationDate(SubscriptionStatus.ACTIVE, dueDate))
			.thenReturn(List.of(subscription(subscriptionId, dueDate)));
		when(renewalAttemptRepository.countBySubscriptionIdAndRenewalDate(subscriptionId, dueDate)).thenReturn(0);
		when(processRenewalUseCase.execute(org.mockito.ArgumentMatchers.any()))
			.thenReturn(failedResult(subscriptionId, dueDate, 1), failedResult(subscriptionId, dueDate, 2), failedResult(subscriptionId, dueDate, 3));

		new SubscriptionRenewalScheduler(subscriptionRepository, renewalAttemptRepository, processRenewalUseCase,
			Clock.fixed(Instant.parse("2026-09-12T12:00:00Z"), ZoneOffset.UTC))
			.processDueSubscriptions();

		verify(processRenewalUseCase, times(1)).execute(org.mockito.ArgumentMatchers.any());
	}

	private RenewalProcessingResult failedResult(UUID subscriptionId, LocalDate dueDate, int attemptNumber) {
		return new RenewalProcessingResult(RenewalAttempt.builder()
			.id(UUID.randomUUID())
			.subscriptionId(subscriptionId)
			.renewalDate(dueDate)
			.attemptNumber(attemptNumber)
			.status(RenewalAttemptStatus.FAILED)
			.idempotencyKey("key-" + attemptNumber)
			.attemptedAt(Instant.parse("2026-09-12T12:00:00Z"))
			.build(), null, null);
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
