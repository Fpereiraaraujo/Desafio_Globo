package com.fernando.sistema_assinaturas.core.domain.model;

public enum Plan {

	BASICO(1_990),
	PREMIUM(3_990),
	FAMILIA(5_990);

	private final int monthlyPriceCents;

	Plan(int monthlyPriceCents) {
		this.monthlyPriceCents = monthlyPriceCents;
	}

	public int monthlyPriceCents() {
		return monthlyPriceCents;
	}
}
