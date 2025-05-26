package com.example.pruebamongodbcss.Modulos.Inventario;

import com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.ObjectInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controlador del buscador sofisticado de medicamentos
 * Permite buscar y filtrar medicamentos del inventario con múltiples criterios
 */
public class BuscadorMedicamentosController implements Initializable {
    
    // Componentes de búsqueda y filtros
    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cmbLaboratorio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbFormaFarmaceutica;
    @FXML private CheckBox chkSoloConStock;
    @FXML private Spinner<Integer> spnStockMinimo;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;
    @FXML private Button btnActualizar;
    
    // Tabla de resultados
    @FXML private TableView<ModeloMedicamentoInventario> tablaMedicamentos;
    @FXML private TableColumn<ModeloMedicamentoInventario, String> colCodigo;
    @FXML private TableColumn<ModeloMedicamentoInventario, String> colNombre;
    @FXML private TableColumn<ModeloMedicamentoInventario, String> colLaboratorio;
    @FXML private TableColumn<ModeloMedicamentoInventario, String> colPrincipioActivo;
    @FXML private TableColumn<ModeloMedicamentoInventario, String> colFormaFarmaceutica;
    @FXML private TableColumn<ModeloMedicamentoInventario, String> colPresentacion;
    @FXML private TableColumn<ModeloMedicamentoInventario, Integer> colStock;
    @FXML private TableColumn<ModeloMedicamentoInventario, String> colPrecio;
    @FXML private TableColumn<ModeloMedicamentoInventario, Void> colAcciones;
    
    // Información y estado
    @FXML private Label lblEstadoConexion;
    @FXML private Label lblResultados;
    @FXML private ProgressIndicator progressIndicator;
    
    // Botones de acción
    @FXML private Button btnSeleccionar;
    @FXML private Button btnCerrar;
    
    // Datos y servicios
    private GestorSocket gestorSocket;
    private ObservableList<ModeloMedicamentoInventario> medicamentosOriginales;
    private ObservableList<ModeloMedicamentoInventario> medicamentosFiltrados;
    private DecimalFormat formatoMoneda;
    private Consumer<ModeloMedicamentoInventario> callbackSeleccion;
    private ModeloMedicamentoInventario medicamentoSeleccionado;
    
    // Códigos de protocolo para el servidor de inventario
    private static final int OBTENER_MEDICAMENTOS_INVENTARIO = 9001;
    private static final int OBTENER_MEDICAMENTOS_INVENTARIO_RESPONSE = 9002;
    private static final int BUSCAR_MEDICAMENTOS_INVENTARIO = 9003;
    private static final int BUSCAR_MEDICAMENTOS_INVENTARIO_RESPONSE = 9004;
    private static final int ERROR_INVENTARIO = 9999;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar servicios y datos
            gestorSocket = GestorSocket.getInstance();
            medicamentosOriginales = FXCollections.observableArrayList();
            medicamentosFiltrados = FXCollections.observableArrayList();
            formatoMoneda = new DecimalFormat("#,##0.00 €");
            
            // Configurar interfaz
            configurarTabla();
            configurarFiltros();
            configurarEventos();
            
