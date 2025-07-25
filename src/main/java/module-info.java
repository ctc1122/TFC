module com.example.pruebamongodbcss {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires mongo.java.driver;
    requires com.jfoenix;
    requires java.desktop;
    requires javafx.graphics;
    requires java.net.http;
    requires org.json;
    requires jcef;
    requires MaterialFX;
    requires java.sql;
    requires org.apache.pdfbox;

    requires jdk.jsobject;

    
    // CalendarFX requirements
    requires com.calendarfx.view;
    
    // Google API requirements - COMPLETO
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.auth;
    requires com.google.api.services.calendar;
    requires google.api.client;
    
    // Fixed Gson dependency
    requires com.google.gson;
    requires transitive java.logging;
    requires itextpdf;
    
    // Apache POI para exportación a Excel
    requires org.apache.poi.poi;
    requires org.apache.xmlbeans;

    
    // Configuración básica del módulo principal
    exports com.example.pruebamongodbcss;
    opens com.example.pruebamongodbcss to javafx.fxml;
    
    // Exportación de submódulos específicos
    exports com.example.pruebamongodbcss.Modulos.AppChat;
    exports com.example.pruebamongodbcss.Modulos.Carrusel;
    exports com.example.pruebamongodbcss.Modulos.InicioSesion;
    exports com.example.pruebamongodbcss.Modulos.UMLSSearch;
    exports com.example.pruebamongodbcss.Modulos.Clinica;
    exports com.example.pruebamongodbcss.Modulos.Empresa;
    exports com.example.pruebamongodbcss.service;
    exports com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico;
    exports com.example.pruebamongodbcss.theme;
    exports com.example.pruebamongodbcss.Modulos.Clinica.Citas;
    exports com.example.pruebamongodbcss.Modulos.Facturacion;
    exports com.example.pruebamongodbcss.Modulos.Inventario;
    exports com.example.pruebamongodbcss.calendar;
    exports com.example.pruebamongodbcss.calendar.google;
    exports com.example.pruebamongodbcss.Modulos.Fichaje;
    exports com.example.pruebamongodbcss.Modulos.Informes;
    

    // Apertura de submódulos para FXML
    opens com.example.pruebamongodbcss.Modulos.AppChat to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.Carrusel to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.InicioSesion to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.UMLSSearch to javafx.fxml;

    opens com.example.pruebamongodbcss.Modulos.Clinica to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.Empresa to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico to javafx.fxml;
    opens com.example.pruebamongodbcss.theme to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.Clinica.Citas to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.Facturacion to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.Inventario to javafx.fxml;
    opens com.example.pruebamongodbcss.calendar to javafx.fxml, javafx.graphics, com.google.api.client, com.google.api.services.calendar, com.google.gson;
    opens com.example.pruebamongodbcss.calendar.google to javafx.fxml, javafx.graphics, com.google.api.client, com.google.api.services.calendar, com.google.gson;
    opens com.example.pruebamongodbcss.Modulos.Fichaje to javafx.fxml;
    opens com.example.pruebamongodbcss.Modulos.Informes to javafx.fxml;

    
    // IMPORTANTE: Cada vez que crees un nuevo paquete dentro de Modulos,
    // necesitarás añadir dos líneas como estas:
    // exports com.example.pruebamongodbcss.Modulos.TuNuevoPaquete;
    // opens com.example.pruebamongodbcss.Modulos.TuNuevoPaquete to javafx.fxml;
}