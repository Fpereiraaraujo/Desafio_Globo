package com.fernando.sistema_assinaturas.core.scheduler;

import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttemptStatus;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessRenewalParam;
import com.fernando.sistema_assinaturas.core.usecase.ProcessRenewalUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.RenewalAttemptRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.time.Clock;
import java.time.LocalDate;
import com.fernando.sistema_assinaturas.core.domain.model.RenewalPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubscriptionRenewalScheduler {

	private final SubscriptionRepository subscriptionRepository;
	private final RenewalAttemptRepository renewalAttemptRepository;
	private final ProcessRenewalUseCase processRenewalUseCase;
	private final Clock clock;

	@Autowired
	public SubscriptionRenewalScheduler(
		SubscriptionRepository subscriptionRepository,
		RenewalAttemptRepository renewalAttemptRepository,
		ProcessRenewalUseCase processRenewalUseCase
	) {
		this(subscriptionRepository, renewalAttemptRepository, processRenewalUseCase, Clock.systemUTC());
	}

	public SubscriptionRenewalScheduler(
		SubscriptionRepository subscriptionRepository,
		RenewalAttemptRepository renewalAttemptRepository,
		ProcessRenewalUseCase processRenewalUseCase,
		Clock clock
	) {
		this.subscriptionRepository = subscriptionRepository;
		this.renewalAttemptRepository = renewalAttemptRepository;
		this.processRenewalUseCase = processRenewalUseCase;
		this.clock = clock;
	}

	@Scheduled(cron = "${subscriptions.renewal.cron:0 0 2 * * *}")
	public void processDueSubscriptions() {
		LocalDate today = LocalDate.now(clock);
		int maxAttempts = RenewalPolicy.defaultPolicy().maxAttempts();
		subscriptionRepository.findAllByStatusAndExpirationDate(SubscriptionStatus.ACTIVE, today)
			.forEach(subscription -> {
				int attempts = renewalAttemptRepository.countBySubscriptionIdAndRenewalDate(
					subscription.getId(), subscription.getExpirationDate()
				);
				if (attempts >= maxAttempts) {
					log.warn("Renewal attempt limit already reached for subscription {}", subscription.getId());
					return;
				}
				for (int attemptNumber = attempts + 1; attemptNumber <= maxAttempts; attemptNumber++) {
					try {
						var result = processRenewalUseCase.execute(new ProcessRenewalParam(
							subscription.getId(), subscription.getExpirationDate(), attemptNumber
						));
						if (result == null || result.attempt().getStatus() != RenewalAttemptStatus.FAILED) {
							return;
						}
					} catch (RuntimeException exception) {
						log.error("Could not process renewal for subscription {}", subscription.getId(), exception);
						return;
					}
				}
			});
	}
}
