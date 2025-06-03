# 🚀 Guía de Ejecución de la Aplicación

Esta guía te ayudará a ejecutar la aplicación de la clínica de forma automatizada.

## 📋 Requisitos Previos

Antes de ejecutar la aplicación, asegúrate de tener instalados los siguientes componentes:

### ✅ Requisitos Obligatorios:
1. **Docker Desktop** - [Descargar aquí](https://www.docker.com/products/docker-desktop/)
2. **Java 21** - [Descargar OpenJDK](https://openjdk.org/projects/jdk/21/)
3. **Maven** - [Descargar aquí](https://maven.apache.org/download.cgi)

### ⚙️ Configuración Opcional:
4. **Cliente OpenSSH** (para Windows):
   - Abrir Configuración → Aplicaciones → Características opcionales
   - Buscar "Cliente OpenSSH" e instalarlo

## 🎯 Scripts Disponibles

### Scripts Principales:

#### 1. `iniciar_aplicacion_fix.bat` 🆕 RECOMENDADO
- **Función**: Versión corregida que usa Maven para el servidor
- **Ventajas**:
  - Usa `mvn exec:java` para iniciar el servidor (más confiable)
  - Verificación extendida (8 intentos en lugar de 5)
  - Debug automático si falla
- **Recomendado**: Para resolver problemas de servidor

#### 2. `iniciar_aplicacion.bat` ⭐ ORIGINAL
- **Función**: Script original con verificación automática
- **Posible problema**: Puede fallar con el classpath manual

### Scripts de Debug y Pruebas:

#### 3. `debug_servidor.bat` 🔍 NUEVO
- **Función**: Diagnóstica problemas del servidor
- **Incluye**:
  - Verifica archivos compilados
  - Revisa dependencias
  - Prueba el servidor en primer plano
- **Cuándo usar**: Si el servidor no se inicia

#### 4. `iniciar_servidor_maven.bat` 🆕 NUEVO
- **Función**: Inicia solo el servidor usando Maven
- **Ventaja**: Ejecuta en primer plano para ver errores
- **Ideal para**: Probar solo el servidor

#### 5. `verificar_servidor.bat` 🔍
- **Función**: Verifica el estado de todos los servicios
- **Útil para**: Comprobar qué está funcionando

### Scripts de Utilidad:

#### 6. `compilar_proyecto.bat`
- **Función**: Compila el proyecto Java
- **Cuándo usar**: Primera vez o después de cambios

#### 7. `detener_aplicacion.bat`
- **Función**: Detiene todos los servicios
- **Recomendado**: Usar siempre al finalizar

#### 8. `iniciar_solo_servidor.bat`
- **Función**: Opciones manuales del servidor
- **Incluye**: Servidor GUI y automático

## 🔧 Instrucciones de Uso

### ❌ Si tienes el problema del servidor que no inicia:

1. **Usar el script corregido**:
   ```
   Doble clic en: iniciar_aplicacion_fix.bat
   ```

2. **O hacer debug paso a paso**:
   ```
   1. Doble clic en: debug_servidor.bat
   2. Doble clic en: iniciar_servidor_maven.bat  
   3. Doble clic en: verificar_servidor.bat
   ```

### ✅ Para uso normal:

1. **Compilar** (si es primera vez):
   ```
   Doble clic en: compilar_proyecto.bat
   ```

2. **Iniciar la aplicación**:
   ```
   Opción A: iniciar_aplicacion_fix.bat      # Script corregido
   Opción B: iniciar_aplicacion.bat          # Script original
   ```

3. **Al finalizar**:
   ```
   Doble clic en: detener_aplicacion.bat
   ```

## 🐛 Solución de Problemas

### ❌ Error: "El servidor no se inició correctamente" (TU PROBLEMA ACTUAL)

**Causa**: El servidor no puede iniciarse con el classpath manual.

**Soluciones en orden de prioridad**:

1. **Usar script corregido**:
   ```bash
   iniciar_aplicacion_fix.bat
   ```

2. **Debug paso a paso**:
   ```bash
   debug_servidor.bat          # Ver qué falla
   iniciar_servidor_maven.bat  # Probar servidor solo
   verificar_servidor.bat      # Comprobar estado
   ```

3. **Manualmente**:
   ```bash
   compilar_proyecto.bat       # Asegurar compilación
   iniciar_servidor_maven.bat  # Servidor en primer plano
   # En otra ventana:
   mvn javafx:run             # Aplicación JavaFX
   ```

### 🔧 Otros Problemas Comunes:

#### Error: "Docker no está instalado"
- Instalar Docker Desktop
- Reiniciar el sistema
- Verificar que Docker esté en el PATH

#### Error: "Maven no está disponible"
- Verificar instalación de Maven
- Agregar Maven al PATH
- Reiniciar PowerShell/CMD

#### Error: "No se puede conectar al servidor"
- Usar `verificar_servidor.bat` para diagnóstico
- Revisar si el puerto 50002 está ocupado: `netstat -an | findstr :50002`

## 📁 Estructura de Archivos

```
📂 Proyecto/
├── 🚀 iniciar_aplicacion_fix.bat    # Script corregido 🆕
├── 🚀 iniciar_aplicacion.bat        # Script original
├── 🔍 debug_servidor.bat            # Debug del servidor 🆕
├── 🖥️ iniciar_servidor_maven.bat    # Solo servidor con Maven 🆕
├── 🔍 verificar_servidor.bat        # Verificación de estado
├── 🔧 compilar_proyecto.bat         # Compilación
├── 🛑 detener_aplicacion.bat        # Parada de servicios
├── 🖥️ iniciar_solo_servidor.bat     # Servidor manual
├── 📖 README_EJECUCION.md           # Esta guía
├── 📂 docker/                       # Configuración Docker
│   └── docker-compose.yml
└── 📂 src/                          # Código fuente
    └── main/java/
        └── com/example/pruebamongodbcss/
            └── Servidor/
                ├── Servidor.java           # Servidor con GUI
                └── ServidorAutoInicio.java # Servidor automático
```

## 📞 Soporte Inmediato

### 🚨 **Para tu problema actual**:

1. **Ejecuta**: `debug_servidor.bat` para ver qué está pasando
2. **Luego**: `iniciar_aplicacion_fix.bat` para usar la versión corregida
3. **Si sigue fallando**: `iniciar_servidor_maven.bat` para ver errores en detalle

### 📋 Checklist de verificación:
- [ ] Docker Desktop está ejecutándose
- [ ] Maven funciona: `mvn --version`
- [ ] Java funciona: `java --version`
- [ ] Proyecto compilado: `compilar_proyecto.bat`

## 🎉 Novedades v2.3 - Solución de Problemas del Servidor

- 🐛 **Problema del servidor solucionado** - Nuevos scripts que usan Maven
- 🔍 **Script de debug completo** - `debug_servidor.bat` para diagnosticar
- 🆕 **Script corregido principal** - `iniciar_aplicacion_fix.bat`
- 🖥️ **Servidor independiente** - `iniciar_servidor_maven.bat` para pruebas
- ✅ **Múltiples opciones** - Varios enfoques para diferentes problemas
- 📋 **Guía de troubleshooting** - Pasos específicos para cada error

---
**¡Problema del servidor solucionado con múltiples opciones! 🔧✅** 