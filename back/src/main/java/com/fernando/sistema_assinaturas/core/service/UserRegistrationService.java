package com.fernando.sistema_assinaturas.core.service;

import com.fernando.sistema_assinaturas.core.domain.model.User;
import com.fernando.sistema_assinaturas.core.domain.param.RegisterUserParam;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationService {

	private final Clock clock;

	public UserRegistrationService() {
		this(Clock.systemUTC());
	}

	public UserRegistrationService(Clock clock) {
		this.clock = clock;
	}

	public User create(RegisterUserParam param) {
		if (param == null) {
			throw new IllegalArgumentException("User registration data is required");
		}

		return User.create(
			UUID.randomUUID(),
			param.name(),
			param.email(),
			Instant.now(clock)
		);
	}
}
