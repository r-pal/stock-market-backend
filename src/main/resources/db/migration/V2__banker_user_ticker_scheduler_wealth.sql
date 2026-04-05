-- Banker user, investment wealth snapshot at buy, scheduler idempotency, ticker baselines

create table if not exists app_user (
  id bigserial primary key,
  username text not null unique,
  password_hash text not null,
  role text not null,
  created_at timestamptz not null default now()
);

create table if not exists scheduler_run (
  job_name text not null,
  bucket_key text not null,
  run_at timestamptz not null default now(),
  primary key (job_name, bucket_key)
);

create table if not exists house_ticker_baseline (
  house_id bigint primary key references house_account(id),
  last_share_price numeric(19, 6) not null,
  updated_at timestamptz not null default now()
);

alter table investment_position
  add column if not exists target_wealth_at_buy numeric(19, 2);

-- For existing empty installs, column added nullable first — backfill not needed if table empty.
update investment_position set target_wealth_at_buy = 0 where target_wealth_at_buy is null;
alter table investment_position alter column target_wealth_at_buy set not null;
