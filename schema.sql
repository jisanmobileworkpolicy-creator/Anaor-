-- Run this in Supabase SQL Editor.
create table if not exists public.products (
  id text primary key,
  name text not null,
  description text not null default '',
  price numeric(12,2) not null default 0 check (price >= 0),
  image_url text not null default '',
  category text not null default '',
  stock integer not null default 0 check (stock >= 0),
  created_at timestamptz not null default now()
);

create table if not exists public.store_orders (
  id bigint generated always as identity primary key,
  customer_name text not null,
  phone text not null,
  address text not null,
  note text not null default '',
  total numeric(12,2) not null check (total >= 0),
  status text not null default 'pending',
  created_at timestamptz not null default now()
);

create table if not exists public.store_order_items (
  id bigint generated always as identity primary key,
  order_id bigint not null references public.store_orders(id) on delete cascade,
  product_id text not null,
  product_name text not null,
  unit_price numeric(12,2) not null,
  quantity integer not null check (quantity > 0),
  line_total numeric(12,2) not null
);

alter table public.products enable row level security;
alter table public.store_orders enable row level security;
alter table public.store_order_items enable row level security;

-- Product listing is public. Writes are done only by the server using service-role key.
drop policy if exists "Public can read products" on public.products;
create policy "Public can read products" on public.products
  for select to anon, authenticated using (true);

-- Orders have no public table policies; clients must use the API.
revoke all on public.store_orders from anon, authenticated;
revoke all on public.store_order_items from anon, authenticated;

create or replace function public.create_store_order(
  p_customer_name text,
  p_phone text,
  p_address text,
  p_note text,
  p_items jsonb,
  p_total numeric
) returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  new_order_id bigint;
  line jsonb;
  product_row public.products%rowtype;
  qty integer;
begin
  -- Lock product rows to avoid concurrent orders overselling stock.
  for line in select value from jsonb_array_elements(p_items)
  loop
    qty := (line->>'quantity')::integer;
    select * into product_row from public.products
      where id = line->>'product_id' for update;
    if not found or product_row.stock < qty then
      raise exception 'Product missing or insufficient stock';
    end if;
  end loop;

  insert into public.store_orders(customer_name, phone, address, note, total)
  values (p_customer_name, p_phone, p_address, p_note, p_total)
  returning id into new_order_id;

  for line in select value from jsonb_array_elements(p_items)
  loop
    qty := (line->>'quantity')::integer;
    select * into product_row from public.products
      where id = line->>'product_id' for update;

    insert into public.store_order_items(order_id, product_id, product_name, unit_price, quantity, line_total)
    values (new_order_id, product_row.id, product_row.name, product_row.price, qty, product_row.price * qty);

    update public.products set stock = stock - qty where id = product_row.id;
  end loop;

  return jsonb_build_object('id', new_order_id, 'status', 'pending', 'total', p_total);
end;
$$;

revoke all on function public.create_store_order(text,text,text,text,jsonb,numeric) from public, anon, authenticated;
grant execute on function public.create_store_order(text,text,text,text,jsonb,numeric) to service_role;
