package com.example.pruebamongodbcss.Modulos.InicioSesion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Protocolo.Protocolo;

import Utilidades.ScreensaverManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PanelInicioSesionController extends Application implements Initializable {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;  // Puerto principal (Docker)
    private static final int SERVER_PORT_ALT = 50002;  // Puerto alternativo (local)
    
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado = false;
    private ScreensaverManager screensaverManager;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Iniciando la aplicación...");
        try {
            // Inicializar el salvapantallas con la ventana principal
            screensaverManager = new ScreensaverManager(stage);
            
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/PruebaDoblePanel.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 700, 450);
            stage.setTitle("Inicio de sesión!");
            stage.setScene(scene);
            stage.show();
            
            // Iniciar el monitoreo de inactividad después de que la ventana esté visible
            Platform.runLater(() -> {
                screensaverManager.startInactivityMonitoring();
            });
            
            System.out.println("Aplicación iniciada correctamente");
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoPassword;

    @FXML
    private Button btnInicioSesion;

    @FXML
    private ProgressIndicator spinnerCarga;

    @FXML
    private StackPane recommendationPane;

    @FXML
    private VBox slideContainer;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnPrev;

    @FXML
    private VBox glassPanel;

    private final List<String> recomendaciones = List.of
    (
    "¿Están abastecidos los cajones?.",
    "¿Has encendido y limpiado las maquinas de análisis?",
    "Si estas solo recueda que tienes que tener cerrada la puerta con llave.",
    "Ante cualquier duda no dudes en preguntar a tu supervisor."
    );

    private int index = 0;

    private final List<VBox> slides = new java.util.ArrayList<>();
    private int currentSlide = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando controlador...");
        
        // Aplicar efecto de cristal (glassmorphism) al panel de login
        if (glassPanel != null) {
            glassPanel.setEffect(new javafx.scene.effect.GaussianBlur(2));
            System.out.println("Efecto cristal aplicado al panel de login");
        }
        
        conectarAlServidor();
        System.out.println("Creando diapositivas...");
        crearDiapositivas();
        System.out.println("Mostrando primera diapositiva...");
        mostrarSlide(currentSlide);
        
        // Configurar los botones
        if (btnNext != null) {
            System.out.println("Botón siguiente encontrado");
            btnNext.setOnAction(e -> siguienteRecomendacion());
        } else {
            System.err.println("¡Error! Botón siguiente no encontrado");
        }
        
        if (btnPrev != null) {
            System.out.println("Botón anterior encontrado");
            btnPrev.setOnAction(e -> anteriorRecomendacion());
        } else {
            System.err.println("¡Error! Botón anterior no encontrado");
        }
        
        if (slideContainer != null) {
            System.out.println("Contenedor de diapositivas encontrado");
        } else {
            System.err.println("¡Error! Contenedor de diapositivas no encontrado");
        }
    }

    private void conectarAlServidor() {
        
        // Si no se pudo conectar al principal, intentamos el alternativo
        try {
            System.out.println("Intentando conectar al servidor alternativo: " + SERVER_HOST + ":" + SERVER_PORT_ALT);
            
            socket = new Socket(SERVER_HOST, SERVER_PORT_ALT);
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            
            conectado = true;
            System.out.println("Conectado al servidor alternativo correctamente.");

            
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor alternativo: " + e.getMessage());
            conectado = false;
            mostrarMensaje("No se pudo conectar a ningún servidor.\nUsando modo local.");
        }
    }

    private void crearDiapositivas() {
        System.out.println("Creando diapositivas...");
        slides.clear(); // Limpiar diapositivas existentes
        
        VBox slide1 = crearSlide("ChichaVet", "Calidad y compromiso");
        VBox slide2 = crearSlide("Recordatorio", "¿Están abastecidos los cajones?");
        VBox slide3 = crearSlide("Checklist", "¿Has encendido y limpiado las maquinas de análisis?");
        VBox slide4 = crearSlide("Seguridad", "Si estás solo, recuerda cerrar la puerta con llave.");
        VBox slide5 = crearSlide("Apoyo", "Ante cualquier duda, no dudes en preguntar a tu supervisor.");
        
        slides.add(slide1);
        slides.add(slide2);
        slides.add(slide3);
        slides.add(slide4);
        slides.add(slide5);
        
        System.out.println("Se crearon " + slides.size() + " diapositivas");
    }

    private VBox crearSlide(String titulo, String contenido) {
        System.out.println("Creando diapositiva: " + titulo);
        
        Label labelTitulo = new Label(titulo);
        labelTitulo.setStyle("-fx-font-size: 24px; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
        labelTitulo.setWrapText(true);
        labelTitulo.setAlignment(javafx.geometry.Pos.CENTER);

        Label labelContenido = new Label(contenido);
        labelContenido.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");
        labelContenido.setWrapText(true);
        labelContenido.setAlignment(javafx.geometry.Pos.CENTER);

        VBox vbox = new VBox(10, labelTitulo, labelContenido);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setPadding(new javafx.geometry.Insets(20));
        vbox.setStyle("-fx-background-color: white;");
        
        return vbox;
    }

    private void mostrarSlide(int index) {
        System.out.println("Mostrando diapositiva " + index);
    
        if (slideContainer == null) {
            System.err.println("¡Error! slideContainer es null");
            return;
        }
    
        if (index < 0 || index >= slides.size()) {
            System.err.println("¡Error! Índice de diapositiva fuera de rango: " + index);
            return;
        }
    
        Platform.runLater(() -> {
            try {
                VBox slide = slides.get(index);
                animarTransicion(slide);
                System.out.println("Diapositiva " + index + " mostrada con transición");
            } catch (Exception e) {
                System.err.println("Error al mostrar diapositiva: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void animarTransicion(VBox nuevoSlide) {
        nuevoSlide.setOpacity(0);
        slideContainer.getChildren().clear();
        slideContainer.getChildren().add(nuevoSlide);
    
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), nuevoSlide);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    

    @FXML
    private void siguienteRecomendacion() {
        System.out.println("Botón siguiente presionado");
        currentSlide = (currentSlide + 1) % slides.size();
        mostrarSlide(currentSlide);
    }

    @FXML
    private void anteriorRecomendacion() {
        System.out.println("Botón anterior presionado");
        currentSlide = (currentSlide - 1 + slides.size()) % slides.size();
        mostrarSlide(currentSlide);
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
    


