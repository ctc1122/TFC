// Script de migración para agregar el campo factura_id a todas las citas existentes
// Ejecutar en MongoDB Compass o CLI de MongoDB

// Primero, veamos cuántas citas no tienen el campo factura_id
print("=== MIGRACIÓN: Agregar campo factura_id a citas ===");

// Contar citas sin factura_id
var citasSinFacturaId = db.citas.countDocuments({ "factura_id": { $exists: false } });
print("Citas sin campo factura_id: " + citasSinFacturaId);

// Contar todas las citas
var totalCitas = db.citas.countDocuments({});
print("Total de citas: " + totalCitas);

if (citasSinFacturaId > 0) {
    print("\n--- Actualizando citas sin factura_id ---");
    
    // Agregar el campo factura_id con valor "null" a todas las citas que no lo tengan
    var resultado = db.citas.updateMany(
        { "factura_id": { $exists: false } },
        { $set: { "factura_id": "null" } }
    );
    
    print("Citas actualizadas: " + resultado.modifiedCount);
    print("Resultado: " + (resultado.modifiedCount === citasSinFacturaId ? "✅ ÉXITO" : "❌ ERROR"));
} else {
    print("✅ Todas las citas ya tienen el campo factura_id");
}

// Verificar el resultado
print("\n--- Verificación ---");
var citasConFacturaId = db.citas.countDocuments({ "factura_id": { $exists: true } });
print("Citas con campo factura_id: " + citasConFacturaId);

var citasConFacturaIdNull = db.citas.countDocuments({ "factura_id": "null" });
print("Citas con factura_id = 'null': " + citasConFacturaIdNull);

// Mostrar algunas citas como ejemplo
print("\n--- Ejemplos de citas actualizadas ---");
db.citas.find({ "factura_id": "null" }).limit(3).forEach(function(cita) {
    print("ID: " + cita._id + ", Paciente: " + cita.nombrePaciente + ", factura_id: " + cita.factura_id);
});

print("\n=== FIN DE MIGRACIÓN ==="); 