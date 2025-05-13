package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Controlador para una celda del calendario
 */
public class CeldaCalendarioController implements Initializable {

    @FXML private VBox container;
    @FXML private Label lblDia;
    @FXML private VBox citasContainer;
    
    private int dia;
    private LocalDate fecha;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización básica
        container.getStyleClass().add("celda-calendario");
        lblDia.getStyleClass().add("numero-dia");
        citasContainer.getStyleClass().add("citas-container");
    }
    
    /**
     * Configura la celda con una fecha, lista de citas y callback
     * 
     * @param fecha Fecha que representa esta celda
     * @param citas Lista de citas para este día
     * @param citaCallback Callback para manejar clic en una cita
     */
    public void configurar(LocalDate fecha, List<ModeloCita> citas, Consumer<ModeloCita> citaCallback) {
        this.fecha = fecha;
        this.dia = fecha.getDayOfMonth();
        
        // Establecer el número del día
        lblDia.setText(String.valueOf(dia));
        
        // Limpiar citas anteriores
        limpiarCitas();
        
        // Si es hoy, destacar la celda
        LocalDate hoy = LocalDate.now();
        if (fecha.equals(hoy)) {
            container.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 2px;");
            lblDia.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196f3;");
        } else {
            container.setStyle("-fx-background-color: white; -fx-border-color: #eee; -fx-border-width: 1px;");
            lblDia.setStyle("");
            
            // Si es fin de semana, cambiar color
            int diaSemana = fecha.getDayOfWeek().getValue();
            if (diaSemana == 6 || diaSemana == 7) { // Sábado o Domingo
                container.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #eee; -fx-border-width: 1px;");
            }
        }
        
        // Si no es del mes actual, mostrar en gris
        if (fecha.getMonth() != LocalDate.now().getMonth()) {
            lblDia.setStyle(lblDia.getStyle() + "; -fx-text-fill: #9e9e9e;");
            container.setStyle(container.getStyle() + "; -fx-opacity: 0.7;");
        }
        
        // Agregar citas
        if (citas != null && !citas.isEmpty()) {
            for (ModeloCita cita : citas) {
                try {
                    // Cargar la vista de item de cita
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/cita-item.fxml"));
                    Parent root = loader.load();
                    
                    // Agregar clase CSS según estado
                    if (cita.getEstado() != null) {
                        root.getStyleClass().add(cita.getEstadoAsString().toLowerCase().replace(" ", "-"));
                    }
                    
                    // Configurar el controlador
                    CitaItemController controller = loader.getController();
                    controller.setCita(cita);
                    
                    // Configurar evento de clic
                    root.setOnMouseClicked(event -> {
                        if (citaCallback != null) {
                            citaCallback.accept(cita);
                        }
                        event.consume();
                    });
                    
                    // Agregar a la lista de citas
                    citasContainer.getChildren().add(root);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Error al agregar cita a celda: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Establece el día representado por esta celda
     */
    public void setDia(int dia) {
        this.dia = dia;
        lblDia.setText(String.valueOf(dia));
        
        // Si es hoy, destacar la celda
        LocalDate hoy = LocalDate.now();
        if (fecha != null && fecha.equals(hoy)) {
            container.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 2px;");
            lblDia.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196f3;");
        }
    }
    
    /**
     * Establece la fecha completa representada por esta celda
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
        
        // Si es hoy, destacar la celda
        LocalDate hoy = LocalDate.now();
        if (fecha.equals(hoy)) {
            container.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 2px;");
            lblDia.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196f3;");
        }
        
        // Si es fin de semana, cambiar color
        int diaSemana = fecha.getDayOfWeek().getValue();
        if (diaSemana == 6 || diaSemana == 7) { // Sábado o Domingo
            container.setStyle(container.getStyle() + "; -fx-background-color: #f5f5f5;");
        }
    }
    
    /**
     * Agrega una cita a la celda
     */
    public void agregarCita(ModeloCita cita) {
        try {
            // Cargar la vista de item de cita
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/cita-item.fxml"));
            Parent root = loader.load();
            
            // Configurar el controlador
            CitaItemController controller = loader.getController();
            controller.setCita(cita);
            
            // Agregar a la lista de citas
            citasContainer.getChildren().add(root);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al agregar cita a celda: " + e.getMessage());
        }
    }
    
    /**
     * Limpia todas las citas de la celda
     */
    public void limpiarCitas() {
        citasContainer.getChildren().clear();
    }
} 