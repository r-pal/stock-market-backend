create table if not exists house_rename_audit (
  id bigserial primary key,
  house_id bigint not null references house_account(id),
  old_name text not null,
  new_name text not null,
  changed_by text not null,
  created_at timestamptz not null default now()
);

create index if not exists idx_house_rename_audit_house_id on house_rename_audit (house_id);
