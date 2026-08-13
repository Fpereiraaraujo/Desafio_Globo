package com.fernando.sistema_assinaturas.dataprovider.infinitepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fernando.sistema_assinaturas.config.InfinitePayProperties;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InfinitePayClient {

	private final RestClient restClient;
	private final InfinitePayProperties properties;

	public InfinitePayClient(InfinitePayProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(properties.normalizedBaseUrl()).build();
	}

	public CreateLinkResponse createLink(String orderNsu, int amountCents, String description) {
		return restClient.post()
			.uri("/links")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new CreateLinkRequest(
				properties.handle(),
				properties.redirectUrl(),
				orderNsu,
				List.of(new Item(1, amountCents, description))
			))
			.retrieve()
			.body(CreateLinkResponse.class);
	}

	public record CreateLinkRequest(
		String handle,
		@JsonProperty("redirect_url") String redirectUrl,
		@JsonProperty("order_nsu") String orderNsu,
		List<Item> items
	) {
	}

	public record Item(int quantity, int price, String description) {
	}

	public record CreateLinkResponse(String url) {
	}

}
