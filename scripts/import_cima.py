#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pymongo import MongoClient
import os
import sys
from datetime import datetime

def conectar_mongodb():
    """Establece conexión con MongoDB."""
    try:
        # Conexión explícita al puerto 27017 donde se ejecuta mongodb1
        client = MongoClient('mongodb://localhost:27017/')
        db = client['cima']
        return db
    except Exception as e:
        print(f"Error al conectar con MongoDB: {e}")
        sys.exit(1)

def limpiar_namespaces(root):
    """Elimina los namespaces de los elementos XML para facilitar la búsqueda."""
    for elem in root.iter():
        if '}' in elem.tag:
            elem.tag = elem.tag.split('}', 1)[1]
    return root

def importar_medicamentos_dcp(xml_file, db):
    """Importa datos del archivo DICCIONARIO_DCP.xml a MongoDB."""
    try:
        print(f"Procesando archivo DCP: {xml_file}...")
        tree = ET.parse(xml_file)
        root = tree.getroot()
        root = limpiar_namespaces(root)
        
        # Obtener colección
        medicamentos = db.medicamentos
        
        # Contador para estadísticas
        total = 0
        importados = 0
        errores = 0
        
        # Procesar cada dcp
        for dcp_elem in root.findall('.//dcp'):
            total += 1
            try:
                codigo_dcp = dcp_elem.find('codigodcp').text if dcp_elem.find('codigodcp') is not None else None
                nombre_dcp = dcp_elem.find('nombredcp').text if dcp_elem.find('nombredcp') is not None else None
                codigo_dcsa = dcp_elem.find('codigodcsa').text if dcp_elem.find('codigodcsa') is not None else None
                
                if codigo_dcp and nombre_dcp:
                    # Insertar o actualizar en MongoDB
                    medicamento = {
                        'codigo_dcp': codigo_dcp,
                        'nombre': nombre_dcp,
                        'codigo_dcsa': codigo_dcsa,
                        'fecha_importacion': datetime.now()
                    }
                    
                    # Usar upsert para actualizar si existe o insertar si no
                    result = medicamentos.update_one(
                        {'codigo_dcp': codigo_dcp}, 
                        {'$set': medicamento},
                        upsert=True
                    )
                    
                    if result.upserted_id is not None or result.modified_count > 0:
                        importados += 1
                        if importados % 100 == 0:
                            print(f"Importados {importados} medicamentos DCP...")
                else:
                    errores += 1
            except Exception as e:
                print(f"Error al procesar DCP: {e}")
                errores += 1
                
        return total, importados, errores
    except Exception as e:
        print(f"Error al procesar archivo DCP {xml_file}: {e}")
        return 0, 0, 0

def importar_medicamentos_dcpf(xml_file, db):
    """Importa datos del archivo DICCIONARIO_DCPF.xml a MongoDB."""
    try:
        print(f"Procesando archivo DCPF: {xml_file}...")
        tree = ET.parse(xml_file)
        root = tree.getroot()
        root = limpiar_namespaces(root)
        
        # Obtener colección
        medicamentos = db.medicamentos
        
        # Contador para estadísticas
        total = 0
        importados = 0
        errores = 0
        
        # Procesar cada dcpf
        for dcpf_elem in root.findall('.//dcpf'):
            total += 1
            try:
                codigo_dcpf = dcpf_elem.find('codigodcpf').text if dcpf_elem.find('codigodcpf') is not None else None
                nombre_dcpf = dcpf_elem.find('nombredcpf').text if dcpf_elem.find('nombredcpf') is not None else None
                codigo_dcp = dcpf_elem.find('codigodcp').text if dcpf_elem.find('codigodcp') is not None else None
                
                if codigo_dcpf and codigo_dcp:
                    # Actualizar medicamento existente o ignorar
                    result = medicamentos.update_one(
                        {'codigo_dcp': codigo_dcp},
                        {'$set': {
                            'codigo_dcpf': codigo_dcpf,
                            'nombre_dcpf': nombre_dcpf,
                            'fecha_actualizacion': datetime.now()
                        }}
                    )
                    
                    if result.modified_count > 0:
                        importados += 1
                        if importados % 100 == 0:
                            print(f"Actualizados {importados} medicamentos DCPF...")
                else:
                    errores += 1
            except Exception as e:
                print(f"Error al procesar DCPF: {e}")
                errores += 1
                
        return total, importados, errores
    except Exception as e:
        print(f"Error al procesar archivo DCPF {xml_file}: {e}")
        return 0, 0, 0

