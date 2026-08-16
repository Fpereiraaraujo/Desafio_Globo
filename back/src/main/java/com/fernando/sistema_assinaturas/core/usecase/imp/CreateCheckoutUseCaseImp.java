package com.fernando.sistema_assinaturas.core.usecase.imp;

import com.fernando.sistema_assinaturas.core.domain.model.CheckoutResult;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentStatus;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentTransaction;
import com.fernando.sistema_assinaturas.core.domain.model.PaymentType;
import com.fernando.sistema_assinaturas.core.domain.model.SubscriptionStatus;
import com.fernando.sistema_assinaturas.core.domain.param.CreateCheckoutParam;
import com.fernando.sistema_assinaturas.core.gateway.PaymentGateway;
import com.fernando.sistema_assinaturas.core.usecase.CreateCheckoutUseCase;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.PaymentTransactionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.mapper.SubscriptionDatabaseMapper;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.PaymentTransactionRepository;
import com.fernando.sistema_assinaturas.dataprovider.database.repository.SubscriptionRepository;
import com.fernando.sistema_assinaturas.entrypoint.api.exception.ResourceNotFoundException;
import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCheckoutUseCaseImp implements CreateCheckoutUseCase {

	private final SubscriptionRepository subscriptionRepository;
	private final PaymentTransactionRepository paymentTransactionRepository;
	private final PaymentGateway paymentGateway;
	private final Clock clock = Clock.systemUTC();

	@Override
	@Transactional
	public CheckoutResult execute(CreateCheckoutParam param) {
		UUID subscriptionId = param == null ? null : param.subscriptionId();
		if (subscriptionId == null) {
			throw new IllegalArgumentException("Subscription id is required");
		}

		var lockedEntity = Optional.ofNullable(subscriptionRepository.findByIdForUpdate(subscriptionId))
			.orElse(Optional.empty());
		var entity = lockedEntity
			.or(() -> subscriptionRepository.findById(subscriptionId))
			.orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
		var subscription = SubscriptionDatabaseMapper.toDomain(entity);
		if (subscription.getStatus() != SubscriptionStatus.PENDING_PAYMENT
			&& subscription.getStatus() != SubscriptionStatus.ACTIVE) {
			throw new IllegalStateException("Only subscriptions awaiting payment can create checkout");
		}

		var previousPayment = Optional.ofNullable(paymentTransactionRepository
			.findTopBySubscriptionIdAndPaymentTypeOrderByAttemptNumberDesc(subscriptionId, PaymentType.INITIAL_CHECKOUT))
			.orElse(Optional.empty());
		if (previousPayment.isPresent()) {
			var previous = PaymentTransactionDatabaseMapper.toDomain(previousPayment.get());
			if (!previous.getStatus().isFinal() || previous.getStatus() == PaymentStatus.APPROVED) {
				return new CheckoutResult(
					previous.getStatus(),
					previous.getCheckoutUrl(),
					previous.getIdempotencyKey(),
					previous.getProviderTransactionId(),
					"Existing checkout",
					previous.getId()
				);
			}
		}

		int attemptNumber = previousPayment.map(entityValue -> entityValue.getAttemptNumber() + 1).orElse(1);
		String orderNsu = subscription.getStatus() == SubscriptionStatus.ACTIVE
			? subscriptionId.toString()
			: subscriptionId + ":initial:" + attemptNumber;
		Instant now = Instant.now(clock);
		PaymentTransaction payment = PaymentTransaction.pending(
			UUID.randomUUID(),
			subscriptionId,
			PaymentType.INITIAL_CHECKOUT,
			attemptNumber,
			orderNsu,
			subscription.getPlan().monthlyPriceCents(),
			now
		);
		paymentTransactionRepository.saveAndFlush(PaymentTransactionDatabaseMapper.toEntity(payment));

		CheckoutResult providerResult;
		try {
			providerResult = paymentGateway.createCheckout(subscription, orderNsu);
		} catch (RuntimeException exception) {
			PaymentTransaction failed = payment.applyProviderStatus(
				PaymentStatus.FAILED,
				null,
				exception.getMessage(),
				Instant.now(clock)
			);
			paymentTransactionRepository.save(PaymentTransactionDatabaseMapper.toEntity(failed));
			return new CheckoutResult(
				failed.getStatus(), null, orderNsu, null, "Checkout creation failed", failed.getId()
			);
		}
		if (providerResult == null || providerResult.status() == null) {
			providerResult = new CheckoutResult(
				PaymentStatus.FAILED, null, orderNsu, null, "Checkout provider returned no result", payment.getId()
			);
		}

		PaymentTransaction updatedPayment = payment
			.applyProviderStatus(
				providerResult.status(),
				providerResult.providerTransactionId(),
				providerResult.status().isFinal()
					&& providerResult.status() != PaymentStatus.APPROVED
					? providerResult.message()
					: null,
				providerResult.status().isFinal() ? Instant.now(clock) : null
			)
			.toBuilder()
			.checkoutUrl(providerResult.checkoutUrl())
			.build();
		paymentTransactionRepository.save(PaymentTransactionDatabaseMapper.toEntity(updatedPayment));

		if (updatedPayment.getStatus() == PaymentStatus.APPROVED
			&& subscription.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
			subscriptionRepository.save(SubscriptionDatabaseMapper.toEntity(subscription.activate()));
		}

		return new CheckoutResult(
			updatedPayment.getStatus(),
			updatedPayment.getCheckoutUrl(),
			updatedPayment.getIdempotencyKey(),
			updatedPayment.getProviderTransactionId(),
			providerResult.message(),
			updatedPayment.getId()
		);
	}
}
