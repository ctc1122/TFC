package com.example.pruebamongodbcss;

import com.example.pruebamongodbcss.Servidor.Servidor;

public class IniciarServidor {
    public static void main(String[] args) {
        // Comprobar si ya hay un servidor corriendo en Docker
        System.out.println("Iniciando servidor en puerto alternativo para evitar conflictos con Docker...");
        
        try {
            // Crear una instancia del servidor con puerto alternativo
            Servidor servidor = new Servidor(50002); // Puerto alternativo
            System.out.println("Servidor creado correctamente. Iniciando...");
            servidor.iniciar();
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 