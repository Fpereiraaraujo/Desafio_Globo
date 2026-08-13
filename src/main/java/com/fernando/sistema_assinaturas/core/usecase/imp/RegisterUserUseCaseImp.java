package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.User;
import com.fernando.sistema_assinaturas.core.domain.param.RegisterUserParam;
import com.fernando.sistema_assinaturas.core.service.UserRegistrationService;
import com.fernando.sistema_assinaturas.core.usecase.RegisterUserUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.UserDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCaseImp implements RegisterUserUseCase {

	private final UserRepository userRepository;
	private final UserRegistrationService userRegistrationService;

	@Override
	@Transactional
	public User execute(RegisterUserParam param) {
		User user = userRegistrationService.create(param);
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new IllegalArgumentException("User email is already registered");
		}

		return UserDatabaseMapper.toDomain(
			userRepository.save(UserDatabaseMapper.toEntity(user))
		);
	}
}
