package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.theme.ThemeManager;

import Utilidades1.GestorSocket;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador principal del módulo de facturación
 */
public class FacturacionController implements Initializable {

    // Componentes principales
    @FXML private BorderPane mainPane;
    @FXML private TabPane tabPane;
    
    // Tab de listado de facturas
    @FXML private Tab tabListado;
    @FXML private ComboBox<String> cmbEstados;
    @FXML private MFXDatePicker dpFechaInicio;
    @FXML private MFXDatePicker dpFechaFin;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;
    @FXML private TextField txtBuscar;
    @FXML private Button btnEliminarFactura;
    @FXML private Button btnExportarPDF;
    @FXML private TableView<ModeloFactura> tablaFacturas;
    @FXML private TableColumn<ModeloFactura, String> colNumero;
    @FXML private TableColumn<ModeloFactura, String> colFecha;
    @FXML private TableColumn<ModeloFactura, String> colCliente;
    @FXML private TableColumn<ModeloFactura, String> colPaciente;
    @FXML private TableColumn<ModeloFactura, String> colVeterinario;
    @FXML private TableColumn<ModeloFactura, String> colEstado;
    @FXML private TableColumn<ModeloFactura, String> colSubtotal;
    @FXML private TableColumn<ModeloFactura, String> colIVA;
    @FXML private TableColumn<ModeloFactura, String> colTotal;
    @FXML private TableColumn<ModeloFactura, Void> colAcciones;
    
    // Tab de estadísticas
    @FXML private Tab tabEstadisticas;
    @FXML private ComboBox<String> cmbPeriodo;
    @FXML private MFXDatePicker dpEstadisticasInicio;
    @FXML private MFXDatePicker dpEstadisticasFin;
    @FXML private Button btnActualizarEstadisticas;
    @FXML private Button btnExportarEstadisticas;
    @FXML private Label lblTotalFacturas;
    @FXML private Label lblIngresosTotales;
    @FXML private Label lblFacturasPendientes;
    @FXML private Label lblPromedioFactura;
    @FXML private PieChart chartEstados;
    @FXML private LineChart<String, Number> chartIngresos;
    @FXML private CategoryAxis xAxisMeses;
    @FXML private NumberAxis yAxisIngresos;
    
    // Tab de borradores
    @FXML private Tab tabBorradores;
    @FXML private TextField txtBuscarBorrador;
    @FXML private TableView<ModeloFactura> tablaBorradores;
    @FXML private TableColumn<ModeloFactura, String> colBorradorFecha;
    @FXML private TableColumn<ModeloFactura, String> colBorradorCliente;
    @FXML private TableColumn<ModeloFactura, String> colBorradorPaciente;
    @FXML private TableColumn<ModeloFactura, String> colBorradorVeterinario;
    @FXML private TableColumn<ModeloFactura, String> colBorradorTotal;
    @FXML private TableColumn<ModeloFactura, Void> colBorradorAcciones;
    
    // Servicios y datos
    private GestorSocket gestorSocket;
    private Usuario usuarioActual;
    private ObservableList<ModeloFactura> listaFacturas;
    private ObservableList<ModeloFactura> listaBorradores;
    private DecimalFormat formatoMoneda;
    private SimpleDateFormat formatoFecha;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar servicios
            gestorSocket = GestorSocket.getInstance();
            formatoMoneda = new DecimalFormat("#,##0.00 €");
            formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            
            // Inicializar listas observables
            listaFacturas = FXCollections.observableArrayList();
            listaBorradores = FXCollections.observableArrayList();
            
            // Configurar interfaz
            configurarTablas();
            configurarFiltros();
            configurarEstadisticas();
            configurarEventos();
            
            // Aplicar tema
            aplicarTema();
            
