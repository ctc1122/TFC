# Implementación de CIMA en la Aplicación Médica

Este proyecto incorpora la base de datos de medicamentos CIMA (Centro de Información online de Medicamentos de la AEMPS) para facilitar la búsqueda y referencia de medicamentos autorizados en España.

## Sobre CIMA

CIMA (Centro de Información online de Medicamentos de la AEMPS) es una base de datos que contiene información sobre los medicamentos autorizados en España. Proporciona información sobre:

1. Medicamentos autorizados
2. Principios activos
3. Laboratorios
4. Presentaciones
5. Vías de administración
6. Dosis
7. Estado de autorización

## Requisitos Previos

Para utilizar la funcionalidad de CIMA en esta aplicación, necesita:

1. **Docker**: Para ejecutar el contenedor de MongoDB
2. **Python 3.x**: Para ejecutar el script de importación
3. **Archivos XML de CIMA**: Descargados de la web de la AEMPS

## Configuración

1. Iniciar el contenedor de MongoDB:
   ```bash
   docker-compose up -d mongodb-cima
   ```

2. Instalar las dependencias de Python:
   ```bash
   cd scripts
   pip install -r requirements.txt
   ```

3. Importar los datos de CIMA:
   ```bash
   python import_cima.py <ruta_al_archivo_xml>
   ```

## Estructura de la Base de Datos

La base de datos CIMA se organiza en las siguientes colecciones:

1. **medicamentos**: Contiene la información principal de los medicamentos
   - código
   - nombre
   - principio_activo
   - laboratorio
   - presentación
   - via_administracion
   - dosis
   - estado
   - fecha_importacion

2. **principios_activos**: Contiene información sobre principios activos
3. **laboratorios**: Contiene información sobre laboratorios

## Uso en la Aplicación

1. La aplicación se conecta automáticamente a la base de datos CIMA en el puerto 27020
2. Use la interfaz de búsqueda para encontrar medicamentos por:
   - Nombre
   - Principio activo
   - Código
3. Los resultados se muestran en una tabla con la información relevante

## Mantenimiento

Para mantener la base de datos actualizada:

1. Descargar los nuevos archivos XML de CIMA desde la web de la AEMPS
2. Ejecutar el script de importación con los nuevos archivos
3. Los datos antiguos se actualizarán automáticamente

## Recursos

- [Web de CIMA](https://cima.aemps.es/cima/publico/home.html)
- [Documentación de MongoDB](https://docs.mongodb.com/)
- [Documentación de PyMongo](https://pymongo.readthedocs.io/) 