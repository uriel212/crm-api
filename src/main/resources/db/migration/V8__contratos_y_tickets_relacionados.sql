-- =====================================================================
-- V8 · Renovación de contratos (P19) y seguimiento de tickets cerrados (P21)
-- =====================================================================

-- Una renovación es un contrato nuevo ligado al anterior (cada contrato se renueva una vez).
ALTER TABLE contrato
    ADD COLUMN contrato_anterior_id INT NULL AFTER venta_id,
    ADD CONSTRAINT uk_contrato_anterior UNIQUE (contrato_anterior_id),
    ADD CONSTRAINT fk_contrato_anterior FOREIGN KEY (contrato_anterior_id) REFERENCES contrato (id);

-- Un ticket CERRADO no se reabre: se crea uno nuevo que apunta al cerrado.
ALTER TABLE ticket_soporte
    ADD COLUMN ticket_relacionado_id INT NULL AFTER usuario_id,
    ADD CONSTRAINT fk_ticket_relacionado FOREIGN KEY (ticket_relacionado_id) REFERENCES ticket_soporte (id);
