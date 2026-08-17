package com.fernando.sistema_assinaturas.dataprovider.database.repository;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentType;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.PaymentTransactionJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionJpaEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from PaymentTransactionJpaEntity p where p.id = :id")
	Optional<PaymentTransactionJpaEntity> findByIdForUpdate(UUID id);

	Optional<PaymentTransactionJpaEntity> findByIdempotencyKey(String idempotencyKey);

	Optional<PaymentTransactionJpaEntity> findTopBySubscriptionIdAndPaymentTypeOrderByAttemptNumberDesc(
		UUID subscriptionId,
		PaymentType paymentType
	);

	Optional<PaymentTransactionJpaEntity> findBySubscriptionIdAndPaymentTypeAndAttemptNumber(
		UUID subscriptionId,
		PaymentType paymentType,
		int attemptNumber
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from PaymentTransactionJpaEntity p where p.subscriptionId = :subscriptionId and p.paymentType = :paymentType and p.status = :status")
	List<PaymentTransactionJpaEntity> findAllBySubscriptionIdAndPaymentTypeAndStatusForUpdate(
		UUID subscriptionId,
		PaymentType paymentType,
		PaymentStatus status
	);

	default Optional<PaymentTransactionJpaEntity> findByOrderNsu(String orderNsu) {
		return findByIdempotencyKey(orderNsu);
	}
}
