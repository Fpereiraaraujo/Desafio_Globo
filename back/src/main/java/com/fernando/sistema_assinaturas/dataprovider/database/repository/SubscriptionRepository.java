package com.fernando.sistema_assinaturas.dataprovider.database.repository;

import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface SubscriptionRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {

	@Query("select count(s) > 0 from SubscriptionJpaEntity s where s.userId = :userId and s.status = 'ACTIVE'")
	boolean existsByUserIdAndStatusActive(UUID userId);

	boolean existsByUserIdAndStatus(UUID userId, SubscriptionStatus status);

	Optional<SubscriptionJpaEntity> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from SubscriptionJpaEntity s where s.id = :id")
	Optional<SubscriptionJpaEntity> findByIdForUpdate(UUID id);

	List<SubscriptionJpaEntity> findAllByStatusAndExpirationDateLessThanEqual(
		SubscriptionStatus status,
		LocalDate expirationDate
	);

	List<SubscriptionJpaEntity> findAllByStatusAndExpirationDate(
		SubscriptionStatus status,
		LocalDate expirationDate
	);

	List<SubscriptionJpaEntity> findAllByStatusAndPendingPaymentExpiresAtLessThanEqual(
		SubscriptionStatus status,
		Instant pendingPaymentExpiresAt
	);
}
