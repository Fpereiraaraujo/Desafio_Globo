package com.fernando.sistema_assinaturas.core.domain.model;

public record RenewalProcessingResult(
	RenewalAttempt attempt,
	PaymentTransaction transaction,
	Subscription subscription
) {

	public boolean succeeded() {
		return attempt.getStatus() == RenewalAttemptStatus.SUCCEEDED
			&& transaction.getStatus() == PaymentStatus.APPROVED;
	}
}
