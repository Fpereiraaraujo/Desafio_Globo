package com.fernando.sistema_assinaturas.core.domain.param;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import java.util.UUID;

public record CreateSubscriptionParam(
	UUID userId,
	Plan plan
) {
}
