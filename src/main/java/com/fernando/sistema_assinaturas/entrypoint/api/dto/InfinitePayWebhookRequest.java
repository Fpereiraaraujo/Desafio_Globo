package com.fernando.sistema_assinaturas.entrypoint.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessPaymentWebhookParam;
import jakarta.validation.constraints.NotBlank;

public record InfinitePayWebhookRequest(
	@NotBlank @JsonProperty("order_nsu") String orderNsu,
	@JsonProperty("transaction_nsu") String transactionNsu,
	String status,
	Boolean paid,
	@JsonProperty("paid_amount") Integer paidAmount,
	Integer amount
) {

	public ProcessPaymentWebhookParam toParam() {
		return new ProcessPaymentWebhookParam(orderNsu, transactionNsu, resolveStatus());
	}

	private PaymentStatus resolveStatus() {
		if (status != null && !status.isBlank()) {
			return switch (status.trim().toUpperCase()) {
				case "PAID", "APPROVED", "SUCCESS", "SUCCEEDED" -> PaymentStatus.APPROVED;
				case "DECLINED" -> PaymentStatus.DECLINED;
				case "FAILED", "FAILURE" -> PaymentStatus.FAILED;
				case "PENDING" -> PaymentStatus.PENDING;
				default -> PaymentStatus.UNKNOWN;
			};
		}
		if (Boolean.TRUE.equals(paid)
			|| (paidAmount != null && amount != null && paidAmount >= amount && paidAmount > 0)) {
			return PaymentStatus.APPROVED;
		}
		return Boolean.FALSE.equals(paid) ? PaymentStatus.PENDING : PaymentStatus.UNKNOWN;
	}
}
