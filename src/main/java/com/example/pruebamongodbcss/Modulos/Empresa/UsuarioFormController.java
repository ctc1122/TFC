package com.example.pruebamongodbcss.Modulos.Empresa;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Data.ServicioUsuarios;

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
    
    private ServicioUsuarios servicio;
    private Usuario usuario;
    private Runnable onSaveCallback;
    private boolean modoEdicion = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración inicial
        usuario = new Usuario();
        
        // Configurar rol combo box
        cmbRol.getItems().clear();
        for (Usuario.Rol rol : Usuario.Rol.values()) {
            cmbRol.getItems().add(rol.getDescripcion());
        }
        cmbRol.getSelectionModel().selectFirst();
        
        // Configurar validaciones básicas
        configurarValidaciones();
        
        // Configurar comportamiento del rol
        cmbRol.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean esVeterinario = newVal.equals(Usuario.Rol.VETERINARIO.getDescripcion());
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
    public void setServicio(ServicioUsuarios servicio) {
        this.servicio = servicio;
    }
    
    /**
     * Establece el usuario a editar
     */
    public void setUsuario(Usuario usuario) {
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
            if (usuario.getRol() == Usuario.Rol.VETERINARIO) {
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
            mostrarError("Email inválido", "Por favor, ingrese una dirección de email válida.");
            return;
        }
        
        try {
            // Asignar datos del formulario al modelo
            usuario.setUsuario(txtUsuario.getText());
            usuario.setPassword(txtPassword.getText());
            usuario.setNombre(txtNombre.getText());
            usuario.setApellido(txtApellido.getText());
            usuario.setEmail(txtEmail.getText());
            usuario.setTelefono(txtTelefono.getText());
            
            // Asignar rol
            String rolStr = cmbRol.getSelectionModel().getSelectedItem();
            Usuario.Rol rol = Usuario.Rol.fromString(rolStr);
            usuario.setRol(rol);
            
            // Si es veterinario, asignar el ID del veterinario
            if (rol == Usuario.Rol.VETERINARIO) {
                ModeloVeterinario veterinario = cmbVeterinario.getSelectionModel().getSelectedItem();
                if (veterinario != null) {
                    usuario.setVeterinarioId(veterinario.getId());
                } else {
                    mostrarError("Veterinario requerido", "Por favor, seleccione un veterinario.");
                    return;
                }
            } else {
                usuario.setVeterinarioId(null);
            }
            
            usuario.setActivo(chkActivo.isSelected());
            
            // Si es nuevo usuario, establecer fecha de creación
            if (!modoEdicion) {
                usuario.setFechaCreacion(new Date());
            }
            
            // Guardar en base de datos
            ObjectId id = servicio.guardarUsuario(usuario);
            
            if (id != null) {
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                cerrarVentana();
            } else {
                mostrarError("Error al guardar", "No se pudo guardar el usuario en la base de datos.");
            }
            
        } catch (Exception e) {
            mostrarError("Error", "Se produjo un error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
} 