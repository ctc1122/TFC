package com.example.pruebamongodbcss.Modulos.Videollamada;

import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class JitsiMeetView {
    private static final String JITSI_MEET_URL = "https://meet.jit.si/";
    private String currentUrl;

    public JitsiMeetView(String roomName, String displayName) throws IOException, URISyntaxException {
        // Codificar los parámetros de manera segura
        String encodedDisplayName = URLEncoder.encode(displayName, StandardCharsets.UTF_8);
        String encodedRoomName = URLEncoder.encode(roomName, StandardCharsets.UTF_8);
        
        // Crear la URL de Jitsi Meet con los parámetros codificados
        currentUrl = JITSI_MEET_URL + encodedRoomName + "#config.displayName='" + encodedDisplayName + "'";
        
        // Abrir en el navegador predeterminado
        Desktop.getDesktop().browse(new URI(currentUrl));
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void dispose() {
        // No es necesario hacer nada aquí ya que el navegador se maneja independientemente
    }
} 