package com.example.pruebamongodbcss.Servidor;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.bson.Document;

import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class ClienteHandler implements Runnable {
    private final Socket clientSocket;
    private final MongoDatabase database;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ClienteHandler(Socket socket, MongoDatabase database) {
        this.clientSocket = socket;
        this.database = database;
    }

    @Override
    public void run() {
        try {
            // Inicializar streams
            salida = new ObjectOutputStream(clientSocket.getOutputStream());
            entrada = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                // Leer tipo de mensaje
                int tipoMensaje = entrada.readInt();

                switch (tipoMensaje) {
                    case Protocolo.LOGIN_REQUEST:
                        procesarLogin();
                        break;
                    default:
                        System.out.println("Mensaje no reconocido: " + tipoMensaje);
                }
            }
        } catch (EOFException e) {
            System.out.println("Cliente desconectado");
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }


    private void procesarLogin() throws IOException {
        String usuario = entrada.readUTF();
        String password = entrada.readUTF();
        
        // Buscar usuario en la colección de usuarios de MongoDB
        MongoCollection<Document> usuarios = database.getCollection("usuarios");
        Document usuarioDoc = usuarios.find(and(
            eq("usuario", usuario),
            eq("password", password)
        )).first();

        if (usuarioDoc != null) {
            salida.writeInt(Protocolo.LOGIN_RESPONSE);
            salida.writeInt(Protocolo.LOGIN_SUCCESS);
        } else {
            salida.writeInt(Protocolo.LOGIN_RESPONSE);
            salida.writeInt(Protocolo.LOGIN_FAILED);
        }
        salida.flush();
    }

    private void cerrarConexion() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
} 