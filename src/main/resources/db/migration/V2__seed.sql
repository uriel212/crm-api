-- =====================================================================
-- CRM de Ventas · Datos de prueba (seed) (migración Flyway V2)
--
-- Se aplica después de V1__esquema.sql.
-- Para reiniciar los datos: borrar y recrear la base crm_ventas;
-- Flyway vuelve a aplicar V1 y V2 al arrancar.
--
-- Todos los datos son ficticios. Los correos usan el dominio .test.
-- Contraseña de todos los usuarios: Password123 (hash BCrypt)
-- =====================================================================

-- Workbench bloquea UPDATE sin WHERE por llave (safe updates); se desactiva
-- solo durante este script para calcular descuentos, subtotales y totales.
SET SQL_SAFE_UPDATES = 0;

-- ---------------------------------------------------------------------
-- Usuarios
-- ---------------------------------------------------------------------
INSERT INTO usuario (id, nombre, email, password_hash, rol, activo, creado_en) VALUES
(1, 'Ana Torres',     'ana.torres@crmventas.test',     '$2a$10$b6SuIcbAW9mH750dLl/qzOwC5kfkCwamTT26OeAhceeNbqPq44yGO', 'ADMIN',    TRUE,  '2025-09-01 09:00:00'),
(2, 'Luis Ramírez',   'luis.ramirez@crmventas.test',   '$2a$10$b6SuIcbAW9mH750dLl/qzOwC5kfkCwamTT26OeAhceeNbqPq44yGO', 'GERENTE',  TRUE,  '2025-09-01 09:00:00'),
(3, 'Carla Mendoza',  'carla.mendoza@crmventas.test',  '$2a$10$b6SuIcbAW9mH750dLl/qzOwC5kfkCwamTT26OeAhceeNbqPq44yGO', 'VENDEDOR', TRUE,  '2025-09-15 09:00:00'),
(4, 'Jorge Pérez',    'jorge.perez@crmventas.test',    '$2a$10$b6SuIcbAW9mH750dLl/qzOwC5kfkCwamTT26OeAhceeNbqPq44yGO', 'VENDEDOR', TRUE,  '2025-10-01 09:00:00'),
(5, 'Sofía Herrera',  'sofia.herrera@crmventas.test',  '$2a$10$b6SuIcbAW9mH750dLl/qzOwC5kfkCwamTT26OeAhceeNbqPq44yGO', 'VENDEDOR', TRUE,  '2026-01-05 09:00:00'),
(6, 'Mario Ruiz',     'mario.ruiz@crmventas.test',     '$2a$10$b6SuIcbAW9mH750dLl/qzOwC5kfkCwamTT26OeAhceeNbqPq44yGO', 'VENDEDOR', FALSE, '2025-09-15 09:00:00');

