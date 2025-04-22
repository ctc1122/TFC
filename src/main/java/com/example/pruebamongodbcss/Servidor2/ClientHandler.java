package com.example.pruebamongodbcss.Servidor2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import com.mongodb.client.MongoDatabase;

/**
 * Clase que maneja la conexión con el cliente, es el manejador de la conexión de cada cliente.<br>
 * Procesa los mensajes recibidos del cliente y los procesa según el tipo de mensaje de acuerdo al protocolo.<br>
 * @author Cristopher
 * 
 */
public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private final MongoDatabase database;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ClientHandler(Socket socket, MongoDatabase database){
        this.clientSocket = socket;
        this.database = database;
    }

    @Override
    public void run() {
        //Inicializar streams
        try {
            entrada = new ObjectInputStream(clientSocket.getInputStream());
            salida = new ObjectOutputStream(clientSocket.getOutputStream());

            while(true){
                //Leer tipo de mensaje
                int tipoMensaje = entrada.readInt();
                //Procesar mensaje
                procesarMensaje(tipoMensaje);
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al recibir datos del cliente: " + e.getMessage());
        }finally{
            cerrarConexion();
        }
    }

    public void procesarMensaje(int tipoMensaje){
        switch(tipoMensaje){

            default:
                System.out.println("Mensaje de tipo desconocido recibido: " + tipoMensaje);
        }
    }

    private void cerrarConexion(){
        try{
            if(entrada != null) entrada.close();
            if(salida != null) salida.close();
            if(clientSocket != null) clientSocket.close();
        }catch(IOException e){
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

}
