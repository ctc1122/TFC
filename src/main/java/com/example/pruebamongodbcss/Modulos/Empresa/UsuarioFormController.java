package com.example.pruebamongodbcss.Modulos.Empresa;

import java.net.URL;
import java.util.ResourceBundle;

import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.PatronExcepcion;
import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private ServicioModeloUsuario servicioModelo;
    private Usuario usuario;
    private ModeloUsuario modeloUsuario;
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
            if (lblVeterinario != null && cmbVeterinario != null) {
                boolean esVeterinario = newVal != null && newVal.equals(Usuario.Rol.VETERINARIO.getDescripcion());
                lblVeterinario.setVisible(esVeterinario);
                cmbVeterinario.setVisible(esVeterinario);
                
                if (esVeterinario && servicio != null) {
                    cargarVeterinarios();
                }
            }
        });
        
        // Configurar botones
        btnGuardar.setOnAction(e -> guardarUsuario());
        btnCancelar.setOnAction(e -> cerrarVentana());
    }
    
    /**
     * Establece el servicio
     */
    public void setServicio(ServicioUsuarios servicio) {
        this.servicio = servicio;
        this.servicioModelo = new ServicioModeloUsuario();
    }
    
    /**
     * Establece el callback a ejecutar al guardar
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    /**
     * Establece el usuario a editar
     */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.modeloUsuario = new ModeloUsuario(usuario);
        this.modoEdicion = (usuario.getId() != null);
        cargarDatosUsuario();
    }
    
    /**
     * Establece el ModeloUsuario a editar
     */
    public void setModeloUsuario(ModeloUsuario modeloUsuario) {
        this.modeloUsuario = modeloUsuario;
        this.usuario = modeloUsuario.getUsuario();
        this.modoEdicion = (usuario.getId() != null);
        cargarDatosUsuario();
    }
    
    /**
     * Configura las validaciones de los campos del formulario
     */
    private void configurarValidaciones() {
        // Validación de correo electrónico
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (!newVal.contains("@") || !newVal.contains(".")) {
                    txtEmail.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                } else {
                    txtEmail.setStyle("");
                }
            }
        });
        
        // Validación de nombre de usuario (alfanumérico)
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (!newVal.matches("^[a-zA-Z0-9]{4,16}$")) {
                    txtUsuario.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                } else {
                    txtUsuario.setStyle("");
                }
            }
        });
        
        // Validación de nombre y apellido
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (!newVal.matches("^[A-Za-zÁÉÍÓÚáéíóúñÑ ]{2,50}$")) {
                    txtNombre.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                } else {
                    txtNombre.setStyle("");
                }
            }
        });
        
        txtApellido.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (!newVal.matches("^[A-Za-zÁÉÍÓÚáéíóúñÑ ]{2,50}$")) {
                    txtApellido.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                } else {
                    txtApellido.setStyle("");
                }
            }
        });
    }
    
    /**
     * Carga el listado de veterinarios
     */
    private void cargarVeterinarios() {
        cmbVeterinario.getItems().clear();
        cmbVeterinario.getItems().addAll(servicio.obtenerTodosVeterinarios());
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
            
            // Guardar usuario
            ObjectId idUsuario = servicio.guardarUsuario(usuario);
            
            if (idUsuario != null) {
                usuario.setId(idUsuario);
                
                // Si hay callback, ejecutarlo
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                
                cerrarVentana();
            } else {
                mostrarError("Error al guardar", "No se pudo guardar el usuario. Inténtelo de nuevo.");
            }
        } catch (PatronExcepcion e) {
            mostrarError("Error de validación", e.getMessage());
        } catch (Exception e) {
            mostrarError("Error", "Se produjo un error al guardar el usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cierra la ventana del formulario
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Muestra un diálogo de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 