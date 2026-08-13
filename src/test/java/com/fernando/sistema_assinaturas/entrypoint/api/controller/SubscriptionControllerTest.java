package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.usecase.CreateSubscriptionUseCase;
import com.fernando.sistema_assinaturas.core.usecase.CancelSubscriptionUseCase;
import com.fernando.sistema_assinaturas.core.usecase.GetSubscriptionUseCase;
import com.fernando.sistema_assinaturas.core.usecase.GetUserSubscriptionUseCase;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean private CreateSubscriptionUseCase createSubscriptionUseCase;
	@MockBean private CancelSubscriptionUseCase cancelSubscriptionUseCase;
	@MockBean private GetSubscriptionUseCase getSubscriptionUseCase;
	@MockBean private GetUserSubscriptionUseCase getUserSubscriptionUseCase;

	@Test
	void createsSubscriptionAndReturnsPlanPrice() throws Exception {
		UUID userId = UUID.randomUUID();
		Subscription subscription = Subscription.create(UUID.randomUUID(), userId, Plan.PREMIUM, LocalDate.of(2026, 8, 12));
		when(createSubscriptionUseCase.execute(any())).thenReturn(subscription);

		mockMvc.perform(post("/api/v1/subscriptions")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":\"" + userId + "\",\"plan\":\"PREMIUM\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.plan").value("PREMIUM"))
			.andExpect(jsonPath("$.monthlyPriceCents").value(3990));
	}
}
