package com.example.pruebamongodbcss;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.example.pruebamongodbcss.Protocolo.Protocolo;

public class TestConexion {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 50002; // Puerto del servidor personalizado
    
    public static void main(String[] args) {
        try {
            // Conectar al servidor
            System.out.println("Intentando conectar al servidor: " + SERVER_HOST + ":" + SERVER_PORT);
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Conexión establecida.");
            
            // Preparar streams
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            
            // Enviar petición de login con usuario admin/admin
            System.out.println("Enviando petición de login...");
            salida.writeInt(Protocolo.LOGIN_REQUEST);
            salida.writeUTF("admin");
            salida.writeUTF("admin");
            salida.flush();
            
            // Recibir respuesta
            int tipoRespuesta = entrada.readInt();
            if (tipoRespuesta == Protocolo.LOGIN_RESPONSE) {
                int codigoRespuesta = entrada.readInt();
                switch (codigoRespuesta) {
                    case Protocolo.LOGIN_SUCCESS:
                        System.out.println("Inicio de sesión exitoso!");
                        break;
                    case Protocolo.LOGIN_FAILED:
                        System.out.println("Usuario o contraseña incorrectos!");
                        break;
                    default:
                        System.out.println("Respuesta desconocida: " + codigoRespuesta);
                }
            } else {
                System.out.println("Respuesta inesperada del servidor: " + tipoRespuesta);
            }
            
            // Cerrar conexión
            entrada.close();
            salida.close();
            socket.close();
            
        } catch (IOException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 