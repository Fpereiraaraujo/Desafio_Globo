package com.fernando.sistema_assinaturas.core.domain.model;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Subscription {

	UUID id;
	UUID userId;
	Plan plan;
	LocalDate startDate;
	LocalDate expirationDate;
	SubscriptionStatus status;
	Instant canceledAt;

	public static Subscription create(UUID id, UUID userId, Plan plan, LocalDate startDate) {
		return create(id, userId, plan, startDate, SubscriptionStatus.ACTIVE);
	}

	public static Subscription createPendingPayment(UUID id, UUID userId, Plan plan, LocalDate startDate) {
		return create(id, userId, plan, startDate, SubscriptionStatus.PENDING_PAYMENT);
	}

	private static Subscription create(
		UUID id,
		UUID userId,
		Plan plan,
		LocalDate startDate,
		SubscriptionStatus status
	) {
		if (id == null || userId == null) {
			throw new IllegalArgumentException("Subscription and user ids are required");
		}
		if (plan == null) {
			throw new IllegalArgumentException("Subscription plan is required");
		}
		if (startDate == null) {
			throw new IllegalArgumentException("Subscription start date is required");
		}

		return Subscription.builder()
			.id(id)
			.userId(userId)
			.plan(plan)
			.startDate(startDate)
			.expirationDate(startDate.plusMonths(1))
			.status(status)
			.build();
	}

	public Subscription activate() {
		if (status == SubscriptionStatus.ACTIVE) {
			return this;
		}
		if (status != SubscriptionStatus.PENDING_PAYMENT) {
			throw new IllegalStateException("Only subscriptions awaiting payment can be activated");
		}
		return toBuilder().status(SubscriptionStatus.ACTIVE).build();
	}

	public Subscription cancel() {
		return cancel(Instant.now());
	}

	public Subscription cancel(Instant canceledAt) {
		if (status != SubscriptionStatus.ACTIVE) {
			throw new IllegalStateException("Only active subscriptions can be canceled");
		}
		if (canceledAt == null) {
			throw new IllegalArgumentException("Cancellation time is required");
		}
		return toBuilder().status(SubscriptionStatus.CANCELED).canceledAt(canceledAt).build();
	}

	public Subscription suspend() {
		if (status != SubscriptionStatus.ACTIVE) {
			throw new IllegalStateException("Only active subscriptions can be suspended");
		}
		return toBuilder().status(SubscriptionStatus.SUSPENDED).build();
	}
}
