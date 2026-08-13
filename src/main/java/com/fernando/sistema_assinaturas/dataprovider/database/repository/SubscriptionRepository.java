package com.fernando.sistema_assinaturas.dataprovider.database.repository;

import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.SubscriptionJpaEntity;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {

	@Query("select count(s) > 0 from SubscriptionJpaEntity s where s.userId = :userId and s.status = 'ACTIVE'")
	boolean existsByUserIdAndStatusActive(UUID userId);

	Optional<SubscriptionJpaEntity> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
}
