# Implementación de UMLS en la Aplicación Médica

Este proyecto incorpora la base de datos de terminología médica UMLS (Unified Medical Language System) para facilitar la búsqueda y referencia de términos médicos estandarizados en la aplicación.

## Sobre UMLS

UMLS (Unified Medical Language System) es un conjunto de archivos y software que reúne muchos vocabularios y estándares de salud y biomedicina para permitir la interoperabilidad entre sistemas informáticos. UMLS contiene:

1. **Metatesauro**: Base de datos de conceptos de salud y biomedicina de múltiples vocabularios
2. **Red Semántica**: Categorización y relaciones entre conceptos
3. **Lexicón Especialista**: Base de datos lexicográfica con información sintáctica, morfológica y ortográfica

## Requisitos Previos

Para utilizar la funcionalidad de UMLS en esta aplicación, necesita:

1. **Una cuenta UMLS**: Registrarse en el [Portal de UMLS](https://uts.nlm.nih.gov/uts/signup-login) (gratuito)
2. **Clave API**: Después de registrarse, obtener una clave API desde el portal de UMLS

## Configuración

1. Obtener una clave API de UMLS:
   - Visite [https://uts.nlm.nih.gov/uts/](https://uts.nlm.nih.gov/uts/)
   - Inicie sesión o cree una cuenta nueva
   - En su perfil, genere una nueva clave API

2. Configurar la aplicación:
   - Abra la clase `UMLSSearchController.java`
   - Reemplace el texto "your-api-key-here" con su clave API de UMLS
   - O configure la clave API en un archivo de propiedades

## Uso

1. Ejecute la aplicación con la clase `UMLSSearchMain`
2. Use la interfaz de búsqueda para encontrar términos médicos
3. La aplicación buscará primero en la base de datos local (MongoDB) y luego consultará la API de UMLS si es necesario
4. Los resultados se guardarán en la base de datos local para futuras consultas

## Limitaciones Actuales

La implementación actual es una versión simplificada con las siguientes limitaciones:

1. Solo soporta búsqueda básica de términos
2. La API real de UMLS ofrece muchas más funcionalidades (relaciones, jerarquías, etc.)
3. La implementación actual simula algunas respuestas para demostración

## Próximos Pasos Recomendados

Para una implementación completa de UMLS:

1. Implementar la autenticación real con la API de UMLS
2. Expandir funcionalidades para incluir jerarquías y relaciones entre conceptos
3. Mejorar el sistema de almacenamiento en caché de términos médicos
4. Añadir capacidad para descargarse subconjuntos relevantes de UMLS para uso offline

## Recursos

- [Documentación oficial de UMLS](https://www.nlm.nih.gov/research/umls/index.html)
- [API Web Services de UMLS](https://documentation.uts.nlm.nih.gov/rest/home.html)
- [Centro de Descargas de UMLS](https://www.nlm.nih.gov/research/umls/licensedcontent/umlsknowledgesources.html) 