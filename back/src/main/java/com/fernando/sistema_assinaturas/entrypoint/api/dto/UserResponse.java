package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fernando.sistema_assinaturas.core.domain.model.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
	UUID id,
	String name,
	String email,
	Instant createdAt
) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
	}
}
