package com.fernando.sistema_assinaturas.entrypoint.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(
		ResourceNotFoundException exception,
		HttpServletRequest request
	) {
		return response(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler({BusinessException.class, IllegalArgumentException.class, IllegalStateException.class})
	public ResponseEntity<ApiErrorResponse> handleBusiness(
		RuntimeException exception,
		HttpServletRequest request
	) {
		return response(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		List<String> details = exception.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.toList();
		var error = new ApiErrorResponse(
			java.time.Instant.now(),
			HttpStatus.BAD_REQUEST.value(),
			"Bad Request",
			"Request validation failed",
			request.getRequestURI(),
			details
		);
		return ResponseEntity.badRequest().body(error);
	}

	private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message, String path) {
		return ResponseEntity.status(status)
			.body(ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, path));
	}
}