-- ---------------------------------------------------------------------
-- Clientes (vendedor 6 está inactivo; el cliente 14 no tiene vendedor)
-- ---------------------------------------------------------------------
INSERT INTO cliente (id, nombre, empresa, email, telefono, direccion, estado, vendedor_id, creado_en) VALUES
(1,  'Roberto Garza',     'Grupo Industrial del Norte',  'rgarza@gin.test',              '8112345678', 'Av. Constitución 1500, Monterrey, NL',           'ACTIVO',    3,    '2025-11-03 10:00:00'),
(2,  'Mariana López',     'Despacho López y Asociados',  'mlopez@lopezasoc.test',        '5512345601', 'Insurgentes Sur 1200, CDMX',                     'ACTIVO',    3,    '2025-12-10 12:30:00'),
(3,  'Fernando Castillo', 'Clínica Santa Fe',            'fcastillo@clinicasantafe.test','3312345602', 'Av. Vallarta 3200, Guadalajara, Jal.',           'ACTIVO',    4,    '2026-01-08 09:15:00'),
(4,  'Patricia Núñez',    'Colegio Montessori Valle',    'pnunez@montessorivalle.test',  '4421234503', 'Blvd. Bernardo Quintana 400, Querétaro, Qro.',   'ACTIVO',    4,    '2026-01-20 16:45:00'),
(5,  'Alejandro Ríos',    'Transportes Ríos',            'arios@transrios.test',         '8112345604', 'Carretera a Laredo km 12, Escobedo, NL',         'ACTIVO',    5,    '2026-02-02 11:00:00'),
(6,  'Daniela Ortega',    NULL,                          'dortega@correo.test',          '5512345605', 'Calle Durango 45, Roma Norte, CDMX',             'ACTIVO',    5,    '2026-02-15 18:20:00'),
(7,  'Héctor Salinas',    'Constructora Salinas',        'hsalinas@csalinas.test',       '2221234506', 'Av. Juárez 2100, Puebla, Pue.',                  'ACTIVO',    3,    '2026-03-05 10:30:00'),
(8,  'Lucía Fernández',   'Agencia Creativa Pixel',      'lfernandez@pixel.test',        '5512345607', 'Álvaro Obregón 120, Roma Norte, CDMX',           'ACTIVO',    4,    '2026-04-11 13:00:00'),
(9,  'Ricardo Méndez',    'Farmacias Méndez',            'rmendez@farmendez.test',       '9991234508', 'Calle 60 450, Centro, Mérida, Yuc.',             'INACTIVO',  6,    '2025-10-01 09:00:00'),
(10, 'Gabriela Soto',     'Hotel Costa Azul',            'gsoto@costaazul.test',         '9981234509', 'Blvd. Kukulcán km 9, Cancún, Q. Roo',            'ACTIVO',    5,    '2026-05-19 12:00:00'),
(11, 'Miguel Ángel Vega', 'Restaurante El Fogón',        'mvega@elfogon.test',           '3312345610', 'Av. Chapultepec 210, Guadalajara, Jal.',         'PROSPECTO', 3,    '2026-08-22 17:30:00'),
(12, 'Valeria Cruz',      'Nube Digital',                'vcruz@nubedigital.test',       '5512345611', 'Paseo de la Reforma 300, CDMX',                  'PROSPECTO', 4,    '2026-09-03 10:00:00'),
(13, 'Andrés Morales',    'Universidad Tecnológica del Bajío', 'amorales@utb.test',      '4771234512', 'Blvd. Torres Landa 800, León, Gto.',             'PROSPECTO', 5,    '2026-09-28 08:30:00'),
(14, 'Elena Rangel',      'Notaría Pública 15',          'erangel@notaria15.test',       '6141234513', 'Av. Independencia 900, Chihuahua, Chih.',        'PROSPECTO', NULL, '2026-10-12 13:00:00'),
(15, 'Tomás Aguilar',     'Taller Mecánico Aguilar',     'taguilar@talleraguilar.test',  '8112345614', 'Calle Zaragoza 77, Centro, Monterrey, NL',       'INACTIVO',  3,    '2025-11-20 15:00:00');

-- ---------------------------------------------------------------------
-- Categorías y productos (productos 3 y 8 están por debajo del stock mínimo)
-- ---------------------------------------------------------------------
INSERT INTO categoria (id, nombre, descripcion) VALUES
(1, 'Laptops',     'Computadoras portátiles para oficina y uso profesional'),
(2, 'Monitores',   'Pantallas para escritorio'),
(3, 'Accesorios',  'Teclados, mouse, diademas y docking stations'),
(4, 'Impresoras',  'Impresoras láser y multifuncionales'),
(5, 'Software',    'Licencias anuales de software');

