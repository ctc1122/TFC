package com.example.pruebamongodbcss.Modulos.InicioSesion;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ResourceBundle;

import org.bson.Document;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.theme.ThemeManager;

import Utilidades1.GestorConexion;
import Utilidades1.GestorSocket;
import Utilidades1.InicializadorMongoDB;
import Utilidades1.importarUMLSsql;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class PanelInicioSesionController extends Application implements Initializable {
// Puerto alternativo (local)
    
    private GestorSocket gestorSocket;
    private boolean conectado = false;

    @FXML
    private VBox leftPanel; // Panel izquierdo que contiene el login
    
    @FXML
    private VBox loginPanel; // Panel de login
    
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
    private Button btnSalir;

    @FXML
    private Hyperlink signUpLink;
    
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

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Iniciando la aplicación...");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/PruebaDoblePanel.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 900, 450);
            //450 width y 900 height
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
            
            // Establecer el icono de la ventana
            try {
                Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
                stage.getIcons().add(icon);
                System.out.println("Icono de la ventana establecido correctamente");
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono de la ventana: " + e.getMessage());
            }
            
            stage.setOnCloseRequest(event -> {
                stage.close();
            });
            stage.setResizable(false);
            stage.show();
            
            System.out.println("Aplicación iniciada correctamente");
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando controlador...");
        
        // Cargar la imagen del icono para mostrar/ocultar contraseña
        try {
            mostrarPasswordBtn.setImage(new Image(getClass().getResourceAsStream("/Iconos/iconPassword.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono del ojo: " + e.getMessage());
        }
        
        conectarAlServidor();
        
        // Verificar e inicializar base de datos UMLS si es necesario
        verificarEInicializarUMLS();
        
        // Verificar e inicializar MongoDB si es necesario
        verificarEInicializarMongoDB();
        
        System.out.println("Creando diapositivas...");
        crearDiapositivas();
        System.out.println("Mostrando primera diapositiva...");
        mostrarSlide(currentSlide);
        
        // Iniciar el temporizador para cambiar las diapositivas automáticamente
        iniciarTransicionAutomatica();
    }

    private void conectarAlServidor() {
        try {
            gestorSocket = GestorSocket.getInstance();
            conectado = gestorSocket.isConectado();
            System.out.println("Conectado: " + conectado);
            if (!conectado) {
                mostrarMensaje("No se pudo conectar al servidor.\nUsando modo local.");
            }
        } catch (Exception e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            conectado = false;
            mostrarMensaje("No se pudo conectar al servidor.\nUsando modo local.");
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
        vbox.setStyle("-fx-background-color: #283618;");

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
        final String usuario = campoUsuario.getText().trim();
        
        // Obtener la contraseña del campo visible o invisible según corresponda
        final String password;
        if (passwordVisible) {
            password = campoPasswordVisible.getText().trim();
        } else {
            password = campoPassword.getText().trim();
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
                boolean autenticado = false;
                Usuario usuarioAutenticado = null;

                System.out.println("Conectado: " + conectado);

                // Si estamos conectados al servidor, intentar autenticar a través de él
                if (conectado) {
                    try {
                        System.out.println("Enviando solicitud de inicio de sesión al servidor...");
                        // Construir el mensaje con el formato correcto
                        String mensaje = Protocolo.LOGIN_REQUEST + Protocolo.SEPARADOR_CODIGO + 
                                      usuario + Protocolo.SEPARADOR_PARAMETROS + password;
                        System.out.println("Mensaje a enviar: " + mensaje);
                        
                        // Obtener el stream de entrada ANTES de enviar la petición
                        ObjectInputStream entrada = gestorSocket.getEntrada();
                        if (entrada == null) {
                            throw new IOException("No se pudo obtener el stream de entrada");
                        }
                        
                        // Enviar la petición
                        gestorSocket.enviarPeticion(mensaje);
                        System.out.println("Datos enviados al servidor. Esperando respuesta...");

                        try {
                            System.out.println("Leyendo tipo de respuesta...");
                            int tipoRespuesta = entrada.readInt();
                            System.out.println("Tipo de respuesta recibido: " + tipoRespuesta);
                            
                            if (tipoRespuesta == Protocolo.LOGIN_RESPONSE) {
                                System.out.println("Leyendo código de respuesta...");
                                int codigoRespuesta = entrada.readInt();
                                System.out.println("Código de respuesta recibido: " + codigoRespuesta);
                                
                                if (codigoRespuesta == Protocolo.LOGIN_SUCCESS) {
                                    System.out.println("Login exitoso");
                                    autenticado = true;
                                } else if (codigoRespuesta == Protocolo.INVALID_CREDENTIALS) {
                                    System.out.println("Credenciales inválidas");
                                    mostrarMensaje("Credenciales inválidas. Por favor, verifique su usuario y contraseña.");
                                } else {
                                    System.out.println("Login fallido");
                                    mostrarMensaje("Error al iniciar sesión. Por favor, intente nuevamente.");
                                }
                            } else {
                                System.out.println("Tipo de respuesta no esperado: " + tipoRespuesta);
                                mostrarMensaje("Error de comunicación con el servidor.");
                            }
                        } catch (EOFException e) {
                            System.err.println("Error al leer la respuesta: conexión cerrada inesperadamente");
                            throw new IOException("La conexión se cerró mientras se leía la respuesta");
                        }
                    } catch (IOException e) {
                        System.err.println("Error en la comunicación con el servidor: " + e.getMessage());
                        e.printStackTrace();
                        conectado = false; // Marcar como desconectado para usar modo local
                        mostrarMensaje("Error de conexión con el servidor. Usando modo local.");
                    }
                }
                // Procesar el resultado de la autenticación en el hilo de la UI
                final boolean resultadoFinal = autenticado;
                final Usuario usuarioFinal = usuarioAutenticado;
                
                Platform.runLater(() -> {
                    spinnerCarga.setVisible(false); // Ocultar spinner
                    
                    if (resultadoFinal) {
                        mostrarMensaje("Inicio de sesión exitoso.");
                        
                        // Intentar obtener el usuario completo de la base de datos
                        try {
                            // Si tenemos un usuario autenticado, usarlo directamente
                            if (usuarioFinal != null) {
                                cambiarAMenuPrincipal(usuarioFinal);
                            } else {
                                // Buscar el usuario completo en la base de datos
                                //Vamos a hacer una petición al servidor para obtener el usuario completo
                                String mensaje = Protocolo.GET_USER_REQUEST + Protocolo.SEPARADOR_CODIGO + usuario + Protocolo.SEPARADOR_PARAMETROS + password;
                                gestorSocket.enviarPeticion(mensaje);
                                
                                ObjectInputStream entrada = gestorSocket.getEntrada();
                                if (entrada == null) {
                                    throw new IOException("No se pudo obtener el stream de entrada");
                                }

                                int tipoRespuesta = entrada.readInt();
                                if (tipoRespuesta == Protocolo.GET_USER_RESPONSE) {
                                    Usuario usuarioCompleto = (Usuario) entrada.readObject();
                                    cambiarAMenuPrincipal(usuarioCompleto);
                                } else {
                                    mostrarMensaje("Error: No se pudo obtener información completa del usuario.");
                                }

                            }
                        } catch (Exception e) {
                            System.err.println("Error al obtener usuario completo: " + e.getMessage());
                            mostrarMensaje("Error al cargar datos de usuario: " + e.getMessage());
                        }
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
    
    
    private void cambiarAMenuPrincipal(Usuario usuario) {
        try {
            System.out.println("Cambiando a menú principal con usuario: " + usuario.getNombre());
            
            // Detener el temporizador de diapositivas si está activo
            if (slideTimer != null) {
                slideTimer.stop();
                System.out.println("Timer de diapositivas detenido");
            }
            
            // Cargar el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/panelInicio.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador y establecer el usuario
            com.example.pruebamongodbcss.PanelInicioController controller = loader.getController();
            controller.setUsuarioActual(usuario);
            
            // Obtener la ventana actual
            Stage currentStage = (Stage) btnInicioSesion.getScene().getWindow();
            
            // Crear una nueva escena responsive (sin dimensiones fijas)
            Scene scene = new Scene(root);
            
            // Registrar la escena en el ThemeManager
            ThemeManager.getInstance().registerScene(scene);
            
            // Si necesitamos una ventana decorada, y la actual no lo es, crear una nueva
            if (currentStage.getStyle() == StageStyle.UNDECORATED) {
                // Crear una nueva ventana con decoración
                Stage newStage = new Stage();
                newStage.setTitle("ChichaVet - Clínica Veterinaria");
                
                // Establecer el icono de la ventana
                try {
                    Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
                    newStage.getIcons().add(icon);
                } catch (Exception e) {
                    System.err.println("No se pudo cargar el icono de la ventana: " + e.getMessage());
                }
                
                // Hacer la ventana redimensionable y responsive
                newStage.setResizable(true);
                
                newStage.setOnCloseRequest(event -> {
                    event.consume();
                    cerrarAplicacion();
                });
                
                // Establecer la escena
                newStage.setScene(scene);
                
                // Cerrar la ventana original
                currentStage.hide();
                
                // Mostrar la nueva ventana y maximizarla automáticamente
                newStage.show();
                newStage.setMaximized(true); // Maximizar automáticamente para ocupar toda la pantalla
                
            } else {
                // Usar la ventana actual
                currentStage.setResizable(true); // Hacer redimensionable
                currentStage.setScene(scene);
                currentStage.setMaximized(true); // Maximizar automáticamente
            }
            
        } catch (Exception e) {
            System.err.println("Error al cambiar a menú principal: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("Error al cargar el menú principal: " + e.getMessage());
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
        if (gestorSocket != null) {
            gestorSocket.cerrarConexion();
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
                        for (Window window : javafx.stage.Window.getWindows()) {
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

    /**
     * Método para crear el panel de login dinámicamente
     */
    private void crearPanelLogin() {
        // Crear el panel principal
        loginPanel = new VBox();
        loginPanel.setAlignment(javafx.geometry.Pos.CENTER);
        loginPanel.setCache(true);
        loginPanel.setMaxWidth(360.0);
        loginPanel.setPrefWidth(360.0);
        loginPanel.setSpacing(8.0); // Reducir el espaciado entre elementos
        loginPanel.getStyleClass().add("glass-panel");
        
        // Título
        Label titulo = new Label("Inicio de Sesión");
        titulo.getStyleClass().add("title");
        titulo.setFont(new javafx.scene.text.Font("Dubai Regular", 24.0));
        loginPanel.getChildren().add(titulo);
        
        // Campo de usuario
        VBox userBox = new VBox();
        userBox.getStyleClass().add("user-box");
        campoUsuario = new TextField();
        campoUsuario.setPromptText("Usuario");
        campoUsuario.getStyleClass().add("input-field");
        userBox.getChildren().add(campoUsuario);
        loginPanel.getChildren().add(userBox);
        
        // Campo de contraseña y su versión visible
        VBox passwordBox = new VBox();
        passwordBox.getStyleClass().add("user-box");
        passwordBox.setMaxHeight(35.0); // Limitar altura
        
        StackPane passwordStackPane = new StackPane();
        passwordStackPane.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        campoPassword = new PasswordField();
        campoPassword.setPromptText("Contraseña");
        campoPassword.getStyleClass().add("input-field");
        
        campoPasswordVisible = new TextField();
        campoPasswordVisible.setPromptText("Contraseña");
        campoPasswordVisible.getStyleClass().add("input-field");
        campoPasswordVisible.setVisible(false); // Inicialmente oculto
        
        // ImageView para mostrar/ocultar contraseña
        mostrarPasswordBtn = new ImageView();
        mostrarPasswordBtn.setFitHeight(20.0);
        mostrarPasswordBtn.setFitWidth(20.0);
        mostrarPasswordBtn.setPreserveRatio(true);
        mostrarPasswordBtn.setCursor(javafx.scene.Cursor.HAND);
        
        // Cargar imagen del ojo
        try {
            mostrarPasswordBtn.setImage(new Image(getClass().getResourceAsStream("/Iconos/ojo.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono del ojo: " + e.getMessage());
        }
        
        // Configurar evento para mostrar/ocultar contraseña
        mostrarPasswordBtn.setOnMouseClicked(event -> togglePasswordVisibility());
        
        // Agregar los elementos al StackPane
        passwordStackPane.getChildren().add(campoPassword);
        passwordStackPane.getChildren().add(campoPasswordVisible);
        passwordStackPane.getChildren().add(mostrarPasswordBtn);
        StackPane.setAlignment(mostrarPasswordBtn, javafx.geometry.Pos.CENTER_RIGHT);
        StackPane.setMargin(mostrarPasswordBtn, new javafx.geometry.Insets(0, 5, 0, 0));
        
        passwordBox.getChildren().add(passwordStackPane);
        loginPanel.getChildren().add(passwordBox);
        
        // Spinner de carga (con margen reducido)
        spinnerCarga = new ProgressIndicator();
        spinnerCarga.setProgress(-1);
        spinnerCarga.setPrefSize(25, 25);
        spinnerCarga.setVisible(false);
        spinnerCarga.setMaxHeight(25);
        VBox.setMargin(spinnerCarga, new javafx.geometry.Insets(2, 0, 2, 0));
        loginPanel.getChildren().add(spinnerCarga);
        
        // Botón de inicio de sesión
        btnInicioSesion = new Button("INICIAR SESIÓN");
        btnInicioSesion.setPrefHeight(40.0);
        btnInicioSesion.setPrefWidth(280.0);
        btnInicioSesion.setOnAction(event -> inicioSesion());
        VBox.setMargin(btnInicioSesion, new javafx.geometry.Insets(5, 0, 5, 0));
        loginPanel.getChildren().add(btnInicioSesion);
        
        // Enlace de registro
        HBox signUpLinkBox = new HBox();
        signUpLinkBox.setAlignment(javafx.geometry.Pos.CENTER);
        signUpLinkBox.setSpacing(5.0);
        
        Label signUpText = new Label("¿No tienes cuenta?");
        signUpText.getStyleClass().add("login-text");
        
        signUpLink = new Hyperlink("Regístrate");
        signUpLink.getStyleClass().add("sign-up-link");
        signUpLink.setOnAction(event -> cambiarARegistro());
        
        signUpLinkBox.getChildren().addAll(signUpText, signUpLink);
        loginPanel.getChildren().add(signUpLinkBox);
        
        // Agregar efecto de reflejo
        javafx.scene.effect.Reflection reflection = new javafx.scene.effect.Reflection();
        reflection.setFraction(0.2);
        reflection.setTopOpacity(0.28);
        loginPanel.setEffect(reflection);
        
        // Agregar el panel al contenedor
        leftPanel.getChildren().add(loginPanel);
    }

    /**
     * Método para cambiar al panel de registro
     */
    @FXML
    private void cambiarARegistro() {
        try {
            // Cargar el panel de registro desde el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/SignUp.fxml"));
            Parent signUpPanel = loader.load();
            
            // Crear una nueva escena con el panel de registro en un HBox junto al panel derecho original
            HBox newRoot = new HBox();
            newRoot.setPrefSize(900, 450);
            
            // Obtener el panel derecho (recommendationPane) desde la escena actual
            StackPane rightPanel = recommendationPane;
            
            // Crear un nuevo VBox para el panel izquierdo con el mismo estilo que el leftPanel original
            VBox newLeftPanel = new VBox();
            newLeftPanel.setAlignment(javafx.geometry.Pos.CENTER);
            newLeftPanel.setMaxWidth(400.0);
            newLeftPanel.setMinWidth(400.0);
            newLeftPanel.setPrefWidth(400.0);
            newLeftPanel.setPrefHeight(450.0);
            newLeftPanel.getStyleClass().add("panel-azul-fondo");
            
            // Añadir el panel de registro al nuevo panel izquierdo
            newLeftPanel.getChildren().add(signUpPanel);
            
            // Añadir el botón SALIR en la parte inferior
            Button exitButton = new Button("SALIR");
            exitButton.getStyleClass().add("button-salir");
            exitButton.setPrefHeight(40.0);
            exitButton.setPrefWidth(120.0);
            exitButton.setOnAction(event -> cerrarAplicacion());
            VBox.setMargin(exitButton, new Insets(15.0, 0, 15.0, 0));
            newLeftPanel.getChildren().add(exitButton);
            
            // Añadir los paneles izquierdo y derecho al nuevo HBox
            newRoot.getChildren().addAll(newLeftPanel, rightPanel);
            
            // Obtener la escena actual y el escenario
            Scene currentScene = leftPanel.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            
            // Crear y establecer la nueva escena
            Scene newScene = new Scene(newRoot, 900, 450);
            newScene.getStylesheets().add(getClass().getResource("/com/example/pruebamongodbcss/InicioSesion/PanelInicioSesionEstilo.css").toExternalForm());
            
            // Configurar eventos de arrastre para la nueva escena
            final double[] xOffset = {0};
            final double[] yOffset = {0};
            
            newScene.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });
            
            newScene.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
            });
            
            stage.setScene(newScene);
            
        } catch (IOException e) {
            System.err.println("Error al cargar el panel de registro: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("Error al cargar el panel de registro: " + e.getMessage());
        }
    }

    /**
     * Verifica si existe la base de datos UMLS y la tabla de diagnósticos
     * Si no existe, ejecuta automáticamente la importación
     */
    private void verificarEInicializarUMLS() {
        // Ejecutar verificación en un hilo separado para no bloquear la UI
        new Thread(() -> {
            System.out.println("🔍 Verificando base de datos UMLS...");
            
            // Configuración de conexión a MariaDB (igual que en importarUMLSsql)
            final String DB_URL = "jdbc:mariadb://localhost:3306/";
            final String DB_NAME = "umls";
            final String USERNAME = "root";
            final String PASSWORD = "miclave";
            final String DRIVER = "org.mariadb.jdbc.Driver";
            
            Connection connection = null;
            
            try {
                // Cargar el driver de MariaDB
                Class.forName(DRIVER);
                
                // Conectar a MariaDB
                String urlCompleta = DB_URL + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true";
                connection = DriverManager.getConnection(urlCompleta, USERNAME, PASSWORD);
                
                // Verificar si existe alguna tabla con diagnósticos
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '%Diagn%'");
                
                boolean tablasDiagnosticosExisten = rs.next();
                rs.close();
                
                if (!tablasDiagnosticosExisten) {
                    // No existen tablas de diagnósticos, verificar si existe alguna tabla
                    rs = stmt.executeQuery("SHOW TABLES");
                    boolean hayTablas = rs.next();
                    rs.close();
                    
                    if (!hayTablas) {
                        System.out.println("⚠️ Base de datos UMLS vacía - Iniciando importación automática...");
                        
                        // Mostrar mensaje informativo en la UI
                        Platform.runLater(() -> {
                            mostrarMensaje("Inicializando base de datos UMLS...\nEsto puede tardar unos minutos.");
                        });
                        
                        // Ejecutar la importación
                        try {
                            System.out.println("🚀 Ejecutando importación de diagnósticos UMLS...");
                            importarUMLSsql.main(new String[]{});
                            System.out.println("✅ Importación UMLS completada exitosamente");
                            
                            // Mostrar mensaje de éxito en la UI
                            Platform.runLater(() -> {
                                mostrarMensaje("Base de datos UMLS inicializada correctamente.");
                            });
                            
                        } catch (Exception e) {
                            System.err.println("❌ Error durante la importación UMLS: " + e.getMessage());
                            e.printStackTrace();
                            
                            Platform.runLater(() -> {
                                mostrarMensaje("Error al inicializar base de datos UMLS: " + e.getMessage());
                            });
                        }
                    } else {
                        System.out.println("ℹ️ Base de datos UMLS existe pero no tiene tablas de diagnósticos");
                    }
                } else {
                    System.out.println("✅ Base de datos UMLS ya existe y contiene diagnósticos");
                }
                
                stmt.close();
                
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MariaDB no encontrado: " + e.getMessage());
                Platform.runLater(() -> {
                    mostrarMensaje("Error: Driver MariaDB no encontrado");
                });
            } catch (SQLException e) {
                // Si no se puede conectar a la base de datos específica, verificar si MariaDB está funcionando
                System.err.println("⚠️ No se pudo conectar a la base de datos UMLS: " + e.getMessage());
                
                // Intentar conectar sin especificar base de datos para verificar si MariaDB funciona
                try {
                    String urlSinBD = DB_URL + "?useSSL=false&allowPublicKeyRetrieval=true";
                    Connection connTest = DriverManager.getConnection(urlSinBD, USERNAME, PASSWORD);
                    
                    // MariaDB funciona, crear base de datos
                    Statement stmt = connTest.createStatement();
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + 
                                     " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    stmt.close();
                    connTest.close();
                    
                    System.out.println("✅ Base de datos UMLS creada - Iniciando importación...");
                    
                    Platform.runLater(() -> {
                        mostrarMensaje("Creando base de datos UMLS...\nEsto puede tardar unos minutos.");
                    });
                    
                    // Ejecutar la importación
                    try {
                        importarUMLSsql.main(new String[]{});
                        System.out.println("✅ Importación UMLS completada exitosamente");
                        
                        Platform.runLater(() -> {
                            mostrarMensaje("Base de datos UMLS inicializada correctamente.");
                        });
                        
                    } catch (Exception ex) {
                        System.err.println("❌ Error durante la importación UMLS: " + ex.getMessage());
                        ex.printStackTrace();
                        
                        Platform.runLater(() -> {
                            mostrarMensaje("Error al inicializar base de datos UMLS: " + ex.getMessage());
                        });
                    }
                    
                } catch (SQLException ex) {
                    System.err.println("❌ MariaDB no está disponible: " + ex.getMessage());
                    Platform.runLater(() -> {
                        mostrarMensaje("Advertencia: MariaDB no está disponible.\nAlgunas funciones pueden no funcionar.");
                    });
                }
            } catch (Exception e) {
                System.err.println("❌ Error inesperado al verificar UMLS: " + e.getMessage());
                e.printStackTrace();
                
                Platform.runLater(() -> {
                    mostrarMensaje("Error inesperado al verificar base de datos UMLS: " + e.getMessage());
                });
            } finally {
                // Cerrar conexión
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        System.err.println("Error al cerrar conexión: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Verifica si existe la base de datos MongoDB y la colección de usuarios
     * Si no existe, ejecuta automáticamente la creación
     */
    private void verificarEInicializarMongoDB() {
        // Ejecutar verificación en un hilo separado para no bloquear la UI
        new Thread(() -> {
            System.out.println("🔍 Verificando base de datos MongoDB...");
            
            try {
                // Verificar si existe el usuario admin en la base de datos Empresa
                com.mongodb.client.MongoDatabase empresaDB = GestorConexion.conectarBD("Empresa");
                com.mongodb.client.MongoCollection<Document> usuariosCollection = empresaDB.getCollection("usuarios");
                
                // Buscar el usuario admin específico
                Document adminQuery = new Document("usuario", "admin");
                Document adminUser = usuariosCollection.find(adminQuery).first();
                
                if (adminUser == null) {
                    System.out.println("⚠️ Usuario admin no encontrado - Iniciando inicialización de MongoDB...");
                    
                    // Mostrar mensaje informativo en la UI
                    Platform.runLater(() -> {
                        mostrarMensaje("Inicializando base de datos MongoDB...\nCreando colecciones y usuario administrador...");
                    });
                    
                    // Ejecutar la inicialización de MongoDB
                    try {
                        System.out.println("🚀 Ejecutando inicialización de MongoDB...");
                        InicializadorMongoDB.main(new String[]{});
                        System.out.println("✅ Inicialización de MongoDB completada exitosamente");
                        
                        // Mostrar mensaje de éxito en la UI
                        Platform.runLater(() -> {
                            mostrarMensaje("Base de datos MongoDB inicializada correctamente.\nUsuario admin creado: admin/admin12345");
                        });
                        
                    } catch (Exception e) {
                        System.err.println("❌ Error durante la inicialización de MongoDB: " + e.getMessage());
                        e.printStackTrace();
                        
                        Platform.runLater(() -> {
                            mostrarMensaje("Error al inicializar MongoDB: " + e.getMessage());
                        });
                    }
                } else {
                    System.out.println("✅ Base de datos MongoDB ya está inicializada (usuario admin existe)");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error al verificar MongoDB: " + e.getMessage());
                
                // Verificar si es un problema de conexión (MongoDB no está funcionando)
                if (e.getMessage().contains("Connection refused") || e.getMessage().contains("UnknownHostException")) {
                    System.err.println("⚠️ MongoDB no está disponible - Intentando inicializar de todos modos...");
                    
                    Platform.runLater(() -> {
                        mostrarMensaje("MongoDB no disponible.\nAsegúrate de que Docker esté funcionando con las bases de datos.");
                    });
                } else {
                    // Error diferente, intentar inicializar
                    Platform.runLater(() -> {
                        mostrarMensaje("Inicializando MongoDB...\nPrimera ejecución del sistema.");
                    });
                    
                    try {
                        System.out.println("🚀 Intentando inicialización de MongoDB debido a error de verificación...");
                        InicializadorMongoDB.main(new String[]{});
                        System.out.println("✅ Inicialización de MongoDB completada");
                        
                        Platform.runLater(() -> {
                            mostrarMensaje("MongoDB inicializado correctamente.");
                        });
                        
                    } catch (Exception ex) {
                        System.err.println("❌ Error durante inicialización de emergencia: " + ex.getMessage());
                        ex.printStackTrace();
                        
                        Platform.runLater(() -> {
                            mostrarMensaje("Error crítico con MongoDB: " + ex.getMessage());
                        });
                    }
                }
            }
        }).start();
    }
}
    


