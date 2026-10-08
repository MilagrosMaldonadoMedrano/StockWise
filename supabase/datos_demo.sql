-- StockWise: DATOS DE DEMOSTRACIÓN (opcional).
-- Carga ventas históricas (mayo a septiembre 2026) de los productos existentes para que
-- el gráfico mensual muestre una evolución. No modifica el stock: son ventas pasadas.
-- Si un producto no tiene costo cargado, se usa el 60% del precio como costo de demo.
--
-- Para borrar SOLO estos datos de demo:
--   delete from ventas where vendido_en < '2026-10-01';

insert into ventas (producto_id, cantidad, precio_unitario, costo_unitario, vendido_en)
select p.id,
       d.cantidad,
       p.precio,
       coalesce(nullif(p.costo, 0), round(p.precio * 0.6, 2)),
       d.fecha
  from (values
        ('Yerba Mate 1kg',   8, timestamptz '2026-05-12 11:00-03'),
        ('Café molido 500g', 3, timestamptz '2026-05-20 17:30-03'),
        ('Yerba Mate 1kg',  10, timestamptz '2026-06-09 10:15-03'),
        ('Leche entera 1L', 15, timestamptz '2026-06-18 09:40-03'),
        ('Detergente 750ml', 4, timestamptz '2026-06-25 18:05-03'),
        ('Yerba Mate 1kg',  12, timestamptz '2026-07-07 12:20-03'),
        ('Café molido 500g', 6, timestamptz '2026-07-15 16:45-03'),
        ('Alfajor',         20, timestamptz '2026-07-22 13:10-03'),
        ('Leche entera 1L', 18, timestamptz '2026-08-04 08:50-03'),
        ('Bizcochitos',     10, timestamptz '2026-08-19 17:00-03'),
        ('Yerba Mate 1kg',   9, timestamptz '2026-08-28 11:35-03'),
        ('Café molido 500g', 7, timestamptz '2026-09-03 15:20-03'),
        ('Alfajor',         25, timestamptz '2026-09-12 14:00-03'),
        ('Yerba Mate 1kg',  14, timestamptz '2026-09-21 10:30-03'),
        ('Detergente 750ml', 6, timestamptz '2026-09-27 19:15-03')
       ) as d(nombre, cantidad, fecha)
  join productos p on p.nombre = d.nombre;
