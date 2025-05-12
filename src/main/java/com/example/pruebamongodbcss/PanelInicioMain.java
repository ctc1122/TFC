package com.example.pruebamongodbcss;

import java.io.IOException;

import com.example.pruebamongodbcss.theme.ThemeUtil;
import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PanelInicioMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Crear y configurar el FXMLLoader para cargar el panel de inicio
        FXMLLoader fxmlLoader = new FXMLLoader(PanelInicioMain.class.getResource("/com/example/pruebamongodbcss/panelInicio.fxml"));
        
        // Cargar el FXML y crear una escena con el tema aplicado
        Scene scene = ThemeUtil.createScene(fxmlLoader.load(), 700, 450);
        
        // Agregar CSS base de la aplicación
        // Nota: esto no es necesario ya que el ThemeManager se encargará de aplicar los CSS adecuados
        // pero lo mantenemos por compatibilidad
        scene.getStylesheets().add(getClass().getResource("/com/example/pruebamongodbcss/app.css").toExternalForm());
        
        // Asignar la escena al stage principal
        stage.setTitle("ChichaVet - Clínica Veterinaria");
        stage.setScene(scene);
        
        // Cierre seguro de la aplicación
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        
        // Mostrar la ventana principal
        stage.show();
        
        // Asegurar que el tema se aplique a todas las ventanas abiertas
        Platform.runLater(ThemeUtil::applyThemeToAllOpenWindows);
    }

    public static void main(String[] args) {
        launch();
        MongoDatabase baseMongo=GestorConexion.conectarEmpresa();
        try{
            baseMongo.createCollection("Prueba");
            System.out.println("Colección creada exitosamente.");
        }catch (Exception ex){
            System.out.println("Error");
        }
    }
}