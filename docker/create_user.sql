-- Script para crear un usuario con todos los privilegios
CREATE USER IF NOT EXISTS 'tfc'@'%' IDENTIFIED BY 'miclave';

-- Otorgar todos los privilegios al usuario
GRANT ALL PRIVILEGES ON *.* TO 'tfc'@'%' WITH GRANT OPTION;

-- Refrescar los privilegios
FLUSH PRIVILEGES; 