INSERT INTO producto (id, categoria_id, nombre, descripcion, precio, stock, stock_minimo, activo) VALUES
(1,  1, 'Laptop Lenovo ThinkPad E14',            'Intel Core i5, 16 GB RAM, 512 GB SSD',         18999.00,  12,  3, TRUE),
(2,  1, 'Laptop HP ProBook 450',                 'Intel Core i7, 16 GB RAM, 1 TB SSD',           16499.00,   8,  3, TRUE),
(3,  1, 'MacBook Air 13 M3',                     'Chip M3, 16 GB RAM, 512 GB SSD',               24999.00,   2,  3, TRUE),
(4,  2, 'Monitor Dell 24" P2425',                'Full HD, IPS, USB-C',                           4299.00,  20,  5, TRUE),
(5,  2, 'Monitor LG 27" UltraFine 4K',           '4K UHD, IPS, calibrado de fábrica',             8999.00,   6,  2, TRUE),
(6,  3, 'Teclado Logitech MX Keys',              'Inalámbrico, retroiluminado',                   2199.00,  35, 10, TRUE),
(7,  3, 'Mouse Logitech MX Master 3S',           'Inalámbrico, 8000 DPI',                         1899.00,  40, 10, TRUE),
(8,  3, 'Docking station Dell WD19',             'USB-C, 2 salidas de video, 90 W',               4599.00,   4,  5, TRUE),
(9,  3, 'Diadema Jabra Evolve2 55',              'Inalámbrica, cancelación de ruido',             3499.00,  15,  5, TRUE),
(10, 4, 'Impresora HP LaserJet Pro M404',        'Láser monocromática, red y dúplex',             6799.00,   5,  2, TRUE),
(11, 4, 'Multifuncional Epson EcoTank L3250',    'Tinta continua, Wi-Fi, escáner',                4299.00,   9,  3, TRUE),
(12, 5, 'Microsoft 365 Empresa Estándar',        'Licencia anual por usuario',                    2899.00, 100, 10, TRUE),
(13, 5, 'ESET Endpoint Security',                'Licencia anual por equipo',                      899.00, 200, 20, TRUE),
(14, 3, 'Mouse inalámbrico básico',              'Producto descontinuado',                         249.00,   0,  0, FALSE);

-- ---------------------------------------------------------------------
-- Oportunidades (las GANADA están ligadas a una venta más abajo)
-- ---------------------------------------------------------------------
INSERT INTO oportunidad (id, cliente_id, usuario_id, titulo, monto_estimado, etapa, fecha_cierre_estimada, creado_en) VALUES
(1,  1,  3, 'Renovación de 10 laptops',              190000.00, 'GANADA',      '2026-02-28', '2026-01-15 10:00:00'),
(2,  2,  3, 'Licencias Microsoft 365 para despacho',  35000.00, 'GANADA',      '2026-03-15', '2026-02-20 16:00:00'),
(3,  3,  4, 'Equipamiento de recepción',              30000.00, 'GANADA',      '2026-04-10', '2026-03-25 12:00:00'),
(4,  4,  4, 'Laboratorio de cómputo',                220000.00, 'NEGOCIACION', '2026-11-15', '2026-08-01 10:30:00'),
(5,  5,  5, 'Impresoras para sucursales',             27000.00, 'GANADA',      '2026-05-30', '2026-05-15 13:00:00'),
(6,  7,  3, 'Monitores para oficina de obra',         51588.00, 'PERDIDA',     '2026-06-30', '2026-05-10 10:00:00'),
(7,  8,  4, 'Estaciones de diseño 4K',                86000.00, 'GANADA',      '2026-07-20', '2026-06-25 16:00:00'),
(8,  10, 5, 'Equipo para recepción del hotel',        60000.00, 'PROPUESTA',   '2026-11-30', '2026-09-10 12:30:00'),
(9,  11, 3, 'Punto de venta y laptop',                25000.00, 'PROSPECTO',   '2026-12-15', '2026-08-25 17:30:00'),
(10, 12, 4, 'Laptops para equipo de desarrollo',     125000.00, 'CALIFICADA',  '2026-12-01', '2026-09-05 11:00:00'),
(11, 13, 5, 'Licencias de antivirus para el campus',  89900.00, 'PROSPECTO',   '2027-01-31', '2026-09-30 08:30:00'),
(12, 1,  3, 'Docking stations y monitores',           70000.00, 'PROPUESTA',   '2026-11-20', '2026-09-15 09:00:00'),
(13, 6,  5, 'Laptop para uso personal',               24999.00, 'PERDIDA',     '2026-04-05', '2026-03-01 18:20:00'),
(14, 14, 5, 'Digitalización de archivo notarial',     40000.00, 'PROSPECTO',   '2027-01-15', '2026-10-14 13:15:00');

