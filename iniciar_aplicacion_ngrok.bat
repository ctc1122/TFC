@echo off
title Clínica - Con Ngrok

echo ====================================
echo      CLINICA - CON NGROK TCP
echo ====================================
echo.
echo 🚀 Opción 1: Ngrok TCP (dinámico)
echo 🌐 Opción 2: Serveo.net (SSH)
echo 🏠 Opción 3: Localhost (siempre funciona)
echo.

cd /d "%~dp0"

REM Configurar ngrok si no está configurado
echo [0/5] Configurando ngrok...
ngrok config add-authtoken 2xzWLRhG1pyqAoR8v7H34acBRzw_7sgWAoSJkyV37LvBJBexD 2>nul
echo ✅ Ngrok configurado

REM 1. Iniciar Docker
echo [1/5] Iniciando Docker...
cd docker
docker-compose up -d mongodb1 mongodb2 mongodb3 mongodb-cima mariadb phpmyadmin
cd ..
echo ✅ Docker iniciado

REM 2. Iniciar servidor local
echo [2/5] Iniciando servidor local...
start "Servidor Local - Puerto 50002" cmd /k "mvn exec:java -Dexec.cleanupDaemonThreads=false"
timeout /t 10 >nul

REM 3. Iniciar túnel Ngrok TCP
echo [3/5] Iniciando túnel Ngrok TCP...
echo.
echo 🚀 Iniciando Ngrok TCP (puerto dinámico)...
start "Túnel Ngrok" cmd /k "ngrok tcp 50002"

timeout /t 5 >nul

REM 4. Intentar túnel SSH backup
echo [4/5] Iniciando túnel SSH backup...
echo.
echo 🌐 Túnel Serveo.net (backup)...
start "Túnel Serveo" cmd /k "ssh -R 50002:localhost:50002 serveo.net 2>nul || echo Serveo no disponible"

echo.
echo ⏳ Esperando túneles (20 segundos)...
timeout /t 20 >nul

REM 5. Iniciar aplicación
echo [5/5] Iniciando aplicación...
echo.
echo 🚀 La aplicación probará automáticamente:
echo    1️⃣ Serveo.net:50002 (backup SSH)
echo    2️⃣ Localhost:50002 (SIEMPRE funciona ✅)
echo.
echo 📱 ACCESO REMOTO NGROK:
echo    🔗 Revisar ventana "Túnel Ngrok" para URL TCP
echo    🌐 Formato: tcp://X.tcp.ngrok.io:PUERTO
echo.
echo 💡 Para acceso remoto: usar la URL de Ngrok manualmente
echo.
mvn javafx:run

echo.
echo ====================================
echo        APLICACION FUNCIONANDO
echo ====================================
echo.
echo ✅ Estado: La aplicación está corriendo
echo 🏠 Conexión automática: Localhost:50002
echo 📱 Acceso remoto: Revisar ventana Ngrok
echo.
pause 