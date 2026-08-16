package com.fernando.sistema_assinaturas.dataprovider.database.repository;

import com.fernando.sistema_assinaturas.dataprovider.database.entity.RenewalAttemptJpaEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RenewalAttemptRepository extends JpaRepository<RenewalAttemptJpaEntity, UUID> {

	List<RenewalAttemptJpaEntity> findAllBySubscriptionIdAndRenewalDateOrderByAttemptNumberAsc(
		UUID subscriptionId,
		LocalDate renewalDate
	);

	int countBySubscriptionIdAndRenewalDate(UUID subscriptionId, LocalDate renewalDate);

	Optional<RenewalAttemptJpaEntity> findBySubscriptionIdAndRenewalDateAndAttemptNumber(
		UUID subscriptionId,
		LocalDate renewalDate,
		int attemptNumber
	);

	Optional<RenewalAttemptJpaEntity> findByIdempotencyKey(String idempotencyKey);

	Optional<RenewalAttemptJpaEntity> findTopBySubscriptionIdAndRenewalDateOrderByAttemptNumberDesc(
		UUID subscriptionId,
		LocalDate renewalDate
	);
}
