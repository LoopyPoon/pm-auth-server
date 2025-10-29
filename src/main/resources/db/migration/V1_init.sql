create table if not exists users (
    id uuid primary key,
    email varchar(255) unique not null,
    password_hash varchar(255) not null,
    email_verified boolean not null default false,
    created_at timestamptz not null default now()
);

create table if not exists authorities (
    user_id uuid not null references users(id) on delete cascade,
    authority varchar(100) not null,
    primary key (user_id, authority)
);