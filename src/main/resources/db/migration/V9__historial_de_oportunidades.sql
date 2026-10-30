-- =====================================================================
-- V9 · Historial de etapas de las oportunidades (P8)
-- Cada cambio de etapa queda registrado (al crear, al avanzar/retroceder y al cerrar).
-- Una oportunidad PERDIDA no se reabre: se crea otra ligada con oportunidad_anterior_id.
-- =====================================================================

CREATE TABLE oportunidad_historial_etapa (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    oportunidad_id INT          NOT NULL,
    etapa_anterior ENUM('PROSPECTO', 'CALIFICADA', 'PROPUESTA', 'NEGOCIACION', 'GANADA', 'PERDIDA') NULL,  -- NULL = creación
    etapa_nueva    ENUM('PROSPECTO', 'CALIFICADA', 'PROPUESTA', 'NEGOCIACION', 'GANADA', 'PERDIDA') NOT NULL,
    usuario_id     INT          NOT NULL,  -- quién hizo el cambio
    fecha          DATETIME     NOT NULL,
    comentario     VARCHAR(255) NULL,
    CONSTRAINT fk_historial_oportunidad FOREIGN KEY (oportunidad_id) REFERENCES oportunidad (id),
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE INDEX idx_historial_oportunidad_fecha ON oportunidad_historial_etapa (oportunidad_id, fecha, id);

ALTER TABLE oportunidad
    ADD COLUMN oportunidad_anterior_id INT NULL AFTER usuario_id,
    ADD CONSTRAINT fk_oportunidad_anterior FOREIGN KEY (oportunidad_anterior_id) REFERENCES oportunidad (id);

-- Historial coherente con el seed: creación en creado_en y cierre GANADA en la fecha de su venta.
INSERT INTO oportunidad_historial_etapa (oportunidad_id, etapa_anterior, etapa_nueva, usuario_id, fecha, comentario) VALUES
-- 1 · Renovación de 10 laptops (GANADA con la venta 4)
(1,  NULL,          'PROSPECTO',   3, '2026-01-15 10:00:00', 'Primer contacto por renovación de equipo'),
(1,  'PROSPECTO',   'CALIFICADA',  3, '2026-01-22 10:00:00', 'Presupuesto aprobado para 10 equipos'),
(1,  'CALIFICADA',  'PROPUESTA',   3, '2026-02-05 11:30:00', 'Demo de ThinkPad E14'),
(1,  'PROPUESTA',   'NEGOCIACION', 3, '2026-02-20 09:15:00', 'Cotización por volumen enviada'),
(1,  'NEGOCIACION', 'GANADA',      3, '2026-02-28 12:00:00', 'Venta #4 registrada'),
-- 2 · Licencias Microsoft 365 (GANADA con la venta 6)
(2,  NULL,          'PROSPECTO',   3, '2026-02-20 16:00:00', NULL),
(2,  'PROSPECTO',   'PROPUESTA',   3, '2026-02-27 10:00:00', 'Propuesta de 12 licencias'),
(2,  'PROPUESTA',   'NEGOCIACION', 3, '2026-03-08 12:00:00', NULL),
(2,  'NEGOCIACION', 'GANADA',      3, '2026-03-15 16:30:00', 'Venta #6 registrada'),
-- 3 · Equipamiento de recepción (GANADA con la venta 8)
(3,  NULL,          'PROSPECTO',   4, '2026-03-25 12:00:00', 'Visita a la recepción'),
(3,  'PROSPECTO',   'CALIFICADA',  4, '2026-03-28 10:00:00', NULL),
(3,  'CALIFICADA',  'PROPUESTA',   4, '2026-04-02 10:00:00', 'Monitores, diademas e impresora'),
(3,  'PROPUESTA',   'GANADA',      4, '2026-04-10 11:20:00', 'Venta #8 registrada'),
-- 4 · Laboratorio de cómputo (abierta en NEGOCIACION)
(4,  NULL,          'PROSPECTO',   4, '2026-08-01 10:30:00', 'Laboratorio de 25 equipos'),
(4,  'PROSPECTO',   'CALIFICADA',  4, '2026-08-15 10:00:00', NULL),
(4,  'CALIFICADA',  'PROPUESTA',   4, '2026-09-18 09:00:00', 'Recorrido por el aula; piden arrendamiento'),
(4,  'PROPUESTA',   'NEGOCIACION', 4, '2026-10-08 17:00:00', 'Piden 5% adicional de descuento'),
-- 5 · Impresoras para sucursales (GANADA con la venta 11)
(5,  NULL,          'PROSPECTO',   5, '2026-05-15 13:00:00', NULL),
(5,  'PROSPECTO',   'PROPUESTA',   5, '2026-05-20 10:00:00', '4 impresoras láser'),
(5,  'PROPUESTA',   'GANADA',      5, '2026-05-30 14:00:00', 'Venta #11 registrada'),
-- 6 · Monitores para oficina de obra (PERDIDA)
(6,  NULL,          'PROSPECTO',   3, '2026-05-10 10:00:00', NULL),
(6,  'PROSPECTO',   'CALIFICADA',  3, '2026-05-12 10:00:00', 'Requieren 12 monitores'),
(6,  'CALIFICADA',  'PROPUESTA',   3, '2026-05-20 10:00:00', NULL),
(6,  'PROPUESTA',   'PERDIDA',     3, '2026-06-30 15:00:00', 'Eligieron un proveedor local'),
-- 7 · Estaciones de diseño 4K (GANADA con la venta 16)
(7,  NULL,          'PROSPECTO',   4, '2026-06-25 16:00:00', NULL),
(7,  'PROSPECTO',   'CALIFICADA',  4, '2026-06-30 10:00:00', NULL),
(7,  'CALIFICADA',  'PROPUESTA',   4, '2026-07-06 10:00:00', NULL),
(7,  'PROPUESTA',   'NEGOCIACION', 4, '2026-07-14 10:00:00', NULL),
(7,  'NEGOCIACION', 'GANADA',      4, '2026-07-20 13:00:00', 'Venta #16 registrada'),
-- 8 · Equipo para recepción del hotel (abierta en PROPUESTA)
(8,  NULL,          'PROSPECTO',   5, '2026-09-10 12:30:00', NULL),
(8,  'PROSPECTO',   'CALIFICADA',  5, '2026-09-15 10:00:00', NULL),
(8,  'CALIFICADA',  'PROPUESTA',   5, '2026-09-24 09:45:00', 'Propuesta por Q60,000 enviada'),
-- 9 · Punto de venta y laptop (abierta en PROSPECTO)
(9,  NULL,          'PROSPECTO',   3, '2026-08-25 17:30:00', 'Lead desde redes sociales'),
-- 10 · Laptops para equipo de desarrollo (abierta en CALIFICADA)
(10, NULL,          'PROSPECTO',   4, '2026-09-05 11:00:00', NULL),
(10, 'PROSPECTO',   'CALIFICADA',  4, '2026-09-12 10:00:00', 'Equipo de 6 desarrolladores'),
-- 11 · Licencias de antivirus (abierta en PROSPECTO)
(11, NULL,          'PROSPECTO',   5, '2026-09-30 08:30:00', NULL),
-- 12 · Docking stations y monitores (abierta en PROPUESTA; tuvo un retroceso)
(12, NULL,          'PROSPECTO',   3, '2026-09-15 09:00:00', NULL),
(12, 'PROSPECTO',   'CALIFICADA',  3, '2026-09-22 10:00:00', NULL),
(12, 'CALIFICADA',  'PROPUESTA',   3, '2026-09-29 10:00:00', NULL),
(12, 'PROPUESTA',   'CALIFICADA',  3, '2026-10-02 10:00:00', 'El cliente cambió el alcance; se recalifica'),
(12, 'CALIFICADA',  'PROPUESTA',   3, '2026-10-09 10:00:00', 'Nueva propuesta con monitores 27"'),
-- 13 · Laptop para uso personal (PERDIDA)
(13, NULL,          'PROSPECTO',   5, '2026-03-01 18:20:00', NULL),
(13, 'PROSPECTO',   'CALIFICADA',  5, '2026-03-02 18:20:00', 'Pregunta por meses sin intereses'),
(13, 'CALIFICADA',  'PERDIDA',     5, '2026-04-05 10:00:00', 'Compró en otra tienda por precio'),
-- 14 · Digitalización de archivo notarial (abierta en PROSPECTO)
(14, NULL,          'PROSPECTO',   5, '2026-10-14 13:15:00', NULL);

-- Oportunidades creadas en V6 a partir de las ventas pendientes del seed.
INSERT INTO oportunidad_historial_etapa (oportunidad_id, etapa_anterior, etapa_nueva, usuario_id, fecha, comentario)
SELECT id, NULL, 'NEGOCIACION', usuario_id, creado_en, 'Creada a partir de una venta que no se concretó'
FROM oportunidad
WHERE titulo LIKE 'Cotización en espera de pago%';
