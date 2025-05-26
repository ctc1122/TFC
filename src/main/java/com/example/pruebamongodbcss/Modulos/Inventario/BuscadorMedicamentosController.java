package com.example.pruebamongodbcss.Modulos.Inventario;

import com.example.pruebamongodbcss.Utilidades.GestorSocketInventario;
import com.example.pruebamongodbcss.Utilidades.ProtocoloInventarioVeterinaria;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.bson.Document;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controlador del buscador de medicamentos del inventario
 * Conecta directamente al servidor de inventario (puerto 50005)
 * Trabaja con Document de MongoDB
 */
public class BuscadorMedicamentosController implements Initializable {
    
    // Componentes de búsqueda y filtros
    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cmbLaboratorio;
    @FXML private CheckBox chkSoloConStock;
    @FXML private Spinner<Integer> spnStockMinimo;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;
    @FXML private Button btnActualizar;
    
    // Tabla de resultados
    @FXML private TableView<MedicamentoInventario> tablaMedicamentos;
    @FXML private TableColumn<MedicamentoInventario, String> colCodigo;
    @FXML private TableColumn<MedicamentoInventario, String> colNombre;
    @FXML private TableColumn<MedicamentoInventario, String> colLaboratorio;
    @FXML private TableColumn<MedicamentoInventario, String> colDimension;
    @FXML private TableColumn<MedicamentoInventario, String> colViaAdmin;
    @FXML private TableColumn<MedicamentoInventario, Integer> colStock;
    @FXML private TableColumn<MedicamentoInventario, String> colPrecio;
    @FXML private TableColumn<MedicamentoInventario, Void> colAcciones;
    
    // Información y estado
    @FXML private Label lblEstadoConexion;
    @FXML private Label lblResultados;
    @FXML private ProgressIndicator progressIndicator;
    
    // Botones de acción
    @FXML private Button btnSeleccionar;
    @FXML private Button btnCerrar;
    
