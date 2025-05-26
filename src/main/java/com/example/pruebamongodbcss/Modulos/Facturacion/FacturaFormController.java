package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import com.jfoenix.controls.*;
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

    // Datos generales
    @FXML private VBox rootPane;
    @FXML private JFXTextField txtNumeroFactura;
    @FXML private JFXDatePicker dpFechaEmision;
    @FXML private JFXDatePicker dpFechaVencimiento;
    @FXML private JFXComboBox<String> cmbEstado;
    @FXML private JFXComboBox<String> cmbMetodoPago;
    
    // Datos del cliente
    @FXML private JFXTextField txtNombreCliente;
    @FXML private JFXTextField txtDniCliente;
    @FXML private JFXTextField txtDireccionCliente;
    @FXML private JFXTextField txtTelefonoCliente;
    @FXML private JFXTextField txtEmailCliente;
    @FXML private JFXButton btnSeleccionarCliente;
    
    // Datos del paciente
    @FXML private JFXTextField txtNombrePaciente;
    @FXML private JFXTextField txtEspeciePaciente;
    @FXML private JFXTextField txtRazaPaciente;
    @FXML private JFXButton btnSeleccionarPaciente;
    
    // Datos del veterinario
    @FXML private JFXTextField txtVeterinario;
    
    // Servicios
    @FXML private TableView<ModeloFactura.ConceptoFactura> tableServicios;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colDescripcionServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Integer> colCantidadServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Double> colPrecioServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Double> colDescuentoServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colTotalServicio;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Void> colAccionesServicio;
    @FXML private JFXButton btnAgregarServicio;
    
    // Medicamentos
    @FXML private TableView<ModeloFactura.ConceptoFactura> tableMedicamentos;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colDescripcionMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Integer> colCantidadMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Double> colPrecioMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Double> colDescuentoMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, String> colTotalMedicamento;
    @FXML private TableColumn<ModeloFactura.ConceptoFactura, Void> colAccionesMedicamento;
    @FXML private JFXButton btnAgregarMedicamento;
    
    // Totales
    @FXML private Label lblSubtotal;
    @FXML private Label lblIvaGeneral;
    @FXML private Label lblIvaMedicamentos;
    @FXML private Label lblTotal;
    
    // Observaciones
    @FXML private JFXTextArea txtObservaciones;
    
    // Botones
    @FXML private JFXButton btnGuardarBorrador;
    @FXML private JFXButton btnFinalizar;
    @FXML private JFXButton btnCancelar;
    
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
        
        // Métodos de pago
        cmbMetodoPago.getItems().addAll(
            "Efectivo", "Tarjeta de crédito", "Tarjeta de débito", 
            "Transferencia bancaria", "Bizum", "PayPal"
        );
        cmbMetodoPago.setValue("Efectivo");
    }
    
    private void configurarTablas() {
        configurarTablaServicios();
        configurarTablaMedicamentos();
    }
    
    private void configurarTablaServicios() {
        colDescripcionServicio.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCantidadServicio.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioServicio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colDescuentoServicio.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        colTotalServicio.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        // Columna de acciones
        colAccionesServicio.setCellFactory(new Callback<TableColumn<ModeloFactura.ConceptoFactura, Void>, TableCell<ModeloFactura.ConceptoFactura, Void>>() {
            @Override
            public TableCell<ModeloFactura.ConceptoFactura, Void> call(TableColumn<ModeloFactura.ConceptoFactura, Void> param) {
                return new TableCell<ModeloFactura.ConceptoFactura, Void>() {
                    private final JFXButton btnEditar = new JFXButton("Editar");
                    private final JFXButton btnEliminar = new JFXButton("Eliminar");
                    private final HBox hbox = new HBox(5, btnEditar, btnEliminar);
                    
                    {
                        btnEditar.getStyleClass().addAll("btn-primary", "btn-sm");
                        btnEliminar.getStyleClass().addAll("btn-danger", "btn-sm");
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
        
        tableServicios.setItems(listaServicios);
    }
    
    private void configurarTablaMedicamentos() {
        colDescripcionMedicamento.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCantidadMedicamento.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioMedicamento.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colDescuentoMedicamento.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        colTotalMedicamento.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(formatoMoneda.format(total));
        });
        
        // Columna de acciones
        colAccionesMedicamento.setCellFactory(new Callback<TableColumn<ModeloFactura.ConceptoFactura, Void>, TableCell<ModeloFactura.ConceptoFactura, Void>>() {
            @Override
            public TableCell<ModeloFactura.ConceptoFactura, Void> call(TableColumn<ModeloFactura.ConceptoFactura, Void> param) {
                return new TableCell<ModeloFactura.ConceptoFactura, Void>() {
                    private final JFXButton btnEditar = new JFXButton("Editar");
                    private final JFXButton btnEliminar = new JFXButton("Eliminar");
                    private final HBox hbox = new HBox(5, btnEditar, btnEliminar);
                    
                    {
                        btnEditar.getStyleClass().addAll("btn-primary", "btn-sm");
                        btnEliminar.getStyleClass().addAll("btn-danger", "btn-sm");
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
        
        tableMedicamentos.setItems(listaMedicamentos);
    }
    
    private void configurarEventos() {
        btnSeleccionarCliente.setOnAction(e -> seleccionarCliente());
        btnSeleccionarPaciente.setOnAction(e -> seleccionarPaciente());
        btnAgregarServicio.setOnAction(e -> agregarServicio());
        btnAgregarMedicamento.setOnAction(e -> agregarMedicamento());
        btnGuardarBorrador.setOnAction(e -> guardarBorrador());
        btnFinalizar.setOnAction(e -> finalizarFactura());
        btnCancelar.setOnAction(e -> cancelar());
        
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
            dpFechaVencimiento.setValue(factura.getFechaVencimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        if (factura.getEstado() != null) {
            cmbEstado.setValue(factura.getEstado().getDescripcion());
        }
        cmbMetodoPago.setValue(factura.getMetodoPago());
        
        // Datos del cliente
        txtNombreCliente.setText(factura.getNombreCliente());
        txtDniCliente.setText(factura.getDniCliente());
        txtDireccionCliente.setText(factura.getDireccionCliente());
        txtTelefonoCliente.setText(factura.getTelefonoCliente());
        txtEmailCliente.setText(factura.getEmailCliente());
        propietarioId = factura.getPropietarioId();
        
        // Datos del paciente
        txtNombrePaciente.setText(factura.getNombrePaciente());
        txtEspeciePaciente.setText(factura.getEspeciePaciente());
        txtRazaPaciente.setText(factura.getRazaPaciente());
        pacienteId = factura.getPacienteId();
        
        // Veterinario
        txtVeterinario.setText(factura.getVeterinarioNombre());
        
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
        
        JFXTextField txtDescripcion = new JFXTextField(concepto.getDescripcion());
        txtDescripcion.setPromptText("Descripción");
        txtDescripcion.setLabelFloat(true);
        
        JFXTextField txtCantidad = new JFXTextField(String.valueOf(concepto.getCantidad()));
        txtCantidad.setPromptText("Cantidad");
        txtCantidad.setLabelFloat(true);
        
        JFXTextField txtPrecio = new JFXTextField(String.valueOf(concepto.getPrecioUnitario()));
        txtPrecio.setPromptText("Precio unitario");
        txtPrecio.setLabelFloat(true);
        
        JFXTextField txtDescuento = new JFXTextField(String.valueOf(concepto.getDescuento()));
        txtDescuento.setPromptText("Descuento %");
        txtDescuento.setLabelFloat(true);
        
        JFXTextField txtIva = new JFXTextField(String.valueOf(concepto.getTipoIva()));
        txtIva.setPromptText("IVA %");
        txtIva.setLabelFloat(true);
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
        double subtotal = 0.0;
        double ivaGeneral = 0.0;
        double ivaMedicamentos = 0.0;
        
        // Calcular servicios
        for (ModeloFactura.ConceptoFactura servicio : listaServicios) {
            subtotal += servicio.getSubtotal();
            ivaGeneral += servicio.getImporteIva();
        }
        
        // Calcular medicamentos
        for (ModeloFactura.ConceptoFactura medicamento : listaMedicamentos) {
            subtotal += medicamento.getSubtotal();
            ivaMedicamentos += medicamento.getImporteIva();
        }
        
        double total = subtotal + ivaGeneral + ivaMedicamentos;
        
        // Actualizar labels
        lblSubtotal.setText(formatoMoneda.format(subtotal));
        lblIvaGeneral.setText(formatoMoneda.format(ivaGeneral));
        lblIvaMedicamentos.setText(formatoMoneda.format(ivaMedicamentos));
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
        if (dpFechaVencimiento.getValue() != null) {
            factura.setFechaVencimiento(Date.from(dpFechaVencimiento.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        factura.setMetodoPago(cmbMetodoPago.getValue());
        
        // Datos del cliente
        factura.setNombreCliente(txtNombreCliente.getText());
        factura.setDniCliente(txtDniCliente.getText());
        factura.setDireccionCliente(txtDireccionCliente.getText());
        factura.setTelefonoCliente(txtTelefonoCliente.getText());
        factura.setEmailCliente(txtEmailCliente.getText());
        factura.setPropietarioId(propietarioId);
        
        // Datos del paciente
        factura.setNombrePaciente(txtNombrePaciente.getText());
        factura.setEspeciePaciente(txtEspeciePaciente.getText());
        factura.setRazaPaciente(txtRazaPaciente.getText());
        factura.setPacienteId(pacienteId);
        
        // Veterinario
        factura.setVeterinarioNombre(txtVeterinario.getText());
        if (usuarioActual != null) {
            factura.setVeterinarioId(usuarioActual.getId().toString());
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
        if (txtNombreCliente.getText().trim().isEmpty()) {
            mostrarError("Validación", "El nombre del cliente es obligatorio");
            return false;
        }
        
        if (txtNombrePaciente.getText().trim().isEmpty()) {
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
        Stage stage = (Stage) rootPane.getScene().getWindow();
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
            txtVeterinario.setText(usuario.getNombre());
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
            txtNombrePaciente.setText(paciente.getNombre());
            txtEspeciePaciente.setText(paciente.getEspecie());
            txtRazaPaciente.setText(paciente.getRaza());
        }
        
        if (propietario != null) {
            this.propietarioId = propietario.getId();
            txtNombreCliente.setText(propietario.getNombre());
            txtDniCliente.setText(propietario.getDni());
            txtDireccionCliente.setText(propietario.getDireccion());
            txtTelefonoCliente.setText(propietario.getTelefono());
            txtEmailCliente.setText(propietario.getEmail());
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
} 