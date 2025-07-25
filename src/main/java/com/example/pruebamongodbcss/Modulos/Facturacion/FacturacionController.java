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
        // Obtener el año actual
        int anoActual = LocalDate.now().getYear();
        
        // Configurar combo de periodos dinámicamente con el año actual y los 4 años anteriores
        cmbPeriodo.getItems().clear();
        for (int i = 0; i < 5; i++) {
            int ano = anoActual - i;
            cmbPeriodo.getItems().add(String.valueOf(ano));
        }
        
        // Establecer el año actual como valor por defecto
        cmbPeriodo.setValue(String.valueOf(anoActual));
        
        System.out.println("📅 Combo de periodos configurado con años: " + cmbPeriodo.getItems());
        System.out.println("📅 Año seleccionado por defecto: " + anoActual);
        
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
     * Aplica el tema actual de forma compatible con el ThemeManager global
     */
    private void aplicarTema() {
        try {
            System.out.println("🎨 Aplicando tema al módulo de facturación...");
            
            // Verificar que tenemos una Scene disponible
            if (mainPane.getScene() == null) {
                System.out.println("⚠️ Scene no disponible aún, aplazando aplicación de tema");
                return;
            }
            
            // PASO 1: Cargar CSS específico de facturación SOLO una vez
            String facturacionCSS = getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/facturacion-styles.css").toExternalForm();
            
            // Verificar si el CSS ya está cargado para evitar duplicados
            boolean cssYaCargado = mainPane.getScene().getStylesheets().contains(facturacionCSS);
            if (!cssYaCargado) {
                mainPane.getScene().getStylesheets().add(facturacionCSS);
                System.out.println("✅ CSS de facturación cargado: " + facturacionCSS);
            } else {
                System.out.println("ℹ️ CSS de facturación ya estaba cargado");
            }
            
            // PASO 2: Aplicar clase identificadora del módulo
            if (!mainPane.getStyleClass().contains("facturacion-module")) {
                mainPane.getStyleClass().add("facturacion-module");
                System.out.println("✅ Clase 'facturacion-module' agregada");
            }
            
            // PASO 3: Gestionar tema oscuro/claro de manera reactiva
            Platform.runLater(() -> {
                if (ThemeManager.getInstance().isDarkTheme()) {
                    if (!mainPane.getStyleClass().contains("dark-theme")) {
                        mainPane.getStyleClass().add("dark-theme");
                        System.out.println("🌙 Tema oscuro aplicado al módulo de facturación");
                    }
                } else {
                    if (mainPane.getStyleClass().contains("dark-theme")) {
                        mainPane.getStyleClass().remove("dark-theme");
                        System.out.println("☀️ Tema claro aplicado al módulo de facturación");
                    }
                }
                
                // PASO 4: Forzar actualización visual de botones
                refrescarBotones();
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error al aplicar tema en facturación: " + e.getMessage());
            e.printStackTrace();
            // Aplicar solo el tema básico si hay error
            Platform.runLater(() -> {
                if (ThemeManager.getInstance().isDarkTheme()) {
                    mainPane.getStyleClass().add("dark-theme");
                } else {
                    mainPane.getStyleClass().remove("dark-theme");
                }
            });
        }
    }
    
    /**
     * Fuerza la actualización visual de todos los botones del módulo
     */
    private void refrescarBotones() {
        try {
            // Buscar todos los botones en el módulo y forzar su actualización
            mainPane.lookupAll(".button").forEach(node -> {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    // Forzar recálculo de estilos
                    btn.applyCss();
                    btn.autosize();
                }
            });
            
            // También actualizar las tablas
            if (tablaFacturas != null) {
                tablaFacturas.refresh();
            }
            if (tablaBorradores != null) {
                tablaBorradores.refresh();
            }
            
            System.out.println("🔄 Botones y tablas actualizados");
            
        } catch (Exception e) {
            System.err.println("⚠️ Error al refrescar botones: " + e.getMessage());
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
     * Carga las estadísticas del periodo seleccionado desde el servidor
     */
    private void cargarEstadisticas() {
        String periodo = cmbPeriodo.getValue();
        if (periodo == null) return;
        
        System.out.println("🔄 Cargando estadísticas desde el servidor para el periodo: " + periodo);
        
        new Thread(() -> {
            GestorSocket gestorSocketEstadisticas = null;
            try {
                // Crear conexión independiente para estadísticas
                gestorSocketEstadisticas = GestorSocket.crearConexionIndependiente();
                
                if (!gestorSocketEstadisticas.isConectado()) {
                    Platform.runLater(() -> mostrarError("Error de conexión", "No hay conexión con el servidor para cargar estadísticas"));
                    return;
                }
                
                // PASO 1: Cargar estadísticas básicas
                System.out.println("📊 Solicitando estadísticas básicas...");
                String peticionEstadisticas = Protocolo.OBTENER_ESTADISTICAS_FACTURACION + "|" + periodo;
                gestorSocketEstadisticas.enviarPeticion(peticionEstadisticas);
                
                ObjectInputStream entrada = gestorSocketEstadisticas.getEntrada();
                int codigoRespuesta1 = entrada.readInt();
                
                if (codigoRespuesta1 == Protocolo.OBTENER_ESTADISTICAS_FACTURACION_RESPONSE) {
                    com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.EstadisticasFacturacion estadisticas = 
                        (com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.EstadisticasFacturacion) entrada.readObject();
                    
                    // PASO 2: Cargar datos para gráfico de estados
                    System.out.println("📈 Solicitando datos para gráfico de estados...");
                    String peticionEstados = Protocolo.OBTENER_DATOS_GRAFICO_ESTADOS_FACTURAS + "|" + periodo;
                    gestorSocketEstadisticas.enviarPeticion(peticionEstados);
                    
                    int codigoRespuesta2 = gestorSocketEstadisticas.getEntrada().readInt();
                    List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosEstados = null;
                    
                    if (codigoRespuesta2 == Protocolo.OBTENER_DATOS_GRAFICO_ESTADOS_FACTURAS_RESPONSE) {
                        datosEstados = (List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico>) 
                            gestorSocketEstadisticas.getEntrada().readObject();
                    }
                    
                    // PASO 3: Cargar datos para gráfico de ingresos mensuales
                    System.out.println("📊 Solicitando datos para gráfico de ingresos...");
                    String peticionIngresos = Protocolo.OBTENER_DATOS_GRAFICO_INGRESOS_MENSUALES + "|" + periodo;
                    gestorSocketEstadisticas.enviarPeticion(peticionIngresos);
                    
                    int codigoRespuesta3 = gestorSocketEstadisticas.getEntrada().readInt();
                    List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosIngresos = null;
                    
                    if (codigoRespuesta3 == Protocolo.OBTENER_DATOS_GRAFICO_INGRESOS_MENSUALES_RESPONSE) {
                        datosIngresos = (List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico>) 
                            gestorSocketEstadisticas.getEntrada().readObject();
                    }
                    
                    // PASO 4: Actualizar la interfaz en el hilo principal
                    final List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosEstadosFinal = datosEstados;
                    final List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosIngresosFinal = datosIngresos;
                    
                    Platform.runLater(() -> {
                        // Actualizar labels con las estadísticas básicas
                        lblTotalFacturas.setText(String.valueOf(estadisticas.getTotalFacturas()));
                        lblIngresosTotales.setText(formatoMoneda.format(estadisticas.getIngresosTotales()));
                        lblFacturasPendientes.setText(String.valueOf(estadisticas.getFacturasPendientes()));
                        lblPromedioFactura.setText(formatoMoneda.format(estadisticas.getPromedioFactura()));
                        
                        // Actualizar gráfico de estados (PieChart)
                        if (datosEstadosFinal != null && !datosEstadosFinal.isEmpty()) {
                            chartEstados.getData().clear();
                            for (com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico dato : datosEstadosFinal) {
                                javafx.scene.chart.PieChart.Data slice = new javafx.scene.chart.PieChart.Data(
                                    dato.getEtiqueta(), dato.getValor());
                                chartEstados.getData().add(slice);
                            }
                            
                            // Agregar tooltips al gráfico circular
                            Platform.runLater(() -> {
                                chartEstados.getData().forEach(data -> {
                                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                                        data.getName() + ": " + (int)data.getPieValue() + " facturas"
                                    );
                                    tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: white;");
                                    javafx.scene.control.Tooltip.install(data.getNode(), tooltip);
                                });
                            });
                            
                            System.out.println("✅ Gráfico de estados actualizado con " + datosEstadosFinal.size() + " elementos");
                        } else {
                            chartEstados.getData().clear();
                            System.out.println("ℹ️ No hay datos para el gráfico de estados");
                        }
                        
                        // Actualizar gráfico de ingresos (LineChart)
                        if (datosIngresosFinal != null && !datosIngresosFinal.isEmpty()) {
                            chartIngresos.getData().clear();
                            javafx.scene.chart.XYChart.Series<String, Number> serie = new javafx.scene.chart.XYChart.Series<>();
                            serie.setName("Ingresos " + periodo);
                            
                            for (com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico dato : datosIngresosFinal) {
                                serie.getData().add(new javafx.scene.chart.XYChart.Data<>(dato.getEtiqueta(), dato.getValor()));
                            }
                            
                            chartIngresos.getData().add(serie);
                            
                            // Agregar tooltips al gráfico de líneas
                            Platform.runLater(() -> {
                                for (javafx.scene.chart.XYChart.Data<String, Number> data : serie.getData()) {
                                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                                        data.getXValue() + ": " + formatoMoneda.format(data.getYValue())
                                    );
                                    tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: white;");
                                    javafx.scene.control.Tooltip.install(data.getNode(), tooltip);
                                }
                            });
                            
                            System.out.println("✅ Gráfico de ingresos actualizado con " + datosIngresosFinal.size() + " puntos");
                        } else {
                            chartIngresos.getData().clear();
                            System.out.println("ℹ️ No hay datos para el gráfico de ingresos");
                        }
                        
                        System.out.println("✅ Estadísticas cargadas exitosamente desde el servidor");
                    });
                    
                } else {
                    System.err.println("❌ Error al obtener estadísticas básicas: " + codigoRespuesta1);
                    Platform.runLater(() -> mostrarError("Error", "No se pudieron cargar las estadísticas básicas"));
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error al cargar estadísticas: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error", "Error al cargar estadísticas: " + e.getMessage()));
            } finally {
                // Cerrar la conexión independiente
                if (gestorSocketEstadisticas != null) {
                    try {
                        gestorSocketEstadisticas.cerrarConexion();
                        System.out.println("🔌 Conexión independiente para estadísticas cerrada");
                    } catch (Exception e) {
                        System.err.println("⚠️ Error al cerrar conexión de estadísticas: " + e.getMessage());
                    }
                }
            }
        }).start();
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
        String periodo = cmbPeriodo.getValue();
        if (periodo == null) {
            mostrarError("Error", "Debe seleccionar un periodo para exportar");
            return;
        }
        
        // Crear diálogo de selección de formato
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exportar Estadísticas");
        alert.setHeaderText("Seleccionar formato de exportación");
        alert.setContentText("¿En qué formato desea exportar las estadísticas?");
        
        ButtonType btnExcel = new ButtonType("Excel (.xlsx)");
        ButtonType btnPDF = new ButtonType("PDF (.pdf)");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());
        
        alert.getButtonTypes().setAll(btnExcel, btnPDF, btnCancelar);
        
        Optional<ButtonType> resultado = alert.showAndWait();
        
        if (resultado.isPresent() && resultado.get() != btnCancelar) {
            boolean esExcel = resultado.get() == btnExcel;
            String extension = esExcel ? ".xlsx" : ".pdf";
            String descripcion = esExcel ? "Archivos Excel" : "Archivos PDF";
            
            // Selector de archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Estadísticas");
            fileChooser.setInitialFileName("Estadisticas_Facturacion_" + periodo + extension);
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(descripcion, "*" + extension)
            );
            
            Stage stage = (Stage) mainPane.getScene().getWindow();
            File archivo = fileChooser.showSaveDialog(stage);
            
            if (archivo != null) {
                exportarEstadisticasAArchivo(archivo.getAbsolutePath(), periodo, esExcel);
            }
        }
    }
    
    /**
     * Exporta las estadísticas al archivo especificado
     */
    private void exportarEstadisticasAArchivo(String rutaArchivo, String periodo, boolean esExcel) {
        new Thread(() -> {
            try {
                System.out.println("📊 Iniciando exportación de estadísticas...");
                
                // Obtener datos actuales (reutilizar la lógica de cargarEstadisticas)
                com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.EstadisticasFacturacion estadisticas = null;
                java.util.List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosEstados = null;
                java.util.List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosIngresos = null;
                
                // Crear conexión independiente para obtener datos
                GestorSocket gestorExport = GestorSocket.crearConexionIndependiente();
                
                try {
                    // Obtener estadísticas básicas
                    String peticionEstadisticas = Protocolo.OBTENER_ESTADISTICAS_FACTURACION + "|" + periodo;
                    gestorExport.enviarPeticion(peticionEstadisticas);
                    
                    ObjectInputStream entrada = gestorExport.getEntrada();
                    int codigoRespuesta1 = entrada.readInt();
                    
                    if (codigoRespuesta1 == Protocolo.OBTENER_ESTADISTICAS_FACTURACION_RESPONSE) {
                        estadisticas = (com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.EstadisticasFacturacion) entrada.readObject();
                        
                        // Obtener datos para gráfico de estados
                        String peticionEstados = Protocolo.OBTENER_DATOS_GRAFICO_ESTADOS_FACTURAS + "|" + periodo;
                        gestorExport.enviarPeticion(peticionEstados);
                        
                        int codigoRespuesta2 = gestorExport.getEntrada().readInt();
                        if (codigoRespuesta2 == Protocolo.OBTENER_DATOS_GRAFICO_ESTADOS_FACTURAS_RESPONSE) {
                            datosEstados = (java.util.List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico>) 
                                gestorExport.getEntrada().readObject();
                        }
                        
                        // Obtener datos para gráfico de ingresos
                        String peticionIngresos = Protocolo.OBTENER_DATOS_GRAFICO_INGRESOS_MENSUALES + "|" + periodo;
                        gestorExport.enviarPeticion(peticionIngresos);
                        
                        int codigoRespuesta3 = gestorExport.getEntrada().readInt();
                        if (codigoRespuesta3 == Protocolo.OBTENER_DATOS_GRAFICO_INGRESOS_MENSUALES_RESPONSE) {
                            datosIngresos = (java.util.List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico>) 
                                gestorExport.getEntrada().readObject();
                        }
                    }
                    
                } finally {
                    gestorExport.cerrarConexion();
                }
                
                // Verificar que tenemos todos los datos
                if (estadisticas == null || datosEstados == null || datosIngresos == null) {
                    Platform.runLater(() -> mostrarError("Error", "No se pudieron obtener todos los datos para la exportación"));
                    return;
                }
                
                // Exportar según el formato
                if (esExcel) {
                    exportarAExcelSimple(rutaArchivo, periodo, estadisticas, datosEstados, datosIngresos);
                } else {
                    exportarAPDFDirecto(rutaArchivo, periodo, estadisticas, datosEstados, datosIngresos);
                }
                
                Platform.runLater(() -> {
                    mostrarInfo("Éxito", "Estadísticas exportadas correctamente a: " + rutaArchivo);
                });
                
                System.out.println("✅ Exportación completada: " + rutaArchivo);
                
            } catch (Exception e) {
                System.err.println("❌ Error en exportación: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    mostrarError("Error de exportación", "Error al exportar estadísticas: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Exporta a Excel usando POI básico (evita problemas de acceso con XSSFWorkbook)
     */
    private void exportarAExcelSimple(String rutaArchivo, String periodo, 
                                     com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.EstadisticasFacturacion estadisticas,
                                     java.util.List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosEstados,
                                     java.util.List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosIngresos) throws Exception {
        
        // Crear archivo de texto con formato tabular como alternativa
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(rutaArchivo.replace(".xlsx", ".txt")))) {
            writer.println("=".repeat(60));
            writer.println("ESTADÍSTICAS DE FACTURACIÓN - " + periodo);
            writer.println("Fecha de generación: " + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            writer.println("=".repeat(60));
            writer.println();
            
            // Resumen general
            writer.println("RESUMEN GENERAL");
            writer.println("-".repeat(30));
            writer.println("Total de Facturas: " + estadisticas.getTotalFacturas());
            writer.println("Ingresos Totales: " + formatoMoneda.format(estadisticas.getIngresosTotales()));
            writer.println("Facturas Pendientes (Borradores): " + estadisticas.getFacturasPendientes());
            writer.println("Promedio por Factura: " + formatoMoneda.format(estadisticas.getPromedioFactura()));
            writer.println();
            
            // Distribución por estados
            writer.println("DISTRIBUCIÓN POR ESTADOS");
            writer.println("-".repeat(30));
            writer.printf("%-20s %-10s %-10s%n", "Estado", "Cantidad", "Porcentaje");
            writer.println("-".repeat(42));
            
            double totalFacturas = datosEstados.stream().mapToDouble(com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico::getValor).sum();
            for (com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico dato : datosEstados) {
                double porcentaje = (dato.getValor() / totalFacturas) * 100;
                writer.printf("%-20s %-10d %-10.1f%%%n", dato.getEtiqueta(), (int)dato.getValor(), porcentaje);
            }
            writer.println();
            
            // Ingresos mensuales
            writer.println("INGRESOS MENSUALES");
            writer.println("-".repeat(30));
            writer.printf("%-10s %-15s%n", "Mes", "Ingresos");
            writer.println("-".repeat(27));
            
            for (com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico dato : datosIngresos) {
                writer.printf("%-10s %-15s%n", dato.getEtiqueta(), formatoMoneda.format(dato.getValor()));
            }
            
            writer.println();
            writer.println("=".repeat(60));
            writer.println("Reporte generado por Sistema de Gestión Veterinaria");
        }
        
        Platform.runLater(() -> {
            mostrarInfo("Formato Excel no disponible", 
                "Se ha generado un archivo de texto con formato tabular en lugar de Excel.\n" +
                "Archivo guardado como: " + rutaArchivo.replace(".xlsx", ".txt"));
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

    /**
     * Método público para actualizar el tema del módulo (llamado desde el sistema principal)
     */
    public void actualizarTema() {
        System.out.println("🔄 Actualizando tema del módulo de facturación desde sistema principal...");
        aplicarTema();
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
     * Exporta a PDF usando una alternativa simple de texto formateado
     */
    private void exportarAPDFDirecto(String rutaArchivo, String periodo,
                                     com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.EstadisticasFacturacion estadisticas,
                                     java.util.List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosEstados,
                                     java.util.List<com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico> datosIngresos) throws Exception {
        
        // Por simplicidad, generar un archivo de texto formateado como alternativa al PDF
        String rutaTexto = rutaArchivo.replace(".pdf", "_informe.txt");
        
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(rutaTexto))) {
            writer.println("████████████████████████████████████████████████████████████");
            writer.println("█                INFORME DE ESTADÍSTICAS                  █");
            writer.println("█                  FACTURACIÓN " + periodo + "                      █");
            writer.println("████████████████████████████████████████████████████████████");
            writer.println();
            writer.println("📅 Fecha: " + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            writer.println("🏥 Sistema de Gestión Veterinaria");
            writer.println();
            
            writer.println("┌─────────────────────────────────────────────────────────┐");
            writer.println("│                   📊 RESUMEN GENERAL                   │");
            writer.println("└─────────────────────────────────────────────────────────┘");
            writer.println();
            writer.printf("📋 Total de Facturas: %,d%n", estadisticas.getTotalFacturas());
            writer.printf("💰 Ingresos Totales: %s%n", formatoMoneda.format(estadisticas.getIngresosTotales()));
            writer.printf("⏳ Facturas Pendientes: %,d (Borradores)%n", estadisticas.getFacturasPendientes());
            writer.printf("📈 Promedio por Factura: %s%n", formatoMoneda.format(estadisticas.getPromedioFactura()));
            writer.println();
            
            writer.println("┌─────────────────────────────────────────────────────────┐");
            writer.println("│                🎯 DISTRIBUCIÓN POR ESTADOS             │");
            writer.println("└─────────────────────────────────────────────────────────┘");
            writer.println();
            writer.printf("%-20s │ %-10s │ %-12s%n", "Estado", "Cantidad", "Porcentaje");
            writer.println("─".repeat(50));
            
            double totalFacturas = datosEstados.stream().mapToDouble(com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico::getValor).sum();
            for (com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico dato : datosEstados) {
                double porcentaje = (dato.getValor() / totalFacturas) * 100;
                String emoji = obtenerEmojiEstado(dato.getEtiqueta());
                writer.printf("%-20s │ %-10d │ %8.1f%%%n", 
                    emoji + " " + dato.getEtiqueta(), (int)dato.getValor(), porcentaje);
            }
            writer.println();
            
            writer.println("┌─────────────────────────────────────────────────────────┐");
            writer.println("│                💹 INGRESOS MENSUALES                   │");
            writer.println("└─────────────────────────────────────────────────────────┘");
            writer.println();
            writer.printf("%-12s │ %-20s%n", "Mes", "Ingresos");
            writer.println("─".repeat(38));
            
            double totalIngresos = 0;
            for (com.example.pruebamongodbcss.Modulos.Informes.ServicioInformes.DatoGrafico dato : datosIngresos) {
                totalIngresos += dato.getValor();
                writer.printf("%-12s │ %s%n", dato.getEtiqueta(), formatoMoneda.format(dato.getValor()));
            }
            
            writer.println("─".repeat(38));
            writer.printf("%-12s │ %s%n", "TOTAL", formatoMoneda.format(totalIngresos));
            writer.println();
            
            writer.println("┌─────────────────────────────────────────────────────────┐");
            writer.println("│                    📋 INFORMACIÓN                      │");
            writer.println("└─────────────────────────────────────────────────────────┘");
            writer.println();
            writer.println("Este informe ha sido generado automáticamente por el");
            writer.println("Sistema de Gestión Veterinaria.");
            writer.println();
            writer.println("Para más información, contacte con el administrador.");
            writer.println();
            writer.println("████████████████████████████████████████████████████████████");
        }
        
        Platform.runLater(() -> {
            mostrarInfo("Informe generado", 
                "Se ha generado un informe detallado en formato texto.\n" +
                "Archivo guardado como: " + rutaTexto);
        });
    }
    
    /**
     * Obtiene emoji apropiado para cada estado
     */
    private String obtenerEmojiEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "borrador": return "📝";
            case "emitida": return "📄";
            case "pagada": return "✅";
            case "vencida": return "⚠️";
            case "anulada": return "❌";
            default: return "📋";
        }
    }
} 