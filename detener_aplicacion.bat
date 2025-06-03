@echo off
title Detener Aplicación

echo ====================================
echo     DETENIENDO APLICACION
echo ====================================
echo.

cd /d "%~dp0"

echo Cerrando aplicaciones...
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul
taskkill /f /im ssh.exe 2>nul

echo Deteniendo Docker...
cd docker
docker-compose down
cd ..

echo.
echo ====================================
echo         TODO DETENIDO
echo ====================================
pause 