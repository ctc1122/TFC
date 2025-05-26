package com.example.pruebamongodbcss.Utilidades;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Gestor de conexión con el servidor de inventario
 * Maneja la comunicación TCP con el servidor en puerto 50005
 */
public class GestorConexionInventario {
    
    // Configuración del servidor de inventario
    private static final String SERVIDOR_HOST = "localhost";
    private static final int SERVIDOR_PUERTO = 50005;
    private static final int TIMEOUT_CONEXION = 5000; // 5 segundos
    private static final int TIMEOUT_LECTURA = 10000; // 10 segundos
    
    // Instancia singleton
    private static GestorConexionInventario instancia;
    
    // Estado de conexión
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private boolean conectado;
    private String ultimoError;
    
    private GestorConexionInventario() {
        this.conectado = false;
    }
    
    /**
     * Obtiene la instancia singleton del gestor
     */
    public static synchronized GestorConexionInventario getInstance() {
        if (instancia == null) {
            instancia = new GestorConexionInventario();
        }
        return instancia;
    }
    
    /**
     * Establece conexión con el servidor de inventario
     */
    public CompletableFuture<Boolean> conectar() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (conectado && socket != null && !socket.isClosed()) {
                    return true; // Ya conectado
                }
                
                System.out.println("Conectando al servidor de inventario en " + SERVIDOR_HOST + ":" + SERVIDOR_PUERTO);
                
                socket = new Socket(SERVIDOR_HOST, SERVIDOR_PUERTO);
                socket.setSoTimeout(TIMEOUT_LECTURA);
                socket.setKeepAlive(true);
                
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                salida = new PrintWriter(socket.getOutputStream(), true);
                
                conectado = true;
                ultimoError = null;
                
                System.out.println("Conexión establecida con el servidor de inventario");
                return true;
                
            } catch (IOException e) {
                ultimoError = "Error de conexión: " + e.getMessage();
                System.err.println("Error al conectar con servidor de inventario: " + e.getMessage());
                conectado = false;
                return false;
            }
        });
    }
    
    /**
     * Desconecta del servidor de inventario
     */
    public void desconectar() {
        try {
            conectado = false;
            
            if (entrada != null) {
                entrada.close();
                entrada = null;
            }
            
            if (salida != null) {
                salida.close();
                salida = null;
            }
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
            
            System.out.println("Desconectado del servidor de inventario");
            
        } catch (IOException e) {
            System.err.println("Error al desconectar del servidor de inventario: " + e.getMessage());
        }
    }
    
    /**
     * Envía un mensaje al servidor y espera respuesta
     */
    public CompletableFuture<String> enviarMensaje(String mensaje) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!conectado || socket == null || socket.isClosed()) {
                    // Intentar reconectar
                    if (!conectar().get(5, TimeUnit.SECONDS)) {
                        throw new RuntimeException("No se pudo establecer conexión con el servidor");
                    }
                }
                
                // Enviar mensaje
                salida.println(mensaje);
                salida.flush();
                
                // Leer respuesta
                String respuesta = entrada.readLine();
                if (respuesta == null) {
                    throw new RuntimeException("El servidor cerró la conexión");
                }
                
                return respuesta;
                
            } catch (SocketTimeoutException e) {
                ultimoError = "Timeout al comunicar con el servidor";
                throw new RuntimeException("Timeout al comunicar con el servidor de inventario");
            } catch (IOException e) {
                ultimoError = "Error de comunicación: " + e.getMessage();
                conectado = false;
                throw new RuntimeException("Error de comunicación con el servidor de inventario: " + e.getMessage());
            } catch (Exception e) {
                ultimoError = "Error inesperado: " + e.getMessage();
                throw new RuntimeException("Error inesperado: " + e.getMessage());
            }
        });
    }
    
    /**
     * Verifica si hay conexión activa con el servidor
     */
    public boolean isConectado() {
        return conectado && socket != null && !socket.isClosed();
    }
    
    /**
     * Obtiene el último error ocurrido
     */
    public String getUltimoError() {
        return ultimoError;
    }
    
    /**
     * Verifica la disponibilidad del servidor
     */
    public CompletableFuture<Boolean> verificarDisponibilidad() {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket testSocket = new Socket()) {
                testSocket.connect(new java.net.InetSocketAddress(SERVIDOR_HOST, SERVIDOR_PUERTO), TIMEOUT_CONEXION);
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }
    
    /**
     * Obtiene información del estado de conexión
     */
    public String getEstadoConexion() {
        if (conectado && socket != null && !socket.isClosed()) {
            return "Conectado a " + SERVIDOR_HOST + ":" + SERVIDOR_PUERTO;
        } else if (ultimoError != null) {
            return "Desconectado - " + ultimoError;
        } else {
            return "Desconectado";
        }
    }
    
    /**
     * Cierra todas las conexiones al finalizar la aplicación
     */
    public static void cerrarTodasLasConexiones() {
        if (instancia != null) {
            instancia.desconectar();
            instancia = null;
        }
    }
} 