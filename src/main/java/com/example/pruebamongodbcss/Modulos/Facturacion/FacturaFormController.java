package com.example.pruebamongodbcss.Modulos.Facturacion;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Modulos.Inventario.BuscadorMedicamentosController;
import com.example.pruebamongodbcss.Modulos.Inventario.ModeloMedicamentoInventario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import com.example.pruebamongodbcss.Utilidades.GestorSocketInventario;
import com.example.pruebamongodbcss.Utilidades.ProtocoloInventarioVeterinaria;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;

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
    
    // Lista para medicamentos agregados solo en esta sesión (para restablecimiento)
    private List<ModeloFactura.ConceptoFactura> medicamentosAgregadosEnSesion;
    
    // IDs seleccionados
    private ObjectId propietarioId;
    private ObjectId pacienteId;
    private ObjectId citaId;
    
    // Servicio para operaciones de calendario
    private com.example.pruebamongodbcss.calendar.CalendarService calendarService;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar el gestor de socket
            this.gestorSocket = GestorSocket.getInstance();
            
            // Inicializar el servicio de calendario
            this.calendarService = new com.example.pruebamongodbcss.calendar.CalendarService();
            
            // Configurar formateo de moneda
            this.formatoMoneda = new DecimalFormat("#,##0.00 €");
            
            // Inicializar listas
            listaServicios = FXCollections.observableArrayList();
            listaMedicamentos = FXCollections.observableArrayList();
            listaVeterinarios = FXCollections.observableArrayList();
            medicamentosAgregadosEnSesion = new ArrayList<>();
            
            // Configurar interfaz
            configurarCombos();
            configurarTablas();
            configurarEventos();
            
            // Cargar datos del servidor
            cargarVeterinarios();
            
            // Inicializar factura nueva si no existe
            if (factura == null) {
                factura = new ModeloFactura();
                // NO asignar ID - debe ser asignado por el servidor al crear
                factura.setFechaEmision(new Date());
                factura.setFechaCreacion(new Date());
                factura.setEsBorrador(true);
                factura.setEstado(ModeloFactura.EstadoFactura.BORRADOR);
                // El número de factura se generará cuando se guarde en el servidor
                LocalDate vencimiento = LocalDate.now().plusDays(30);
                factura.setFechaVencimiento(Date.from(vencimiento.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                System.out.println("🆕 Nueva factura creada sin ID (se asignará al guardar)");
                System.out.println("📄 El número de factura se asignará al guardar");
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
            GestorSocket gestorSocketVet = null;
            try {
                // Crear una conexión independiente para evitar conflictos
                gestorSocketVet = GestorSocket.crearConexionIndependiente();
                
                if (!gestorSocketVet.isConectado()) {
                    Platform.runLater(() -> mostrarError("Error de conexión", "No hay conexión con el servidor"));
                    return;
                }
                
                System.out.println("🔄 Cargando veterinarios...");
                
                // Enviar petición usando el protocolo correcto
                String peticion = Protocolo.GETALLVETERINARIOS + "|" + "VETERINARIO";
                gestorSocketVet.enviarPeticion(peticion);
                
                // El servidor espera el objeto Rol después de la petición
                gestorSocketVet.getSalida().writeObject(Usuario.Rol.VETERINARIO);
                gestorSocketVet.getSalida().flush();
                
                // Leer respuesta
                ObjectInputStream entrada = gestorSocketVet.getEntrada();
                int codigoRespuesta = entrada.readInt();
                System.out.println("📋 Código de respuesta veterinarios: " + codigoRespuesta);
                
                if (codigoRespuesta == Protocolo.GETALLVETERINARIOS_RESPONSE) {
                    @SuppressWarnings("unchecked")
                    List<Usuario> veterinarios = (List<Usuario>) entrada.readObject();
                    
                    Platform.runLater(() -> {
                        listaVeterinarios.clear();
                        if (veterinarios != null && !veterinarios.isEmpty()) {
                            listaVeterinarios.addAll(veterinarios);
                            System.out.println("✅ Veterinarios cargados exitosamente: " + veterinarios.size());
                            
                            // Auto-seleccionar el usuario actual si es veterinario
                            if (usuarioActual != null && usuarioActual.getRol() == Usuario.Rol.VETERINARIO) {
                                for (Usuario vet : listaVeterinarios) {
                                    if (vet.getId().equals(usuarioActual.getId())) {
                                        cmbVeterinario.setValue(vet);
                                        System.out.println("🎯 Auto-seleccionado veterinario actual: " + vet.getNombre());
                                        break;
                                    }
                                }
                            }
                        } else {
                            System.out.println("⚠️ No se encontraron veterinarios en la respuesta");
                        }
                    });
                } else if (codigoRespuesta == Protocolo.ERRORGETALLVETERINARIOS) {
                    System.err.println("❌ Error del servidor al obtener veterinarios");
                    Platform.runLater(() -> mostrarAdvertencia("Advertencia", 
                        "No se pudieron cargar los veterinarios. Puede continuar sin seleccionar uno."));
                } else {
                    System.err.println("❌ Respuesta inesperada del servidor: " + codigoRespuesta);
                    Platform.runLater(() -> mostrarAdvertencia("Advertencia", 
                        "Respuesta inesperada del servidor. Puede continuar sin veterinarios."));
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error al cargar veterinarios: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.out.println("⚠️ Continuando sin cargar veterinarios debido a error de conexión");
                    mostrarAdvertencia("Conexión", 
                        "No se pudieron cargar los veterinarios debido a un error de conexión. " +
                        "Puede continuar y seleccionar veterinario manualmente más tarde.");
                });
            } finally {
                // Cerrar la conexión independiente
                if (gestorSocketVet != null) {
                    try {
                        gestorSocketVet.cerrarConexion();
                        System.out.println("🔌 Conexión independiente para veterinarios cerrada");
                    } catch (Exception e) {
                        System.err.println("⚠️ Error al cerrar conexión de veterinarios: " + e.getMessage());
                    }
                }
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
                            
                            // Confirmar eliminación
                            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            confirmAlert.setTitle("Confirmar eliminación");
                            confirmAlert.setHeaderText("¿Eliminar medicamento de la factura?");
                            confirmAlert.setContentText("Se eliminará " + concepto.getDescripcion() + 
                                                       " y se restablecerá el inventario (" + concepto.getCantidad() + " unidades).");
                            
                            Optional<ButtonType> result = confirmAlert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                // Verificar si el medicamento fue agregado en esta sesión
                                boolean fueAgregadoEnSesion = medicamentosAgregadosEnSesion.contains(concepto);
                                
                                // Eliminar de la lista principal
                                listaMedicamentos.remove(concepto);
                                
                                // Si fue agregado en esta sesión, también quitarlo de esa lista
                                if (fueAgregadoEnSesion) {
                                    medicamentosAgregadosEnSesion.remove(concepto);
                                }
                                
                                calcularTotales();
                                
                                // Solo restablecer inventario si fue agregado en esta sesión
                                if (fueAgregadoEnSesion) {
                                    restablecerInventarioMedicamento(concepto);
                                } else {
                                    mostrarInfo("Medicamento eliminado", 
                                        "El medicamento " + concepto.getDescripcion() + 
                                        " se eliminó de la factura.\n\n" +
                                        "Como ya estaba en el borrador original, no se restablece el inventario.");
                                }
                            }
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
        if (factura.getNumeroFactura() != null && !factura.getNumeroFactura().isEmpty() && 
            !factura.getNumeroFactura().contains("XXXX")) {
            txtNumeroFactura.setText(factura.getNumeroFactura());
        } else {
            // Mostrar preview del formato que tendrá
            LocalDate now = LocalDate.now();
            String preview = String.format("ChichaVet-%04d%02d####", now.getYear(), now.getMonthValue());
            txtNumeroFactura.setText(preview);
            txtNumeroFactura.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
            txtNumeroFactura.setEditable(false);
        }
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
        // Verificar disponibilidad del servidor principal para inventario
        boolean servidorDisponible = false;
        try {
            servidorDisponible = gestorSocket != null && gestorSocket.isConectado();
            if (servidorDisponible) {
                // Hacer una prueba rápida de conectividad
                // Si hay problemas, se detectarán en el buscador
                System.out.println("Servidor disponible, intentando abrir buscador avanzado...");
            }
        } catch (Exception e) {
            System.err.println("Error al verificar conexión del servidor: " + e.getMessage());
            servidorDisponible = false;
        }
        
        if (servidorDisponible) {
            // Servidor disponible - intentar abrir buscador sofisticado
            System.out.println("Abriendo buscador avanzado de medicamentos...");
            abrirBuscadorMedicamentos();
        } else {
            // Servidor no disponible - usar método manual tradicional
            System.out.println("Servidor no disponible, usando método manual...");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Servidor Principal");
            alert.setHeaderText("Servidor no disponible");
            alert.setContentText("El servidor principal no está disponible para acceder al inventario.\n" +
                               "Se abrirá el formulario manual para agregar medicamentos.");
            alert.showAndWait();
            
            agregarMedicamentoManual();
        }
    }
    
    /**
     * Abre el buscador sofisticado de medicamentos del inventario
     */
    private void abrirBuscadorMedicamentos() {
        try {
            // Verificar que existe el archivo FXML
            URL fxmlUrl = getClass().getResource("/com/example/pruebamongodbcss/Modulos/Inventario/buscador-medicamentos.fxml");
            if (fxmlUrl == null) {
                System.err.println("No se encontró el archivo FXML del buscador de medicamentos");
                mostrarAdvertencia("Buscador no disponible", 
                    "El buscador avanzado de medicamentos no está disponible. Se usará el método manual.");
                agregarMedicamentoManual();
                return;
            }
            
            // Cargar el FXML del buscador
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Obtener el controlador
            BuscadorMedicamentosController controller = loader.getController();
            if (controller == null) {
                System.err.println("No se pudo obtener el controlador del buscador");
                throw new RuntimeException("Controlador no disponible");
            }
            
            // Configurar callback para cuando se seleccione un medicamento
            controller.setCallbackSeleccion(this::procesarMedicamentoSeleccionado);
            
            // Crear y configurar la ventana
            Stage stage = new Stage();
            stage.setTitle("🔍 Buscador de Medicamentos - Inventario");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainPane.getScene().getWindow());
            
            // Configurar la escena
            Scene scene = new Scene(root, 1400, 800);
            
            // Agregar estilos si existen
            try {
                URL stylesUrl = getClass().getResource("/com/example/pruebamongodbcss/Modulos/Inventario/inventario-styles.css");
                if (stylesUrl != null) {
                    scene.getStylesheets().add(stylesUrl.toExternalForm());
                } else {
                    // Intentar con estilos de facturación
                    URL facturacionStylesUrl = getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/facturacion-styles.css");
                    if (facturacionStylesUrl != null) {
                        scene.getStylesheets().add(facturacionStylesUrl.toExternalForm());
                    }
                }
            } catch (Exception e) {
                System.out.println("No se pudieron cargar los estilos para el buscador: " + e.getMessage());
            }
            
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
            
            // Mostrar la ventana
            stage.show();
            
            System.out.println("✅ Buscador de medicamentos abierto exitosamente");
            
        } catch (IOException e) {
            System.err.println("Error de E/O al cargar el buscador: " + e.getMessage());
            e.printStackTrace();
            mostrarAdvertencia("Error al cargar buscador", 
                "No se pudo cargar el buscador avanzado de medicamentos.\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Se usará el método manual para agregar medicamentos.");
            agregarMedicamentoManual();
        } catch (Exception e) {
            System.err.println("Error general al abrir buscador: " + e.getMessage());
            e.printStackTrace();
            mostrarAdvertencia("Buscador no disponible", 
                "El buscador avanzado no está disponible en este momento.\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Se usará el método manual para agregar medicamentos.");
            agregarMedicamentoManual();
        }
    }
    
    /**
     * Procesa el medicamento seleccionado del inventario y lo agrega a la factura
     */
    private void procesarMedicamentoSeleccionado(ModeloMedicamentoInventario medicamentoInventario) {
        try {
            // Crear concepto de factura desde el medicamento del inventario
            ModeloFactura.ConceptoFactura concepto = new ModeloFactura.ConceptoFactura();
            
            // Mapear datos del medicamento del inventario
            // Incluir código del producto en la descripción para poder extraerlo después
            String descripcionConCodigo = "[" + medicamentoInventario.getCodigo() + "] " + medicamentoInventario.getNombreCompleto();
            concepto.setDescripcion(descripcionConCodigo);
            concepto.setCantidad(1); // Cantidad inicial
            concepto.setPrecioUnitario(medicamentoInventario.getPrecioUnitario());
            concepto.setTipoIva(10.0); // IVA reducido para medicamentos
            concepto.setDescuento(0.0);
            
            // Mostrar diálogo para confirmar/editar cantidad y otros detalles
            if (editarConceptoMedicamentoInventario(concepto, medicamentoInventario)) {
                // Verificar stock disponible
                if (medicamentoInventario.haySuficienteStock(concepto.getCantidad())) {
                    // Agregar a la lista
                    listaMedicamentos.add(concepto);
                    
                    // IMPORTANTE: Registrar en la lista de medicamentos agregados en esta sesión
                    medicamentosAgregadosEnSesion.add(concepto);
                    
                    calcularTotales();
                    
                    // Opcional: Actualizar stock en el inventario
                    // actualizarStockInventario(medicamentoInventario, concepto.getCantidad());
                    
                    mostrarInfo("Medicamento agregado", 
                        "Se agregó " + concepto.getCantidad() + " unidad(es) de " + 
                        medicamentoInventario.getNombre() + " a la factura.");
                } else {
                    mostrarError("Stock insuficiente", 
                        "No hay suficiente stock disponible. Stock actual: " + 
                        medicamentoInventario.getUnidadesDisponibles() + 
                        ", cantidad solicitada: " + concepto.getCantidad());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "Error al procesar el medicamento seleccionado: " + e.getMessage());
        }
    }
    
    /**
     * Diálogo específico para editar medicamentos del inventario
     */
    private boolean editarConceptoMedicamentoInventario(ModeloFactura.ConceptoFactura concepto, ModeloMedicamentoInventario medicamentoInventario) {
        Dialog<ModeloFactura.ConceptoFactura> dialog = new Dialog<>();
        dialog.setTitle("💊 Medicamento del Inventario");
        dialog.setHeaderText("Confirme los detalles del medicamento");
        
        // Crear contenido del diálogo
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Información del medicamento (solo lectura)
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        Label lblInfo = new Label("📋 Información del Medicamento");
        lblInfo.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label lblNombre = new Label("Nombre: " + medicamentoInventario.getNombreCompleto());
        Label lblLaboratorio = new Label("Laboratorio: " + (medicamentoInventario.getLaboratorio() != null ? medicamentoInventario.getLaboratorio() : "No especificado"));
        Label lblStock = new Label("Stock disponible: " + medicamentoInventario.getUnidadesDisponibles() + " unidades");
        Label lblPrecioBase = new Label("Precio base: " + formatoMoneda.format(medicamentoInventario.getPrecioUnitario()));
        
        infoBox.getChildren().addAll(lblInfo, lblNombre, lblLaboratorio, lblStock, lblPrecioBase);
        
        // Campos editables
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        // Cantidad
        Label lblCantidad = new Label("Cantidad:");
        lblCantidad.setFont(Font.font("System", FontWeight.BOLD, 12));
        Spinner<Integer> spnCantidad = new Spinner<>(1, medicamentoInventario.getUnidadesDisponibles(), concepto.getCantidad());
        spnCantidad.setEditable(true);
        
        // Precio unitario
        Label lblPrecio = new Label("Precio unitario (€):");
        lblPrecio.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField txtPrecio = new TextField(String.valueOf(concepto.getPrecioUnitario()));
        
        // Descuento
        Label lblDescuento = new Label("Descuento (%):");
        lblDescuento.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField txtDescuento = new TextField(String.valueOf(concepto.getDescuento()));
        
        // IVA
        Label lblIva = new Label("IVA (%):");
        lblIva.setFont(Font.font("System", FontWeight.BOLD, 12));
        ComboBox<Double> cmbIva = new ComboBox<>();
        cmbIva.getItems().addAll(0.0, 4.0, 10.0, 21.0);
        cmbIva.setValue(concepto.getTipoIva());
        
        grid.add(lblCantidad, 0, 0);
        grid.add(spnCantidad, 1, 0);
        grid.add(lblPrecio, 0, 1);
        grid.add(txtPrecio, 1, 1);
        grid.add(lblDescuento, 0, 2);
        grid.add(txtDescuento, 1, 2);
        grid.add(lblIva, 0, 3);
        grid.add(cmbIva, 1, 3);
        
        // Vista previa del total
        VBox previewBox = new VBox(10);
        previewBox.setStyle("-fx-background-color: #f0f9ff; -fx-border-color: #0ea5e9; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        Label lblPreview = new Label("💰 Resumen del Costo");
        lblPreview.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label lblSubtotal = new Label("Subtotal: 0,00 €");
        Label lblIvaAmount = new Label("IVA: 0,00 €");
        Label lblTotalPreview = new Label("Total: 0,00 €");
        lblTotalPreview.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTotalPreview.setStyle("-fx-text-fill: #0ea5e9;");
        
        previewBox.getChildren().addAll(lblPreview, lblSubtotal, lblIvaAmount, new Separator(), lblTotalPreview);
        
        // Función para actualizar vista previa
        Runnable updatePreview = () -> {
            try {
                int cantidad = spnCantidad.getValue();
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
                
                // Advertencia de stock
                if (cantidad > medicamentoInventario.getUnidadesDisponibles()) {
                    lblStock.setText("⚠️ Stock disponible: " + medicamentoInventario.getUnidadesDisponibles() + " unidades (INSUFICIENTE)");
                    lblStock.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    lblStock.setText("✅ Stock disponible: " + medicamentoInventario.getUnidadesDisponibles() + " unidades");
                    lblStock.setStyle("-fx-text-fill: green;");
                }
                
            } catch (NumberFormatException e) {
                lblSubtotal.setText("Subtotal: 0,00 €");
                lblIvaAmount.setText("IVA: 0,00 €");
                lblTotalPreview.setText("Total: 0,00 €");
            }
        };
        
        // Listeners
        spnCantidad.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        txtPrecio.textProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        txtDescuento.textProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        cmbIva.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        
        content.getChildren().addAll(infoBox, new Separator(), grid, previewBox);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Configurar resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    int cantidadSeleccionada = spnCantidad.getValue();
                    concepto.setCantidad(cantidadSeleccionada);
                    concepto.setPrecioUnitario(Double.parseDouble(txtPrecio.getText()));
                    concepto.setDescuento(Double.parseDouble(txtDescuento.getText()));
                    concepto.setTipoIva(cmbIva.getValue());
                    concepto.calcularImportes();
                    
                    // NUEVO: Enviar petición al puerto 50005 para reducir inventario
                    reducirInventarioEnServidor(medicamentoInventario.getCodigo(), cantidadSeleccionada);
                    
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
        
        Optional<ModeloFactura.ConceptoFactura> result = dialog.showAndWait();
        return result.isPresent();
    }
    
    /**
     * Actualiza el stock en el inventario después de agregar a la factura
     */
    private void actualizarStockInventario(ModeloMedicamentoInventario medicamento, int cantidadUsada) {
        // Solo actualizar si la factura se finaliza, no en borrador
        // Por ahora solo mostramos la información
        System.out.println("Se debería actualizar el stock de " + medicamento.getCodigo() + 
                          " restando " + cantidadUsada + " unidades cuando se finalice la factura");
    }
    
    /**
     * Método tradicional para agregar medicamentos manualmente
     * NOTA: Los medicamentos agregados manualmente NO se agregan a medicamentosAgregadosEnSesion
     * porque no reducen inventario automáticamente, por lo tanto no necesitan restablecimiento
     */
    private void agregarMedicamentoManual() {
        ModeloFactura.ConceptoFactura concepto = new ModeloFactura.ConceptoFactura();
        concepto.setTipoIva(10.0); // IVA reducido para medicamentos
        
        if (editarConcepto(concepto, false)) {
            listaMedicamentos.add(concepto);
            // NO agregar a medicamentosAgregadosEnSesion porque no reduce inventario automáticamente
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
            System.out.println("💾 Guardando borrador de factura...");
            
            // Actualizar datos del formulario
            actualizarFacturaDesdeFormulario();
            
            // Configurar como borrador
            factura.setEsBorrador(true);
            factura.setEstado(ModeloFactura.EstadoFactura.BORRADOR);
            factura.setFechaModificacion(new Date());
            
            // Enviar al servidor
            enviarFacturaAlServidor();
            
        } catch (Exception e) {
            System.err.println("❌ Error al guardar borrador: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "No se pudo guardar el borrador: " + e.getMessage());
        }
    }
    
    private void finalizarFactura() {
        try {
            System.out.println("🏁 Finalizando factura...");
            
            // Validar formulario
            if (!validarFormulario()) {
                return;
            }
            
            // Actualizar datos del formulario
            actualizarFacturaDesdeFormulario();
            
            // Configurar como finalizada
            factura.setEsBorrador(false);
            factura.setEstado(ModeloFactura.EstadoFactura.EMITIDA);
            factura.setFechaModificacion(new Date());
            
            // Enviar al servidor
            enviarFacturaAlServidor();
            
        } catch (Exception e) {
            System.err.println("❌ Error al finalizar factura: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "No se pudo finalizar la factura: " + e.getMessage());
        }
    }
    
    /**
     * Método unificado para enviar factura al servidor
     */
    private void enviarFacturaAlServidor() {
        new Thread(() -> {
            GestorSocket gestorSocketFactura = null;
            try {
                System.out.println("📤 Enviando factura al servidor...");
                System.out.println("📋 Estado: " + factura.getEstado());
                System.out.println("📋 Es borrador: " + factura.isEsBorrador());
                System.out.println("📋 ID de factura: " + (factura.getId() != null ? factura.getId().toString() : "null"));
                
                // Crear conexión independiente para evitar conflictos
                gestorSocketFactura = GestorSocket.crearConexionIndependiente();
                
                // Verificar conexión
                if (!gestorSocketFactura.isConectado()) {
                    Platform.runLater(() -> mostrarError("Error de conexión", "No hay conexión con el servidor"));
                    return;
                }
                
                // Actualizar datos desde formulario
                actualizarFacturaDesdeFormulario();
                
                // Determinar si es una factura nueva o una actualización
                final boolean esFacturaNueva = (factura.getId() == null);
                String peticion;
                
                if (esFacturaNueva) {
                    peticion = String.valueOf(Protocolo.CREAR_FACTURA);
                    System.out.println("🆕 Creando nueva factura (sin ID)...");
                } else {
                    peticion = String.valueOf(Protocolo.ACTUALIZAR_FACTURA);
                    System.out.println("📝 Actualizando factura existente: " + factura.getId());
                }
                
                System.out.println("🔗 Enviando petición: " + peticion);
                
                // Enviar petición
                gestorSocketFactura.enviarPeticion(peticion);
                gestorSocketFactura.getSalida().writeObject(factura);
                gestorSocketFactura.getSalida().flush();
                
                System.out.println("📤 Petición enviada, esperando respuesta...");
                
                // Leer respuesta
                ObjectInputStream entrada = gestorSocketFactura.getEntrada();
                int codigoRespuesta = entrada.readInt();
                
                System.out.println("📥 Código de respuesta recibido: " + codigoRespuesta);
                
                final boolean operacionExitosa;
                final ModeloFactura facturaGuardada;
                
                if (esFacturaNueva && codigoRespuesta == Protocolo.CREAR_FACTURA_RESPONSE) {
                    facturaGuardada = (ModeloFactura) entrada.readObject();
                    operacionExitosa = true;
                    System.out.println("✅ Factura nueva creada exitosamente con ID: " + facturaGuardada.getId());
                } else if (!esFacturaNueva && codigoRespuesta == Protocolo.ACTUALIZAR_FACTURA_RESPONSE) {
                    boolean actualizada = entrada.readBoolean();
                    facturaGuardada = factura; // Para facturas actualizadas, conservamos la instancia actual
                    operacionExitosa = actualizada;
                    System.out.println("✅ Factura actualizada exitosamente: " + actualizada);
                } else {
                    operacionExitosa = false;
                    facturaGuardada = null;
                    System.err.println("❌ Error en respuesta del servidor.");
                    System.err.println("   - Esperaba factura nueva: " + esFacturaNueva);
                    System.err.println("   - Código recibido: " + codigoRespuesta);
                    System.err.println("   - Código esperado CREAR: " + Protocolo.CREAR_FACTURA_RESPONSE);
                    System.err.println("   - Código esperado ACTUALIZAR: " + Protocolo.ACTUALIZAR_FACTURA_RESPONSE);
                }
                
                // Solo continuar si la operación fue exitosa
                if (!operacionExitosa || facturaGuardada == null) {
                    Platform.runLater(() -> mostrarError("Error", "No se pudo guardar la factura en el servidor"));
                    return;
                }
                
                // Actualizar la referencia local
                factura = facturaGuardada;
                
                // Solo para facturas nuevas: asociar a cita y actualizar contador
                if (esFacturaNueva && citaId != null && facturaGuardada.getId() != null) {
                    try {
                        // Asociar factura a cita usando la misma conexión
                        String mensajeAsociacion = Protocolo.ASOCIAR_FACTURA_A_CITA + "|" + citaId.toString() + ":" + facturaGuardada.getId().toString();
                        gestorSocketFactura.enviarPeticion(mensajeAsociacion);
                        
                        int respuestaAsociacion = gestorSocketFactura.getEntrada().readInt();
                        if (respuestaAsociacion == Protocolo.ASOCIAR_FACTURA_A_CITA_RESPONSE) {
                            boolean asociada = gestorSocketFactura.getEntrada().readBoolean();
                            if (asociada) {
                                System.out.println("✅ Factura asociada correctamente a la cita");
                                
                                // Actualizar contador de facturas solo para facturas nuevas
                                String mensajeContador = Protocolo.ACTUALIZAR_CONTADOR_FACTURAS + "|" + citaId.toString() + ":" + "true";
                                gestorSocketFactura.enviarPeticion(mensajeContador);
                                
                                int respuestaContador = gestorSocketFactura.getEntrada().readInt();
                                if (respuestaContador == Protocolo.ACTUALIZAR_CONTADOR_FACTURAS_RESPONSE) {
                                    boolean contadorActualizado = gestorSocketFactura.getEntrada().readBoolean();
                                    if (contadorActualizado) {
                                        System.out.println("✅ Contador de facturas actualizado");
                                    }
                                }
                            } else {
                                System.err.println("⚠️ No se pudo asociar la factura a la cita");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error al asociar factura a cita: " + e.getMessage());
                    }
                }
                
                // Si la factura se finalizó (no es borrador), cambiar el estado de la cita a COMPLETADA
                if (!factura.isEsBorrador() && citaId != null) {
                    try {
                        String mensajeEstadoCita = Protocolo.CAMBIAR_ESTADO_CITA + "|" + citaId.toString() + ":" + "COMPLETADA";
                        gestorSocketFactura.enviarPeticion(mensajeEstadoCita);
                        
                        int respuestaEstado = gestorSocketFactura.getEntrada().readInt();
                        if (respuestaEstado == Protocolo.CAMBIAR_ESTADO_CITA_RESPONSE) {
                            boolean estadoCambiado = gestorSocketFactura.getEntrada().readBoolean();
                            if (estadoCambiado) {
                                System.out.println("✅ Estado de la cita cambiado a COMPLETADA");
                            } else {
                                System.out.println("⚠️ No se pudo cambiar el estado de la cita");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error al cambiar estado de la cita: " + e.getMessage());
                    }
                }
                
                // Actualizar la interfaz en el hilo principal
                Platform.runLater(() -> {
                    // Actualizar las tablas en el controlador padre
                    if (facturacionController != null) {
                        System.out.println("🔄 Actualizando tablas en FacturacionController...");
                        facturacionController.actualizarListas();
                        
                        // Forzar actualización específica según el tipo de factura
                        if (factura.isEsBorrador()) {
                            facturacionController.cargarBorradores();
                            System.out.println("🔄 Lista de borradores actualizada");
                        } else {
                            facturacionController.cargarFacturas();
                            System.out.println("🔄 Lista de facturas finalizadas actualizada");
                        }
                    } else {
                        System.out.println("⚠️ FacturacionController es null, no se pueden actualizar las listas");
                    }
                    
                    String mensaje = factura.isEsBorrador() ? 
                        "Borrador guardado correctamente" : 
                        "Factura finalizada correctamente";
                    mostrarInfo("Éxito", mensaje);
                    cerrarVentana();
                });
                
            } catch (Exception e) {
                System.err.println("❌ Error al comunicar con el servidor: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> mostrarError("Error de comunicación", 
                    "Error de comunicación con el servidor: " + e.getMessage()));
            } finally {
                // Cerrar la conexión independiente
                if (gestorSocketFactura != null) {
                    try {
                        gestorSocketFactura.cerrarConexion();
                        System.out.println("🔌 Conexión independiente para factura cerrada");
                    } catch (Exception e) {
                        System.err.println("⚠️ Error al cerrar conexión de factura: " + e.getMessage());
                    }
                }
            }
        }).start();
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
    
    private void cancelar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText("¿Cancelar edición?");
        alert.setContentText("Se perderán los cambios no guardados y se restablecerá el inventario de los medicamentos agregados.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Restablecer inventario antes de cerrar
            restablecerInventarioMedicamentos();
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
            // NO generar ID automáticamente - debe ser asignado por el servidor al crear
            System.out.println("📋 Factura configurada. ID actual: " + (factura.getId() != null ? factura.getId().toString() : "null (nueva factura)"));
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
                            System.out.println("🎯 Auto-seleccionado veterinario actual: " + vet.getNombre());
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
        System.out.println("✅ FacturacionController configurado en FacturaFormController");
    }
    
    public void cargarDatosDesdeCita(ModeloCita cita, ModeloPaciente paciente, ModeloPropietario propietario) {
        // Verificar si la cita puede tener más facturas antes de proceder
        if (cita != null && cita.getId() != null) {
            try {
                // Verificar si se puede agregar una factura a esta cita
                String peticionVerificar = Protocolo.PUEDE_AGREGAR_FACTURA + "|" + cita.getId().toString();
                gestorSocket.enviarPeticion(peticionVerificar);
                
                int codigoRespuesta = gestorSocket.getEntrada().readInt();
                if (codigoRespuesta == Protocolo.PUEDE_AGREGAR_FACTURA_RESPONSE) {
                    boolean puedeAgregar = gestorSocket.getEntrada().readBoolean();
                    
                    if (!puedeAgregar) {
                        // La cita ya tiene una factura asociada
                        Platform.runLater(() -> {
                            mostrarAdvertencia("Factura ya existe", 
                                "Esta cita ya tiene una factura asociada. " +
                                "En una clínica real, cada cita solo puede tener una factura. " +
                                "Si necesita modificar la factura existente, búsquela en el listado de facturas.");
                            cerrarVentana();
                        });
                        return;
                    }
                } else {
                    System.err.println("Error al verificar si se puede agregar factura");
                }
            } catch (Exception e) {
                System.err.println("Error al verificar contador de facturas: " + e.getMessage());
                // Continuar con la creación de la factura en caso de error de comunicación
            }
        }
        
        // Si llegamos aquí, se puede crear la factura
        // Solo configurar los datos, NO tocar el ID de la factura
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
            txtCliente.setText(propietario.getNombre() + " " + propietario.getApellidos());
            txtDNI.setText(propietario.getDni());
            txtTelefono.setText(propietario.getTelefono());
            txtDireccion.setText(propietario.getDireccion());
        }
        
        System.out.println("📋 Datos de cita cargados. Factura ID: " + (factura.getId() != null ? factura.getId().toString() : "null (nueva)"));
    }
    
    /**
     * Carga una factura existente en el formulario (modo edición para borradores, solo lectura para finalizadas)
     */
    public void cargarFacturaExistente(ModeloFactura facturaExistente, ModeloCita cita, ModeloPaciente paciente, ModeloPropietario propietario) {
        this.factura = facturaExistente;
        this.citaId = cita.getId();
        this.pacienteId = paciente.getId();
        this.propietarioId = propietario.getId();
        
        // Determinar si es editable (solo los borradores se pueden editar)
        boolean soloLectura = !facturaExistente.isEsBorrador();
        
        // Configurar el título de la ventana
        String titulo = soloLectura ? "Ver Factura" : "Editar Borrador";
        lblTitulo.setText(titulo);
        
        // Cargar datos de la factura en el formulario
        cargarDatosEnFormulario();
        
        // Cargar datos del cliente y paciente
        if (propietario != null) {
            txtCliente.setText(propietario.getNombre() + " " + propietario.getApellidos());
            txtDNI.setText(propietario.getDni());
            txtTelefono.setText(propietario.getTelefono());
            txtDireccion.setText(propietario.getDireccion());
        }
        
        if (paciente != null) {
            txtPaciente.setText(paciente.getNombre() + " (" + paciente.getEspecie() + " - " + paciente.getRaza() + ")");
        }
        
        // Configurar el formulario según el modo
        configurarModoSoloLectura(soloLectura);
        
        System.out.println("✅ Factura existente cargada: " + (soloLectura ? "Modo solo lectura" : "Modo edición"));
    }
    
    /**
     * Configura el formulario en modo solo lectura o editable
     */
    private void configurarModoSoloLectura(boolean soloLectura) {
        // Controles de datos básicos
        txtNumeroFactura.setEditable(!soloLectura);
        dpFechaEmision.setDisable(soloLectura);
        cmbEstado.setDisable(soloLectura);
        
        // Controles de cliente
        txtCliente.setEditable(!soloLectura);
        txtDNI.setEditable(!soloLectura);
        txtTelefono.setEditable(!soloLectura);
        txtDireccion.setEditable(!soloLectura);
        btnSeleccionarCliente.setDisable(soloLectura);
        
        // Controles de paciente
        txtPaciente.setEditable(!soloLectura);
        btnSeleccionarPaciente.setDisable(soloLectura);
        
        // Controles de veterinario
        cmbVeterinario.setDisable(soloLectura);
        txtNumeroColegiado.setEditable(!soloLectura);
        
        // Tablas y botones de servicios/medicamentos
        btnAgregarServicio.setDisable(soloLectura);
        btnAgregarMedicamento.setDisable(soloLectura);
        
        // Observaciones
        txtObservaciones.setEditable(!soloLectura);
        
        // Botones de acción
        if (soloLectura) {
            // Solo mostrar botón de cancelar/cerrar en modo solo lectura
            btnGuardarBorrador.setVisible(false);
            btnGuardar.setVisible(false);
            btnFinalizar.setVisible(false);
            btnCancelar.setText("Cerrar");
        } else {
            // Mostrar todos los botones en modo edición
            btnGuardarBorrador.setVisible(true);
            btnGuardar.setVisible(true);
            btnFinalizar.setVisible(true);
            btnCancelar.setText("Cancelar");
        }
        
        // Configurar estilo visual
        if (soloLectura) {
            mainPane.setStyle("-fx-background-color: #f8f9fa;");
            lblTitulo.setStyle("-fx-text-fill: #6c757d;");
        }
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
        try {
            System.out.println("💾 Guardando factura...");
            
            // Actualizar datos del formulario
            actualizarFacturaDesdeFormulario();
            
            // Configurar como factura normal (no borrador)
            factura.setEsBorrador(false);
            factura.setEstado(ModeloFactura.EstadoFactura.EMITIDA);
            factura.setFechaModificacion(new Date());
            
            // Enviar al servidor
            enviarFacturaAlServidor();
            
        } catch (Exception e) {
            System.err.println("❌ Error al guardar factura: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "No se pudo guardar la factura: " + e.getMessage());
        }
    }

    @FXML
    private void onFinalizar() {
        finalizarFactura();
    }

    @FXML
    private void onCancelar() {
        cancelar();
    }

    /**
     * Actualiza el número de factura en la interfaz después de guardarlo
     */
    private void actualizarNumeroFacturaEnInterfaz() {
        Platform.runLater(() -> {
            if (factura != null && factura.getNumeroFactura() != null && 
                !factura.getNumeroFactura().isEmpty() && !factura.getNumeroFactura().contains("XXXX")) {
                txtNumeroFactura.setText(factura.getNumeroFactura());
                txtNumeroFactura.setStyle(""); // Restaurar estilo normal
                txtNumeroFactura.setEditable(false); // Mantener como solo lectura
                System.out.println("✅ Número de factura actualizado en interfaz: " + factura.getNumeroFactura());
            }
        });
    }
    
    /**
     * Actualiza el ID de factura en la cita relacionada
     */
    private void actualizarFacturaIdEnCita(ObjectId citaId, String facturaId) {
        if (citaId != null) {
            try {
                System.out.println("ℹ️ Se debería actualizar el facturaId de la cita " + citaId + " con el valor: " + facturaId);
                // TODO: Implementar la actualización de la cita cuando se resuelvan las dependencias
            } catch (Exception e) {
                System.err.println("❌ Error al actualizar facturaId en cita: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Reduce el inventario en el servidor del puerto 50005
     * @param codigoProducto Código del medicamento
     * @param cantidad Cantidad a reducir
     */
    private void reducirInventarioEnServidor(String codigoProducto, int cantidad) {
        System.out.println("📤 Iniciando reducción de inventario para producto: " + codigoProducto + " cantidad: " + cantidad);
        
        // Ejecutar en hilo separado para no bloquear la UI
        Thread hiloReduccion = new Thread(() -> {
            try {
                // Obtener instancia del gestor de inventario
                GestorSocketInventario gestorInventario = GestorSocketInventario.getInstance();
                
                // Verificar si ya está conectado, si no, conectar
                if (!gestorInventario.isConectado()) {
                    System.out.println("🔗 Conectando al servidor de inventario...");
                    boolean conectado = gestorInventario.conectarAlServidorInventario().get();
                    if (!conectado) {
                        Platform.runLater(() -> {
                            mostrarError("Error de conexión", 
                                "No se pudo conectar al servidor de inventario (puerto 50005).\n" +
                                "El medicamento se ha agregado a la factura, pero el inventario no se actualizó.");
                        });
                        return;
                    }
                }
                
                // Construir mensaje de reducción de inventario
                String idMensaje = "MSG_REDUCE_" + System.currentTimeMillis();
                String mensajeReduccion = ProtocoloInventarioVeterinaria.construirMensaje(
                    ProtocoloInventarioVeterinaria.REDUCIR_INVENTARIO,
                    codigoProducto,
                    String.valueOf(cantidad),
                    idMensaje
                );
                
                System.out.println("📤 Enviando petición de reducción: " + mensajeReduccion);
                
                // Enviar petición
                gestorInventario.enviarPeticion(mensajeReduccion);
                
                // Leer respuesta
                String respuesta = gestorInventario.leerRespuesta();
                System.out.println("📥 Respuesta del servidor: " + respuesta);
                
                // Parsear respuesta
                String[] partesRespuesta = ProtocoloInventarioVeterinaria.parsearMensaje(respuesta);
                
                if (partesRespuesta.length >= 3) {
                    int codigoRespuesta = Integer.parseInt(partesRespuesta[0]);
                    String idMensajeRespuesta = partesRespuesta[1];
                    String mensaje = partesRespuesta[2];
                    
                    System.out.println("📋 Código respuesta: " + codigoRespuesta);
                    System.out.println("📋 ID mensaje: " + idMensajeRespuesta);
                    System.out.println("📋 Mensaje: " + mensaje);
                    
                    Platform.runLater(() -> {
                        if (codigoRespuesta == ProtocoloInventarioVeterinaria.REDUCIR_INVENTARIO_RESPONSE) {
                            // Éxito
                            mostrarInfo("Inventario actualizado", 
                                "Se han reducido " + cantidad + " unidades del producto " + codigoProducto + 
                                " del inventario correctamente.");
                            System.out.println("✅ Inventario reducido exitosamente");
                        } else if (codigoRespuesta == ProtocoloInventarioVeterinaria.ERROR_REDUCIR_INVENTARIO) {
                            // Error específico de reducción
                            mostrarAdvertencia("Error al reducir inventario", 
                                "No se pudo reducir el inventario para el producto " + codigoProducto + ".\n" +
                                "Motivo: " + mensaje + "\n\n" +
                                "El medicamento se ha agregado a la factura, pero verifique el inventario manualmente.");
                            System.out.println("⚠️ Error al reducir inventario: " + mensaje);
                        } else {
                            // Otro tipo de error
                            mostrarError("Error inesperado", 
                                "Respuesta inesperada del servidor de inventario.\n" +
                                "Código: " + codigoRespuesta + "\n" +
                                "Mensaje: " + mensaje);
                            System.out.println("❌ Respuesta inesperada: " + codigoRespuesta);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        mostrarError("Error de comunicación", 
                            "Respuesta inválida del servidor de inventario: " + respuesta + 
                            "\nPartes recibidas: " + partesRespuesta.length);
                    });
                    System.out.println("❌ Respuesta inválida del servidor: " + respuesta + " (partes: " + partesRespuesta.length + ")");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error al reducir inventario: " + e.getMessage());
                e.printStackTrace();
                
                Platform.runLater(() -> {
                    mostrarError("Error de comunicación", 
                        "Error al comunicarse con el servidor de inventario:\n" + e.getMessage() + 
                        "\n\nEl medicamento se ha agregado a la factura, pero el inventario no se actualizó.");
                });
            }
        });
        
        hiloReduccion.setDaemon(true);
        hiloReduccion.start();
    }
    
    /**
     * Restablece el inventario de un medicamento específico cuando se elimina de la factura
     */
    private void restablecerInventarioMedicamento(ModeloFactura.ConceptoFactura medicamento) {
        System.out.println("🔄 Restableciendo inventario individual para: " + medicamento.getDescripcion());
        
        // Obtener ID de factura si existe
        String facturaId = (factura != null && factura.getId() != null) ? 
            factura.getId().toString() : "TEMP_" + System.currentTimeMillis();
        
        // Extraer código del producto
        String codigoProducto = extraerCodigoProducto(medicamento.getDescripcion());
        
        if (codigoProducto != null && !codigoProducto.isEmpty()) {
            restablecerInventarioEnServidor(facturaId, codigoProducto, medicamento.getCantidad());
            
            // Mostrar notificación de éxito
            Platform.runLater(() -> {
                mostrarInfo("Inventario restablecido", 
                    "Se han restablecido " + medicamento.getCantidad() + 
                    " unidades del producto " + codigoProducto + " en el inventario.");
            });
        } else {
            System.out.println("⚠️ No se pudo extraer código de producto para: " + medicamento.getDescripcion());
            Platform.runLater(() -> {
                mostrarAdvertencia("Código no encontrado", 
                    "No se pudo extraer el código del producto de '" + medicamento.getDescripcion() + "'.\n" +
                    "El medicamento se eliminó de la factura, pero el inventario debe actualizarse manualmente.");
            });
        }
    }
    
    /**
     * Restablece el inventario de todos los medicamentos agregados a la factura EN ESTA SESIÓN
     * Esto se ejecuta cuando se cancela la factura para devolver el stock
     */
    private void restablecerInventarioMedicamentos() {
        if (medicamentosAgregadosEnSesion.isEmpty()) {
            System.out.println("ℹ️ No hay medicamentos agregados en esta sesión para restablecer en el inventario");
            return;
        }
        
        System.out.println("🔄 Restableciendo inventario de " + medicamentosAgregadosEnSesion.size() + 
                          " medicamentos agregados en esta sesión...");
        System.out.println("ℹ️ Medicamentos que ya estaban en el borrador NO se restablecerán");
        
        // Obtener ID de factura si existe
        String facturaId = (factura != null && factura.getId() != null) ? 
            factura.getId().toString() : "TEMP_" + System.currentTimeMillis();
        
        // Restablecer inventario solo para medicamentos agregados en esta sesión
        for (ModeloFactura.ConceptoFactura medicamento : medicamentosAgregadosEnSesion) {
            // Extraer código del producto del nombre/descripción del medicamento
            String codigoProducto = extraerCodigoProducto(medicamento.getDescripcion());
            if (codigoProducto != null && !codigoProducto.isEmpty()) {
                restablecerInventarioEnServidor(facturaId, codigoProducto, medicamento.getCantidad());
                System.out.println("🔄 Restableciendo: " + medicamento.getDescripcion() + " (" + medicamento.getCantidad() + " unidades)");
            } else {
                System.out.println("⚠️ No se pudo extraer código de producto para: " + medicamento.getDescripcion());
            }
        }
        
        // Mostrar resumen
        Platform.runLater(() -> {
            mostrarInfo("Inventario restablecido", 
                "Se han restablecido " + medicamentosAgregadosEnSesion.size() + 
                " medicamentos agregados en esta sesión.\n\n" +
                "Los medicamentos que ya estaban en el borrador original no se modificaron.");
        });
    }
    
    /**
     * Extrae el código del producto desde la descripción del medicamento
     * Busca primero el formato [CODIGO] agregado por el sistema, luego otros patrones
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
                    System.out.println("✅ Código extraído de corchetes: " + codigo);
                    return codigo;
                }
            }
        }
        
        // Buscar patrones comunes de códigos de producto (números al inicio)
        if (descripcion.matches("^\\d+.*")) {
            // Si empieza con números, extraer esos números
            String codigo = descripcion.replaceAll("^(\\d+).*", "$1");
            System.out.println("✅ Código extraído del inicio: " + codigo);
            return codigo;
        }
        
        // Buscar códigos entre paréntesis
        if (descripcion.contains("(") && descripcion.contains(")")) {
            String codigo = descripcion.replaceAll(".*\\((\\d+)\\).*", "$1");
            if (!codigo.equals(descripcion)) {
                System.out.println("✅ Código extraído de paréntesis: " + codigo);
                return codigo;
            }
        }
        
        // Si no se encuentra un patrón, intentar extraer cualquier secuencia de números larga
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{6,}");
        java.util.regex.Matcher matcher = pattern.matcher(descripcion);
        if (matcher.find()) {
            String codigo = matcher.group();
            System.out.println("✅ Código extraído por patrón numérico: " + codigo);
            return codigo;
        }
        
        System.out.println("⚠️ No se pudo determinar el código de producto de: " + descripcion);
        return null;
    }
    
    /**
     * Restablece el inventario en el servidor del puerto 50005
     * @param facturaId ID de la factura
     * @param codigoProducto Código del medicamento  
     * @param cantidad Cantidad a restablecer
     */
    private void restablecerInventarioEnServidor(String facturaId, String codigoProducto, int cantidad) {
        System.out.println("📤 Iniciando restablecimiento de inventario para producto: " + codigoProducto + " cantidad: " + cantidad);
        
        // Ejecutar en hilo separado para no bloquear la UI
        Thread hiloRestablecimiento = new Thread(() -> {
            try {
                // Obtener instancia del gestor de inventario
                GestorSocketInventario gestorInventario = GestorSocketInventario.getInstance();
                
                // Verificar si ya está conectado, si no, conectar
                if (!gestorInventario.isConectado()) {
                    System.out.println("🔗 Conectando al servidor de inventario para restablecimiento...");
                    boolean conectado = gestorInventario.conectarAlServidorInventario().get();
                    if (!conectado) {
                        System.out.println("❌ No se pudo conectar al servidor de inventario para restablecimiento");
                        return;
                    }
                }
                
                // Construir mensaje de restablecimiento de inventario
                String idMensaje = "MSG_RESTORE_" + System.currentTimeMillis();
                String mensajeRestablecimiento = ProtocoloInventarioVeterinaria.construirMensaje(
                    ProtocoloInventarioVeterinaria.RESTABLECER_INVENTARIO,
                    facturaId,
                    codigoProducto,
                    String.valueOf(cantidad),
                    idMensaje
                );
                
                System.out.println("📤 Enviando petición de restablecimiento: " + mensajeRestablecimiento);
                
                // Enviar petición
                gestorInventario.enviarPeticion(mensajeRestablecimiento);
                
                // Leer respuesta
                String respuesta = gestorInventario.leerRespuesta();
                System.out.println("📥 Respuesta del servidor: " + respuesta);
                
                // Parsear respuesta
                String[] partesRespuesta = ProtocoloInventarioVeterinaria.parsearMensaje(respuesta);
                
                if (partesRespuesta.length >= 3) {
                    int codigoRespuesta = Integer.parseInt(partesRespuesta[0]);
                    String idMensajeRespuesta = partesRespuesta[1];
                    String mensaje = partesRespuesta[2];
                    
                    System.out.println("📋 Código respuesta restablecimiento: " + codigoRespuesta);
                    System.out.println("📋 ID mensaje: " + idMensajeRespuesta);
                    System.out.println("📋 Mensaje: " + mensaje);
                    
                    if (codigoRespuesta == ProtocoloInventarioVeterinaria.RESTABLECER_INVENTARIO_RESPONSE) {
                        // Éxito
                        System.out.println("✅ Inventario restablecido exitosamente para producto " + codigoProducto);
                    } else if (codigoRespuesta == ProtocoloInventarioVeterinaria.ERROR_RESTABLECER_INVENTARIO) {
                        // Error específico de restablecimiento
                        System.out.println("⚠️ Error al restablecer inventario: " + mensaje);
                    } else {
                        // Otro tipo de error
                        System.out.println("❌ Respuesta inesperada al restablecer: " + codigoRespuesta);
                    }
                } else {
                    System.out.println("❌ Respuesta inválida del servidor para restablecimiento: " + respuesta);
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error al restablecer inventario: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        hiloRestablecimiento.setDaemon(true);
        hiloRestablecimiento.start();
    }
} 