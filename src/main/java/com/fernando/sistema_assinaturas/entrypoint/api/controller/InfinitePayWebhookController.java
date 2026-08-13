package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import com.fernando.sistema_assinaturas.core.usecase.ProcessPaymentWebhookUseCase;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.InfinitePayWebhookRequest;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.PaymentWebhookResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/infinitepay")
@RequiredArgsConstructor
public class InfinitePayWebhookController {

	private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

	@PostMapping("/webhook")
	public ResponseEntity<PaymentWebhookResponse> receive(@Valid @RequestBody InfinitePayWebhookRequest request) {
		var transaction = processPaymentWebhookUseCase.execute(request.toParam());
		return ResponseEntity.ok(PaymentWebhookResponse.from(transaction));
	}
}
