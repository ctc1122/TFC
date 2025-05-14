-- Script para recrear e insertar solo los registros en español en la tabla MRCONSO_SPA

-- Eliminamos la tabla si existe
DROP TABLE IF EXISTS MRCONSO_SPA;

-- Creamos la tabla MRCONSO_SPA con la misma estructura que MRCONSO pero SIN clave primaria en AUI
CREATE TABLE MRCONSO_SPA (
    CUI VARCHAR(8) NOT NULL,
    LAT CHAR(3) NOT NULL,
    TS CHAR(1) NOT NULL,
    LUI VARCHAR(10) NOT NULL,
    STT VARCHAR(3) NOT NULL,
    SUI VARCHAR(10) NOT NULL,
    ISPREF CHAR(1) NOT NULL,
    AUI VARCHAR(9) NOT NULL,
    SAUI VARCHAR(255),
    SCUI VARCHAR(255),
    SDUI VARCHAR(255),
    SAB VARCHAR(20) NOT NULL,
    TTY VARCHAR(20) NOT NULL,
    CODE VARCHAR(100) NOT NULL,
    STR TEXT NOT NULL,
    SRL VARCHAR(10) NOT NULL,
    SUPPRESS CHAR(1) NOT NULL,
    CVF VARCHAR(50),
    
    -- Índices para mejorar rendimiento pero sin clave primaria
    INDEX idx_aui (AUI),
    INDEX idx_cui (CUI),
    INDEX idx_str (STR(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertamos solo los registros de español (LAT='SPA') de la tabla MRCONSO a MRCONSO_SPA
-- Sin usar DISTINCT para permitir duplicados
INSERT INTO MRCONSO_SPA
SELECT * FROM MRCONSO
WHERE LAT = 'SPA';

-- Verificamos cuántos registros se han insertado
SELECT COUNT(*) AS 'Registros en español insertados' FROM MRCONSO_SPA; 