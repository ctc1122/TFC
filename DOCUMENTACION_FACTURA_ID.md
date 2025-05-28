# Documentación: Campo factura_id en Citas

## Descripción del Cambio

Se ha implementado un nuevo campo `factura_id` en el modelo de citas (`ModeloCita`) para establecer una relación directa entre citas y facturas.

## Objetivos

1. **Relación Única**: Garantizar que cada cita pueda tener máximo una factura asociada
2. **Trazabilidad**: Facilitar la identificación de facturas relacionadas con citas específicas
3. **Control de Facturación**: Evitar facturación duplicada de la misma cita

## Cambios Implementados

### 1. Modelo de Cita (`ModeloCita.java`)

**Nuevo campo agregado:**
```java
private String facturaId; // Campo para relacionar con facturas
```

**Inicialización automática:**
- En el constructor vacío: `this.facturaId = "null";`
- En el constructor desde Document: Manejo de compatibilidad con documentos existentes
- En toDocument(): Inclusión del campo en la serialización a MongoDB

**Getters y Setters:**
```java
public String getFacturaId() {
    return facturaId;
}

public void setFacturaId(String facturaId) {
    this.facturaId = facturaId;
}
```

### 2. Valores del Campo

- **"null"**: String que indica que la cita no tiene factura asociada (valor por defecto)
- **ObjectId como String**: ID de la factura cuando existe una factura asociada
- **Compatibilidad**: Documentos existentes sin el campo se inicializan automáticamente con "null"

### 3. Migración de Datos Existentes

Se proporciona un script de migración `migrar_citas_factura_id.js` para actualizar citas existentes:

```javascript
// Agregar factura_id = "null" a todas las citas sin este campo
db.citas.updateMany(
    { "factura_id": { $exists: false } },
    { $set: { "factura_id": "null" } }
);
```

## Uso del Campo

### Crear Nueva Cita
```java
ModeloCita cita = new ModeloCita(); // facturaId se inicializa automáticamente como "null"
```

### Verificar si Cita Tiene Factura
```java
if (cita.getFacturaId().equals("null")) {
    // Cita sin factura asociada
} else {
    // Cita tiene factura con ID: cita.getFacturaId()
}
```

### Asociar Factura a Cita
```java
cita.setFacturaId(factura.getId().toString());
```

### Desasociar Factura de Cita
```java
cita.setFacturaId("null");
```

## Beneficios

1. **Integridad de Datos**: Relación clara entre citas y facturas
2. **Rendimiento**: Consultas más eficientes para encontrar facturas por cita
3. **Escalabilidad**: Preparación para funcionalidades futuras de facturación
4. **Compatibilidad**: Funcionamiento transparente con datos existentes

## Consideraciones Técnicas

- **Tipo de Dato**: Se usa String en lugar de ObjectId para permitir valor "null" explícito
- **Retrocompatibilidad**: Constructor desde Document maneja documentos sin el campo
- **Validación**: El sistema debe validar que el ID de factura existe antes de asignarlo
- **Sincronización**: Cambios en facturas deben reflejarse en las citas correspondientes

## Archivos Modificados

- `src/main/java/com/example/pruebamongodbcss/Modulos/Clinica/ModeloCita.java`

## Archivos Creados

- `migrar_citas_factura_id.js` - Script de migración para MongoDB
- `DOCUMENTACION_FACTURA_ID.md` - Esta documentación

## Próximos Pasos Sugeridos

1. Ejecutar el script de migración en la base de datos de producción
2. Implementar métodos utilitarios para gestión del campo factura_id
3. Agregar validaciones en el lado del servidor
4. Crear índices en MongoDB para optimizar consultas por factura_id
5. Implementar logs de auditoría para cambios en este campo

---
**Fecha**: 2024-12-28
**Autor**: Sistema de Facturación Veterinaria 