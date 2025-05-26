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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.bson.Document;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
        // Hacer la tabla editable
        tablaMedicamentos.setEditable(true);
        
        // Configurar columnas básicas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDimension.setCellValueFactory(new PropertyValueFactory<>("dimension"));
        colViaAdmin.setCellValueFactory(new PropertyValueFactory<>("viaAdmin"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("unidades"));
        
        // Columna de laboratorio EDITABLE
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colLaboratorio.setCellFactory(TextFieldTableCell.forTableColumn());
        colLaboratorio.setOnEditCommit(event -> {
            MedicamentoInventario medicamento = event.getRowValue();
            String nuevoLaboratorio = event.getNewValue();
            medicamento.setLaboratorio(nuevoLaboratorio);
            System.out.println("✏️ Laboratorio actualizado: " + medicamento.getNombre() + " -> " + nuevoLaboratorio);
            mostrarNotificacion("Laboratorio actualizado", "Nuevo laboratorio: " + nuevoLaboratorio);
        });
        
        // Columna de precio EDITABLE con formato
        colPrecio.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getPrecio();
            return new SimpleStringProperty(precio > 0 ? String.valueOf(precio) : "0.0");
        });
        colPrecio.setCellFactory(TextFieldTableCell.forTableColumn());
        colPrecio.setOnEditCommit(event -> {
            MedicamentoInventario medicamento = event.getRowValue();
            String nuevoPrecioStr = event.getNewValue();
            try {
                double nuevoPrecio = Double.parseDouble(nuevoPrecioStr);
                medicamento.setPrecio(nuevoPrecio);
                System.out.println("💰 Precio actualizado: " + medicamento.getNombre() + " -> " + formatoMoneda.format(nuevoPrecio));
                mostrarNotificacion("Precio actualizado", "Nuevo precio: " + formatoMoneda.format(nuevoPrecio));
                // Refrescar la tabla para mostrar el formato correcto
                tablaMedicamentos.refresh();
            } catch (NumberFormatException e) {
                mostrarError("Error", "Precio inválido: " + nuevoPrecioStr + "\nUse formato: 12.50");
                // Revertir el cambio
                tablaMedicamentos.refresh();
            }
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
        
        // Agregar tooltip para indicar que las celdas son editables
        colLaboratorio.setText("Laboratorio 📝");
        colPrecio.setText("Precio (€) 📝");
        
        // Tooltip explicativo
        Tooltip tooltipLab = new Tooltip("Doble clic para editar el laboratorio");
        Tooltip.install(colLaboratorio.getGraphic(), tooltipLab);
        
        Tooltip tooltipPrecio = new Tooltip("Doble clic para editar el precio");
        Tooltip.install(colPrecio.getGraphic(), tooltipPrecio);
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
                    
                    // Usar el protocolo correcto: BUSCAR_MEDICAMENTOS_INVENTARIO con término vacío para obtener todos
                    String peticion = ProtocoloInventarioVeterinaria.construirBuscarMedicamentos("");
                    System.out.println("📤 Enviando petición: " + peticion);
                    System.out.println("📤 Código usado: " + ProtocoloInventarioVeterinaria.BUSCAR_MEDICAMENTOS_INVENTARIO);
                    
                    gestorInventario.enviarPeticion(peticion);
                    
                    // Verificar conexión después de enviar
                    if (!gestorInventario.isConectado()) {
                        throw new RuntimeException("Conexión perdida después de enviar petición");
                    }
                    
                    // Leer respuesta como texto
                    System.out.println("📥 Esperando respuesta del servidor...");
                    String respuestaTexto = gestorInventario.leerRespuesta();
                    System.out.println("📋 Respuesta completa recibida: '" + respuestaTexto + "'");
                    
                    // Parsear la respuesta según el protocolo del servidor
                    String[] partes = ProtocoloInventarioVeterinaria.parsearMensaje(respuestaTexto);
                    System.out.println("📋 Partes parseadas: " + java.util.Arrays.toString(partes));
                    
                    if (partes.length < 3) {
                        throw new RuntimeException("Respuesta incompleta del servidor: " + respuestaTexto + 
                                                 " (partes: " + partes.length + ")");
                    }
                    
                    int codigoRespuesta = Integer.parseInt(partes[0]);
                    String idMensaje = partes[1];
                    String codigoEstado = partes[2];
                    
                    System.out.println("📋 Código de respuesta: " + codigoRespuesta + " (esperado: " + ProtocoloInventarioVeterinaria.BUSCAR_MEDICAMENTOS_INVENTARIO_RESPONSE + ")");
                    System.out.println("📋 ID mensaje: " + idMensaje);
                    System.out.println("📋 Código estado: " + codigoEstado + " (esperado: " + ProtocoloInventarioVeterinaria.SUCCESS + ")");
                    
                    if (codigoRespuesta == ProtocoloInventarioVeterinaria.BUSCAR_MEDICAMENTOS_INVENTARIO_RESPONSE) {
                        if (Integer.parseInt(codigoEstado) == ProtocoloInventarioVeterinaria.SUCCESS) {
                            System.out.println("✅ Respuesta exitosa del servidor");
                            
                            // Los datos están en Base64 en la parte 4
                            if (partes.length >= 4) {
                                String datosBase64 = partes[3];
                                System.out.println("📦 Datos Base64 recibidos (longitud: " + datosBase64.length() + ")");
                                
                                // Decodificar Base64 y parsear JSON
                                List<Document> medicamentosReales = decodificarMedicamentosBase64(datosBase64);
                                System.out.println("✅ Medicamentos reales recibidos: " + medicamentosReales.size());
                                return medicamentosReales;
                            } else {
                                throw new RuntimeException("No se recibieron datos de medicamentos (partes: " + partes.length + ")");
                            }
                        } else {
                            // Error del servidor - mostrar mensaje de error si está disponible
                            String mensajeError = partes.length >= 4 ? partes[3] : "Error desconocido";
                            throw new RuntimeException("Error del servidor: estado " + codigoEstado + " - " + mensajeError);
                        }
                    } else {
                        throw new RuntimeException("Código de respuesta inesperado: " + codigoRespuesta + 
                                                 " (esperado: " + ProtocoloInventarioVeterinaria.BUSCAR_MEDICAMENTOS_INVENTARIO_RESPONSE + ")");
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
     * Decodifica los medicamentos que vienen en Base64 desde el servidor
     */
    private List<Document> decodificarMedicamentosBase64(String datosBase64) {
        List<Document> medicamentos = new ArrayList<>();
        
        try {
            // Decodificar Base64
            byte[] datosDecodificados = java.util.Base64.getDecoder().decode(datosBase64);
            String jsonData = new String(datosDecodificados, "UTF-8");
            
            System.out.println("📋 JSON decodificado (primeros 200 chars): " + 
                             (jsonData.length() > 200 ? jsonData.substring(0, 200) + "..." : jsonData));
            
            // Parsear JSON manualmente (sin dependencias externas)
            medicamentos = parsearJsonMedicamentos(jsonData);
            
            System.out.println("✅ Medicamentos parseados del JSON: " + medicamentos.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error al decodificar medicamentos Base64: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicamentos;
    }
    
    /**
     * Parsea el JSON de medicamentos de forma simple (sin librerías externas)
     */
    private List<Document> parsearJsonMedicamentos(String jsonData) {
        List<Document> medicamentos = new ArrayList<>();
        
        try {
            // El JSON debería ser un array de objetos
            if (jsonData.trim().startsWith("[") && jsonData.trim().endsWith("]")) {
                // Remover corchetes externos
                String contenido = jsonData.trim().substring(1, jsonData.trim().length() - 1);
                
                // Dividir por objetos (buscar patrones de {})
                String[] objetos = dividirObjetosJson(contenido);
                
                for (String objetoJson : objetos) {
                    if (!objetoJson.trim().isEmpty()) {
                        Document medicamento = parsearObjetoJsonMedicamento(objetoJson.trim());
                        if (medicamento != null) {
                            medicamentos.add(medicamento);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al parsear JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicamentos;
    }
    
    /**
     * Divide el contenido JSON en objetos individuales
     */
    private String[] dividirObjetosJson(String contenido) {
        List<String> objetos = new ArrayList<>();
        StringBuilder objetoActual = new StringBuilder();
        int nivelLlaves = 0;
        boolean enCadena = false;
        char caracterAnterior = ' ';
        
        for (char c : contenido.toCharArray()) {
            if (c == '"' && caracterAnterior != '\\') {
                enCadena = !enCadena;
            }
            
            if (!enCadena) {
                if (c == '{') {
                    nivelLlaves++;
                } else if (c == '}') {
                    nivelLlaves--;
                }
            }
            
            objetoActual.append(c);
            
            if (nivelLlaves == 0 && c == '}') {
                objetos.add(objetoActual.toString());
                objetoActual = new StringBuilder();
            }
            
            caracterAnterior = c;
        }
        
        return objetos.toArray(new String[0]);
    }
    
    /**
     * Parsea un objeto JSON individual de medicamento
     */
    private Document parsearObjetoJsonMedicamento(String objetoJson) {
        try {
            Document medicamento = new Document();
            
            // Remover llaves externas
            if (objetoJson.startsWith("{") && objetoJson.endsWith("}")) {
                objetoJson = objetoJson.substring(1, objetoJson.length() - 1);
            }
            
            // Dividir por campos (cuidado con las comas dentro de strings)
            String[] campos = dividirCamposJson(objetoJson);
            
            for (String campo : campos) {
                String[] partes = campo.split(":", 2);
                if (partes.length == 2) {
                    String clave = limpiarCadenaJson(partes[0]);
                    String valor = limpiarCadenaJson(partes[1]);
                    
                    // Convertir tipos apropiados
                    if (clave.equals("unidades")) {
                        try {
                            medicamento.append(clave, Integer.parseInt(valor));
                        } catch (NumberFormatException e) {
                            medicamento.append(clave, 0);
                        }
                    } else if (clave.equals("precio")) {
                        try {
                            medicamento.append(clave, Double.parseDouble(valor));
                        } catch (NumberFormatException e) {
                            medicamento.append(clave, 0.0);
                        }
                    } else {
                        medicamento.append(clave, valor);
                    }
                }
            }
            
            return medicamento;
            
        } catch (Exception e) {
            System.err.println("❌ Error al parsear objeto medicamento: " + objetoJson + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Divide los campos JSON respetando las comas dentro de strings
     */
    private String[] dividirCamposJson(String contenido) {
        List<String> campos = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();
        boolean enCadena = false;
        char caracterAnterior = ' ';
        
        for (char c : contenido.toCharArray()) {
            if (c == '"' && caracterAnterior != '\\') {
                enCadena = !enCadena;
            }
            
            if (c == ',' && !enCadena) {
                campos.add(campoActual.toString().trim());
                campoActual = new StringBuilder();
            } else {
                campoActual.append(c);
            }
            
            caracterAnterior = c;
        }
        
        if (campoActual.length() > 0) {
            campos.add(campoActual.toString().trim());
        }
        
        return campos.toArray(new String[0]);
    }
    
    /**
     * Limpia una cadena JSON removiendo comillas y espacios
     */
    private String limpiarCadenaJson(String cadena) {
        if (cadena == null) return "";
        
        cadena = cadena.trim();
        
        // Remover comillas si las tiene
        if (cadena.startsWith("\"") && cadena.endsWith("\"")) {
            cadena = cadena.substring(1, cadena.length() - 1);
        }
        
        // Decodificar caracteres escapados básicos
        cadena = cadena.replace("\\\"", "\"");
        cadena = cadena.replace("\\\\", "\\");
        
        return cadena;
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
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("📋 Detalles del Medicamento");
        dialog.setHeaderText(medicamento.getNombreCompleto());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Información básica (solo lectura)
        VBox infoBasica = new VBox(10);
        infoBasica.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        Label lblTituloBasico = new Label("📋 Información Básica (del servidor)");
        lblTituloBasico.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        infoBasica.getChildren().addAll(
            lblTituloBasico,
            crearCampoDetalle("Código:", medicamento.getCodigo()),
            crearCampoDetalle("Nombre:", medicamento.getNombre()),
            crearCampoDetalle("Stock disponible:", String.valueOf(medicamento.getUnidades()))
        );
        
        // Información editable
        VBox infoEditable = new VBox(10);
        infoEditable.setStyle("-fx-background-color: #f0f9ff; -fx-border-color: #0ea5e9; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        Label lblTituloEditable = new Label("✏️ Información Editable");
        lblTituloEditable.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Campos editables
        TextField txtLaboratorio = new TextField(medicamento.getLaboratorio());
        txtLaboratorio.setPromptText("Ej: Laboratorios Veterinarios S.A.");
        
        TextField txtPrecio = new TextField(String.valueOf(medicamento.getPrecio()));
        txtPrecio.setPromptText("Precio en euros");
        
        TextField txtDimension = new TextField(medicamento.getDimension());
        txtDimension.setPromptText("Ej: 250mg, 10ml, etc.");
        
        ComboBox<String> cmbViaAdmin = new ComboBox<>();
        cmbViaAdmin.getItems().addAll("Oral", "Inyectable", "Tópica", "Oftálmica", "Pulverización", "Sublingual", "Rectal");
        cmbViaAdmin.setValue(medicamento.getViaAdmin());
        
        GridPane gridEditable = new GridPane();
        gridEditable.setHgap(10);
        gridEditable.setVgap(10);
        
        gridEditable.add(new Label("Laboratorio:"), 0, 0);
        gridEditable.add(txtLaboratorio, 1, 0);
        gridEditable.add(new Label("Precio (€):"), 0, 1);
        gridEditable.add(txtPrecio, 1, 1);
        gridEditable.add(new Label("Dimensión:"), 0, 2);
        gridEditable.add(txtDimension, 1, 2);
        gridEditable.add(new Label("Vía Admin.:"), 0, 3);
        gridEditable.add(cmbViaAdmin, 1, 3);
        
        infoEditable.getChildren().addAll(lblTituloEditable, gridEditable);
        
        content.getChildren().addAll(infoBasica, infoEditable);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Configurar botones
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button btnCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        
        btnOk.setText("💾 Guardar Cambios");
        btnCancel.setText("❌ Cancelar");
        
        // Manejar resultado
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Actualizar el medicamento con los nuevos datos
                medicamento.setLaboratorio(txtLaboratorio.getText().trim());
                medicamento.setPrecio(Double.parseDouble(txtPrecio.getText()));
                medicamento.setDimension(txtDimension.getText().trim());
                medicamento.setViaAdmin(cmbViaAdmin.getValue());
                
                // Refrescar la tabla
                tablaMedicamentos.refresh();
                
                mostrarInfo("Cambios guardados", 
                    "Los cambios se han guardado correctamente para " + medicamento.getNombre() + 
                    "\n\nNota: Estos cambios son locales. Para persistirlos en el servidor, " +
                    "necesitarías implementar una función de actualización.");
                
            } catch (NumberFormatException e) {
                mostrarError("Error", "El precio debe ser un número válido");
            }
        }
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
    
    private void mostrarNotificacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Actualización");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        
        // Hacer que se cierre automáticamente después de 2 segundos
        alert.show();
        
        // Opcional: cerrar automáticamente
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
            } catch (InterruptedException e) {
                // Ignorar
            }
        }).start();
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