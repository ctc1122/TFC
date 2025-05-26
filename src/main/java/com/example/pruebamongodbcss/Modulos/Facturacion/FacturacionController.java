package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import com.example.pruebamongodbcss.theme.ThemeManager;
import com.jfoenix.controls.*;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.bson.types.ObjectId;

import java.io.File;
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
    @FXML private BorderPane rootPane;
    @FXML private TabPane tabPane;
    
    // Tab de listado de facturas
    @FXML private Tab tabListado;
    @FXML private VBox vboxListado;
    @FXML private HBox hboxFiltros;
    @FXML private JFXTextField txtFiltroNumero;
    @FXML private JFXComboBox<String> cmbFiltroEstado;
    @FXML private JFXDatePicker dpFechaInicio;
    @FXML private JFXDatePicker dpFechaFin;
    @FXML private JFXButton btnBuscar;
    @FXML private JFXButton btnLimpiarFiltros;
    @FXML private JFXButton btnNuevaFactura;
    @FXML private TableView<ModeloFactura> tableFacturas;
    @FXML private TableColumn<ModeloFactura, String> colNumero;
    @FXML private TableColumn<ModeloFactura, String> colFecha;
    @FXML private TableColumn<ModeloFactura, String> colCliente;
    @FXML private TableColumn<ModeloFactura, String> colPaciente;
    @FXML private TableColumn<ModeloFactura, String> colEstado;
    @FXML private TableColumn<ModeloFactura, String> colTotal;
    @FXML private TableColumn<ModeloFactura, Void> colAcciones;
    
    // Tab de estadísticas
    @FXML private Tab tabEstadisticas;
    @FXML private VBox vboxEstadisticas;
    @FXML private JFXComboBox<Integer> cmbAño;
    @FXML private Label lblTotalFacturado;
    @FXML private Label lblNumeroFacturas;
    @FXML private Label lblPromedioFactura;
    
    // Tab de borradores
    @FXML private Tab tabBorradores;
    @FXML private VBox vboxBorradores;
    @FXML private TableView<ModeloFactura> tableBorradores;
    
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
            cargarFacturas();
            cargarBorradores();
            
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
        colEstado.setCellValueFactory(cellData -> {
            ModeloFactura.EstadoFactura estado = cellData.getValue().getEstado();
            return new SimpleStringProperty(estado != null ? estado.getDescripcion() : "");
        });
        colTotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        // Configurar columna de acciones
        configurarColumnaAcciones();
        
        // Asignar datos
        tableFacturas.setItems(listaFacturas);
        
        // Configurar selección
        tableFacturas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Habilitar botones según el estado de la factura
                actualizarEstadoBotones(newSelection);
            }
        });
        
        // Hacer la tabla responsive
        tableFacturas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarTablaBorradores() {
        // Clonar configuración de la tabla principal
        TableColumn<ModeloFactura, String> colNumeroBorrador = new TableColumn<>("Número");
        TableColumn<ModeloFactura, String> colFechaBorrador = new TableColumn<>("Fecha");
        TableColumn<ModeloFactura, String> colClienteBorrador = new TableColumn<>("Cliente");
        TableColumn<ModeloFactura, String> colPacienteBorrador = new TableColumn<>("Paciente");
        TableColumn<ModeloFactura, String> colTotalBorrador = new TableColumn<>("Total");
        TableColumn<ModeloFactura, Void> colAccionesBorrador = new TableColumn<>("Acciones");
        
        // Configurar cell value factories
        colNumeroBorrador.setCellValueFactory(cellData -> {
            String numero = cellData.getValue().getNumeroFactura();
            return new SimpleStringProperty(numero != null ? numero : "BORRADOR");
        });
        colFechaBorrador.setCellValueFactory(cellData -> {
            Date fecha = cellData.getValue().getFechaCreacion();
            return new SimpleStringProperty(fecha != null ? formatoFecha.format(fecha) : "");
        });
        colClienteBorrador.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colPacienteBorrador.setCellValueFactory(new PropertyValueFactory<>("nombrePaciente"));
        colTotalBorrador.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        // Configurar acciones para borradores
        colAccionesBorrador.setCellFactory(new Callback<TableColumn<ModeloFactura, Void>, TableCell<ModeloFactura, Void>>() {
            @Override
            public TableCell<ModeloFactura, Void> call(TableColumn<ModeloFactura, Void> param) {
                return new TableCell<ModeloFactura, Void>() {
                    private final JFXButton btnEditar = new JFXButton("Editar");
                    private final JFXButton btnEliminar = new JFXButton("Eliminar");
                    private final HBox hbox = new HBox(5, btnEditar, btnEliminar);
                    
                    {
                        btnEditar.getStyleClass().addAll("btn-primary", "btn-sm");
                        btnEliminar.getStyleClass().addAll("btn-danger", "btn-sm");
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
        
        // Agregar columnas a la tabla
        tableBorradores.getColumns().addAll(colNumeroBorrador, colFechaBorrador, 
                                          colClienteBorrador, colPacienteBorrador, 
                                          colTotalBorrador, colAccionesBorrador);
        
        // Asignar datos
        tableBorradores.setItems(listaBorradores);
        tableBorradores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(new Callback<TableColumn<ModeloFactura, Void>, TableCell<ModeloFactura, Void>>() {
            @Override
            public TableCell<ModeloFactura, Void> call(TableColumn<ModeloFactura, Void> param) {
                return new TableCell<ModeloFactura, Void>() {
                    private final JFXButton btnVer = new JFXButton("Ver");
                    private final JFXButton btnPDF = new JFXButton("PDF");
                    private final JFXButton btnEditar = new JFXButton("Editar");
                    private final HBox hbox = new HBox(5, btnVer, btnPDF, btnEditar);
                    
                    {
                        btnVer.getStyleClass().addAll("btn-info", "btn-sm");
                        btnPDF.getStyleClass().addAll("btn-success", "btn-sm");
                        btnEditar.getStyleClass().addAll("btn-primary", "btn-sm");
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
        cmbFiltroEstado.getItems().addAll(
            "Todos",
            "Borrador",
            "Emitida", 
            "Pagada",
            "Vencida",
            "Anulada"
        );
        cmbFiltroEstado.setValue("Todos");
        
        // Configurar fechas por defecto (último mes)
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.minusMonths(1));
        dpFechaFin.setValue(hoy);
    }
    
    /**
     * Configura la sección de estadísticas
     */
    private void configurarEstadisticas() {
        // Configurar combo de años
        int añoActual = LocalDate.now().getYear();
        for (int i = añoActual; i >= añoActual - 5; i--) {
            cmbAño.getItems().add(i);
        }
        cmbAño.setValue(añoActual);
        
        // Cargar estadísticas iniciales
        cargarEstadisticas();
    }
    
    /**
     * Configura los eventos de los botones
     */
    private void configurarEventos() {
        btnBuscar.setOnAction(e -> buscarFacturas());
        btnLimpiarFiltros.setOnAction(e -> limpiarFiltros());
        btnNuevaFactura.setOnAction(e -> crearNuevaFactura());
        cmbAño.setOnAction(e -> cargarEstadisticas());
    }
    
    /**
     * Aplica el tema actual
     */
    private void aplicarTema() {
        if (ThemeManager.getInstance().isDarkTheme()) {
            rootPane.getStyleClass().add("dark-theme");
        } else {
            rootPane.getStyleClass().remove("dark-theme");
        }
    }
    
    /**
     * Carga todas las facturas desde el servidor
     */
    private void cargarFacturas() {
        new Thread(() -> {
            try {
                String peticion = String.valueOf(Protocolo.OBTENER_TODAS_FACTURAS);
                gestorSocket.enviarPeticion(peticion);
                
                ObjectInputStream entrada = gestorSocket.getEntrada();
                int codigoRespuesta = entrada.readInt();
                
                if (codigoRespuesta == Protocolo.OBTENER_TODAS_FACTURAS_RESPONSE) {
                    @SuppressWarnings("unchecked")
                    List<ModeloFactura> facturas = (List<ModeloFactura>) entrada.readObject();
                    
                    Platform.runLater(() -> {
                        listaFacturas.clear();
                        listaFacturas.addAll(facturas);
                    });
                } else {
                    Platform.runLater(() -> mostrarError("Error", "No se pudieron cargar las facturas"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error", "Error de comunicación: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * Carga los borradores desde el servidor
     */
    private void cargarBorradores() {
        new Thread(() -> {
            try {
                String peticion = String.valueOf(Protocolo.OBTENER_FACTURAS_BORRADOR);
                gestorSocket.enviarPeticion(peticion);
                
                ObjectInputStream entrada = gestorSocket.getEntrada();
                int codigoRespuesta = entrada.readInt();
                
                if (codigoRespuesta == Protocolo.OBTENER_FACTURAS_BORRADOR_RESPONSE) {
                    @SuppressWarnings("unchecked")
                    List<ModeloFactura> borradores = (List<ModeloFactura>) entrada.readObject();
                    
                    Platform.runLater(() -> {
                        listaBorradores.clear();
                        listaBorradores.addAll(borradores);
                    });
                } else {
                    Platform.runLater(() -> mostrarError("Error", "No se pudieron cargar los borradores"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error", "Error de comunicación: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * Busca facturas con filtros
     */
    private void buscarFacturas() {
        new Thread(() -> {
            try {
                // Preparar parámetros de búsqueda
                String numeroFactura = txtFiltroNumero.getText().trim();
                String estado = cmbFiltroEstado.getValue();
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
                Platform.runLater(() -> mostrarError("Error", "Error en la búsqueda: " + e.getMessage()));
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
    private void limpiarFiltros() {
        txtFiltroNumero.clear();
        cmbFiltroEstado.setValue("Todos");
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.minusMonths(1));
        dpFechaFin.setValue(hoy);
        cargarFacturas();
    }
    
    /**
     * Carga las estadísticas del año seleccionado
     */
    private void cargarEstadisticas() {
        Integer año = cmbAño.getValue();
        if (año == null) return;
        
        // Calcular estadísticas de las facturas cargadas
        double totalFacturado = listaFacturas.stream()
            .filter(f -> !f.isEsBorrador())
            .filter(f -> {
                if (f.getFechaEmision() != null) {
                    LocalDate fecha = f.getFechaEmision().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return fecha.getYear() == año;
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
                    return fecha.getYear() == año;
                }
                return false;
            })
            .count();
        
        double promedioFactura = numeroFacturas > 0 ? totalFacturado / numeroFacturas : 0;
        
        // Actualizar labels
        lblTotalFacturado.setText(formatoMoneda.format(totalFacturado));
        lblNumeroFacturas.setText(String.valueOf(numeroFacturas));
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
        
        Stage stage = (Stage) rootPane.getScene().getWindow();
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
        cargarFacturas();
        cargarBorradores();
        cargarEstadisticas();
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
} 