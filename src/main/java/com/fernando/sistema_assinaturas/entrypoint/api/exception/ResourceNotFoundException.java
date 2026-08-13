package com.fernando.sistema_assinaturas.entrypoint.api.exception;

public class ResourceNotFoundException extends IllegalArgumentException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
