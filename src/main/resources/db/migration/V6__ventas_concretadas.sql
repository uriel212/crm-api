-- =====================================================================
-- V6 · Ventas concretadas (P11, P12, P13, P23) y descuento sobre el total (P17)
-- Una venta solo existe si se concretó (pagada): no hay PENDIENTE ni CANCELADA.
-- El "trato en proceso" se representa con una oportunidad del pipeline.
-- =====================================================================

-- Las ventas PENDIENTE del seed pasan a ser oportunidades en negociación.
INSERT INTO oportunidad (cliente_id, usuario_id, titulo, monto_estimado, etapa, fecha_cierre_estimada, creado_en)
SELECT cliente_id, usuario_id, CONCAT('Cotización en espera de pago (antes venta #', id, ')'),
       total, 'NEGOCIACION', '2026-10-31', fecha
FROM venta
WHERE estado = 'PENDIENTE';

-- Se eliminan las ventas que no se concretaron (sus renglones se borran en cascada;
-- no tienen puntos, contratos ni movimientos de inventario asociados).
DELETE FROM venta WHERE estado IN ('PENDIENTE', 'CANCELADA');

ALTER TABLE venta DROP COLUMN estado;

-- Promoción sobre el total de la compra (alcance VENTA):
-- total = subtotal (suma de renglones) - descuento.
ALTER TABLE venta
    ADD COLUMN subtotal     DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER fecha,
    ADD COLUMN descuento    DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER subtotal,
    ADD COLUMN promocion_id INT NULL AFTER descuento,
    ADD CONSTRAINT chk_venta_subtotal CHECK (subtotal >= 0),
    ADD CONSTRAINT chk_venta_descuento CHECK (descuento >= 0),
    ADD CONSTRAINT fk_venta_promocion FOREIGN KEY (promocion_id) REFERENCES promocion (id);

UPDATE venta SET subtotal = total WHERE id > 0;

CREATE INDEX idx_venta_fecha ON venta (fecha);
