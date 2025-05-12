-- Script para crear la tabla Diagnosticos filtrando registros específicos de MRCONSO_SPA
-- Filtra registros donde:
-- 1. SAB = 'SCTSPA' y STR contiene 'trastorno', o
-- 2. TTY = 'LLT'

-- Eliminamos la tabla si existe
DROP TABLE IF EXISTS Diagnosticos;

-- Creamos la tabla Diagnosticos con la misma estructura que MRCONSO_SPA
CREATE TABLE Diagnosticos (
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
    INDEX idx_aui (AUI),
    INDEX idx_cui (CUI),
    INDEX idx_str (STR(255)),
    INDEX idx_sab (SAB),
    INDEX idx_tty (TTY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertamos los registros filtrados de MRCONSO_SPA a Diagnosticos
INSERT INTO Diagnosticos
SELECT * FROM MRCONSO_SPA
WHERE (SAB = 'SCTSPA' AND STR LIKE '%trastorno%')
   OR (TTY = 'LLT');

-- Verificamos cuántos registros se han insertado
SELECT COUNT(*) AS 'Total de Diagnósticos' FROM Diagnosticos;

-- Mostramos algunos ejemplos
SELECT CUI, STR, SAB, TTY FROM Diagnosticos LIMIT 10; 