package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentType;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttemptStatus;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalPolicy;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.param.UpdatePaymentStatusParam;
import com.fernando.sistema_assinaturas.core.service.SubscriptionRenewalService;
import com.fernando.sistema_assinaturas.core.usecase.UpdatePaymentStatusUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.PaymentTransactionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.RenewalAttemptDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.RenewalAttemptRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePaymentStatusUseCaseImp implements UpdatePaymentStatusUseCase {

	private final PaymentTransactionRepository paymentTransactionRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final RenewalAttemptRepository renewalAttemptRepository;
	private final SubscriptionRenewalService subscriptionRenewalService;
	private final Clock clock = Clock.systemUTC();

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(cacheNames = "subscriptionsById", allEntries = true, cacheManager = "redisCacheManager"),
		@CacheEvict(cacheNames = "subscriptionsByUser", allEntries = true, cacheManager = "redisCacheManager")
	})
	public PaymentTransaction execute(UpdatePaymentStatusParam param) {
		if (param == null || param.paymentId() == null) {
			throw new IllegalArgumentException("Payment id is required");
		}
		if (param.status() == null) {
			throw new IllegalArgumentException("Payment status is required");
		}

		PaymentTransaction reference = paymentTransactionRepository.findById(param.paymentId())
			.map(PaymentTransactionDatabaseMapper::toDomain)
			.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
		subscriptionRepository.findByIdForUpdate(reference.getSubscriptionId())
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

		PaymentTransaction current = paymentTransactionRepository.findByIdForUpdate(param.paymentId())
			.map(PaymentTransactionDatabaseMapper::toDomain)
			.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
		if (current.getStatus() == param.status()) {
			reconcileSideEffects(current);
			return current;
		}

		PaymentTransaction updated = current.applyProviderStatus(
			param.status(),
			param.providerTransactionId(),
			param.failureReason(),
			Instant.now(clock)
		);
		paymentTransactionRepository.save(PaymentTransactionDatabaseMapper.toEntity(updated));

		reconcileSideEffects(updated);

		return updated;
	}

	private void reconcileSideEffects(PaymentTransaction payment) {
		if (payment.getPaymentType() == PaymentType.INITIAL_CHECKOUT) {
			applyInitialPaymentResult(payment);
		} else if (payment.getPaymentType() == PaymentType.RENEWAL) {
			applyRenewalPaymentResult(payment);
		}
	}

	private void applyInitialPaymentResult(PaymentTransaction payment) {
		if (payment.getStatus() != PaymentStatus.APPROVED) {
			return;
		}

		var subscriptionEntity = subscriptionRepository.findByIdForUpdate(payment.getSubscriptionId())
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
		Subscription subscription = SubscriptionDatabaseMapper.toDomain(subscriptionEntity);
		if (subscription.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
			subscriptionRepository.save(SubscriptionDatabaseMapper.toEntity(subscription.activate()));
		} else if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
			throw new IllegalStateException("Subscription payment has expired");
		}
	}

	private void applyRenewalPaymentResult(PaymentTransaction payment) {
		var attemptEntity = renewalAttemptRepository.findByIdempotencyKey(payment.getIdempotencyKey())
			.orElse(null);
		if (attemptEntity == null) {
			return;
		}

		var attempt = RenewalAttemptDatabaseMapper.toDomain(attemptEntity);
		var subscriptionEntity = subscriptionRepository.findByIdForUpdate(payment.getSubscriptionId())
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
		Subscription subscription = SubscriptionDatabaseMapper.toDomain(subscriptionEntity);

		if (payment.getStatus() == PaymentStatus.APPROVED) {
			if (attempt.getStatus() != RenewalAttemptStatus.SUCCEEDED) {
				attempt = attempt.toBuilder()
					.status(RenewalAttemptStatus.SUCCEEDED)
					.failureReason(null)
					.build();
				renewalAttemptRepository.save(RenewalAttemptDatabaseMapper.toEntity(attempt));
			}
			if (subscription.getStatus() == SubscriptionStatus.ACTIVE
				&& subscription.getExpirationDate().equals(attempt.getRenewalDate())) {
				subscriptionRepository.save(SubscriptionDatabaseMapper.toEntity(
					subscriptionRenewalService.renew(subscription, attempt.getRenewalDate()).subscription()
				));
			}
			return;
		}

		if (payment.getStatus() == PaymentStatus.DECLINED
			|| payment.getStatus() == PaymentStatus.FAILED
			|| payment.getStatus() == PaymentStatus.EXPIRED) {
			attempt = attempt.toBuilder()
				.status(RenewalAttemptStatus.FAILED)
				.failureReason(payment.getFailureReason() != null ? payment.getFailureReason() : payment.getStatus().name())
				.build();
			renewalAttemptRepository.save(RenewalAttemptDatabaseMapper.toEntity(attempt));
			if (attempt.isFinalFailure(RenewalPolicy.defaultPolicy().maxAttempts())
				&& subscription.getStatus() == SubscriptionStatus.ACTIVE) {
				subscriptionRepository.save(SubscriptionDatabaseMapper.toEntity(subscription.suspend()));
			}
		}
	}
}
