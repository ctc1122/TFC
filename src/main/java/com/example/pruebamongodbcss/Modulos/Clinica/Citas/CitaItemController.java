package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Controlador para un ítem de cita en el calendario
 */
public class CitaItemController implements Initializable {

    @FXML private HBox container;
    @FXML private Circle circuloEstado;
    @FXML private Label lblHora;
    @FXML private Label lblPaciente;
    
    private ModeloCita cita;
    private DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización básica
    }
    
    /**
     * Establece la cita a representar
     */
    public void setCita(ModeloCita cita) {
        this.cita = cita;
        
        // Configurar etiquetas
        lblHora.setText(cita.getFechaHora().format(formatoHora));
        lblPaciente.setText(cita.getNombrePaciente());
        
        // Configurar tooltip con información detallada
        Tooltip tooltip = new Tooltip(
            "Paciente: " + cita.getNombrePaciente() + "\n" +
            "Animal: " + cita.getTipoAnimal() + " - " + cita.getRazaAnimal() + "\n" +
            "Veterinario: " + cita.getNombreVeterinario() + "\n" +
            "Motivo: " + cita.getMotivo() + "\n" +
            "Estado: " + cita.getEstadoAsString()
        );
        Tooltip.install(container, tooltip);
        
        // Establecer color según estado
        configurarEstilo();
    }
    
    /**
     * Configura el estilo visual según el estado de la cita
     */
    private void configurarEstilo() {
        if (cita != null) {
            switch (cita.getEstado()) {
                case PENDIENTE:
                    circuloEstado.setFill(Color.ORANGE);
                    container.setStyle("-fx-background-color: #fff9c4;"); // Amarillo claro
                    break;
                case EN_CURSO:
                    circuloEstado.setFill(Color.BLUE);
                    container.setStyle("-fx-background-color: #bbdefb;"); // Azul claro
                    break;
                case COMPLETADA:
                    circuloEstado.setFill(Color.GREEN);
                    container.setStyle("-fx-background-color: #c8e6c9;"); // Verde claro
                    break;
                case CANCELADA:
                    circuloEstado.setFill(Color.RED);
                    container.setStyle("-fx-background-color: #ffcdd2;"); // Rojo claro
                    break;
                case REPROGRAMADA:
                    circuloEstado.setFill(Color.PURPLE);
                    container.setStyle("-fx-background-color: #e1bee7;"); // Morado claro
                    break;
                default:
                    circuloEstado.setFill(Color.GRAY);
                    container.setStyle("-fx-background-color: #e0e0e0;"); // Gris claro
            }
        }
    }
} 