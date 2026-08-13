package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateSubscriptionRequest(
	@NotNull(message = "userId is required")
	UUID userId,
	@NotNull(message = "plan is required")
	Plan plan
) {
}
