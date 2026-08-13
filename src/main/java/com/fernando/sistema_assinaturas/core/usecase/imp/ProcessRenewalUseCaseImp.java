package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.RenewalProcessingResult;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalPolicy;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessRenewalParam;
import com.fernando.sistema_assinaturas.core.service.SubscriptionRenewalProcessingService;
import com.fernando.sistema_assinaturas.core.usecase.ProcessRenewalUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.PaymentTransactionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.RenewalAttemptDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.RenewalAttemptRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.ResourceNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
@RequiredArgsConstructor
public class ProcessRenewalUseCaseImp implements ProcessRenewalUseCase {

	private final SubscriptionRepository subscriptionRepository;
	private final PaymentTransactionRepository paymentTransactionRepository;
	private final RenewalAttemptRepository renewalAttemptRepository;
	private final SubscriptionRenewalProcessingService renewalProcessingService;
	private final Clock clock = Clock.systemUTC();

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(cacheNames = "subscriptionsById", key = "#param.subscriptionId()", cacheManager = "redisCacheManager"),
		@CacheEvict(cacheNames = "subscriptionsByUser", allEntries = true, cacheManager = "redisCacheManager")
	})
	public RenewalProcessingResult execute(ProcessRenewalParam param) {
		if (param == null || param.subscriptionId() == null) {
			throw new IllegalArgumentException("Subscription id is required");
		}
		LocalDate renewalDate = param.renewalDate() == null ? LocalDate.now(clock) : param.renewalDate();
		var entity = subscriptionRepository.findById(param.subscriptionId())
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
		var subscription = SubscriptionDatabaseMapper.toDomain(entity);
		var result = renewalProcessingService.process(subscription, param.attemptNumber());

		paymentTransactionRepository.save(PaymentTransactionDatabaseMapper.toEntity(result.transaction()));
		renewalAttemptRepository.save(RenewalAttemptDatabaseMapper.toEntity(result.attempt()));
		var finalSubscription = result.succeeded()
			? result.subscription()
			: (result.attempt().isFinalFailure(RenewalPolicy.defaultPolicy().maxAttempts())
				? subscription.suspend() : subscription);
		if (!finalSubscription.equals(subscription)) {
			subscriptionRepository.save(SubscriptionDatabaseMapper.toEntity(finalSubscription));
		}
		return new RenewalProcessingResult(result.attempt(), result.transaction(), finalSubscription);
	}
}
