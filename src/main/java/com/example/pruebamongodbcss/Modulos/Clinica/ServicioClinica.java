package com.example.pruebamongodbcss.Modulos.Clinica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.EstadoCita;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import Utilidades.GestorConexion;

/**
 * Servicio para gestionar la interacción con la base de datos de la clínica veterinaria.
 */
public class ServicioClinica {
    private final MongoDatabase database;
    private final MongoCollection<Document> pacientesCollection;
    private final MongoCollection<Document> propietariosCollection;
    private final MongoCollection<Document> diagnosticosCollection;
    private final MongoCollection<Document> citasCollection;
    private final MongoCollection<Document> seguimientosCollection;

    /**
     * Constructor del servicio clínico.
     */
    public ServicioClinica() {
        // Conectar a la base de datos
        this.database = GestorConexion.conectarClinica();
        
        // Asegurar que existen las colecciones necesarias
        List<String> collectionNames = new ArrayList<>();
        database.listCollectionNames().into(collectionNames);
        
        if (!collectionNames.contains("pacientes")) {
            database.createCollection("pacientes");
        }
        
        if (!collectionNames.contains("propietarios")) {
            database.createCollection("propietarios");
        }
        
        if (!collectionNames.contains("diagnosticos")) {
            database.createCollection("diagnosticos");
        }
        
        if (!collectionNames.contains("citas")) {
            database.createCollection("citas");
        }
        
        if (!collectionNames.contains("seguimientos")) {
            database.createCollection("seguimientos");
        }
        
        // Obtener referencia a las colecciones
        this.pacientesCollection = database.getCollection("pacientes");
        this.propietariosCollection = database.getCollection("propietarios");
        this.diagnosticosCollection = database.getCollection("diagnosticos");
        this.citasCollection = database.getCollection("citas");
        this.seguimientosCollection = database.getCollection("seguimientos");
    }

    // ********** MÉTODOS PARA PACIENTES **********
    
    /**
     * Guardar un paciente en la base de datos.
     * @param paciente El paciente a guardar
     * @return El ID del paciente guardado
     */
    public ObjectId guardarPaciente(ModeloPaciente paciente) {
        Document doc = paciente.toDocument();
        
        if (paciente.getId() == null) {
            // Nuevo paciente
            pacientesCollection.insertOne(doc);
            return doc.getObjectId("_id");
        } else {
            // Actualizar paciente existente
            pacientesCollection.replaceOne(
                    Filters.eq("_id", paciente.getId()),
                    doc
            );
            return paciente.getId();
        }
    }
    
    /**
     * Obtener un paciente por su ID.
     * @param id ID del paciente
     * @return El paciente encontrado o null si no existe
     */
    public ModeloPaciente obtenerPacientePorId(ObjectId id) {
        Document doc = pacientesCollection.find(Filters.eq("_id", id)).first();
        return doc != null ? new ModeloPaciente(doc) : null;
    }
    
    /**
     * Buscar pacientes por el nombre.
     * @param nombre Nombre o parte del nombre del paciente
     * @return Lista de pacientes que coinciden con el criterio
     */
    public List<ModeloPaciente> buscarPacientesPorNombre(String nombre) {
        Bson filtro = Filters.regex("nombre", ".*" + nombre + ".*", "i");
        return buscarPacientes(filtro);
    }
    
    /**
     * Buscar pacientes por propietario.
     * @param propietarioId ID del propietario
     * @return Lista de pacientes del propietario
     */
    public List<ModeloPaciente> buscarPacientesPorPropietario(ObjectId propietarioId) {
        Bson filtro = Filters.eq("propietarioId", propietarioId);
        return buscarPacientes(filtro);
    }
    
