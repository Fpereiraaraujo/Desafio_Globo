package com.fernando.sistema_assinaturas.dataprovider.database.repository;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentType;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.PaymentTransactionJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionJpaEntity, UUID> {

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

	default Optional<PaymentTransactionJpaEntity> findByOrderNsu(String orderNsu) {
		return findByIdempotencyKey(orderNsu);
	}
}
