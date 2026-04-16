-- Generalized tradable stocks (house-backed + banker-managed items) and positions link.

create table if not exists tradable_stock (
  id bigserial primary key,
  display_name text not null,
  stock_type text not null, -- HOUSE | ITEM
  house_account_id bigint null references house_account(id),
  current_price numeric(19, 6) null,
  active boolean not null default true,
  created_by text null,
  created_at timestamptz not null default now(),
  updated_by text null,
  updated_at timestamptz not null default now(),
  constraint chk_tradable_stock_type
    check (
      (stock_type = 'HOUSE' and house_account_id is not null and current_price is null)
      or
      (stock_type = 'ITEM' and house_account_id is null and current_price is not null)
    )
);

create unique index if not exists uq_tradable_stock_house_account
  on tradable_stock (house_account_id)
  where stock_type = 'HOUSE';

create unique index if not exists uq_tradable_stock_display_name_active
  on tradable_stock (lower(display_name))
  where active;

-- Backfill HOUSE-backed stocks for each active house.
insert into tradable_stock (display_name, stock_type, house_account_id, current_price, created_by, updated_by)
select ha.house_name, 'HOUSE', ha.id, null, 'seed', 'seed'
from house_account ha
where ha.deleted_at is null
  and not exists (
    select 1
    from tradable_stock ts
    where ts.stock_type = 'HOUSE'
      and ts.house_account_id = ha.id
  );

alter table investment_position
  add column if not exists stock_id bigint;

alter table investment_position
  alter column target_house_id drop not null;

-- Link existing investment positions to the appropriate HOUSE-backed stock.
update investment_position ip
set stock_id = ts.id
from tradable_stock ts
where ip.stock_id is null
  and ip.target_house_id is not null
  and ts.stock_type = 'HOUSE'
  and ts.house_account_id = ip.target_house_id;

alter table investment_position
  alter column stock_id set not null;

alter table investment_position
  add constraint fk_investment_position_stock_id
    foreign key (stock_id) references tradable_stock(id);

