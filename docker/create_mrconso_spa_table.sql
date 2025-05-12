-- Script para crear la tabla MRCONSO_SPA con solo los registros en español
-- Esta tabla tendrá solamente los registros de MRCONSO2 donde LAT='SPA'

-- Eliminamos la tabla MRCONSO_SPA si ya existe
DROP TABLE IF EXISTS MRCONSO_SPA;

-- Creamos la tabla MRCONSO_SPA con la misma estructura que MRCONSO2
CREATE TABLE MRCONSO_SPA (
    CUI VARCHAR(8) NOT NULL,
    LAT CHAR(3) NOT NULL,
    TS CHAR(1) NOT NULL,
    LUI VARCHAR(10) NOT NULL,
    STT VARCHAR(3) NOT NULL,
    SUI VARCHAR(10) NOT NULL,
    ISPREF CHAR(1) NOT NULL,
    AUI VARCHAR(9) NOT NULL,
    SAUI VARCHAR(50),
    SCUI VARCHAR(50),
    SDUI VARCHAR(50),
    SAB VARCHAR(20) NOT NULL,
    TTY VARCHAR(20) NOT NULL,
    CODE VARCHAR(100) NOT NULL,
    STR TEXT NOT NULL,
    SRL VARCHAR(10) NOT NULL,
    SUPPRESS CHAR(1) NOT NULL,
    CVF VARCHAR(50),
    
    -- Índices para mejorar rendimiento
    PRIMARY KEY (AUI),
    INDEX idx_cui (CUI),
    INDEX idx_str (STR(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertamos solo los registros de español (LAT='SPA') de la tabla MRCONSO2 a MRCONSO_SPA
INSERT INTO MRCONSO_SPA
SELECT * FROM MRCONSO2
WHERE LAT = 'SPA';

-- Si deseas verificar cuántos registros se han insertado, puedes ejecutar:
SELECT COUNT(*) FROM MRCONSO_SPA;

-- Si deseas verificar algunos registros de ejemplo:
-- SELECT * FROM MRCONSO_SPA LIMIT 10; 