-- ---------------------------------------------------------------------
-- Interacciones
-- ---------------------------------------------------------------------
INSERT INTO interaccion (cliente_id, usuario_id, tipo, fecha, asunto, descripcion) VALUES
(1,  3, 'LLAMADA', '2026-01-15 10:00:00', 'Primer contacto: renovación de equipo',  'Necesitan reemplazar 10 laptops con más de 5 años de uso.'),
(1,  3, 'REUNION', '2026-02-05 11:30:00', 'Demo de ThinkPad E14',                    'Se presentó el equipo; piden precio por volumen.'),
(1,  3, 'CORREO',  '2026-02-20 09:15:00', 'Envío de cotización',                     'Cotización por 10 unidades con garantía extendida.'),
(2,  3, 'CORREO',  '2026-02-20 16:00:00', 'Licencias para el despacho',              'Solicitan 12 licencias de Microsoft 365.'),
(3,  4, 'REUNION', '2026-03-25 12:00:00', 'Visita a la recepción de la clínica',     'Levantamiento de necesidades: monitores, diademas e impresora.'),
(6,  5, 'CORREO',  '2026-03-02 18:20:00', 'Consulta por MacBook Air',                'Pregunta por meses sin intereses.'),
(6,  5, 'NOTA',    '2026-04-05 10:00:00', 'Oportunidad perdida',                     'Compró en otra tienda por precio.'),
(15, 3, 'LLAMADA', '2026-04-03 12:00:00', 'Seguimiento',                             'Ya no requiere equipo; cerró una sucursal.'),
(7,  3, 'REUNION', '2026-05-12 10:00:00', 'Monitores para la oficina de obra',       'Requieren 12 monitores; comparan con otro proveedor.'),
(5,  5, 'LLAMADA', '2026-05-15 13:00:00', 'Impresoras para sucursales',              'Necesitan 4 impresoras láser para sus sucursales.'),
(9,  3, 'NOTA',    '2026-06-01 09:00:00', 'Cliente sin actividad',                   'Sin compras desde enero; su vendedor ya no labora en la empresa.'),
(8,  4, 'REUNION', '2026-06-25 16:00:00', 'Estaciones de diseño',                    'El equipo creativo necesita pantallas 4K y laptops potentes.'),
(7,  3, 'NOTA',    '2026-06-30 15:00:00', 'Se pierde la oportunidad',                'Eligieron un proveedor local con entrega inmediata.'),
(4,  4, 'LLAMADA', '2026-08-01 10:30:00', 'Proyecto de laboratorio',                 'Quieren equipar un laboratorio de 25 equipos para enero.'),
(11, 3, 'LLAMADA', '2026-08-25 17:30:00', 'Lead desde redes sociales',               'Interesado en un punto de venta para su restaurante.'),
(12, 4, 'REUNION', '2026-09-05 11:00:00', 'Reunión inicial con Nube Digital',        'Equipo de 6 desarrolladores; prefieren 32 GB de RAM.'),
(10, 5, 'LLAMADA', '2026-09-10 12:30:00', 'Renovación de la recepción',              'Piden propuesta de equipos para recepción y lobby.'),
(4,  4, 'REUNION', '2026-09-18 09:00:00', 'Recorrido por el aula',                   'Se validaron instalaciones eléctricas y red; piden opción de arrendamiento.'),
(10, 5, 'CORREO',  '2026-09-24 09:45:00', 'Propuesta enviada',                       'Se envió propuesta por $60,000.'),
(13, 5, 'CORREO',  '2026-09-30 08:30:00', 'Licencias de antivirus',                  'Solicitan cotización para 100 equipos del campus.'),
(4,  4, 'NOTA',    '2026-10-08 17:00:00', 'Negociación',                             'La dirección pide 5% adicional de descuento; consultar con gerencia.'),
(2,  3, 'LLAMADA', '2026-10-14 10:20:00', 'Problema con activación de licencias',    'Se abrió un ticket de soporte.'),
(14, 5, 'LLAMADA', '2026-10-14 13:15:00', 'Contacto inicial',                        'Les interesa digitalizar su archivo; agendar visita.');

