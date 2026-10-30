-- =====================================================================
-- CRM de Ventas · Esquema de la base de datos (migración Flyway V1)
-- Motor: MySQL 8+
--
-- Flyway ejecuta este script sobre la base de spring.datasource.url,
-- que debe existir de antemano:
--   CREATE DATABASE crm_ventas CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
--
-- ⚠ No editar una vez aplicado: los cambios van en una nueva versión (V3__...).
-- =====================================================================

-- =====================================================================
-- FASE 1 · NÚCLEO
-- =====================================================================

-- Personas que usan el sistema. El rol define permisos (Spring Security).
CREATE TABLE usuario (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol           ENUM('VENDEDOR', 'GERENTE', 'ADMIN') NOT NULL DEFAULT 'VENDEDOR',
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

-- Personas o empresas. Un lead es un cliente con estado PROSPECTO.
CREATE TABLE cliente (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    empresa        VARCHAR(100) NULL,
    email          VARCHAR(150) NOT NULL,
    telefono       VARCHAR(20)  NULL,
    direccion      VARCHAR(255) NULL,
    estado         ENUM('PROSPECTO', 'ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'PROSPECTO',
    vendedor_id    INT          NULL,  -- NULL = sin vendedor asignado
    creado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_cliente_email UNIQUE (email),
    CONSTRAINT fk_cliente_vendedor FOREIGN KEY (vendedor_id) REFERENCES usuario (id)
);

-- Historial de contacto con cada cliente.
CREATE TABLE interaccion (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id  INT          NOT NULL,
    usuario_id  INT          NOT NULL,
    tipo        ENUM('LLAMADA', 'CORREO', 'REUNION', 'NOTA') NOT NULL,
    fecha       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asunto      VARCHAR(150) NOT NULL,
    descripcion TEXT         NULL,
    CONSTRAINT fk_interaccion_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_interaccion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- Posibles negocios que avanzan por etapas.
CREATE TABLE oportunidad (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id            INT           NOT NULL,
    usuario_id            INT           NOT NULL,
    titulo                VARCHAR(150)  NOT NULL,
    monto_estimado        DECIMAL(12,2) NOT NULL DEFAULT 0,
    etapa                 ENUM('PROSPECTO', 'CALIFICADA', 'PROPUESTA', 'NEGOCIACION', 'GANADA', 'PERDIDA')
                          NOT NULL DEFAULT 'PROSPECTO',
    fecha_cierre_estimada DATE          NULL,
    creado_en             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_oportunidad_monto CHECK (monto_estimado >= 0),
    CONSTRAINT fk_oportunidad_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_oportunidad_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE TABLE categoria (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion VARCHAR(255) NULL,
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
);

-- Catálogo. Los productos se desactivan (activo = FALSE) en lugar de borrarse.
CREATE TABLE producto (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    categoria_id INT           NOT NULL,
    nombre       VARCHAR(120)  NOT NULL,
    descripcion  TEXT          NULL,
    precio       DECIMAL(10,2) NOT NULL,
    stock        INT           NOT NULL DEFAULT 0,
    stock_minimo INT           NOT NULL DEFAULT 0,  -- stock <= stock_minimo → alerta de resurtir
    activo       BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0),
    CONSTRAINT chk_producto_stock_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id)
);

-- Ventas cerradas. oportunidad_id es opcional: hay ventas directas.
-- total = suma de los subtotales de detalle_venta (lo calcula la API).
CREATE TABLE venta (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id     INT           NOT NULL,
    usuario_id     INT           NOT NULL,
    oportunidad_id INT           NULL,
    fecha          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total          DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado         ENUM('PENDIENTE', 'PAGADA', 'CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT uk_venta_oportunidad UNIQUE (oportunidad_id),  -- una oportunidad genera como máximo una venta
    CONSTRAINT chk_venta_total CHECK (total >= 0),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_venta_oportunidad FOREIGN KEY (oportunidad_id) REFERENCES oportunidad (id)
);

-- =====================================================================
-- FASE 2 · MÓDULOS EXTRA
-- (campana y promocion van antes de detalle_venta porque esta las referencia)
-- =====================================================================

-- Automatización del marketing: el segmento decide a qué clientes se envía.
CREATE TABLE campana (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(120)  NOT NULL,
    canal        ENUM('CORREO', 'SMS', 'REDES') NOT NULL,
    asunto       VARCHAR(150)  NOT NULL,
    mensaje      TEXT          NOT NULL,
    segmento     ENUM('TODOS', 'PROSPECTOS', 'ACTIVOS', 'INACTIVOS', 'SIN_COMPRA_90_DIAS')
                 NOT NULL DEFAULT 'TODOS',
    fecha_inicio DATE          NOT NULL,
    fecha_fin    DATE          NOT NULL,
    presupuesto  DECIMAL(10,2) NOT NULL DEFAULT 0,
    estado       ENUM('PLANEADA', 'ACTIVA', 'FINALIZADA') NOT NULL DEFAULT 'PLANEADA',
    CONSTRAINT chk_campana_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_campana_presupuesto CHECK (presupuesto >= 0)
);

-- Destinatarios de cada campaña (muchos a muchos).
CREATE TABLE campana_cliente (
    campana_id INT NOT NULL,
    cliente_id INT NOT NULL,
    resultado  ENUM('ENVIADO', 'ABIERTO', 'RESPONDIO') NOT NULL DEFAULT 'ENVIADO',
    PRIMARY KEY (campana_id, cliente_id),
    CONSTRAINT fk_campcli_campana FOREIGN KEY (campana_id) REFERENCES campana (id) ON DELETE CASCADE,
    CONSTRAINT fk_campcli_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id) ON DELETE CASCADE
);

-- Ofertas con vigencia.
-- PORCENTAJE: valor = % de descuento (1 a 100).
-- MONTO_FIJO: valor = pesos de descuento por unidad.
-- Regla: no se acumulan; si hay varias vigentes se aplica la de mayor descuento.
CREATE TABLE promocion (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    campana_id   INT           NULL,  -- campaña que la anuncia (opcional)
    nombre       VARCHAR(120)  NOT NULL,
    tipo         ENUM('PORCENTAJE', 'MONTO_FIJO') NOT NULL,
    valor        DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE          NOT NULL,
    fecha_fin    DATE          NOT NULL,
    activa       BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_promocion_valor CHECK (valor > 0 AND (tipo <> 'PORCENTAJE' OR valor <= 100)),
    CONSTRAINT chk_promocion_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT fk_promocion_campana FOREIGN KEY (campana_id) REFERENCES campana (id)
);

-- Productos a los que aplica cada promoción (muchos a muchos).
CREATE TABLE promocion_producto (
    promocion_id INT NOT NULL,
    producto_id  INT NOT NULL,
    PRIMARY KEY (promocion_id, producto_id),
    CONSTRAINT fk_promprod_promocion FOREIGN KEY (promocion_id) REFERENCES promocion (id) ON DELETE CASCADE,
    CONSTRAINT fk_promprod_producto FOREIGN KEY (producto_id) REFERENCES producto (id) ON DELETE CASCADE
);

-- =====================================================================
-- FASE 1 · NÚCLEO (continuación)
-- =====================================================================

-- Renglones de cada venta. precio_unitario y descuento se copian al vender,
-- así la venta no cambia si después cambia el precio o termina la promoción.
-- subtotal = cantidad * precio_unitario - descuento
CREATE TABLE detalle_venta (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    venta_id        INT           NOT NULL,
    producto_id     INT           NOT NULL,
    promocion_id    INT           NULL,  -- fase 2
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    descuento       DECIMAL(10,2) NOT NULL DEFAULT 0,  -- fase 2
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT uk_detalle_venta_producto UNIQUE (venta_id, producto_id),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_descuento CHECK (descuento >= 0),
    CONSTRAINT fk_detalle_venta FOREIGN KEY (venta_id) REFERENCES venta (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_detalle_promocion FOREIGN KEY (promocion_id) REFERENCES promocion (id)
);

-- =====================================================================
-- FASE 2 · MÓDULOS EXTRA (continuación)
-- =====================================================================

-- Acuerdos con condiciones y vigencia (no son facturas).
CREATE TABLE contrato (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id   INT           NOT NULL,
    venta_id     INT           NULL,  -- venta que lo originó (opcional)
    titulo       VARCHAR(150)  NOT NULL,
    fecha_inicio DATE          NOT NULL,
    fecha_fin    DATE          NOT NULL,
    monto        DECIMAL(12,2) NOT NULL,
    estado       ENUM('BORRADOR', 'VIGENTE', 'VENCIDO', 'CANCELADO') NOT NULL DEFAULT 'BORRADOR',
    CONSTRAINT chk_contrato_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_contrato_monto CHECK (monto >= 0),
    CONSTRAINT fk_contrato_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_contrato_venta FOREIGN KEY (venta_id) REFERENCES venta (id)
);

-- Integración con soporte al cliente.
CREATE TABLE ticket_soporte (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id  INT          NOT NULL,
    usuario_id  INT          NULL,  -- NULL = sin asignar
    asunto      VARCHAR(150) NOT NULL,
    descripcion TEXT         NULL,
    prioridad   ENUM('BAJA', 'MEDIA', 'ALTA') NOT NULL DEFAULT 'MEDIA',
    estado      ENUM('ABIERTO', 'EN_PROCESO', 'CERRADO') NOT NULL DEFAULT 'ABIERTO',
    creado_en   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cerrado_en  DATETIME     NULL,
    CONSTRAINT fk_ticket_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_ticket_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- Programa de fidelización, como un estado de cuenta.
-- Regla: 1 punto por cada $100 de una venta PAGADA.
-- Saldo = SUM(GANADO) - SUM(CANJEADO). Los puntos siempre se guardan positivos.
CREATE TABLE movimiento_puntos (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT          NOT NULL,
    venta_id   INT          NULL,  -- venta que generó los puntos (solo en GANADO)
    tipo       ENUM('GANADO', 'CANJEADO') NOT NULL,
    puntos     INT          NOT NULL,
    motivo     VARCHAR(150) NOT NULL,
    fecha      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_movimiento_puntos CHECK (puntos > 0),
    CONSTRAINT fk_movimiento_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_movimiento_venta FOREIGN KEY (venta_id) REFERENCES venta (id)
);

-- Meta mensual de cada vendedor (informes de rendimiento).
CREATE TABLE meta_venta (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id     INT           NOT NULL,
    anio           INT           NOT NULL,
    mes            INT           NOT NULL,
    monto_objetivo DECIMAL(12,2) NOT NULL,
    CONSTRAINT uk_meta_usuario_periodo UNIQUE (usuario_id, anio, mes),
    CONSTRAINT chk_meta_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT chk_meta_monto CHECK (monto_objetivo > 0),
    CONSTRAINT fk_meta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