def importar_medicamentos_dcsa(xml_file, db):
    """Importa datos del archivo DICCIONARIO_DCSA.xml a MongoDB."""
    try:
        print(f"Procesando archivo DCSA: {xml_file}...")
        tree = ET.parse(xml_file)
        root = tree.getroot()
        root = limpiar_namespaces(root)
        
        # Obtener colección
        medicamentos = db.medicamentos
        principios_activos = db.principios_activos
        
        # Contador para estadísticas
        total = 0
        importados = 0
        errores = 0
        
        # Procesar cada dcsa
        for dcsa_elem in root.findall('.//dcsa'):
            total += 1
            try:
                codigo_dcsa = dcsa_elem.find('codigodcsa').text if dcsa_elem.find('codigodcsa') is not None else None
                nombre_dcsa = dcsa_elem.find('nombredcsa').text if dcsa_elem.find('nombredcsa') is not None else None
                
                if codigo_dcsa and nombre_dcsa:
                    # Insertar principio activo
                    principio_activo = {
                        'codigo_dcsa': codigo_dcsa,
                        'nombre': nombre_dcsa,
                        'fecha_importacion': datetime.now()
                    }
                    
                    # Usar upsert para actualizar si existe o insertar si no
                    principios_activos.update_one(
                        {'codigo_dcsa': codigo_dcsa},
                        {'$set': principio_activo},
                        upsert=True
                    )
                    
                    # Actualizar todos los medicamentos que tienen este código DCSA
                    result = medicamentos.update_many(
                        {'codigo_dcsa': codigo_dcsa},
                        {'$set': {
                            'principio_activo': nombre_dcsa,
                            'fecha_actualizacion': datetime.now()
                        }}
                    )
                    
                    if result.modified_count > 0:
                        importados += 1
                        if importados % 100 == 0:
                            print(f"Actualizados {importados} principios activos DCSA...")
                else:
                    errores += 1
            except Exception as e:
                print(f"Error al procesar DCSA: {e}")
                errores += 1
                
        return total, importados, errores
    except Exception as e:
        print(f"Error al procesar archivo DCSA {xml_file}: {e}")
        return 0, 0, 0

def crear_indices(db):
    """Crea índices para mejorar el rendimiento de las consultas."""
    try:
        print("Creando índices...")
        db.medicamentos.create_index('codigo_dcp')
        db.medicamentos.create_index('codigo_dcpf')
        db.medicamentos.create_index('codigo_dcsa')
        db.medicamentos.create_index('nombre')
        db.principios_activos.create_index('codigo_dcsa')
        db.principios_activos.create_index('nombre')
        print("Índices creados correctamente.")
    except Exception as e:
        print(f"Error al crear índices: {e}")

def procesar_archivos_cima(directorio, db):
    """Procesa todos los archivos XML de CIMA en el directorio especificado."""
    archivos = {
        'dcp': None,
        'dcpf': None,
        'dcsa': None
    }
    
    # Buscar los archivos en el directorio
    for archivo in os.listdir(directorio):
        ruta_completa = os.path.join(directorio, archivo)
        if os.path.isfile(ruta_completa) and archivo.endswith('.xml'):
            if 'DICCIONARIO_DCP.xml' in archivo:
                archivos['dcp'] = ruta_completa
            elif 'DICCIONARIO_DCPF.xml' in archivo:
                archivos['dcpf'] = ruta_completa
            elif 'DICCIONARIO_DCSA.xml' in archivo:
                archivos['dcsa'] = ruta_completa
    
    resultados = {}
    
    # Procesar archivos en orden: primero DCP, luego DCPF y finalmente DCSA
    if archivos['dcp']:
        resultados['dcp'] = importar_medicamentos_dcp(archivos['dcp'], db)
    else:
        print("No se encontró el archivo DICCIONARIO_DCP.xml")
        
    if archivos['dcpf']:
        resultados['dcpf'] = importar_medicamentos_dcpf(archivos['dcpf'], db)
    else:
        print("No se encontró el archivo DICCIONARIO_DCPF.xml")
        
    if archivos['dcsa']:
        resultados['dcsa'] = importar_medicamentos_dcsa(archivos['dcsa'], db)
    else:
        print("No se encontró el archivo DICCIONARIO_DCSA.xml")
    
    crear_indices(db)
    return resultados

def main():
    """Función principal."""
    if len(sys.argv) != 2:
        print("Uso: python import_cima.py <directorio_xml>")
        sys.exit(1)
    
    directorio = sys.argv[1]
    if not os.path.exists(directorio):
        print(f"El directorio {directorio} no existe.")
        sys.exit(1)
    
    print(f"Conectando a MongoDB...")
    db = conectar_mongodb()
    
    print(f"Procesando archivos CIMA desde {directorio}...")
    resultados = procesar_archivos_cima(directorio, db)
    
    print("\nResumen de la importación:")
    if 'dcp' in resultados:
        total, importados, errores = resultados['dcp']
        print(f"DCP - Total procesados: {total}, Importados: {importados}, Errores: {errores}")
        
    if 'dcpf' in resultados:
        total, importados, errores = resultados['dcpf']
        print(f"DCPF - Total procesados: {total}, Actualizados: {importados}, Errores: {errores}")
        
    if 'dcsa' in resultados:
        total, importados, errores = resultados['dcsa']
        print(f"DCSA - Total procesados: {total}, Actualizados: {importados}, Errores: {errores}")

if __name__ == '__main__':
    main() 