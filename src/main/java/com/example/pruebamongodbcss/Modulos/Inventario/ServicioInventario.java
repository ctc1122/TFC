package com.example.pruebamongodbcss.Modulos.Inventario;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.example.pruebamongodbcss.Utilidades.GestorConexionInventario;
import com.example.pruebamongodbcss.Utilidades.ProtocoloInventario;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio para la comunicación con el servidor de inventario
 * Maneja todas las operaciones relacionadas con medicamentos
 */
public class ServicioInventario {
    
    private static ServicioInventario instancia;
    private final GestorConexionInventario gestorConexion;
    private final Gson gson;
    private ObservableList<ModeloMedicamentoInventario> medicamentosCache;
    private boolean cacheValido;
    
    private ServicioInventario() {
        this.gestorConexion = GestorConexionInventario.getInstance();
        this.gson = new Gson();
        this.medicamentosCache = FXCollections.observableArrayList();
        this.cacheValido = false;
    }
    
    /**
     * Obtiene la instancia singleton del servicio
     */
    public static synchronized ServicioInventario getInstance() {
        if (instancia == null) {
            instancia = new ServicioInventario();
        }
        return instancia;
    }
    
    /**
     * Verifica la disponibilidad del servidor de inventario
     */
    public CompletableFuture<Boolean> verificarDisponibilidad() {
        return gestorConexion.verificarDisponibilidad();
    }
    
    /**
     * Obtiene todos los medicamentos disponibles (con stock > 0)
     */
    public CompletableFuture<ObservableList<ModeloMedicamentoInventario>> obtenerMedicamentosDisponibles() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Si el cache es válido, devolverlo
                if (cacheValido && !medicamentosCache.isEmpty()) {
                    return medicamentosCache;
                }
                
                // Conectar si no está conectado
                if (!gestorConexion.isConectado()) {
                    boolean conectado = gestorConexion.conectar().get();
                    if (!conectado) {
                        throw new RuntimeException("No se pudo conectar al servidor de inventario");
                    }
                }
                
                // Construir mensaje para obtener farmacia completa
                String mensaje = ProtocoloInventario.construirObtenerFarmacia();
                
                // Enviar petición
                String respuesta = gestorConexion.enviarMensaje(mensaje).get();
                
