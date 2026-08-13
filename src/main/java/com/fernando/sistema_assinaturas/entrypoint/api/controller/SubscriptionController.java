package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import com.fernando.sistema_assinaturas.core.usecase.CreateSubscriptionUseCase;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.CreateSubscriptionRequest;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.SubscriptionResponse;
import com.fernando.sistema_assinaturas.entrypoint.api.mapper.SubscriptionApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

	private final CreateSubscriptionUseCase createSubscriptionUseCase;

	@PostMapping
	public ResponseEntity<SubscriptionResponse> create(
		@Valid @RequestBody CreateSubscriptionRequest request
	) {
		var subscription = createSubscriptionUseCase.execute(SubscriptionApiMapper.toParam(request));
		return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionApiMapper.toResponse(subscription));
	}
}
