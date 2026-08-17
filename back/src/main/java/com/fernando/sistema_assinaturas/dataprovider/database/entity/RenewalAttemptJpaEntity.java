package com.fernando.sistema_assinaturas.dataprovider.database.entity;

import com.fernando.sistema_assinaturas.core.domain.model.RenewalAttemptStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "renewal_attempts")
public class RenewalAttemptJpaEntity {

	@Id
	private UUID id;

	@Column(name = "subscription_id", nullable = false)
	private UUID subscriptionId;

	@Column(name = "renewal_date", nullable = false)
	private LocalDate renewalDate;

	@Column(name = "attempt_number", nullable = false)
	private int attemptNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RenewalAttemptStatus status;

	@Column(name = "idempotency_key", nullable = false, unique = true)
	private String idempotencyKey;

	@Column(name = "failure_reason")
	private String failureReason;

	@Column(name = "attempted_at", nullable = false)
	private Instant attemptedAt;

	@Column(name = "next_retry_at")
	private Instant nextRetryAt;
}
