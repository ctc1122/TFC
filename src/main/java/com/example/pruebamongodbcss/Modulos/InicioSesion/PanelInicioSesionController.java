package com.example.pruebamongodbcss.Modulos.InicioSesion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.PanelInicioMain;
import com.example.pruebamongodbcss.Protocolo.Protocolo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PanelInicioSesionController extends Application implements Initializable {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 50000;  // Puerto principal (Docker)
    private static final int SERVER_PORT_ALT = 50002;  // Puerto alternativo (local)
    
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado = false;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(PanelInicioMain.class.getResource("/com/example/pruebamongodbcss/InicioSesion/PanelInicioSesion.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 450);
        stage.setTitle("Inicio de sesión!");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private TextField campoUsuario;

    @FXML
    private TextField campoPassword;

    @FXML
    private Button btnInicioSesion;

    @FXML
    private ProgressIndicator spinnerCarga;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Intentar conectar al servidor al iniciar la app
        conectarAlServidor();
    }

    private void conectarAlServidor() {
        // Primero intentamos conectar al puerto principal (Docker)
        try {
            System.out.println("Conectando al servidor principal: " + SERVER_HOST + ":" + SERVER_PORT);
            
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            
            conectado = true;
            System.out.println("Conectado al servidor principal correctamente.");
            return;
            
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor principal: " + e.getMessage());
        }
        
        // Si no se pudo conectar al principal, intentamos el alternativo
        try {
            System.out.println("Intentando conectar al servidor alternativo: " + SERVER_HOST + ":" + SERVER_PORT_ALT);
            
            socket = new Socket(SERVER_HOST, SERVER_PORT_ALT);
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            
            conectado = true;
            System.out.println("Conectado al servidor alternativo correctamente.");
            return;
            
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor alternativo: " + e.getMessage());
            conectado = false;
            mostrarMensaje("No se pudo conectar a ningún servidor.\nUsando modo local.");
        }
    }

    /**
     * Método para iniciar sesión en el servidor MongoDB consulta los datos de usuario y contraseña en la base de datos de MongoDB
     * @return void
     * @throws IOException
     */
    @FXML
    private void inicioSesion() {
        String usuario = campoUsuario.getText().toLowerCase();
        String password = campoPassword.getText().toLowerCase();

        // Si el usuario o la contraseña están vacíos, mostrar un mensaje de error
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor, ingrese un usuario y contraseña.");
            return;
        }

        // Si no estamos conectados al servidor, usar modo local de prueba
        if (!conectado) {
            if (usuario.equals("admin") && password.equals("admin")) {
                mostrarMensaje("Inicio de sesión exitoso (modo local).");
                cambiarPantalla("/com/example/pruebamongodbcss/panelInicio.fxml");
            } else {
                mostrarMensaje("Usuario o contraseña incorrectos (modo local).");
            }
            return;
        }
        
        // Modo conectado al servidor
        try {
            // Enviar petición de login
            salida.writeInt(Protocolo.LOGIN_REQUEST);
            salida.writeUTF(usuario);
            salida.writeUTF(password);
            //flush para enviar los datos al servidor
            salida.flush();

            // Recibir respuesta
            int tipoRespuesta = entrada.readInt();
            if (tipoRespuesta == Protocolo.LOGIN_RESPONSE) {
                int codigoRespuesta = entrada.readInt();
                // Si el código de respuesta es 1, se ha iniciado sesión correctamente
                switch (codigoRespuesta) {
                    case Protocolo.LOGIN_SUCCESS:
                        mostrarMensaje("Inicio de sesión exitoso.");
                        // Cambiar a la siguiente pantalla
                        cambiarPantalla("/com/example/pruebamongodbcss/panelInicio.fxml");
                        break;
                    case Protocolo.LOGIN_FAILED:
                        mostrarMensaje("Usuario o contraseña incorrectos.");
                        break;
                    default:
                        mostrarMensaje("Error desconocido en el inicio de sesión.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el servidor: " + e.getMessage());
            mostrarMensaje("Error en la comunicación con el servidor: " + e.getMessage() + 
                          "\nUsando modo local...");
            
            // Intentar modo local como fallback
            if (usuario.equals("admin") && password.equals("admin")) {
                mostrarMensaje("Inicio de sesión exitoso (modo local).");
                cambiarPantalla("/com/example/pruebamongodbcss/panelInicio.fxml");
            } else {
                mostrarMensaje("Usuario o contraseña incorrectos (modo local).");
            }
        }
    }

    /**
     * Método para mostrar un mensaje en la pantalla de tipo información
     * @param mensaje
     * @return void
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

    /**
     * Método para cerrar la conexión con el servidor
     * @return void
     */
    public void cerrarConexion() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    /**
     * Método para cambiar de pantalla
     * @param nombreFXML
     * @return void
     */
    public void cambiarPantalla(String nombreFXML) {
        // Mostrar el spinner
        Platform.runLater(() -> {
            spinnerCarga.setVisible(true);
            spinnerCarga.setProgress(-1); // -1 para animación infinita
        });

        // Cargar la nueva escena en un hilo separado
        new Thread(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(nombreFXML));
                Scene nuevaEscena = new Scene(fxmlLoader.load(), 700, 450);
                
                // Cambiar la escena en el hilo de JavaFX
                Platform.runLater(() -> {
                    // Cerrar cualquier diálogo de alerta que esté abierto
                    // Usando un enfoque más seguro para cerrar diálogos
                    try {
                        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                            if (window instanceof Stage && window.isShowing() && 
                                ((Stage) window).getModality() == javafx.stage.Modality.APPLICATION_MODAL) {
                                ((Stage) window).close();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error al cerrar diálogos: " + e.getMessage());
                    }
                    
                    Stage stage = (Stage) campoUsuario.getScene().getWindow();
                    stage.setScene(nuevaEscena);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    spinnerCarga.setVisible(false);
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Error al cambiar de pantalla: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
}
    


