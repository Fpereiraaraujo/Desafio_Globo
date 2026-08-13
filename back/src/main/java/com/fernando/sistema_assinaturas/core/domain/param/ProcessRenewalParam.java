package com.fernando.sistema_assinaturas.core.domain.param;

import java.time.LocalDate;
import java.util.UUID;

public record ProcessRenewalParam(
	UUID subscriptionId,
	LocalDate renewalDate,
	int attemptNumber
) {
}
