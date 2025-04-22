module com.example.pruebamongodbcss {
    requires javafx.fxml;
    requires javafx.controls;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires mongo.java.driver;
    requires com.jfoenix;
    requires java.desktop;
    requires javafx.graphics;
    requires java.net.http;
    requires org.json;

    // Configuración básica del módulo principal
    exports com.example.pruebamongodbcss;
    opens com.example.pruebamongodbcss to javafx.fxml;
    
    // Exportación de submódulos específicos
    exports com.example.pruebamongodbcss.Modulos.AppChat;
    exports com.example.pruebamongodbcss.Modulos.Carrusel;
    exports com.example.pruebamongodbcss.Modulos.InicioSesion;
    exports com.example.pruebamongodbcss.Modulos.UMLSSearch;
    exports com.example.pruebamongodbcss.service;

    
    // Apertura de submódulos para FXML
    opens com.example.pruebamongodbcss.Modulos.AppChat to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.Carrusel to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.InicioSesion to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.UMLSSearch to javafx.fxml;
 
    
    // IMPORTANTE: Cada vez que crees un nuevo paquete dentro de Modulos,
    // necesitarás añadir dos líneas como estas:
    // exports com.example.pruebamongodbcss.Modulos.TuNuevoPaquete;
    // opens com.example.pruebamongodbcss.Modulos.TuNuevoPaquete to javafx.fxml;
}