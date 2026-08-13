package com.fernando.sistema_assinaturas.core.usecase.imp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.User;
import com.fernando.sistema_assinaturas.core.domain.param.RegisterUserParam;
import com.fernando.sistema_assinaturas.core.service.UserRegistrationService;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.UserJpaEntity;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImpTest {

	private static final Instant NOW = Instant.parse("2026-08-12T23:00:00Z");

	@Mock
	private UserRepository userRepository;

	private RegisterUserUseCaseImp useCase;

	@BeforeEach
	void setUp() {
		useCase = new RegisterUserUseCaseImp(
			userRepository,
			new UserRegistrationService(Clock.fixed(NOW, ZoneOffset.UTC))
		);
	}

	@Test
	void registersUserWithNormalizedEmail() {
		when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
		when(userRepository.save(any(UserJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User user = useCase.execute(new RegisterUserParam(" Ana ", " ANA@EXAMPLE.COM "));

		assertThat(user.getId()).isNotNull();
		assertThat(user.getName()).isEqualTo("Ana");
		assertThat(user.getEmail()).isEqualTo("ana@example.com");
		assertThat(user.getCreatedAt()).isEqualTo(NOW);
		verify(userRepository).save(any(UserJpaEntity.class));
	}

	@Test
	void rejectsAlreadyRegisteredEmail() {
		when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

		assertThatThrownBy(() -> useCase.execute(new RegisterUserParam("Ana", "ANA@example.com")))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("User email is already registered");
	}
}
