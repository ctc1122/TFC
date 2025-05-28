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

import com.example.pruebamongodbcss.calendar.CalendarEvent.EventoTipo;
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
            if (event.getId() == null || event.getId().isEmpty()) {
                event.setId(new ObjectId().toString());
                // Insertar el documento (solo para nuevos eventos)
                Document doc = calendarEventToDocument(event);
                appointmentsCollection.insertOne(doc);
                LOGGER.info("Cita nueva guardada correctamente: " + event.getId());
            } else {
                // Si ya tiene ID, actualizar en lugar de insertar
                String eventId = event.getId();
                Document doc = calendarEventToDocument(event);
                // Verificar si el ID tiene formato UUID (contiene guiones)
                if (event.getId().contains("-")) {
                    LOGGER.warning("Detectado ID con formato UUID: " + event.getId() + ". Generando nuevo ObjectId.");
                    // Generar un nuevo ID y asignarlo al evento
                    ObjectId nuevoId = new ObjectId();
                    event.setId(nuevoId.toString());
                    // Configurar el documento con el nuevo ID
                    doc.put("_id", nuevoId);
                    // Insertar como nuevo documento (no actualizar)
                    appointmentsCollection.insertOne(doc);
                    LOGGER.info("Convertido UUID a ObjectId y guardado como nuevo: " + event.getId());
                    return event;
                }
                // Verificar si el ID es un ObjectId válido
                ObjectId objectId;
                try {
                    objectId = new ObjectId(event.getId());
                    // Si es válido, actualizar usando el ID como filtro
                    UpdateResult result = appointmentsCollection.updateOne(
                        Filters.eq("_id", objectId),
                        new Document("$set", doc)
                    );
                    if (result.getModifiedCount() == 0 && result.getMatchedCount() == 0) {
                        LOGGER.warning("No se encontró la cita para actualizar. Intentando insertar.");
                        // Si no existe, intentar insertarla (caso poco común)
                        doc.put("_id", objectId);
                        appointmentsCollection.insertOne(doc);
                    }
                } catch (IllegalArgumentException e) {
                    // El ID no es un ObjectId válido, crear uno nuevo
                    LOGGER.warning("ID no válido como ObjectId: " + event.getId() + ". Generando uno nuevo.");
                    // Generar un nuevo ID y asignarlo al evento
                    ObjectId nuevoId = new ObjectId();
                    event.setId(nuevoId.toString());
                    // Configurar el documento con el nuevo ID
                    doc.put("_id", nuevoId);
                    // Insertar como nuevo documento
                    appointmentsCollection.insertOne(doc);
                }
                LOGGER.info("Cita existente actualizada correctamente: " + event.getId());
            }
            
            return event;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar cita", e);
            return null;
        }
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
     * Obtiene un evento de calendario por su ID
     * @param eventId ID del evento
     * @return El evento o null si no se encuentra
     */
    public CalendarEvent getEventById(String eventId) {
        try {
            if (eventId == null || eventId.isEmpty()) {
                return null;
            }
            
            // Primero intentar en la colección appointments
            Optional<CalendarEvent> appointmentOpt = getAppointmentById(eventId);
            if (appointmentOpt.isPresent()) {
                return appointmentOpt.get();
            }
            
            // Si no se encuentra, intentar en la colección citas
            try {
                String idToSearch = eventId.startsWith("_") ? eventId.substring(1) : eventId;
                Document doc = citasCollection.find(Filters.eq("_id", new ObjectId(idToSearch))).first();
                if (doc != null) {
                    return citaDocumentToCalendarEvent(doc);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error al buscar en colección de citas", e);
            }
            
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener evento por ID", e);
            return null;
        }
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
     * Obtiene todas las citas solo citas de un usuario
     * @param usuario nombre de usuario
     * @return Lista de citas
     */
    public List<CalendarEvent> getsoloCitasporUsuario(String usuario) {
        List<CalendarEvent> appointments = new ArrayList<>();
        try {
            // Verificar que el usuario no sea null
            if (usuario == null || usuario.isEmpty()) {
                LOGGER.warning("Usuario null o vacío, no se puede buscar");
                return appointments;
            }
            
            System.out.println("Buscando citas para usuario: '" + usuario + "'");
            
            // Buscar por el campo veterinario.usuario (estructura anidada)
            FindIterable<Document> citasUsuario2 = citasCollection.find(
                Filters.eq("veterinario.usuario", usuario)
            );
            
            for (Document doc : citasUsuario2) {
                CalendarEvent event = citaDocumentToCalendarEvent(doc);
                appointments.add(event);
                
                System.out.println("Cita encontrada: " + doc.getObjectId("_id") + 
                                 ", título: " + event.getTitle() + 
                                 ", veterinario: " + event.getUsuario());
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
        // Asignar ID puro de MongoDB
        event.setId(doc.getObjectId("_id").toString());
        
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
        
        // Campo eventType y tipoEvento (para eventos creados por el usuario)
        if (doc.containsKey("eventType")) {
            String eventType = doc.getString("eventType");
            event.setEventType(eventType);
            event.setTipoEvento(EventoTipo.fromString(eventType));
        }
        
        // Campo estado
        if (doc.containsKey("estado")) {
            event.setEstado(doc.getString("estado"));
        }
        
        // Campo usuario
        if (doc.containsKey("usuario")) {
            event.setUsuario(doc.getString("usuario"));
        }
        
        // Campo pacienteId
        if (doc.containsKey("pacienteId")) {
            event.setPacienteId(doc.getString("pacienteId"));
        }
        
        return event;
    }
    
    /**
     * Convierte un documento de cita a un objeto CalendarEvent
     * @param doc Documento de cita de la colección 'citas'
     * @return CalendarEvent
     */
    private CalendarEvent citaDocumentToCalendarEvent(Document doc) {
        try {
            CalendarEvent event = new CalendarEvent();
            // Asignar ID puro de MongoDB
            event.setId(doc.getObjectId("_id").toString());
            
            // Extraer datos del documento de la cita
            // Título: combinar el motivo y paciente si están disponibles
            String titulo = "Cita";
            
            if (doc.containsKey("motivo") && doc.getString("motivo") != null) {
                titulo = doc.getString("motivo");
            }
            
            // Intentar extraer información del paciente para el título
            Document pacienteDoc = (Document) doc.get("paciente");
            if (pacienteDoc != null) {
                String nombrePaciente = pacienteDoc.getString("nombre");
                String especiePaciente = pacienteDoc.getString("especie");
                
                if (nombrePaciente != null) {
                    titulo += " - " + nombrePaciente;
                    if (especiePaciente != null) {
                        titulo += " (" + especiePaciente + ")";
                    }
                }
            }
            
            event.setTitle(titulo);
            
            // Intentar obtener la fecha de la cita
            Date fechaCita = doc.getDate("fechaHora");
            if (fechaCita != null) {
                // Configurar inicio y fin con 1 hora de duración por defecto
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                
                LocalDateTime fechaHoraInicio = fechaCita.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
                
                event.setStart(fechaHoraInicio.format(formatter));
                event.setEnd(fechaHoraInicio.plusHours(1).format(formatter));
            } else {
                event.setStart(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                event.setEnd(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            // Información adicional
            if (doc.containsKey("observaciones")) {
                event.setDescription(doc.getString("observaciones"));
            }
            
            // Ubicación: consulta
            event.setLocation("Consulta");
            
            // Establecer explícitamente que es una cita médica
            event.setTipoEvento(EventoTipo.CITA_MEDICA);
            
            // Estado de la cita
            if (doc.containsKey("estado")) {
                String estadoStr = doc.getString("estado");
                event.setEstado(estadoStr);
                
                // Determinar el color y tipo según el estado
                switch (estadoStr.toUpperCase()) {
                    case "PENDIENTE":
                        event.setType("default");
                        event.setColor("#4285f4"); // Azul para pendientes
                        break;
                    case "EN_CURSO":
                        event.setType("urgent");
                        event.setColor("#fbbc04"); // Amarillo para en curso
                        break;
                    case "COMPLETADA":
                        event.setType("completed");
                        event.setColor("#34a853"); // Verde para completadas
                        break;
                    case "CANCELADA":
                        event.setType("cancelled");
                        event.setColor("#ea4335"); // Rojo para canceladas
                        break;
                    case "REPROGRAMADA":
                        event.setType("default");
                        event.setColor("#9c27b0"); // Morado para reprogramadas
                        break;
                    default:
                        event.setType("default");
                        event.setColor("#4285f4"); // Azul por defecto
                }
            } else {
                // Si no tiene estado, asignar pendiente por defecto
                event.setType("default");
                event.setEstado("PENDIENTE");
                event.setColor("#4285f4");
            }
            
            // Usuario asignado (veterinario)
            if (doc.containsKey("veterinario")) {
                Document vetDoc = (Document) doc.get("veterinario");
                if (vetDoc != null && vetDoc.containsKey("usuario")) {
                    event.setUsuario(vetDoc.getString("usuario"));
                }
            } else if (doc.containsKey("usuarioAsignado")) {
                event.setUsuario(doc.getString("usuarioAsignado"));
            }
            
            return event;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al convertir documento de cita a evento de calendario", e);
            return new CalendarEvent();
        }
    }
    
    /**
     * Convierte un objeto CalendarEvent a un documento MongoDB
     * @param event Evento a convertir
     * @return Document para MongoDB
     */
    private Document calendarEventToDocument(CalendarEvent event) {
        Document doc = new Document();
        
        // Si ya tiene un ID válido (con prefijo _), quitarlo para guardar
        if (event.getId() != null && event.getId().startsWith("_")) {
            // El ID empieza con _, quitar prefijo para la BD
            doc.append("_id", new ObjectId(event.getId().substring(1)));
        } else if (event.getId() != null && !event.getId().isEmpty() && !event.getId().startsWith("local_")) {
            try {
                doc.append("_id", new ObjectId(event.getId()));
            } catch (Exception e) {
                doc.append("_id", new ObjectId());
            }
        } else {
            // Si no tiene ID o es local, generar uno nuevo
            doc.append("_id", new ObjectId());
        }
        
        // Datos básicos
        doc.append("title", event.getTitle());
        
        // Fechas
        try {
            doc.append("start", parseDate(event.getStart()));
            doc.append("end", parseDate(event.getEnd()));
        } catch (Exception e) {
            LOGGER.warning("Error al parsear fechas: " + e.getMessage());
            doc.append("start", new Date());
            doc.append("end", new Date());
        }
        
        // Datos adicionales
        if (event.getLocation() != null) {
            doc.append("location", event.getLocation());
        }
        
        if (event.getDescription() != null) {
            doc.append("description", event.getDescription());
        }
        
        // Propiedades visuales
        if (event.getColor() != null) {
            doc.append("color", event.getColor());
        }
        
        if (event.getTextColor() != null) {
            doc.append("textColor", event.getTextColor());
        }
        
        doc.append("allDay", event.isAllDay());
        
        // Tipo de evento
        if (event.getType() != null) {
            doc.append("type", event.getType());
        } else {
            doc.append("type", "default");
        }
        
        // Tipo de evento: guardar tanto el string como el enum
        if (event.getEventType() != null) {
            doc.append("eventType", event.getEventType());
        }
        
        // Guardar también el tipo de evento usando el enumerado (si está disponible)
        if (event.getTipoEvento() != null) {
            doc.append("tipoEvento", event.getTipoEvento().name());
        }
        
        // Estado de la cita (para filtros avanzados)
        if (event.getEstado() != null) {
            doc.append("estado", event.getEstado());
        } else {
            // Estado por defecto según el tipo
            String tipo = event.getType();
            if (tipo != null) {
                switch (tipo) {
                    case "urgent":
                        doc.append("estado", "EN_CURSO");
                        break;
                    case "completed":
                        doc.append("estado", "COMPLETADA");
                        break;
                    case "cancelled":
                        doc.append("estado", "CANCELADA");
                        break;
                    default:
                        doc.append("estado", "PENDIENTE");
                }
            } else {
                doc.append("estado", "PENDIENTE");
            }
        }
        
        // Usuario
        if (event.getUsuario() != null) {
            doc.append("usuario", event.getUsuario());
        }
        
        // PacienteId
        if (event.getPacienteId() != null) {
            doc.append("pacienteId", event.getPacienteId());
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
    
    /**
     * Determina si un evento es una cita médica
     * @param event Evento a evaluar
     * @return true si es una cita médica
     */
    public boolean esCitaMedica(CalendarEvent event) {
        return event.getTipoEvento() == EventoTipo.CITA_MEDICA;
    }
    
    /**
     * Determina si un evento es una reunión
     * @param event Evento a evaluar
     * @return true si es una reunión
     */
    public boolean esReunion(CalendarEvent event) {
        return event.getTipoEvento() == EventoTipo.REUNION;
    }
    
    /**
     * Determina si un evento es un recordatorio
     * @param event Evento a evaluar
     * @return true si es un recordatorio
     */
    public boolean esRecordatorio(CalendarEvent event) {
        return event.getTipoEvento() == EventoTipo.RECORDATORIO;
    }
    
    /**
     * Verifica y actualiza automáticamente los estados de las citas según las reglas de negocio:
     * 1. Citas PENDIENTES que llegan a su hora de inicio -> EN_CURSO
     * 2. Citas EN_CURSO que han pasado su hora de fin sin gestionar -> ABSENTISMO
     * @return Número de citas actualizadas
     */
    public int verificarYActualizarEstadosAutomaticos() {
        int citasActualizadas = 0;
        try {
            LocalDateTime ahora = LocalDateTime.now();
            System.out.println("🔄 Verificando estados automáticos de citas...");
            System.out.println("⏰ Hora actual del servidor: " + ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            // Obtener todas las citas médicas de la colección de citas
            FindIterable<Document> citas = citasCollection.find(
                Filters.exists("fechaHora")
            );
            
            int citasRevisadas = 0;
            
            for (Document citaDoc : citas) {
                try {
                    citasRevisadas++;
                    String estadoActual = citaDoc.getString("estado");
                    Date fechaHoraCitaDate = citaDoc.getDate("fechaHora");
                    Integer duracionMinutos = citaDoc.getInteger("duracionMinutos", 30); // 30 min por defecto
                    ObjectId citaId = citaDoc.getObjectId("_id");
                    
                    if (estadoActual == null || fechaHoraCitaDate == null) {
                        System.out.println("⚠️ Cita " + citaId + " sin estado o fecha válida, saltando...");
                        continue;
                    }
                    
                    // Convertir Date a LocalDateTime
                    LocalDateTime fechaHoraCita = fechaHoraCitaDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                    
                    LocalDateTime fechaFinCita = fechaHoraCita.plusMinutes(duracionMinutos);
                    
                    // Log detallado para cada cita
                    System.out.println("📋 Revisando cita " + citaId + ":");
                    System.out.println("   📅 Fecha/hora cita: " + fechaHoraCita.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    System.out.println("   🏁 Fecha/hora fin: " + fechaFinCita.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    System.out.println("   📊 Estado actual: " + estadoActual);
                    System.out.println("   ⏱️ Duración: " + duracionMinutos + " minutos");
                    
                    // Calcular diferencias de tiempo
                    long minutosHastaInicio = java.time.Duration.between(ahora, fechaHoraCita).toMinutes();
                    long minutosHastaFin = java.time.Duration.between(ahora, fechaFinCita).toMinutes();
                    
                    System.out.println("   🕐 Minutos hasta inicio: " + minutosHastaInicio + " (negativo = ya pasó)");
                    System.out.println("   🕑 Minutos hasta fin: " + minutosHastaFin + " (negativo = ya pasó)");
                    
                    String nuevoEstado = null;
                    
                    // REGLA 1: Citas PENDIENTES que llegan a su hora de inicio -> EN_CURSO
                    if ("PENDIENTE".equals(estadoActual)) {
                        if (ahora.isAfter(fechaHoraCita) && ahora.isBefore(fechaFinCita.plusMinutes(15))) {
                            nuevoEstado = "EN_CURSO";
                            System.out.println("🕐 ✅ REGLA 1 APLICADA: Cita " + citaId + " puesta EN_CURSO automáticamente (hora de inicio alcanzada)");
                        } else if (ahora.isAfter(fechaFinCita.plusMinutes(15))) {
                            nuevoEstado = "ABSENTISMO";
                            System.out.println("📅 ✅ REGLA 1B APLICADA: Cita " + citaId + " marcada como ABSENTISMO (pasó más de 15 min después del fin)");
                        } else {
                            System.out.println("   ⏳ Cita PENDIENTE aún no ha llegado su hora");
                        }
                    }
                    // REGLA 2: Citas EN_CURSO que han pasado su hora de fin + 15 min -> ABSENTISMO
                    else if ("EN_CURSO".equals(estadoActual)) {
                        if (ahora.isAfter(fechaFinCita.plusMinutes(15))) {
                            nuevoEstado = "ABSENTISMO";
                            System.out.println("📅 ✅ REGLA 2 APLICADA: Cita " + citaId + " marcada como ABSENTISMO (pasó 15 min después del fin)");
                        } else {
                            System.out.println("   ⏳ Cita EN_CURSO aún dentro del tiempo permitido");
                        }
                    } else {
                        System.out.println("   ℹ️ Estado " + estadoActual + " no requiere cambio automático");
                    }
                    
                    // Actualizar estado si es necesario
                    if (nuevoEstado != null) {
                        System.out.println("🔄 Actualizando estado de " + estadoActual + " a " + nuevoEstado + "...");
                        
                        // Actualizar en la colección de citas
                        UpdateResult resultCita = citasCollection.updateOne(
                            Filters.eq("_id", citaId),
                            Updates.set("estado", nuevoEstado)
                        );
                        
                        if (resultCita.getModifiedCount() > 0) {
                            System.out.println("✅ Actualizado en colección de citas");
                            
                            // También actualizar en la colección de appointments si existe
                            try {
                                UpdateResult resultAppointment = appointmentsCollection.updateOne(
                                    Filters.eq("_id", citaId),
                                    Updates.combine(
                                        Updates.set("estado", nuevoEstado),
                                        Updates.set("type", getTypeFromEstado(nuevoEstado))
                                    )
                                );
                                
                                if (resultAppointment.getModifiedCount() > 0) {
                                    System.out.println("✅ También actualizado en colección de appointments");
                                } else {
                                    System.out.println("ℹ️ No se encontró en colección de appointments (normal para citas nuevas)");
                                }
                                
                                citasActualizadas++;
                                System.out.println("🎉 Estado actualizado exitosamente: " + citaId + " -> " + nuevoEstado);
                                
                            } catch (Exception appointmentException) {
                                System.out.println("⚠️ Cita actualizada en colección principal, pero error en appointments: " + appointmentException.getMessage());
                                citasActualizadas++; // Contar como exitosa de todos modos
                            }
                        } else {
                            System.err.println("❌ No se pudo actualizar la cita en la base de datos");
                        }
                    }
                    
                    System.out.println("   ─────────────────────────────────────");
                    
                } catch (Exception citaException) {
                    System.err.println("❌ Error al procesar cita individual: " + citaException.getMessage());
                    citaException.printStackTrace();
                    // Continuar con la siguiente cita
                }
            }
            
            System.out.println("✅ Verificación automática completada.");
            System.out.println("📊 Estadísticas:");
            System.out.println("   📋 Citas revisadas: " + citasRevisadas);
            System.out.println("   🔄 Citas actualizadas: " + citasActualizadas);
            
        } catch (Exception e) {
            System.err.println("❌ Error general en verificación automática de estados: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Error en verificación automática de estados", e);
        }
        
        return citasActualizadas;
    }
    
    /**
     * Convierte un estado a su tipo visual correspondiente
     * @param estado Estado de la cita
     * @return Tipo visual para el calendario
     */
    private String getTypeFromEstado(String estado) {
        switch (estado.toUpperCase()) {
            case "PENDIENTE":
                return "default";
            case "EN_CURSO":
                return "urgent";
            case "COMPLETADA":
            case "PENDIENTE_DE_FACTURAR":
                return "completed";
            case "CANCELADA":
            case "ABSENTISMO":
                return "cancelled";
            default:
                return "default";
        }
    }
    
    /**
     * Obtiene el número total de eventos de un usuario
     * @param usuario El nombre de usuario
     * @return Número total de eventos
     */
    public int getEventCountForUser(String usuario) {
        try {
            if (usuario == null || usuario.isEmpty()) {
                return 0;
            }
            
            long count = appointmentsCollection.countDocuments(
                Filters.eq("usuarioAsignado", usuario)
            );
            return (int) count;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar eventos del usuario", e);
            return 0;
        }
    }
    
    /**
     * Obtiene el número de eventos de un usuario por tipo
     * @param usuario El nombre de usuario
     * @param tipoEvento El tipo de evento (REUNION, RECORDATORIO, CITA_MEDICA)
     * @return Número de eventos del tipo especificado
     */
    public int getEventCountByType(String usuario, EventoTipo tipoEvento) {
        try {
            if (usuario == null || usuario.isEmpty() || tipoEvento == null) {
                return 0;
            }
            
            // Filtrar por usuario y tipo de evento
            long count = appointmentsCollection.countDocuments(
                Filters.and(
                    Filters.eq("usuarioAsignado", usuario),
                    Filters.eq("tipoEvento", tipoEvento.name())
                )
            );
            return (int) count;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar eventos por tipo", e);
            return 0;
        }
    }
    
    /**
     * Obtiene un resumen de eventos por tipo para un usuario
     * @param usuario El nombre de usuario
     * @return Un objeto con el conteo de cada tipo de evento
     */
    public EventSummary getEventSummaryForUser(String usuario) {
        EventSummary summary = new EventSummary();
        
        if (usuario == null || usuario.isEmpty()) {
            return summary;
        }
        
        try {
            // Contar reuniones
            summary.setMeetings(getEventCountByType(usuario, EventoTipo.REUNION));
            
            // Contar recordatorios
            summary.setReminders(getEventCountByType(usuario, EventoTipo.RECORDATORIO));
            
            // Contar citas médicas
            summary.setAppointments(getEventCountByType(usuario, EventoTipo.CITA_MEDICA));
            
            // Total
            summary.setTotal(summary.getMeetings() + summary.getReminders() + summary.getAppointments());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener resumen de eventos", e);
        }
        
        return summary;
    }
    
    /**
     * Clase que representa un resumen de eventos
     */
    public static class EventSummary {
        private int meetings = 0;      // Reuniones
        private int reminders = 0;     // Recordatorios
        private int appointments = 0;  // Citas médicas
        private int total = 0;         // Total de eventos
        
        public int getMeetings() {
            return meetings;
        }
        
        public void setMeetings(int meetings) {
            this.meetings = meetings;
        }
        
        public int getReminders() {
            return reminders;
        }
        
        public void setReminders(int reminders) {
            this.reminders = reminders;
        }
        
        public int getAppointments() {
            return appointments;
        }
        
        public void setAppointments(int appointments) {
            this.appointments = appointments;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        @Override
        public String toString() {
            return String.format("Eventos: %d total\n- %d reuniones\n- %d recordatorios\n- %d citas", 
                total, meetings, reminders, appointments);
        }
    }
    
    /**
     * Método público para probar manualmente la verificación automática
     * @return Número de citas actualizadas
     */
    public int probarVerificacionAutomatica() {
        System.out.println("🧪 PRUEBA MANUAL: Ejecutando verificación automática...");
        return verificarYActualizarEstadosAutomaticos();
    }
} 