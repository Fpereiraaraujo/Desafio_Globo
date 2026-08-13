package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.param.CreateCheckoutParam;
import com.fernando.sistema_assinaturas.core.gateway.PaymentGateway;
import com.fernando.sistema_assinaturas.core.usecase.CreateCheckoutUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateCheckoutUseCaseImp implements CreateCheckoutUseCase {

	private final SubscriptionRepository subscriptionRepository;
	private final PaymentGateway paymentGateway;

	@Override
	public com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult execute(CreateCheckoutParam param) {
		UUID subscriptionId = param == null ? null : param.subscriptionId();
		if (subscriptionId == null) {
			throw new IllegalArgumentException("Subscription id is required");
		}

		var entity = subscriptionRepository.findById(subscriptionId)
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
		var subscription = SubscriptionDatabaseMapper.toDomain(entity);
		if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
			throw new IllegalStateException("Only active subscriptions can create checkout");
		}

		return paymentGateway.createCheckout(subscription, subscriptionId.toString());
	}
}
