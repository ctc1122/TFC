-- Script simple para crear la tabla Diagnosticos con registros que contengan 'trastorno'

-- Eliminamos la tabla si existe
DROP TABLE IF EXISTS Diagnosticos;

-- Creamos la tabla Diagnosticos sin restricciones complejas
CREATE TABLE Diagnosticos LIKE MRCONSO_SPA;

-- Insertamos solo los registros que contienen la palabra 'trastorno'
-- Sin condiciones complejas ni subconsultas
INSERT INTO Diagnosticos
SELECT * FROM MRCONSO_SPA
WHERE STR LIKE '%trastorno%' OR TTY = 'LLT';

-- Verificamos cuántos registros se han insertado
SELECT COUNT(*) AS 'Total_Diagnosticos' FROM Diagnosticos; 