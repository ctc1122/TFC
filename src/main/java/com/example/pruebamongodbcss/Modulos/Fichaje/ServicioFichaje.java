package com.example.pruebamongodbcss.Modulos.Fichaje;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import Utilidades1.GestorConexion;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Sorts.descending;

public class ServicioFichaje {
    private MongoCollection<Document> coleccionFichajes;
    private ScheduledExecutorService scheduler;
    
    public ServicioFichaje() {
        try {
            MongoDatabase database = GestorConexion.conectarEmpresa();
            this.coleccionFichajes = database.getCollection("fichajes");
            
            // Iniciar el scheduler para verificar fichajes automáticos
            iniciarSchedulerIncidenciasAutomaticas();
            
        } catch (Exception e) {
            System.err.println("Error al inicializar ServicioFichaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inicia el scheduler que verifica cada hora si hay fichajes que deben cerrarse automáticamente
     */
    private void iniciarSchedulerIncidenciasAutomaticas() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::verificarYCerrarFichajesAutomaticos, 1, 1, TimeUnit.HOURS);
    }
    
    /**
     * Verifica y cierra automáticamente fichajes que han superado las 12 horas
     */
    public void verificarYCerrarFichajesAutomaticos() {
        try {
            List<Document> fichajesAbiertos = coleccionFichajes.find(
                eq("estado", ModeloFichaje.EstadoFichaje.ABIERTO.name())
            ).into(new ArrayList<>());
            
            for (Document doc : fichajesAbiertos) {
                ModeloFichaje fichaje = new ModeloFichaje(doc);
                if (fichaje.puedeSerCerradoAutomaticamente()) {
                    fichaje.marcarComoIncidenciaAutomatica();
                    coleccionFichajes.replaceOne(eq("_id", fichaje.getId()), fichaje.toDocument());
                    System.out.println("Fichaje cerrado automáticamente por incidencia: " + fichaje.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al verificar fichajes automáticos: " + e.getMessage());
        }
    }
    
    /**
     * Registra una entrada de fichaje
     */
    public ModeloFichaje ficharEntrada(ObjectId empleadoId, String nombreEmpleado, String usuarioEmpleado, 
                                      ModeloFichaje.TipoFichaje tipo, String motivoIncidencia) {
        try {
            // Verificar si ya hay un fichaje abierto para hoy
            ModeloFichaje fichajeExistente = obtenerFichajeAbiertoHoy(empleadoId);
            if (fichajeExistente != null) {
                throw new IllegalStateException("Ya existe un fichaje abierto para hoy");
            }
            
            ModeloFichaje nuevoFichaje = new ModeloFichaje(empleadoId, nombreEmpleado, usuarioEmpleado);
            nuevoFichaje.ficharEntrada(LocalDateTime.now(), tipo, motivoIncidencia);
            
            Document doc = nuevoFichaje.toDocument();
            coleccionFichajes.insertOne(doc);
            nuevoFichaje.setId(doc.getObjectId("_id"));
            
            return nuevoFichaje;
        } catch (Exception e) {
            System.err.println("Error al fichar entrada: " + e.getMessage());
            throw new RuntimeException("Error al fichar entrada: " + e.getMessage());
        }
    }
    
    /**
     * Registra una salida de fichaje
     */
    public boolean ficharSalida(ObjectId empleadoId, ModeloFichaje.TipoFichaje tipo, String motivoIncidencia) {
        try {
            ModeloFichaje fichajeAbierto = obtenerFichajeAbiertoHoy(empleadoId);
            if (fichajeAbierto == null) {
                throw new IllegalStateException("No hay fichaje de entrada para hoy");
            }
            
            fichajeAbierto.ficharSalida(LocalDateTime.now(), tipo, motivoIncidencia);
            
            return coleccionFichajes.replaceOne(
                eq("_id", fichajeAbierto.getId()), 
                fichajeAbierto.toDocument()
            ).getModifiedCount() > 0;
            
        } catch (Exception e) {
            System.err.println("Error al fichar salida: " + e.getMessage());
            throw new RuntimeException("Error al fichar salida: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el fichaje abierto de hoy para un empleado
     */
    public ModeloFichaje obtenerFichajeAbiertoHoy(ObjectId empleadoId) {
        try {
            String fechaHoy = LocalDate.now().toString();
            Document doc = coleccionFichajes.find(and(
                eq("empleadoId", empleadoId),
                eq("fecha", fechaHoy),
                eq("estado", ModeloFichaje.EstadoFichaje.ABIERTO.name())
            )).first();
            
            return doc != null ? new ModeloFichaje(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener fichaje abierto: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene el historial de fichajes de un empleado
     */
    public List<ModeloFichaje> obtenerHistorialEmpleado(ObjectId empleadoId, int limite) {
        try {
            List<Document> docs = coleccionFichajes.find(eq("empleadoId", empleadoId))
                .sort(descending("fecha"))
                .limit(limite)
                .into(new ArrayList<>());
            
            List<ModeloFichaje> fichajes = new ArrayList<>();
            for (Document doc : docs) {
                fichajes.add(new ModeloFichaje(doc));
            }
            return fichajes;
        } catch (Exception e) {
            System.err.println("Error al obtener historial: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene todos los fichajes (solo para administradores)
     */
    public List<ModeloFichaje> obtenerTodosFichajes(int limite) {
        try {
            List<Document> docs = coleccionFichajes.find()
                .sort(descending("fecha"))
                .limit(limite)
                .into(new ArrayList<>());
            
            List<ModeloFichaje> fichajes = new ArrayList<>();
            for (Document doc : docs) {
                fichajes.add(new ModeloFichaje(doc));
            }
            return fichajes;
        } catch (Exception e) {
            System.err.println("Error al obtener todos los fichajes: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene fichajes por rango de fechas
     */
    public List<ModeloFichaje> obtenerFichajesPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<Document> docs = coleccionFichajes.find(and(
                gte("fecha", fechaInicio.toString()),
                lte("fecha", fechaFin.toString())
            )).sort(descending("fecha")).into(new ArrayList<>());
            
            List<ModeloFichaje> fichajes = new ArrayList<>();
            for (Document doc : docs) {
                fichajes.add(new ModeloFichaje(doc));
            }
            return fichajes;
        } catch (Exception e) {
            System.err.println("Error al obtener fichajes por fecha: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene fichajes de un empleado por rango de fechas
     */
    public List<ModeloFichaje> obtenerFichajesEmpleadoPorFecha(ObjectId empleadoId, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<Document> docs = coleccionFichajes.find(and(
                eq("empleadoId", empleadoId),
                gte("fecha", fechaInicio.toString()),
                lte("fecha", fechaFin.toString())
            )).sort(descending("fecha")).into(new ArrayList<>());
            
            List<ModeloFichaje> fichajes = new ArrayList<>();
            for (Document doc : docs) {
                fichajes.add(new ModeloFichaje(doc));
            }
            return fichajes;
        } catch (Exception e) {
            System.err.println("Error al obtener fichajes del empleado por fecha: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene fichajes por empleado (nombre de usuario)
     */
    public List<ModeloFichaje> obtenerFichajesPorUsuario(String usuarioEmpleado, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<Document> docs = coleccionFichajes.find(and(
                eq("usuarioEmpleado", usuarioEmpleado),
                gte("fecha", fechaInicio.toString()),
                lte("fecha", fechaFin.toString())
            )).sort(descending("fecha")).into(new ArrayList<>());
            
            List<ModeloFichaje> fichajes = new ArrayList<>();
            for (Document doc : docs) {
                fichajes.add(new ModeloFichaje(doc));
            }
            return fichajes;
        } catch (Exception e) {
            System.err.println("Error al obtener fichajes por usuario: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Genera un resumen de fichajes para un empleado en un período
     */
    public ResumenFichaje generarResumenEmpleado(String usuarioEmpleado, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<ModeloFichaje> fichajes = obtenerFichajesPorUsuario(usuarioEmpleado, fechaInicio, fechaFin);
            ResumenFichaje resumen = new ResumenFichaje(usuarioEmpleado, fechaInicio, fechaFin);
            resumen.calcularEstadisticas(fichajes);
            return resumen;
        } catch (Exception e) {
            System.err.println("Error al generar resumen: " + e.getMessage());
            return new ResumenFichaje(usuarioEmpleado, fechaInicio, fechaFin);
        }
    }
    
    /**
     * Obtiene fichajes de un día específico
     */
    public List<ModeloFichaje> obtenerFichajesPorDia(LocalDate fecha) {
        try {
            List<Document> docs = coleccionFichajes.find(eq("fecha", fecha.toString()))
                .sort(descending("fechaHoraEntrada"))
                .into(new ArrayList<>());
            
            List<ModeloFichaje> fichajes = new ArrayList<>();
            for (Document doc : docs) {
                fichajes.add(new ModeloFichaje(doc));
            }
            return fichajes;
        } catch (Exception e) {
            System.err.println("Error al obtener fichajes del día: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Elimina un fichaje (solo para administradores)
     */
    public boolean eliminarFichaje(ObjectId fichajeId) {
        try {
            return coleccionFichajes.deleteOne(eq("_id", fichajeId)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error al eliminar fichaje: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza un fichaje (solo para administradores)
     */
    public boolean actualizarFichaje(ModeloFichaje fichaje) {
        try {
            return coleccionFichajes.replaceOne(
                eq("_id", fichaje.getId()), 
                fichaje.toDocument()
            ).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar fichaje: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene estadísticas generales de fichajes
     */
    public Document obtenerEstadisticasGenerales(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<ModeloFichaje> fichajes = obtenerFichajesPorFecha(fechaInicio, fechaFin);
            
            long totalMinutos = 0;
            int fichajesCompletos = 0;
            int incidencias = 0;
            int fichajesIncompletos = 0;
            
            for (ModeloFichaje fichaje : fichajes) {
                if (fichaje.getEstado() == ModeloFichaje.EstadoFichaje.CERRADO && !fichaje.isEsIncidencia()) {
                    totalMinutos += fichaje.getMinutosTrabajoTotal();
                    fichajesCompletos++;
                }
                if (fichaje.isEsIncidencia()) {
                    incidencias++;
                }
                if (fichaje.getEstado() == ModeloFichaje.EstadoFichaje.ABIERTO || 
                    fichaje.getEstado() == ModeloFichaje.EstadoFichaje.INCOMPLETO) {
                    fichajesIncompletos++;
                }
            }
            
            return new Document()
                .append("totalFichajes", fichajes.size())
                .append("fichajesCompletos", fichajesCompletos)
                .append("incidencias", incidencias)
                .append("fichajesIncompletos", fichajesIncompletos)
                .append("totalMinutosTrabajados", totalMinutos)
                .append("promedioHorasDiarias", fichajesCompletos > 0 ? (double) totalMinutos / fichajesCompletos / 60.0 : 0.0);
                
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            return new Document();
        }
    }
    
    /**
     * Cierra el scheduler al finalizar
     */
    public void cerrarServicio() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
} 