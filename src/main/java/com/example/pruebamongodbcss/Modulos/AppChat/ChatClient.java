package com.example.pruebamongodbcss.Modulos.AppChat;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PUERTO = 60000;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private final String userId;
    private final String userName;
    private volatile boolean conectado;
    private Thread receptorThread;

    private final List<Consumer<ChatMessage>> messageHandlers = new ArrayList<>();
    private final List<Consumer<List<Usuario>>> userListHandlers = new ArrayList<>();

    public static class Usuario {
        public final String id;
        public final String nombre;

        public Usuario(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    public ChatClient(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public void conectar() throws IOException {
        if (conectado) return;

        socket = new Socket(HOST, PUERTO);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        conectado = true;

        // Enviar mensaje de conexión
        ChatMessage mensajeConexion = new ChatMessage(
            ChatMessage.TipoMensaje.MENSAJE_PRIVADO,
            userName,
            "SERVIDOR",
            "Conexión"
        );
        enviarMensaje(mensajeConexion);

        // Iniciar thread para recibir mensajes
        receptorThread = new Thread(this::recibirMensajes);
        receptorThread.setDaemon(true);
        receptorThread.start();
    }

    public void desconectar() {
        if (!conectado) return;

        try {
            conectado = false;
            if (socket != null && !socket.isClosed()) {
                // Enviar mensaje de desconexión
                ChatMessage mensajeDesconexion = new ChatMessage(
                    ChatMessage.TipoMensaje.MENSAJE_PRIVADO,
                    userName,
                    "SERVIDOR",
                    "Desconexión"
                );
                enviarMensaje(mensajeDesconexion);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensajePrivado(String destinatarioId, String destinatarioNombre, String mensaje) {
        if (!conectado) return;

        try {
            ChatMessage chatMessage = new ChatMessage(
                ChatMessage.TipoMensaje.MENSAJE_PRIVADO,
                userName,
                destinatarioNombre,
                mensaje
            );
            enviarMensaje(chatMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarArchivo(String destinatarioId, String destinatarioNombre, File archivo) {
        if (!conectado || archivo == null || !archivo.exists() || !archivo.isFile()) return;

        try {
            // Leer el contenido del archivo
            byte[] archivoData = Files.readAllBytes(archivo.toPath());
            
            // Crear mensaje con información del archivo
            ChatMessage chatMessage = new ChatMessage(userName, destinatarioNombre, archivo);
            chatMessage.setArchivoData(archivoData);
            
            // Enviar el mensaje
            enviarMensaje(chatMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensaje(ChatMessage mensaje) throws IOException {
        if (output != null) {
            output.writeObject(mensaje);
            output.flush();
        }
    }

    private void recibirMensajes() {
        while (conectado) {
            try {
                Object mensaje = input.readObject();
                if (mensaje instanceof ChatMessage) {
                    ChatMessage chatMessage = (ChatMessage) mensaje;
                    procesarMensaje(chatMessage);
                } else if (mensaje instanceof String) {
                    procesarListaUsuarios((String) mensaje);
                }
            } catch (IOException | ClassNotFoundException e) {
                if (conectado) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    private void procesarMensaje(ChatMessage mensaje) {
        switch (mensaje.getTipo()) {
            case LISTA_USUARIOS:
                procesarListaUsuarios(mensaje.getContenido());
                break;
            case ARCHIVO:
            case MENSAJE_PRIVADO:
                notificarMensaje(mensaje);
                break;
        }
    }

    private void procesarListaUsuarios(String contenido) {
        List<Usuario> usuarios = new ArrayList<>();
        String[] usuariosStr = contenido.split(";");
        for (String usuarioStr : usuariosStr) {
            if (!usuarioStr.isEmpty()) {
                String[] partes = usuarioStr.split(",");
                usuarios.add(new Usuario(partes[1], partes[0]));
            }
        }
        notificarListaUsuarios(usuarios);
    }

    public void addMessageHandler(Consumer<ChatMessage> handler) {
        messageHandlers.add(handler);
    }

    public void addUserListHandler(Consumer<List<Usuario>> handler) {
        userListHandlers.add(handler);
    }

    private void notificarMensaje(ChatMessage mensaje) {
        for (Consumer<ChatMessage> handler : messageHandlers) {
            handler.accept(mensaje);
        }
    }

    private void notificarListaUsuarios(List<Usuario> usuarios) {
        for (Consumer<List<Usuario>> handler : userListHandlers) {
            handler.accept(usuarios);
        }
    }

    public boolean isConectado() {
        return conectado;
    }
} 