package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessPaymentWebhookParam;
import com.fernando.sistema_assinaturas.core.service.SubscriptionRenewalService;
import com.fernando.sistema_assinaturas.core.usecase.ProcessPaymentWebhookUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.PaymentTransactionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessPaymentWebhookUseCaseImp implements ProcessPaymentWebhookUseCase {

	private final PaymentTransactionRepository paymentTransactionRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final SubscriptionRenewalService subscriptionRenewalService;
	private final Clock clock = Clock.systemUTC();

	@Override
	@Transactional
	public PaymentTransaction execute(ProcessPaymentWebhookParam param) {
		if (param == null || param.orderNsu() == null || param.orderNsu().isBlank()) {
			throw new IllegalArgumentException("Order NSU is required");
		}
		if (param.status() == null) {
			throw new IllegalArgumentException("Payment status is required");
		}

		var entity = paymentTransactionRepository.findByOrderNsu(param.orderNsu())
			.orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found"));
		PaymentTransaction current = PaymentTransactionDatabaseMapper.toDomain(entity);
		if (current.getStatus() == PaymentStatus.APPROVED) {
			return current;
		}
		Instant completedAt = isFinal(param.status()) ? Instant.now(clock) : current.getCompletedAt();
		PaymentTransaction updated = current.toBuilder()
			.status(param.status())
			.providerTransactionId(firstNonBlank(param.transactionNsu(), current.getProviderTransactionId()))
			.completedAt(completedAt)
			.build();
		PaymentTransaction saved = PaymentTransactionDatabaseMapper.toDomain(
			paymentTransactionRepository.save(PaymentTransactionDatabaseMapper.toEntity(updated))
		);
		if (param.status() == PaymentStatus.APPROVED) {
			advanceSubscription(saved);
		}
		return saved;
	}

	private void advanceSubscription(PaymentTransaction transaction) {
		var subscriptionEntity = subscriptionRepository.findById(transaction.getSubscriptionId())
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
		var subscription = SubscriptionDatabaseMapper.toDomain(subscriptionEntity);
		if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
			return;
		}
		var renewed = subscriptionRenewalService
			.renew(subscription, subscription.getExpirationDate())
			.subscription();
		subscriptionRepository.save(SubscriptionDatabaseMapper.toEntity(renewed));
	}

	private static boolean isFinal(PaymentStatus status) {
		return status == PaymentStatus.APPROVED
			|| status == PaymentStatus.DECLINED
			|| status == PaymentStatus.FAILED;
	}

	private static String firstNonBlank(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}
}
