package com.fernando.sistema_assinaturas.core.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PaymentTransaction {

	UUID id;
	UUID subscriptionId;
	String idempotencyKey;
	int amountCents;
	PaymentStatus status;
	String providerTransactionId;
	Instant createdAt;
	Instant completedAt;
}
