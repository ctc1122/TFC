package com.example.pruebamongodbcss.Modulos.AppChat;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class VentanaChat {
    @FXML private VBox contenedorMensajes;
    @FXML private TextField campoMensaje;
    @FXML private Button btnEnviar;
    @FXML private Button btnAdjuntar;
    @FXML private ListView<String> listaContactos;
    @FXML private Circle btnCerrar;
    @FXML private Circle btnMaximizar;
    @FXML private Circle btnMinimizar;
    @FXML private HBox barraTitulo;
    @FXML private ScrollPane scrollPane;
    @FXML private Label lblNoUsuarios;

    private double xOffset = 0;
    private double yOffset = 0;
    private ChatClient chatClient;
    private String usuarioSeleccionado;
    private String idUsuarioSeleccionado;
    private final ObservableList<String> contactos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarInterfaz();
        listaContactos.setItems(contactos);
        
        // Configurar el mensaje de no usuarios
        lblNoUsuarios = new Label("No hay usuarios conectados");
        lblNoUsuarios.getStyleClass().add("mensaje-sistema");
        lblNoUsuarios.setVisible(false);
        contenedorMensajes.getChildren().add(lblNoUsuarios);
        
        // Configurar el drag and drop
        configurarDragAndDrop();
        
        // Hacer visible el botón de adjuntar
        if (btnAdjuntar != null) {
            btnAdjuntar.setVisible(true);
            
            // Configurar icono para el botón adjuntar
            Region iconoAdjuntar = new Region();
            iconoAdjuntar.getStyleClass().add("icono-adjuntar");
            btnAdjuntar.setGraphic(iconoAdjuntar);
            btnAdjuntar.setTooltip(new Tooltip("Adjuntar archivo"));
        }
    }

    private void configurarInterfaz() {
        // Configurar envío de mensajes
        campoMensaje.setOnAction(e -> enviarMensaje());
        btnEnviar.setOnAction(e -> enviarMensaje());
        btnAdjuntar.setOnAction(e -> mostrarSelectorArchivos());

        // Configurar botones de ventana
        btnCerrar.setOnMouseClicked(e -> {
            if (chatClient != null) {
                chatClient.desconectar();
            }
            ((Stage) btnCerrar.getScene().getWindow()).close();
        });

        btnMaximizar.setOnMouseClicked(e -> {
            Stage stage = (Stage) btnMaximizar.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        });

        btnMinimizar.setOnMouseClicked(e -> {
            Stage stage = (Stage) btnMinimizar.getScene().getWindow();
            stage.setIconified(true);
        });

        // Configurar arrastre de ventana
        barraTitulo.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        barraTitulo.setOnMouseDragged(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Configurar selección de contactos
        listaContactos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String[] partes = newVal.split(" \\(ID: ");
                usuarioSeleccionado = partes[0];
                idUsuarioSeleccionado = partes[1].replace(")", "");
                mostrarMensajeSistema("Ahora estás chateando con: " + usuarioSeleccionado);
                campoMensaje.setDisable(false);
                btnEnviar.setDisable(false);
                btnAdjuntar.setDisable(false);
            }
        });

        // Deshabilitar inicialmente el campo de mensaje y botones
        campoMensaje.setDisable(true);
        btnEnviar.setDisable(true);
        btnAdjuntar.setDisable(true);
    }

    private void configurarDragAndDrop() {
        contenedorMensajes.setOnDragOver(event -> {
            if (event.getGestureSource() != contenedorMensajes && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                contenedorMensajes.getStyleClass().add("drag-over");
            }
            event.consume();
        });

        contenedorMensajes.setOnDragExited(event -> {
            contenedorMensajes.getStyleClass().remove("drag-over");
            event.consume();
        });

        contenedorMensajes.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasFiles() && usuarioSeleccionado != null) {
                File file = db.getFiles().get(0);
                enviarArchivo(file);
                success = true;
            } else if (usuarioSeleccionado == null) {
                mostrarMensajeSistema("Selecciona un usuario para enviar archivos");
            }
            
            event.setDropCompleted(success);
            contenedorMensajes.getStyleClass().remove("drag-over");
            event.consume();
        });
    }

    public void setUsuarioActual(String userId, String userName) {
        try {
            chatClient = new ChatClient(userId, userName);
            
            // Configurar manejadores de eventos
            chatClient.addMessageHandler(this::procesarMensajeRecibido);
            chatClient.addUserListHandler(usuarios -> {
                Platform.runLater(() -> {
                    contactos.clear();
                    for (ChatClient.Usuario usuario : usuarios) {
                        if (!usuario.id.equals(userId)) {
                            contactos.add(usuario.nombre + " (ID: " + usuario.id + ")");
                        }
                    }
                    
                    // Mostrar/ocultar mensaje de no usuarios
                    boolean hayUsuarios = !contactos.isEmpty();
                    lblNoUsuarios.setVisible(!hayUsuarios);
                    if (!hayUsuarios) {
                        campoMensaje.setDisable(true);
                        btnEnviar.setDisable(true);
                        btnAdjuntar.setDisable(true);
                    }
                });
            });

            // Conectar al servidor
            chatClient.conectar();
            mostrarMensajeSistema("Conectado al servidor de chat");

        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensajeSistema("Error al conectar al servidor: " + e.getMessage());
        }
    }

    private void enviarMensaje() {
        String mensaje = campoMensaje.getText().trim();
        if (!mensaje.isEmpty() && usuarioSeleccionado != null) {
            chatClient.enviarMensajePrivado(idUsuarioSeleccionado, usuarioSeleccionado, mensaje);
            mostrarMensajeEnviado(mensaje);
            campoMensaje.clear();
        }
    }

    private void procesarMensajeRecibido(ChatMessage mensaje) {
        Platform.runLater(() -> {
            switch (mensaje.getTipo()) {
                case MENSAJE_PRIVADO:
                    mostrarMensajeRecibido(mensaje.getRemitente() + ": " + mensaje.getContenido());
                    break;
                case ARCHIVO:
                    procesarArchivoRecibido(mensaje);
                    break;
            }
        });
    }

    private void mostrarMensajeSistema(String mensaje) {
        Platform.runLater(() -> {
            Label mensajeLabel = new Label(mensaje);
            mensajeLabel.getStyleClass().add("mensaje-sistema");
            mensajeLabel.setWrapText(true);
            mensajeLabel.setMaxWidth(350);

            HBox burbuja = new HBox(mensajeLabel);
            burbuja.setAlignment(Pos.CENTER);
            VBox.setMargin(burbuja, new Insets(5));

            contenedorMensajes.getChildren().add(burbuja);
            scrollPane.setVvalue(1.0);
        });
    }

    private void mostrarMensajeEnviado(String mensaje) {
        Label mensajeLabel = new Label("Tú: " + mensaje);
        mensajeLabel.getStyleClass().add("mensaje-propio");
        mensajeLabel.setWrapText(true);
        mensajeLabel.setMaxWidth(350);

        HBox burbuja = new HBox(mensajeLabel);
        burbuja.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(burbuja, new Insets(5));

        contenedorMensajes.getChildren().add(burbuja);
        scrollPane.setVvalue(1.0);
    }

    private void mostrarMensajeRecibido(String mensaje) {
        Label mensajeLabel = new Label(mensaje);
        mensajeLabel.getStyleClass().add("mensaje-ajeno");
        mensajeLabel.setWrapText(true);
        mensajeLabel.setMaxWidth(350);

        HBox burbuja = new HBox(mensajeLabel);
        burbuja.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(burbuja, new Insets(5));

        contenedorMensajes.getChildren().add(burbuja);
        scrollPane.setVvalue(1.0);
    }

    private void mostrarSelectorArchivos() {
        if (usuarioSeleccionado == null) {
            mostrarMensajeSistema("Selecciona un usuario para enviar archivos");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
        
        // Configurar directorio inicial a Descargas
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (downloadsDir.exists()) {
            fileChooser.setInitialDirectory(downloadsDir);
        }
        
        // Configurar filtros de archivo
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*"),
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Documentos", "*.pdf", "*.doc", "*.docx", "*.txt")
        );

        Stage stage = (Stage) btnAdjuntar.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            enviarArchivo(selectedFile);
        }
    }

    private void enviarArchivo(File archivo) {
        if (!archivo.exists() || !archivo.isFile()) {
            mostrarMensajeSistema("Error: El archivo no existe o no es válido");
            return;
        }

        // Mostrar mensaje de envío
        mostrarMensajeSistema("Enviando archivo: " + archivo.getName());
        
        // Crear copia en la carpeta de archivos enviados
        String userHome = System.getProperty("user.home");
        File sentDir = new File(userHome, "ChatFiles/Sent");
        sentDir.mkdirs();
        
        try {
            // Copiar archivo a la carpeta de enviados
            File archivoCopiado = new File(sentDir, archivo.getName());
            java.nio.file.Files.copy(
                archivo.toPath(), 
                archivoCopiado.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
            // Enviar el archivo
            chatClient.enviarArchivo(idUsuarioSeleccionado, usuarioSeleccionado, archivo);
            
            // Crear el contenedor del mensaje
            HBox burbuja = new HBox(10); // 10px de espacio entre elementos
            burbuja.setAlignment(Pos.CENTER_RIGHT);
            VBox.setMargin(burbuja, new Insets(5));
            burbuja.getStyleClass().add("mensaje-archivo-propio");

            // Crear el contenedor para el texto
            VBox textoContainer = new VBox(2); // 2px de espacio vertical
            
            // Etiqueta "Archivo enviado"
            Label tipoLabel = new Label("Archivo enviado");
            tipoLabel.getStyleClass().add("mensaje-archivo-tipo");
            
            // Nombre del archivo
            Label nombreLabel = new Label(archivo.getName());
            nombreLabel.getStyleClass().add("mensaje-archivo-nombre");
            
            textoContainer.getChildren().addAll(tipoLabel, nombreLabel);

            // Crear botón de descarga con icono
            Button btnDescargar = crearBotonDescarga();
            btnDescargar.setOnAction(e -> abrirArchivo(archivoCopiado));

            // Añadir elementos a la burbuja
            burbuja.getChildren().addAll(textoContainer, btnDescargar);
            
            // Añadir la burbuja al chat
            contenedorMensajes.getChildren().add(burbuja);
            scrollPane.setVvalue(1.0);
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensajeSistema("Error al enviar el archivo: " + e.getMessage());
        }
    }

    private void procesarArchivoRecibido(ChatMessage mensaje) {
        String nombreArchivo = mensaje.getNombreArchivo();
        
        // Crear directorio para archivos recibidos
        String userHome = System.getProperty("user.home");
        File receivedDir = new File(userHome, "ChatFiles/Received");
        receivedDir.mkdirs();
        
        File archivoDestino = new File(receivedDir, nombreArchivo);
        
        try {
            // Guardar el archivo
            java.nio.file.Files.write(archivoDestino.toPath(), mensaje.getArchivoData());
            
            // Mostrar mensaje con el archivo
            Platform.runLater(() -> {
                // Crear el contenedor del mensaje
                HBox burbuja = new HBox(10); // 10px de espacio entre elementos
                burbuja.setAlignment(Pos.CENTER_LEFT);
                VBox.setMargin(burbuja, new Insets(5));
                burbuja.getStyleClass().add("mensaje-archivo-ajeno");

                // Crear el contenedor para el texto
                VBox textoContainer = new VBox(2); // 2px de espacio vertical
                
                // Remitente
                Label remitenteLabel = new Label(mensaje.getRemitente());
                remitenteLabel.getStyleClass().add("mensaje-archivo-remitente");
                
                // Nombre del archivo
                Label nombreLabel = new Label(nombreArchivo);
                nombreLabel.getStyleClass().add("mensaje-archivo-nombre");
                
                textoContainer.getChildren().addAll(remitenteLabel, nombreLabel);

                // Crear botón de descarga con icono
                Button btnDescargar = crearBotonDescarga();
                btnDescargar.setOnAction(e -> abrirArchivo(archivoDestino));

                // Añadir elementos a la burbuja
                burbuja.getChildren().addAll(textoContainer, btnDescargar);
                
                // Añadir la burbuja al chat
                contenedorMensajes.getChildren().add(burbuja);
                scrollPane.setVvalue(1.0);
                
                // Notificación de sistema
                mostrarMensajeSistema("Archivo guardado en: " + archivoDestino.getAbsolutePath());
            });
        } catch (IOException e) {
            Platform.runLater(() -> 
                mostrarMensajeSistema("Error al guardar el archivo: " + e.getMessage())
            );
        }
    }

    private Button crearBotonDescarga() {
        Button btnDescargar = new Button();
        btnDescargar.getStyleClass().add("boton-descarga");
        
        // Crear el icono de flecha hacia abajo
        Region iconoFlecha = new Region();
        iconoFlecha.getStyleClass().add("icono-flecha-descarga");
        
        // Configurar el botón
        btnDescargar.setGraphic(iconoFlecha);
        btnDescargar.setTooltip(new Tooltip("Descargar archivo"));
        
        return btnDescargar;
    }

    private void abrirArchivo(File archivo) {
        try {
            java.awt.Desktop.getDesktop().open(archivo);
        } catch (IOException e) {
            mostrarMensajeSistema("Error al abrir el archivo: " + e.getMessage());
        }
    }

    public void habilitarRedimension(Stage stage, Scene scene) {
        final int borde = 8;
        scene.setOnMouseMoved(event -> {
            if (event.getSceneX() >= scene.getWidth() - borde && event.getSceneY() >= scene.getHeight() - borde) {
                scene.setCursor(javafx.scene.Cursor.SE_RESIZE);
            } else {
                scene.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        scene.setOnMouseDragged(event -> {
            if (scene.getCursor() == javafx.scene.Cursor.SE_RESIZE) {
                stage.setWidth(event.getSceneX());
                stage.setHeight(event.getSceneY());
            }
        });
    }
}
