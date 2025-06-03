package Utilidades1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GestorSocket {
    private static final String SERVER_HOST_NGROK = "0.tcp.ngrok.io";     // Base ngrok (puerto dinámico)
    private static final String SERVER_HOST_SERVEO = "serveo.net";
    private static final String SERVER_HOST_LOCAL = "localhost";
    private static final int SERVER_PORT_SERVEO = 50002;  // Puerto serveo y localhost
    private static final int[] NGROK_COMMON_PORTS = {10000, 11000, 12000, 13000, 14000, 15000, 16000, 17000, 18000, 19000}; // Puertos comunes ngrok
    
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
        // Primer intento: ngrok TCP (intentar puertos comunes)
        System.out.println("🚀 Intentando conectar a Ngrok TCP (puertos comunes)...");
        for (int puerto : NGROK_COMMON_PORTS) {
            try {
                System.out.println("   Probando " + SERVER_HOST_NGROK + ":" + puerto);
                socket = new Socket(SERVER_HOST_NGROK, puerto);
                configurarSocket();
                conectado = true;
                System.out.println("✅ Conectado a Ngrok TCP (" + SERVER_HOST_NGROK + ":" + puerto + ") correctamente.");
                return;
            } catch (IOException e) {
                // Continuar con el siguiente puerto
            }
        }
        System.out.println("⚠️ No se pudo conectar a Ngrok TCP en puertos comunes");
        
        // Segundo intento: serveo.net (túnel SSH)
        try {
            System.out.println("🌐 Intentando conectar a Serveo.net: " + SERVER_HOST_SERVEO + ":" + SERVER_PORT_SERVEO);
            socket = new Socket(SERVER_HOST_SERVEO, SERVER_PORT_SERVEO);
            configurarSocket();
            conectado = true;
            System.out.println("✅ Conectado a Serveo.net correctamente.");
            return;
        } catch (IOException e) {
            System.out.println("⚠️ No se pudo conectar a Serveo.net: " + e.getMessage());
        }
        
        // Tercer intento (fallback final): conexión local
        try {
            System.out.println("🏠 Intentando conectar al servidor local: " + SERVER_HOST_LOCAL + ":" + SERVER_PORT_SERVEO);
            socket = new Socket(SERVER_HOST_LOCAL, SERVER_PORT_SERVEO);
            configurarSocket();
            conectado = true;
            System.out.println("✅ Conectado al servidor local correctamente.");
        } catch (IOException e) {
            System.err.println("❌ Error al conectar con todos los servidores: " + e.getMessage());
            conectado = false;
        }
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