alter table subscriptions add column pending_payment_expires_at timestamp with time zone;

update subscriptions
set pending_payment_expires_at = current_timestamp
where status = 'PENDING_PAYMENT';

alter table subscriptions add constraint ck_subscriptions_pending_payment_expiration
    check (status <> 'PENDING_PAYMENT' or pending_payment_expires_at is not null);

create index ix_subscriptions_pending_payment_expiration
    on subscriptions (pending_payment_expires_at, status);
