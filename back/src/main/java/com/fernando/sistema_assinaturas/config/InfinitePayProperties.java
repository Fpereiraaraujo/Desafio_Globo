package com.fernando.sistema_assinaturas.config;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infinitepay")
public record InfinitePayProperties(
	String apiBaseUrl,
	String handle,
	String redirectUrl,
	Map<String, String> checkoutUrls
) {
	public String normalizedBaseUrl() {
		if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
			return "https://api.checkout.infinitepay.io";
		}
		return apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
	}

	public String configuredCheckoutUrl(Plan plan) {
		if (checkoutUrls == null || plan == null) {
			return null;
		}
		String url = checkoutUrls.get(plan.name());
		return url == null || url.isBlank() ? null : url;
	}
}
