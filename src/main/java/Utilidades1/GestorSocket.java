package Utilidades1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GestorSocket {
    private static final String SERVER_HOST = "192.168.56.1";

    private static final int SERVER_PORT_ALT = 50002;  // Puerto alternativo (local)
    
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
        try {
            System.out.println("Intentando conectar al servidor alternativo: " + SERVER_HOST + ":" + SERVER_PORT_ALT);
            
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT_ALT), 30000); // 30 segundos de timeout para conexión
            socket.setKeepAlive(true); // Mantener la conexión viva
            socket.setTcpNoDelay(true); // Desactivar el algoritmo de Nagle para envío inmediato
            socket.setSoTimeout(60000); // Timeout de 60 segundos para operaciones de lectura
            
            // Importante: primero crear el ObjectOutputStream antes que el ObjectInputStream
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush(); // Es importante hacer flush después de crear el ObjectOutputStream
            
            entrada = new ObjectInputStream(socket.getInputStream());
            
            conectado = true;
            System.out.println("Conectado al servidor correctamente.");
            
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            conectado = false;
        }
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