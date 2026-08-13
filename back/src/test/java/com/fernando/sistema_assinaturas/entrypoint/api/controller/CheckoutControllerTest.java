package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.usecase.CreateCheckoutUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CheckoutController.class)
class CheckoutControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CreateCheckoutUseCase createCheckoutUseCase;

	@Test
	void returnsCheckoutUrl() throws Exception {
		UUID subscriptionId = UUID.randomUUID();
		when(createCheckoutUseCase.execute(any())).thenReturn(
			new CheckoutResult(PaymentStatus.PENDING, "https://checkout.test/1", subscriptionId.toString(), null, "created")
		);

		mockMvc.perform(post("/api/v1/subscriptions/{id}/checkout", subscriptionId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("PENDING"))
			.andExpect(jsonPath("$.checkoutUrl").value("https://checkout.test/1"))
			.andExpect(jsonPath("$.orderNsu").value(subscriptionId.toString()));
	}

	@Test
	void rejectsInvalidSubscriptionId() throws Exception {
		mockMvc.perform(post("/api/v1/subscriptions/not-a-uuid/checkout")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
}
