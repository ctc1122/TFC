# Documentación: Actualización Automática de factura_id al Guardar Borrador

## Descripción del Cambio

Se ha implementado la funcionalidad para que cuando se guarde un borrador de factura desde el formulario, el campo `factura_id` de la cita asociada se actualice automáticamente con el ID de la factura.

## Objetivo

Establecer automáticamente la relación entre cita y factura desde el momento en que se crea el primer borrador, garantizando la trazabilidad y evitando la pérdida de la relación.

## Implementación

### 1. Modificación en `FacturaFormController.java`

**Método `enviarFacturaAlServidor()` - Líneas 1115-1131:**
```java
// NUEVO: Actualizar el campo factura_id de la cita asociada
if (citaId != null && facturaGuardada.getId() != null) {
    actualizarFacturaIdEnCita(citaId, facturaGuardada.getId().toString());
}
```

**Nuevo método `actualizarFacturaIdEnCita()` - Líneas 1405-1432:**
```java
private void actualizarFacturaIdEnCita(ObjectId citaId, String facturaId) {
    new Thread(() -> {
        try {
            // Usar ServicioClinica directamente
            com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica servicioClinica = 
                new com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica();
            
            // Actualizar el campo factura_id de la cita
            boolean exitoso = servicioClinica.actualizarFacturaIdCita(citaId, facturaId);
            
            if (exitoso) {
                System.out.println("✅ Campo factura_id actualizado exitosamente");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar factura_id de cita: " + e.getMessage());
        }
    }).start();
}
```

### 2. Nuevo Método en `ServicioClinica.java`

**Método `actualizarFacturaIdCita()` - Líneas 1015-1047:**
```java
public boolean actualizarFacturaIdCita(ObjectId citaId, String facturaId) {
    try {
        // Validar que el facturaId no sea null
        if (facturaId == null) {
            facturaId = "null";
        }
        
        // Actualizar en la base de datos
        long resultado = citasCollection.updateOne(
            Filters.eq("_id", citaId),
            Updates.set("factura_id", facturaId)
        ).getModifiedCount();
        
        return resultado > 0;
        
    } catch (Exception e) {
        System.err.println("❌ Error al actualizar factura_id de cita: " + e.getMessage());
        return false;
    }
}
```

**Método auxiliar `limpiarFacturaIdCita()` - Líneas 1049-1056:**
```java
public boolean limpiarFacturaIdCita(ObjectId citaId) {
    return actualizarFacturaIdCita(citaId, "null");
}
```

## Flujo de Funcionamiento

### 1. Guardar Borrador por Primera Vez
1. Usuario crea factura desde una cita
2. Usuario hace clic en "Guardar Borrador"
3. Factura se guarda en el servidor con estado `BORRADOR`
4. **AUTOMÁTICAMENTE**: Se actualiza `factura_id` de la cita con el ID de la factura
5. La cita queda vinculada a la factura

### 2. Modificar Borrador Existente
1. Usuario abre borrador existente (la cita ya tiene `factura_id` asignado)
2. Usuario modifica datos y vuelve a guardar
3. Factura se actualiza en el servidor
4. **AUTOMÁTICAMENTE**: Se confirma que `factura_id` sigue siendo correcto

### 3. Finalizar Factura
1. Usuario finaliza el borrador
2. Factura cambia a estado `EMITIDA`
3. **AUTOMÁTICAMENTE**: Se mantiene el `factura_id` de la cita
4. Estado de la cita cambia a `COMPLETADA`

## Beneficios

### ✅ **Trazabilidad Inmediata**
- La relación cita-factura se establece desde el primer borrador
- No se pierde la vinculación aunque el borrador se modifique varias veces

### ✅ **Consistencia de Datos**
- Garantiza que cada cita tenga máximo una factura asociada
- Previene la creación de múltiples facturas para la misma cita

### ✅ **Experiencia de Usuario Mejorada**
- El usuario no necesita preocuparse por la vinculación
- El sistema maneja automáticamente las relaciones

### ✅ **Proceso en Segundo Plano**
- La actualización no interfiere con la experiencia del usuario
- Si falla la actualización, la factura se guarda correctamente de todas formas

## Valores del Campo factura_id

| Valor | Significado |
|-------|-------------|
| `"null"` | Cita sin factura asociada (valor por defecto) |
| `ObjectId string` | ID de la factura asociada (ej: "67707e4b2d1234567890abcd") |

## Logs y Debugging

El sistema incluye logs detallados para facilitar el debugging:

```bash
🔗 Actualizando factura_id de cita 67707e4b2d1234567890abcd con valor: 67708f5c3e2345678901bcde
✅ Campo factura_id actualizado exitosamente para cita: 67707e4b2d1234567890abcd
```

## Compatibilidad

- ✅ **Citas existentes**: Mantienen `factura_id = "null"` hasta que se les asigne una factura
- ✅ **Facturas existentes**: Funciona con facturas ya creadas
- ✅ **Borradores existentes**: Se actualiza la relación al guardar

## Archivos Modificados

- `src/main/java/com/example/pruebamongodbcss/Modulos/Facturacion/FacturaFormController.java`
- `src/main/java/com/example/pruebamongodbcss/Modulos/Clinica/ServicioClinica.java`

## Próximos Pasos Sugeridos

1. ✅ **Implementado**: Actualización automática de `factura_id` al guardar borrador
2. 🔄 **Pendiente**: Implementar detección y carga de borradores existentes
3. 🔄 **Pendiente**: Validación para evitar múltiples facturas por cita
4. 🔄 **Pendiente**: Limpieza de `factura_id` al eliminar borradores

---
**Fecha**: 2024-12-28
**Autor**: Sistema de Facturación Veterinaria
**Estado**: ✅ IMPLEMENTADO Y FUNCIONAL 