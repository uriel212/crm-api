-- =====================================================================
-- V7 · Campañas cancelables (P15) y promociones sobre el total (P17)
-- =====================================================================

ALTER TABLE campana
    MODIFY COLUMN estado ENUM('PLANEADA', 'ACTIVA', 'FINALIZADA', 'CANCELADA') NOT NULL DEFAULT 'PLANEADA';

-- alcance PRODUCTO: descuento por renglón (MONTO_FIJO = quetzales por unidad).
-- alcance VENTA: descuento sobre el total (MONTO_FIJO = quetzales una sola vez),
--                solo si el subtotal de la compra alcanza monto_minimo.
ALTER TABLE promocion
    ADD COLUMN alcance      ENUM('PRODUCTO', 'VENTA') NOT NULL DEFAULT 'PRODUCTO' AFTER tipo,
    ADD COLUMN monto_minimo DECIMAL(12,2) NULL AFTER valor,
    ADD CONSTRAINT chk_promocion_monto_minimo CHECK (monto_minimo IS NULL OR monto_minimo >= 0);

INSERT INTO promocion (campana_id, nombre, tipo, alcance, valor, monto_minimo, fecha_inicio, fecha_fin, activa) VALUES
(NULL, 'Q500 de descuento en compras desde Q10,000', 'MONTO_FIJO', 'VENTA', 500.00, 10000.00, '2026-10-01', '2026-12-31', TRUE);
