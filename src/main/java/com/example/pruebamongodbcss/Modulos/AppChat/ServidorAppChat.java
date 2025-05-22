package com.example.pruebamongodbcss.Modulos.AppChat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorAppChat {
    private static final int PUERTO = 60000;
    private static ServidorAppChat instance;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private final Map<String, ClientHandler> clientes = new ConcurrentHashMap<>();
    private Thread serverThread;

    private ServidorAppChat() {}

    public static ServidorAppChat getInstance() {
        if (instance == null) {
            instance = new ServidorAppChat();
        }
        return instance;
    }

    public void iniciar() {
        if (running) return;

        try {
            serverSocket = new ServerSocket(PUERTO);
            running = true;
            
            serverThread = new Thread(() -> {
                System.out.println("Servidor de chat iniciado en puerto " + PUERTO);
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket);
                        handler.start();
                    } catch (IOException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void detener() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            clientes.values().forEach(ClientHandler::detener);
            clientes.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastListaUsuarios() {
        StringBuilder listaUsuarios = new StringBuilder();
        clientes.forEach((id, handler) -> {
            if (listaUsuarios.length() > 0) listaUsuarios.append(";");
            listaUsuarios.append(handler.userName).append(",").append(handler.userId);
        });
        
        ChatMessage mensaje = new ChatMessage(
            ChatMessage.TipoMensaje.LISTA_USUARIOS,
            "SERVIDOR",
            "TODOS",
            listaUsuarios.toString()
        );
        
        clientes.values().forEach(handler -> {
            try {
                handler.enviarMensaje(mensaje);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private class ClientHandler extends Thread {
        private final Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String userId;
        private String userName;
        private volatile boolean running = true;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while (running) {
                    Object mensaje = in.readObject();
                    if (mensaje instanceof ChatMessage) {
                        procesarMensaje((ChatMessage) mensaje);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (running) {
                    e.printStackTrace();
                }
            } finally {
                detener();
            }
        }

        private void procesarMensaje(ChatMessage mensaje) throws IOException {
            switch (mensaje.getTipo()) {
                case MENSAJE_PRIVADO:
                    if (userId == null) {
                        // Primer mensaje - conexión
                        userId = mensaje.getRemitente();
                        userName = mensaje.getRemitente();
                        clientes.put(userId, this);
                        broadcastListaUsuarios();
                    } else {
                        // Mensaje normal
                        ClientHandler destinatario = clientes.get(mensaje.getDestinatario());
                        if (destinatario != null) {
                            destinatario.enviarMensaje(mensaje);
                        }
                    }
                    break;
                    
                case ARCHIVO:
                    ClientHandler destinatario = clientes.get(mensaje.getDestinatario());
                    if (destinatario != null) {
                        destinatario.enviarMensaje(mensaje);
                    }
                    break;
            }
        }

        public void enviarMensaje(ChatMessage mensaje) throws IOException {
            if (out != null && running) {
                out.writeObject(mensaje);
                out.flush();
            }
        }

        public void detener() {
            running = false;
            try {
                if (userId != null) {
                    clientes.remove(userId);
                    broadcastListaUsuarios();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
} 