package com.example.pruebamongodbcss.Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor sin GUI que se inicia automáticamente en puerto 50002
 * Versión simplificada del servidor original
 */
public class ServidorSinGUI {
    private static final int PUERTO = 50002;
    private ServerSocket serverSocket;
    private final ExecutorService pool;
    private boolean running;

    public ServidorSinGUI() {
        this.pool = Executors.newCachedThreadPool();
        this.running = true;
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(PUERTO);
            System.out.println("🚀 Servidor iniciado automáticamente en puerto " + PUERTO);
            System.out.println("✅ Listo para recibir conexiones");
            System.out.println("📋 Para detener: Ctrl+C");
            System.out.println();

            // Iniciar hilo automático para gestión de estados de citas
            Thread autoStatusThread = new Thread("Server: Auto Status Management Thread") {
                @Override
                public void run() {
                    try {
                        Thread.sleep(30000); // Esperar 30 segundos
                    } catch (InterruptedException e) {
                        System.out.println("Hilo de gestión automática interrumpido durante inicialización");
                        return;
                    }
                    
                    com.example.pruebamongodbcss.calendar.CalendarService calendarService = 
                        new com.example.pruebamongodbcss.calendar.CalendarService();
                    
                    while (running) {
                        try {
                            int citasActualizadas = calendarService.verificarYActualizarEstadosAutomaticos();
                            if (citasActualizadas > 0) {
                                System.out.println("🔄 Servidor: " + citasActualizadas + " citas actualizadas automáticamente");
                            }
                            Thread.sleep(600000); // 10 minutos
                        } catch (InterruptedException e) {
                            System.out.println("Hilo de gestión automática interrumpido");
                            break;
                        } catch (Exception e) {
                            System.err.println("Error en gestión automática: " + e.getMessage());
                        }
                    }
                }
            };
            
            autoStatusThread.setDaemon(true);
            autoStatusThread.start();
            System.out.println("🤖 Gestión automática de citas iniciada");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("📱 Cliente conectado: " + clientSocket.getInetAddress());
                    ClienteHandler handler = new ClienteHandler(clientSocket);
                    pool.execute(handler);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Error al aceptar conexión: " + e.getMessage());
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error al iniciar servidor: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("🛑 Servidor detenido correctamente");
        } catch (IOException e) {
            System.err.println("Error al detener servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("  🏥 SERVIDOR CLÍNICA - INICIO AUTOMÁTICO 🏥");
        System.out.println("================================================");
        System.out.println();
        
        ServidorSinGUI servidor = new ServidorSinGUI();
        
        // Agregar shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Cerrando servidor...");
            servidor.detener();
        }));
        
        // Iniciar servidor
        servidor.iniciar();
    }
} 