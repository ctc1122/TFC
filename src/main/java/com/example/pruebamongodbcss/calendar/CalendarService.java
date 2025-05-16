package com.example.pruebamongodbcss.calendar;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import Utilidades.GestorConexion;

/**
 * Servicio para gestionar las citas en la base de datos
 */
public class CalendarService {
    
    private static final Logger LOGGER = Logger.getLogger(CalendarService.class.getName());
    private static final String COLLECTION_NAME = "appointments";
    private static final String CITAS_COLLECTION_NAME = "citas"; // Añadir colección de citas
    private static final String DATABASE_NAME = "Clinica"; // Usamos la base de datos Clinica
    
    private MongoDatabase database;
    private MongoCollection<Document> appointmentsCollection;
    private MongoCollection<Document> citasCollection; // Nueva colección
    
    /**
     * Constructor
     */
    public CalendarService() {
        try {
            // Usar el GestorConexion para conectar a la BD
            this.database = GestorConexion.conectarClinica();
            this.appointmentsCollection = database.getCollection(COLLECTION_NAME);
            this.citasCollection = database.getCollection(CITAS_COLLECTION_NAME); // Inicializar colección de citas
            LOGGER.info("Servicio de calendario inicializado correctamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar servicio de calendario", e);
        }
    }
    
