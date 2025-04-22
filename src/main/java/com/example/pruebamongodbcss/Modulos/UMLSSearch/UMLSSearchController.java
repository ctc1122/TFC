package com.example.pruebamongodbcss.Modulos.UMLSSearch;

import com.example.pruebamongodbcss.service.InventarioMedicoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.util.List;

public class UMLSSearchController {
    private final InventarioMedicoService inventarioService;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ListView<String> suggestionsList;
    
    @FXML
    private Label validationLabel;
    
    @FXML
    private Label ontologyLabel;
    
    @FXML
    private Button validateButton;
    
    @FXML
    private Button useSuggestionButton;
    
    public UMLSSearchController() {
        this.inventarioService = new InventarioMedicoService("ba9de3d5-083d-45e0-88d9-4deb353a28d4");
    }
    
    @FXML
    public void initialize() {
        // Configurar el ListView para mostrar sugerencias
        suggestionsList.setVisible(false);
        useSuggestionButton.setDisable(true);
        
        // Mensaje inicial en el validationLabel
        validationLabel.setText("Ingrese un término para buscar en la base de datos médica");
        validationLabel.setStyle("-fx-text-fill: #333333;");
        
        // Configurar el evento de búsqueda mientras se escribe
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.length() >= 3) {
                updateSuggestions(newValue);
            } else {
                suggestionsList.setVisible(false);
                useSuggestionButton.setDisable(true);
            }
        });
        
        // Configurar el evento de selección en la lista de sugerencias
        suggestionsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.startsWith("Error:") && !newValue.startsWith("No se encontraron")) {
                searchField.setText(newValue);
                validateTerm(newValue);
                useSuggestionButton.setDisable(false);
            } else {
                useSuggestionButton.setDisable(true);
            }
        });
    }
    
    private void updateSuggestions(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            suggestionsList.setVisible(false);
            return;
        }
        
        // Ejecutar la búsqueda de sugerencias en un hilo separado
        Thread suggestionsThread = new Thread(() -> {
            try {
                List<String> sugerencias = inventarioService.obtenerSugerencias(searchTerm);
                
                // Actualizar la UI en el hilo de JavaFX
                javafx.application.Platform.runLater(() -> {
                    ObservableList<String> items = FXCollections.observableArrayList(sugerencias);
                    suggestionsList.setItems(items);
                    suggestionsList.setVisible(!sugerencias.isEmpty());
                });
            } catch (Exception e) {
                // Manejar cualquier error
                javafx.application.Platform.runLater(() -> {
                    suggestionsList.setVisible(false);
                });
            }
        });
        
        suggestionsThread.setDaemon(true);
        suggestionsThread.start();
    }
    
    @FXML
    private void handleValidate(ActionEvent event) {
        String termino = searchField.getText();
        if (termino == null || termino.trim().isEmpty()) {
            validationLabel.setText("✗ Por favor, ingrese un término para buscar");
            validationLabel.setStyle("-fx-text-fill: red;");
            ontologyLabel.setVisible(false);
            return;
        }
        validateTerm(termino);
    }
    
    private void validateTerm(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            validationLabel.setText("✗ El término no puede estar vacío");
            validationLabel.setStyle("-fx-text-fill: red;");
            ontologyLabel.setVisible(false);
            return;
        }
        
        validationLabel.setText("Buscando...");
        validationLabel.setStyle("-fx-text-fill: blue;");
        
        // Ejecutar la validación en un hilo separado para no bloquear la UI
        Thread searchThread = new Thread(() -> {
            try {
                boolean esValido = inventarioService.esTerminoValido(termino);
                
                // Actualizar la UI en el hilo de JavaFX
                javafx.application.Platform.runLater(() -> {
                    validationLabel.setText(esValido ? "✓ Término válido encontrado en base de datos médica" : "✗ No se encontraron coincidencias en la base de datos");
                    validationLabel.setStyle(esValido ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                    
                    // Actualizar la ontología si el término es válido
                    if (esValido) {
                        inventarioService.obtenerOntologia(termino).ifPresent(ontologia -> {
                            ontologyLabel.setText("Ontología: " + ontologia);
                            ontologyLabel.setVisible(true);
                        });
                    } else {
                        ontologyLabel.setVisible(false);
                    }
                });
            } catch (Exception e) {
                // Manejar cualquier error y mostrar en la UI
                javafx.application.Platform.runLater(() -> {
                    validationLabel.setText("✗ Error: " + e.getMessage());
                    validationLabel.setStyle("-fx-text-fill: red;");
                    ontologyLabel.setVisible(false);
                });
            }
        });
        
        searchThread.setDaemon(true);
        searchThread.start();
    }
    
    @FXML
    private void handleUseSuggestion(ActionEvent event) {
        String selectedSuggestion = suggestionsList.getSelectionModel().getSelectedItem();
        if (selectedSuggestion != null && !selectedSuggestion.startsWith("Error:") && !selectedSuggestion.startsWith("No se encontraron")) {
            searchField.setText(selectedSuggestion);
            validateTerm(selectedSuggestion);
        }
    }
    
    public String getSelectedTerm() {
        return searchField.getText();
    }
} 