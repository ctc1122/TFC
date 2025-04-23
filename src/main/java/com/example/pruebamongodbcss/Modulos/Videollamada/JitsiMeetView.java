package com.example.pruebamongodbcss.Modulos.Videollamada;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;

public class JitsiMeetView extends StackPane {
    private final WebView webView;
    private final WebEngine webEngine;
    private static final String JITSI_MEET_URL = "https://meet.jit.si/";

    public JitsiMeetView(String roomName, String displayName) {
        webView = new WebView();
        webEngine = webView.getEngine();
        
        // Configurar el WebView
        webView.setPrefSize(800, 600);
        
        // Cargar la URL de Jitsi Meet con los parámetros
        String url = JITSI_MEET_URL + roomName + "#userInfo.displayName=\"" + displayName + "\"";
        webEngine.load(url);
        
        // Manejar errores de carga
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.FAILED) {
                System.err.println("Error al cargar la página: " + webEngine.getLoadWorker().getException());
            }
        });
        
        // Agregar el WebView al StackPane
        getChildren().add(webView);
    }

    public void dispose() {
        if (webEngine != null) {
            webEngine.load(null);
        }
        if (webView != null) {
            webView.getEngine().load(null);
        }
    }
} 