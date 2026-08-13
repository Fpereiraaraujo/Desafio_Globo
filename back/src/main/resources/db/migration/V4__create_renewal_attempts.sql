create table renewal_attempts (
    id uuid primary key,
    subscription_id uuid not null,
    renewal_date date not null,
    attempt_number integer not null,
    status varchar(32) not null,
    idempotency_key varchar(180) not null,
    failure_reason varchar(500),
    attempted_at timestamp with time zone not null,
    constraint uk_renewal_attempts_idempotency unique (idempotency_key),
    constraint fk_renewal_attempts_subscription foreign key (subscription_id) references subscriptions (id),
    constraint ck_renewal_attempts_number check (attempt_number > 0),
    constraint ck_renewal_attempts_status check (status in ('PENDING', 'SUCCEEDED', 'FAILED'))
);

create unique index uk_renewal_attempts_cycle_number
    on renewal_attempts (subscription_id, renewal_date, attempt_number);
