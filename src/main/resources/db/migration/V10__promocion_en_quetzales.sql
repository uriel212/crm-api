-- =====================================================================
-- V10 · Moneda del nombre de la promoción 3 (P1: quetzales).
-- V2 la creó como "$300 menos ..."; V2 ya está aplicada y no se edita.
-- =====================================================================
UPDATE promocion
SET nombre = 'Q300 menos por licencia de Microsoft 365'
WHERE id = 3 AND nombre = '$300 menos por licencia de Microsoft 365';
