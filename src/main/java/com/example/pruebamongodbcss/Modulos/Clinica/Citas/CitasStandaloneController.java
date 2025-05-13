package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

/**
 * Controlador para la vista standalone de citas
 */
public class CitasStandaloneController implements Initializable {

    @FXML
    private BorderPane mainPane;
    
    @FXML
    private BorderPane contentPane;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // La vista de citas se carga automáticamente mediante la inclusión fx:include
        // No necesitamos hacer nada más, el controlador de citas se encarga de todo
    }
} 