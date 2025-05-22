package com.example.pruebamongodbcss.Servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.bson.Document;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import Utilidades.GestorConexion;

public class ClienteHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ClienteHandler(Socket socket) {
        this.clientSocket = socket;
 
    }

    @Override
    public void run() {
        try {
            System.out.println("Nuevo cliente conectado desde: " + clientSocket.getInetAddress());
            
            // Inicializar streams
            salida = new ObjectOutputStream(clientSocket.getOutputStream());
            salida.flush(); // Importante: flush después de crear el stream
            entrada = new ObjectInputStream(clientSocket.getInputStream());
            
            System.out.println("Streams inicializados correctamente");

            while (!clientSocket.isClosed()) {
                try {
                    // Leer la cadena completa del mensaje
                    String mensajeCompleto = entrada.readUTF();
                    System.out.println("Mensaje recibido del cliente: [" + mensajeCompleto + "]");
                    
                    // Separar el código y los parámetros
                    String[] partes = mensajeCompleto.split("\\|");  // Escapar el carácter |
                    System.out.println("Partes del mensaje: " + String.join(", ", partes));
                    
                    if (partes.length == 0) {
                        System.err.println("Error: Mensaje vacío recibido");
                        continue;
                    }
                    
                    int codigo = Integer.parseInt(partes[0]);
                    System.out.println("Código recibido: " + codigo);
                    
                    // Separar los parámetros si existen
                    String[] parametros = new String[0];
                    if (partes.length > 1) {
                        parametros = partes[1].split(":");  // Separar usuario y contraseña
                        System.out.println("Parámetros recibidos: " + String.join(", ", parametros));
                    } else {
                        System.err.println("Mensaje recibido sin parámetros: " + mensajeCompleto);
                    }

                    switch (codigo) {
                        case Protocolo.LOGIN_REQUEST:
                            System.out.println("Procesando solicitud de login...");
                            if (parametros.length >= 2) {
                                procesarLogin(parametros[0], parametros[1]);
                                System.out.println("Solicitud de login procesada");
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud de login");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.LOGIN_RESPONSE);
                                    salida.writeInt(Protocolo.INVALID_CREDENTIALS);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.REGISTRO_REQUEST:
                            try {
                                // El usuario viene en formato texto después de la barra
                                String usuarioStr = mensajeCompleto.substring(mensajeCompleto.indexOf("|") + 1);
                                // Convertir el string a un objeto Usuario
                                Document doc = Document.parse(usuarioStr);
                                Usuario nuevoUsuario = new Usuario(doc);
                                procesarRegistro(nuevoUsuario);
                                
                                // Enviar respuesta de éxito
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.REGISTRO_RESPONSE);
                                    salida.writeInt(Protocolo.REGISTRO_SUCCESS);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al procesar registro: " + e.getMessage());
                                e.printStackTrace();
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.REGISTRO_RESPONSE);
                                    salida.writeInt(Protocolo.REGISTRO_FAILED);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.CREARPROPIETARIO:
                            procesarCrearPropietario();
                            break;
                        default:
                            System.out.println("Mensaje no reconocido: " + codigo);
                    }
                } catch (IOException e) {
                    if (clientSocket.isClosed()) {
                        System.out.println("Cliente desconectado normalmente");
                        break;
                    }
                    System.err.println("Error al procesar mensaje: " + e.getMessage());
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    System.err.println("Error: Código de operación inválido");
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("Error inesperado: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarConexion();
        }
    }

    private void procesarLogin(String usuario, String password) throws IOException {
        System.out.println("Procesando login para usuario: " + usuario);
        
        try {
            // Buscar usuario en la colección de usuarios de MongoDB
            MongoCollection<Document> usuarios = GestorConexion.conectarEmpresa().getCollection("usuarios");
            Document usuarioDoc = usuarios.find(and(
                eq("usuario", usuario),
                eq("password", password)
            )).first();

            System.out.println("Enviando respuesta de login...");
            
            synchronized (salida) {
                if (usuarioDoc != null) {
                    System.out.println("Usuario encontrado, enviando LOGIN_SUCCESS");
                    salida.writeInt(Protocolo.LOGIN_RESPONSE);
                    salida.writeInt(Protocolo.LOGIN_SUCCESS);
                    System.out.println("Respuesta LOGIN_SUCCESS enviada");
                } else {
                    System.out.println("Usuario no encontrado, enviando LOGIN_FAILED");
                    salida.writeInt(Protocolo.LOGIN_RESPONSE);
                    salida.writeInt(Protocolo.LOGIN_FAILED);
                    System.out.println("Respuesta LOGIN_FAILED enviada");
                }
                salida.flush();
                System.out.println("Respuesta enviada y flush completado");
            }
            
        } catch (Exception e) {
            System.err.println("Error al procesar login: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al procesar login: " + e.getMessage());
        }
    }

    private void procesarRegistro(Usuario usuario) {
        ServicioUsuarios servicioUsuarios=new ServicioUsuarios();
        servicioUsuarios.guardarUsuario(usuario);
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

    private void procesarCrearPropietario() {
        MongoDatabase database = GestorConexion.conectarClinica();
        MongoCollection<Document> propietarios = database.getCollection("propietarios");
        Document propietario = new Document();
        propietario.append("nombre", "Juan Perez");
        propietario.append("telefono", "123456789");
        propietario.append("email", "juan.perez@example.com");
        propietarios.insertOne(propietario);
    }
} 