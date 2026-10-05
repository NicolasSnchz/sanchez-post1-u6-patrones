-- El cliente 4 es corporativo: tiene NIT registrado
INSERT INTO clientes (id, nombre, tipo_cliente, nit) VALUES
    (1, 'Cliente VIP', 'VIP', NULL),
    (2, 'Cliente Frecuente', 'FRECUENTE', NULL),
    (3, 'Cliente Moroso', 'MOROSO', NULL),
    (4, 'Cliente Estandar A', 'ESTANDAR', '900123456-1'),
    (5, 'Cliente Estandar B', 'ESTANDAR', NULL);

INSERT INTO productos (id, nombre, precio) VALUES
    (1, 'Teclado', 100000),
    (2, 'Mouse', 50000),
    (3, 'Monitor', 800000),
    (4, 'Cable USB', 5000);

INSERT INTO inventario (producto_id, stock) VALUES
    (1, 50),
    (2, 50),
    (3, 5),
    (4, 100);

-- Deuda pendiente del cliente moroso
INSERT INTO facturas (cliente_id, monto, pagada) VALUES
    (3, 150000, false);

-- Historial: el cliente frecuente tiene 5 pedidos previos (rango > 3 y <= 10 -> 4%)
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES
    (2, 10000, 0, 1900, 11900, TIMESTAMP '2026-01-10 10:00:00', 'CONFIRMADO'),
    (2, 10000, 0, 1900, 11900, TIMESTAMP '2026-02-10 10:00:00', 'CONFIRMADO'),
    (2, 10000, 0, 1900, 11900, TIMESTAMP '2026-03-10 10:00:00', 'CONFIRMADO'),
    (2, 10000, 0, 1900, 11900, TIMESTAMP '2026-04-10 10:00:00', 'CONFIRMADO'),
    (2, 10000, 0, 1900, 11900, TIMESTAMP '2026-05-10 10:00:00', 'CONFIRMADO');
