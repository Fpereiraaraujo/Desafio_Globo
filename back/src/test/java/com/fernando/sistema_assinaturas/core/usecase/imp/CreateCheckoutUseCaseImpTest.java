package com.fernando.sistema_assinaturas.core.usecase.imp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import com.fernando.sistema_assinaturas.core.domain.model.Subscription;
import com.fernando.sistema_assinaturas.core.domain.param.CreateCheckoutParam;
import com.fernando.sistema_assinaturas.core.gateway.PaymentGateway;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCheckoutUseCaseImpTest {

	@Mock private SubscriptionRepository subscriptionRepository;
	@Mock private PaymentTransactionRepository paymentTransactionRepository;
	@Mock private PaymentGateway paymentGateway;
	@InjectMocks private CreateCheckoutUseCaseImp useCase;

	@Test
	void createsCheckoutForActiveSubscription() {
		UUID id = UUID.randomUUID();
		var subscription = Subscription.create(id, UUID.randomUUID(), Plan.PREMIUM, LocalDate.of(2026, 8, 12));
		when(subscriptionRepository.findById(id)).thenReturn(Optional.of(SubscriptionDatabaseMapper.toEntity(subscription)));
		var checkout = new CheckoutResult(PaymentStatus.PENDING, "https://checkout.test/1", id.toString(), null, "created");
		when(paymentGateway.createCheckout(any(), eq(id.toString()))).thenReturn(checkout);

		var result = useCase.execute(new CreateCheckoutParam(id));

		assertThat(result.checkoutUrl()).isEqualTo("https://checkout.test/1");
		verify(paymentGateway).createCheckout(any(), eq(id.toString()));
	}
}
