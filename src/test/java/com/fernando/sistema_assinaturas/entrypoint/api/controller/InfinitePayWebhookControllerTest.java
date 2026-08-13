package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.usecase.ProcessPaymentWebhookUseCase;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InfinitePayWebhookController.class)
class InfinitePayWebhookControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

	@Test
	void receivesApprovedWebhook() throws Exception {
		when(processPaymentWebhookUseCase.execute(any())).thenReturn(PaymentTransaction.builder()
			.id(UUID.randomUUID())
			.idempotencyKey("order-1")
			.status(PaymentStatus.APPROVED)
			.providerTransactionId("transaction-1")
			.completedAt(Instant.parse("2026-08-12T12:00:00Z"))
			.build());

		mockMvc.perform(post("/api/v1/payments/infinitepay/webhook")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"order_nsu\":\"order-1\",\"transaction_nsu\":\"transaction-1\",\"paid\":true}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.orderNsu").value("order-1"))
			.andExpect(jsonPath("$.status").value("APPROVED"));
	}
}