            // Verificar conexión y cargar datos
            verificarConexionYCargarDatos();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de inicialización", "No se pudo inicializar el buscador: " + e.getMessage());
        }
    }
    
    private void configurarTabla() {
        // Configurar columnas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colPrincipioActivo.setCellValueFactory(new PropertyValueFactory<>("principioActivo"));
        colFormaFarmaceutica.setCellValueFactory(new PropertyValueFactory<>("formaFarmaceutica"));
        colPresentacion.setCellValueFactory(new PropertyValueFactory<>("presentacion"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("unidadesDisponibles"));
        
        // Columna de precio con formato
        colPrecio.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getPrecioUnitario();
            return new SimpleStringProperty(precio > 0 ? formatoMoneda.format(precio) : "No establecido");
        });
        
        // Columna de acciones
        colAcciones.setCellFactory(new Callback<TableColumn<ModeloMedicamentoInventario, Void>, TableCell<ModeloMedicamentoInventario, Void>>() {
            @Override
            public TableCell<ModeloMedicamentoInventario, Void> call(TableColumn<ModeloMedicamentoInventario, Void> param) {
                return new TableCell<ModeloMedicamentoInventario, Void>() {
                    private final Button btnSeleccionar = new Button("✅ Seleccionar");
                    private final Button btnDetalles = new Button("ℹ️ Detalles");
                    private final Button btnEstablecerPrecio = new Button("💰 Precio");
                    private final HBox hbox = new HBox(5, btnSeleccionar, btnDetalles, btnEstablecerPrecio);
                    
                    {
                        btnSeleccionar.getStyleClass().addAll("btn-primary", "btn-small");
                        btnDetalles.getStyleClass().addAll("btn-secondary", "btn-small");
                        btnEstablecerPrecio.getStyleClass().addAll("btn-warning", "btn-small");
                        hbox.setAlignment(Pos.CENTER);
                        
                        btnSeleccionar.setOnAction(e -> {
                            ModeloMedicamentoInventario medicamento = getTableView().getItems().get(getIndex());
                            seleccionarMedicamento(medicamento);
                        });
                        
                        btnDetalles.setOnAction(e -> {
                            ModeloMedicamentoInventario medicamento = getTableView().getItems().get(getIndex());
                            mostrarDetallesMedicamento(medicamento);
                        });
                        
                        btnEstablecerPrecio.setOnAction(e -> {
                            ModeloMedicamentoInventario medicamento = getTableView().getItems().get(getIndex());
                            establecerPrecio(medicamento);
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
        
        // Configurar selección
        tablaMedicamentos.setItems(medicamentosFiltrados);
        tablaMedicamentos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Listener para selección
        tablaMedicamentos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            medicamentoSeleccionado = newSelection;
            btnSeleccionar.setDisable(newSelection == null);
        });
        
        // Configurar ancho de columnas
        colCodigo.setPrefWidth(100);
        colNombre.setPrefWidth(200);
        colLaboratorio.setPrefWidth(150);
        colPrincipioActivo.setPrefWidth(150);
        colFormaFarmaceutica.setPrefWidth(120);
        colPresentacion.setPrefWidth(150);
        colStock.setPrefWidth(80);
        colPrecio.setPrefWidth(100);
        colAcciones.setPrefWidth(250);
        
        // Hacer que la tabla sea redimensionable
        tablaMedicamentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarFiltros() {
        // Configurar spinner de stock mínimo
        spnStockMinimo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 1));
        spnStockMinimo.setEditable(true);
        
        // Configurar checkbox
        chkSoloConStock.setSelected(true);
        
        // Configurar ComboBoxes
        cmbLaboratorio.getItems().add("Todos");
        cmbCategoria.getItems().add("Todas");
        cmbFormaFarmaceutica.getItems().add("Todas");
        
        cmbLaboratorio.setValue("Todos");
        cmbCategoria.setValue("Todas");
        cmbFormaFarmaceutica.setValue("Todas");
    }
    
    private void configurarEventos() {
        // Eventos de búsqueda
        btnBuscar.setOnAction(e -> aplicarFiltros());
        btnLimpiarFiltros.setOnAction(e -> limpiarFiltros());
        btnActualizar.setOnAction(e -> actualizarDatos());
        
        // Búsqueda en tiempo real
        txtBusqueda.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() >= 3 || newText.isEmpty()) {
                aplicarFiltros();
            }
        });
        
        // Filtros automáticos
        cmbLaboratorio.setOnAction(e -> aplicarFiltros());
        cmbCategoria.setOnAction(e -> aplicarFiltros());
        cmbFormaFarmaceutica.setOnAction(e -> aplicarFiltros());
        chkSoloConStock.setOnAction(e -> aplicarFiltros());
        spnStockMinimo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        
        // Botones principales
        btnSeleccionar.setOnAction(e -> seleccionarMedicamentoActual());
        btnCerrar.setOnAction(e -> cerrarVentana());
        
        // Doble clic en tabla para seleccionar
        tablaMedicamentos.setRowFactory(tv -> {
            TableRow<ModeloMedicamentoInventario> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    seleccionarMedicamento(row.getItem());
                }
            });
            return row;
        });
    }
    
    private void verificarConexionYCargarDatos() {
        // Mostrar indicador de carga
        progressIndicator.setVisible(true);
        lblEstadoConexion.setText("Verificando conexión...");
        
        Task<Boolean> taskConexion = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return gestorSocket.isConectado();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (getValue()) {
                        lblEstadoConexion.setText("✅ Conectado al servidor principal");
                        lblEstadoConexion.setStyle("-fx-text-fill: green;");
                        cargarMedicamentos();
                    } else {
                        lblEstadoConexion.setText("❌ Servidor principal no disponible");
                        lblEstadoConexion.setStyle("-fx-text-fill: red;");
                        progressIndicator.setVisible(false);
                        mostrarError("Conexión", "No se puede conectar al servidor principal");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    lblEstadoConexion.setText("❌ Error de conexión");
                    lblEstadoConexion.setStyle("-fx-text-fill: red;");
                    progressIndicator.setVisible(false);
                    mostrarError("Error", "Error al verificar la conexión: " + getException().getMessage());
                });
            }
        };
        
        new Thread(taskConexion).start();
    }
    
    private void cargarMedicamentos() {
        lblResultados.setText("Cargando medicamentos...");
        
        new Thread(() -> {
            try {
                // Verificar conexión antes de enviar petición
                if (!gestorSocket.isConectado()) {
                    Platform.runLater(() -> {
                        mostrarError("Error de conexión", "No hay conexión con el servidor");
                        progressIndicator.setVisible(false);
                    });
                    return;
                }
                
                String peticion = String.valueOf(OBTENER_MEDICAMENTOS_INVENTARIO);
                System.out.println("Enviando petición de medicamentos inventario: " + peticion);
                
                // Usar sincronización para evitar conflictos
                synchronized (gestorSocket) {
                    gestorSocket.enviarPeticion(peticion);
                    gestorSocket.getSalida().flush();
                    
                    ObjectInputStream entrada = gestorSocket.getEntrada();
                    if (entrada == null) {
                        System.err.println("No se pudo obtener el stream de entrada");
                        Platform.runLater(() -> {
                            mostrarError("Error", "No se pudo obtener el stream de entrada");
                            progressIndicator.setVisible(false);
                        });
                        return;
                    }
                    
                    System.out.println("Esperando respuesta del servidor...");
                    
                    try {
                        int codigoRespuesta = entrada.readInt();
                        System.out.println("Código de respuesta recibido: " + codigoRespuesta);
                        
                        if (codigoRespuesta == OBTENER_MEDICAMENTOS_INVENTARIO_RESPONSE) {
                            @SuppressWarnings("unchecked")
                            List<ModeloMedicamentoInventario> medicamentos = (List<ModeloMedicamentoInventario>) entrada.readObject();
                            
                            Platform.runLater(() -> {
                                medicamentosOriginales.clear();
                                if (medicamentos != null && !medicamentos.isEmpty()) {
                                    medicamentosOriginales.addAll(medicamentos);
                                    System.out.println("Medicamentos cargados exitosamente: " + medicamentos.size());
                                    
                                    // Cargar opciones de filtros
                                    cargarOpcionesFiltros();
                                    
                                    // Aplicar filtros iniciales
                                    aplicarFiltros();
                                    
                                    lblResultados.setText("Medicamentos cargados: " + medicamentos.size());
                                } else {
                                    System.out.println("No se encontraron medicamentos en la respuesta");
                                    lblResultados.setText("No se encontraron medicamentos");
                                }
                                progressIndicator.setVisible(false);
                            });
                        } else if (codigoRespuesta == ERROR_INVENTARIO) {
                            System.err.println("Error del servidor al obtener medicamentos");
                            Platform.runLater(() -> {
                                mostrarError("Error", "Error del servidor al obtener los medicamentos");
                                progressIndicator.setVisible(false);
                            });
                        } else {
                            System.err.println("Respuesta inesperada del servidor: " + codigoRespuesta);
                            Platform.runLater(() -> {
                                mostrarError("Error", "Respuesta inesperada del servidor: " + codigoRespuesta);
                                progressIndicator.setVisible(false);
                            });
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        System.err.println("Timeout al esperar respuesta del servidor");
                        Platform.runLater(() -> {
                            mostrarError("Error de timeout", "El servidor tardó demasiado en responder. Intente más tarde.");
                            progressIndicator.setVisible(false);
                        });
                    } catch (java.io.EOFException e) {
                        System.err.println("Error de EOF - conexión cerrada inesperadamente");
                        Platform.runLater(() -> {
                            mostrarError("Error de conexión", "La conexión se cerró inesperadamente. Verifique el servidor.");
                            progressIndicator.setVisible(false);
                        });
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error al cargar medicamentos: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    mostrarError("Error de comunicación", "No se pudieron cargar los medicamentos: " + e.getMessage());
                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }
    
    private void cargarOpcionesFiltros() {
        // Cargar laboratorios
        ObservableList<String> laboratorios = medicamentosOriginales.stream()
                .map(ModeloMedicamentoInventario::getLaboratorio)
                .filter(lab -> lab != null && !lab.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        
        cmbLaboratorio.getItems().clear();
        cmbLaboratorio.getItems().add("Todos");
        cmbLaboratorio.getItems().addAll(laboratorios);
        cmbLaboratorio.setValue("Todos");
        
        // Cargar categorías
        ObservableList<String> categorias = medicamentosOriginales.stream()
                .map(ModeloMedicamentoInventario::getCategoria)
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        
        cmbCategoria.getItems().clear();
        cmbCategoria.getItems().add("Todas");
        cmbCategoria.getItems().addAll(categorias);
        cmbCategoria.setValue("Todas");
        
        // Cargar formas farmacéuticas
        ObservableList<String> formas = medicamentosOriginales.stream()
                .map(ModeloMedicamentoInventario::getFormaFarmaceutica)
                .filter(forma -> forma != null && !forma.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        
        cmbFormaFarmaceutica.getItems().clear();
        cmbFormaFarmaceutica.getItems().add("Todas");
        cmbFormaFarmaceutica.getItems().addAll(formas);
        cmbFormaFarmaceutica.setValue("Todas");
    }
    
    private void aplicarFiltros() {
        if (medicamentosOriginales.isEmpty()) {
            return;
        }
        
        String textoBusqueda = txtBusqueda.getText();
        String laboratorio = cmbLaboratorio.getValue();
        String categoria = cmbCategoria.getValue();
        String formaFarmaceutica = cmbFormaFarmaceutica.getValue();
        boolean soloConStock = chkSoloConStock.isSelected();
        int stockMinimo = spnStockMinimo.getValue();
        
        ObservableList<ModeloMedicamentoInventario> filtrados = medicamentosOriginales.stream()
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
        
        medicamentosFiltrados.clear();
        medicamentosFiltrados.addAll(filtrados);
        
        lblResultados.setText("Resultados: " + filtrados.size() + " de " + medicamentosOriginales.size());
    }
    
    private void limpiarFiltros() {
        txtBusqueda.clear();
        cmbLaboratorio.setValue("Todos");
        cmbCategoria.setValue("Todas");
        cmbFormaFarmaceutica.setValue("Todas");
        chkSoloConStock.setSelected(true);
        spnStockMinimo.getValueFactory().setValue(1);
        aplicarFiltros();
    }
    
    private void actualizarDatos() {
        verificarConexionYCargarDatos();
    }
    
    private void seleccionarMedicamentoActual() {
        if (medicamentoSeleccionado != null) {
            seleccionarMedicamento(medicamentoSeleccionado);
        } else {
            mostrarAdvertencia("Selección", "Por favor, seleccione un medicamento de la tabla");
        }
    }
    
    private void seleccionarMedicamento(ModeloMedicamentoInventario medicamento) {
        if (medicamento.getPrecioUnitario() <= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Precio no establecido");
            alert.setHeaderText("El medicamento no tiene precio establecido");
            alert.setContentText("¿Desea establecer un precio antes de agregarlo a la factura?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (establecerPrecio(medicamento)) {
                    ejecutarCallbackSeleccion(medicamento);
                }
            } else {
                ejecutarCallbackSeleccion(medicamento);
            }
        } else {
            ejecutarCallbackSeleccion(medicamento);
        }
    }
    
    private void ejecutarCallbackSeleccion(ModeloMedicamentoInventario medicamento) {
        if (callbackSeleccion != null) {
            callbackSeleccion.accept(medicamento);
        }
        cerrarVentana();
    }
    
    private boolean establecerPrecio(ModeloMedicamentoInventario medicamento) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(medicamento.getPrecioUnitario()));
        dialog.setTitle("Establecer Precio");
        dialog.setHeaderText("Precio para: " + medicamento.getNombreCompleto());
        dialog.setContentText("Precio unitario (€):");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double precio = Double.parseDouble(result.get());
                if (precio >= 0) {
                    medicamento.setPrecioUnitario(precio);
                    tablaMedicamentos.refresh();
                    return true;
                } else {
                    mostrarError("Precio inválido", "El precio debe ser mayor o igual a 0");
                }
            } catch (NumberFormatException e) {
                mostrarError("Precio inválido", "Por favor, ingrese un número válido");
            }
        }
        return false;
    }
    
    private void mostrarDetallesMedicamento(ModeloMedicamentoInventario medicamento) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalles del Medicamento");
        dialog.setHeaderText(medicamento.getNombreCompleto());
        
        // Crear contenido del diálogo
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // Información básica
        content.getChildren().addAll(
                crearCampoDetalle("Código:", medicamento.getCodigo()),
                crearCampoDetalle("Nombre:", medicamento.getNombre()),
                crearCampoDetalle("Laboratorio:", medicamento.getLaboratorio()),
                crearCampoDetalle("Principio Activo:", medicamento.getPrincipioActivo()),
                crearCampoDetalle("Forma Farmacéutica:", medicamento.getFormaFarmaceutica()),
                crearCampoDetalle("Dosis:", medicamento.getDosis()),
                crearCampoDetalle("Vía:", medicamento.getVia()),
                crearCampoDetalle("Presentación:", medicamento.getPresentacion()),
                new Separator(),
                crearCampoDetalle("Stock Disponible:", String.valueOf(medicamento.getUnidadesDisponibles())),
                crearCampoDetalle("Precio Unitario:", medicamento.getPrecioUnitario() > 0 ? 
                        formatoMoneda.format(medicamento.getPrecioUnitario()) : "No establecido"),
                crearCampoDetalle("Ubicación:", medicamento.getUbicacion()),
                crearCampoDetalle("Lote:", medicamento.getLote()),
                crearCampoDetalle("Fecha Caducidad:", medicamento.getFechaCaducidad()),
                new Separator(),
                crearCampoDetalle("Categoría:", medicamento.getCategoria()),
                crearCampoDetalle("Requiere Receta:", medicamento.isRequiereReceta() ? "Sí" : "No"),
                crearCampoDetalle("Observaciones:", medicamento.getObservaciones())
        );
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 400);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    private HBox crearCampoDetalle(String etiqueta, String valor) {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblEtiqueta.setPrefWidth(150);
        
        Label lblValor = new Label(valor != null && !valor.trim().isEmpty() ? valor : "No especificado");
        lblValor.setWrapText(true);
        HBox.setHgrow(lblValor, Priority.ALWAYS);
        
        hbox.getChildren().addAll(lblEtiqueta, lblValor);
        return hbox;
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
    
    // Métodos públicos para configurar el controlador
    
    public void setCallbackSeleccion(Consumer<ModeloMedicamentoInventario> callback) {
        this.callbackSeleccion = callback;
    }
    
    // Métodos de utilidad para mostrar mensajes
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 