package com.example.pruebamongodbcss.Modulos.Empresa;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import org.bson.types.ObjectId;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para el formulario de creación/edición de usuarios.
 */
public class UsuarioFormController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> cmbRol;
    @FXML private ComboBox<ModeloVeterinario> cmbVeterinario;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Label lblVeterinario;
    
    private ServicioEmpresa servicio;
    private ModeloUsuario usuario;
    private Runnable onSaveCallback;
    private boolean modoEdicion = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración inicial
        usuario = new ModeloUsuario();
        
        // Configurar rol combo box
        cmbRol.getItems().clear();
        for (ModeloUsuario.RolUsuario rol : ModeloUsuario.RolUsuario.values()) {
            cmbRol.getItems().add(rol.getDescripcion());
        }
        cmbRol.getSelectionModel().selectFirst();
        
        // Configurar validaciones básicas
        configurarValidaciones();
        
        // Configurar comportamiento del rol
        cmbRol.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean esVeterinario = newVal.equals(ModeloUsuario.RolUsuario.VETERINARIO.getDescripcion());
            lblVeterinario.setVisible(esVeterinario);
            cmbVeterinario.setVisible(esVeterinario);
            
            if (esVeterinario && servicio != null) {
                cargarVeterinarios();
            }
        });
        
        // Configurar botones
        btnGuardar.setOnAction(e -> guardarUsuario());
        btnCancelar.setOnAction(e -> cerrarVentana());
    }
    
    /**
     * Configura las validaciones de los campos del formulario
     */
    private void configurarValidaciones() {
        // Usuario: sin espacios
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.contains(" ")) {
                txtUsuario.setText(newVal.replace(" ", ""));
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
     * Establece el usuario a editar
     */
    public void setUsuario(ModeloUsuario usuario) {
        this.usuario = usuario;
        this.modoEdicion = true;
        cargarDatosUsuario();
    }
    
    /**
     * Establece la función de callback a ejecutar al guardar
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    /**
     * Carga la lista de veterinarios en el combo
     */
    private void cargarVeterinarios() {
        List<ModeloVeterinario> veterinarios = servicio.buscarVeterinariosActivos();
        cmbVeterinario.setItems(FXCollections.observableArrayList(veterinarios));
        
        // Configurar como se muestran los veterinarios
        cmbVeterinario.setCellFactory(cell -> new ListCell<ModeloVeterinario>() {
            @Override
            protected void updateItem(ModeloVeterinario item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getNombreCompleto());
                }
            }
        });
        
        cmbVeterinario.setButtonCell(new ListCell<ModeloVeterinario>() {
            @Override
            protected void updateItem(ModeloVeterinario item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getNombreCompleto());
                }
            }
        });
    }
    
    /**
     * Carga los datos del usuario en el formulario
     */
    private void cargarDatosUsuario() {
        if (usuario != null) {
            txtUsuario.setText(usuario.getUsuario());
            txtUsuario.setDisable(true);  // No permitir cambiar nombre de usuario
            txtPassword.setText(usuario.getPassword());
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            txtEmail.setText(usuario.getEmail());
            txtTelefono.setText(usuario.getTelefono());
            
            // Seleccionar rol
            cmbRol.getSelectionModel().select(usuario.getRol().getDescripcion());
            
            // Si es veterinario, cargar y seleccionar el veterinario correspondiente
            if (usuario.getRol() == ModeloUsuario.RolUsuario.VETERINARIO) {
                cargarVeterinarios();
                
                if (usuario.getVeterinarioId() != null) {
                    ModeloVeterinario veterinario = servicio.obtenerVeterinarioPorId(usuario.getVeterinarioId());
                    if (veterinario != null) {
                        cmbVeterinario.getSelectionModel().select(veterinario);
                    }
                }
            }
            
            chkActivo.setSelected(usuario.isActivo());
        }
    }
    
    /**
     * Guarda los datos del usuario
     */
    private void guardarUsuario() {
        // Validar campos obligatorios
        if (txtUsuario.getText().isEmpty() || txtNombre.getText().isEmpty() || 
            txtApellido.getText().isEmpty() || txtEmail.getText().isEmpty() || 
            txtTelefono.getText().isEmpty()) {
            mostrarError("Campos incompletos", "Por favor, complete todos los campos obligatorios.");
            return;
        }
        
        // Validar formato de email
        if (!txtEmail.getText().contains("@") || !txtEmail.getText().contains(".")) {
            mostrarError("Email inválido", "Por favor, ingrese un email válido.");
            return;
        }
        
        // Validar contraseña para nuevos usuarios
        if (!modoEdicion && (txtPassword.getText().isEmpty() || txtPassword.getText().length() < 6)) {
            mostrarError("Contraseña inválida", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        
        // Verificar si el nombre de usuario ya existe (solo para nuevos usuarios)
        if (!modoEdicion && servicio.existeUsuario(txtUsuario.getText())) {
            mostrarError("Usuario duplicado", "Ya existe un usuario con ese nombre.");
            return;
        }
        
        // Obtener el rol seleccionado
        String rolStr = cmbRol.getSelectionModel().getSelectedItem();
        ModeloUsuario.RolUsuario rol = ModeloUsuario.RolUsuario.fromString(rolStr);
        
        // Actualizar datos del usuario
        usuario.setUsuario(txtUsuario.getText());
        
        // Solo actualizar contraseña si no está vacía en modo edición
        if (!modoEdicion || !txtPassword.getText().isEmpty()) {
            usuario.setPassword(txtPassword.getText());
        }
        
        usuario.setNombre(txtNombre.getText());
        usuario.setApellido(txtApellido.getText());
        usuario.setEmail(txtEmail.getText());
        usuario.setTelefono(txtTelefono.getText());
        usuario.setRol(rol);
        
        // Si es veterinario, asignar el ID del veterinario
        if (rol == ModeloUsuario.RolUsuario.VETERINARIO) {
            ModeloVeterinario veterinario = cmbVeterinario.getSelectionModel().getSelectedItem();
            if (veterinario != null) {
                usuario.setVeterinarioId(veterinario.getId());
            } else {
                mostrarError("Veterinario requerido", "Debe seleccionar un veterinario para este rol.");
                return;
            }
        } else {
            usuario.setVeterinarioId(null);
        }
        
        // Asignar fecha de creación si es nuevo
        if (!modoEdicion) {
            usuario.setFechaCreacion(new Date());
        }
        
        usuario.setActivo(chkActivo.isSelected());
        
        // Guardar en la base de datos
        servicio.guardarUsuario(usuario);
        
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
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