package com.fernando.sistema_assinaturas.core.usecase.imp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessRenewalParam;
import com.fernando.sistema_assinaturas.core.gateway.PaymentGateway;
import com.fernando.sistema_assinaturas.core.service.SubscriptionRenewalProcessingService;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.RenewalAttemptRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessRenewalUseCaseImpTest {

	@Mock private SubscriptionRepository subscriptionRepository;
	@Mock private PaymentTransactionRepository paymentTransactionRepository;
	@Mock private RenewalAttemptRepository renewalAttemptRepository;
	@Mock private PaymentGateway paymentGateway;

	private ProcessRenewalUseCaseImp useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessRenewalUseCaseImp(
			subscriptionRepository,
			paymentTransactionRepository,
			renewalAttemptRepository,
			new SubscriptionRenewalProcessingService(
				paymentGateway,
				Clock.fixed(Instant.parse("2026-09-12T12:00:00Z"), ZoneOffset.UTC),
				new com.fernando.sistema_assinaturas.core.domain.model.RenewalPolicy(3)
			)
		);
	}

	@Test
	void persistsApprovedRenewalAndAdvancesSubscription() {
		UUID subscriptionId = UUID.randomUUID();
		Subscription subscription = subscription(subscriptionId);
		when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(SubscriptionDatabaseMapper.toEntity(subscription)));
		when(paymentGateway.createCheckout(any(), any())).thenReturn(new CheckoutResult(PaymentStatus.APPROVED, "https://checkout.test/1", "order-1", "tx-1", "approved"));
		when(paymentTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(renewalAttemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = useCase.execute(new ProcessRenewalParam(subscriptionId, subscription.getExpirationDate(), 1));

		assertThat(result.succeeded()).isTrue();
		assertThat(result.subscription().getExpirationDate()).isEqualTo(LocalDate.of(2026, 10, 12));
		verify(paymentTransactionRepository).save(any());
		verify(renewalAttemptRepository).save(any());
		verify(subscriptionRepository).save(any());
	}

	@Test
	void suspendsSubscriptionAfterThirdFailedAttempt() {
		UUID subscriptionId = UUID.randomUUID();
		Subscription subscription = subscription(subscriptionId);
		when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(SubscriptionDatabaseMapper.toEntity(subscription)));
		when(paymentGateway.createCheckout(any(), any())).thenReturn(new CheckoutResult(PaymentStatus.DECLINED, null, "order-1", null, "declined"));
		when(paymentTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(renewalAttemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(subscriptionRepository.save(any(SubscriptionJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = useCase.execute(new ProcessRenewalParam(subscriptionId, subscription.getExpirationDate(), 3));

		assertThat(result.subscription().getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
		verify(subscriptionRepository).save(any(SubscriptionJpaEntity.class));
	}

	private Subscription subscription(UUID id) {
		return Subscription.create(id, UUID.randomUUID(), Plan.PREMIUM, LocalDate.of(2026, 8, 12));
	}
}
