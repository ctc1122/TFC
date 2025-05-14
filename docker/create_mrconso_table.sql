-- Script para crear la tabla MRCONSO en la base de datos umls

CREATE TABLE IF NOT EXISTS MRCONSO (
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

-- Para cargar los datos, es necesario usar un volumen o copiar el archivo al contenedor
-- Por ejemplo, si se monta el directorio de UMLS como volumen en docker-compose.yml:
-- Se podría usar: LOAD DATA INFILE '/var/lib/mysql-files/MRCONSO.RRF'

-- Alternativamente, se puede cargar desde el cliente usando LOCAL:
-- LOAD DATA LOCAL INFILE '/path/to/UMLS/2024AB/META/MRCONSO.RRF' 
-- INTO TABLE MRCONSO
-- FIELDS TERMINATED BY '|'
-- LINES TERMINATED BY '\n'
-- (CUI, LAT, TS, LUI, STT, SUI, ISPREF, AUI, SAUI, SCUI, SDUI, SAB, TTY, CODE, STR, SRL, SUPPRESS, CVF); 