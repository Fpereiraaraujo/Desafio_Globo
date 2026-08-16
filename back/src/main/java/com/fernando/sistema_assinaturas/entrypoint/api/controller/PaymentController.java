package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import com.fernando.sistema_assinaturas.core.domain.param.UpdatePaymentStatusParam;
import com.fernando.sistema_assinaturas.core.usecase.GetPaymentUseCase;
import com.fernando.sistema_assinaturas.core.usecase.UpdatePaymentStatusUseCase;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.PaymentResponse;
import com.fernando.sistema_assinaturas.entrypoint.api.dto.UpdatePaymentStatusRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final GetPaymentUseCase getPaymentUseCase;
	private final UpdatePaymentStatusUseCase updatePaymentStatusUseCase;

	@GetMapping("/{paymentId}")
	public ResponseEntity<PaymentResponse> get(@PathVariable UUID paymentId) {
		return ResponseEntity.ok(PaymentResponse.from(getPaymentUseCase.execute(paymentId)));
	}

	@PostMapping("/{paymentId}/status")
	public ResponseEntity<PaymentResponse> updateStatus(
		@PathVariable UUID paymentId,
		@Valid @RequestBody UpdatePaymentStatusRequest request
	) {
		var payment = updatePaymentStatusUseCase.execute(new UpdatePaymentStatusParam(
			paymentId,
			request.status(),
			request.providerTransactionId(),
			request.failureReason()
		));
		return ResponseEntity.ok(PaymentResponse.from(payment));
	}
}
