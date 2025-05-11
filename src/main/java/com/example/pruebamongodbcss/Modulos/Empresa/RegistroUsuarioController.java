package com.example.pruebamongodbcss.Modulos.Empresa;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.PatronExcepcion;
import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador para el registro y edición de usuarios/veterinarios
 */
public class RegistroUsuarioController implements Initializable {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelefono;

    @FXML
    private ComboBox<Usuario.Rol> comboRol;

    // Campos de veterinario
    @FXML
    private VBox panelVeterinario;

    @FXML
    private TextField txtEspecialidad;

    @FXML
    private TextField txtNumeroColegiado;

    @FXML
    private TextField txtHoraInicio;

    @FXML
    private TextField txtHoraFin;

    @FXML
    private CheckBox chkDisponible;

    // Controles principales
    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Label lblTitulo;

    @FXML
    private CheckBox chkAdmin;

    @FXML
    private TextField txtContraseñaAdmin;

    @FXML
    private VBox panelAdmin;

    // Servicio
    private ServicioUsuarios servicio;

    // Usuario para editar
    private Usuario usuario;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Inicializar servicio
            servicio = new ServicioUsuarios();

            // Configurar combo de roles
            comboRol.setItems(FXCollections.observableArrayList(Usuario.Rol.values()));
            comboRol.getSelectionModel().select(Usuario.Rol.NORMAL);

