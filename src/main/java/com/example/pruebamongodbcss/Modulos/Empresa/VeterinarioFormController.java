package com.example.pruebamongodbcss.Modulos.Empresa;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Controlador para el formulario de creación/edición de veterinarios.
 */
public class VeterinarioFormController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDNI;
    @FXML private TextField txtNumeroTitulo;
    @FXML private TextField txtEspecialidad;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private DatePicker dpFechaContratacion;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    
    private ServicioEmpresa servicio;
    private ModeloVeterinario veterinario;
    private Runnable onSaveCallback;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración inicial
        veterinario = new ModeloVeterinario();
        
        // Configurar validaciones básicas
        configurarValidaciones();
        
        // Configurar botones
        btnGuardar.setOnAction(e -> guardarVeterinario());
        btnCancelar.setOnAction(e -> cerrarVentana());
    }
    
    /**
     * Configura las validaciones de los campos del formulario
     */
    private void configurarValidaciones() {
        // DNI: 8 números y 1 letra (simple)
        txtDNI.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d{0,8}[A-Za-z]?")) {
                txtDNI.setText(oldVal);
            }
        });
        
        // Teléfono: solo números, máximo 9 dígitos
        txtTelefono.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d{0,9}")) {
                txtTelefono.setText(oldVal);
            }
        });
        
        // Email: validación simple
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 0) {
                if (!newVal.contains("@") || !newVal.contains(".")) {
                    txtEmail.setStyle("-fx-border-color: red;");
                } else {
                    txtEmail.setStyle("");
                }
            }
        });
    }
    
    /**
     * Establece el servicio a utilizar
     */
    public void setServicio(ServicioEmpresa servicio) {
        this.servicio = servicio;
    }
    
    /**
     * Establece el veterinario a editar
     */
    public void setVeterinario(ModeloVeterinario veterinario) {
        this.veterinario = veterinario;
        cargarDatosVeterinario();
    }
    
    /**
     * Establece la función de callback a ejecutar al guardar
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    /**
     * Carga los datos del veterinario en el formulario
     */
    private void cargarDatosVeterinario() {
        if (veterinario != null) {
            txtNombre.setText(veterinario.getNombre());
            txtApellidos.setText(veterinario.getApellidos());
            txtDNI.setText(veterinario.getDni());
            txtNumeroTitulo.setText(veterinario.getNumeroTitulo());
            txtEspecialidad.setText(veterinario.getEspecialidad());
            txtEmail.setText(veterinario.getEmail());
            txtTelefono.setText(veterinario.getTelefono());
            
            if (veterinario.getFechaContratacion() != null) {
                LocalDate fecha = veterinario.getFechaContratacion().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                dpFechaContratacion.setValue(fecha);
            } else {
                dpFechaContratacion.setValue(LocalDate.now());
            }
            
            chkActivo.setSelected(veterinario.isActivo());
        }
    }
    
    /**
     * Guarda los datos del veterinario
     */
    private void guardarVeterinario() {
        // Validar campos obligatorios
        if (txtNombre.getText().isEmpty() || txtApellidos.getText().isEmpty() || 
            txtDNI.getText().isEmpty() || txtEmail.getText().isEmpty() || 
            txtTelefono.getText().isEmpty()) {
            mostrarError("Campos incompletos", "Por favor, complete todos los campos obligatorios.");
            return;
        }
        
        // Validar formato de email
        if (!txtEmail.getText().contains("@") || !txtEmail.getText().contains(".")) {
            mostrarError("Email inválido", "Por favor, ingrese un email válido.");
            return;
        }
        
        // Verificar si el DNI ya existe (solo para nuevos veterinarios)
        if (veterinario.getId() == null && 
            servicio.existeVeterinarioPorDNI(txtDNI.getText())) {
            mostrarError("DNI duplicado", "Ya existe un veterinario con ese DNI.");
            return;
        }
        
        // Actualizar datos del veterinario
        veterinario.setNombre(txtNombre.getText());
        veterinario.setApellidos(txtApellidos.getText());
        veterinario.setDni(txtDNI.getText());
        veterinario.setNumeroTitulo(txtNumeroTitulo.getText());
        veterinario.setEspecialidad(txtEspecialidad.getText());
        veterinario.setEmail(txtEmail.getText());
        veterinario.setTelefono(txtTelefono.getText());
        
        if (dpFechaContratacion.getValue() != null) {
            Date fecha = Date.from(dpFechaContratacion.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            veterinario.setFechaContratacion(fecha);
        }
        
        veterinario.setActivo(chkActivo.isSelected());
        
        // Guardar en la base de datos
        servicio.guardarVeterinario(veterinario);
        
        // Ejecutar callback si existe
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        // Cerrar ventana
        cerrarVentana();
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Cierra la ventana del formulario
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
} 