package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.bson.types.ObjectId;

import java.io.ObjectInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador del formulario de factura
 */
public class FacturaFormController implements Initializable {

    // Componente principal
    @FXML private BorderPane mainPane;
    @FXML private Label lblTitulo;
    
    // Datos generales
    @FXML private TextField txtNumeroFactura;
    @FXML private MFXDatePicker dpFechaEmision;
    @FXML private ComboBox<String> cmbEstado;
    
    // Datos del cliente
    @FXML private TextField txtCliente;
    @FXML private TextField txtDNI;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private Button btnSeleccionarCliente;
    
    // Datos del paciente
    @FXML private TextField txtPaciente;
    @FXML private Button btnSeleccionarPaciente;
    
    // Datos del veterinario
    @FXML private ComboBox<Usuario> cmbVeterinario;
    @FXML private TextField txtNumeroColegiado;
    
    // Servicios
    @FXML private TableView<ModeloFactura.ConceptoFactura> tablaServicios;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colServicioDescripcion;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Integer> colServicioCantidad;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Double> colServicioPrecio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Double> colServicioIVA;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colServicioSubtotal;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Void> colServicioAcciones;
    @FXML private Button btnAgregarServicio;
    
    // Medicamentos
    @FXML private TableView<ModeloFactura.ConceptoFactura> tablaMedicamentos;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colMedicamentoNombre;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Integer> colMedicamentoCantidad;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Double> colMedicamentoPrecio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Double> colMedicamentoIVA;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colMedicamentoSubtotal;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Void> colMedicamentoAcciones;
    @FXML private Button btnAgregarMedicamento;
    
    // Totales
    @FXML private Label lblSubtotalServicios;
    @FXML private Label lblSubtotalMedicamentos;
    @FXML private Label lblIVAServicios;
    @FXML private Label lblIVAMedicamentos;
    @FXML private Label lblTotal;
    
    // Observaciones
    @FXML private TextArea txtObservaciones;
    
    // Botones
    @FXML private Button btnGuardarBorrador;
    @FXML private Button btnGuardar;
    @FXML private Button btnFinalizar;
    @FXML private Button btnCancelar;
    
    // Datos
    private ModeloFactura factura;
    private Usuario usuarioActual;
    private FacturacionController facturacionController;
    private GestorSocket gestorSocket;
    private DecimalFormat formatoMoneda;
    private ObservableList<ModeloFactura.ConceptoFactura> listaServicios;
    private ObservableList<ModeloFactura.ConceptoFactura> listaMedicamentos;
    private ObservableList<Usuario> listaVeterinarios;
    
    // IDs seleccionados
    private ObjectId propietarioId;
    private ObjectId pacienteId;
    private ObjectId citaId;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            gestorSocket = GestorSocket.getInstance();
            formatoMoneda = new DecimalFormat("#,##0.00 €");
            
            // Inicializar listas
            listaServicios = FXCollections.observableArrayList();
            listaMedicamentos = FXCollections.observableArrayList();
            listaVeterinarios = FXCollections.observableArrayList();
            
            // Configurar interfaz
            configurarCombos();
            configurarTablas();
            configurarEventos();
            
            // Cargar datos del servidor
            cargarVeterinarios();
            