    /**
     * Obtiene todas las citas
     * @return Lista de citas
     */
    public List<CalendarEvent> getAllAppointments() {
        List<CalendarEvent> appointments = new ArrayList<>();
        try {
            // Obtener citas de la colección 'appointments'
            FindIterable<Document> documents = appointmentsCollection.find();
            for (Document doc : documents) {
                appointments.add(documentToCalendarEvent(doc));
            }
            
            // NUEVO: Obtener citas de la colección 'citas'
            FindIterable<Document> citasDocuments = citasCollection.find();
            for (Document doc : citasDocuments) {
                CalendarEvent citaEvent = citaDocumentToCalendarEvent(doc);
                if (citaEvent != null) {
                    appointments.add(citaEvent);
                }
            }
            
            System.out.println("Total de citas encontradas (ambas colecciones): " + appointments.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener citas", e);
        }
        return appointments;
    }
    
    /**
     * Obtiene citas entre dos fechas
     * @param start Fecha inicio
     * @param end Fecha fin
     * @return Lista de citas
     */
    public List<CalendarEvent> getAppointmentsInRange(LocalDateTime start, LocalDateTime end) {
        List<CalendarEvent> appointments = new ArrayList<>();
        try {
            Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
            
            FindIterable<Document> documents = appointmentsCollection.find(
                Filters.and(
                    Filters.gte("start", startDate),
                    Filters.lte("end", endDate)
                )
            );
            
            for (Document doc : documents) {
                appointments.add(documentToCalendarEvent(doc));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener citas por rango", e);
        }
        return appointments;
    }
    
    /**
     * Guarda una cita
     * @param event Cita a guardar
     * @return La cita guardada
     */
    public CalendarEvent saveAppointment(CalendarEvent event) {
        try {
            // Si no tiene ID, crear uno nuevo
            if (event.getId() == null || event.getId().isEmpty() || !event.getId().startsWith("_")) {
                event.setId("_" + new ObjectId().toString());
            }
            
            Document doc = calendarEventToDocument(event);
            
            // Insertar el documento
            appointmentsCollection.insertOne(doc);
            LOGGER.info("Cita guardada correctamente: " + event.getId());
            return event;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar cita", e);
        }
        return null;
    }
    
    /**
     * Actualiza una cita existente
     * @param event Cita a actualizar
     * @return true si se actualizó correctamente
     */
    public boolean updateAppointment(CalendarEvent event) {
        try {
            if (event.getId() == null || event.getId().isEmpty()) {
                LOGGER.warning("No se puede actualizar cita sin ID");
                return false;
            }
            
            Document updates = new Document();
            updates.put("title", event.getTitle());
            updates.put("start", parseDate(event.getStart()));
            updates.put("end", parseDate(event.getEnd()));
            updates.put("location", event.getLocation());
            updates.put("description", event.getDescription());
            
            UpdateResult result = appointmentsCollection.updateOne(
                Filters.eq("_id", event.getId().startsWith("_") ? 
                    new ObjectId(event.getId().substring(1)) : new ObjectId(event.getId())),
                new Document("$set", updates)
            );
            
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar cita", e);
            return false;
        }
    }
    
    /**
     * Elimina una cita
     * @param eventId ID de la cita a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteAppointment(String eventId) {
        try {
            if (eventId == null || eventId.isEmpty()) {
                LOGGER.warning("No se puede eliminar cita sin ID");
                return false;
            }
            
            DeleteResult result = appointmentsCollection.deleteOne(
                Filters.eq("_id", eventId.startsWith("_") ? 
                    new ObjectId(eventId.substring(1)) : new ObjectId(eventId))
            );
            
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar cita", e);
            return false;
        }
    }
    
    /**
     * Obtiene una cita por su ID
     * @param eventId ID de la cita
     * @return La cita encontrada o null
     */
    public Optional<CalendarEvent> getAppointmentById(String eventId) {
        try {
            Document doc = appointmentsCollection.find(
                Filters.eq("_id", eventId.startsWith("_") ? 
                    new ObjectId(eventId.substring(1)) : new ObjectId(eventId))
            ).first();
            
            if (doc != null) {
                return Optional.of(documentToCalendarEvent(doc));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener cita por ID", e);
        }
        return Optional.empty();
    }
    
    /**
     * Busca citas por texto en título o descripción
     * @param searchText Texto a buscar
     * @return Lista de citas que coinciden
     */
    public List<CalendarEvent> searchAppointments(String searchText) {
        List<CalendarEvent> appointments = new ArrayList<>();
        try {
            FindIterable<Document> documents = appointmentsCollection.find(
                Filters.or(
                    Filters.regex("title", searchText, "i"),
                    Filters.regex("description", searchText, "i")
                )
            );
            
            for (Document doc : documents) {
                appointments.add(documentToCalendarEvent(doc));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar citas", e);
        }
        return appointments;
    }
    
    /**
     * Obtiene todas las citas de un usuario
     * @param usuario nombre de usuario
     * @return Lista de citas
     */
    public List<CalendarEvent> getAppointmentsByUser(String usuario) {
        List<CalendarEvent> appointments = new ArrayList<>();
        try {
            // Verificar que el usuario no sea null
            if (usuario == null || usuario.isEmpty()) {
                LOGGER.warning("Usuario null o vacío, no se puede buscar");
                return appointments;
            }
            
            System.out.println("Buscando citas para usuario: '" + usuario + "'");
            
            // Buscar solo por usuarioAsignado - simplificado
            FindIterable<Document> citasUsuario = appointmentsCollection.find(
                Filters.eq("usuarioAsignado", usuario)
            );
            FindIterable<Document> citasUsuario2 = citasCollection.find(
                Filters.eq("usuarioAsignado", usuario)
            );
            
            // Convertir documentos a eventos de calendario
            for (Document doc : citasUsuario) {
                appointments.add(documentToCalendarEvent(doc));
                
                System.out.println("Cita encontrada: " + doc.getObjectId("_id") + 
                                 ", título: " + doc.getString("title"));
            }

            for (Document doc : citasUsuario2) {
                appointments.add(citaDocumentToCalendarEvent(doc));
                
                System.out.println("Cita encontrada: " + doc.getObjectId("_id") + 
                                 ", título: " + doc.getString("title"));
            }
            
            LOGGER.info("Se encontraron " + appointments.size() + " citas para el usuario: " + usuario);
            return appointments;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener citas por usuario", e);
            return appointments;
        }
    }
    
    /**
     * Convierte un documento MongoDB a un objeto CalendarEvent
     * @param doc Documento a convertir
     * @return CalendarEvent
     */
    private CalendarEvent documentToCalendarEvent(Document doc) {
        CalendarEvent event = new CalendarEvent();
        
        // Asignar ID (prefijo _ para diferenciar de los IDs generados por el frontend)
        event.setId("_" + doc.getObjectId("_id").toString());
        
        // Datos básicos
        event.setTitle(doc.getString("title"));
        
        // Fechas - convertir de Date a String ISO
        Date startDate = doc.getDate("start");
        Date endDate = doc.getDate("end");
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        event.setStart(startDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime().format(formatter));
        
        event.setEnd(endDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime().format(formatter));
        
        // Datos adicionales
        event.setLocation(doc.getString("location"));
        event.setDescription(doc.getString("description"));
        
        // Propiedades visuales
        if (doc.containsKey("color")) {
            event.setColor(doc.getString("color"));
        }
        
        if (doc.containsKey("textColor")) {
            event.setTextColor(doc.getString("textColor"));
        }
        
        if (doc.containsKey("allDay")) {
            event.setAllDay(doc.getBoolean("allDay"));
        }
        
        // Tipo de evento
        if (doc.containsKey("estado")) {
            event.setType(doc.getString("estado").toLowerCase()); // pendiente, cancelada, etc.
        } else if (doc.containsKey("type")) {
            event.setType(doc.getString("type"));
        } else {
            // Determinar tipo por el título para compatibilidad
            String title = doc.getString("title").toLowerCase();
            if (title.contains("urgente") || title.contains("urgencia")) {
                event.setType("urgent");
            } else if (title.contains("completada") || title.contains("realizada")) {
                event.setType("completed");
            } else if (title.contains("cancelada")) {
                event.setType("cancelled");
            } else {
                event.setType("default");
            }
        }
        
        // Usuario - primero buscar usuarioAsignado, luego los campos antiguos por compatibilidad
        if (doc.containsKey("usuarioAsignado")) {
            event.setUsuario(doc.getString("usuarioAsignado"));
        } else if (doc.containsKey("usuarioId")) {
            event.setUsuario(doc.getString("usuarioId"));
        } else if (doc.containsKey("usuario")) {
            event.setUsuario(doc.getString("usuario"));
        }
        
        return event;
    }
    
    /**
     * Convierte un documento de cita a un objeto CalendarEvent
     * @param doc Documento de cita a convertir
     * @return CalendarEvent o null si no se pudo convertir
     */
    private CalendarEvent citaDocumentToCalendarEvent(Document doc) {
        try {
            CalendarEvent event = new CalendarEvent();
            
            // Asignar ID
            String idCita = doc.getString("id");
            if (idCita == null && doc.containsKey("_id")) {
                // Si no tiene id pero tiene _id, usar ese
                Object id = doc.get("_id");
                if (id instanceof ObjectId) {
                    idCita = "_" + ((ObjectId)id).toString();
                } else {
                    idCita = "_" + id.toString();
                }
            }
            
            event.setId(idCita != null ? idCita : "_cita_" + System.currentTimeMillis());
            
            // Datos del paciente y títulos
            String nombrePaciente = doc.getString("nombrePaciente");
            String tipoAnimal = doc.getString("tipoAnimal");
            String nombreVeterinario = doc.getString("nombreVeterinario");
            
            // Crear un título descriptivo
            StringBuilder title = new StringBuilder();
            if (nombrePaciente != null && !nombrePaciente.isEmpty()) {
                title.append(nombrePaciente);
                if (tipoAnimal != null && !tipoAnimal.isEmpty()) {
                    title.append(" (").append(tipoAnimal).append(")");
                }
            } else {
                title.append("Cita");
            }
            
            if (doc.containsKey("motivo") && doc.getString("motivo") != null) {
                title.append(" - ").append(doc.getString("motivo"));
            }
            
            event.setTitle(title.toString());
            
            // Fechas
            // Verificar el formato de fecha en la colección citas
            if (doc.containsKey("fechaHora")) {
                Object fechaHora = doc.get("fechaHora");
                
                if (fechaHora instanceof Date) {
                    // Si es un objeto Date
                    Date startDate = (Date) fechaHora;
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime dateTime = startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                    
                    // Asignar fecha de inicio
                    event.setStart(dateTime.format(formatter));
                    
                    // Calcular fecha fin (30 minutos después)
                    LocalDateTime endDateTime = dateTime.plusMinutes(30);
                    event.setEnd(endDateTime.format(formatter));
                    
                    System.out.println("📅 Cita con fecha: " + dateTime + " a " + endDateTime);
                } else if (fechaHora instanceof String) {
                    // Si es un string, intentar parsear
                    String fechaStr = (String) fechaHora;
                    
                    // Varios formatos posibles
                    LocalDateTime dateTime = null;
                    try {
                        // Intentar como ISO
                        dateTime = LocalDateTime.parse(fechaStr);
                    } catch (Exception e1) {
                        try {
                            // Intentar formato dd/MM/yyyy HH:mm
                            dateTime = LocalDateTime.parse(fechaStr, 
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                        } catch (Exception e2) {
                            // Como último recurso, usar la fecha actual
                            dateTime = LocalDateTime.now();
                            System.err.println("No se pudo parsear fecha: " + fechaStr);
                        }
                    }
                    
                    // Asignar fechas
                    event.setStart(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    event.setEnd(dateTime.plusMinutes(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            } else {
                // Si no hay fecha, usar valores por defecto
                LocalDateTime now = LocalDateTime.now();
                event.setStart(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                event.setEnd(now.plusMinutes(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            // Datos adicionales y descripción
            StringBuilder description = new StringBuilder();
            
            if (nombreVeterinario != null && !nombreVeterinario.isEmpty()) {
                description.append("Veterinario: ").append(nombreVeterinario).append("\n");
                
                // Buscar el usuario asociado
                if (nombreVeterinario.contains("Juan V")) {
                    event.setUsuario("jvazquez");
                }
            }
            
            if (doc.containsKey("observaciones") && doc.getString("observaciones") != null) {
                description.append("Observaciones: ").append(doc.getString("observaciones"));
            }
            
            event.setDescription(description.toString());
            
            // Estado y colores
            String estado = doc.containsKey("estado") ? doc.getString("estado") : "PENDIENTE";
            if (estado != null) {
                switch (estado) {
                    case "PENDIENTE":
                        event.setType("default");
                        event.setColor("#1a73e8");
                        break;
                    case "COMPLETADA":
                        event.setType("completed");
                        event.setColor("#4caf50");
                        break;
                    case "CANCELADA":
                        event.setType("cancelled");
                        event.setColor("#f44336");
                        break;
                    case "EN_CURSO":
                        event.setType("urgent");
                        event.setColor("#ff9800");
                        break;
                    default:
                        event.setType("default");
                        event.setColor("#1a73e8");
                }
            }
            
            // Asignar el usuario (veterinario)
            if (event.getUsuario() == null) {
                event.setUsuario("jvazquez"); // Por defecto, asignar a jvazquez
            }
            
            return event;
        } catch (Exception e) {
            System.err.println("Error al convertir documento de cita: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Convierte un objeto CalendarEvent a un documento MongoDB
     * @param event CalendarEvent a convertir
     * @return Document
     */
    private Document calendarEventToDocument(CalendarEvent event) {
        Document doc = new Document();
        
        // Si tiene un ID con el prefijo _, quitarlo para obtener el ObjectId
        if (event.getId() != null && event.getId().startsWith("_")) {
            try {
                doc.put("_id", new ObjectId(event.getId().substring(1)));
            } catch (IllegalArgumentException e) {
                // Si no es un ObjectId válido, usar el ID tal cual
                doc.put("_id", event.getId().substring(1));
            }
        } else if (event.getId() != null) {
            // Si tiene un ID pero sin prefijo, intentar usarlo como ObjectId
            try {
                doc.put("_id", new ObjectId(event.getId()));
            } catch (IllegalArgumentException e) {
                // Si no es un ObjectId válido, generar uno nuevo
                doc.put("_id", new ObjectId());
            }
        } else {
            // Si no tiene ID, generar uno nuevo
            doc.put("_id", new ObjectId());
        }
        
        // Datos básicos
        doc.put("title", event.getTitle());
        
        // Fechas
        doc.put("start", parseDate(event.getStart()));
        doc.put("end", parseDate(event.getEnd()));
        
        // Datos adicionales
        if (event.getLocation() != null) {
            doc.put("location", event.getLocation());
        }
        
        if (event.getDescription() != null) {
            doc.put("description", event.getDescription());
        }
        
        // Propiedades visuales
        if (event.getColor() != null) {
            doc.put("color", event.getColor());
        }
        
        if (event.getTextColor() != null) {
            doc.put("textColor", event.getTextColor());
        }
        
        doc.put("allDay", event.isAllDay());
        
        // Tipo de evento
        if (event.getType() != null) {
            doc.put("type", event.getType());
        } else {
            // Determinar tipo por el título
            String title = event.getTitle().toLowerCase();
            if (title.contains("urgente") || title.contains("urgencia")) {
                doc.put("type", "urgent");
            } else if (title.contains("completada") || title.contains("realizada")) {
                doc.put("type", "completed");
            } else if (title.contains("cancelada")) {
                doc.put("type", "cancelled");
            } else {
                doc.put("type", "default");
            }
        }
        
        // Usuario (usar usuarioAsignado como campo principal)
        if (event.getUsuario() != null && !event.getUsuario().isEmpty()) {
            doc.put("usuarioAsignado", event.getUsuario());
            
            // Para compatibilidad con el código antiguo, mantener los campos antiguos
            doc.put("usuario", event.getUsuario());
            doc.put("usuarioId", event.getUsuario());
        } else {
            // Usar valor predeterminado
            doc.put("usuarioAsignado", "jvazquez");
            doc.put("usuario", "jvazquez");
            doc.put("usuarioId", "jvazquez");
        }
        
        return doc;
    }
    
    /**
     * Convierte una fecha en formato ISO a Date
     * @param dateStr Fecha en formato ISO
     * @return Date
     */
    private Date parseDate(String dateStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al convertir fecha: " + dateStr, e);
            return new Date();
        }
    }
    
    /**
     * Actualiza todas las citas para asegurar que tienen el campo usuarioAsignado
     * @return Número de citas actualizadas
     */
    public int actualizarCitasParaAgregarUsuario() {
        int contador = 0;
        try {
            // Buscar todas las citas sin usuarioAsignado
            FindIterable<Document> citasSinUsuario = appointmentsCollection.find(
                Filters.exists("usuarioAsignado", false)
            );
            
            LOGGER.info("Iniciando migración de campos de usuario...");
            for (Document citaDoc : citasSinUsuario) {
                String nombreUsuario = null;
                
                // Intentar obtener el nombreVeterinario para buscar el usuario
                if (citaDoc.containsKey("nombreVeterinario")) {
                    String nombreVeterinario = citaDoc.getString("nombreVeterinario");
                    LOGGER.info("Procesando cita con nombreVeterinario: " + nombreVeterinario);
                    
                    // Si es Juan Vazquez o Juan Vázquez, asignar jvazquez
                    if (nombreVeterinario.contains("Juan V")) {
                        nombreUsuario = "jvazquez";
                        LOGGER.info("Asignado usuario: jvazquez");
                    }
                }
                
                // Si encontramos, actualizar la cita con el usuarioAsignado
                if (nombreUsuario != null) {
                    contador++;
                    ObjectId citaId = citaDoc.getObjectId("_id");
                    LOGGER.info("Actualizando cita " + citaId + " con usuarioAsignado: " + nombreUsuario);
                    
                    // Actualizar la cita con el usuarioAsignado y antiguos campos para compatibilidad
                    UpdateResult result = appointmentsCollection.updateOne(
                        Filters.eq("_id", citaId),
                        Updates.combine(
                            Updates.set("usuarioAsignado", nombreUsuario),
                            Updates.set("usuario", nombreUsuario),
                            Updates.set("usuarioId", nombreUsuario)
                        )
                    );
                    
                    LOGGER.info("Resultado de actualización: " + result.getModifiedCount() + " documentos");
                }
            }
            
            // Si no se actualizó ninguna cita, podemos forzar jvazquez en todas
            if (contador == 0) {
                LOGGER.info("No se encontraron citas para actualizar por nombre. Forzando valor predeterminado...");
                UpdateResult result = appointmentsCollection.updateMany(
                    Filters.exists("usuarioAsignado", false),
                    Updates.combine(
                        Updates.set("usuarioAsignado", "jvazquez"),
                        Updates.set("usuario", "jvazquez"),
                        Updates.set("usuarioId", "jvazquez")
                    )
                );
                contador = (int) result.getModifiedCount();
                LOGGER.info("Se actualizaron " + contador + " citas con valor predeterminado");
            }
            
            LOGGER.info("Se actualizaron " + contador + " citas para agregar el campo usuarioAsignado");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar citas para agregar usuario", e);
        }
        return contador;
    }
} 