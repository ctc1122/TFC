package com.example.pruebamongodbcss.calendar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Aplicación para probar el componente de calendario
 */
public class CalendarMain extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Crear el componente de calendario
            GoogleCalendarWebView calendarView = new GoogleCalendarWebView();
            
            // Crear el contenedor principal
            BorderPane root = new BorderPane();
            root.setCenter(calendarView);
            
            // Configurar la escena
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com/example/pruebamongodbcss/styles/calendar-component.css").toExternalForm());
            
            // Configurar la ventana
            stage.setTitle("Calendario de Citas");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        // Cerrar conexiones a bases de datos
        try {
            Utilidades.GestorConexion.cerrarConexion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 