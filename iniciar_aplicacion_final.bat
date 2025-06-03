@echo off
title Clínica - Aplicación Final

echo ====================================
echo      CLINICA - APLICACION FINAL
echo ====================================
echo.
echo 🌐 Opción 1: Serveo.net (SSH)
echo 🔗 Opción 2: Tunnel.pyjam.as (SSH)
echo 🏠 Opción 3: Localhost (siempre funciona)
echo.

cd /d "%~dp0"

REM 1. Iniciar Docker
echo [1/4] Iniciando Docker...
cd docker
docker-compose up -d mongodb1 mongodb2 mongodb3 mongodb-cima mariadb phpmyadmin
cd ..
echo ✅ Docker iniciado

REM 2. Iniciar servidor local
echo [2/4] Iniciando servidor local...
start "Servidor Local - Puerto 50002" cmd /k "mvn exec:java -Dexec.cleanupDaemonThreads=false"
timeout /t 10 >nul

REM 3. Intentar túneles SSH (opcional)
echo [3/4] Iniciando túneles SSH opcionales...
echo.
echo 🔗 Túnel Serveo.net (puede fallar, no es problema)...
start "Túnel Serveo" cmd /k "ssh -R 50002:localhost:50002 serveo.net 2>nul || echo Serveo no disponible"

timeout /t 2 >nul
echo.
echo 🔗 Túnel Pyjam.as (puede fallar, no es problema)...
start "Túnel Pyjam" cmd /k "ssh -R 50002:localhost:50002 tunnel.pyjam.as 2>nul || echo Pyjam no disponible"

echo.
echo ⏳ Esperando túneles (15 segundos)...
timeout /t 15 >nul

REM 4. Iniciar aplicación
echo [4/4] Iniciando aplicación...
echo.
echo 🚀 La aplicación probará automáticamente:
echo    1️⃣ Serveo.net:50002 (si túnel está activo)
echo    2️⃣ Tunnel.pyjam.as:50002 (si túnel está activo)
echo    3️⃣ Localhost:50002 (SIEMPRE funciona ✅)
echo.
echo 💡 No te preocupes si los túneles fallan - la app funciona en local
echo.
mvn javafx:run

echo.
echo ====================================
echo        APLICACION FUNCIONANDO
echo ====================================
echo.
echo ✅ Estado: La aplicación está corriendo
echo 🏠 Conexión: Servidor local (puerto 50002)
echo 📱 Acceso remoto: Solo si los túneles funcionaron
echo.
pause 