alter table subscriptions drop constraint ck_subscriptions_status;

alter table subscriptions add constraint ck_subscriptions_status
    check (status in ('PENDING_PAYMENT', 'ACTIVE', 'CANCELED', 'SUSPENDED', 'EXPIRED'));

alter table payment_transactions drop constraint ck_payment_transactions_status;

alter table payment_transactions add constraint ck_payment_transactions_status
    check (status in ('PENDING', 'APPROVED', 'DECLINED', 'FAILED', 'UNKNOWN', 'EXPIRED'));

drop index if exists uk_subscriptions_active_user;

create unique index uk_subscriptions_current_user
    on subscriptions (user_id)
    where status in ('ACTIVE', 'PENDING_PAYMENT');

alter table payment_transactions add column payment_type varchar(32) not null default 'RENEWAL';
alter table payment_transactions add column attempt_number integer not null default 1;
alter table payment_transactions add column checkout_url varchar(500);
alter table payment_transactions add column failure_reason varchar(500);

alter table payment_transactions add constraint ck_payment_transactions_type
    check (payment_type in ('INITIAL_CHECKOUT', 'RENEWAL'));

alter table payment_transactions add constraint ck_payment_transactions_attempt
    check (attempt_number > 0);

create index ix_payment_transactions_subscription_type
    on payment_transactions (subscription_id, payment_type, attempt_number);
