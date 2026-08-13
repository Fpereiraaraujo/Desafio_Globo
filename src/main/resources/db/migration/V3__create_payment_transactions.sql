create table payment_transactions (
    id uuid primary key,
    subscription_id uuid not null,
    idempotency_key varchar(180) not null,
    amount_cents integer not null,
    status varchar(32) not null,
    provider_transaction_id varchar(180),
    created_at timestamp with time zone not null,
    completed_at timestamp with time zone,
    constraint uk_payment_transactions_idempotency unique (idempotency_key),
    constraint fk_payment_transactions_subscription foreign key (subscription_id) references subscriptions (id),
    constraint ck_payment_transactions_amount check (amount_cents > 0),
    constraint ck_payment_transactions_status check (status in ('PENDING', 'APPROVED', 'DECLINED', 'FAILED', 'UNKNOWN'))
);

create index ix_payment_transactions_subscription
    on payment_transactions (subscription_id, created_at);
