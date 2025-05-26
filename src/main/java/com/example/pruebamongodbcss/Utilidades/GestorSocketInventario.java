package com.example.pruebamongodbcss.Utilidades;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;

/**
 * Gestor de socket específico para el servidor de inventario (puerto 50005)
 * Maneja la comunicación con el servidor de inventario usando streams de texto
 */
public class GestorSocketInventario {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT_INVENTARIO = 50005;  // Puerto del servidor de inventario
    private static final int TIMEOUT_CONEXION = 10000; // 10 segundos
    private static final int TIMEOUT_LECTURA = 30000; // 30 segundos
    
    private static GestorSocketInventario instance;
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private boolean conectado = false;
    private String ultimoError;

    private GestorSocketInventario() {
        // Constructor privado para singleton
    }

    public static synchronized GestorSocketInventario getInstance() {
        if (instance == null) {
            instance = new GestorSocketInventario();
        }
        return instance;
    }

    /**
     * Conecta al servidor de inventario de forma asíncrona
     */
    public CompletableFuture<Boolean> conectarAlServidorInventario() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Cerrar conexión anterior si existe
                if (socket != null && !socket.isClosed()) {
                    cerrarConexion();
                }
                
                System.out.println("🔄 Intentando conectar al servidor de inventario: " + SERVER_HOST + ":" + SERVER_PORT_INVENTARIO);
                
                // Crear socket con timeout de conexión
                socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT_INVENTARIO), TIMEOUT_CONEXION);
                
                // Configurar socket
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(TIMEOUT_LECTURA);
                
                System.out.println("🔗 Socket conectado, creando streams de texto...");
                
                // Crear streams de texto para comunicación
                salida = new PrintWriter(socket.getOutputStream(), true); // autoFlush = true
                System.out.println("📤 PrintWriter creado");
                
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("📥 BufferedReader creado");
                
                conectado = true;
                ultimoError = null;
                System.out.println("✅ Conectado al servidor de inventario correctamente.");
                
                // Verificar que la conexión sigue activa
                if (socket.isClosed() || !socket.isConnected()) {
                    throw new IOException("La conexión se cerró inmediatamente después de establecerse");
                }
                
                return true;
                
            } catch (IOException e) {
                ultimoError = "Error al conectar con el servidor de inventario: " + e.getMessage();
                System.err.println("❌ " + ultimoError);
                e.printStackTrace();
                conectado = false;
                
                // Limpiar recursos en caso de error
                limpiarRecursos();
                return false;
            }
        });
    }

    /**
     * Verifica si hay conexión disponible
     */
    public CompletableFuture<Boolean> verificarDisponibilidad() {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket testSocket = new Socket()) {
                testSocket.connect(new java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT_INVENTARIO), 5000);
                System.out.println("✅ Servidor de inventario disponible en puerto " + SERVER_PORT_INVENTARIO);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Servidor de inventario no disponible: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Verifica si la conexión actual está activa
     */
    public boolean verificarConexionActiva() {
        if (!conectado || socket == null) {
            return false;
        }
        
        try {
            // Verificar estado del socket
            if (socket.isClosed() || !socket.isConnected()) {
                System.err.println("⚠️ Socket cerrado o desconectado");
                conectado = false;
                return false;
            }
            
            // Verificar streams
            if (salida == null || entrada == null) {
                System.err.println("⚠️ Streams no disponibles");
                conectado = false;
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("⚠️ Error al verificar conexión: " + e.getMessage());
            conectado = false;
            return false;
        }
    }

    /**
     * Envía una petición al servidor de inventario con reconexión automática
     */
    public void enviarPeticion(String mensaje) throws IOException {
        // Verificar y reconectar si es necesario
        if (!verificarConexionActiva()) {
            System.out.println("🔄 Reconectando al servidor de inventario...");
            boolean reconectado = reconectar().join();
            if (!reconectado) {
                throw new IOException("No se pudo reconectar al servidor de inventario");
            }
        }
        
        try {
            System.out.println("📤 Enviando mensaje al inventario: " + mensaje);
            salida.println(mensaje);
            
            if (salida.checkError()) {
                throw new IOException("Error al enviar mensaje - PrintWriter error");
            }
            
            System.out.println("✅ Mensaje enviado al inventario");
        } catch (Exception e) {
            System.err.println("❌ Error al enviar mensaje: " + e.getMessage());
            conectado = false;
            throw new IOException("Error al enviar mensaje", e);
        }
    }

    /**
     * Lee la respuesta del servidor como texto
     */
    public String leerRespuesta() throws IOException {
        if (!verificarConexionActiva()) {
            throw new IOException("No hay conexión activa con el servidor de inventario");
        }
        
        try {
            String respuesta = entrada.readLine();
            if (respuesta == null) {
                throw new IOException("El servidor cerró la conexión");
            }
            System.out.println("📥 Respuesta recibida: " + respuesta);
            return respuesta;
        } catch (SocketTimeoutException e) {
            System.err.println("⏰ Timeout al leer respuesta del servidor");
            throw new IOException("Timeout al leer respuesta del servidor de inventario");
        } catch (IOException e) {
            System.err.println("❌ Error al leer respuesta: " + e.getMessage());
            conectado = false;
            throw e;
        }
    }

    /**
     * Limpia los recursos sin cerrar la conexión
     */
    private void limpiarRecursos() {
        try {
            if (entrada != null) {
                entrada.close();
                entrada = null;
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar entrada: " + e.getMessage());
        }
        
        try {
            if (salida != null) {
                salida.close();
                salida = null;
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar salida: " + e.getMessage());
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar socket: " + e.getMessage());
        }
        
        conectado = false;
    }

    public BufferedReader getEntrada() {
        return entrada;
    }

    public PrintWriter getSalida() {
        return salida;
    }

    public boolean isConectado() {
        return verificarConexionActiva();
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public String getEstadoConexion() {
        if (verificarConexionActiva()) {
            return "Conectado al servidor de inventario (" + SERVER_HOST + ":" + SERVER_PORT_INVENTARIO + ")";
        } else if (ultimoError != null) {
            return "Desconectado - " + ultimoError;
        } else {
            return "Desconectado del servidor de inventario";
        }
    }

    /**
     * Cierra la conexión con el servidor de inventario
     */
    public void cerrarConexion() {
        if (conectado || socket != null) {
            System.out.println("🔌 Cerrando conexión con servidor de inventario...");
            limpiarRecursos();
            System.out.println("✅ Conexión cerrada correctamente");
        }
    }

    /**
     * Reinicia la conexión
     */
    public CompletableFuture<Boolean> reconectar() {
        cerrarConexion();
        return conectarAlServidorInventario();
    }

    /**
     * Método para limpiar la instancia (útil para testing)
     */
    public static void limpiarInstancia() {
        if (instance != null) {
            instance.cerrarConexion();
            instance = null;
        }
    }
} 