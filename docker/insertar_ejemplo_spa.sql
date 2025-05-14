-- Insertar un registro de ejemplo en español en MRCONSO
INSERT INTO MRCONSO (CUI, LAT, TS, LUI, STT, SUI, ISPREF, AUI, SAB, TTY, CODE, STR, SRL, SUPPRESS) 
VALUES ('C0001234', 'SPA', 'P', 'L0123456', 'PF', 'S0123456', 'Y', 'A9999999', 'MSH', 'PT', 'D012345', 'Concepto de ejemplo en español', '0', 'N');

-- Verificar que se haya insertado el registro
SELECT * FROM MRCONSO WHERE LAT = 'SPA' LIMIT 1;

-- Luego, insertamos en MRCONSO_SPA solo los registros en español
INSERT INTO MRCONSO_SPA
SELECT * FROM MRCONSO
WHERE LAT = 'SPA';

-- Verificamos cuántos registros se han insertado
SELECT COUNT(*) AS 'Registros en español insertados' FROM MRCONSO_SPA; 