-- =====================================================================
-- V4 · Interacciones como actividades (P6, P7)
-- PROGRAMADA: actividad futura (reunión, llamada). Luego pasa a REALIZADA
-- (con su resultado) o a CANCELADA. Las interacciones nunca se borran.
-- =====================================================================

ALTER TABLE interaccion
    ADD COLUMN estado    ENUM('PROGRAMADA', 'REALIZADA', 'CANCELADA') NOT NULL DEFAULT 'REALIZADA' AFTER tipo,
    ADD COLUMN resultado TEXT NULL AFTER descripcion;

CREATE INDEX idx_interaccion_usuario_estado_fecha ON interaccion (usuario_id, estado, fecha);

-- Actividades agendadas para los próximos días (datos de prueba).
INSERT INTO interaccion (cliente_id, usuario_id, tipo, estado, fecha, asunto, descripcion) VALUES
(1,  3, 'LLAMADA', 'PROGRAMADA', '2026-10-21 16:00:00', 'Seguimiento de la propuesta de docking stations', 'Confirmar cantidades y fecha de entrega.'),
(4,  4, 'LLAMADA', 'PROGRAMADA', '2026-10-22 09:30:00', 'Respuesta sobre el descuento adicional',        'Confirmar con gerencia el 5% adicional antes de llamar.'),
(14, 5, 'REUNION', 'PROGRAMADA', '2026-10-23 10:00:00', 'Visita a la notaría',                           'Levantamiento del archivo a digitalizar.'),
(12, 4, 'REUNION', 'PROGRAMADA', '2026-10-27 11:00:00', 'Demo de laptops con 32 GB de RAM',              NULL);