            // Cargar datos iniciales
            cargarDatosIniciales();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de inicialización", "No se pudo inicializar el módulo de facturación: " + e.getMessage());
        }
    }
    
    /**
     * Configura las tablas de facturas
     */
    private void configurarTablas() {
        // Configurar tabla principal de facturas
        configurarTablaFacturas();
        
        // Configurar tabla de borradores
        configurarTablaBorradores();
    }
    
    private void configurarTablaFacturas() {
        // Configurar columnas
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colFecha.setCellValueFactory(cellData -> {
            Date fecha = cellData.getValue().getFechaEmision();
            return new SimpleStringProperty(fecha != null ? formatoFecha.format(fecha) : "");
        });
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("nombrePaciente"));
        colVeterinario.setCellValueFactory(new PropertyValueFactory<>("veterinarioNombre"));
        colEstado.setCellValueFactory(cellData -> {
            ModeloFactura.EstadoFactura estado = cellData.getValue().getEstado();
            return new SimpleStringProperty(estado != null ? estado.getDescripcion() : "");
        });
        colSubtotal.setCellValueFactory(cellData -> {
            double subtotal = cellData.getValue().getSubtotal();
            return new SimpleStringProperty(formatoMoneda.format(subtotal));
        });
        colIVA.setCellValueFactory(cellData -> {
            double ivaTotal = cellData.getValue().getIvaGeneral() + cellData.getValue().getIvaMedicamentos();
            return new SimpleStringProperty(formatoMoneda.format(ivaTotal));
        });
        colTotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        // Configurar columna de acciones
        configurarColumnaAcciones();
        
        // Asignar datos
        tablaFacturas.setItems(listaFacturas);
        
        // Configurar selección
        tablaFacturas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Habilitar botones según el estado de la factura
                actualizarEstadoBotones(newSelection);
            }
        });
        
        // Hacer la tabla responsive
        tablaFacturas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarTablaBorradores() {
        // Configurar columnas existentes del FXML
        colBorradorFecha.setCellValueFactory(cellData -> {
            Date fecha = cellData.getValue().getFechaCreacion();
            return new SimpleStringProperty(fecha != null ? formatoFecha.format(fecha) : "");
        });
        colBorradorCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colBorradorPaciente.setCellValueFactory(new PropertyValueFactory<>("nombrePaciente"));
        colBorradorVeterinario.setCellValueFactory(new PropertyValueFactory<>("veterinarioNombre"));
        colBorradorTotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        // Configurar acciones para borradores
        colBorradorAcciones.setCellFactory(new Callback<TableColumn<ModeloFactura, Void>, TableCell<ModeloFactura, Void>>() {
            @Override
            public TableCell<ModeloFactura, Void> call(TableColumn<ModeloFactura, Void> param) {
                return new TableCell<ModeloFactura, Void>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox hbox = new HBox(5, btnEditar, btnEliminar);
                    
                    {
                        btnEditar.getStyleClass().add("btn-primary");
                        btnEliminar.getStyleClass().add("btn-danger");
                        hbox.setAlignment(Pos.CENTER);
                        
                        btnEditar.setOnAction(e -> {
                            ModeloFactura factura = getTableView().getItems().get(getIndex());
                            editarFactura(factura);
                        });
                        
                        btnEliminar.setOnAction(e -> {
                            ModeloFactura factura = getTableView().getItems().get(getIndex());
                            eliminarBorrador(factura);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : hbox);
                    }
                };
            }
        });
        
        // Asignar datos
        tablaBorradores.setItems(listaBorradores);
        tablaBorradores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(new Callback<TableColumn<ModeloFactura, Void>, TableCell<ModeloFactura, Void>>() {
            @Override
            public TableCell<ModeloFactura, Void> call(TableColumn<ModeloFactura, Void> param) {
                return new TableCell<ModeloFactura, Void>() {
                    private final Button btnVer = new Button("Ver");
                    private final Button btnPDF = new Button("PDF");
                    private final Button btnEditar = new Button("Editar");
                    private final VBox vbox = new VBox(5, btnVer, btnPDF, btnEditar);
                    
                    {
                        btnVer.getStyleClass().add("btn-info");
                        btnPDF.getStyleClass().add("btn-success");
                        btnEditar.getStyleClass().add("btn-primary");
                        
                        // Configurar el VBox para disposición vertical
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setSpacing(5);
                        
                        // Hacer que los botones ocupen todo el ancho disponible
                        btnVer.setMaxWidth(Double.MAX_VALUE);
                        btnPDF.setMaxWidth(Double.MAX_VALUE);
                        btnEditar.setMaxWidth(Double.MAX_VALUE);
                        
                        // Ajustar el tamaño de los botones para mejor legibilidad
                        btnVer.setMinHeight(30);
                        btnPDF.setMinHeight(30);
                        btnEditar.setMinHeight(30);
                        btnVer.setPrefHeight(30);
                        btnPDF.setPrefHeight(30);
                        btnEditar.setPrefHeight(30);
                        
                        // Asegurar un ancho mínimo para que el texto se vea completo
                        btnVer.setMinWidth(70);
                        btnPDF.setMinWidth(70);
                        btnEditar.setMinWidth(70);
                        
                        // Configurar el estilo de fuente para mejor legibilidad
                        btnVer.setStyle(btnVer.getStyle() + "; -fx-font-size: 11px; -fx-font-weight: 500;");
                        btnPDF.setStyle(btnPDF.getStyle() + "; -fx-font-size: 11px; -fx-font-weight: 500;");
                        btnEditar.setStyle(btnEditar.getStyle() + "; -fx-font-size: 11px; -fx-font-weight: 500;");
                        
                        btnVer.setOnAction(e -> {
                            ModeloFactura factura = getTableView().getItems().get(getIndex());
                            verFactura(factura);
                        });
                        
                        btnPDF.setOnAction(e -> {
                            ModeloFactura factura = getTableView().getItems().get(getIndex());
                            exportarPDF(factura);
                        });
                        
                        btnEditar.setOnAction(e -> {
                            ModeloFactura factura = getTableView().getItems().get(getIndex());
                            // Verificar si la factura es borrador antes de permitir edición
                            if (factura.isEsBorrador()) {
                                editarFactura(factura);
                            } else {
                                mostrarError("Factura ya emitida", "Factura ya emitida, no se puede modificar");
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Ahora el botón Editar siempre es visible
                            setGraphic(vbox);
                        }
                    }
                };
            }
        });
    }
    
    /**
     * Configura los filtros de búsqueda
     */
    private void configurarFiltros() {
        // Configurar combo de estados
        cmbEstados.getItems().addAll(
            "Todos",
            "Borrador",
            "Emitida", 
            "Pagada",
            "Vencida",
            "Anulada"
        );
        cmbEstados.setValue("Todos");
        
        // Configurar fechas por defecto (último mes)
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.minusMonths(1));
        dpFechaFin.setValue(hoy);
    }
    
    /**
     * Configura la sección de estadísticas
     */
    private void configurarEstadisticas() {
        // Configurar combo de periodos
        cmbPeriodo.getItems().addAll("2024", "2023", "2022", "2021", "2020");
        cmbPeriodo.setValue("2024");
        
        // Cargar estadísticas iniciales
        cargarEstadisticas();
    }
    
    /**
     * Configura los eventos de los botones
     */
    private void configurarEventos() {
        btnBuscar.setOnAction(e -> buscarFacturas());
        btnLimpiarFiltros.setOnAction(e -> limpiarFiltros());
        btnEliminarFactura.setOnAction(e -> onEliminarFactura());
        btnExportarPDF.setOnAction(e -> onExportarPDF());
        btnActualizarEstadisticas.setOnAction(e -> actualizarEstadisticas());
        btnExportarEstadisticas.setOnAction(e -> exportarEstadisticas());
        cmbPeriodo.setOnAction(e -> cargarEstadisticas());
    }
    
    /**
     * Aplica el tema actual
     */
    private void aplicarTema() {
        try {
            // Cargar el archivo CSS específico de facturación
            String facturacionCSS = getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/facturacion-styles.css").toExternalForm();
            
            // Agregar la hoja de estilos específica si no está ya agregada
            if (!mainPane.getScene().getStylesheets().contains(facturacionCSS)) {
                mainPane.getScene().getStylesheets().add(facturacionCSS);
                System.out.println("✅ Archivo CSS de facturación cargado: " + facturacionCSS);
            }
            
            // Aplicar clase CSS al contenedor principal para identificar el módulo
            if (!mainPane.getStyleClass().contains("facturacion-module")) {
                mainPane.getStyleClass().add("facturacion-module");
            }
            
            // Aplicar tema oscuro/claro
            if (ThemeManager.getInstance().isDarkTheme()) {
                mainPane.getStyleClass().add("dark-theme");
            } else {
                mainPane.getStyleClass().remove("dark-theme");
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar estilos CSS de facturación: " + e.getMessage());
            // Aplicar solo el tema básico si hay error
            if (ThemeManager.getInstance().isDarkTheme()) {
                mainPane.getStyleClass().add("dark-theme");
            } else {
                mainPane.getStyleClass().remove("dark-theme");
            }
        }
    }
    
    /**
     * Carga los datos iniciales del módulo
     */
    private void cargarDatosIniciales() {
        System.out.println("🚀 Iniciando carga de datos iniciales del módulo de facturación...");
        
        // Verificar conexión primero
        if (!verificarConexion()) {
            Platform.runLater(() -> {
                mostrarError("Error de conexión", 
                    "No se pudo establecer conexión con el servidor.\n" +
                    "Verifique que el servidor esté ejecutándose y vuelva a intentarlo.");
            });
            return;
        }
        
        System.out.println("✅ Conexión verificada, procediendo con la carga de datos...");
        
        // Ejecutar diagnóstico en modo debug
        if (System.getProperty("debug.facturacion", "false").equals("true")) {
            diagnosticarConexion();
        }
        
        // Cargar datos con pequeños retrasos para evitar conflictos
        new Thread(() -> {
            try {
                System.out.println("📊 Cargando facturas...");
                cargarFacturasSync();
                
                // Pequeña pausa entre cargas
                Thread.sleep(500);
                
                System.out.println("📝 Cargando borradores...");
                cargarBorradoresSync();
                
                // Cargar estadísticas en el hilo de JavaFX
                Platform.runLater(() -> {
                    System.out.println("📈 Cargando estadísticas...");
                    cargarEstadisticas();
                });
                
                System.out.println("✅ Carga de datos iniciales completada");
                
            } catch (Exception e) {
                System.err.println("❌ Error en carga de datos iniciales: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    mostrarError("Error de carga", 
                        "Error al cargar los datos iniciales: " + e.getMessage() + "\n" +
                        "Puede intentar recargar manualmente usando los botones de la interfaz.");
                });
            }
        }).start();
    }
    
    /**
     * Verifica y reestablece la conexión si es necesario
     */
    private boolean verificarConexion() {
        try {
            if (!gestorSocket.isConectado()) {
                System.out.println("Conexión perdida, intentando reconectar...");
                gestorSocket.cerrarConexion();
                gestorSocket = GestorSocket.getInstance();
                Thread.sleep(500); // Esperar un poco para la reconexión
            }
            return gestorSocket.isConectado();
        } catch (Exception e) {
            System.err.println("Error al verificar conexión: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Carga facturas finalizadas de forma síncrona con conexión independiente
     */
    private void cargarFacturasSync() {
        // Usar una conexión independiente para evitar conflictos con otros módulos
        GestorSocket gestorSocketIndependiente = null;
        try {
            System.out.println("🔄 Iniciando carga de facturas finalizadas con conexión independiente...");
            gestorSocketIndependiente = GestorSocket.crearConexionIndependiente();
            
            String peticion = String.valueOf(Protocolo.OBTENER_FACTURAS_FINALIZADAS);
            System.out.println("📤 Enviando petición de facturas finalizadas: " + peticion);
            
            gestorSocketIndependiente.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocketIndependiente.getEntrada();
            if (entrada == null) {
                System.err.println("No se pudo obtener el stream de entrada");
                Platform.runLater(() -> mostrarError("Error", "No se pudo obtener el stream de entrada"));
                return;
            }
            
            System.out.println("⏳ Esperando respuesta del servidor...");
            
            int codigoRespuesta = entrada.readInt();
            System.out.println("📥 Código de respuesta recibido: " + codigoRespuesta);
            
            if (codigoRespuesta == Protocolo.OBTENER_FACTURAS_FINALIZADAS_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloFactura> facturas = (List<ModeloFactura>) entrada.readObject();
                
                Platform.runLater(() -> {
                    listaFacturas.clear();
                    if (facturas != null && !facturas.isEmpty()) {
                        listaFacturas.addAll(facturas);
                        System.out.println("✅ Facturas finalizadas cargadas exitosamente: " + facturas.size());
                    } else {
                        System.out.println("ℹ️ No se encontraron facturas finalizadas");
                    }
                });
            } else if (codigoRespuesta == Protocolo.ERROR_OBTENER_FACTURAS_FINALIZADAS) {
                System.err.println("❌ Error del servidor al obtener facturas finalizadas");
                Platform.runLater(() -> mostrarError("Error", "Error del servidor al obtener las facturas"));
            } else {
                System.err.println("❌ Respuesta inesperada del servidor: " + codigoRespuesta);
                Platform.runLater(() -> mostrarError("Error", "Respuesta inesperada del servidor: " + codigoRespuesta));
            }
            
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("❌ Timeout al cargar facturas");
            Platform.runLater(() -> mostrarError("Error de timeout", "El servidor tardó demasiado en responder. Intente más tarde."));
        } catch (java.io.EOFException e) {
            System.err.println("❌ Error de EOF - conexión cerrada inesperadamente");
            Platform.runLater(() -> mostrarError("Error de conexión", "La conexión se cerró inesperadamente. Verifique el servidor."));
        } catch (IOException e) {
            System.err.println("❌ Error de E/O al cargar facturas: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> mostrarError("Error de comunicación", "Error de comunicación con el servidor: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Error al cargar facturas: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                mostrarError("Error de comunicación", "No se pudieron cargar las facturas: " + e.getMessage());
            });
        } finally {
            // Cerrar la conexión independiente
            if (gestorSocketIndependiente != null) {
                try {
                    gestorSocketIndependiente.cerrarConexion();
                    System.out.println("🔌 Conexión independiente cerrada correctamente");
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión independiente: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Carga borradores de forma síncrona con conexión independiente
     */
    private void cargarBorradoresSync() {
        // Usar una conexión independiente para evitar conflictos con otros módulos
        GestorSocket gestorSocketIndependiente = null;
        try {
            System.out.println("🔄 Iniciando carga de borradores con conexión independiente...");
            gestorSocketIndependiente = GestorSocket.crearConexionIndependiente();
            
            String peticion = String.valueOf(Protocolo.OBTENER_FACTURAS_BORRADOR);
            System.out.println("📤 Enviando petición de borradores: " + peticion);
            
            // Enviar petición
            gestorSocketIndependiente.enviarPeticion(peticion);
            System.out.println("✅ Petición enviada correctamente");
            
            // Obtener stream de entrada
            ObjectInputStream entrada = gestorSocketIndependiente.getEntrada();
            if (entrada == null) {
                System.err.println("❌ No se pudo obtener el stream de entrada");
                Platform.runLater(() -> mostrarError("Error", "No se pudo obtener el stream de entrada"));
                return;
            }
            
            System.out.println("⏳ Esperando respuesta del servidor para borradores...");
            
            // Leer respuesta con timeout
            int codigoRespuesta;
            try {
                codigoRespuesta = entrada.readInt();
                System.out.println("📥 Código de respuesta recibido para borradores: " + codigoRespuesta);
            } catch (java.io.EOFException e) {
                System.err.println("❌ EOFException al leer código de respuesta - conexión cerrada inesperadamente");
                Platform.runLater(() -> mostrarError("Error de conexión", 
                    "La conexión se cerró inesperadamente. Verifique que el servidor esté funcionando correctamente."));
                return;
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("❌ Timeout al esperar respuesta del servidor");
                Platform.runLater(() -> mostrarError("Error de timeout", 
                    "El servidor tardó demasiado en responder. Intente más tarde."));
                return;
            }
            
            if (codigoRespuesta == Protocolo.OBTENER_FACTURAS_BORRADOR_RESPONSE) {
                System.out.println("✅ Respuesta exitosa, leyendo lista de borradores...");
                try {
                    @SuppressWarnings("unchecked")
                    List<ModeloFactura> borradores = (List<ModeloFactura>) entrada.readObject();
                    
                    Platform.runLater(() -> {
                        listaBorradores.clear();
                        if (borradores != null) {
                            listaBorradores.addAll(borradores);
                            System.out.println("✅ Borradores cargados exitosamente: " + borradores.size());
                            
                            // Forzar actualización de la tabla
                            tablaBorradores.refresh();
                            tablaBorradores.requestFocus();
                            
                            // Log de debug para verificar el estado
                            System.out.println("📊 Lista observable contiene: " + listaBorradores.size() + " elementos");
                        } else {
                            System.out.println("⚠️ Lista de borradores es null");
                        }
                    });
                } catch (ClassNotFoundException e) {
                    System.err.println("❌ Error de deserialización: " + e.getMessage());
                    Platform.runLater(() -> mostrarError("Error", "Error al procesar la respuesta del servidor"));
                }
            } else if (codigoRespuesta == Protocolo.ERROR_OBTENER_FACTURAS_BORRADOR) {
                System.err.println("❌ Error del servidor al obtener borradores");
                Platform.runLater(() -> mostrarError("Error", "Error del servidor al obtener los borradores"));
            } else {
                System.err.println("❌ Respuesta inesperada del servidor: " + codigoRespuesta);
                Platform.runLater(() -> mostrarError("Error", "Respuesta inesperada del servidor para borradores: " + codigoRespuesta));
            }
            
        } catch (java.io.IOException e) {
            System.err.println("❌ Error de E/O en la comunicación: " + e.getMessage());
            Platform.runLater(() -> mostrarError("Error de comunicación", 
                "Error de comunicación con el servidor: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Error general al cargar borradores: " + e.getMessage());
            Platform.runLater(() -> mostrarError("Error", "Error al cargar borradores: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido")));
        } finally {
            // Cerrar la conexión independiente
            if (gestorSocketIndependiente != null) {
                try {
                    gestorSocketIndependiente.cerrarConexion();
                    System.out.println("🔌 Conexión independiente para borradores cerrada correctamente");
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión independiente de borradores: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Busca facturas con filtros
     */
    @FXML
    private void buscarFacturas() {
        new Thread(() -> {
            try {
                // Preparar parámetros de búsqueda
                String numeroFactura = txtBuscar.getText().trim();
                String estado = cmbEstados.getValue();
                LocalDate fechaInicio = dpFechaInicio.getValue();
                LocalDate fechaFin = dpFechaFin.getValue();
                
                // Construir petición
                StringBuilder peticion = new StringBuilder();
                peticion.append(Protocolo.BUSCAR_FACTURAS_POR_FECHA);
                peticion.append(Protocolo.SEPARADOR_CODIGO);
                
                if (fechaInicio != null && fechaFin != null) {
                    Date fechaInicioDate = Date.from(fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date fechaFinDate = Date.from(fechaFin.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    
                    gestorSocket.enviarPeticion(peticion.toString());
                    gestorSocket.getSalida().writeObject(fechaInicioDate);
                    gestorSocket.getSalida().writeObject(fechaFinDate);
                    gestorSocket.getSalida().flush();
                    
                    ObjectInputStream entrada = gestorSocket.getEntrada();
                    int codigoRespuesta = entrada.readInt();
                    
                    if (codigoRespuesta == Protocolo.BUSCAR_FACTURAS_POR_FECHA_RESPONSE) {
                        @SuppressWarnings("unchecked")
                        List<ModeloFactura> facturas = (List<ModeloFactura>) entrada.readObject();
                        
                        // Filtrar por estado y número si es necesario
                        facturas = filtrarFacturasLocalmente(facturas, numeroFactura, estado);
                        
                        final List<ModeloFactura> facturasFinal = facturas;
                        Platform.runLater(() -> {
                            listaFacturas.clear();
                            listaFacturas.addAll(facturasFinal);
                        });
                    } else {
                        Platform.runLater(() -> mostrarError("Error", "No se pudieron buscar las facturas"));
                    }
                } else {
                    // Si no hay fechas, cargar todas y filtrar localmente
                    cargarFacturas();
                }
                
            } catch (Exception e) {

                Platform.runLater(() -> mostrarError("Error", "Error en la búsqueda: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido")));
            }
        }).start();
    }
    
    /**
     * Filtra facturas localmente por número y estado
     */
    private List<ModeloFactura> filtrarFacturasLocalmente(List<ModeloFactura> facturas, String numeroFactura, String estado) {
        return facturas.stream()
            .filter(f -> {
                // Filtro por número
                if (numeroFactura != null && !numeroFactura.isEmpty()) {
                    String numero = f.getNumeroFactura();
                    if (numero == null || !numero.toLowerCase().contains(numeroFactura.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filtro por estado
                if (estado != null && !estado.equals("Todos")) {
                    if (f.getEstado() == null || !f.getEstado().getDescripcion().equals(estado)) {
                        return false;
                    }
                }
                
                return true;
            })
            .toList();
    }
    
    /**
     * Limpia todos los filtros
     */
    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbEstados.setValue("Todos");
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.minusMonths(1));
        dpFechaFin.setValue(hoy);
        cargarFacturas();
    }
    
    /**
     * Carga las estadísticas del periodo seleccionado
     */
    private void cargarEstadisticas() {
        String periodo = cmbPeriodo.getValue();
        if (periodo == null) return;
        
        // Calcular estadísticas de las facturas cargadas
        double totalFacturado = listaFacturas.stream()
            .filter(f -> !f.isEsBorrador())
            .filter(f -> {
                if (f.getFechaEmision() != null) {
                    LocalDate fecha = f.getFechaEmision().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return fecha.getYear() == Integer.parseInt(periodo);
                }
                return false;
            })
            .mapToDouble(ModeloFactura::getTotal)
            .sum();
        
        long numeroFacturas = listaFacturas.stream()
            .filter(f -> !f.isEsBorrador())
            .filter(f -> {
                if (f.getFechaEmision() != null) {
                    LocalDate fecha = f.getFechaEmision().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return fecha.getYear() == Integer.parseInt(periodo);
                }
                return false;
            })
            .count();
        
        double promedioFactura = numeroFacturas > 0 ? totalFacturado / numeroFacturas : 0;
        
        // Actualizar labels
        lblTotalFacturas.setText(String.valueOf(numeroFacturas));
        lblIngresosTotales.setText(formatoMoneda.format(totalFacturado));
        lblFacturasPendientes.setText(String.valueOf(numeroFacturas - listaFacturas.stream().filter(f -> f.isEsBorrador()).count()));
        lblPromedioFactura.setText(formatoMoneda.format(promedioFactura));
    }
    
    /**
     * Crea una nueva factura
     */
    private void crearNuevaFactura() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/factura-form.fxml"));
            Parent root = loader.load();
            
            FacturaFormController controller = loader.getController();
            controller.setUsuarioActual(usuarioActual);
            controller.setFacturacionController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Nueva Factura");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el formulario de factura: " + e.getMessage());
        }
    }
    
    /**
     * Muestra los detalles de una factura usando el mismo formulario que editar pero en modo solo lectura
     */
    private void verFactura(ModeloFactura factura) {
        if (factura == null) {
            mostrarError("Error", "No se ha seleccionado ninguna factura");
            return;
        }

        // Usar la misma funcionalidad que editarFactura pero en modo solo lectura
        System.out.println("👁️ Abriendo factura en modo SOLO LECTURA: " + 
                          (factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "BORRADOR"));
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/factura-form.fxml"));
            Parent root = loader.load();
            
            FacturaFormController controller = loader.getController();
            controller.setUsuarioActual(usuarioActual);
            controller.setFacturacionController(this);
            controller.setFactura(factura); // Cargar factura existente
            
            // CONFIGURAR EN MODO SOLO LECTURA
            controller.configurarModoVisualizacion();
            
            Stage stage = new Stage();
            stage.setTitle("Ver Factura (Solo Lectura) - " + (factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "BORRADOR"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Error al abrir la factura: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir la factura: " + e.getMessage());
        }
    }
    
    /**
     * Edita una factura existente
     */
    private void editarFactura(ModeloFactura factura) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/factura-form.fxml"));
            Parent root = loader.load();
            
            FacturaFormController controller = loader.getController();
            controller.setUsuarioActual(usuarioActual);
            controller.setFacturacionController(this);
            controller.setFactura(factura); // Cargar factura existente
            
            Stage stage = new Stage();
            stage.setTitle("Editar Factura - " + (factura.getNumeroFactura() != null ? factura.getNumeroFactura() : "BORRADOR"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();
            
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el editor de factura: " + e.getMessage());
        }
    }
    
    /**
     * Elimina un borrador y restablece el inventario de sus medicamentos
     */
    private void eliminarBorrador(ModeloFactura factura) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar borrador?");
        alert.setContentText("Esta acción eliminará el borrador y restablecerá el inventario de todos sus medicamentos.\n" +
                            "Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    System.out.println("🗑️ Iniciando eliminación de borrador: " + factura.getId());
                    
                    // PASO 1: Restablecer inventario de medicamentos antes de eliminar
                    System.out.println("🔄 Restableciendo inventario de medicamentos del borrador...");
                    restablecerInventarioFactura(factura);
                    
                    // PASO 2: Crear una conexión independiente para las operaciones de servidor
                    GestorSocket gestorSocketEliminar = null;
                    try {
                        gestorSocketEliminar = GestorSocket.crearConexionIndependiente();
                        
                        // PASO 2A: Eliminar la factura del servidor principal
                        System.out.println("🗑️ Eliminando factura del servidor...");
                        String peticion = Protocolo.ELIMINAR_FACTURA + Protocolo.SEPARADOR_CODIGO + factura.getId().toString();
                        gestorSocketEliminar.enviarPeticion(peticion);
                        
                        ObjectInputStream entrada = gestorSocketEliminar.getEntrada();
                        int codigoRespuesta = entrada.readInt();
                        
                        if (codigoRespuesta == Protocolo.ELIMINAR_FACTURA_RESPONSE) {
                            boolean eliminada = entrada.readBoolean();
                            if (eliminada) {
                                System.out.println("✅ Factura eliminada correctamente del servidor");
                                
                                // PASO 2B: Desasociar factura de la cita si hay cita asociada
                                if (factura.getCitaId() != null) {
                                    System.out.println("🔗 Desasociando factura de la cita...");
                                    String mensajeDesasociacion = Protocolo.DESASOCIAR_FACTURA_DE_CITA + "|" + factura.getCitaId().toString();
                                    gestorSocketEliminar.enviarPeticion(mensajeDesasociacion);
                                    
                                    int respuestaDesasociacion = gestorSocketEliminar.getEntrada().readInt();
                                    if (respuestaDesasociacion == Protocolo.DESASOCIAR_FACTURA_DE_CITA_RESPONSE) {
                                        boolean desasociado = gestorSocketEliminar.getEntrada().readBoolean();
                                        if (desasociado) {
                                            System.out.println("✅ Factura desasociada correctamente de la cita");
                                        } else {
                                            System.out.println("⚠️ No se pudo desasociar la factura de la cita");
                                        }
                                    } else {
                                        System.out.println("❌ Error al desasociar factura de cita, código: " + respuestaDesasociacion);
                                    }
                                }
                                
                                // PASO 3: Actualizar UI en el hilo principal
                                Platform.runLater(() -> {
                                    System.out.println("🔄 Actualizando lista local de borradores...");
                                    listaBorradores.remove(factura);
                                    tablaBorradores.refresh();
                                    
                                    mostrarInfo("Éxito", "Borrador eliminado correctamente y inventario restablecido");
                                });
                                
                                // PASO 4: Recargar desde el servidor para asegurar consistencia
                                System.out.println("🔄 Recargando borradores desde el servidor...");
                                Thread.sleep(500); // Esperar un poco para que el servidor procese completamente
                                cargarBorradoresSync();
                                
                            } else {
                                System.err.println("❌ El servidor indicó que no se pudo eliminar la factura");
                                Platform.runLater(() -> mostrarError("Error", "No se pudo eliminar el borrador en el servidor"));
                            }
                        } else {
                            System.err.println("❌ Código de respuesta inesperado del servidor: " + codigoRespuesta);
                            Platform.runLater(() -> mostrarError("Error", "Respuesta inesperada del servidor: " + codigoRespuesta));
                        }
                        
                    } finally {
                        // Cerrar la conexión independiente
                        if (gestorSocketEliminar != null) {
                            try {
                                gestorSocketEliminar.cerrarConexion();
                                System.out.println("🔌 Conexión independiente para eliminación cerrada correctamente");
                            } catch (Exception e) {
                                System.err.println("Error al cerrar conexión independiente: " + e.getMessage());
                            }
                        }
                    }
                    
                } catch (InterruptedException e) {
                    System.err.println("❌ Operación de eliminación interrumpida: " + e.getMessage());
                    Platform.runLater(() -> mostrarError("Error", "Operación interrumpida"));
                } catch (Exception e) {
                    System.err.println("❌ Error general en eliminación de borrador: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarError("Error", "Error de comunicación: " + e.getMessage()));
                }
            }).start();
        }
    }
    
    /**
     * Exporta una factura a PDF
     */
    private void exportarPDF(ModeloFactura factura) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Factura PDF");
        fileChooser.setInitialFileName("Factura_" + factura.getNumeroFactura() + ".pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );
        
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                GeneradorPDFFactura generador = new GeneradorPDFFactura();
                generador.generarPDF(factura, file.getAbsolutePath());
                mostrarInfo("Éxito", "PDF generado correctamente en: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError("Error", "No se pudo generar el PDF: " + e.getMessage());
            }
        }
    }
    
    /**
     * Actualiza el estado de los botones según la factura seleccionada
     */
    private void actualizarEstadoBotones(ModeloFactura factura) {
        // Implementar lógica para habilitar/deshabilitar botones
        // según el estado de la factura
    }
    
    /**
     * Crea una factura desde una cita
     */
    public void crearFacturaDesdeCita(ModeloCita cita, ModeloPaciente paciente, ModeloPropietario propietario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/factura-form.fxml"));
            Parent root = loader.load();
            
            FacturaFormController controller = loader.getController();
            controller.setUsuarioActual(usuarioActual);
            controller.setFacturacionController(this);
            controller.cargarDatosDesdeCita(cita, paciente, propietario);
            
            Stage stage = new Stage();
            stage.setTitle("Nueva Factura desde Cita");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo crear la factura desde la cita: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza las listas después de cambios
     */
    public void actualizarListas() {
        System.out.println("🔄 Actualizando todas las listas...");
        cargarFacturas();
        cargarBorradores();
    }
    
    /**
     * Carga facturas (método público)
     */
    public void cargarFacturas() {
        new Thread(() -> {
            try {
                cargarFacturasSync();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error", "Error al cargar facturas: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * Carga borradores con reintentos automáticos
     */
    public void cargarBorradores() {
        cargarBorradoresConReintentos(3); // Máximo 3 intentos
    }
    
    /**
     * Carga borradores con un número específico de reintentos
     */
    private void cargarBorradoresConReintentos(int maxIntentos) {
        new Thread(() -> {
            for (int intento = 1; intento <= maxIntentos; intento++) {
                System.out.println("🔄 Intento " + intento + " de " + maxIntentos + " para cargar borradores");
                
                try {
                    // Pequeña pausa entre intentos
                    if (intento > 1) {
                        Thread.sleep(1000 * intento); // Pausa progresiva
                    }
                    
                    cargarBorradoresSync();
                    
                    // Si llegamos aquí sin excepción, el intento fue exitoso
                    System.out.println("✅ Borradores cargados exitosamente en el intento " + intento);
                    return;
                    
                } catch (Exception e) {
                    System.err.println("❌ Error en intento " + intento + ": " + e.getMessage());
                    
                    if (intento == maxIntentos) {
                        // Último intento fallido
                        Platform.runLater(() -> {
                            mostrarError("Error persistente", 
                                "No se pudieron cargar los borradores después de " + maxIntentos + " intentos.\n" +
                                "Verifique la conexión con el servidor y vuelva a intentarlo.");
                        });
                    } else {
                        System.out.println("⏳ Reintentando en " + (intento + 1) + " segundos...");
                    }
                }
            }
        }).start();
    }
    
    /**
     * Establece el usuario actual
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Muestra un mensaje de información
     */
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Métodos de eventos para los botones del FXML
     */
    @FXML
    private void onEliminarFactura() {
        ModeloFactura facturaSeleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        if (facturaSeleccionada != null) {
            eliminarBorrador(facturaSeleccionada);
        } else {
            mostrarError("Error", "Debe seleccionar una factura");
        }
    }

    @FXML
    private void onExportarPDF() {
        ModeloFactura facturaSeleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        if (facturaSeleccionada != null) {
            exportarPDF(facturaSeleccionada);
        } else {
            mostrarError("Error", "Debe seleccionar una factura");
        }
    }

    @FXML
    private void actualizarEstadisticas() {
        cargarEstadisticas();
    }

    @FXML
    private void exportarEstadisticas() {
        // Implementar exportación de estadísticas
        mostrarInfo("Información", "Funcionalidad en desarrollo");
    }

    /**
     * Recarga manualmente los borradores
     */
    @FXML
    private void recargarBorradores() {
        System.out.println("🔄 Recarga manual de borradores solicitada por el usuario");
        
        // Forzar limpieza y actualización inmediata
        Platform.runLater(() -> {
            System.out.println("🧹 Limpiando lista de borradores...");
            listaBorradores.clear();
            tablaBorradores.refresh();
            
            // Cargar nuevamente
            cargarBorradoresConReintentos(1); // Solo un intento para recarga manual
        });
    }

    /**
     * Método de debug para verificar el estado de la tabla
     */
    @FXML
    private void verificarEstadoTabla() {
        System.out.println("🔍 === DEBUG TABLA BORRADORES ===");
        System.out.println("📊 Elementos en lista observable: " + listaBorradores.size());
        System.out.println("🏢 Tabla visible: " + tablaBorradores.isVisible());
        System.out.println("📋 Items en tabla: " + tablaBorradores.getItems().size());
        System.out.println("🔗 Lista vinculada: " + (tablaBorradores.getItems() == listaBorradores));
        
        // Mostrar detalles de cada borrador
        for (int i = 0; i < listaBorradores.size(); i++) {
            ModeloFactura borrador = listaBorradores.get(i);
            System.out.println("  " + i + ": " + borrador.getNombreCliente() + " - " + 
                             (borrador.getId() != null ? borrador.getId().toString() : "Sin ID"));
        }
        
        // Forzar refresh completo
        Platform.runLater(() -> {
            tablaBorradores.refresh();
            tablaBorradores.requestLayout();
        });
        
        System.out.println("🔍 === FIN DEBUG ===");
    }

    /**
     * Diagnóstica el estado de la conexión y el servidor
     */
    private void diagnosticarConexion() {
        new Thread(() -> {
            try {
                System.out.println("🔍 === DIAGNÓSTICO DE CONEXIÓN ===");
                System.out.println("📡 GestorSocket: " + (gestorSocket != null ? "Inicializado" : "NULL"));
                
                if (gestorSocket != null) {
                    System.out.println("🔗 Conectado: " + gestorSocket.isConectado());
                    System.out.println("📥 Stream entrada: " + (gestorSocket.getEntrada() != null ? "OK" : "NULL"));
                    System.out.println("📤 Stream salida: " + (gestorSocket.getSalida() != null ? "OK" : "NULL"));
                    
                    // Intentar una petición simple para probar la conexión
                    if (gestorSocket.isConectado()) {
                        System.out.println("🧪 Probando conexión con petición simple...");
                        
                        try {
                            synchronized (gestorSocket) {
                                // Intentar obtener todas las facturas como prueba
                                gestorSocket.enviarPeticion(String.valueOf(Protocolo.OBTENER_TODAS_FACTURAS));
                                
                                ObjectInputStream entrada = gestorSocket.getEntrada();
                                if (entrada != null) {
                                    int codigo = entrada.readInt();
                                    System.out.println("✅ Respuesta del servidor: " + codigo);
                                    
                                    if (codigo == Protocolo.OBTENER_TODAS_FACTURAS_RESPONSE) {
                                        @SuppressWarnings("unchecked")
                                        List<ModeloFactura> facturas = (List<ModeloFactura>) entrada.readObject();
                                        System.out.println("✅ Conexión OK - Facturas recibidas: " + (facturas != null ? facturas.size() : 0));
                                    }
                                } else {
                                    System.err.println("❌ Stream de entrada es NULL");
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Error en prueba de conexión: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("❌ GestorSocket no está inicializado");
                }
                
                System.out.println("🔍 === FIN DIAGNÓSTICO ===");
                
            } catch (Exception e) {
                System.err.println("❌ Error en diagnóstico: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Muestra información del estado del sistema al usuario
     */
    @FXML
    private void mostrarEstadoSistema() {
        StringBuilder estado = new StringBuilder();
        estado.append("📊 ESTADO DEL SISTEMA DE FACTURACIÓN\n\n");
        
        // Estado de la conexión
        estado.append("🔗 Conexión: ");
        if (gestorSocket != null && gestorSocket.isConectado()) {
            estado.append("✅ Conectado\n");
        } else {
            estado.append("❌ Desconectado\n");
        }
        
        // Estado de los datos
        estado.append("📋 Facturas cargadas: ").append(listaFacturas.size()).append("\n");
        estado.append("📝 Borradores cargados: ").append(listaBorradores.size()).append("\n");
        
        // Usuario actual
        estado.append("👤 Usuario: ");
        if (usuarioActual != null) {
            estado.append(usuarioActual.getNombre()).append(" (").append(usuarioActual.getRol()).append(")\n");
        } else {
            estado.append("No identificado\n");
        }
        
        estado.append("\n💡 Si hay problemas de conexión, use los botones de recarga o reinicie la aplicación.");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Estado del Sistema");
        alert.setHeaderText("Información del Sistema de Facturación");
        alert.setContentText(estado.toString());
        alert.showAndWait();
    }

    /**
     * Restablece el inventario de todos los medicamentos de una factura en el servidor de inventario
     * @param factura La factura cuyos medicamentos se van a restablecer
     */
    private void restablecerInventarioFactura(ModeloFactura factura) {
        if (factura.getMedicamentos() == null || factura.getMedicamentos().isEmpty()) {
            System.out.println("ℹ️ La factura no tiene medicamentos para restablecer");
            return;
        }
        
        System.out.println("🔄 Restableciendo inventario de " + factura.getMedicamentos().size() + " medicamentos...");
        
        try {
            // Obtener instancia del gestor de inventario
            Utilidades1.GestorSocketInventario gestorInventario = 
                Utilidades1.GestorSocketInventario.getInstance();
            
            // Verificar conexión al servidor de inventario
            if (!gestorInventario.isConectado()) {
                System.out.println("🔗 Conectando al servidor de inventario para restablecimiento...");
                boolean conectado = gestorInventario.conectarAlServidorInventario().get();
                if (!conectado) {
                    System.err.println("❌ No se pudo conectar al servidor de inventario");
                    Platform.runLater(() -> {
                        mostrarError("Error de conexión", 
                            "No se pudo conectar al servidor de inventario (puerto 50005).\n" +
                            "El borrador se eliminará, pero el inventario debe restablecerse manualmente.");
                    });
                    return;
                }
            }
            
            // Restablecer cada medicamento
            for (ModeloFactura.ConceptoFactura medicamento : factura.getMedicamentos()) {
                String codigoProducto = extraerCodigoProducto(medicamento.getDescripcion());
                
                if (codigoProducto != null && !codigoProducto.isEmpty()) {
                    // Construir mensaje de restablecimiento
                    String idMensaje = "MSG_RESTORE_DELETE_" + System.currentTimeMillis();
                    String facturaId = factura.getId() != null ? factura.getId().toString() : "UNKNOWN";
                    
                    String mensajeRestablecimiento = Utilidades1.ProtocoloInventarioVeterinaria.construirMensaje(
                        Utilidades1.ProtocoloInventarioVeterinaria.RESTABLECER_INVENTARIO,
                        facturaId,
                        codigoProducto,
                        String.valueOf(medicamento.getCantidad()),
                        idMensaje
                    );
                    
                    System.out.println("📤 Restableciendo: " + medicamento.getDescripcion() + 
                                     " (Código: " + codigoProducto + ", Cantidad: " + medicamento.getCantidad() + ")");
                    
                    // Enviar petición
                    gestorInventario.enviarPeticion(mensajeRestablecimiento);
                    
                    // Leer respuesta
                    String respuesta = gestorInventario.leerRespuesta();
                    System.out.println("📥 Respuesta: " + respuesta);
                    
                    // Parsear respuesta
                    String[] partesRespuesta = Utilidades1.ProtocoloInventarioVeterinaria.parsearMensaje(respuesta);
                    
                    if (partesRespuesta.length >= 3) {
                        int codigoRespuesta = Integer.parseInt(partesRespuesta[0]);
                        if (codigoRespuesta == Utilidades1.ProtocoloInventarioVeterinaria.RESTABLECER_INVENTARIO_RESPONSE) {
                            System.out.println("✅ Inventario restablecido para: " + medicamento.getDescripcion());
                        } else {
                            System.out.println("⚠️ Error al restablecer inventario para: " + medicamento.getDescripcion());
                        }
                    }
                    
                    // Pequeña pausa entre medicamentos para no saturar el servidor
                    Thread.sleep(100);
                    
                } else {
                    System.out.println("⚠️ No se pudo extraer código de producto para: " + medicamento.getDescripcion());
                }
            }
            
            System.out.println("✅ Proceso de restablecimiento de inventario completado");
            
        } catch (Exception e) {
            System.err.println("❌ Error al restablecer inventario: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                mostrarError("Error de inventario", 
                    "Error al restablecer el inventario:\n" + e.getMessage() + 
                    "\n\nEl borrador se eliminará, pero verifique el inventario manualmente.");
            });
        }
    }
    
    /**
     * Extrae el código del producto desde la descripción del medicamento
     * Busca patrones comunes de códigos de producto
     */
    private String extraerCodigoProducto(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        
        // Buscar códigos entre corchetes al inicio [CODIGO]
        if (descripcion.startsWith("[") && descripcion.contains("]")) {
            int finCorchete = descripcion.indexOf("]");
            if (finCorchete > 1) {
                String codigo = descripcion.substring(1, finCorchete);
                if (codigo.matches("\\d+")) {
                    return codigo;
                }
            }
        }
        
        // Buscar patrones comunes de códigos de producto (números al inicio)
        if (descripcion.matches("^\\d+.*")) {
            String codigo = descripcion.replaceAll("^(\\d+).*", "$1");
            return codigo;
        }
        
        // Buscar códigos entre paréntesis
        if (descripcion.contains("(") && descripcion.contains(")")) {
            String codigo = descripcion.replaceAll(".*\\((\\d+)\\).*", "$1");
            if (!codigo.equals(descripcion)) {
                return codigo;
            }
        }
        
        // Si no se encuentra un patrón, intentar extraer cualquier secuencia de números larga
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{6,}");
        java.util.regex.Matcher matcher = pattern.matcher(descripcion);
        if (matcher.find()) {
            return matcher.group();
        }
        
        System.out.println("⚠️ No se pudo determinar el código de producto de: " + descripcion);
        return null;
    }
} 