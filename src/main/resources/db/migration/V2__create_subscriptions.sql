create table subscriptions (
    id uuid primary key,
    user_id uuid not null,
    plan varchar(32) not null,
    start_date date not null,
    expiration_date date not null,
    status varchar(32) not null,
    canceled_at timestamp with time zone,
    constraint fk_subscriptions_user foreign key (user_id) references users (id),
    constraint ck_subscriptions_plan check (plan in ('BASICO', 'PREMIUM', 'FAMILIA')),
    constraint ck_subscriptions_status check (status in ('ACTIVE', 'CANCELED', 'SUSPENDED', 'EXPIRED'))
);

create unique index uk_subscriptions_active_user
    on subscriptions (user_id)
    where status = 'ACTIVE';

create index ix_subscriptions_expiration_status
    on subscriptions (expiration_date, status);