-- ---------------------------------------------------------------------
-- Campañas y destinatarios
-- ---------------------------------------------------------------------
INSERT INTO campana (id, nombre, canal, asunto, mensaje, segmento, fecha_inicio, fecha_fin, presupuesto, estado) VALUES
(1, 'Bienvenida a nuevos clientes', 'CORREO',
    'Bienvenido: conoce nuestras soluciones para tu oficina',
    'Gracias por tu interés. Te compartimos nuestro catálogo y un asesor te contactará en breve.',
    'PROSPECTOS', '2026-01-15', '2026-12-31', 0.00, 'ACTIVA'),
(2, 'Regreso a clases 2026', 'CORREO',
    '15% de descuento en accesorios',
    'Durante agosto, todos los teclados, mouse, diademas y docking stations con 15% de descuento.',
    'TODOS', '2026-07-25', '2026-08-31', 5000.00, 'FINALIZADA'),
(3, 'Buen Fin 2026', 'CORREO',
    '10% en laptops y monitores este Buen Fin',
    'Del 13 al 16 de noviembre, 10% de descuento en laptops y monitores seleccionados.',
    'ACTIVOS', '2026-11-10', '2026-11-16', 12000.00, 'PLANEADA'),
(4, 'Reactivación de clientes', 'SMS',
    'Te extrañamos',
    'Hace tiempo que no nos visitas. Responde este mensaje y recibe una cotización con precio preferente.',
    'SIN_COMPRA_90_DIAS', '2026-10-01', '2026-10-31', 1500.00, 'ACTIVA');

INSERT INTO campana_cliente (campana_id, cliente_id, resultado) VALUES
(1, 11, 'ABIERTO'), (1, 12, 'RESPONDIO'), (1, 13, 'ABIERTO'), (1, 14, 'ENVIADO'),
(2, 1, 'ABIERTO'),  (2, 2, 'RESPONDIO'),  (2, 3, 'ENVIADO'),  (2, 4, 'RESPONDIO'),
(2, 5, 'ENVIADO'),  (2, 6, 'RESPONDIO'),  (2, 7, 'ABIERTO'),  (2, 8, 'ENVIADO'), (2, 10, 'ABIERTO'),
(4, 9, 'ENVIADO'),  (4, 15, 'ABIERTO');

-- ---------------------------------------------------------------------
-- Promociones
-- 1: terminada (agosto), 2: futura (Buen Fin), 3: vigente (licencias)
-- ---------------------------------------------------------------------
INSERT INTO promocion (id, campana_id, nombre, tipo, valor, fecha_inicio, fecha_fin, activa) VALUES
(1, 2,    'Regreso a clases: 15% en accesorios', 'PORCENTAJE', 15.00,  '2026-08-01', '2026-08-31', FALSE),
(2, 3,    'Buen Fin: 10% en laptops y monitores', 'PORCENTAJE', 10.00, '2026-11-13', '2026-11-16', TRUE),
(3, NULL, '$300 menos por licencia de Microsoft 365', 'MONTO_FIJO', 300.00, '2026-10-01', '2026-12-31', TRUE);

INSERT INTO promocion_producto (promocion_id, producto_id) VALUES
(1, 6), (1, 7), (1, 8), (1, 9),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
(3, 12);

