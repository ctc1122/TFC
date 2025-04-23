package com.example.pruebamongodbcss.Modulos.Videollamada;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.UUID;

public class VideollamadaController {
    @FXML
    private Button iniciarButton;
    @FXML
    private TextField nombreTextField;
    @FXML
    private Label estadoLabel;
    
    private JitsiMeetView jitsiView;
    private Stage videollamadaStage;

    @FXML
    public void initialize() {
        iniciarButton.setOnAction(event -> iniciarVideollamada());
        nombreTextField.setPromptText("Ingrese su nombre para la videollamada");
    }

    private void iniciarVideollamada() {
        try {
            String displayName = nombreTextField.getText();
            if (displayName.trim().isEmpty()) {
                displayName = "Usuario" + System.currentTimeMillis() % 1000;
            }

            // Generar un ID único para la sala
            String roomName = "MedConnect_" + UUID.randomUUID().toString().substring(0, 8);

            // Crear la vista de Jitsi
            jitsiView = new JitsiMeetView(roomName, displayName);

            // Crear una nueva ventana para la videollamada
            if (videollamadaStage == null) {
                videollamadaStage = new Stage();
                videollamadaStage.setTitle("MedConnect - Videollamada");
                videollamadaStage.setOnCloseRequest(e -> {
                    if (jitsiView != null) {
                        jitsiView.dispose();
                    }
                });
            }

            Scene scene = new Scene(jitsiView, 800, 600);
            videollamadaStage.setScene(scene);
            videollamadaStage.show();

            estadoLabel.setText("Videollamada iniciada - Sala: " + roomName);
        } catch (Exception e) {
            estadoLabel.setText("Error al iniciar la videollamada: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cerrarVideollamada() {
        if (jitsiView != null) {
            jitsiView.dispose();
        }
        if (videollamadaStage != null) {
            videollamadaStage.close();
        }
    }
} 