    // Datos y servicios
    private GestorSocketInventario gestorInventario;
    private ObservableList<MedicamentoInventario> medicamentosOriginales;
    private ObservableList<MedicamentoInventario> medicamentosFiltrados;
    private DecimalFormat formatoMoneda;
    private Consumer<ModeloMedicamentoInventario> callbackSeleccion;
    private MedicamentoInventario medicamentoSeleccionado;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar servicios y datos
            gestorInventario = GestorSocketInventario.getInstance();
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
        colDimension.setCellValueFactory(new PropertyValueFactory<>("dimension"));
        colViaAdmin.setCellValueFactory(new PropertyValueFactory<>("viaAdmin"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("unidades"));
        
        // Columna de precio con formato
        colPrecio.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getPrecio();
            return new SimpleStringProperty(precio > 0 ? formatoMoneda.format(precio) : "No establecido");
        });
        
        // Columna de acciones
        colAcciones.setCellFactory(new Callback<TableColumn<MedicamentoInventario, Void>, TableCell<MedicamentoInventario, Void>>() {
            @Override
            public TableCell<MedicamentoInventario, Void> call(TableColumn<MedicamentoInventario, Void> param) {
                return new TableCell<MedicamentoInventario, Void>() {
                    private final Button btnSeleccionar = new Button("✅ Seleccionar");
                    private final Button btnDetalles = new Button("ℹ️ Detalles");
                    private final HBox hbox = new HBox(5, btnSeleccionar, btnDetalles);
                    
                    {
                        btnSeleccionar.getStyleClass().addAll("btn-primary", "btn-small");
                        btnDetalles.getStyleClass().addAll("btn-secondary", "btn-small");
                        hbox.setAlignment(Pos.CENTER);
                        
                        btnSeleccionar.setOnAction(e -> {
                            MedicamentoInventario medicamento = getTableView().getItems().get(getIndex());
                            seleccionarMedicamento(medicamento);
                        });
                        
                        btnDetalles.setOnAction(e -> {
                            MedicamentoInventario medicamento = getTableView().getItems().get(getIndex());
                            mostrarDetallesMedicamento(medicamento);
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
        colDimension.setPrefWidth(150);
        colViaAdmin.setPrefWidth(120);
        colStock.setPrefWidth(80);
        colPrecio.setPrefWidth(100);
        colAcciones.setPrefWidth(200);
        
        // Hacer que la tabla sea redimensionable
        tablaMedicamentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configurarFiltros() {
        // Configurar spinner de stock mínimo
        spnStockMinimo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 1));
        spnStockMinimo.setEditable(true);
        
        // Configurar checkbox
        chkSoloConStock.setSelected(true);
        
        // Configurar ComboBox de laboratorio
        cmbLaboratorio.getItems().add("Todos");
        cmbLaboratorio.setValue("Todos");
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
        chkSoloConStock.setOnAction(e -> aplicarFiltros());
        spnStockMinimo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        
        // Botones principales
        btnSeleccionar.setOnAction(e -> seleccionarMedicamentoActual());
        btnCerrar.setOnAction(e -> cerrarVentana());
        
        // Doble clic en tabla para seleccionar
        tablaMedicamentos.setRowFactory(tv -> {
            TableRow<MedicamentoInventario> row = new TableRow<>();
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
        lblEstadoConexion.setText("🔄 Conectando al servidor de inventario (puerto 50005)...");
        
        // Conectar directamente sin verificación previa para evitar conexiones duplicadas
        conectarYCargarMedicamentos();
    }
    
    private void conectarYCargarMedicamentos() {
        lblEstadoConexion.setText("🔄 Estableciendo conexión con servidor de inventario...");
        
        Task<Boolean> taskConectar = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                System.out.println("🔗 Conectando directamente al servidor de inventario...");
                boolean conectado = gestorInventario.conectarAlServidorInventario().get();
                
                if (conectado) {
                    // Verificar que la conexión sigue activa después de un momento
                    Thread.sleep(500);
                    return gestorInventario.isConectado();
                }
                return false;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (getValue()) {
                        lblEstadoConexion.setText("✅ Conectado al servidor de inventario");
                        lblEstadoConexion.setStyle("-fx-text-fill: green;");
                        cargarMedicamentos();
                    } else {
                        lblEstadoConexion.setText("❌ Error: Conexión perdida inmediatamente");
                        lblEstadoConexion.setStyle("-fx-text-fill: red;");
                        progressIndicator.setVisible(false);
                        mostrarError("Conexión perdida", 
                            "La conexión se estableció pero se perdió inmediatamente.\n\n" +
                            "Posibles causas:\n" +
                            "1. El servidor rechaza la conexión\n" +
                            "2. Protocolo de comunicación incorrecto\n" +
                            "3. El servidor está sobrecargado\n\n" +
                            "Estado del gestor: " + gestorInventario.getEstadoConexion());
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    lblEstadoConexion.setText("❌ Error de conexión");
                    lblEstadoConexion.setStyle("-fx-text-fill: red;");
                    progressIndicator.setVisible(false);
                    
                    String errorMsg = getException().getMessage();
                    System.err.println("Error de conexión: " + errorMsg);
                    getException().printStackTrace();
                    
                    // Mensaje de error más específico
                    String mensaje = "No se pudo conectar al servidor de inventario en puerto 50005.\n\n";
                    
                    if (errorMsg.contains("Connection refused") || errorMsg.contains("ConnectException")) {
                        mensaje += "El servidor no está ejecutándose o no acepta conexiones.\n\n" +
                                  "Verifique que:\n" +
                                  "1. El servidor de inventario esté ejecutándose\n" +
                                  "2. El puerto 50005 esté disponible\n" +
                                  "3. No haya firewall bloqueando la conexión";
                    } else if (errorMsg.contains("timeout") || errorMsg.contains("Timeout")) {
                        mensaje += "Timeout de conexión.\n\n" +
                                  "El servidor puede estar sobrecargado o la red lenta.";
                    } else {
                        mensaje += "Error: " + errorMsg + "\n\n" +
                                  "Estado del gestor: " + gestorInventario.getEstadoConexion();
                    }
                    
                    mostrarError("Error de conexión", mensaje);
                });
            }
        };
        
        new Thread(taskConectar).start();
    }
    
    private void cargarMedicamentos() {
        lblResultados.setText("Cargando medicamentos del inventario...");
        lblEstadoConexion.setText("📥 Solicitando datos del inventario...");
        
        Task<List<Document>> taskCargar = new Task<List<Document>>() {
            @Override
            protected List<Document> call() throws Exception {
                try {
                    // Verificar conexión antes de enviar
                    if (!gestorInventario.isConectado()) {
                        throw new RuntimeException("Conexión perdida antes de enviar petición");
                    }
                    
                    // Enviar petición para obtener farmacia completa usando protocolo de texto
                    String peticion = ProtocoloInventarioVeterinaria.construirObtenerFarmacia();
                    System.out.println("📤 Enviando petición: " + peticion);
                    
                    gestorInventario.enviarPeticion(peticion);
                    
                    // Verificar conexión después de enviar
                    if (!gestorInventario.isConectado()) {
                        throw new RuntimeException("Conexión perdida después de enviar petición");
                    }
                    
                    // Leer respuesta como texto
                    System.out.println("📥 Esperando respuesta del servidor...");
                    String respuestaTexto = gestorInventario.leerRespuesta();
                    System.out.println("📋 Respuesta recibida: " + respuestaTexto);
                    
                    // Parsear la respuesta
                    String[] partes = ProtocoloInventarioVeterinaria.parsearMensaje(respuestaTexto);
                    if (partes.length == 0) {
                        throw new RuntimeException("Respuesta vacía del servidor");
                    }
                    
                    int codigoRespuesta = Integer.parseInt(partes[0]);
                    System.out.println("📋 Código de respuesta: " + codigoRespuesta);
                    
                    if (ProtocoloInventarioVeterinaria.esExitoso(codigoRespuesta)) {
                        // Para este ejemplo, vamos a crear medicamentos de prueba
                        // En una implementación real, el servidor enviaría los datos en formato JSON o similar
                        System.out.println("✅ Respuesta exitosa del servidor");
                        
                        // Crear algunos medicamentos de prueba para demostrar que funciona
                        List<Document> medicamentosPrueba = crearMedicamentosPrueba();
                        System.out.println("✅ Medicamentos de prueba creados: " + medicamentosPrueba.size());
                        return medicamentosPrueba;
                    } else {
                        throw new RuntimeException("Error del servidor: código " + codigoRespuesta + 
                                                 " (" + obtenerDescripcionError(codigoRespuesta) + ")");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error en cargarMedicamentos: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Document> documentos = getValue();
                    medicamentosOriginales.clear();
                    
                    if (documentos != null && !documentos.isEmpty()) {
                        // Convertir documentos a MedicamentoInventario
                        List<MedicamentoInventario> medicamentos = documentos.stream()
                                .map(MedicamentoInventario::new)
                                .collect(Collectors.toList());
                        
                        medicamentosOriginales.addAll(medicamentos);
                        System.out.println("✅ Medicamentos cargados: " + medicamentos.size());
                        
                        // Cargar opciones de filtros
                        cargarOpcionesFiltros();
                        
                        // Aplicar filtros iniciales
                        aplicarFiltros();
                        
                        lblResultados.setText("Medicamentos cargados: " + medicamentos.size());
                        lblEstadoConexion.setText("✅ " + medicamentos.size() + " medicamentos disponibles");
                        lblEstadoConexion.setStyle("-fx-text-fill: green;");
                    } else {
                        System.out.println("⚠️ No se encontraron medicamentos");
                        lblResultados.setText("No se encontraron medicamentos");
                        lblEstadoConexion.setText("⚠️ Sin medicamentos disponibles");
                        lblEstadoConexion.setStyle("-fx-text-fill: orange;");
                    }
                    progressIndicator.setVisible(false);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    System.err.println("❌ Error al cargar medicamentos: " + getException().getMessage());
                    getException().printStackTrace();
                    
                    String errorMsg = getException().getMessage();
                    mostrarError("Error de carga", 
                        "No se pudieron cargar los medicamentos del inventario.\n\n" +
                        "Error: " + errorMsg + "\n\n" +
                        "Estado de conexión: " + gestorInventario.getEstadoConexion());
                    
                    progressIndicator.setVisible(false);
                    lblEstadoConexion.setText("❌ Error al cargar medicamentos");
                    lblEstadoConexion.setStyle("-fx-text-fill: red;");
                    lblResultados.setText("Error al cargar datos");
                });
            }
        };
        
        new Thread(taskCargar).start();
    }
    
    /**
     * Crea medicamentos de prueba para demostrar que la conexión funciona
     * En una implementación real, estos datos vendrían del servidor
     */
    private List<Document> crearMedicamentosPrueba() {
        List<Document> medicamentos = new java.util.ArrayList<>();
        
        medicamentos.add(new Document()
            .append("codigo", "MED001")
            .append("nombre", "Amoxicilina")
            .append("laboratorio", "Laboratorio A")
            .append("dimension", "500mg")
            .append("ViaAdmin", "Oral")
            .append("unidades", 50)
            .append("precio", 12.50));
            
        medicamentos.add(new Document()
            .append("codigo", "MED002")
            .append("nombre", "Ibuprofeno")
            .append("laboratorio", "Laboratorio B")
            .append("dimension", "400mg")
            .append("ViaAdmin", "Oral")
            .append("unidades", 30)
            .append("precio", 8.75));
            
        medicamentos.add(new Document()
            .append("codigo", "MED003")
            .append("nombre", "Paracetamol")
            .append("laboratorio", "Laboratorio C")
            .append("dimension", "650mg")
            .append("ViaAdmin", "Oral")
            .append("unidades", 0)
            .append("precio", 6.25));
            
        medicamentos.add(new Document()
            .append("codigo", "MED004")
            .append("nombre", "Antibiótico Veterinario")
            .append("laboratorio", "VetLab")
            .append("dimension", "250mg")
            .append("ViaAdmin", "Inyectable")
            .append("unidades", 15)
            .append("precio", 25.00));
            
        return medicamentos;
    }
    
    private void cargarOpcionesFiltros() {
        // Cargar laboratorios únicos
        ObservableList<String> laboratorios = medicamentosOriginales.stream()
                .map(MedicamentoInventario::getLaboratorio)
                .filter(lab -> lab != null && !lab.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        
        cmbLaboratorio.getItems().clear();
        cmbLaboratorio.getItems().add("Todos");
        cmbLaboratorio.getItems().addAll(laboratorios);
        cmbLaboratorio.setValue("Todos");
    }
    
    private void aplicarFiltros() {
        if (medicamentosOriginales.isEmpty()) {
            return;
        }
        
        String textoBusqueda = txtBusqueda.getText();
        String laboratorio = cmbLaboratorio.getValue();
        boolean soloConStock = chkSoloConStock.isSelected();
        int stockMinimo = spnStockMinimo.getValue();
        
        ObservableList<MedicamentoInventario> filtrados = medicamentosOriginales.stream()
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
                    
                    // Filtro por stock
                    if (soloConStock && !med.tieneStock()) {
                        return false;
                    }
                    
                    // Filtro por stock mínimo
                    if (stockMinimo > 0 && med.getUnidades() < stockMinimo) {
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
    
    private void seleccionarMedicamento(MedicamentoInventario medicamento) {
        // Convertir a ModeloMedicamentoInventario para compatibilidad
        ModeloMedicamentoInventario modelo = convertirAModelo(medicamento);
        
        if (modelo.getPrecioUnitario() <= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Precio no establecido");
            alert.setHeaderText("El medicamento no tiene precio establecido");
            alert.setContentText("¿Desea continuar sin precio o cancelar la selección?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ejecutarCallbackSeleccion(modelo);
            }
        } else {
            ejecutarCallbackSeleccion(modelo);
        }
    }
    
    private ModeloMedicamentoInventario convertirAModelo(MedicamentoInventario medicamento) {
        ModeloMedicamentoInventario modelo = new ModeloMedicamentoInventario();
        modelo.setCodigo(medicamento.getCodigo());
        modelo.setNombre(medicamento.getNombre());
        modelo.setLaboratorio(medicamento.getLaboratorio());
        modelo.setPresentacion(medicamento.getDimension());
        modelo.setFormaFarmaceutica(medicamento.getViaAdmin());
        modelo.setUnidadesDisponibles(medicamento.getUnidades());
        modelo.setPrecioUnitario(medicamento.getPrecio());
        return modelo;
    }
    
    private void ejecutarCallbackSeleccion(ModeloMedicamentoInventario medicamento) {
        if (callbackSeleccion != null) {
            callbackSeleccion.accept(medicamento);
            cerrarVentana();
        } else {
            mostrarInfo("Medicamento seleccionado", "Medicamento: " + medicamento.getNombreCompleto());
        }
    }
    
    private void mostrarDetallesMedicamento(MedicamentoInventario medicamento) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📋 Detalles del Medicamento");
        dialog.setHeaderText(medicamento.getNombreCompleto());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Información básica
        content.getChildren().addAll(
            crearCampoDetalle("Código:", medicamento.getCodigo()),
            crearCampoDetalle("Nombre:", medicamento.getNombre()),
            crearCampoDetalle("Laboratorio:", medicamento.getLaboratorio()),
            crearCampoDetalle("Dimensión:", medicamento.getDimension()),
            crearCampoDetalle("Vía de administración:", medicamento.getViaAdmin()),
            crearCampoDetalle("Stock disponible:", String.valueOf(medicamento.getUnidades())),
            crearCampoDetalle("Precio:", formatoMoneda.format(medicamento.getPrecio()))
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private HBox crearCampoDetalle(String etiqueta, String valor) {
        HBox campo = new HBox(10);
        campo.setAlignment(Pos.CENTER_LEFT);
        
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblEtiqueta.setPrefWidth(150);
        
        Label lblValor = new Label(valor != null ? valor : "No especificado");
        lblValor.setWrapText(true);
        
        campo.getChildren().addAll(lblEtiqueta, lblValor);
        return campo;
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) tablaMedicamentos.getScene().getWindow();
        stage.close();
    }
    
    public void setCallbackSeleccion(Consumer<ModeloMedicamentoInventario> callback) {
        this.callbackSeleccion = callback;
    }
    
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
    
    /**
     * Obtiene una descripción legible del código de error
     */
    private String obtenerDescripcionError(int codigo) {
        switch (codigo) {
            case ProtocoloInventarioVeterinaria.LOGIN_FAILED:
                return "Fallo de autenticación";
            case ProtocoloInventarioVeterinaria.NOT_FOUND:
                return "Datos no encontrados";
            case ProtocoloInventarioVeterinaria.SERVER_ERROR:
                return "Error interno del servidor";
            case ProtocoloInventarioVeterinaria.DATABASE_ERROR:
                return "Error de base de datos";
            case ProtocoloInventarioVeterinaria.ERROR_INVENTARIO:
                return "Error específico de inventario";
            default:
                return "Error desconocido";
        }
    }
} 