package com.example.pruebamongodbcss.Modulos.UMLSSearch;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.pruebamongodbcss.service.ULMSService;
import com.example.pruebamongodbcss.service.InventarioMedicoService;
import java.util.List;
import java.util.Optional;

public class UMLSSearchController {
    @FXML private TextField searchField;
    @FXML private Button validateButton;
    @FXML private ListView<String> suggestionsList;
    @FXML private Label validationLabel;
    @FXML private TextArea detailsTextArea;
    @FXML private Button useSuggestionButton;

    private final ULMSService umlsService;
    private final InventarioMedicoService inventarioService;
    private ObservableList<String> suggestions;

    public UMLSSearchController() {
        // API key de UMLS
        String apiKey = "9b36785d-ec95-4911-9f4e-28e4819178f1";
        this.umlsService = new ULMSService(apiKey);
        this.inventarioService = new InventarioMedicoService(apiKey);
        this.suggestions = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        suggestionsList.setItems(suggestions);
        validationLabel.setText("Ingrese un término para buscar en la base de datos UMLS");
        detailsTextArea.setText("");
        useSuggestionButton.setDisable(true);
    }

    @FXML
    private void handleValidate() {
        String term = searchField.getText().trim();
        if (term.length() < 3) {
            validationLabel.setText("✗ El término debe tener al menos 3 caracteres");
            suggestions.clear();
            detailsTextArea.setText("");
            return;
        }

        String result = inventarioService.validarNombreEquipo(term);
        if (result.startsWith("✓")) {
            validationLabel.setText(result);
            List<String> suggestionsList = inventarioService.obtenerSugerencias(term);
            suggestions.setAll(suggestionsList);
            if (!suggestionsList.isEmpty()) {
                useSuggestionButton.setDisable(false);
                showTermDetails(suggestionsList.get(0));
            }
        } else {
            validationLabel.setText(result);
            suggestions.clear();
            detailsTextArea.setText("");
            useSuggestionButton.setDisable(true);
        }
    }

    private void showTermDetails(String term) {
        Optional<String> ontology = inventarioService.obtenerOntologia(term);
        if (ontology.isPresent()) {
            detailsTextArea.setText("Fuente: " + ontology.get());
        } else {
            detailsTextArea.setText("No hay información adicional disponible");
        }
    }

    @FXML
    private void handleUseSuggestion() {
        String selectedTerm = suggestionsList.getSelectionModel().getSelectedItem();
        if (selectedTerm != null) {
            searchField.setText(selectedTerm);
            showTermDetails(selectedTerm);
        }
    }
} 