CREATE DATABASE IF NOT EXISTS AlquiNow;
USE AlquiNow;

-- ===========================================================
-- 1) BORRADO DE TABLAS EXISTENTES
--    Desactivamos la verificacion de FK para borrar sin
--    preocuparnos por el orden, y la reactivamos al final.
-- ===========================================================
/*
 SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Resena;
DROP TABLE IF EXISTS Historial_Busqueda;
DROP TABLE IF EXISTS Lista_Deseos;
DROP TABLE IF EXISTS Sena;
DROP TABLE IF EXISTS Reserva;
DROP TABLE IF EXISTS Disponibilidad;
DROP TABLE IF EXISTS Imagen_Propiedad;
DROP TABLE IF EXISTS Propiedad;
DROP TABLE IF EXISTS Vendedor;
DROP TABLE IF EXISTS Comprador;
DROP TABLE IF EXISTS Usuario;

SET FOREIGN_KEY_CHECKS = 1;
*/
-- ===========================================================
-- 2) CREACION DE TABLAS (base limpia)
-- ===========================================================

-- =======================
-- TABLA USUARIO
-- =======================

CREATE TABLE IF NOT EXISTS Usuario (
  ID_usuario INT AUTO_INCREMENT,
  contrasena VARCHAR(100) NOT NULL,
  dni VARCHAR(20),
  mail VARCHAR(100) UNIQUE,
  tel VARCHAR(20),
  PRIMARY KEY (ID_usuario)
);

-- =======================
-- TABLA COMPRADOR
-- =======================

CREATE TABLE IF NOT EXISTS Comprador (
  ID_usuario INT,
  preferencias_busqueda TEXT,
  metodo_pago_preferido VARCHAR(50),

  PRIMARY KEY (ID_usuario),

  FOREIGN KEY (ID_usuario)
    REFERENCES Usuario(ID_usuario)
);

-- =======================
-- TABLA VENDEDOR
-- =======================

CREATE TABLE IF NOT EXISTS Vendedor (
  ID_usuario INT,
  cbu_cvu VARCHAR(50),
  tipo_vendedor VARCHAR(50),
  verificado BOOLEAN DEFAULT FALSE,

  PRIMARY KEY (ID_usuario),

  FOREIGN KEY (ID_usuario)
    REFERENCES Usuario(ID_usuario)
);

-- =======================
-- TABLA PROPIEDAD
-- =======================

CREATE TABLE IF NOT EXISTS Propiedad (
  ID_propiedad INT AUTO_INCREMENT,
  ID_vendedor_fk INT,

  estadia_minima INT,

  calle VARCHAR(100),
  altura INT,
  codigo_postal VARCHAR(10),
  ciudad VARCHAR(50),

  -- provincia definida UNA sola vez, como ENUM
  provincia ENUM(
    'Buenos Aires',
    'Catamarca',
    'Chaco',
    'Chubut',
    'Córdoba',
    'Corrientes',
    'Entre Ríos',
    'Formosa',
    'Jujuy',
    'La Pampa',
    'La Rioja',
    'Mendoza',
    'Misiones',
    'Neuquén',
    'Río Negro',
    'Salta',
    'San Juan',
    'San Luis',
    'Santa Cruz',
    'Santa Fe',
    'Santiago del Estero',
    'Tierra del Fuego',
    'Tucumán',
    'CABA'
  ) NOT NULL,

  pais VARCHAR(30) DEFAULT 'Argentina',

  -- GOOGLE MAPS (a futuro)
  -- latitud DECIMAL(10,8),
  -- longitud DECIMAL(11,8),

  precio_por_noche DECIMAL(10,2),
  metros_cuadrados INT,
  cant_personas INT,
  piso INT,

  descripcion TEXT,

  disponibilidad_inmediata BOOLEAN DEFAULT FALSE,

  dias_cancelacion_sin_penalizacion INT,

  PRIMARY KEY (ID_propiedad),

  FOREIGN KEY (ID_vendedor_fk)
    REFERENCES Vendedor(ID_usuario)
);

-- =======================
-- TABLA IMAGEN PROPIEDAD
-- =======================

CREATE TABLE IF NOT EXISTS Imagen_Propiedad (
  ID_imagen INT AUTO_INCREMENT,
  ID_propiedad_fk INT NOT NULL,
  url_imagen VARCHAR(255) NOT NULL,

  PRIMARY KEY (ID_imagen),

  FOREIGN KEY (ID_propiedad_fk)
    REFERENCES Propiedad(ID_propiedad)
    ON DELETE CASCADE
);

