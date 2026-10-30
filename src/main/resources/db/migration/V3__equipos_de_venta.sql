-- =====================================================================
-- V3 · Equipos de venta (P3, P4)
-- Cada VENDEDOR pertenece al equipo de un GERENTE (usuario.gerente_id).
-- El GERENTE ve y gestiona lo de los vendedores de su equipo.
-- =====================================================================

ALTER TABLE usuario
    ADD COLUMN gerente_id INT NULL AFTER rol,
    ADD CONSTRAINT fk_usuario_gerente FOREIGN KEY (gerente_id) REFERENCES usuario (id);

-- Todos los vendedores del seed pertenecen al equipo de Luis Ramírez (gerente).
UPDATE usuario SET gerente_id = 2 WHERE id IN (3, 4, 5, 6);

-- El vendedor 6 está inactivo: su cliente pasa a su gerente (regla P4).
UPDATE cliente SET vendedor_id = 2 WHERE id = 9;

-- El cliente 14 no tenía vendedor pero su oportunidad es de Sofía Herrera (5):
-- solo el vendedor asignado puede registrarle oportunidades (P3), así que se le asigna.
UPDATE cliente SET vendedor_id = 5 WHERE id = 14;
