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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
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
    @FXML private ComboBox<String> cmbVeterinario;
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
            
            // Configurar interfaz
            configurarCombos();
            configurarTablas();
            configurarEventos();
            
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
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox hbox = new HBox(5, btnEditar, btnEliminar);
                    
                    {
                        btnEditar.getStyleClass().add("btn-primary");
                        btnEliminar.getStyleClass().add("btn-danger");
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
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox hbox = new HBox(5, btnEditar, btnEliminar);
                    
                    {
                        btnEditar.getStyleClass().add("btn-primary");
                        btnEliminar.getStyleClass().add("btn-danger");
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
        
        // Veterinario
        cmbVeterinario.setValue(factura.getVeterinarioNombre());
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
    
    private boolean editarConcepto(ModeloFactura.ConceptoFactura concepto, boolean esServicio) {
        Dialog<ModeloFactura.ConceptoFactura> dialog = new Dialog<>();
        dialog.setTitle(esServicio ? "Servicio" : "Medicamento");
        dialog.setHeaderText("Ingrese los datos del " + (esServicio ? "servicio" : "medicamento"));
        
        // Crear formulario
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        TextField txtDescripcion = new TextField(concepto.getDescripcion());
        txtDescripcion.setPromptText("Descripción");
        
        TextField txtCantidad = new TextField(String.valueOf(concepto.getCantidad()));
        txtCantidad.setPromptText("Cantidad");
        
        TextField txtPrecio = new TextField(String.valueOf(concepto.getPrecioUnitario()));
        txtPrecio.setPromptText("Precio unitario");
        
        TextField txtDescuento = new TextField(String.valueOf(concepto.getDescuento()));
        txtDescuento.setPromptText("Descuento %");
        
        TextField txtIva = new TextField(String.valueOf(concepto.getTipoIva()));
        txtIva.setPromptText("IVA %");
        txtIva.setDisable(!esServicio); // Solo servicios pueden cambiar IVA
        
        content.getChildren().addAll(txtDescripcion, txtCantidad, txtPrecio, txtDescuento, txtIva);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    concepto.setDescripcion(txtDescripcion.getText());
                    concepto.setCantidad(Integer.parseInt(txtCantidad.getText()));
                    concepto.setPrecioUnitario(Double.parseDouble(txtPrecio.getText()));
                    concepto.setDescuento(Double.parseDouble(txtDescuento.getText()));
                    concepto.setTipoIva(Double.parseDouble(txtIva.getText()));
                    concepto.calcularImportes();
                    return concepto;
                } catch (NumberFormatException e) {
                    mostrarError("Error", "Por favor, ingrese valores numéricos válidos");
                    return null;
                }
            }
            return null;
        });
        
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
        factura.setVeterinarioNombre(cmbVeterinario.getValue());
        if (usuarioActual != null) {
            factura.setVeterinarioId(txtNumeroColegiado.getText());
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
        if (usuario != null) {
            cmbVeterinario.setValue(usuario.getNombre());
            txtNumeroColegiado.setText(usuario.getId().toString());
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