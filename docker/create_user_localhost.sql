-- Script para crear un usuario con todos los privilegios para localhost
CREATE USER IF NOT EXISTS 'tfc'@'localhost' IDENTIFIED BY 'miclave';

-- Otorgar todos los privilegios al usuario
GRANT ALL PRIVILEGES ON *.* TO 'tfc'@'localhost' WITH GRANT OPTION;

-- Refrescar los privilegios
FLUSH PRIVILEGES; 