-- ---------------------------------------------------------------------
-- Ventas (enero a octubre de 2026). total se calcula al final.
-- ---------------------------------------------------------------------
INSERT INTO venta (id, cliente_id, usuario_id, oportunidad_id, fecha, estado) VALUES
(1,  2,  3, NULL, '2026-01-12 11:00:00', 'PAGADA'),
(2,  9,  6, NULL, '2026-01-25 10:30:00', 'PAGADA'),
(3,  3,  4, NULL, '2026-02-07 13:15:00', 'PAGADA'),
(4,  1,  3, 1,    '2026-02-28 12:00:00', 'PAGADA'),
(5,  5,  5, NULL, '2026-03-10 09:45:00', 'PAGADA'),
(6,  2,  3, 2,    '2026-03-15 16:30:00', 'PAGADA'),
(7,  6,  5, NULL, '2026-03-29 18:00:00', 'PAGADA'),
(8,  3,  4, 3,    '2026-04-10 11:20:00', 'PAGADA'),
(9,  7,  3, NULL, '2026-04-22 10:00:00', 'PAGADA'),
(10, 4,  4, NULL, '2026-05-08 12:40:00', 'PAGADA'),
(11, 5,  5, 5,    '2026-05-30 14:00:00', 'PAGADA'),
(12, 8,  4, NULL, '2026-06-05 17:10:00', 'PAGADA'),
(13, 1,  3, NULL, '2026-06-18 09:30:00', 'PAGADA'),
(14, 10, 5, NULL, '2026-06-27 15:00:00', 'CANCELADA'),
(15, 10, 5, NULL, '2026-07-03 11:45:00', 'PAGADA'),
(16, 8,  4, 7,    '2026-07-20 13:00:00', 'PAGADA'),
(17, 7,  3, NULL, '2026-07-28 10:15:00', 'PAGADA'),
(18, 4,  4, NULL, '2026-08-06 12:00:00', 'PAGADA'),
(19, 6,  5, NULL, '2026-08-14 19:30:00', 'PAGADA'),
(20, 2,  3, NULL, '2026-08-21 16:00:00', 'PAGADA'),
(21, 1,  3, NULL, '2026-09-02 10:00:00', 'PAGADA'),
(22, 3,  4, NULL, '2026-09-12 12:30:00', 'PAGADA'),
(23, 5,  5, NULL, '2026-09-25 09:00:00', 'PAGADA'),
(24, 10, 5, NULL, '2026-10-03 13:20:00', 'PAGADA'),
(25, 8,  4, NULL, '2026-10-10 17:45:00', 'PENDIENTE'),
(26, 7,  3, NULL, '2026-10-16 11:00:00', 'PENDIENTE');

-- Detalle. descuento y subtotal se calculan con los UPDATE de abajo.
INSERT INTO detalle_venta (venta_id, producto_id, promocion_id, cantidad, precio_unitario) VALUES
(1,  4,  NULL, 2,  4299.00), (1,  7,  NULL, 2,  1899.00),
(2,  13, NULL, 10,  899.00),
(3,  1,  NULL, 1, 18999.00), (3,  6,  NULL, 1,  2199.00),
(4,  1,  NULL, 10, 18999.00),
(5,  11, NULL, 2,  4299.00),
(6,  12, NULL, 12, 2899.00),
(7,  7,  NULL, 1,  1899.00), (7,  6,  NULL, 1,  2199.00),
(8,  4,  NULL, 3,  4299.00), (8,  9,  NULL, 3,  3499.00), (8, 10, NULL, 1, 6799.00),
(9,  2,  NULL, 2, 16499.00),
(10, 13, NULL, 30,  899.00),
(11, 10, NULL, 4,  6799.00),
(12, 6,  NULL, 4,  2199.00), (12, 7,  NULL, 4,  1899.00),
(13, 9,  NULL, 5,  3499.00),
(14, 11, NULL, 3,  4299.00),
(15, 11, NULL, 2,  4299.00), (15, 4,  NULL, 2,  4299.00),
(16, 3,  NULL, 2, 24999.00), (16, 5,  NULL, 4,  8999.00),
(17, 12, NULL, 8,  2899.00),
(18, 6,  1,    10, 2199.00), (18, 7,  1,    10, 1899.00),
(19, 9,  1,    1,  3499.00),
(20, 8,  1,    2,  4599.00), (20, 7,  1,    2,  1899.00),
(21, 4,  NULL, 6,  4299.00),
(22, 12, NULL, 15, 2899.00),
(23, 2,  NULL, 3, 16499.00),
(24, 13, NULL, 20,  899.00), (24, 12, 3,    5,  2899.00),
(25, 5,  NULL, 1,  8999.00),
(26, 1,  NULL, 2, 18999.00), (26, 8,  NULL, 2,  4599.00);

-- Descuento según el tipo de promoción (la API hará este cálculo al vender).
UPDATE detalle_venta d
JOIN promocion p ON p.id = d.promocion_id
SET d.descuento = CASE p.tipo
    WHEN 'PORCENTAJE' THEN ROUND(d.cantidad * d.precio_unitario * p.valor / 100, 2)
    WHEN 'MONTO_FIJO' THEN d.cantidad * p.valor
END;

UPDATE detalle_venta
SET subtotal = cantidad * precio_unitario - descuento;