-- =======================
-- TABLA DISPONIBILIDAD
-- =======================

CREATE TABLE IF NOT EXISTS Disponibilidad (
  ID_disponibilidad INT AUTO_INCREMENT,
  ID_propiedad_fk INT,
  fecha DATE,
  estado VARCHAR(20),

  PRIMARY KEY (ID_disponibilidad),

  FOREIGN KEY (ID_propiedad_fk)
    REFERENCES Propiedad(ID_propiedad)
);

-- =======================
-- TABLA RESERVA
-- =======================

CREATE TABLE IF NOT EXISTS Reserva (
  ID_reserva INT AUTO_INCREMENT,
  ID_comprador_fk INT,
  ID_propiedad_fk INT,

  fecha_inicio DATE,
  fecha_final DATE,

  estado VARCHAR(20),

  monto_total DECIMAL(10,2),

  fecha_reserva DATE,

  dias_cancelacion_aplicados INT,
  fecha_limite_cancelacion DATE,

  PRIMARY KEY (ID_reserva),

  FOREIGN KEY (ID_comprador_fk)
    REFERENCES Comprador(ID_usuario),

  FOREIGN KEY (ID_propiedad_fk)
    REFERENCES Propiedad(ID_propiedad)
);

-- =======================
-- TABLA SEÑA
-- =======================

CREATE TABLE IF NOT EXISTS Sena (
  ID_pago INT AUTO_INCREMENT,
  ID_reserva_fk INT,

  monto DECIMAL(10,2),
  fecha_pago DATE,
  metodo_pago VARCHAR(50),
  estado VARCHAR(20),

  PRIMARY KEY (ID_pago),

  FOREIGN KEY (ID_reserva_fk)
    REFERENCES Reserva(ID_reserva)
);

-- =======================
-- TABLA LISTA DE DESEOS
-- =======================

CREATE TABLE IF NOT EXISTS Lista_Deseos (
  ID_lista INT AUTO_INCREMENT,
  ID_comprador_fk INT,
  ID_propiedad_fk INT,
  fecha_agregado DATE,

  PRIMARY KEY (ID_lista),

  FOREIGN KEY (ID_comprador_fk)
    REFERENCES Comprador(ID_usuario),

  FOREIGN KEY (ID_propiedad_fk)
    REFERENCES Propiedad(ID_propiedad)
);

-- =======================
-- TABLA HISTORIAL BUSQUEDA
-- =======================

CREATE TABLE IF NOT EXISTS Historial_Busqueda (
  ID_historial INT AUTO_INCREMENT,
  ID_comprador_fk INT,
  filtros_aplicados TEXT,
  fecha_busqueda DATE,

  PRIMARY KEY (ID_historial),

  FOREIGN KEY (ID_comprador_fk)
    REFERENCES Comprador(ID_usuario)
);

-- =======================
-- TABLA RESEÑAS
-- =======================

CREATE TABLE IF NOT EXISTS Resena (
  ID_resena INT AUTO_INCREMENT,

  ID_comprador_fk INT,
  ID_propiedad_fk INT,

  puntuacion INT CHECK (puntuacion BETWEEN 1 AND 5),
  comentario TEXT,
  fecha DATE,

  PRIMARY KEY (ID_resena),

  FOREIGN KEY (ID_comprador_fk)
    REFERENCES Comprador(ID_usuario),

  FOREIGN KEY (ID_propiedad_fk)
    REFERENCES Propiedad(ID_propiedad)
);

-- ===========================================================
-- 3) INDICES
-- ===========================================================

CREATE INDEX idx_propiedad_precio
ON Propiedad(precio_por_noche);

CREATE INDEX idx_propiedad_capacidad
ON Propiedad(cant_personas);

CREATE INDEX idx_propiedad_ubicacion
ON Propiedad(ciudad, provincia);

CREATE INDEX idx_disponibilidad_fecha
ON Disponibilidad(fecha);

CREATE INDEX idx_disponibilidad_propiedad
ON Disponibilidad(ID_propiedad_fk);

CREATE INDEX idx_reserva_propiedad
ON Reserva(ID_propiedad_fk);

CREATE INDEX idx_reserva_fechas
ON Reserva(fecha_inicio, fecha_final);

CREATE INDEX idx_imagen_propiedad
ON Imagen_Propiedad(ID_propiedad_fk);

CREATE INDEX idx_resena_propiedad
ON Resena(ID_propiedad_fk);

-- ===========================================================
-- FIN DEL SCRIPT - base recreada y limpia
-- ===========================================================