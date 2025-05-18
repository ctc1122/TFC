package com.example.pruebamongodbcss.calendar;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.theme.ThemeManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Pantalla principal para mostrar el calendario
 */
public class CalendarScreen extends BorderPane {

    private CalendarFXComponent calendarComponent;
    private Usuario usuarioActual;
    
    /**
     * Constructor
     */
    public CalendarScreen() {
        this(null);
    }
    
    /**
     * Constructor que recibe el usuario actual
     * @param usuario El usuario actual logueado
     */
    public CalendarScreen(Usuario usuario) {
        this.usuarioActual = usuario;
        initialize();
    }
    
    /**
     * Inicializa la pantalla
     */
    private void initialize() {
        // Aplicar estilo y tema
        getStyleClass().add("calendar-screen");
        
        if (ThemeManager.getInstance().isDarkTheme()) {
            getStyleClass().add("dark-theme");
        } else {
            getStyleClass().add("light-theme");
        }
        
        // Crear cabecera
        Label titleLabel = new Label("Calendario de Citas");
        titleLabel.getStyleClass().add("screen-title");
        
        Button backButton = new Button("Volver");
        backButton.getStyleClass().add("back-button");
        // backButton.setOnAction(e -> onBackAction());
        
        HBox headerBox = new HBox(15);
        headerBox.setPadding(new Insets(10, 15, 10, 15));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(backButton, titleLabel);
        
        // Crear componente de calendario nativo
        calendarComponent = new CalendarFXComponent();
        
        // Establecer el usuario actual
        if (usuarioActual != null) {
            calendarComponent.setUsuarioActual(usuarioActual);
        }
        
        // Crear área de información
        String infoText = "Calendario de Citas";
        if (usuarioActual != null) {
            infoText += " - Usuario: " + usuarioActual.getUsuario();
        } else {
            infoText += " - ATENCIÓN: No hay usuario logueado";
        }
        Label infoLabel = new Label(infoText);
        infoLabel.getStyleClass().add("info-label");
        
        Label instructionsLabel = new Label("Utiliza esta interfaz para gestionar tus citas. " +
                "Puedes crear, editar y eliminar eventos directamente en el calendario.");
        instructionsLabel.getStyleClass().add("instructions-label");
        instructionsLabel.setWrapText(true);
        
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(10, 15, 10, 15));
        infoBox.getChildren().addAll(infoLabel, instructionsLabel);
        
        // Configurar layout
        setTop(headerBox);
        setCenter(calendarComponent);
        setBottom(infoBox);
    }
    
    /**
     * Establece el usuario actual
     * @param usuario El usuario actual logueado
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        // Actualizar el componente del calendario con el nuevo usuario
        if (calendarComponent != null) {
            calendarComponent.setUsuarioActual(usuario);
        }
    }
    
    /**
     * Libera recursos al cerrar
     */
    public void dispose() {
        // No se requiere liberar recursos adicionales con CalendarFXComponent
    }
} 