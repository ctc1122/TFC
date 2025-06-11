# 🏥 INSTRUCCIONES DE INSTALACIÓN - CLÍNICA

## 📋 REQUISITOS PREVIOS

### Instalar en el ordenador destino:
1. **Docker Desktop** - https://www.docker.com/products/docker-desktop/
2. **Java JDK 11+** - https://adoptium.net/
3. **Maven 3.6+** - https://maven.apache.org/download.cgi

### Verificar instalación:
```bash
java -version      # Debe mostrar Java 11+
mvn -version       # Debe mostrar Maven 3.6+
docker --version   # Debe mostrar Docker
```

## 🚀 INSTALACIÓN

### Paso 1: Descomprimir
1. Extraer **todo el contenido** del ZIP
2. Mantener la estructura de carpetas

### Paso 2: Ejecutar
1. Abrir CMD/PowerShell como **Administrador**
2. Navegar a la carpeta extraída
3. Ejecutar: `iniciar_aplicacion_final.bat`

## 🔄 QUÉ HACE EL SCRIPT AUTOMÁTICAMENTE

```
[1/4] 🐳 Inicia Docker (MongoDB + MariaDB)
[2/4] 🖥️  Inicia servidor local (puerto 50002)  
[3/4] 🌐 Intenta crear túneles SSH (opcional)
[4/4] 🚀 Inicia la aplicación JavaFX
```

## ✅ VERIFICACIÓN AUTOMÁTICA UMLS

- **Primera ejecución**: Descarga e instala diagnósticos UMLS (puede tardar 5-10 minutos)
- **Siguientes ejecuciones**: Verifica que existe y continúa normalmente

## 🔧 SOLUCIÓN DE PROBLEMAS

### Error: "Docker no encontrado"
```bash
# Instalar Docker Desktop y asegurar que está funcionando
docker ps  # Debe mostrar contenedores
```

### Error: "Java no encontrado"
```bash
# Añadir Java al PATH del sistema
# O instalar desde: https://adoptium.net/
```

### Error: "Maven no encontrado"  
```bash
# Añadir Maven al PATH del sistema
# O usar Maven Wrapper incluido: ./mvnw
```

### Error: "Puerto ocupado"
```bash
# Cambiar puertos en docker/docker-compose.yml si es necesario
# Por defecto usa: 27017-27020, 3306, 50002
```

## 📱 ACCESO A LA APLICACIÓN

### Local (siempre funciona):
```
http://localhost:50002
```

### Remoto (si túneles funcionan):
```
https://serveo.net:50002  
https://tunnel.pyjam.as:50002
```

## 🎯 CONTACTO

Si hay problemas, reportar con:
1. Sistema operativo
2. Versiones de Java/Maven/Docker
3. Mensaje de error completo 