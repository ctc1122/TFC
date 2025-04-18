package com.example.pruebamongodbcss.Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;

public class Servidor {
    private static final int PUERTO_DEFAULT = 50002;
    private final int puerto;
    private ServerSocket serverSocket;
    private final ExecutorService pool;
    private final MongoDatabase database;
    private boolean running;

    /**
     * Constructor por defecto que usa el puerto 50000
     */
    public Servidor() {
        this(PUERTO_DEFAULT);
    }
    
    /**
     * Constructor que permite especificar un puerto personalizado
     * @param puerto Puerto en el que se iniciará el servidor
     */
    public Servidor(int puerto) {
        this.puerto = puerto;
        this.pool = Executors.newCachedThreadPool();
        // Intentar conectar a MongoDB local (Docker) primero
        MongoDatabase localDB = GestorConexion.conectarBD();
        
        // Si la conexión local falla, intentar con la conexión remota
        if (localDB == null) {
            System.out.println("Conexión a MongoDB local fallida, intentando conexión remota...");
            MongoDatabase remoteDB = GestorConexion.conectarBD();
            if (remoteDB == null) {
                System.err.println("Error: No se pudo conectar a ninguna base de datos MongoDB.");
                throw new RuntimeException("No se pudo establecer conexión con MongoDB");
            }
            this.database = remoteDB;
            System.out.println("Conexión a MongoDB remota establecida exitosamente.");
        } else {
            this.database = localDB;
            System.out.println("Conexión a MongoDB local establecida exitosamente.");
        }
        
        this.running = true;
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en puerto " + puerto);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress());
                ClienteHandler handler = new ClienteHandler(clientSocket, database);
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
                serverSocket.close();
            }
            pool.shutdown();
        } catch (IOException e) {
            System.err.println("Error al detener el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Servidor servidor = new Servidor();
            servidor.iniciar();
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
} 