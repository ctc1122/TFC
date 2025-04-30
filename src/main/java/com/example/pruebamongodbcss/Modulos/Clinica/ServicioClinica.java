package com.example.pruebamongodbcss.Modulos.Clinica;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Utilidades.GestorConexion;

/**
 * Servicio para gestionar la interacción con la base de datos de la clínica veterinaria.
 */
public class ServicioClinica {
    private final MongoDatabase database;
    private final MongoCollection<Document> pacientesCollection;
    private final MongoCollection<Document> propietariosCollection;
    private final MongoCollection<Document> diagnosticosCollection;

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
        
        // Obtener referencia a las colecciones
        this.pacientesCollection = database.getCollection("pacientes");
        this.propietariosCollection = database.getCollection("propietarios");
        this.diagnosticosCollection = database.getCollection("diagnosticos");
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
        
        // Finalmente eliminar el paciente
        DeleteResult result = pacientesCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
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
        Bson filtro = Filters.or(
                Filters.regex("nombre", ".*" + nombre + ".*", "i"),
                Filters.regex("apellidos", ".*" + nombre + ".*", "i")
        );
        return buscarPropietarios(filtro);
    }
    
    /**
     * Buscar propietarios por DNI.
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
        FindIterable<Document> docs = propietariosCollection.find(filtro).sort(Sorts.ascending("apellidos", "nombre"));
        
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
        // Primero obtener las mascotas asociadas
        ModeloPropietario propietario = obtenerPropietarioPorId(id);
        if (propietario != null) {
            // Eliminar la referencia al propietario en las mascotas
            for (ObjectId mascotaId : propietario.getMascotas()) {
                pacientesCollection.updateOne(
                        Filters.eq("_id", mascotaId),
                        Updates.set("propietarioId", null)
                );
            }
        }
        
        // Eliminar el propietario
        DeleteResult result = propietariosCollection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
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
            
            // Actualizar la fecha de última visita del paciente
            pacientesCollection.updateOne(
                    Filters.eq("_id", diagnostico.getPacienteId()),
                    Updates.set("ultimaVisita", diagnostico.getFecha())
            );
            
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
     * Buscar diagnósticos de un paciente.
     * @param pacienteId ID del paciente
     * @return Lista de diagnósticos del paciente
     */
    public List<ModeloDiagnostico> buscarDiagnosticosPorPaciente(ObjectId pacienteId) {
        Bson filtro = Filters.eq("pacienteId", pacienteId);
        return buscarDiagnosticos(filtro, Sorts.descending("fecha"));
    }
    
    /**
     * Buscar diagnósticos por fecha.
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de diagnósticos en el rango de fechas
     */
    public List<ModeloDiagnostico> buscarDiagnosticosPorFecha(Date fechaInicio, Date fechaFin) {
        Bson filtro = Filters.and(
                Filters.gte("fecha", fechaInicio),
                Filters.lte("fecha", fechaFin)
        );
        return buscarDiagnosticos(filtro, Sorts.descending("fecha"));
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
} 