    /**
     * Buscar pacientes con un filtro personalizado.
     * @param filtro Filtro de búsqueda
     * @return Lista de pacientes que coinciden con el filtro
     */
    public List<ModeloPaciente> buscarPacientes(Bson filtro) {
        List<ModeloPaciente> pacientes = new ArrayList<>();
        FindIterable<Document> docs = pacientesCollection.find(filtro).sort(Sorts.ascending("nombre"));
        
        try (MongoCursor<Document> cursor = docs.iterator()) {
            while (cursor.hasNext()) {
                pacientes.add(new ModeloPaciente(cursor.next()));
            }
        }
        
        return pacientes;
    }
    
    /**
     * Obtener todos los pacientes.
     * @return Lista completa de pacientes
     */
    public List<ModeloPaciente> obtenerTodosPacientes() {
        return buscarPacientes(new Document());
    }
    
    /**
     * Eliminar un paciente por su ID.
     * @param id ID del paciente a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminarPaciente(ObjectId id) {
        // Primero eliminar las referencias en propietarios
        ModeloPaciente paciente = obtenerPacientePorId(id);
        if (paciente != null && paciente.getPropietarioId() != null) {
            propietariosCollection.updateOne(
                    Filters.eq("_id", paciente.getPropietarioId()),
                    Updates.pull("mascotas", id)
            );
        }
        
        // Luego eliminar los diagnósticos asociados
        diagnosticosCollection.deleteMany(Filters.eq("pacienteId", id));
        
        // Eliminar citas asociadas
        citasCollection.deleteMany(Filters.eq("pacienteId", id));
        
        // Eliminar seguimientos
        seguimientosCollection.deleteMany(Filters.eq("pacienteId", id));
        
        // Finalmente eliminar el paciente
        DeleteResult result = pacientesCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
    }
    
    /**
     * Agregar un nuevo paciente a la base de datos.
     * @param paciente El paciente a agregar
     * @return true si se agregó correctamente, false en caso contrario
     */
    public boolean agregarPaciente(ModeloPaciente paciente) {
        try {
            // Asegurarse de que no tenga un ID (es un nuevo registro)
            paciente.setId(null);
            
            // Guardar en la base de datos
            ObjectId id = guardarPaciente(paciente);
            
            // Actualizar el ID en el modelo
            paciente.setId(id);
            
            return id != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Actualizar un paciente existente en la base de datos.
     * @param paciente El paciente a actualizar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizarPaciente(ModeloPaciente paciente) {
        try {
            if (paciente.getId() == null) {
                return false; // No podemos actualizar sin ID
            }
            
            // Guardar en la base de datos
            ObjectId id = guardarPaciente(paciente);
            return id != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ********** MÉTODOS PARA PROPIETARIOS **********
    
    /**
     * Guardar un propietario en la base de datos.
     * @param propietario El propietario a guardar
     * @return El ID del propietario guardado
     */
    public ObjectId guardarPropietario(ModeloPropietario propietario) {
        Document doc = propietario.toDocument();
        
        if (propietario.getId() == null) {
            // Nuevo propietario
            propietariosCollection.insertOne(doc);
            return doc.getObjectId("_id");
        } else {
            // Actualizar propietario existente
            propietariosCollection.replaceOne(
                    Filters.eq("_id", propietario.getId()),
                    doc
            );
            return propietario.getId();
        }
    }
    
    /**
     * Obtener un propietario por su ID.
     * @param id ID del propietario
     * @return El propietario encontrado o null si no existe
     */
    public ModeloPropietario obtenerPropietarioPorId(ObjectId id) {
        Document doc = propietariosCollection.find(Filters.eq("_id", id)).first();
        return doc != null ? new ModeloPropietario(doc) : null;
    }
    
    /**
     * Buscar propietarios por nombre o apellidos.
     * @param nombre Nombre, apellidos o parte de ellos
     * @return Lista de propietarios que coinciden con el criterio
     */
    public List<ModeloPropietario> buscarPropietariosPorNombre(String nombre) {
        Bson filtro = Filters.regex("nombreCompleto", ".*" + nombre + ".*", "i");
        return buscarPropietarios(filtro);
    }
    
    /**
     * Buscar un propietario por su DNI.
     * @param dni DNI del propietario
     * @return El propietario encontrado o null si no existe
     */
    public ModeloPropietario buscarPropietarioPorDNI(String dni) {
        Document doc = propietariosCollection.find(Filters.eq("dni", dni)).first();
        return doc != null ? new ModeloPropietario(doc) : null;
    }
    
    /**
     * Buscar propietarios con un filtro personalizado.
     * @param filtro Filtro de búsqueda
     * @return Lista de propietarios que coinciden con el filtro
     */
    public List<ModeloPropietario> buscarPropietarios(Bson filtro) {
        List<ModeloPropietario> propietarios = new ArrayList<>();
        FindIterable<Document> docs = propietariosCollection.find(filtro).sort(Sorts.ascending("nombreCompleto"));
        
        try (MongoCursor<Document> cursor = docs.iterator()) {
            while (cursor.hasNext()) {
                propietarios.add(new ModeloPropietario(cursor.next()));
            }
        }
        
        return propietarios;
    }
    
    /**
     * Obtener todos los propietarios.
     * @return Lista completa de propietarios
     */
    public List<ModeloPropietario> obtenerTodosPropietarios() {
        return buscarPropietarios(new Document());
    }
    
    /**
     * Eliminar un propietario por su ID.
     * @param id ID del propietario a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminarPropietario(ObjectId id) {
        // Verificar si tiene mascotas asociadas
        ModeloPropietario propietario = obtenerPropietarioPorId(id);
        if (propietario != null && propietario.getMascotas() != null && !propietario.getMascotas().isEmpty()) {
            // No permitir eliminar propietarios con mascotas
            return false;
        }
        
        DeleteResult result = propietariosCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
    }
    
    /**
     * Actualizar un propietario existente en la base de datos.
     * @param propietario El propietario a actualizar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizarPropietario(ModeloPropietario propietario) {
        try {
            if (propietario.getId() == null) {
                return false; // No podemos actualizar sin ID
            }
            
            // Guardar en la base de datos
            ObjectId id = guardarPropietario(propietario);
            return id != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ********** MÉTODOS PARA DIAGNÓSTICOS **********
    
    /**
     * Guardar un diagnóstico en la base de datos.
     * @param diagnostico El diagnóstico a guardar
     * @return El ID del diagnóstico guardado
     */
    public ObjectId guardarDiagnostico(ModeloDiagnostico diagnostico) {
        Document doc = diagnostico.toDocument();
        
        if (diagnostico.getId() == null) {
            // Nuevo diagnóstico
            diagnosticosCollection.insertOne(doc);
            return doc.getObjectId("_id");
        } else {
            // Actualizar diagnóstico existente
            diagnosticosCollection.replaceOne(
                    Filters.eq("_id", diagnostico.getId()),
                    doc
            );
            return diagnostico.getId();
        }
    }
    
    /**
     * Obtener un diagnóstico por su ID.
     * @param id ID del diagnóstico
     * @return El diagnóstico encontrado o null si no existe
     */
    public ModeloDiagnostico obtenerDiagnosticoPorId(ObjectId id) {
        Document doc = diagnosticosCollection.find(Filters.eq("_id", id)).first();
        return doc != null ? new ModeloDiagnostico(doc) : null;
    }
    
    /**
     * Buscar diagnósticos para un paciente específico.
     * @param pacienteId ID del paciente
     * @return Lista de diagnósticos del paciente
     */
    public List<ModeloDiagnostico> buscarDiagnosticosPorPaciente(ObjectId pacienteId) {
        Bson filtro = Filters.eq("pacienteId", pacienteId);
        Bson ordenamiento = Sorts.descending("fecha");
        return buscarDiagnosticos(filtro, ordenamiento);
    }
    
    /**
     * Buscar diagnósticos en un rango de fechas.
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de diagnósticos en el rango de fechas
     */
    public List<ModeloDiagnostico> buscarDiagnosticosPorFecha(Date fechaInicio, Date fechaFin) {
        Bson filtro = Filters.and(
                Filters.gte("fecha", fechaInicio),
                Filters.lte("fecha", fechaFin)
        );
        Bson ordenamiento = Sorts.descending("fecha");
        return buscarDiagnosticos(filtro, ordenamiento);
    }
    
    /**
     * Buscar diagnósticos con un filtro personalizado.
     * @param filtro Filtro de búsqueda
     * @param ordenamiento Criterio de ordenamiento
     * @return Lista de diagnósticos que coinciden con el filtro
     */
    public List<ModeloDiagnostico> buscarDiagnosticos(Bson filtro, Bson ordenamiento) {
        List<ModeloDiagnostico> diagnosticos = new ArrayList<>();
        FindIterable<Document> docs = diagnosticosCollection.find(filtro).sort(ordenamiento);
        
        try (MongoCursor<Document> cursor = docs.iterator()) {
            while (cursor.hasNext()) {
                diagnosticos.add(new ModeloDiagnostico(cursor.next()));
            }
        }
        
        return diagnosticos;
    }
    
    /**
     * Eliminar un diagnóstico por su ID.
     * @param id ID del diagnóstico a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminarDiagnostico(ObjectId id) {
        DeleteResult result = diagnosticosCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
    }
    
    /**
     * Eliminar todos los diagnósticos de un paciente.
     * @param pacienteId ID del paciente
     * @return Número de diagnósticos eliminados
     */
    public long eliminarDiagnosticosDePaciente(ObjectId pacienteId) {
        DeleteResult result = diagnosticosCollection.deleteMany(Filters.eq("pacienteId", pacienteId));
        return result.getDeletedCount();
    }
    
    // ********** MÉTODOS PARA CITAS **********
    
    /**
     * Guardar una cita en la base de datos.
     * @param cita La cita a guardar
     * @return El ID de la cita guardada
     */
    public ObjectId guardarCita(ModeloCita cita) {
        Document doc = cita.toDocument();
        
        if (cita.getId() == null) {
            // Nueva cita
            citasCollection.insertOne(doc);
            return doc.getObjectId("_id");
        } else {
            // Actualizar cita existente
            citasCollection.replaceOne(
                    Filters.eq("_id", cita.getId()),
                    doc
            );
            return cita.getId();
        }
    }
    
    /**
     * Obtener una cita por su ID.
     * @param id ID de la cita
     * @return La cita encontrada o null si no existe
     */
    public ModeloCita obtenerCitaPorId(ObjectId id) {
        Document doc = citasCollection.find(Filters.eq("_id", id)).first();
        return doc != null ? new ModeloCita(doc) : null;
    }
    
    /**
     * Buscar citas para un paciente específico.
     * @param pacienteId ID del paciente
     * @return Lista de citas del paciente
     */
    public List<ModeloCita> buscarCitasPorPaciente(ObjectId pacienteId) {
        Bson filtro = Filters.eq("pacienteId", pacienteId);
        Bson ordenamiento = Sorts.ascending("fechaHora");
        return buscarCitas(filtro, ordenamiento);
    }
    
    /**
     * Buscar citas para un veterinario específico.
     * @param veterinarioId ID del veterinario
     * @return Lista de citas del veterinario
     */
    public List<ModeloCita> buscarCitasPorVeterinario(ObjectId veterinarioId) {
        Bson filtro = Filters.eq("veterinarioId", veterinarioId);
        Bson ordenamiento = Sorts.ascending("fechaHora");
        return buscarCitas(filtro, ordenamiento);
    }
    
    /**
     * Buscar citas en un día específico.
     * @param fecha Fecha a buscar
     * @return Lista de citas para la fecha
     */
    public List<ModeloCita> buscarCitasPorDia(LocalDate fecha) {
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);
        
        Date inicio = Date.from(inicioDelDia.atZone(ZoneId.systemDefault()).toInstant());
        Date fin = Date.from(finDelDia.atZone(ZoneId.systemDefault()).toInstant());
        
        Bson filtro = Filters.and(
                Filters.gte("fechaHora", inicio),
                Filters.lte("fechaHora", fin)
        );
        Bson ordenamiento = Sorts.ascending("fechaHora");
        return buscarCitas(filtro, ordenamiento);
    }
    
    /**
     * Buscar citas en un rango de fechas.
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de citas en el rango de fechas
     */
    public List<ModeloCita> buscarCitasPorRangoFechas(LocalDate inicio, LocalDate fin) {
        LocalDateTime inicioDelDia = inicio.atStartOfDay();
        LocalDateTime finDelDia = fin.atTime(LocalTime.MAX);
        
        Date fechaInicio = Date.from(inicioDelDia.atZone(ZoneId.systemDefault()).toInstant());
        Date fechaFin = Date.from(finDelDia.atZone(ZoneId.systemDefault()).toInstant());
        
        Bson filtro = Filters.and(
                Filters.gte("fechaHora", fechaInicio),
                Filters.lte("fechaHora", fechaFin)
        );
        Bson ordenamiento = Sorts.ascending("fechaHora");
        return buscarCitas(filtro, ordenamiento);
    }
    
    /**
     * Buscar citas por estado.
     * @param estado Estado de la cita
     * @return Lista de citas con el estado especificado
     */
    public List<ModeloCita> buscarCitasPorEstado(EstadoCita estado) {
        Bson filtro = Filters.eq("estado", estado.name());
        Bson ordenamiento = Sorts.ascending("fechaHora");
        return buscarCitas(filtro, ordenamiento);
    }
    
    /**
     * Buscar citas con un filtro personalizado.
     * @param filtro Filtro de búsqueda
     * @param ordenamiento Criterio de ordenamiento
     * @return Lista de citas que coinciden con el filtro
     */
    public List<ModeloCita> buscarCitas(Bson filtro, Bson ordenamiento) {
        List<ModeloCita> citas = new ArrayList<>();
        FindIterable<Document> docs = citasCollection.find(filtro).sort(ordenamiento);
        
        try (MongoCursor<Document> cursor = docs.iterator()) {
            while (cursor.hasNext()) {
                citas.add(new ModeloCita(cursor.next()));
            }
        }
        
        return citas;
    }
    
    /**
     * Obtener todas las citas.
     * @return Lista completa de citas
     */
    public List<ModeloCita> obtenerTodasCitas() {
        Bson ordenamiento = Sorts.ascending("fechaHora");
        return buscarCitas(new Document(), ordenamiento);
    }
    
    /**
     * Cambiar el estado de una cita.
     * @param citaId ID de la cita
     * @param nuevoEstado Nuevo estado para la cita
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean cambiarEstadoCita(ObjectId citaId, EstadoCita nuevoEstado) {
        UpdateResult result = citasCollection.updateOne(
                Filters.eq("_id", citaId),
                Updates.set("estado", nuevoEstado.name())
        );
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Agregar observaciones a una cita.
     * @param citaId ID de la cita
     * @param observaciones Observaciones a agregar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean agregarObservacionesCita(ObjectId citaId, String observaciones) {
        UpdateResult result = citasCollection.updateOne(
                Filters.eq("_id", citaId),
                Updates.set("observaciones", observaciones)
        );
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Eliminar una cita por su ID.
     * @param id ID de la cita a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminarCita(ObjectId id) {
        DeleteResult result = citasCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
    }
    
    /**
     * Verificar si hay conflicto de horario con otras citas.
     * @param fechaHora Fecha y hora a verificar
     * @param duracionMinutos Duración de la cita en minutos
     * @param citaIdExcluir ID de cita a excluir (para actualizaciones)
     * @return true si hay conflicto, false si no lo hay
     */
    public boolean hayConflictoHorario(LocalDateTime fechaHora, int duracionMinutos, ObjectId citaIdExcluir) {
        LocalDateTime finCita = fechaHora.plusMinutes(duracionMinutos);
        
        Date inicio = Date.from(fechaHora.atZone(ZoneId.systemDefault()).toInstant());
        Date fin = Date.from(finCita.atZone(ZoneId.systemDefault()).toInstant());
        
        Bson filtro;
        if (citaIdExcluir != null) {
            filtro = Filters.and(
                    Filters.ne("_id", citaIdExcluir),
                    Filters.or(
                            Filters.and(Filters.lte("fechaHora", inicio), Filters.gt("fechaHoraFin", inicio)),
                            Filters.and(Filters.lt("fechaHora", fin), Filters.gte("fechaHoraFin", fin)),
                            Filters.and(Filters.gte("fechaHora", inicio), Filters.lte("fechaHoraFin", fin))
                    )
            );
        } else {
            filtro = Filters.or(
                    Filters.and(Filters.lte("fechaHora", inicio), Filters.gt("fechaHoraFin", inicio)),
                    Filters.and(Filters.lt("fechaHora", fin), Filters.gte("fechaHoraFin", fin)),
                    Filters.and(Filters.gte("fechaHora", inicio), Filters.lte("fechaHoraFin", fin))
            );
        }
        
        return citasCollection.countDocuments(filtro) > 0;
    }
    
    /**
     * Busca las razas disponibles para un tipo de animal
     * @param tipoAnimal Tipo de animal (perro, gato, etc.)
     * @return Array de strings con las razas disponibles
     * @throws Exception Si ocurre un error en la búsqueda
     */
    public String[] buscarRazasPorTipoAnimal(String tipoAnimal) throws Exception {
        // Razas de perros
        if (tipoAnimal.equalsIgnoreCase("Perro")) {
            return new String[] {
                "Labrador Retriever", "Pastor Alemán", "Bulldog", "Beagle", "Poodle",
                "Golden Retriever", "Boxer", "Dachshund", "Rottweiler", "Yorkshire Terrier",
                "Chihuahua", "Husky Siberiano", "Gran Danés", "Doberman", "Schnauzer",
                "Border Collie", "Cocker Spaniel", "Shih Tzu", "Mastín", "Galgo"
            };
        }
        // Razas de gatos
        else if (tipoAnimal.equalsIgnoreCase("Gato")) {
            return new String[] {
                "Persa", "Siamés", "Maine Coon", "Bengalí", "Ragdoll",
                "Sphynx", "British Shorthair", "Abisinio", "Scottish Fold", "Birmano",
                "Bombay", "Burmés", "Exótico", "Himalayo", "Munchkin",
                "Noruego del Bosque", "Oriental", "Savannah", "Siberiano", "Somalí"
            };
        }
        // Razas de aves
        else if (tipoAnimal.equalsIgnoreCase("Ave")) {
            return new String[] {
                "Canario", "Periquito", "Agapornis", "Cacatúa", "Guacamayo",
                "Loro Amazona", "Ninfas", "Diamante Mandarín", "Jilguero", "Tucán",
                "Pájaros Exóticos", "Cotorra", "Azulejo", "Cardenal", "Loro Gris Africano"
            };
        }
        // Razas de roedores
        else if (tipoAnimal.equalsIgnoreCase("Roedor")) {
            return new String[] {
                "Hámster Sirio", "Hámster Enano", "Rata", "Ratón", "Cobaya",
                "Chinchilla", "Jerbo", "Ardilla", "Degú", "Conejillo de Indias"
            };
        }
        // Razas de reptiles
        else if (tipoAnimal.equalsIgnoreCase("Reptil")) {
            return new String[] {
                "Tortuga", "Iguana", "Gecko", "Dragón Barbudo", "Camaleón",
                "Serpiente de Maíz", "Pitón Real", "Tortuga de Caja", "Lagarto Monitor", "Boa Constrictor"
            };
        }
        // Razas de peces
        else if (tipoAnimal.equalsIgnoreCase("Pez")) {
            return new String[] {
                "Pez Dorado", "Pez Betta", "Guppy", "Tetras", "Ángel",
                "Disco", "Koi", "Molly", "Pez Gato", "Tiburón Arcoiris"
            };
        }
        // Otros tipos de animales
        else {
            return new String[] {"No hay razas disponibles para este tipo de animal"};
        }
    }
} 