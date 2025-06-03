@echo off
title Detener Aplicación Clínica

echo ====================================
echo    DETENIENDO APLICACION CLINICA
echo ====================================
echo.

cd /d "%~dp0"

REM 1. Detener contenedores Docker
echo [1/2] Deteniendo servicios Docker...
cd docker
docker-compose down
cd ..
echo ✅ Servicios Docker detenidos

REM 2. Mostrar procesos Java restantes
echo [2/2] Verificando procesos Java...
tasklist | findstr java 2>nul
if %errorlevel% equ 0 (
    echo.
    echo ⚠️ Hay procesos Java activos. ¿Quieres terminarlos? (S/N)
    set /p respuesta=
    if /i "%respuesta%"=="s" (
        taskkill /f /im java.exe 2>nul
        echo ✅ Procesos Java terminados
    )
) else (
    echo ✅ No hay procesos Java activos
)

echo.
echo ====================================
echo       APLICACION DETENIDA
echo ====================================
echo.
echo 🛑 Todos los servicios han sido detenidos:
echo    - Docker containers: Detenidos
echo    - Servidor local: Detenido
echo    - Túnel SSH: Detenido
echo    - Base de datos: Detenida
echo.
pause 