            // Mostrar/ocultar panel de veterinario según el rol seleccionado
            comboRol.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == Usuario.Rol.VETERINARIO) {
                    panelVeterinario.setVisible(true);
                    panelVeterinario.setManaged(true);
                } else {
                    panelVeterinario.setVisible(false);
                    panelVeterinario.setManaged(false);
                }
                
                if (newVal == Usuario.Rol.ADMINISTRADOR) {
                    chkAdmin.setSelected(true);
                } else {
                    if (!modoEdicion) {
                        chkAdmin.setSelected(false);
                    }
                }
            });
            
            // Mostrar/ocultar panel de admin según checkbox
            chkAdmin.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    comboRol.getSelectionModel().select(Usuario.Rol.ADMINISTRADOR);
                } else {
                    if (comboRol.getValue() == Usuario.Rol.ADMINISTRADOR) {
                        comboRol.getSelectionModel().select(Usuario.Rol.NORMAL);
                    }
                }
            });
            
            // Ocultar panel de admin (ya no se necesita contraseña de admin)
            if (panelAdmin != null) {
                panelAdmin.setVisible(false);
                panelAdmin.setManaged(false);
            }
            
            // Inicialmente ocultar panel de veterinario y admin
            panelVeterinario.setVisible(false);
            panelVeterinario.setManaged(false);

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al inicializar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Configura el controlador para editar un usuario existente
     */
    public void setUsuarioParaEditar(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        
        this.usuario = usuario;
        this.modoEdicion = true;
        
        // Configurar título
        lblTitulo.setText("Editar " + (usuario.getRol() == Usuario.Rol.VETERINARIO ? "Veterinario" : "Usuario"));
        
        // Cargar datos en los campos
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtUsuario.setText(usuario.getUsuario());
        txtPassword.setText(usuario.getPassword());
        txtEmail.setText(usuario.getEmail());
        txtTelefono.setText(usuario.getTelefono());
        comboRol.getSelectionModel().select(usuario.getRol());
        
        // Si es admin, seleccionar checkbox
        if (usuario.getRol() == Usuario.Rol.ADMINISTRADOR) {
            chkAdmin.setSelected(true);
        }
        
        // Si es veterinario, cargar datos adicionales
        if (usuario.getRol() == Usuario.Rol.VETERINARIO) {
            txtEspecialidad.setText(usuario.getEspecialidad());
            txtNumeroColegiado.setText(usuario.getNumeroColegiado());
            txtHoraInicio.setText(usuario.getHoraInicio());
            txtHoraFin.setText(usuario.getHoraFin());
            chkDisponible.setSelected(usuario.isDisponible());
        }
        
        // Deshabilitar usuario en modo edición
        txtUsuario.setDisable(true);
    }

    @FXML
    private void guardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }
        
        try {
            if (usuario == null) {
                // Nuevo usuario
                if (chkAdmin.isSelected()) {
                    // Crear usuario normal primero
                    usuario = new Usuario(
                        txtNombre.getText(),
                        txtApellido.getText(),
                        txtUsuario.getText(),
                        txtPassword.getText(),
                        txtEmail.getText(),
                        txtTelefono.getText()
                    );
                    // Establecer rol administrador
                    usuario.setRol(Usuario.Rol.ADMINISTRADOR);
                } else {
                    // Crear usuario normal
                    usuario = new Usuario(
                        txtNombre.getText(),
                        txtApellido.getText(),
                        txtUsuario.getText(),
                        txtPassword.getText(),
                        txtEmail.getText(),
                        txtTelefono.getText()
                    );
                    
                    // Asignar rol seleccionado
                    usuario.setRol(comboRol.getValue());
                }
            } else {
                // Actualizar usuario existente
                usuario.setNombre(txtNombre.getText());
                usuario.setApellido(txtApellido.getText());
                usuario.setPassword(txtPassword.getText());
                usuario.setEmail(txtEmail.getText());
                usuario.setTelefono(txtTelefono.getText());
                usuario.setRol(comboRol.getValue());
            }
            
            // Si es veterinario, actualizar campos adicionales
            if (comboRol.getValue() == Usuario.Rol.VETERINARIO) {
                usuario.setEspecialidad(txtEspecialidad.getText());
                usuario.setNumeroColegiado(txtNumeroColegiado.getText());
                usuario.setHoraInicio(txtHoraInicio.getText());
                usuario.setHoraFin(txtHoraFin.getText());
                usuario.setDisponible(chkDisponible.isSelected());
            }
            
            // Guardar en la base de datos
            servicio.guardarUsuario(usuario);
            
            // Mostrar mensaje de éxito
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Usuario guardado");
            alert.setHeaderText("Usuario guardado correctamente");
            alert.setContentText("El usuario ha sido guardado en la base de datos.");
            alert.showAndWait();
            
            // Cerrar ventana
            cerrar();
            
        } catch (PatronExcepcion e) {
            mostrarError("Error de validación", e.getMessage());
        } catch (Exception e) {
            mostrarError("Error", "Error al guardar usuario: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar(ActionEvent event) {
        cerrar();
    }

    /**
     * Valida los campos del formulario
     */
    private boolean validarCampos() {
        // Validar campos obligatorios
        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() || 
            txtUsuario.getText().isEmpty() || txtPassword.getText().isEmpty() || 
            txtEmail.getText().isEmpty() || txtTelefono.getText().isEmpty()) {
            
            mostrarError("Campos incompletos", "Todos los campos marcados con * son obligatorios.");
            return false;
        }
        
        // Validar longitud de contraseña
        if (txtPassword.getText().length() < 8) {
            mostrarError("Contraseña inválida", "La contraseña debe tener al menos 8 caracteres.");
            return false;
        }
        
        // Validar formato de email
        if (!txtEmail.getText().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            mostrarError("Email inválido", "El formato del email no es válido.");
            return false;
        }
        
        // Validar formato de teléfono
        if (!txtTelefono.getText().matches("^[0-9]{9}$")) {
            mostrarError("Teléfono inválido", "El teléfono debe tener 9 dígitos.");
            return false;
        }
        
        // Validar que el usuario no exista (solo en modo creación)
        if (!modoEdicion && servicio.existeUsuario(txtUsuario.getText())) {
            mostrarError("Usuario existente", "El nombre de usuario ya existe en el sistema.");
            return false;
        }
        
        // Validar campos de veterinario si aplica
        if (comboRol.getValue() == Usuario.Rol.VETERINARIO) {
            if (txtEspecialidad.getText().isEmpty() || txtNumeroColegiado.getText().isEmpty() || 
                txtHoraInicio.getText().isEmpty() || txtHoraFin.getText().isEmpty()) {
                
                mostrarError("Campos incompletos", "Todos los campos de veterinario son obligatorios.");
                return false;
            }
            
            // Validar formato de hora
            if (!txtHoraInicio.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$") || 
                !txtHoraFin.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                
                mostrarError("Formato de hora inválido", "El formato de hora debe ser HH:MM.");
                return false;
            }
        }
        
        // Validar contraseña de admin si aplica
        if (chkAdmin.isSelected() && !modoEdicion) {
            // Verificar que el usuario actual tiene permisos de administrador
            if (!servicio.esUsuarioAdmin()) {
                mostrarError("Permisos insuficientes", "Solo los administradores pueden crear nuevos usuarios administradores.");
                return false;
            }
        }
        
        return true;
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
     * Cierra la ventana
     */
    private void cerrar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
} 