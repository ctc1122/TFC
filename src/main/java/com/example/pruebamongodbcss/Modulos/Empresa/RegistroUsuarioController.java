package com.example.pruebamongodbcss.Modulos.Empresa;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.PatronExcepcion;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;

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
    private GestorSocket gestorSocket;

    // Usuario para editar
    private Usuario usuario;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Inicializar servicio
            gestorSocket = GestorSocket.getInstance();

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
            
            // Aplicar estilos a los elementos del formulario
            if (btnGuardar != null) btnGuardar.getStyleClass().add("form-button");
            if (btnCancelar != null) btnCancelar.getStyleClass().add("danger-button");
            
            // Aplicar estilos a los campos de texto
            if (txtNombre != null) txtNombre.getStyleClass().add("text-field");
            if (txtApellido != null) txtApellido.getStyleClass().add("text-field");
            if (txtUsuario != null) txtUsuario.getStyleClass().add("text-field");
            if (txtPassword != null) txtPassword.getStyleClass().add("password-field");
            if (txtEmail != null) txtEmail.getStyleClass().add("text-field");
            if (txtTelefono != null) txtTelefono.getStyleClass().add("text-field");
            
            // Aplicar estilos a los campos de veterinario
            if (txtEspecialidad != null) txtEspecialidad.getStyleClass().add("text-field");
            if (txtNumeroColegiado != null) txtNumeroColegiado.getStyleClass().add("text-field");
            if (txtHoraInicio != null) txtHoraInicio.getStyleClass().add("text-field");
            if (txtHoraFin != null) txtHoraFin.getStyleClass().add("text-field");

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al inicializar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Configura el controlador para editar un usuario existente
     */
    public void setUsuarioParaEditar(Usuario usuario) {
        if (usuario == null) {
            // Si recibimos null, crear un usuario nuevo
            this.usuario = new Usuario();
            this.modoEdicion = false;
            
            // Configurar título para usuario nuevo
            lblTitulo.setText("Nuevo Usuario");
            
            // Asegurarse de habilitar el campo usuario en modo creación
            txtUsuario.setDisable(false);
            
            return;
        }
        
        this.usuario = usuario;
        this.modoEdicion = usuario.getId() != null;
        
        // Configurar título según modo (nuevo o edición)
        if (modoEdicion) {
            lblTitulo.setText("Editar " + (usuario.getRol() == Usuario.Rol.VETERINARIO ? "Veterinario" : "Usuario"));
            
            // Cargar datos en los campos solo si estamos en modo edición
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            txtUsuario.setText(usuario.getUsuario());
            txtPassword.setText(usuario.getPassword());
            txtEmail.setText(usuario.getEmail());
            txtTelefono.setText(usuario.getTelefono());
            
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
        } else {
            // En modo nuevo, asegurarnos de que el campo usuario esté habilitado
            txtUsuario.setDisable(false);
            
            // Configurar título para usuario nuevo
            lblTitulo.setText("Nuevo " + (usuario.getRol() == Usuario.Rol.VETERINARIO ? "Veterinario" : "Usuario"));
        }
        
        // Configurar combo de roles según el tipo de usuario
        comboRol.getSelectionModel().select(usuario.getRol());
        
        // Si es admin, seleccionar checkbox
        if (usuario.getRol() == Usuario.Rol.ADMINISTRADOR) {
            chkAdmin.setSelected(true);
        }
    }

    @FXML
    private void guardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }
        
        try {
            if (usuario == null) {
                // Nuevo usuario
                usuario = new Usuario();
                usuario.setNombre(txtNombre.getText());
                usuario.setApellido(txtApellido.getText());
                usuario.setUsuario(txtUsuario.getText());
                usuario.setPassword(txtPassword.getText());
                usuario.setEmail(txtEmail.getText());
                usuario.setTelefono(txtTelefono.getText());
                
                // Establecer rol según la selección
                if (chkAdmin.isSelected()) {
                    usuario.setRol(Usuario.Rol.ADMINISTRADOR);
                } else {
                    usuario.setRol(comboRol.getValue());
                }
                
                // Si es veterinario, establecer campos adicionales
                if (comboRol.getValue() == Usuario.Rol.VETERINARIO) {
                    usuario.setEspecialidad(txtEspecialidad.getText());
                    usuario.setNumeroColegiado(txtNumeroColegiado.getText());
                    usuario.setHoraInicio(txtHoraInicio.getText());
                    usuario.setHoraFin(txtHoraFin.getText());
                    usuario.setDisponible(chkDisponible.isSelected());
                }
            } else {
                // Actualizar usuario existente
                usuario.setNombre(txtNombre.getText());
                usuario.setApellido(txtApellido.getText());
                // Importante: En modo edición no actualizamos el usuario porque está deshabilitado
                if (!modoEdicion) {
                    usuario.setUsuario(txtUsuario.getText());
                }
                usuario.setPassword(txtPassword.getText());
                usuario.setEmail(txtEmail.getText());
                usuario.setTelefono(txtTelefono.getText());
                
                // Asignar rol según la selección
                if (chkAdmin.isSelected()) {
                    usuario.setRol(Usuario.Rol.ADMINISTRADOR);
                } else {
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
            }
            
            // Asegurarse de que el nombre de usuario no sea nulo antes de guardar
            if (usuario.getUsuario() == null || usuario.getUsuario().isEmpty()) {
                mostrarError("Error", "El nombre de usuario no puede estar vacío");
                return;
            }
            
            // Guardar en la base de datos
            try {
                gestorSocket.enviarPeticion(Protocolo.GUARDAR_USUARIO + Protocolo.SEPARADOR_CODIGO);
                gestorSocket.getSalida().writeObject(usuario);
                gestorSocket.getSalida().flush();
                
                int codigo = gestorSocket.getEntrada().readInt();
                if (codigo != Protocolo.GUARDAR_USUARIO_RESPONSE) {
                    throw new Exception("Error al guardar usuario en el servidor");
                }
            } catch (IOException e) {
                throw new Exception("Error de comunicación con el servidor: " + e.getMessage());
            }
            
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
        try {
            // Verificar que los campos básicos existan antes de validarlos
            if (txtNombre == null || txtApellido == null || txtUsuario == null || 
                txtPassword == null || txtEmail == null || txtTelefono == null || 
                comboRol == null) {
                mostrarError("Error de inicialización", "No se han inicializado correctamente todos los campos del formulario.");
                return false;
            }
            
            // Validar campos obligatorios
            String nombre = txtNombre.getText();
            String apellido = txtApellido.getText();
            String nombreUsuario = txtUsuario.getText();
            String password = txtPassword.getText();
            String email = txtEmail.getText();
            String telefono = txtTelefono.getText();
            
            if (nombre == null || apellido == null || nombreUsuario == null || 
                password == null || email == null || telefono == null) {
                mostrarError("Error de datos", "Algunos campos contienen valores nulos.");
                return false;
            }
            
            if (nombre.isEmpty() || apellido.isEmpty() || nombreUsuario.isEmpty() || 
                password.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
                mostrarError("Campos incompletos", "Todos los campos marcados con * son obligatorios.");
                return false;
            }
            
            // Validar longitud de contraseña
            if (password.length() < 8) {
                mostrarError("Contraseña inválida", "La contraseña debe tener al menos 8 caracteres.");
                return false;
            }
            
            // Validar formato de email
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                mostrarError("Email inválido", "El formato del email no es válido.");
                return false;
            }
            
            // Validar formato de teléfono
            if (!telefono.matches("^[0-9]{9}$")) {
                mostrarError("Teléfono inválido", "El teléfono debe tener 9 dígitos.");
                return false;
            }
            
            // Validar que el usuario no exista (solo en modo creación)
            if (!modoEdicion && gestorSocket != null) {
                try {
                    gestorSocket.enviarPeticion(Protocolo.VERIFICAR_USUARIO_EXISTE + Protocolo.SEPARADOR_CODIGO + nombreUsuario);
                    gestorSocket.getSalida().flush();
                    
                    int codigo = gestorSocket.getEntrada().readInt();
                    if (codigo == Protocolo.VERIFICAR_USUARIO_EXISTE_RESPONSE) {
                        boolean existe = gestorSocket.getEntrada().readBoolean();
                        if (existe) {
                            mostrarError("Usuario existente", "El nombre de usuario ya existe en el sistema.");
                            return false;
                        }
                    } else {
                        System.err.println("Error al verificar si el usuario existe");
                    }
                } catch (IOException e) {
                    System.err.println("Error de comunicación al verificar usuario: " + e.getMessage());
                }
            }
            
            // Validar campos de veterinario si aplica
            if (comboRol.getValue() == Usuario.Rol.VETERINARIO) {
                // Verificar que los campos de veterinario existen
                if (txtEspecialidad == null || txtNumeroColegiado == null || 
                    txtHoraInicio == null || txtHoraFin == null) {
                    mostrarError("Error de inicialización", "No se han inicializado correctamente los campos de veterinario.");
                    return false;
                }
                
                String especialidad = txtEspecialidad.getText();
                String numeroColegiado = txtNumeroColegiado.getText();
                String horaInicio = txtHoraInicio.getText();
                String horaFin = txtHoraFin.getText();
                
                if (especialidad == null || numeroColegiado == null || 
                    horaInicio == null || horaFin == null) {
                    mostrarError("Error de datos", "Algunos campos de veterinario contienen valores nulos.");
                    return false;
                }
                
                if (especialidad.isEmpty() || numeroColegiado.isEmpty() || 
                    horaInicio.isEmpty() || horaFin.isEmpty()) {
                    mostrarError("Campos incompletos", "Todos los campos de veterinario son obligatorios.");
                    return false;
                }
                
                // Validar formato de hora
                if (!horaInicio.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$") || 
                    !horaFin.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    mostrarError("Formato de hora inválido", "El formato de hora debe ser HH:MM.");
                    return false;
                }
            }
            
            // Nota: La validación para creación de administradores se eliminó intencionalmente.
            // Ahora un usuario administrador puede crear otros administradores sin restricciones adicionales.
            
            return true;
        } catch (Exception e) {
            // Capturar cualquier excepción inesperada
            System.err.println("Error inesperado en validación de campos: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "Error inesperado en la validación: " + e.getMessage());
            return false;
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