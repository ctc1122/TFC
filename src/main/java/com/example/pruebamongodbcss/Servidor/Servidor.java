package com.example.pruebamongodbcss.Servidor;

import java.awt.GridLayout;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Servidor {
    private static final int PUERTO_DEFAULT = 50002;
    private final int puerto;
    private ServerSocket serverSocket;
    private final ExecutorService pool;

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
        this.running = true;
    }

    /**
     * Método para iniciar el servidor
     * @return void
     */
    public void iniciar() {
        try {
            //serverSocket = new ServerSocket(puerto,50, InetAddress.getByName("0.0.0.0"));
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en puerto " + puerto);

            // Iniciar hilo automático para gestión de estados de citas
            Thread autoStatusThread = new Thread("Server: Auto Status Management Thread") {
                @Override
                public void run() { 
                    // Esperar 30 segundos antes de la primera verificación para que el servidor esté completamente iniciado
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        System.out.println("Hilo de gestión automática de estados interrumpido durante inicialización");
                        return;
                    }
                    
                    // Crear instancia del servicio de calendario
                    com.example.pruebamongodbcss.calendar.CalendarService calendarService = 
                        new com.example.pruebamongodbcss.calendar.CalendarService();
                    
                    while (running) {
                        try {
                            // Ejecutar verificación automática de estados cada 2 minutos
                            int citasActualizadas = calendarService.verificarYActualizarEstadosAutomaticos();
                            if (citasActualizadas > 0) {
                                System.out.println("🔄 Servidor: " + citasActualizadas + " citas actualizadas automáticamente");
                            }
                            
                            // Esperar 2 minutos antes de la siguiente verificación
                            Thread.sleep(600000); // 10 minutos
                        } catch (InterruptedException e) {
                            System.out.println("Hilo de gestión automática de estados interrumpido");
                            break;
                        } catch (Exception e) {
                            System.err.println("Error en gestión automática de estados: " + e.getMessage());
                            e.printStackTrace();
                            // Continuar ejecutándose a pesar del error
                        }
                    }
                }
            };
            
            autoStatusThread.setDaemon(true);
            autoStatusThread.start();
            System.out.println("🤖 Hilo automático de gestión de estados de citas iniciado");

            while (running) {
                try {
                    // Esperar a que un cliente se conecte
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress());
                    // Crear un nuevo hilo para manejar la conexión del cliente
                    ClienteHandler handler = new ClienteHandler(clientSocket);
                    // Ejecutar el hilo en el pool de hilos
                    pool.execute(handler);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error al aceptar conexión del cliente: " + e.getMessage());
                    } else {
                        // Si ya no está corriendo, es normal recibir una excepción al cerrar el socket
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
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
        
        VentanaServer ventana = new VentanaServer();
        ventana.setVisible(true);
        ventana.revalidate();


    }


} 


    //Ventana para mostrar el servidor
    class VentanaServer extends JFrame{

        Servidor servidor;

        JButton btnIniciar = new JButton("Iniciar");
        JButton btnDetener = new JButton("Detener");
        JButton btnSalir = new JButton("Salir");

        public VentanaServer( ){
            setTitle("Servidor Clinica");
            setSize(500, 500);
            setLocationRelativeTo(null);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            setLayout(new GridLayout(3, 1));
            add(btnIniciar);
            add(btnDetener);
            add(btnSalir);

            btnIniciar.addActionListener(e -> {
                try {
                    this.servidor = new Servidor();
                    // Iniciar el servidor en un hilo separado para no bloquear la UI
                    new Thread(() -> {
                        servidor.iniciar();
                    }).start();
                    btnIniciar.setEnabled(false);
                    btnDetener.setEnabled(true);
                } catch (Exception ex) {
                    System.err.println("Error al iniciar el servidor: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            btnDetener.addActionListener(e -> {
                if (servidor != null) {
                    servidor.detener();
                    btnIniciar.setEnabled(true);
                    btnDetener.setEnabled(false);
                }
            });

            btnSalir.addActionListener(e -> {
                if (servidor != null) {
                    servidor.detener();
                }
                this.dispose();
                System.exit(0);
            });

            // Inicialmente, el botón detener debería estar deshabilitado
            btnDetener.setEnabled(false);
        }
    }