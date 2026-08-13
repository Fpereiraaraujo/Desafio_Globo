package com.fernando.sistema_assinaturas.entrypoint.api.mapper;

import com.fernando.sistema_assinaturas.core.domain.model.User;
import com.fernando.sistema_assinaturas.core.domain.param.RegisterUserParam;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.RegisterUserRequest;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.UserResponse;

public final class UserApiMapper {

	private UserApiMapper() {
	}

	public static RegisterUserParam toParam(RegisterUserRequest request) {
		return new RegisterUserParam(request.name(), request.email());
	}

	public static UserResponse toResponse(User user) {
		return UserResponse.from(user);
	}
}
