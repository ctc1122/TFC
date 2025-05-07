package com.example.pruebamongodbcss.Servidor2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;

public class ServidorGestionInventario {
    private static final int PUERTO_DEFAULT = 50005;
    private final int puerto;
    private ServerSocket serverSocket;
    private final ExecutorService pool;
    private final MongoDatabase database;
    private boolean running;

    /**
     * Constructor por defecto que usa el puerto 50000
     */
    public ServidorGestionInventario() {
        this(PUERTO_DEFAULT);
    }
    

    public ServidorGestionInventario(int puerto) {
        this.puerto = puerto;
        this.pool = Executors.newCachedThreadPool();
        
        this.running = true;
    }

    /**
     * Método para iniciar el servidor
     * @return void
     */
    public void iniciar() {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en puerto " + puerto);

            while (running) {
                // Esperar a que un cliente se conecte
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress());
                // Crear un nuevo hilo para manejar la conexión del cliente
                ClientHandler handler = new ClientHandler(clientSocket, database);
                // Ejecutar el hilo en el pool de hilos
                pool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        } finally {
            detener();
        }
    }

    public void detener() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                // Cerrar el socket del servidor
                serverSocket.close();
            }
            // Esperar a que todos los hilos se completen
            pool.shutdown();
        } catch (IOException e) {
            System.err.println("Error al detener el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            ServidorGestionInventario servidor = new ServidorGestionInventario();
            servidor.iniciar();
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
} 