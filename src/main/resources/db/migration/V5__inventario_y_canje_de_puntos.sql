-- =====================================================================
-- V5 · Inventario con historial (P9) y canje de puntos por productos (P22)
-- =====================================================================

-- Precio en puntos: solo los productos que lo tienen se pueden canjear.
ALTER TABLE producto
    ADD COLUMN precio_puntos INT NULL AFTER precio,
    ADD CONSTRAINT chk_producto_precio_puntos CHECK (precio_puntos IS NULL OR precio_puntos > 0);

-- Kardex: cada cambio de stock deja un movimiento. No se edita ni se borra.
-- ENTRADA = llegó mercancía, SALIDA = ajuste manual (merma, conteo),
-- VENTA = salió por una venta, CANJE = salió por un canje de puntos.
CREATE TABLE movimiento_inventario (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    producto_id      INT          NOT NULL,
    usuario_id       INT          NULL,
    venta_id         INT          NULL,
    tipo             ENUM('ENTRADA', 'SALIDA', 'VENTA', 'CANJE') NOT NULL,
    cantidad         INT          NOT NULL,
    stock_resultante INT          NOT NULL,
    motivo           VARCHAR(150) NOT NULL,
    fecha            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_movinv_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_movinv_stock CHECK (stock_resultante >= 0),
    CONSTRAINT fk_movinv_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_movinv_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_movinv_venta FOREIGN KEY (venta_id) REFERENCES venta (id)
);

CREATE INDEX idx_movinv_producto_fecha ON movimiento_inventario (producto_id, fecha);

-- Un canje (CANJEADO) indica qué producto se entregó, cuántas unidades y quién lo registró.
ALTER TABLE movimiento_puntos
    ADD COLUMN producto_id INT NULL AFTER venta_id,
    ADD COLUMN cantidad    INT NULL AFTER producto_id,
    ADD COLUMN usuario_id  INT NULL AFTER cantidad,
    ADD CONSTRAINT chk_movimiento_cantidad CHECK (cantidad IS NULL OR cantidad > 0),
    ADD CONSTRAINT fk_movimiento_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    ADD CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);

-- Productos canjeables del seed (1 punto ≈ Q1 del precio del producto).
UPDATE producto SET precio_puntos = 2200 WHERE id = 6;
UPDATE producto SET precio_puntos = 1900 WHERE id = 7;
UPDATE producto SET precio_puntos = 3500 WHERE id = 9;
UPDATE producto SET precio_puntos =  900 WHERE id = 13;

-- El kardex arranca con el stock actual de cada producto (las ventas anteriores no tienen movimiento).
INSERT INTO movimiento_inventario (producto_id, usuario_id, tipo, cantidad, stock_resultante, motivo, fecha)
SELECT id, 1, 'ENTRADA', stock, stock, 'Saldo inicial del kardex', '2026-10-19 00:00:00'
FROM producto
WHERE stock > 0;
