package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.usecase.GetPaymentUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.PaymentTransactionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPaymentUseCaseImp implements GetPaymentUseCase {

	private final PaymentTransactionRepository paymentTransactionRepository;

	@Override
	public PaymentTransaction execute(UUID paymentId) {
		if (paymentId == null) {
			throw new IllegalArgumentException("Payment id is required");
		}
		return paymentTransactionRepository.findById(paymentId)
			.map(PaymentTransactionDatabaseMapper::toDomain)
			.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
	}
}
