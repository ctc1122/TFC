// Script para inicializar las bases de datos y colecciones en MongoDB

// Crear las tres bases de datos y sus colecciones iniciales

// Base de datos Clinica
db = db.getSiblingDB('Clinica');
db.createCollection('usuarios');
db.createCollection('pacientes');
db.createCollection('citas');

// Base de datos Inventario
db = db.getSiblingDB('Inventario');
db.createCollection('productos');
db.createCollection('proveedores');
db.createCollection('stock');

// Base de datos Terminos
db = db.getSiblingDB('Terminos');
db.createCollection('terminos_medicos');
db.createCollection('categorias');

// Crear índices necesarios
db = db.getSiblingDB('Clinica');
db.usuarios.createIndex({ "email": 1 }, { unique: true });
db.pacientes.createIndex({ "dni": 1 }, { unique: true });

db = db.getSiblingDB('Inventario');
db.productos.createIndex({ "codigo": 1 }, { unique: true });

db = db.getSiblingDB('Terminos');
db.terminos_medicos.createIndex({ "nombre": 1 }, { unique: true });

// Insertar algunos datos de ejemplo
db.pacientes.insertMany([
  {
    nombre: "Juan",
    apellidos: "García López",
    dni: "12345678A",
    telefono: "600123456",
    email: "juan.garcia@ejemplo.com"
  },
  {
    nombre: "María",
    apellidos: "Rodríguez Pérez",
    dni: "87654321B",
    telefono: "600765432",
    email: "maria.rodriguez@ejemplo.com"
  }
]);

// Insertar datos de ejemplo en productos
db.productos.insertMany([
  {
    nombre: "Paracetamol",
    descripcion: "Analgésico y antipirético",
    codigo: "PAR001",
    precio: 5.95,
    categoria: "Medicamentos"
  },
  {
    nombre: "Vendas elásticas",
    descripcion: "Pack de 5 vendas",
    codigo: "VEN001",
    precio: 8.50,
    categoria: "Material sanitario"
  }
]);

// Insertar algunos términos médicos de ejemplo
db.terminos_medicos.insertMany([
  {
    termino: "Hipertensión",
    definicion: "Presión arterial alta",
    categoria: "Cardiología"
  },
  {
    termino: "Diabetes mellitus",
    definicion: "Trastorno metabólico caracterizado por nivel elevado de glucosa en sangre",
    categoria: "Endocrinología"
  }
]);

// Script de inicialización para MongoDB
db = db.getSiblingDB('Tienda');

// Crear colección de usuarios si no existe
if (!db.getCollectionNames().includes('usuarios')) {
    db.createCollection('usuarios');
    
    // Insertar usuario administrador
    db.usuarios.insertOne({
        usuario: "admin",
        password: "admin",
        rol: "ADMIN",
        nombre: "Administrador",
        apellido: "Sistema",
        fechaCreacion: new Date()
    });
    
    print("Colección de usuarios creada con usuario admin");
} else {
    print("La colección de usuarios ya existe");
}

// Crear índices para mejorar rendimiento
db.usuarios.createIndex({ "usuario": 1 }, { unique: true });

print("Inicialización de base de datos completada"); 