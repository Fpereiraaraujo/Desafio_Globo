package com.fernando.sistema_assinaturas.entrypoint.api.exception;

public class BusinessException extends IllegalArgumentException {

	public BusinessException(String message) {
		super(message);
	}
}
