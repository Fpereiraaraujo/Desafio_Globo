package com.fernando.sistema_assinaturas.core.domain.model;

public record RenewalPolicy(int maxAttempts) {

	public RenewalPolicy {
		if (maxAttempts < 1) {
			throw new IllegalArgumentException("maximum renewal attempts must be positive");
		}
	}

	public static RenewalPolicy defaultPolicy() {
		return new RenewalPolicy(3);
	}
}
