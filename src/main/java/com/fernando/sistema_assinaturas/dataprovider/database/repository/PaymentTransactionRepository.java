package com.fernando.sistema_assinaturas.dataprovider.database.repository;

import com.fernando.sistema_assinaturas.dataprovider.database.entity.PaymentTransactionJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionJpaEntity, UUID> {

	Optional<PaymentTransactionJpaEntity> findByIdempotencyKey(String idempotencyKey);

	default Optional<PaymentTransactionJpaEntity> findByOrderNsu(String orderNsu) {
		return findByIdempotencyKey(orderNsu);
	}
}
