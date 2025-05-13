package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/cita-item.fxml"));
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