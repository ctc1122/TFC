package com.example.pruebamongodbcss.Servidores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MiServidor {
    private static final int PUERTO_BASE = 50000;
    private static final Set<PrintWriter> salidasClientes = ConcurrentHashMap.newKeySet();
    private static final Map<String, Socket> usuariosConectados = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PUERTO_BASE)) {
            System.out.println("[SERVIDOR TCP] Escuchando en puerto " + PUERTO_BASE);

            while (true) {
                Socket cliente = servidor.accept();
                new Thread(new ManejadorCliente(cliente)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ManejadorCliente implements Runnable {
        private Socket socket;
        private String nombre;
        private BufferedReader entrada;
        private PrintWriter salida;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                salida = new PrintWriter(socket.getOutputStream(), true);

                salida.println("BIENVENIDO - Dime tu nombre:");
                nombre = entrada.readLine();
                System.out.println("[+] Conectado: " + nombre);
                usuariosConectados.put(nombre, socket);
                salidasClientes.add(salida);

                // Notificar a todos del nuevo usuario
                enviarATodos("🟢 Se ha conectado: " + nombre);
                enviarListaUsuarios();

                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    System.out.println("[" + nombre + "]: " + mensaje);
                    enviarATodos("[" + nombre + "]: " + mensaje);
                }
            } catch (IOException e) {
                System.out.println("[-] Usuario desconectado: " + nombre);
            } finally {
                try {
                    if (nombre != null) {
                        usuariosConectados.remove(nombre);
                        enviarATodos("🔴 Desconectado: " + nombre);
                        enviarListaUsuarios();
                    }
                    if (salida != null) {
                        salidasClientes.remove(salida);
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void enviarATodos(String mensaje) {
            for (PrintWriter out : salidasClientes) {
                out.println(mensaje);
            }
        }

        private void enviarListaUsuarios() {
            StringBuilder lista = new StringBuilder("@USUARIOS:");
            for (String usuario : usuariosConectados.keySet()) {
                lista.append(usuario).append(",");
            }
            for (PrintWriter out : salidasClientes) {
                out.println(lista.toString());
            }
        }
    }
}