            // Inicializar factura nueva si no existe
            if (factura == null) {
                factura = new ModeloFactura();
                factura.setFechaEmision(new Date());
                LocalDate vencimiento = LocalDate.now().plusDays(30);
                factura.setFechaVencimiento(Date.from(vencimiento.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            
            cargarDatosEnFormulario();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de inicialización", "No se pudo inicializar el formulario: " + e.getMessage());
        }
    }
    
    private void configurarCombos() {
        // Estados
        cmbEstado.getItems().addAll("Borrador", "Emitida", "Pagada", "Vencida", "Anulada");
        cmbEstado.setValue("Borrador");
        
        // Configurar ComboBox de veterinarios
        cmbVeterinario.setItems(listaVeterinarios);
        cmbVeterinario.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                if (usuario == null) return "";
                return usuario.getNombre() + " " + usuario.getApellido() + 
                       (usuario.getEspecialidad() != null ? " - " + usuario.getEspecialidad() : "");
            }
            
            @Override
            public Usuario fromString(String string) {
                return null; // No necesario para este caso
            }
        });
        
        // Listener para actualizar número de colegiado
        cmbVeterinario.setOnAction(e -> {
            Usuario veterinario = cmbVeterinario.getValue();
            if (veterinario != null) {
                txtNumeroColegiado.setText(veterinario.getNumeroColegiado());
            }
        });
    }
    
    /**
     * Carga la lista de veterinarios desde el servidor
     */
    private void cargarVeterinarios() {
        new Thread(() -> {
            try {
                // Verificar conexión antes de enviar petición
                if (!gestorSocket.isConectado()) {
                    Platform.runLater(() -> mostrarError("Error de conexión", "No hay conexión con el servidor"));
                    return;
                }
                
                String peticion = String.valueOf(Protocolo.GETALLVETERINARIOS);
                System.out.println("Enviando petición: " + peticion);
                
                // Usar sincronización para evitar conflictos
                synchronized (gestorSocket) {
                    gestorSocket.enviarPeticion(peticion);
                    
                    ObjectInputStream entrada = gestorSocket.getEntrada();
                    if (entrada == null) {
                        System.err.println("No se pudo obtener el stream de entrada");
                        Platform.runLater(() -> mostrarError("Error", "No se pudo obtener el stream de entrada"));
                        return;
                    }
                    
                    System.out.println("Esperando respuesta del servidor...");
                    
                    // Usar timeout más corto y manejar mejor los errores
                    try {
                        int codigoRespuesta = entrada.readInt();
                        System.out.println("Código de respuesta recibido: " + codigoRespuesta);
                        
                        if (codigoRespuesta == Protocolo.GETALLVETERINARIOS_RESPONSE) {
                            @SuppressWarnings("unchecked")
                            List<Usuario> veterinarios = (List<Usuario>) entrada.readObject();
                            
                            Platform.runLater(() -> {
                                listaVeterinarios.clear();
                                if (veterinarios != null && !veterinarios.isEmpty()) {
                                    listaVeterinarios.addAll(veterinarios);
                                    System.out.println("Veterinarios cargados exitosamente: " + veterinarios.size());
                                    
                                    // Auto-seleccionar el usuario actual si es veterinario
                                    if (usuarioActual != null && usuarioActual.getRol() == Usuario.Rol.VETERINARIO) {
                                        for (Usuario vet : listaVeterinarios) {
                                            if (vet.getId().equals(usuarioActual.getId())) {
                                                cmbVeterinario.setValue(vet);
                                                System.out.println("Auto-seleccionado veterinario actual: " + vet.getNombre());
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    System.out.println("No se encontraron veterinarios en la respuesta");
                                }
                            });
                        } else if (codigoRespuesta == Protocolo.ERRORGETALLVETERINARIOS) {
                            System.err.println("Error del servidor al obtener veterinarios");
                            Platform.runLater(() -> mostrarError("Error", "Error del servidor al obtener los veterinarios"));
                        } else {
                            System.err.println("Respuesta inesperada del servidor: " + codigoRespuesta);
                            Platform.runLater(() -> mostrarError("Error", "Respuesta inesperada del servidor: " + codigoRespuesta));
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        System.err.println("Timeout al esperar respuesta del servidor");
                        Platform.runLater(() -> mostrarError("Error de timeout", "El servidor tardó demasiado en responder. Intente más tarde."));
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error al cargar veterinarios: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    // Si hay error, al menos permitir continuar sin veterinarios
                    System.out.println("Continuando sin cargar veterinarios debido a error de conexión");
                });
            }
        }).start();
    }
    
    private void configurarTablas() {
        configurarTablaServicios();
        configurarTablaMedicamentos();
    }
    
    private void configurarTablaServicios() {
        colServicioDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colServicioCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colServicioPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colServicioIVA.setCellValueFactory(new PropertyValueFactory<>("tipoIva"));
        colServicioSubtotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        // Columna de acciones
        colServicioAcciones.setCellFactory(new Callback<TableColumn<ModeloFactura.ConceptoFactura, Void>, TableCell<ModeloFactura.ConceptoFactura, Void>>() {
            @Override
            public TableCell<ModeloFactura.ConceptoFactura, Void> call(TableColumn<ModeloFactura.ConceptoFactura, Void> param) {
                return new TableCell<ModeloFactura.ConceptoFactura, Void>() {
                    private final Button btnEditar = new Button("✏️");
                    private final Button btnEliminar = new Button("🗑️");
                    private final HBox hbox = new HBox(8, btnEditar, btnEliminar);
                    
                    {
                        btnEditar.getStyleClass().addAll("btn-secondary", "btn-small");
                        btnEliminar.getStyleClass().addAll("btn-danger", "btn-small");
                        hbox.setAlignment(Pos.CENTER);
                        
                        btnEditar.setOnAction(e -> {
                            ModeloFactura.ConceptoFactura concepto = getTableView().getItems().get(getIndex());
                            editarConcepto(concepto, true);
                        });
                        
                        btnEliminar.setOnAction(e -> {
                            ModeloFactura.ConceptoFactura concepto = getTableView().getItems().get(getIndex());
                            listaServicios.remove(concepto);
                            calcularTotales();
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
        
        tablaServicios.setItems(listaServicios);
    }
    
    private void configurarTablaMedicamentos() {
        colMedicamentoNombre.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colMedicamentoCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colMedicamentoPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colMedicamentoIVA.setCellValueFactory(new PropertyValueFactory<>("tipoIva"));
        colMedicamentoSubtotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        // Columna de acciones
        colMedicamentoAcciones.setCellFactory(new Callback<TableColumn<ModeloFactura.ConceptoFactura, Void>, TableCell<ModeloFactura.ConceptoFactura, Void>>() {
            @Override
            public TableCell<ModeloFactura.ConceptoFactura, Void> call(TableColumn<ModeloFactura.ConceptoFactura, Void> param) {
                return new TableCell<ModeloFactura.ConceptoFactura, Void>() {
                    private final Button btnEditar = new Button("✏️");
                    private final Button btnEliminar = new Button("🗑️");
                    private final HBox hbox = new HBox(8, btnEditar, btnEliminar);
                    
                    {
                        btnEditar.getStyleClass().addAll("btn-secondary", "btn-small");
                        btnEliminar.getStyleClass().addAll("btn-danger", "btn-small");
                        hbox.setAlignment(Pos.CENTER);
                        
                        btnEditar.setOnAction(e -> {
                            ModeloFactura.ConceptoFactura concepto = getTableView().getItems().get(getIndex());
                            editarConcepto(concepto, false);
                        });
                        
                        btnEliminar.setOnAction(e -> {
                            ModeloFactura.ConceptoFactura concepto = getTableView().getItems().get(getIndex());
                            listaMedicamentos.remove(concepto);
                            calcularTotales();
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
        
        tablaMedicamentos.setItems(listaMedicamentos);
    }
    
    private void configurarEventos() {
        btnSeleccionarCliente.setOnAction(e -> onSeleccionarCliente());
        btnSeleccionarPaciente.setOnAction(e -> onSeleccionarPaciente());
        btnAgregarServicio.setOnAction(e -> onAgregarServicio());
        btnAgregarMedicamento.setOnAction(e -> onAgregarMedicamento());
        btnGuardarBorrador.setOnAction(e -> onGuardarBorrador());
        btnGuardar.setOnAction(e -> onGuardar());
        btnFinalizar.setOnAction(e -> onFinalizar());
        btnCancelar.setOnAction(e -> onCancelar());
        
        // Listeners para recalcular totales
        listaServicios.addListener((javafx.collections.ListChangeListener<ModeloFactura.ConceptoFactura>) c -> calcularTotales());
        listaMedicamentos.addListener((javafx.collections.ListChangeListener<ModeloFactura.ConceptoFactura>) c -> calcularTotales());
    }
    
    private void cargarDatosEnFormulario() {
        if (factura == null) return;
        
        // Datos generales
        txtNumeroFactura.setText(factura.getNumeroFactura());
        if (factura.getFechaEmision() != null) {
            dpFechaEmision.setValue(factura.getFechaEmision().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        if (factura.getFechaVencimiento() != null) {
            dpFechaEmision.setValue(factura.getFechaVencimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        if (factura.getEstado() != null) {
            cmbEstado.setValue(factura.getEstado().getDescripcion());
        }
        
        // Datos del cliente
        txtCliente.setText(factura.getNombreCliente());
        txtDNI.setText(factura.getDniCliente());
        txtTelefono.setText(factura.getTelefonoCliente());
        txtDireccion.setText(factura.getDireccionCliente());
        propietarioId = factura.getPropietarioId();
        
        // Datos del paciente
        txtPaciente.setText(factura.getNombrePaciente());
        pacienteId = factura.getPacienteId();
        
        // Veterinario - buscar en la lista por nombre
        if (factura.getVeterinarioNombre() != null) {
            for (Usuario vet : listaVeterinarios) {
                if (vet.getNombre().equals(factura.getVeterinarioNombre())) {
                    cmbVeterinario.setValue(vet);
                    break;
                }
            }
        }
        txtNumeroColegiado.setText(factura.getVeterinarioId());
        
        // Servicios y medicamentos
        if (factura.getServicios() != null) {
            listaServicios.addAll(factura.getServicios());
        }
        if (factura.getMedicamentos() != null) {
            listaMedicamentos.addAll(factura.getMedicamentos());
        }
        
        // Observaciones
        txtObservaciones.setText(factura.getObservaciones());
        
        // IDs
        citaId = factura.getCitaId();
        
        calcularTotales();
    }
    
    private void seleccionarCliente() {
        // Implementar selector de cliente
        mostrarInfo("Funcionalidad", "Selector de cliente en desarrollo");
    }
    
    private void seleccionarPaciente() {
        // Implementar selector de paciente
        mostrarInfo("Funcionalidad", "Selector de paciente en desarrollo");
    }
    
    private void agregarServicio() {
        ModeloFactura.ConceptoFactura concepto = new ModeloFactura.ConceptoFactura();
        concepto.setTipoIva(21.0); // IVA general para servicios
        
        if (editarConcepto(concepto, true)) {
            listaServicios.add(concepto);
            calcularTotales();
        }
    }
    
    private void agregarMedicamento() {
        ModeloFactura.ConceptoFactura concepto = new ModeloFactura.ConceptoFactura();
        concepto.setTipoIva(10.0); // IVA reducido para medicamentos
        
        if (editarConcepto(concepto, false)) {
            listaMedicamentos.add(concepto);
            calcularTotales();
        }
    }
    
    /**
     * Diálogo mejorado para editar conceptos (servicios/medicamentos)
     */
    private boolean editarConcepto(ModeloFactura.ConceptoFactura concepto, boolean esServicio) {
        Dialog<ModeloFactura.ConceptoFactura> dialog = new Dialog<>();
        dialog.setTitle(esServicio ? "🛠️ Servicio Veterinario" : "💊 Medicamento");
        dialog.setHeaderText("Complete la información del " + (esServicio ? "servicio" : "medicamento"));
        
        // Configurar el diálogo con estilo moderno
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/facturacion-styles.css").toExternalForm());
        dialogPane.getStyleClass().add("modern-dialog");
        
        // Crear formulario con GridPane
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.getStyleClass().add("form-grid");
        
        // Campos del formulario
        TextField txtDescripcion = new TextField(concepto.getDescripcion());
        txtDescripcion.setPromptText(esServicio ? "Ej: Consulta general, Vacunación..." : "Ej: Antibiótico, Antiinflamatorio...");
        txtDescripcion.getStyleClass().add("form-field");
        
        TextField txtCantidad = new TextField(concepto.getCantidad() > 0 ? String.valueOf(concepto.getCantidad()) : "1");
        txtCantidad.setPromptText("Cantidad");
        txtCantidad.getStyleClass().add("form-field");
        
        TextField txtPrecio = new TextField(concepto.getPrecioUnitario() > 0 ? String.valueOf(concepto.getPrecioUnitario()) : "");
        txtPrecio.setPromptText("Precio unitario (€)");
        txtPrecio.getStyleClass().add("form-field");
        
        TextField txtDescuento = new TextField(concepto.getDescuento() > 0 ? String.valueOf(concepto.getDescuento()) : "0");
        txtDescuento.setPromptText("Descuento (%)");
        txtDescuento.getStyleClass().add("form-field");
        
        ComboBox<Double> cmbIva = new ComboBox<>();
        if (esServicio) {
            cmbIva.getItems().addAll(0.0, 4.0, 10.0, 21.0);
            cmbIva.setValue(21.0);
        } else {
            cmbIva.getItems().addAll(0.0, 4.0, 10.0);
            cmbIva.setValue(10.0);
        }
        cmbIva.getStyleClass().add("form-combobox");
        
        // Labels con estilo
        Label lblDescripcion = new Label("Descripción:");
        lblDescripcion.getStyleClass().add("form-label");
        lblDescripcion.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label lblCantidad = new Label("Cantidad:");
        lblCantidad.getStyleClass().add("form-label");
        lblCantidad.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label lblPrecio = new Label("Precio Unitario:");
        lblPrecio.getStyleClass().add("form-label");
        lblPrecio.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label lblDescuento = new Label("Descuento (%):");
        lblDescuento.getStyleClass().add("form-label");
        lblDescuento.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label lblIva = new Label("IVA (%):");
        lblIva.getStyleClass().add("form-label");
        lblIva.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Añadir campos al grid
        grid.add(lblDescripcion, 0, 0);
        grid.add(txtDescripcion, 1, 0, 2, 1);
        
        grid.add(lblCantidad, 0, 1);
        grid.add(txtCantidad, 1, 1);
        
        grid.add(lblPrecio, 2, 1);
        grid.add(txtPrecio, 3, 1);
        
        grid.add(lblDescuento, 0, 2);
        grid.add(txtDescuento, 1, 2);
        
        grid.add(lblIva, 2, 2);
        grid.add(cmbIva, 3, 2);
        
        // Área de vista previa del total
        VBox previewBox = new VBox(10);
        previewBox.getStyleClass().add("preview-box");
        previewBox.setPadding(new Insets(15));
        previewBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        
        Label lblPreview = new Label("Vista Previa:");
        lblPreview.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label lblSubtotal = new Label("Subtotal: 0,00 €");
        Label lblIvaAmount = new Label("IVA: 0,00 €");
        Label lblTotalPreview = new Label("Total: 0,00 €");
        lblTotalPreview.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTotalPreview.setStyle("-fx-text-fill: #2563eb;");
        
        previewBox.getChildren().addAll(lblPreview, lblSubtotal, lblIvaAmount, new Separator(), lblTotalPreview);
        
        // Función para actualizar la vista previa
        Runnable updatePreview = () -> {
            try {
                double cantidad = Double.parseDouble(txtCantidad.getText().isEmpty() ? "0" : txtCantidad.getText());
                double precio = Double.parseDouble(txtPrecio.getText().isEmpty() ? "0" : txtPrecio.getText());
                double descuento = Double.parseDouble(txtDescuento.getText().isEmpty() ? "0" : txtDescuento.getText());
                double iva = cmbIva.getValue();
                
                double subtotal = cantidad * precio;
                double descuentoAmount = subtotal * (descuento / 100);
                double subtotalConDescuento = subtotal - descuentoAmount;
                double ivaAmount = subtotalConDescuento * (iva / 100);
                double total = subtotalConDescuento + ivaAmount;
                
                lblSubtotal.setText("Subtotal: " + formatoMoneda.format(subtotalConDescuento));
                lblIvaAmount.setText("IVA (" + iva + "%): " + formatoMoneda.format(ivaAmount));
                lblTotalPreview.setText("Total: " + formatoMoneda.format(total));
            } catch (NumberFormatException e) {
                lblSubtotal.setText("Subtotal: 0,00 €");
                lblIvaAmount.setText("IVA: 0,00 €");
                lblTotalPreview.setText("Total: 0,00 €");
            }
        };
        
        // Listeners para actualizar vista previa
        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        txtPrecio.textProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        txtDescuento.textProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        cmbIva.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        
        // Contenedor principal
        VBox mainContent = new VBox(20);
        mainContent.getChildren().addAll(grid, previewBox);
        
        dialog.getDialogPane().setContent(mainContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Estilizar botones
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        
        okButton.getStyleClass().addAll("btn-primary", "btn-large");
        cancelButton.getStyleClass().addAll("btn-secondary", "btn-large");
        
        okButton.setText("✅ Guardar");
        cancelButton.setText("❌ Cancelar");
        
        // Validación y resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    if (txtDescripcion.getText().trim().isEmpty()) {
                        mostrarError("Error", "La descripción es obligatoria");
                        return null;
                    }
                    
                    concepto.setDescripcion(txtDescripcion.getText().trim());
                    concepto.setCantidad(Integer.parseInt(txtCantidad.getText()));
                    concepto.setPrecioUnitario(Double.parseDouble(txtPrecio.getText()));
                    concepto.setDescuento(Double.parseDouble(txtDescuento.getText()));
                    concepto.setTipoIva(cmbIva.getValue());
                    concepto.calcularImportes();
                    return concepto;
                } catch (NumberFormatException e) {
                    mostrarError("Error", "Por favor, ingrese valores numéricos válidos");
                    return null;
                }
            }
            return null;
        });
        
        // Actualizar vista previa inicial
        updatePreview.run();
        
        // Enfocar el primer campo
        Platform.runLater(() -> txtDescripcion.requestFocus());
        
        Optional<ModeloFactura.ConceptoFactura> result = dialog.showAndWait();
        return result.isPresent();
    }
    
    private void calcularTotales() {
        double subtotalServicios = 0.0;
        double ivaServicios = 0.0;
        double subtotalMedicamentos = 0.0;
        double ivaMedicamentos = 0.0;
        
        // Calcular servicios
        for (ModeloFactura.ConceptoFactura servicio : listaServicios) {
            subtotalServicios += servicio.getSubtotal();
            ivaServicios += servicio.getImporteIva();
        }
        
        // Calcular medicamentos
        for (ModeloFactura.ConceptoFactura medicamento : listaMedicamentos) {
            subtotalMedicamentos += medicamento.getSubtotal();
            ivaMedicamentos += medicamento.getImporteIva();
        }
        
        double total = subtotalServicios + ivaServicios + subtotalMedicamentos + ivaMedicamentos;
        
        // Actualizar labels
        lblSubtotalServicios.setText(formatoMoneda.format(subtotalServicios));
        lblIVAServicios.setText(formatoMoneda.format(ivaServicios));
        lblSubtotalMedicamentos.setText(formatoMoneda.format(subtotalMedicamentos));
        lblIVAMedicamentos.setText(formatoMoneda.format(ivaMedicamentos));
        lblTotal.setText(formatoMoneda.format(total));
    }
    
    private void guardarBorrador() {
        try {
            actualizarFacturaDesdeFormulario();
            factura.setEsBorrador(true);
            factura.setEstado(ModeloFactura.EstadoFactura.BORRADOR);
            
            guardarFactura();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo guardar el borrador: " + e.getMessage());
        }
    }
    
    private void finalizarFactura() {
        try {
            if (!validarFormulario()) {
                return;
            }
            
            actualizarFacturaDesdeFormulario();
            factura.finalizar();
            
            guardarFactura();
            
            // Cambiar estado de cita si existe
            if (citaId != null) {
                cambiarEstadoCita();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo finalizar la factura: " + e.getMessage());
        }
    }
    
    private void actualizarFacturaDesdeFormulario() {
        // Datos generales
        factura.setNumeroFactura(txtNumeroFactura.getText());
        if (dpFechaEmision.getValue() != null) {
            factura.setFechaEmision(Date.from(dpFechaEmision.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        factura.setMetodoPago(cmbEstado.getValue());
        
        // Datos del cliente
        factura.setNombreCliente(txtCliente.getText());
        factura.setDniCliente(txtDNI.getText());
        factura.setTelefonoCliente(txtTelefono.getText());
        factura.setDireccionCliente(txtDireccion.getText());
        factura.setPropietarioId(propietarioId);
        
        // Datos del paciente
        factura.setNombrePaciente(txtPaciente.getText());
        factura.setPacienteId(pacienteId);
        
        // Veterinario
        Usuario veterinario = cmbVeterinario.getValue();
        if (veterinario != null) {
            factura.setVeterinarioNombre(veterinario.getNombre() + " " + veterinario.getApellido());
            factura.setVeterinarioId(veterinario.getNumeroColegiado());
        }
        if (usuarioActual != null) {
            factura.setUsuarioCreacion(usuarioActual.getUsuario());
        }
        
        // Servicios y medicamentos
        factura.getServicios().clear();
        factura.getServicios().addAll(listaServicios);
        factura.getMedicamentos().clear();
        factura.getMedicamentos().addAll(listaMedicamentos);
        
        // Observaciones
        factura.setObservaciones(txtObservaciones.getText());
        
        // Cita
        factura.setCitaId(citaId);
        
        // Calcular importes
        factura.calcularImportes();
    }
    
    private boolean validarFormulario() {
        if (txtCliente.getText().trim().isEmpty()) {
            mostrarError("Validación", "El nombre del cliente es obligatorio");
            return false;
        }
        
        if (txtPaciente.getText().trim().isEmpty()) {
            mostrarError("Validación", "El nombre del paciente es obligatorio");
            return false;
        }
        
        if (cmbVeterinario.getValue() == null) {
            mostrarError("Validación", "Debe seleccionar un veterinario");
            return false;
        }
        
        if (listaServicios.isEmpty() && listaMedicamentos.isEmpty()) {
            mostrarError("Validación", "Debe agregar al menos un servicio o medicamento");
            return false;
        }
        
        return true;
    }
    
    private void guardarFactura() {
        new Thread(() -> {
            try {
                String peticion;
                if (factura.getId() == null) {
                    peticion = String.valueOf(Protocolo.CREAR_FACTURA);
                } else {
                    peticion = String.valueOf(Protocolo.ACTUALIZAR_FACTURA);
                }
                
                gestorSocket.enviarPeticion(peticion);
                gestorSocket.getSalida().writeObject(factura);
                gestorSocket.getSalida().flush();
                
                ObjectInputStream entrada = gestorSocket.getEntrada();
                int codigoRespuesta = entrada.readInt();
                
                if (codigoRespuesta == Protocolo.CREAR_FACTURA_RESPONSE || 
                    codigoRespuesta == Protocolo.ACTUALIZAR_FACTURA_RESPONSE) {
                    
                    Platform.runLater(() -> {
                        mostrarInfo("Éxito", "Factura guardada correctamente");
                        if (facturacionController != null) {
                            facturacionController.actualizarListas();
                        }
                        cerrarVentana();
                    });
                } else {
                    Platform.runLater(() -> mostrarError("Error", "No se pudo guardar la factura"));
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error", "Error de comunicación: " + e.getMessage()));
            }
        }).start();
    }
    
    private void cambiarEstadoCita() {
        new Thread(() -> {
            try {
                String peticion = Protocolo.CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR + 
                                Protocolo.SEPARADOR_CODIGO + citaId.toString();
                gestorSocket.enviarPeticion(peticion);
                
                ObjectInputStream entrada = gestorSocket.getEntrada();
                int codigoRespuesta = entrada.readInt();
                
                if (codigoRespuesta != Protocolo.CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR_RESPONSE) {
                    System.err.println("No se pudo cambiar el estado de la cita");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void cancelar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText("¿Cancelar edición?");
        alert.setContentText("Se perderán los cambios no guardados.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cerrarVentana();
        }
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.close();
    }
    
    // Métodos públicos para configurar el controlador
    public void setFactura(ModeloFactura factura) {
        this.factura = factura;
        if (factura != null) {
            Platform.runLater(this::cargarDatosEnFormulario);
        }
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        System.out.println("Usuario actual establecido: " + (usuario != null ? usuario.getNombre() + " (" + usuario.getRol() + ")" : "null"));
        
        if (usuario != null && usuario.getRol() == Usuario.Rol.VETERINARIO) {
            // Si el usuario actual es veterinario, intentar seleccionarlo automáticamente
            Platform.runLater(() -> {
                // Si ya hay veterinarios cargados, seleccionar inmediatamente
                if (!listaVeterinarios.isEmpty()) {
                    for (Usuario vet : listaVeterinarios) {
                        if (vet.getId().equals(usuario.getId())) {
                            cmbVeterinario.setValue(vet);
                            System.out.println("Veterinario auto-seleccionado inmediatamente: " + vet.getNombre());
                            break;
                        }
                    }
                }
                // Si no hay veterinarios cargados aún, la auto-selección se hará en cargarVeterinarios()
            });
        }
    }
    
    public void setFacturacionController(FacturacionController controller) {
        this.facturacionController = controller;
    }
    
    public void cargarDatosDesdeCita(ModeloCita cita, ModeloPaciente paciente, ModeloPropietario propietario) {
        if (cita != null) {
            this.citaId = cita.getId();
            dpFechaEmision.setValue(LocalDate.now());
        }
        
        if (paciente != null) {
            this.pacienteId = paciente.getId();
            txtPaciente.setText(paciente.getNombre());
        }
        
        if (propietario != null) {
            this.propietarioId = propietario.getId();
            txtCliente.setText(propietario.getNombre());
            txtDNI.setText(propietario.getDni());
            txtTelefono.setText(propietario.getTelefono());
            txtDireccion.setText(propietario.getDireccion());
        }
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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

    // Métodos FXML referenciados en el archivo FXML
    @FXML
    private void onSeleccionarCliente() {
        seleccionarCliente();
    }

    @FXML
    private void onSeleccionarPaciente() {
        seleccionarPaciente();
    }

    @FXML
    private void onAgregarServicio() {
        agregarServicio();
    }

    @FXML
    private void onAgregarMedicamento() {
        agregarMedicamento();
    }

    @FXML
    private void onGuardarBorrador() {
        guardarBorrador();
    }

    @FXML
    private void onGuardar() {
        guardarFactura();
    }

    @FXML
    private void onFinalizar() {
        finalizarFactura();
    }

    @FXML
    private void onCancelar() {
        cancelar();
    }
} 