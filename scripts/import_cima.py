#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pymongo import MongoClient
import os
import sys
from datetime import datetime

def conectar_mongodb():
    """Establece conexión con MongoDB."""
    try:
        client = MongoClient('mongodb://localhost:27020/')
        db = client['cima']
        return db
    except Exception as e:
        print(f"Error al conectar con MongoDB: {e}")
        sys.exit(1)

def procesar_medicamento(medicamento_elem):
    """Procesa un elemento medicamento y extrae sus datos."""
    try:
        # Extraer datos básicos
        codigo = medicamento_elem.find('nregistro').text if medicamento_elem.find('nregistro') is not None else None
        nombre = medicamento_elem.find('nombre').text if medicamento_elem.find('nombre') is not None else None
        
        # Extraer principio activo
        principio_activo = None
        if medicamento_elem.find('principiosActivos') is not None:
            pa_elem = medicamento_elem.find('principiosActivos/principioActivo')
            if pa_elem is not None:
                principio_activo = pa_elem.text
        
        # Extraer laboratorio
        laboratorio = None
        if medicamento_elem.find('laboratorio') is not None:
            lab_elem = medicamento_elem.find('laboratorio/nombre')
            if lab_elem is not None:
                laboratorio = lab_elem.text
        
        # Extraer presentación
        presentacion = None
        if medicamento_elem.find('presentaciones') is not None:
            pres_elem = medicamento_elem.find('presentaciones/presentacion')
            if pres_elem is not None:
                presentacion = pres_elem.text
        
        # Extraer vía de administración
        via_administracion = None
        if medicamento_elem.find('viasAdministracion') is not None:
            via_elem = medicamento_elem.find('viasAdministracion/viaAdministracion')
            if via_elem is not None:
                via_administracion = via_elem.text
        
        # Extraer dosis
        dosis = None
        if medicamento_elem.find('dosis') is not None:
            dosis_elem = medicamento_elem.find('dosis')
            if dosis_elem is not None:
                dosis = dosis_elem.text
        
        # Extraer estado
        estado = None
        if medicamento_elem.find('estado') is not None:
            estado_elem = medicamento_elem.find('estado')
            if estado_elem is not None:
                estado = estado_elem.text
        
        # Crear documento para MongoDB
        return {
            'codigo': codigo,
            'nombre': nombre,
            'principio_activo': principio_activo,
            'laboratorio': laboratorio,
            'presentacion': presentacion,
            'via_administracion': via_administracion,
            'dosis': dosis,
            'estado': estado,
            'fecha_importacion': datetime.now()
        }
    except Exception as e:
        print(f"Error al procesar medicamento: {e}")
        return None

def importar_medicamentos(xml_file, db):
    """Importa medicamentos desde un archivo XML a MongoDB."""
    try:
        # Parsear XML
        tree = ET.parse(xml_file)
        root = tree.getroot()
        
        # Obtener colección
        medicamentos = db.medicamentos
        
        # Contador para estadísticas
        total = 0
        importados = 0
        errores = 0
        
        # Procesar cada medicamento
        for medicamento_elem in root.findall('.//medicamento'):
            total += 1
            doc = procesar_medicamento(medicamento_elem)
            
            if doc and doc['codigo'] and doc['nombre']:
                try:
                    # Insertar en MongoDB
                    medicamentos.insert_one(doc)
                    importados += 1
                except Exception as e:
                    print(f"Error al insertar medicamento {doc['codigo']}: {e}")
                    errores += 1
            else:
                errores += 1
        
        # Crear índices
        medicamentos.create_index('codigo')
        medicamentos.create_index('nombre')
        medicamentos.create_index('principio_activo')
        
        return total, importados, errores
    
    except Exception as e:
        print(f"Error al procesar archivo {xml_file}: {e}")
        return 0, 0, 0

def main():
    """Función principal."""
    if len(sys.argv) != 2:
        print("Uso: python import_cima.py <archivo_xml>")
        sys.exit(1)
    
    xml_file = sys.argv[1]
    if not os.path.exists(xml_file):
        print(f"El archivo {xml_file} no existe.")
        sys.exit(1)
    
    print(f"Conectando a MongoDB...")
    db = conectar_mongodb()
    
    print(f"Importando medicamentos desde {xml_file}...")
    total, importados, errores = importar_medicamentos(xml_file, db)
    
    print("\nResumen de la importación:")
    print(f"Total de medicamentos procesados: {total}")
    print(f"Medicamentos importados correctamente: {importados}")
    print(f"Errores durante la importación: {errores}")

if __name__ == '__main__':
    main() 