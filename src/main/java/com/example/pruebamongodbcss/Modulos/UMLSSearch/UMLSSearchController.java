package com.example.pruebamongodbcss.Modulos.UMLSSearch;

import com.example.pruebamongodbcss.service.ULMSService;
import com.example.pruebamongodbcss.service.ULMSService.MedicalTerm;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class UMLSSearchController {
    @FXML
    private TextField searchField;
    
    @FXML
    private ListView<MedicalTerm> suggestionsList;
    
    @FXML
    private TextArea detailsArea;
    
    private final ULMSService umlsService;
    private final ObservableList<MedicalTerm> suggestions;
    
    public UMLSSearchController() {
        // API key de UMLS
        String apiKey = "9b36785d-ec95-4911-9f4e-28e4819178f1";
        this.umlsService = new ULMSService(apiKey);
        this.suggestions = FXCollections.observableArrayList();
    }
    
    @FXML
    public void initialize() {
        // Configurar la ListView
        suggestionsList.setItems(suggestions);
        
        // Personalizar cómo se muestran los items en la lista
        suggestionsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(MedicalTerm item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        
        // Añadir listener para cuando se selecciona un término
        suggestionsList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    showTermDetails(newValue);
                }
            }
        );
        
        // Añadir listener para búsqueda mientras se escribe
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty() && newValue.length() >= 3) {
                searchTerm(newValue);
            } else {
                suggestions.clear();
            }
        });
    }
    
    private void searchTerm(String term) {
        // Limpiar sugerencias anteriores
        suggestions.clear();
        
        // Buscar nuevas sugerencias
        List<MedicalTerm> searchResults = umlsService.searchTermSuggestions(term);
        
        // Actualizar la lista de sugerencias
        suggestions.addAll(searchResults);
        
        if (suggestions.isEmpty()) {
            detailsArea.setText("No se encontraron resultados para \"" + term + "\"");
        }
    }
    
    private void showTermDetails(MedicalTerm term) {
        StringBuilder details = new StringBuilder();
        details.append("Nombre: ").append(term.getName()).append("\n");
        details.append("Código: ").append(term.getCode()).append("\n");
        details.append("Fuente: ").append(term.getSource()).append("\n");
        details.append("URI: ").append(term.getUri()).append("\n");
        
        detailsArea.setText(details.toString());
    }
} 