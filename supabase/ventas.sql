-- StockWise: ventas y estadísticas.
-- Correr una sola vez en Supabase: SQL Editor -> New query -> pegar todo -> Run.

-- 1. Costo de compra de cada producto (para calcular ganancia)
alter table productos
  add column if not exists costo numeric(12,2) not null default 0 check (costo >= 0);

-- 2. Cada venta registrada. Se copian precio y costo del momento de la venta:
--    si mañana cambia el precio, las ventas de ayer siguen calculando con el de ayer.
create table if not exists ventas (
  id uuid primary key default gen_random_uuid(),
  producto_id uuid not null references productos(id) on delete cascade,
  cantidad integer not null check (cantidad > 0),
  precio_unitario numeric(12,2) not null,
  costo_unitario numeric(12,2) not null,
  vendido_en timestamptz not null default now()
);

alter table ventas enable row level security;
-- Misma decisión que en productos: acceso público porque la demo no tiene login.
drop policy if exists "acceso publico" on ventas;
create policy "acceso publico" on ventas for all to anon using (true) with check (true);

-- 3. Registrar una venta = descontar stock + guardar la venta, en UNA transacción (todo o nada).
create or replace function registrar_venta(p_producto_id uuid, p_cantidad integer)
returns void
language plpgsql
set search_path = public -- fija dónde busca las tablas (recomendación del Security Advisor)
as $$
begin
  if p_cantidad is null or p_cantidad <= 0 then
    raise exception 'CANTIDAD_INVALIDA';
  end if;

  update productos
     set cantidad = cantidad - p_cantidad
   where id = p_producto_id
     and cantidad >= p_cantidad;

  if not found then
    raise exception 'STOCK_INSUFICIENTE';
  end if;

  insert into ventas (producto_id, cantidad, precio_unitario, costo_unitario)
  select id, p_cantidad, precio, costo
    from productos
   where id = p_producto_id;
end;
$$;

-- 4. Estadísticas por producto, calculadas en la base (viajan pocas filas aunque haya miles de ventas).
--    security_invoker = la vista respeta las políticas RLS de quien consulta.
create or replace view estadisticas_productos with (security_invoker = true) as
select p.id,
       p.nombre,
       sum(v.cantidad)                                          as unidades,
       sum(v.cantidad * v.precio_unitario)                      as ingresos,
       sum(v.cantidad * (v.precio_unitario - v.costo_unitario)) as ganancia
  from ventas v
  join productos p on p.id = v.producto_id
 group by p.id, p.nombre;
