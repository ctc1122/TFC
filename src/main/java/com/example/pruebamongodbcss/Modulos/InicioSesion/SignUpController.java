package com.example.pruebamongodbcss.Modulos.InicioSesion;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Clinica;
import com.example.pruebamongodbcss.Data.PatronExcepcion;
import com.example.pruebamongodbcss.Data.Usuario;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SignUpController implements Initializable {

    private static final String ADMIN_PASSWORD = "admin12345";

    @FXML
    private VBox signUpPanel;

    @FXML
    private TextField nombreField;

    @FXML
    private TextField apellidoField;

    @FXML
    private TextField usuarioField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telefonoField;

    @FXML
    private PasswordField passwordSignUpField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private PasswordField adminPasswordField;

    @FXML
    private ComboBox<String> rolComboBox;

    @FXML
    private Button submitButton;

    @FXML
    private Hyperlink loginLink;

    private ProgressIndicator spinnerCarga;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar el ComboBox de roles
        rolComboBox.getItems().addAll(
            "ADMINISTRADOR",
            "VETERINARIO",
            "RECEPCIONISTA",
            "AUXILIAR",
            "NORMAL"
        );
        rolComboBox.setValue("NORMAL"); // Valor por defecto
        
        // Crear el spinner de carga para usarlo más tarde
        spinnerCarga = new ProgressIndicator();
        spinnerCarga.setProgress(-1);
        spinnerCarga.setPrefSize(25, 25);
        spinnerCarga.setVisible(false);
        spinnerCarga.setStyle("-fx-progress-color: white;");
    }

    /**
     * Método para volver al panel de login
     */
    @FXML
    private void volverALogin(ActionEvent event) {
        try {
            // Cargar el panel de inicio de sesión completo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/PruebaDoblePanel.fxml"));
            Parent fullLoginPanel = loader.load();
            
            // Obtener el VBox que contiene el panel de login de PruebaDoblePanel
            VBox leftPanel = (VBox) signUpPanel.getParent();
            
            // Obtener la escena actual
            Scene currentScene = signUpPanel.getScene();
            
            // Reemplazar la escena completa en lugar de solo el panel
            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(fullLoginPanel, 900, 450));
            stage.centerOnScreen();
            
        } catch (Exception e) {
            System.err.println("Error al cargar el panel de login: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("Error al cargar el panel de login: " + e.getMessage());
        }
    }

    /**
     * Método para registrar un nuevo usuario
     */
    @FXML
    private void registrarUsuario(ActionEvent event) {
        // Mostrar el spinner de carga
        spinnerCarga.setVisible(true);
        
        // Agregar temporalmente el spinner al panel
        signUpPanel.getChildren().add(spinnerCarga);
        
        // Obtener los datos del formulario
        final String nombre = nombreField.getText().trim();
        final String apellido = apellidoField.getText().trim();
        final String usuario = usuarioField.getText().trim();
        final String email = emailField.getText().trim();
        final String telefono = telefonoField.getText().trim();
        final String password = passwordSignUpField.getText();
        final String confirmPassword = confirmPasswordField.getText();
        final String adminPassword = adminPasswordField.getText();
        final String rolSeleccionado = rolComboBox.getValue();
        
        // Validar en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Validar que todos los campos estén completos
                if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() ||
                    email.isEmpty() || telefono.isEmpty() || password.isEmpty() || 
                    confirmPassword.isEmpty() || adminPassword.isEmpty() || rolSeleccionado == null) {
                    throw new PatronExcepcion("Todos los campos son obligatorios");
                }
                
                // Validar que las contraseñas coincidan
                if (!password.equals(confirmPassword)) {
                    throw new PatronExcepcion("Las contraseñas no coinciden");
                }
                
                // Validar la contraseña de administrador
                if (!adminPassword.equals(ADMIN_PASSWORD)) {
                    throw new PatronExcepcion("La contraseña de administrador es incorrecta");
                }
                
                // Crear el usuario (esto ya valida los patrones de los campos)
                Usuario nuevoUsuario = new Usuario(nombre, apellido, usuario, password, email, telefono);
                
                // Establecer el rol seleccionado
                nuevoUsuario.setRol(Usuario.Rol.valueOf(rolSeleccionado));
                
                // Registrar el usuario en la base de datos
                Clinica clinica = new Clinica("12345678A", "ChichaVet", "Dirección de la clínica");
                clinica.registrarUsuario(nuevoUsuario);
                
                // Si llegamos aquí, el registro fue exitoso
                Platform.runLater(() -> {
                    // Eliminar el spinner
                    signUpPanel.getChildren().remove(spinnerCarga);
                    
                    mostrarMensaje("Usuario registrado exitosamente");
                    
                    // Limpiar los campos
                    limpiarCampos();
                    
                    // Volver al panel de login
                    loginLink.fire();
                });
                
            } catch (PatronExcepcion e) {
                // Error en las validaciones
                Platform.runLater(() -> {
                    // Eliminar el spinner
                    signUpPanel.getChildren().remove(spinnerCarga);
                    
                    mostrarMensaje(e.getMessage());
                });
            } catch (Exception e) {
                // Otro tipo de error
                Platform.runLater(() -> {
                    // Eliminar el spinner
                    signUpPanel.getChildren().remove(spinnerCarga);
                    
                    mostrarMensaje("Error al registrar el usuario: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Método para limpiar los campos del formulario
     */
    private void limpiarCampos() {
        nombreField.clear();
        apellidoField.clear();
        usuarioField.clear();
        emailField.clear();
        telefonoField.clear();
        passwordSignUpField.clear();
        confirmPasswordField.clear();
        adminPasswordField.clear();
        rolComboBox.setValue("NORMAL");
    }

    /**
     * Método para mostrar un mensaje en un diálogo de alerta
     */
    private void mostrarMensaje(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Mensaje");
            alert.setContentText(mensaje);
            
            // Si el mensaje contiene "exitoso", cerrar automáticamente después de 1.5 segundos
            if (mensaje.toLowerCase().contains("exitoso")) {
                // Mostrar la alerta sin bloquear
                alert.show();
                
                // Crear un temporizador para cerrarla automáticamente
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(event -> alert.close());
                delay.play();
            } else {
                // Para otros mensajes, usar showAndWait como antes
                alert.showAndWait();
            }
        });
    }
} 