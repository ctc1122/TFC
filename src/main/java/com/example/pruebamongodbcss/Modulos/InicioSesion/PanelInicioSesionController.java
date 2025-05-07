package com.example.pruebamongodbcss.Modulos.InicioSesion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Clinica;
import com.example.pruebamongodbcss.Data.PatronExcepcion;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import Utilidades.GestorConexion;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
            Scene scene = new Scene(fxmlLoader.load(), 900, 450);
            
            // Quitar decoración de la ventana
            stage.initStyle(StageStyle.UNDECORATED);
            
            // Variables para manejar el arrastre de la ventana
            final double[] xOffset = {0};
            final double[] yOffset = {0};
            
            // Evento para detectar cuando se presiona el mouse
            scene.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });
            
            // Evento para mover la ventana cuando se arrastra
            scene.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
            });
            
            stage.setTitle("Inicio de sesión!");
            stage.setScene(scene);
            stage.setResizable(false);
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
    private TextField campoPasswordVisible;
    
    @FXML
    private ImageView mostrarPasswordBtn;
    
    // Variable para controlar si la contraseña está visible o no
    private boolean passwordVisible = false;
    
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

    @FXML
    private Button btnSalir;

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

    private javafx.animation.Timeline slideTimer;

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
        
        // Iniciar el temporizador para cambiar las diapositivas automáticamente
        iniciarTransicionAutomatica();
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
        slides.clear();
        
        // Verificar las rutas de las imágenes
        String[] imagePaths = {
            null,
            "/PanelLogin/OrganizarCajones.png",
            "/PanelLogin/EncenderMaquinas.png",
            "/PanelLogin/Seguridad.png",
            "/PanelLogin/Consulta.png"
        };
        
        // Verificar cada ruta de imagen
        for (String path : imagePaths) {
            if (path != null) {
                System.out.println("Verificando imagen: " + path);
                java.io.InputStream stream = getClass().getResourceAsStream(path);
                System.out.println("¿Existe? " + (stream != null));
                if (stream == null) {
                    System.err.println("No se pudo encontrar la imagen en: " + path);
                } else {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        VBox slide1 = crearSlide("ChichaVet", "Calidad y compromiso", imagePaths[0]);
        VBox slide2 = crearSlide("Recordatorio", "¿Están abastecidos los cajones?", imagePaths[1]);
        VBox slide3 = crearSlide("Checklist", "¿Has encendido y limpiado las maquinas de análisis?", imagePaths[2]);
        VBox slide4 = crearSlide("Seguridad", "Si estás solo, recuerda cerrar la puerta con llave.", imagePaths[3]);
        VBox slide5 = crearSlide("Apoyo", "Ante cualquier duda, no dudes en preguntar a tu supervisor.", imagePaths[4]);
        
        slides.add(slide1);
        slides.add(slide2);
        slides.add(slide3);
        slides.add(slide4);
        slides.add(slide5);
        
        System.out.println("Se crearon " + slides.size() + " diapositivas");
    }

    private VBox crearSlide(String titulo, String contenido, String imagenPath) {
        System.out.println("Creando diapositiva: " + titulo);
        
        // Crear el VBox principal
        VBox vbox = new VBox();
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setPrefSize(500, 450);
        vbox.setMaxSize(500, 450);
        vbox.setMinSize(500, 450);
        vbox.setStyle("-fx-background-color: #1976d2;");

        // Crear un StackPane para la imagen y el overlay
        StackPane imagePane = new StackPane();
        imagePane.setAlignment(javafx.geometry.Pos.CENTER);
        imagePane.setPrefSize(500, 450);
        imagePane.setMaxSize(500, 450);
        imagePane.setMinSize(500, 450);

        // Si se proporciona una ruta de imagen, establecerla como fondo
        if (imagenPath != null && !imagenPath.isEmpty()) {
            try {
                System.out.println("Intentando cargar imagen: " + imagenPath);
                java.io.InputStream imageStream = getClass().getResourceAsStream(imagenPath);
                if (imageStream != null) {
                    Image image = new Image(imageStream);
                    ImageView imageView = new ImageView(image);
                    
                    // Configurar la imagen
                    imageView.setFitWidth(500);
                    imageView.setFitHeight(450);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageView.setCache(true);
                    
                    // Centrar la imagen
                    StackPane.setAlignment(imageView, javafx.geometry.Pos.CENTER);
                    imagePane.getChildren().add(imageView);
                    
                    // Agregar overlay semi-transparente
                    javafx.scene.shape.Rectangle overlay = new javafx.scene.shape.Rectangle(500, 450);
                    overlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.5));
                    StackPane.setAlignment(overlay, javafx.geometry.Pos.CENTER);
                    imagePane.getChildren().add(overlay);
                    
                    System.out.println("Imagen cargada correctamente: " + imagenPath);
                    imageStream.close();
                } else {
                    System.err.println("No se pudo encontrar la imagen: " + imagenPath);
                }
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen: " + imagenPath + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        // VBox para el texto
        VBox textBox = new VBox(20);
        textBox.setAlignment(javafx.geometry.Pos.CENTER);
        textBox.setPadding(new javafx.geometry.Insets(20));
        textBox.setMaxWidth(400);
        textBox.setMinWidth(400);
        textBox.setPrefWidth(400);

        // Título
        Label labelTitulo = new Label(titulo);
        labelTitulo.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 4, 0, 0, 0);"
        );
        labelTitulo.setWrapText(true);
        labelTitulo.setAlignment(javafx.geometry.Pos.CENTER);
        labelTitulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        labelTitulo.setMaxWidth(400);

        // Contenido
        Label labelContenido = new Label(contenido);
        labelContenido.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-text-fill: white;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 4, 0, 0, 0);"
        );
        labelContenido.setWrapText(true);
        labelContenido.setAlignment(javafx.geometry.Pos.CENTER);
        labelContenido.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        labelContenido.setMaxWidth(400);

        // Agregar elementos al textBox
        textBox.getChildren().addAll(labelTitulo, labelContenido);

        // Crear un StackPane final para superponer el texto sobre la imagen
        StackPane finalPane = new StackPane();
        finalPane.setAlignment(javafx.geometry.Pos.CENTER);
        finalPane.setPrefSize(500, 450);
        finalPane.setMaxSize(500, 450);
        finalPane.setMinSize(500, 450);
        finalPane.getChildren().addAll(imagePane, textBox);
        
        vbox.getChildren().add(finalPane);
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
        // Obtener el usuario tal como se escribe (respetando mayúsculas/minúsculas)
        String usuario = campoUsuario.getText();
        
        // Obtener la contraseña del campo visible o invisible según corresponda
        final String password;
        if (passwordVisible) {
            password = campoPasswordVisible.getText();
        } else {
            password = campoPassword.getText();
        }

        // Si el usuario o la contraseña están vacíos, mostrar un mensaje de error
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor, ingrese un usuario y contraseña.");
            return;
        }

        // Mostrar el spinner de carga
        spinnerCarga.setVisible(true);
        spinnerCarga.setProgress(-1); // -1 para animación infinita

        // Crear un hilo para la autenticación para no bloquear la interfaz
        new Thread(() -> {
            try {
                // Verificar si ya existen usuarios en la base de datos
                crearUsuariosPruebaEnBaseDatos();
                
                boolean autenticado = false;
                Usuario usuarioAutenticado = null;

                // Si estamos conectados al servidor, intentar autenticar a través de él
                if (conectado) {
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
                            if (codigoRespuesta == Protocolo.LOGIN_SUCCESS) {
                                autenticado = true;
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error en la comunicación con el servidor: " + e.getMessage());
                        conectado = false; // Marcar como desconectado para usar modo local
                    }
                }

                // Si no estamos conectados al servidor o falló la autenticación remota, usar la clase Clinica
                if (!conectado || !autenticado) {
                    try {
                        // Crear una instancia de la clínica
                        Clinica clinica = new Clinica("12345678A", "ChichaVet", "Dirección de la clínica");
                        
                        // Usar el método de iniciarSesion de la clase Clinica
                        usuarioAutenticado = clinica.iniciarSesion(usuario, password);
                        
                        // Si no lanza excepción, el usuario se autenticó correctamente
                        autenticado = (usuarioAutenticado != null);
                        
                    } catch (PatronExcepcion e) {
                        System.err.println("Error de autenticación: " + e.getMessage());
                        autenticado = false;
                        
                        // Imprimir información adicional para debug
                        System.out.println("Intentando iniciar sesión con: usuario='" + usuario + "', password='" + password + "'");
                        
                        // Si todo falla, usar el modo "Administrador"/"admin12345" como último recurso
                        if (usuario.equals("Administrador") && password.equals("admin12345")) {
                            autenticado = true;
                        }
                    } catch (Exception e) {
                        System.err.println("Error general al iniciar sesión con la clase Clinica: " + e.getMessage());
                        autenticado = false;
                        
                        // Si todo falla, usar el modo "Administrador"/"admin12345" como último recurso
                        if (usuario.equals("Administrador") && password.equals("admin12345")) {
                            autenticado = true;
                        }
                    }
                }

                // Procesar el resultado de la autenticación en el hilo de la UI
                final boolean resultadoFinal = autenticado;
                Platform.runLater(() -> {
                    spinnerCarga.setVisible(false); // Ocultar spinner
                    
                    if (resultadoFinal) {
                        mostrarMensaje("Inicio de sesión exitoso.");
                        cambiarPantalla("/com/example/pruebamongodbcss/panelInicio.fxml");
                    } else {
                        mostrarMensaje("Usuario o contraseña incorrectos.");
                    }
                });
            } catch (Exception e) {
                System.err.println("Error general en el proceso de login: " + e.getMessage());
                e.printStackTrace();
                
                // Procesar el error en el hilo de la UI
                Platform.runLater(() -> {
                    spinnerCarga.setVisible(false); // Ocultar spinner
                    mostrarMensaje("Error al iniciar sesión: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Crea usuarios de prueba en la base de datos si no existen.
     */
    private void crearUsuariosPruebaEnBaseDatos() {
        try {
            // Conectar a la base de datos de la empresa
            MongoDatabase empresaDB = GestorConexion.conectarEmpresa();
            MongoCollection<Document> usuariosCollection = empresaDB.getCollection("usuarios");
            
            // Verificar si existen usuarios en la colección
            long usuariosCount = usuariosCollection.countDocuments();
            
            if (usuariosCount == 0) {
                System.out.println("No se encontraron usuarios en la base de datos. Creando usuarios de prueba...");
                
                // Crear usuario Administrador (con mayúscula)
                Document adminDoc = new Document()
                    .append("nombre", "Administrador")
                    .append("email", "admin@sistema.com")
                    .append("contraseña", "admin12345")
                    .append("telefono", "123456789")
                    .append("rol", "ADMIN");
                usuariosCollection.insertOne(adminDoc);
                
                // Crear un usuario normal para pruebas
                Document usuarioDoc = new Document()
                    .append("nombre", "usuario")
                    .append("email", "usuario@chichavet.com")
                    .append("contraseña", "usuario123")
                    .append("telefono", "123456789")
                    .append("rol", "USER");
                usuariosCollection.insertOne(usuarioDoc);
                
                System.out.println("Usuarios de prueba creados con éxito");
            } else {
                System.out.println("Ya existen " + usuariosCount + " usuarios en la base de datos");
            }
        } catch (Exception e) {
            System.err.println("Error al crear usuarios de prueba: " + e.getMessage());
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

    /**
     * Método para alternar entre mostrar y ocultar la contraseña
     */
    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        
        // Si la contraseña debe mostrarse
        if (passwordVisible) {
            // Copiar el texto de la contraseña al campo de texto
            campoPasswordVisible.setText(campoPassword.getText());
            // Mostrar el campo de texto y ocultar el campo de contraseña
            campoPasswordVisible.setVisible(true);
            campoPassword.setVisible(false);
        } else {
            // Copiar el texto del campo de texto al campo de contraseña
            campoPassword.setText(campoPasswordVisible.getText());
            // Mostrar el campo de contraseña y ocultar el campo de texto
            campoPasswordVisible.setVisible(false);
            campoPassword.setVisible(true);
        }
    }

    private void iniciarTransicionAutomatica() {
        slideTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(4), event -> {
                currentSlide = (currentSlide + 1) % slides.size();
                mostrarSlide(currentSlide);
            })
        );
        slideTimer.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        slideTimer.play();
    }

    @FXML
    private void cerrarAplicacion() {
        System.out.println("Cerrando la aplicación...");
        
        // Detener el temporizador de diapositivas si está activo
        if (slideTimer != null) {
            slideTimer.stop();
        }
        
        // Cerrar la conexión con el servidor si está activa
        cerrarConexion();
        
        // Cerrar la aplicación
        Platform.runLater(() -> {
            Stage stage = (Stage) btnSalir.getScene().getWindow();
            stage.close();
            Platform.exit();
            System.exit(0);
        });
    }
}
    


