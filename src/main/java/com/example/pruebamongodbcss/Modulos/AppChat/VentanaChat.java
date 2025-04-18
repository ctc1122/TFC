package com.example.pruebamongodbcss.Modulos.AppChat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class VentanaChat {

    @FXML
    private VBox contenedorMensajes;

    @FXML
    private TextField campoMensaje;

    @FXML
    private Button btnAdjuntar;


    //-----------
    //BLOQUE DE LLAMADA
    //-----------

    @FXML
    private Button btnLlamar;

    @FXML
    private ScrollPane scrollPane;

    private AudioSenderUDP emisor;
    private AudioReceiverUDP receptor;
    private boolean enLlamada = false;

    @FXML
    private ListView<String> listaContactos;


    @FXML private Button btnCerrar;
    @FXML private Button btnMaximizar;
    @FXML private HBox barraTitulo;

    private double xOffset = 0;
    private double yOffset = 0;


    @FXML
    public void initialize() {
        // Drag & Drop de archivos
        contenedorMensajes.setOnDragOver(event -> {
            if (event.getGestureSource() != contenedorMensajes && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        contenedorMensajes.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                for (File archivo : db.getFiles()) {
                    mostrarArchivoEnviado(archivo);
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

        campoMensaje.setOnAction(e -> enviarMensaje());

        btnAdjuntar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File archivo = fileChooser.showOpenDialog(null);
            if (archivo != null) {
                mostrarArchivoEnviado(archivo);
            }
        });


        //VENTANA FUNCIONALIDAD
        // Acción de cerrar
        btnCerrar.setOnAction(e -> ((Stage) btnCerrar.getScene().getWindow()).close());

        btnMaximizar.setOnAction(e -> {
            Stage stage = (Stage) btnMaximizar.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        });


        // Arrastrar ventana
        barraTitulo.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        barraTitulo.setOnMouseDragged(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        btnLlamar.setOnAction(e -> toggleLlamada());

        //
        //PRUEBA DE LISTA CONTACTOS
        //
        listaContactos.getItems().addAll("Alice", "Bob", "Charlie");
        listaContactos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Label cambio = new Label("🔄 Cambiaste a chatear con: " + newVal);
                cambio.getStyleClass().add("mensaje-ajeno");
                contenedorMensajes.getChildren().add(cambio);

                // En el futuro: aquí cambiarías el destino del mensaje o socket
            }
        });


    }

    private void enviarMensaje() {
        String texto = campoMensaje.getText().trim();
        if (!texto.isEmpty()) {
            Label mensaje = new Label(texto);
            mensaje.getStyleClass().add("mensaje-propio");
            mensaje.setWrapText(true);
            mensaje.setMaxWidth(350); // Ajusta si quieres que el texto se vea más corto/largo

            HBox burbuja = new HBox(mensaje);
            burbuja.setAlignment(Pos.CENTER_RIGHT);
            VBox.setMargin(burbuja, new Insets(5));

            contenedorMensajes.getChildren().add(burbuja);
            campoMensaje.clear();
        }
    }

    private void mostrarArchivoEnviado(File archivo) {
        Label mensaje = new Label("📎 Tú adjuntaste: " + archivo.getName());
        mensaje.getStyleClass().add("mensaje-propio");
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(300);

        Button btnDescargar = new Button("⬇");
        btnDescargar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(archivo.getName());
            File destino = fileChooser.showSaveDialog(null);
            if (destino != null) {
                copiarArchivo(archivo, destino);
            }
        });

        HBox caja = new HBox(10, mensaje, btnDescargar);
        caja.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(caja, new Insets(5));

        contenedorMensajes.getChildren().add(caja);
    }

    public void mostrarArchivoRecibido(String nombreArchivo) {
        Label mensaje = new Label("📎 Archivo recibido: " + nombreArchivo);
        mensaje.getStyleClass().add("mensaje-ajeno");
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(300);

        Button btnDescargar = new Button("⬇");
        btnDescargar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(nombreArchivo);
            File destino = fileChooser.showSaveDialog(null);
            if (destino != null) {
                simularDescarga(nombreArchivo, destino);
            }
        });

        HBox caja = new HBox(10, mensaje, btnDescargar);
        caja.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(caja, new Insets(5));

        contenedorMensajes.getChildren().add(caja);
    }

    private void simularDescarga(String nombreArchivo, File destino) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destino))) {
            writer.write("Contenido simulado de: " + nombreArchivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copiarArchivo(File origen, File destino) {
        try (InputStream in = new FileInputStream(origen);
             OutputStream out = new FileOutputStream(destino)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void habilitarRedimension(Stage stage, Scene scene) {
        final int borde = 8;

        scene.setOnMouseMoved(event -> {
            if (event.getSceneX() >= scene.getWidth() - borde && event.getSceneY() >= scene.getHeight() - borde) {
                scene.setCursor(Cursor.SE_RESIZE);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });

        scene.setOnMouseDragged(event -> {
            if (scene.getCursor() == Cursor.SE_RESIZE) {
                stage.setWidth(event.getSceneX());
                stage.setHeight(event.getSceneY());
            }
        });
    }


    private void toggleLlamada() {
        if (!enLlamada) {
            TextInputDialog dialog = new TextInputDialog("127.0.0.1");
            dialog.setTitle("Iniciar llamada");
            dialog.setHeaderText("Introduce la IP del destinatario:");
            dialog.setContentText("IP:");
            dialog.showAndWait().ifPresent(ip -> {
                receptor = new AudioReceiverUDP(50000);
                receptor.start();

                emisor = new AudioSenderUDP(ip, 50000);
                emisor.start();

                enLlamada = true;
                btnLlamar.setText("❌ Colgar");

                Label llamando = new Label("📡 Llamando a " + ip);
                llamando.getStyleClass().add("mensaje-propio");
                contenedorMensajes.getChildren().add(llamando);
            });
        } else {
            emisor.terminar();
            receptor.terminar();
            emisor = null;
            receptor = null;
            enLlamada = false;
            btnLlamar.setText("📞 Llamar");

            Label fin = new Label("📴 Llamada finalizada");
            fin.getStyleClass().add("mensaje-propio");
            contenedorMensajes.getChildren().add(fin);
        }
    }

    @FXML
    private void enviarMensajeBoton(ActionEvent event) {
        enviarMensaje();
    }
}
