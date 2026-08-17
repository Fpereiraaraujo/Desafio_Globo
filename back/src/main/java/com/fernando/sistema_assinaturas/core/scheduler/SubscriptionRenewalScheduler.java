package com.fernando.sistema_assinaturas.core.scheduler;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentType;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttemptStatus;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessRenewalParam;
import com.fernando.sistema_assinaturas.core.usecase.ProcessRenewalUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.PaymentTransactionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.RenewalAttemptDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.RenewalAttemptRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalPolicy;
import com.fernando.sistema_assinaturas.config.RenewalRetryProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class SubscriptionRenewalScheduler {

	private final SubscriptionRepository subscriptionRepository;
	private final RenewalAttemptRepository renewalAttemptRepository;
	private final PaymentTransactionRepository paymentTransactionRepository;
	private final ProcessRenewalUseCase processRenewalUseCase;
	private final Clock clock;
	private final RenewalRetryProperties retryProperties;

	@Autowired
	public SubscriptionRenewalScheduler(
		SubscriptionRepository subscriptionRepository,
		RenewalAttemptRepository renewalAttemptRepository,
		PaymentTransactionRepository paymentTransactionRepository,
		ProcessRenewalUseCase processRenewalUseCase,
		RenewalRetryProperties retryProperties
	) {
		this(subscriptionRepository, renewalAttemptRepository, paymentTransactionRepository, processRenewalUseCase, Clock.systemUTC(), retryProperties);
	}

	public SubscriptionRenewalScheduler(
		SubscriptionRepository subscriptionRepository,
		RenewalAttemptRepository renewalAttemptRepository,
		ProcessRenewalUseCase processRenewalUseCase,
		Clock clock
	) {
		this(subscriptionRepository, renewalAttemptRepository, null, processRenewalUseCase, clock, new RenewalRetryProperties(null, null, null));
	}

	public SubscriptionRenewalScheduler(
		SubscriptionRepository subscriptionRepository,
		RenewalAttemptRepository renewalAttemptRepository,
		PaymentTransactionRepository paymentTransactionRepository,
		ProcessRenewalUseCase processRenewalUseCase,
		Clock clock,
		RenewalRetryProperties retryProperties
	) {
		this.subscriptionRepository = subscriptionRepository;
		this.renewalAttemptRepository = renewalAttemptRepository;
		this.paymentTransactionRepository = paymentTransactionRepository;
		this.processRenewalUseCase = processRenewalUseCase;
		this.clock = clock;
		this.retryProperties = retryProperties;
	}

	@Scheduled(cron = "${subscriptions.renewal.cron:0 */5 * * * *}", zone = "UTC")
	@Transactional
	public void processDueSubscriptions() {
		LocalDate today = LocalDate.now(clock);
		Instant now = Instant.now(clock);
		int maxAttempts = RenewalPolicy.defaultPolicy().maxAttempts();
		var dueSubscriptions = subscriptionRepository.findAllByStatusAndExpirationDateLessThanEqual(
			SubscriptionStatus.ACTIVE,
			today
		);
		if (dueSubscriptions.isEmpty()) {
			dueSubscriptions = subscriptionRepository.findAllByStatusAndExpirationDate(SubscriptionStatus.ACTIVE, today);
		}
		dueSubscriptions
			.forEach(subscription -> {
				int attempts = renewalAttemptRepository.countBySubscriptionIdAndRenewalDate(
					subscription.getId(), subscription.getExpirationDate()
				);
				var lastAttempt = renewalAttemptRepository.findTopBySubscriptionIdAndRenewalDateOrderByAttemptNumberDesc(
					subscription.getId(), subscription.getExpirationDate()
				);
				if (lastAttempt.isPresent() && lastAttempt.get().getStatus() == RenewalAttemptStatus.PENDING) {
					expireTimedOutPendingAttempt(subscription, lastAttempt.get(), now, maxAttempts);
					log.info("Waiting for payment confirmation for subscription {}", subscription.getId());
					return;
				}
				if (lastAttempt.isPresent() && lastAttempt.get().getStatus() == RenewalAttemptStatus.WAITING_RETRY
					&& lastAttempt.get().getNextRetryAt() != null
					&& lastAttempt.get().getNextRetryAt().isAfter(now)) {
					return;
				}
				if (attempts >= maxAttempts) {
					log.warn("Renewal attempt limit already reached for subscription {}", subscription.getId());
					return;
				}
				try {
					processRenewalUseCase.execute(new ProcessRenewalParam(
						subscription.getId(), subscription.getExpirationDate(), attempts + 1
					));
				} catch (RuntimeException exception) {
					log.error("Could not process renewal for subscription {}", subscription.getId(), exception);
				}
		});
	}

	private void expireTimedOutPendingAttempt(
		com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity subscription,
		com.fernando.sistema_assinaturas.dataprovider.database.entity.RenewalAttemptJpaEntity attemptEntity,
		Instant now,
		int maxAttempts
	) {
		if (attemptEntity.getAttemptedAt().plus(retryProperties.pendingTimeout()).isAfter(now)
			|| paymentTransactionRepository == null) {
			return;
		}

		paymentTransactionRepository.findByIdempotencyKey(attemptEntity.getIdempotencyKey()).ifPresent(payment ->
			paymentTransactionRepository.save(PaymentTransactionDatabaseMapper.toEntity(
				PaymentTransactionDatabaseMapper.toDomain(payment).applyProviderStatus(
					PaymentStatus.EXPIRED, null, "Renewal payment confirmation timed out", now
				)
			))
		);

		var attempt = RenewalAttemptDatabaseMapper.toDomain(attemptEntity);
		boolean finalFailure = attempt.getAttemptNumber() >= maxAttempts;
		renewalAttemptRepository.save(RenewalAttemptDatabaseMapper.toEntity(attempt.toBuilder()
			.status(finalFailure ? RenewalAttemptStatus.FAILED : RenewalAttemptStatus.WAITING_RETRY)
			.failureReason("Renewal payment confirmation timed out")
			.nextRetryAt(finalFailure ? null : now.plus(retryProperties.delayForFailure(attempt.getAttemptNumber())))
			.build()));
		if (finalFailure && subscription.getStatus() == SubscriptionStatus.ACTIVE) {
			subscription.setStatus(SubscriptionStatus.SUSPENDED);
			subscriptionRepository.save(subscription);
		}
	}

	@Scheduled(cron = "${subscriptions.pending-payment-expiration.cron:0 */5 * * * *}", zone = "UTC")
	@Transactional
	@Caching(evict = {
		@CacheEvict(cacheNames = "subscriptionsById", allEntries = true, cacheManager = "redisCacheManager"),
		@CacheEvict(cacheNames = "subscriptionsByUser", allEntries = true, cacheManager = "redisCacheManager")
	})
	public void expirePendingPayments() {
		Instant now = Instant.now(clock);
		var candidates = subscriptionRepository.findAllByStatusAndPendingPaymentExpiresAtLessThanEqual(
			SubscriptionStatus.PENDING_PAYMENT,
			now
		);
		candidates.forEach(candidate -> expirePendingPayment(candidate.getId(), now));
	}

	private void expirePendingPayment(java.util.UUID subscriptionId, Instant now) {
		var lockedEntity = subscriptionRepository.findByIdForUpdate(subscriptionId).orElse(null);
		if (lockedEntity == null) {
			return;
		}

		var subscription = SubscriptionDatabaseMapper.toDomain(lockedEntity);
		if (subscription.getStatus() != SubscriptionStatus.PENDING_PAYMENT
			|| subscription.getPendingPaymentExpiresAt() == null
			|| subscription.getPendingPaymentExpiresAt().isAfter(now)) {
			return;
		}

		if (paymentTransactionRepository != null) {
			var pendingPayments = paymentTransactionRepository
				.findAllBySubscriptionIdAndPaymentTypeAndStatusForUpdate(
					subscriptionId,
					PaymentType.INITIAL_CHECKOUT,
					PaymentStatus.PENDING
				);
			pendingPayments.forEach(payment -> paymentTransactionRepository.save(
				PaymentTransactionDatabaseMapper.toEntity(
					PaymentTransactionDatabaseMapper.toDomain(payment).applyProviderStatus(
						PaymentStatus.EXPIRED,
						null,
						"Checkout expired",
						now
					)
				)
			));
		}

		subscriptionRepository.save(
			SubscriptionDatabaseMapper.toEntity(subscription.expirePendingPayment())
		);
		log.info("Expired pending payment for subscription {}", subscriptionId);
	}
}
