package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import com.fernando.sistema_assinaturas.core.usecase.CreateSubscriptionUseCase;
import com.fernando.sistema_assinaturas.core.usecase.CancelSubscriptionUseCase;
import com.fernando.sistema_assinaturas.core.usecase.GetSubscriptionUseCase;
import com.fernando.sistema_assinaturas.core.usecase.GetUserSubscriptionUseCase;
import com.fernando.sistema_assinaturas.core.domain.param.CancelSubscriptionParam;
import com.fernando.sistema_assinaturas.core.domain.param.GetSubscriptionParam;
import com.fernando.sistema_assinaturas.core.domain.param.GetUserSubscriptionParam;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.CreateSubscriptionRequest;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.SubscriptionResponse;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.GetUserSubscriptionResponse;
import com.fernando.sistema_assinaturas.entrypoint.api.mapper.SubscriptionApiMapper;
import com.fernando.sistema_assinaturas.entrypoint.api.mapper.SubscriptionQueryApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

	private final CreateSubscriptionUseCase createSubscriptionUseCase;
	private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
	private final GetSubscriptionUseCase getSubscriptionUseCase;
	private final GetUserSubscriptionUseCase getUserSubscriptionUseCase;

	@PostMapping
	public ResponseEntity<SubscriptionResponse> create(
		@Valid @RequestBody CreateSubscriptionRequest request
	) {
		var subscription = createSubscriptionUseCase.execute(SubscriptionApiMapper.toParam(request));
		return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionApiMapper.toResponse(subscription));
	}

	@PostMapping("/{subscriptionId}/cancel")
	public ResponseEntity<SubscriptionResponse> cancel(@PathVariable java.util.UUID subscriptionId) {
		var subscription = cancelSubscriptionUseCase.execute(new CancelSubscriptionParam(subscriptionId));
		return ResponseEntity.ok(SubscriptionApiMapper.toResponse(subscription));
	}

	@GetMapping("/{subscriptionId}")
	public ResponseEntity<SubscriptionResponse> get(@PathVariable java.util.UUID subscriptionId) {
		var subscription = getSubscriptionUseCase.execute(new GetSubscriptionParam(subscriptionId));
		return ResponseEntity.ok(SubscriptionApiMapper.toResponse(subscription));
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<GetUserSubscriptionResponse> getByUser(@PathVariable java.util.UUID userId) {
		var subscription = getUserSubscriptionUseCase.execute(new GetUserSubscriptionParam(userId));
		return ResponseEntity.ok(SubscriptionQueryApiMapper.toUserResponse(subscription));
	}
}
