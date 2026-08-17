package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import com.fernando.sistema_assinaturas.core.domain.param.CreateCheckoutParam;
import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.usecase.CreateCheckoutUseCase;
import com.fernando.sistema_assinaturas.config.InfinitePayProperties;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.CheckoutResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class CheckoutController {

	private final CreateCheckoutUseCase createCheckoutUseCase;
	private final InfinitePayProperties infinitePayProperties;

	@PostMapping("/{subscriptionId}/checkout")
	public ResponseEntity<CheckoutResponse> create(@PathVariable UUID subscriptionId) {
		var result = createCheckoutUseCase.execute(new CreateCheckoutParam(subscriptionId));
		return ResponseEntity.ok(CheckoutResponse.from(result));
	}

	@GetMapping("/checkout-url")
	public ResponseEntity<CheckoutUrlResponse> infinitePayCheckoutUrl(@RequestParam Plan plan) {
		String url = infinitePayProperties.configuredCheckoutUrl(plan);
		if (url == null) {
			throw new IllegalArgumentException("No InfinitePay checkout URL is configured for this plan");
		}
		return ResponseEntity.ok(new CheckoutUrlResponse(url));
	}

	public record CheckoutUrlResponse(String checkoutUrl) {
	}
}