                // Procesar respuesta
                if (ProtocoloInventario.esExitoso(respuesta)) {
                    String datosBase64 = ProtocoloInventario.extraerDatos(respuesta);
                    String datosJson = new String(Base64.getDecoder().decode(datosBase64));
                    
                    // Parsear JSON y convertir a modelos
                    ObservableList<ModeloMedicamentoInventario> medicamentos = parsearMedicamentos(datosJson);
                    
                    // Filtrar solo los que tienen stock disponible
                    ObservableList<ModeloMedicamentoInventario> disponibles = medicamentos.stream()
                            .filter(ModeloMedicamentoInventario::estaDisponible)
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));
                    
                    // Actualizar cache
                    Platform.runLater(() -> {
                        medicamentosCache.clear();
                        medicamentosCache.addAll(disponibles);
                        cacheValido = true;
                    });
                    
                    return disponibles;
                } else {
                    throw new RuntimeException("Error del servidor: " + ProtocoloInventario.extraerDatos(respuesta));
                }
                
            } catch (Exception e) {
                System.err.println("Error al obtener medicamentos disponibles: " + e.getMessage());
                throw new RuntimeException("Error al obtener medicamentos: " + e.getMessage());
            }
        });
    }
    
    /**
     * Busca medicamentos por nombre
     */
    public CompletableFuture<ObservableList<ModeloMedicamentoInventario>> buscarMedicamentos(String nombre) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Conectar si no está conectado
                if (!gestorConexion.isConectado()) {
                    boolean conectado = gestorConexion.conectar().get();
                    if (!conectado) {
                        throw new RuntimeException("No se pudo conectar al servidor de inventario");
                    }
                }
                
                // Construir mensaje de búsqueda
                String mensaje = ProtocoloInventario.construirBuscarFarmacia(nombre);
                
                // Enviar petición
                String respuesta = gestorConexion.enviarMensaje(mensaje).get();
                
                // Procesar respuesta
                if (ProtocoloInventario.esExitoso(respuesta)) {
                    String datosBase64 = ProtocoloInventario.extraerDatos(respuesta);
                    String datosJson = new String(Base64.getDecoder().decode(datosBase64));
                    
                    // Parsear JSON y convertir a modelos
                    ObservableList<ModeloMedicamentoInventario> medicamentos = parsearMedicamentos(datosJson);
                    
                    // Filtrar solo los que tienen stock disponible
                    return medicamentos.stream()
                            .filter(ModeloMedicamentoInventario::estaDisponible)
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));
                } else {
                    throw new RuntimeException("Error del servidor: " + ProtocoloInventario.extraerDatos(respuesta));
                }
                
            } catch (Exception e) {
                System.err.println("Error al buscar medicamentos: " + e.getMessage());
                throw new RuntimeException("Error al buscar medicamentos: " + e.getMessage());
            }
        });
    }
    
    /**
     * Actualiza las unidades de un medicamento en el inventario
     */
    public CompletableFuture<Boolean> actualizarUnidades(String codigo, int nuevasUnidades) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Conectar si no está conectado
                if (!gestorConexion.isConectado()) {
                    boolean conectado = gestorConexion.conectar().get();
                    if (!conectado) {
                        throw new RuntimeException("No se pudo conectar al servidor de inventario");
                    }
                }
                
                // Construir mensaje de actualización
                String mensaje = ProtocoloInventario.construirActualizarUnidades(codigo, nuevasUnidades);
                
                // Enviar petición
                String respuesta = gestorConexion.enviarMensaje(mensaje).get();
                
                // Procesar respuesta
                if (ProtocoloInventario.esExitoso(respuesta)) {
                    // Invalidar cache para forzar recarga
                    cacheValido = false;
                    
                    // Actualizar el medicamento en el cache si existe
                    Platform.runLater(() -> {
                        medicamentosCache.stream()
                                .filter(med -> codigo.equals(med.getCodigo()))
                                .findFirst()
                                .ifPresent(med -> med.setUnidadesDisponibles(nuevasUnidades));
                    });
                    
                    return true;
                } else {
                    System.err.println("Error al actualizar unidades: " + ProtocoloInventario.extraerDatos(respuesta));
                    return false;
                }
                
            } catch (Exception e) {
                System.err.println("Error al actualizar unidades: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Filtra medicamentos por múltiples criterios
     */
    public ObservableList<ModeloMedicamentoInventario> filtrarMedicamentos(
            ObservableList<ModeloMedicamentoInventario> medicamentos,
            String textoBusqueda,
            String laboratorio,
            String categoria,
            String formaFarmaceutica,
            boolean soloConStock,
            int stockMinimo) {
        
        return medicamentos.stream()
                .filter(med -> {
                    // Filtro por texto de búsqueda
                    if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                        if (!med.coincideConBusqueda(textoBusqueda)) {
                            return false;
                        }
                    }
                    
                    // Filtro por laboratorio
                    if (laboratorio != null && !laboratorio.trim().isEmpty() && !"Todos".equals(laboratorio)) {
                        if (med.getLaboratorio() == null || !med.getLaboratorio().equalsIgnoreCase(laboratorio)) {
                            return false;
                        }
                    }
                    
                    // Filtro por categoría
                    if (categoria != null && !categoria.trim().isEmpty() && !"Todas".equals(categoria)) {
                        if (med.getCategoria() == null || !med.getCategoria().equalsIgnoreCase(categoria)) {
                            return false;
                        }
                    }
                    
                    // Filtro por forma farmacéutica
                    if (formaFarmaceutica != null && !formaFarmaceutica.trim().isEmpty() && !"Todas".equals(formaFarmaceutica)) {
                        if (med.getFormaFarmaceutica() == null || !med.getFormaFarmaceutica().equalsIgnoreCase(formaFarmaceutica)) {
                            return false;
                        }
                    }
                    
                    // Filtro por stock
                    if (soloConStock && !med.estaDisponible()) {
                        return false;
                    }
                    
                    // Filtro por stock mínimo
                    if (stockMinimo > 0 && med.getUnidadesDisponibles() < stockMinimo) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    
    /**
     * Obtiene la lista de laboratorios únicos
     */
    public ObservableList<String> obtenerLaboratorios() {
        return medicamentosCache.stream()
                .map(ModeloMedicamentoInventario::getLaboratorio)
                .filter(lab -> lab != null && !lab.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    
    /**
     * Obtiene la lista de categorías únicas
     */
    public ObservableList<String> obtenerCategorias() {
        return medicamentosCache.stream()
                .map(ModeloMedicamentoInventario::getCategoria)
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    
    /**
     * Obtiene la lista de formas farmacéuticas únicas
     */
    public ObservableList<String> obtenerFormasFarmaceuticas() {
        return medicamentosCache.stream()
                .map(ModeloMedicamentoInventario::getFormaFarmaceutica)
                .filter(forma -> forma != null && !forma.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    
    /**
     * Invalida el cache forzando una recarga en la próxima consulta
     */
    public void invalidarCache() {
        cacheValido = false;
        Platform.runLater(() -> medicamentosCache.clear());
    }
    
    /**
     * Obtiene el estado de conexión del servicio
     */
    public String getEstadoConexion() {
        return gestorConexion.getEstadoConexion();
    }
    
    /**
     * Parsea el JSON de medicamentos y los convierte a modelos
     */
    private ObservableList<ModeloMedicamentoInventario> parsearMedicamentos(String jsonData) {
        ObservableList<ModeloMedicamentoInventario> medicamentos = FXCollections.observableArrayList();
        
        try {
            JsonArray jsonArray = gson.fromJson(jsonData, JsonArray.class);
            
            for (JsonElement element : jsonArray) {
                JsonObject jsonMedicamento = element.getAsJsonObject();
                ModeloMedicamentoInventario medicamento = new ModeloMedicamentoInventario();
                
                // Mapear campos del JSON al modelo
                medicamento.setCodigo(getStringFromJson(jsonMedicamento, "codigo"));
                medicamento.setNombre(getStringFromJson(jsonMedicamento, "nombre"));
                medicamento.setLaboratorio(getStringFromJson(jsonMedicamento, "laboratorio"));
                medicamento.setPrincipioActivo(getStringFromJson(jsonMedicamento, "principioActivo"));
                medicamento.setFormaFarmaceutica(getStringFromJson(jsonMedicamento, "formaFarmaceutica"));
                medicamento.setDosis(getStringFromJson(jsonMedicamento, "dosis"));
                medicamento.setVia(getStringFromJson(jsonMedicamento, "via"));
                medicamento.setPresentacion(getStringFromJson(jsonMedicamento, "presentacion"));
                medicamento.setCategoria(getStringFromJson(jsonMedicamento, "categoria"));
                medicamento.setDescripcion(getStringFromJson(jsonMedicamento, "descripcion"));
                medicamento.setUbicacion(getStringFromJson(jsonMedicamento, "ubicacion"));
                medicamento.setLote(getStringFromJson(jsonMedicamento, "lote"));
                medicamento.setFechaCaducidad(getStringFromJson(jsonMedicamento, "fechaCaducidad"));
                medicamento.setObservaciones(getStringFromJson(jsonMedicamento, "observaciones"));
                
                // Campos numéricos
                medicamento.setUnidadesDisponibles(getIntFromJson(jsonMedicamento, "unidades"));
                medicamento.setPrecioUnitario(getDoubleFromJson(jsonMedicamento, "precio"));
                
                // Campo booleano
                medicamento.setRequiereReceta(getBooleanFromJson(jsonMedicamento, "requiereReceta"));
                
                medicamentos.add(medicamento);
            }
            
        } catch (Exception e) {
            System.err.println("Error al parsear medicamentos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicamentos;
    }
    
    // Métodos auxiliares para extraer datos del JSON
    private String getStringFromJson(JsonObject json, String campo) {
        return json.has(campo) && !json.get(campo).isJsonNull() ? json.get(campo).getAsString() : "";
    }
    
    private int getIntFromJson(JsonObject json, String campo) {
        return json.has(campo) && !json.get(campo).isJsonNull() ? json.get(campo).getAsInt() : 0;
    }
    
    private double getDoubleFromJson(JsonObject json, String campo) {
        return json.has(campo) && !json.get(campo).isJsonNull() ? json.get(campo).getAsDouble() : 0.0;
    }
    
    private boolean getBooleanFromJson(JsonObject json, String campo) {
        return json.has(campo) && !json.get(campo).isJsonNull() && json.get(campo).getAsBoolean();
    }
} 