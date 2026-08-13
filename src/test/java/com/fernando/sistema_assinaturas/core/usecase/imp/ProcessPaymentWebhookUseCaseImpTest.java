package com.fernando.sistema_assinaturas.core.usecase.imp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessPaymentWebhookParam;
import com.fernando.sistema_assinaturas.core.service.SubscriptionRenewalService;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.PaymentTransactionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentWebhookUseCaseImpTest {

	@Mock private PaymentTransactionRepository paymentTransactionRepository;
	@Mock private SubscriptionRepository subscriptionRepository;

	private ProcessPaymentWebhookUseCaseImp useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessPaymentWebhookUseCaseImp(
			paymentTransactionRepository,
			subscriptionRepository,
			new SubscriptionRenewalService()
		);
	}

	@Test
	void approvesPaymentAndAdvancesActiveSubscription() {
		UUID subscriptionId = UUID.randomUUID();
		Subscription subscription = Subscription.create(subscriptionId, UUID.randomUUID(), Plan.PREMIUM, LocalDate.of(2026, 8, 12));
		PaymentTransaction transaction = transaction(subscriptionId, PaymentStatus.PENDING);
		when(paymentTransactionRepository.findByOrderNsu("order-1"))
			.thenReturn(Optional.of(PaymentTransactionDatabaseMapper.toEntity(transaction)));
		when(paymentTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(subscriptionRepository.findById(subscriptionId))
			.thenReturn(Optional.of(SubscriptionDatabaseMapper.toEntity(subscription)));
		when(subscriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		PaymentTransaction result = useCase.execute(
			new ProcessPaymentWebhookParam("order-1", "transaction-1", PaymentStatus.APPROVED)
		);

		assertThat(result.getStatus()).isEqualTo(PaymentStatus.APPROVED);
		assertThat(result.getProviderTransactionId()).isEqualTo("transaction-1");
		assertThat(result.getCompletedAt()).isNotNull();
		verify(subscriptionRepository).save(any());
	}

	@Test
	void doesNotAdvanceSubscriptionWhenApprovedWebhookIsRepeated() {
		UUID subscriptionId = UUID.randomUUID();
		PaymentTransaction transaction = transaction(subscriptionId, PaymentStatus.APPROVED);
		when(paymentTransactionRepository.findByOrderNsu("order-1"))
			.thenReturn(Optional.of(PaymentTransactionDatabaseMapper.toEntity(transaction)));

		PaymentTransaction result = useCase.execute(
			new ProcessPaymentWebhookParam("order-1", "transaction-1", PaymentStatus.APPROVED)
		);

		assertThat(result).isEqualTo(transaction);
		verify(subscriptionRepository, never()).findById(any());
		verify(paymentTransactionRepository, never()).save(any());
	}

	private PaymentTransaction transaction(UUID subscriptionId, PaymentStatus status) {
		return PaymentTransaction.builder()
			.id(UUID.randomUUID())
			.subscriptionId(subscriptionId)
			.idempotencyKey("order-1")
			.amountCents(3990)
			.status(status)
			.createdAt(Instant.parse("2026-08-12T12:00:00Z"))
			.build();
	}
}
