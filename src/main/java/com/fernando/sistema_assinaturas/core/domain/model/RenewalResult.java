package com.fernando.sistema_assinaturas.core.domain.model;

import java.time.LocalDate;

public record RenewalResult(
	Subscription subscription,
	LocalDate renewedUntil
) {
}
