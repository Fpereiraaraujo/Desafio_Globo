create table users (
    id uuid primary key,
    name varchar(160) not null,
    email varchar(320) not null,
    created_at timestamp with time zone not null,
    constraint uk_users_email unique (email)
);
