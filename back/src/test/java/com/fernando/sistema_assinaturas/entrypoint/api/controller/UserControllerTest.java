package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fernando.sistema_assinaturas.core.domain.model.User;
import com.fernando.sistema_assinaturas.core.usecase.RegisterUserUseCase;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RegisterUserUseCase registerUserUseCase;

	@Test
	void registersUserAndReturnsCreated() throws Exception {
		UUID id = UUID.randomUUID();
		when(registerUserUseCase.execute(any())).thenReturn(
			User.create(id, "Ana", "ana@example.com", Instant.parse("2026-08-12T12:00:00Z"))
		);

		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ana\",\"email\":\"ana@example.com\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(id.toString()))
			.andExpect(jsonPath("$.email").value("ana@example.com"));
	}

	@Test
	void rejectsInvalidUserRequest() throws Exception {
		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"email\":\"invalid\"}"))
			.andExpect(status().isBadRequest());
	}
}