UPDATE venta v
SET v.total = (SELECT COALESCE(SUM(d.subtotal), 0) FROM detalle_venta d WHERE d.venta_id = v.id);

-- ---------------------------------------------------------------------
-- Contratos
-- ---------------------------------------------------------------------
INSERT INTO contrato (cliente_id, venta_id, titulo, fecha_inicio, fecha_fin, monto, estado) VALUES
(1, 4,    'Soporte y garantía extendida de 10 laptops',  '2026-03-01', '2028-02-29',  30000.00, 'VIGENTE'),
(2, 6,    'Licenciamiento anual de Microsoft 365',       '2026-03-15', '2027-03-14',  34788.00, 'VIGENTE'),
(9, NULL, 'Mantenimiento preventivo de equipos 2025',    '2025-01-01', '2025-12-31',  18000.00, 'VENCIDO'),
(4, NULL, 'Arrendamiento de laboratorio de cómputo',     '2026-12-01', '2029-11-30', 220000.00, 'BORRADOR');

-- ---------------------------------------------------------------------
-- Tickets de soporte
-- ---------------------------------------------------------------------
INSERT INTO ticket_soporte (cliente_id, usuario_id, asunto, descripcion, prioridad, estado, creado_en, cerrado_en) VALUES
(1,  3,    'Laptop no enciende',                 'Una de las ThinkPad entregadas no enciende; se cambió por garantía.', 'ALTA',  'CERRADO',    '2026-03-20 09:00:00', '2026-03-22 17:00:00'),
(3,  4,    'Impresora atasca papel',             'La LaserJet de recepción atasca hojas en dúplex.',                    'MEDIA', 'CERRADO',    '2026-05-02 10:30:00', '2026-05-06 12:00:00'),
(5,  5,    'Configuración de impresoras en red', 'Apoyo para agregar las impresoras a la red de cada sucursal.',        'BAJA',  'CERRADO',    '2026-06-03 11:00:00', '2026-06-04 13:30:00'),
(2,  3,    'Activación de licencias pendiente',  'Tres usuarios no pueden activar Microsoft 365.',                      'ALTA',  'EN_PROCESO', '2026-10-14 10:30:00', NULL),
(8,  4,    'Monitor con píxel muerto',           'Un monitor 4K presenta un píxel muerto en la esquina superior.',      'MEDIA', 'ABIERTO',    '2026-10-15 16:00:00', NULL),
(10, NULL, 'Solicitud de factura',               'Piden reenvío de la factura de la compra de octubre.',               'BAJA',  'ABIERTO',    '2026-10-17 09:15:00', NULL);

-- ---------------------------------------------------------------------
-- Puntos de fidelización: 1 punto por cada $100 de cada venta PAGADA
-- ---------------------------------------------------------------------
INSERT INTO movimiento_puntos (cliente_id, venta_id, tipo, puntos, motivo, fecha)
SELECT cliente_id, id, 'GANADO', FLOOR(total / 100), CONCAT('Compra #', id), fecha
FROM venta
WHERE estado = 'PAGADA' AND total >= 100;

INSERT INTO movimiento_puntos (cliente_id, venta_id, tipo, puntos, motivo, fecha) VALUES
(2, NULL, 'CANJEADO',  500, 'Canje por descuento en siguiente compra', '2026-09-01 12:00:00'),
(1, NULL, 'CANJEADO', 1000, 'Canje por docking station de regalo',     '2026-09-20 10:00:00');

-- ---------------------------------------------------------------------
-- Metas mensuales de los vendedores activos (julio a octubre de 2026)
-- ---------------------------------------------------------------------
INSERT INTO meta_venta (usuario_id, anio, mes, monto_objetivo) VALUES
(3, 2026, 7, 60000.00), (3, 2026, 8, 60000.00), (3, 2026, 9, 65000.00), (3, 2026, 10, 65000.00),
(4, 2026, 7, 50000.00), (4, 2026, 8, 50000.00), (4, 2026, 9, 55000.00), (4, 2026, 10, 55000.00),
(5, 2026, 7, 40000.00), (5, 2026, 8, 40000.00), (5, 2026, 9, 45000.00), (5, 2026, 10, 45000.00);

SET SQL_SAFE_UPDATES = 1;
