package com.fernando.sistema_assinaturas.core.domain.model;

public enum PaymentStatus {
	PENDING,
	APPROVED,
	DECLINED,
	FAILED,
	UNKNOWN,
	EXPIRED;

	public boolean isFinal() {
		return this == APPROVED || this == DECLINED || this == FAILED || this == EXPIRED;
	}
}
