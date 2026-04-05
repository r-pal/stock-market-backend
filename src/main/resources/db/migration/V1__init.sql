-- Core schema for Goblin Bank backend.

create table if not exists house_account (
  id bigserial primary key,
  house_name text not null,
  balance numeric(19, 2) not null default 0,
  account_rate_adjustment_per_hour numeric(12, 8) not null default 0,
  portal_password_hash text null,
  portal_password_updated_at timestamptz null,
  deleted_at timestamptz null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create unique index if not exists uq_house_account_house_name_active
  on house_account (lower(house_name))
  where deleted_at is null;

create table if not exists global_interest_config (
  id bigint primary key,
  base_rate_per_hour numeric(12, 8) not null,
  updated_by text null,
  updated_at timestamptz not null default now()
);

create table if not exists share_price_config (
  id bigint primary key,
  hype_sensitivity numeric(12, 8) not null,
  interest_horizon_hours integer not null,
  momentum_lookback_hours integer not null,
  updated_by text null,
  updated_at timestamptz not null default now()
);

create table if not exists history_config (
  id bigint primary key,
  snapshot_interval_minutes integer not null,
  updated_by text null,
  updated_at timestamptz not null default now()
);

create table if not exists game_clock_config (
  id bigint primary key,
  game_start_at timestamptz null,
  game_duration_minutes integer not null,
  updated_by text null,
  updated_at timestamptz not null default now()
);

create table if not exists ledger_entry (
  id bigserial primary key,
  account_id bigint not null references house_account(id),
  entry_type text not null,
  amount numeric(19, 2) not null,
  before_balance numeric(19, 2) not null,
  after_balance numeric(19, 2) not null,
  performed_by text null,
  created_at timestamptz not null default now()
);

create index if not exists idx_ledger_entry_account_created_at
  on ledger_entry (account_id, created_at desc);

create table if not exists investment_position (
  id bigserial primary key,
  buyer_house_id bigint not null references house_account(id),
  target_house_id bigint not null references house_account(id),
  principal_amount numeric(19, 2) not null,
  target_share_price_at_buy numeric(19, 6) not null,
  target_share_price_at_sell numeric(19, 6) null,
  payout_amount numeric(19, 2) null,
  status text not null,
  bought_at timestamptz not null default now(),
  sold_at timestamptz null
);

create index if not exists idx_investment_position_buyer_status
  on investment_position (buyer_house_id, status);

create index if not exists idx_investment_position_target_status
  on investment_position (target_house_id, status);

create table if not exists house_wealth_snapshot (
  id bigserial primary key,
  house_id bigint not null references house_account(id),
  captured_at timestamptz not null,
  minutes integer not null,
  house_name text null,
  balance numeric(19, 2) not null,
  share_price numeric(19, 6) not null,
  effective_rate_per_hour numeric(12, 8) not null
);

create unique index if not exists uq_house_wealth_snapshot_house_captured
  on house_wealth_snapshot (house_id, captured_at);

create index if not exists idx_house_wealth_snapshot_house_minutes
  on house_wealth_snapshot (house_id, minutes);

-- Seed singleton rows.
insert into global_interest_config (id, base_rate_per_hour, updated_by)
values (1, 0.05000000, 'seed')
on conflict (id) do nothing;

insert into share_price_config (id, hype_sensitivity, interest_horizon_hours, momentum_lookback_hours, updated_by)
values (1, 0.50000000, 24, 2, 'seed')
on conflict (id) do nothing;

insert into history_config (id, snapshot_interval_minutes, updated_by)
values (1, 20, 'seed')
on conflict (id) do nothing;

insert into game_clock_config (id, game_start_at, game_duration_minutes, updated_by)
values (1, null, 1440, 'seed')
on conflict (id) do nothing;

-- Seed initial six houses (banker may create/delete later).
insert into house_account (house_name, balance, account_rate_adjustment_per_hour)
values
  ('House1', 1000.00, 0.00000000),
  ('House2', 1000.00, 0.00000000),
  ('House3', 1000.00, 0.00000000),
  ('House4', 1000.00, 0.00000000),
  ('House5', 1000.00, 0.00000000),
  ('House6', 1000.00, 0.00000000)
on conflict do nothing;

