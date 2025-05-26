package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import com.example.pruebamongodbcss.theme.ThemeManager;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import org.bson.types.ObjectId;

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
    @FXML private ComboBox<String> cmbClientes;
    @FXML private ComboBox<String> cmbEstados;
    @FXML private MFXDatePicker dpFechaInicio;
    @FXML private MFXDatePicker dpFechaFin;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;
    @FXML private TextField txtBuscar;
    @FXML private Button btnNuevaFactura;
    @FXML private Button btnVerFactura;
    @FXML private Button btnEditarFactura;
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
    @FXML private Button btnNuevoBorrador;
    @FXML private Button btnEditarBorrador;
    @FXML private Button btnFinalizarBorrador;
    @FXML private Button btnEliminarBorrador;
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
            
            // Cargar datos iniciales de forma secuencial para evitar interferencias
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
                    private final HBox hbox = new HBox(5, btnVer, btnPDF, btnEditar);
                    
                    {
                        btnVer.getStyleClass().add("btn-info");
                        btnPDF.getStyleClass().add("btn-success");
                        btnEditar.getStyleClass().add("btn-primary");
                        hbox.setAlignment(Pos.CENTER);
                        
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
                            editarFactura(factura);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            ModeloFactura factura = getTableView().getItems().get(getIndex());
                            // Mostrar/ocultar botones según el estado
                            btnEditar.setVisible(factura.isEsBorrador());
                            setGraphic(hbox);
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
        btnNuevaFactura.setOnAction(e -> onNuevaFactura());
        btnVerFactura.setOnAction(e -> onVerFactura());
        btnEditarFactura.setOnAction(e -> onEditarFactura());
        btnEliminarFactura.setOnAction(e -> onEliminarFactura());
        btnExportarPDF.setOnAction(e -> onExportarPDF());
        btnNuevoBorrador.setOnAction(e -> onNuevoBorrador());
        btnEditarBorrador.setOnAction(e -> onEditarBorrador());
        btnFinalizarBorrador.setOnAction(e -> onFinalizarBorrador());
        btnEliminarBorrador.setOnAction(e -> onEliminarBorrador());
        btnActualizarEstadisticas.setOnAction(e -> actualizarEstadisticas());
        btnExportarEstadisticas.setOnAction(e -> exportarEstadisticas());
        cmbPeriodo.setOnAction(e -> cargarEstadisticas());
    }
    
    /**
     * Aplica el tema actual
     */
    private void aplicarTema() {
        if (ThemeManager.getInstance().isDarkTheme()) {
            mainPane.getStyleClass().add("dark-theme");
        } else {
            mainPane.getStyleClass().remove("dark-theme");
        }
    }
    
    /**
     * Carga los datos iniciales de forma secuencial
     */
    private void cargarDatosIniciales() {
        new Thread(() -> {
            try {
                // Primero cargar facturas
                cargarFacturasSync();
                
                // Esperar un poco antes de cargar borradores
                Thread.sleep(500);
                
                // Luego cargar borradores
                cargarBorradoresSync();
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error", "Error al cargar datos iniciales: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * Carga facturas de forma síncrona
     */
    private void cargarFacturasSync() {
        try {
            // Verificar conexión antes de enviar petición
            if (!gestorSocket.isConectado()) {
                Platform.runLater(() -> mostrarError("Error de conexión", "No hay conexión con el servidor"));
                return;
            }
            
            String peticion = String.valueOf(Protocolo.OBTENER_TODAS_FACTURAS);
            System.out.println("Enviando petición de facturas: " + peticion);
            
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            if (entrada == null) {
                Platform.runLater(() -> mostrarError("Error", "No se pudo obtener el stream de entrada"));
                return;
            }
            
            System.out.println("Esperando respuesta del servidor para facturas...");
            int codigoRespuesta = entrada.readInt();
            System.out.println("Código de respuesta recibido para facturas: " + codigoRespuesta);
            
            if (codigoRespuesta == Protocolo.OBTENER_TODAS_FACTURAS_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloFactura> facturas = (List<ModeloFactura>) entrada.readObject();
                
                Platform.runLater(() -> {
                    listaFacturas.clear();
                    if (facturas != null) {
                        listaFacturas.addAll(facturas);
                        System.out.println("Facturas cargadas: " + facturas.size());
                    }
                });
            } else if (codigoRespuesta == Protocolo.ERROR_OBTENER_TODAS_FACTURAS) {
                Platform.runLater(() -> mostrarError("Error", "Error del servidor al obtener las facturas"));
            } else {
                Platform.runLater(() -> mostrarError("Error", "Respuesta inesperada del servidor para facturas: " + codigoRespuesta));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar facturas: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> mostrarError("Error", "Error al cargar facturas: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido")));
        }
    }
    
    /**
     * Carga borradores de forma síncrona
     */
    private void cargarBorradoresSync() {
        try {
            // Verificar conexión antes de enviar petición
            if (!gestorSocket.isConectado()) {
                Platform.runLater(() -> mostrarError("Error de conexión", "No hay conexión con el servidor"));
                return;
            }
            
            String peticion = String.valueOf(Protocolo.OBTENER_FACTURAS_BORRADOR);
            System.out.println("Enviando petición de borradores: " + peticion);
            
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            if (entrada == null) {
                Platform.runLater(() -> mostrarError("Error", "No se pudo obtener el stream de entrada"));
                return;
            }
            
            System.out.println("Esperando respuesta del servidor para borradores...");
            int codigoRespuesta = entrada.readInt();
            System.out.println("Código de respuesta recibido para borradores: " + codigoRespuesta);
            
            if (codigoRespuesta == Protocolo.OBTENER_FACTURAS_BORRADOR_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloFactura> borradores = (List<ModeloFactura>) entrada.readObject();
                
                Platform.runLater(() -> {
                    listaBorradores.clear();
                    if (borradores != null) {
                        listaBorradores.addAll(borradores);
                        System.out.println("Borradores cargados: " + borradores.size());
                    }
                });
            } else if (codigoRespuesta == Protocolo.ERROR_OBTENER_FACTURAS_BORRADOR) {
                Platform.runLater(() -> mostrarError("Error", "Error del servidor al obtener los borradores"));
            } else {
                Platform.runLater(() -> mostrarError("Error", "Respuesta inesperada del servidor para borradores: " + codigoRespuesta));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar borradores: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> mostrarError("Error", "Error al cargar borradores: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido")));
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
                e.printStackTrace();
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
     * Ve los detalles de una factura
     */
    private void verFactura(ModeloFactura factura) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/factura-detalle.fxml"));
            Parent root = loader.load();
            
            FacturaDetalleController controller = loader.getController();
            controller.setFactura(factura);
            
            Stage stage = new Stage();
            stage.setTitle("Detalle de Factura - " + factura.getNumeroFactura());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el detalle de la factura: " + e.getMessage());
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
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el editor de factura: " + e.getMessage());
        }
    }
    
    /**
     * Elimina un borrador
     */
    private void eliminarBorrador(ModeloFactura factura) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar borrador?");
        alert.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    String peticion = Protocolo.ELIMINAR_FACTURA + Protocolo.SEPARADOR_CODIGO + factura.getId().toString();
                    gestorSocket.enviarPeticion(peticion);
                    
                    ObjectInputStream entrada = gestorSocket.getEntrada();
                    int codigoRespuesta = entrada.readInt();
                    
                    if (codigoRespuesta == Protocolo.ELIMINAR_FACTURA_RESPONSE) {
                        Platform.runLater(() -> {
                            listaBorradores.remove(factura);
                            mostrarInfo("Éxito", "Borrador eliminado correctamente");
                        });
                    } else {
                        Platform.runLater(() -> mostrarError("Error", "No se pudo eliminar el borrador"));
                    }
                } catch (Exception e) {
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
        new Thread(() -> {
            try {
                cargarFacturasSync();
                Thread.sleep(300);
                cargarBorradoresSync();
                Platform.runLater(() -> cargarEstadisticas());
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error", "Error al actualizar listas: " + e.getMessage()));
            }
        }).start();
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
     * Carga borradores (método público)
     */
    public void cargarBorradores() {
        new Thread(() -> {
            try {
                cargarBorradoresSync();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error", "Error al cargar borradores: " + e.getMessage()));
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
    private void onNuevaFactura() {
        crearNuevaFactura();
    }

    @FXML
    private void onVerFactura() {
        ModeloFactura facturaSeleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        if (facturaSeleccionada != null) {
            verFactura(facturaSeleccionada);
        } else {
            mostrarError("Error", "Debe seleccionar una factura");
        }
    }

    @FXML
    private void onEditarFactura() {
        ModeloFactura facturaSeleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        if (facturaSeleccionada != null) {
            editarFactura(facturaSeleccionada);
        } else {
            mostrarError("Error", "Debe seleccionar una factura");
        }
    }

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
    private void onNuevoBorrador() {
        crearNuevaFactura();
    }

    @FXML
    private void onEditarBorrador() {
        ModeloFactura borradorSeleccionado = tablaBorradores.getSelectionModel().getSelectedItem();
        if (borradorSeleccionado != null) {
            editarFactura(borradorSeleccionado);
        } else {
            mostrarError("Error", "Debe seleccionar un borrador");
        }
    }

    @FXML
    private void onFinalizarBorrador() {
        ModeloFactura borradorSeleccionado = tablaBorradores.getSelectionModel().getSelectedItem();
        if (borradorSeleccionado != null) {
            // Implementar lógica para finalizar borrador
            mostrarInfo("Información", "Funcionalidad en desarrollo");
        } else {
            mostrarError("Error", "Debe seleccionar un borrador");
        }
    }

    @FXML
    private void onEliminarBorrador() {
        ModeloFactura borradorSeleccionado = tablaBorradores.getSelectionModel().getSelectedItem();
        if (borradorSeleccionado != null) {
            eliminarBorrador(borradorSeleccionado);
        } else {
            mostrarError("Error", "Debe seleccionar un borrador");
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
} 