@echo off
title Clínica - Cuádruple Conexión

echo ====================================
echo    CLINICA - CUADRUPLE CONEXION
echo ====================================
echo.
echo 🌐 Opción 1: Serveo.net (SSH)
echo 🔗 Opción 2: Tunnel.pyjam.as (SSH)
echo 🚀 Opción 3: Localhost.run (SSH)
echo 🏠 Opción 4: Localhost (fallback)
echo.

cd /d "%~dp0"

REM 1. Iniciar Docker (solo bases de datos)
echo [1/4] Iniciando Docker...
cd docker
docker-compose up -d mongodb1 mongodb2 mongodb3 mongodb-cima mariadb phpmyadmin
cd ..
echo ✅ Docker iniciado

REM 2. Iniciar servidor local
echo [2/4] Iniciando servidor local...
start "Servidor Local - Puerto 50002" cmd /k "mvn exec:java -Dexec.cleanupDaemonThreads=false"
timeout /t 10 >nul

REM 3. Iniciar túneles SSH en paralelo
echo [3/4] Iniciando túneles SSH...
echo.
echo 🔗 Túnel 1: Serveo.net...
start "Túnel Serveo" cmd /k "ssh -R 50002:localhost:50002 serveo.net"

timeout /t 2 >nul
echo.
echo 🔗 Túnel 2: Tunnel.pyjam.as...
start "Túnel Pyjam" cmd /k "ssh -R 50002:localhost:50002 tunnel.pyjam.as"

timeout /t 2 >nul
echo.
echo 🔗 Túnel 3: Localhost.run...
start "Túnel Localhost.run" cmd /k "ssh -R 80:localhost:50002 ssh.localhost.run"

echo.
echo ⏳ Esperando túneles (25 segundos)...
timeout /t 25 >nul

REM 4. Iniciar aplicación
echo [4/4] Iniciando aplicación...
echo.
echo 🚀 La aplicación probará automáticamente:
echo    1️⃣ Serveo.net:50002 (SSH tunnel)
echo    2️⃣ Tunnel.pyjam.as:50002 (SSH tunnel)
echo    3️⃣ Localhost.run:80 (SSH tunnel dinámico)
echo    4️⃣ Localhost:50002 (local)
echo.
mvn javafx:run

echo.
echo ====================================
echo      CUADRUPLE CONEXION ACTIVA
echo ====================================
echo.
echo 📊 Estado de túneles:
echo    🌐 Serveo: Revisar ventana "Túnel Serveo"
echo    🔗 Pyjam: Revisar ventana "Túnel Pyjam"
echo    🚀 Localhost.run: Revisar ventana "Túnel Localhost.run"
echo    🏠 Local: Servidor corriendo en puerto 50002
echo.
pause 