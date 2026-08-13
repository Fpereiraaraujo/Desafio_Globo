package com.fernando.sistema_assinaturas.core.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PaymentResultTest {

	@Test
	void identifiesApprovedPayment() {
		assertThat(new PaymentResult(PaymentStatus.APPROVED, "tx-1", "approved").isApproved()).isTrue();
	}

	@Test
	void identifiesNonApprovedPayment() {
		assertThat(new PaymentResult(PaymentStatus.DECLINED, null, "declined").isApproved()).isFalse();
	}
}
