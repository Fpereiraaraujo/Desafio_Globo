alter table renewal_attempts add column next_retry_at timestamp with time zone;

alter table renewal_attempts drop constraint ck_renewal_attempts_status;

alter table renewal_attempts add constraint ck_renewal_attempts_status
    check (status in ('PENDING', 'WAITING_RETRY', 'SUCCEEDED', 'FAILED'));

create index ix_renewal_attempts_retry_schedule
    on renewal_attempts (status, next_retry_at);
