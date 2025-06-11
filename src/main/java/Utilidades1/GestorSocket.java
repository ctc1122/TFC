package Utilidades1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GestorSocket {
    private static final String SERVER_HOST_SERVEO = "serveo.net";
    private static final String SERVER_HOST_LOCAL = "localhost";
    private static final int SERVER_PORT_SERVEO = 50002;  // Puerto serveo y localhost

    // Variables estáticas para recordar la configuración exitosa
    private static String servidorExitoso = null;
    private static boolean configuracionDeterminada = false;
    
    private static GestorSocket instance;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado = false;

    private GestorSocket() {
        conectarAlServidor();
    }

    public static GestorSocket getInstance() {
        if (instance == null) {
            instance = new GestorSocket();
        }
        return instance;
    }

    /**
     * Crea una nueva conexión independiente al servidor.
     * Esta conexión no interfiere con el singleton y debe ser cerrada manualmente.
     * @return Una nueva instancia de GestorSocket con conexión independiente
     * @throws IOException Si no se puede establecer la conexión
     */
    public static GestorSocket crearConexionIndependiente() throws IOException {
        GestorSocket nuevaConexion = new GestorSocket();
        if (!nuevaConexion.isConectado()) {
            throw new IOException("No se pudo establecer conexión independiente con el servidor");
        }
        return nuevaConexion;
    }

    private void conectarAlServidor() {
        // Si ya sabemos qué configuración funciona, usarla directamente
        if (configuracionDeterminada && servidorExitoso != null) {
            conectarConConfiguracionConocida();
            return;
        }
        
        // Primera vez: determinar qué configuración funciona
        determinarConfiguracionExitosa();
    }
    
    private void conectarConConfiguracionConocida() {
        try {
            System.out.println("🔄 Conectando usando configuración conocida: " + servidorExitoso);
            
            if (servidorExitoso.equals("SERVEO")) {
                socket = new Socket(SERVER_HOST_SERVEO, SERVER_PORT_SERVEO);
                System.out.println("✅ Reconectado a Serveo.net correctamente.");
            } else if (servidorExitoso.equals("LOCAL")) {
                socket = new Socket(SERVER_HOST_LOCAL, SERVER_PORT_SERVEO);
                System.out.println("✅ Reconectado al servidor local correctamente.");
            }
            
            configurarSocket();
            conectado = true;
            
        } catch (IOException e) {
            System.err.println("❌ Error al reconectar con configuración conocida: " + e.getMessage());
            // Si falla la configuración conocida, volver a determinarla
            configuracionDeterminada = false;
            servidorExitoso = null;
            determinarConfiguracionExitosa();
        }
    }
    
    private void determinarConfiguracionExitosa() {
        System.out.println("🔍 Determinando configuración de servidor por primera vez...");
        
        // Primer intento: serveo.net (túnel SSH)
        try {
            System.out.println("🌐 Intentando conectar a Serveo.net: " + SERVER_HOST_SERVEO + ":" + SERVER_PORT_SERVEO);
            socket = new Socket(SERVER_HOST_SERVEO, SERVER_PORT_SERVEO);
            configurarSocket();
            conectado = true;
            servidorExitoso = "SERVEO";
            configuracionDeterminada = true;
            System.out.println("✅ Conectado a Serveo.net correctamente. Configuración guardada.");
            return;
        } catch (IOException e) {
            System.out.println("⚠️ No se pudo conectar a Serveo.net: " + e.getMessage());
        }
        
        // Segundo intento: conexión local
        try {
            System.out.println("🏠 Intentando conectar al servidor local: " + SERVER_HOST_LOCAL + ":" + SERVER_PORT_SERVEO);
            socket = new Socket(SERVER_HOST_LOCAL, SERVER_PORT_SERVEO);
            configurarSocket();
            conectado = true;
            servidorExitoso = "LOCAL";
            configuracionDeterminada = true;
            System.out.println("✅ Conectado al servidor local correctamente. Configuración guardada.");
        } catch (IOException e) {
            System.err.println("❌ Error al conectar con todos los servidores: " + e.getMessage());
            conectado = false;
            configuracionDeterminada = false;
            servidorExitoso = null;
        }
    }
    
    /**
     * Método para forzar la redetección del servidor (útil si cambia la configuración de red)
     */
    public static void resetearConfiguracion() {
        configuracionDeterminada = false;
        servidorExitoso = null;
        System.out.println("🔄 Configuración de servidor reseteada. Se volverá a detectar en la próxima conexión.");
    }
    
    /**
     * Método para conocer qué configuración está usando actualmente
     */
    public static String getConfiguracionActual() {
        if (configuracionDeterminada && servidorExitoso != null) {
            return servidorExitoso.equals("SERVEO") ? "Serveo.net" : "Servidor Local";
        }
        return "No determinada";
    }
    
    private void configurarSocket() throws IOException {
            socket.setKeepAlive(true); // Mantener la conexión viva
            socket.setTcpNoDelay(true); // Desactivar el algoritmo de Nagle para envío inmediato
            socket.setSoTimeout(60000); // Timeout de 60 segundos para operaciones de lectura
            
            // Importante: primero crear el ObjectOutputStream antes que el ObjectInputStream
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush(); // Es importante hacer flush después de crear el ObjectOutputStream
            
            entrada = new ObjectInputStream(socket.getInputStream());
    }

    public void enviarPeticion(String codigoPeticion) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión con el servidor");
        }
        System.out.println("Enviando peticion: " + codigoPeticion);
        synchronized (salida) {
            salida.writeUTF(codigoPeticion);
            salida.flush();
        }
        System.out.println("Petición enviada y flush completado");
    }

    public ObjectInputStream getEntrada() {
        return entrada;
    }

    public boolean isConectado() {
        return conectado && socket != null && !socket.isClosed();
    }

    public void cerrarConexion() {
        try {
            if (conectado) {
                if (entrada != null) {
                    entrada.close();
                }
                if (salida != null) {
                    salida.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                conectado = false;
                // Solo resetear instance si es la instancia singleton
                if (this == instance) {
                    instance = null;
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
        
    }

    public ObjectOutputStream getSalida() {
        return salida;
    }
} 