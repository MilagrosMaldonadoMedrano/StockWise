-- StockWise: estadísticas por producto y por mes.
-- Correr una vez en Supabase: SQL Editor -> New query -> pegar -> Run.
-- Requiere haber corrido antes ventas.sql.

-- El mes se calcula en hora de Argentina: la base guarda en UTC, y sin la conversión
-- una venta del 31 a las 22 h (01 h UTC del día siguiente) caería en el mes siguiente.
create or replace view estadisticas_mensuales with (security_invoker = true) as
select p.id                                                                         as producto_id,
       p.nombre,
       to_char(v.vendido_en at time zone 'America/Argentina/Buenos_Aires', 'YYYY-MM') as mes,
       sum(v.cantidad)                                                              as unidades,
       sum(v.cantidad * v.precio_unitario)                                          as ingresos,
       sum(v.cantidad * (v.precio_unitario - v.costo_unitario))                     as ganancia
  from ventas v
  join productos p on p.id = v.producto_id
 group by p.id, p.nombre, mes;
