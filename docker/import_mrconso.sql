-- Script para importar datos de MRCONSO.RRF a la tabla MRCONSO
-- Asegúrate de crear primero la tabla MRCONSO con el script create_mrconso_table.sql

-- Carga los datos desde el archivo CSV (RRF es similar a CSV pero usa "|" como separador)
LOAD DATA LOCAL INFILE 'C:/ProyectoFinCurso/PruebaMongoDBCSS/MRCONSO.RRF' 
INTO TABLE MRCONSO2
FIELDS TERMINATED BY '|' 
ENCLOSED BY '' 
LINES TERMINATED BY '\n'
(CUI, LAT, TS, LUI, STT, SUI, ISPREF, AUI, SAUI, SCUI, SDUI, SAB, TTY, CODE, STR, SRL, SUPPRESS, CVF);

-- Alternativa si no tienes acceso local al archivo:
-- Puedes cargar los datos uno por uno con INSERT statements
-- INSERT INTO MRCONSO (CUI, LAT, TS, LUI, STT, SUI, ISPREF, AUI, SAUI, SCUI, SDUI, SAB, TTY, CODE, STR, SRL, SUPPRESS, CVF)
-- VALUES ('C0001234', 'ENG', 'P', 'L0123456', 'PF', 'S0123456', 'Y', 'A0123456', NULL, NULL, NULL, 'MSH', 'PT', 'D012345', 'Sample concept', '0', 'N', NULL);

-- Nota: Para usar LOAD DATA LOCAL INFILE en MySQL/MariaDB:
-- 1. Asegúrate de que local_infile=1 esté habilitado en la configuración de MySQL
-- 2. Si usas phpMyAdmin, asegúrate de tener acceso al sistema de archivos donde está el archivo
-- 3. Si el archivo está en tu máquina local, deberás cargar el archivo a través de la interfaz de importación de phpMyAdmin 