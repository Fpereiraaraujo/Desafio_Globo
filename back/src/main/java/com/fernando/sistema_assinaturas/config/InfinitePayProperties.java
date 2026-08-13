package com.fernando.sistema_assinaturas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infinitepay")
public record InfinitePayProperties(
	String apiBaseUrl,
	String handle,
	String redirectUrl
) {
	public String normalizedBaseUrl() {
		if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
			return "https://api.checkout.infinitepay.io";
		}
		return apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
	}
}
