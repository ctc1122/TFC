@echo off
title Iniciar Aplicación Clínica

echo ====================================
echo     INICIANDO APLICACION CLINICA
echo ====================================
echo.

cd /d "%~dp0"

REM Configurar ngrok
echo [0/6] Configurando ngrok...
ngrok config add-authtoken 2xzWLRhG1pyqAoR8v7H34acBRzw_7sgWAoSJkyV37LvBJBexD 2>nul

REM Iniciar Docker
echo [1/6] Iniciando Docker...
cd docker
docker-compose up -d
cd ..
echo Docker iniciado

REM Esperar Docker
echo [2/6] Esperando Docker (15 segundos)...
timeout /t 15 >nul

REM Iniciar Servidor SIN GUI con Maven (usando configuración del pom.xml)
echo [3/6] Iniciando Servidor SIN GUI...
start "Servidor SIN GUI" cmd /k "mvn exec:java -Dexec.cleanupDaemonThreads=false"

echo Esperando servidor (15 segundos)...
timeout /t 15 >nul

REM Intentar túneles
echo [4/6] Iniciando túneles...
echo 🚀 Túnel Ngrok TCP (primario)...
start "Ngrok Tunnel" cmd /k "ngrok tcp 50002"

timeout /t 3 >nul
echo 🔗 Túnel SSH Serveo (backup)...
start "SSH Tunnel" cmd /k "ssh -R 50002:localhost:50002 serveo.net || echo SSH no disponible - continuando..."

echo Esperando túneles (10 segundos)...
timeout /t 10 >nul

REM Iniciar Aplicación
echo [5/6] Iniciando aplicación...
echo.
echo 🚀 La aplicación intentará automáticamente:
echo    1. serveo.net:50002 (si funciona)
echo    2. localhost:50002 (siempre funciona)
echo.
echo 📱 Acceso remoto: Revisar ventana "Ngrok Tunnel"
echo.
mvn javafx:run

echo.
echo ====================================
echo         TODO INICIADO
echo ====================================
pause 