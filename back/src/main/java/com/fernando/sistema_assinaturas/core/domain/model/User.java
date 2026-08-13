package com.fernando.sistema_assinaturas.core.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class User {

	UUID id;
	String name;
	String email;
	Instant createdAt;

	public static User create(UUID id, String name, String email, Instant createdAt) {
		if (id == null) {
			throw new IllegalArgumentException("User id is required");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("User name is required");
		}
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("User email is required");
		}
		if (createdAt == null) {
			throw new IllegalArgumentException("User creation time is required");
		}

		return User.builder()
			.id(id)
			.name(name.trim())
			.email(normalizeEmail(email))
			.createdAt(createdAt)
			.build();
	}

	public static String normalizeEmail(String email) {
		Objects.requireNonNull(email, "email is required");
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
