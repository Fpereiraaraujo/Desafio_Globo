package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import com.fernando.sistema_assinaturas.core.usecase.RegisterUserUseCase;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.RegisterUserRequest;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.UserResponse;
import com.fernando.sistema_assinaturas.entrypoint.api.mapper.UserApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final RegisterUserUseCase registerUserUseCase;

	@PostMapping
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
		var user = registerUserUseCase.execute(UserApiMapper.toParam(request));
		return ResponseEntity.status(HttpStatus.CREATED).body(UserApiMapper.toResponse(user));
	}
}
