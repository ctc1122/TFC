package com.example.pruebamongodbcss.Servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import Utilidades.GestorConexion;

public class ClienteHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    //Declaro los servicios
    private ServicioUsuarios servicioUsuarios;
    private ServicioClinica servicioClinica;

    
    //Declaro el constructor
    public ClienteHandler(Socket socket) {
        this.clientSocket = socket;
        this.servicioUsuarios = new ServicioUsuarios();
        this.servicioClinica = new ServicioClinica();
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
                    System.out.println("GET_USER_REQUEST valor: " + Protocolo.GET_USER_REQUEST);
                    System.out.println("¿Código == GET_USER_REQUEST? " + (codigo == Protocolo.GET_USER_REQUEST));
                    
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
                                Usuario nuevoUsuario = (Usuario) entrada.readObject();
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
                        case Protocolo.GET_USER_REQUEST:
                            System.out.println("Procesando solicitud de obtener usuario...");
                            if (parametros.length >= 2) {
                                Usuario usuario = procesarGetUserConectado(parametros[0], parametros[1]);
                                synchronized (salida) {
                                    if (usuario != null) {
                                        salida.writeInt(Protocolo.GET_USER_RESPONSE);
                                        salida.writeObject(usuario);
                                        salida.flush();
                                        System.out.println("Usuario encontrado y enviado");
                                    } else {
                                        salida.writeInt(Protocolo.ERRORGET_USER);
                                        salida.flush();
                                        System.out.println("Usuario no encontrado, enviando error");
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud GET_USER");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORGET_USER);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENERPACIENTE_POR_ID:
                            System.out.println("Procesando solicitud de obtener paciente por ID...");
                            if (parametros.length >= 1) {
                                ModeloPaciente paciente = procesarObtenerPacientePorId(parametros[0]);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENERPACIENTE_POR_ID_RESPONSE);
                                    salida.writeObject(paciente);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENERPACIENTE_POR_ID");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROROBTENERPACIENTE_POR_ID);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.CREARPACIENTE:
                            System.out.println("Procesando solicitud de crear paciente...");
                            ModeloPaciente paciente = (ModeloPaciente) entrada.readObject();
                            if (paciente != null) {
                                procesarCrearPaciente(paciente);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.CREARPACIENTE_RESPONSE);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud CREARPACIENTE");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORCREARPACIENTE);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.ACTUALIZARPACIENTE:
                            System.out.println("Procesando solicitud de actualizar paciente...");
                            ModeloPaciente pacienteActualizado = (ModeloPaciente) entrada.readObject();
                            if (pacienteActualizado != null) {
                                procesarActualizarPaciente(pacienteActualizado);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ACTUALIZAREVENTOS_RESPONSE);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud ACTUALIZARPACIENTE");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORACTUALIZARPACIENTE);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENERPROPIETARIO_POR_ID:
                            System.out.println("Procesando solicitud de obtener propietario por ID...");
                            if (parametros.length >= 1) {
                                ModeloPropietario propietario = procesarObtenerPropietarioPorId(parametros[0]);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENERPROPIETARIO_POR_ID_RESPONSE);
                                    salida.writeObject(propietario);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENERPROPIETARIO_POR_ID");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROROBTENERPROPIETARIO_POR_ID);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.CREARPROPIETARIO:
                            System.out.println("Procesando solicitud de crear propietario...");
                            ModeloPropietario propietario = (ModeloPropietario) entrada.readObject();
                            if (propietario != null) {
                                ObjectId id = procesarGuardarPropietario(propietario);
                                propietario.setId(id);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.CREARPROPIETARIO_RESPONSE);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud CREARPROPIETARIO");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROPROPIETARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.ACTUALIZARPROPIETARIO:
                            System.out.println("Procesando solicitud de actualizar propietario...");
                            ModeloPropietario propietarioActualizado = (ModeloPropietario) entrada.readObject();
                            if (propietarioActualizado != null) {
                                servicioClinica.actualizarPropietario(propietarioActualizado);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ACTUALIZARPROPIETARIO_RESPONSE);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud ACTUALIZARPROPIETARIO");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORACTUALIZARPROPIETARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENER_TODOS_PACIENTES:
                            System.out.println("Procesando solicitud de obtener todos los pacientes...");
                            List<ModeloPaciente> pacientes = servicioClinica.obtenerTodosPacientes();
                            if (pacientes != null) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_TODOS_PACIENTES_RESPONSE);
                                    salida.writeObject(pacientes);
                                    salida.flush();
                                    System.out.println("Todos los pacientes enviados");
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENER_TODOS_PACIENTES");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROROBTENER_TODOS_PACIENTES);
                                    salida.flush();
                                }

                            }
                            break;

                        default:
                            System.out.println("Mensaje no reconocido: " + codigo);
                    }
                } catch (IOException e) {
                    if (clientSocket.isClosed()) {
                        System.out.println("Cliente desconectado normalmente");
                        break;
                    }
                    if (e instanceof java.io.EOFException) {
                        System.out.println("Conexión cerrada por el cliente");
                        break;
                    }
                    System.err.println("Error al procesar mensaje: " + e.getMessage());
                    e.printStackTrace();
                    break;
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



    private Usuario procesarGetUserConectado(String usuario, String password) {
        MongoCollection<Document> usuarios = GestorConexion.conectarEmpresa().getCollection("usuarios");
        Document usuarioDoc = usuarios.find(and(
            eq("usuario", usuario),
            eq("password", password)
        )).first();


        if (usuarioDoc != null) {
            return new Usuario(usuarioDoc);
        }else{
            return null;
        }
    }

    /*METODOS DE CLINICA CONTROLADOS POR EL SERVIDOR*/

    private ModeloPaciente procesarObtenerPacientePorId(String parametro) {
        return servicioClinica.obtenerPacientePorId(new ObjectId(parametro));
    }

    private void procesarCrearPaciente(ModeloPaciente paciente) {
        servicioClinica.agregarPaciente(paciente);
    }

    private void procesarActualizarPaciente(ModeloPaciente paciente) {
        servicioClinica.actualizarPaciente(paciente);
    }

    private ModeloPropietario procesarObtenerPropietarioPorId(String parametro) {
        return servicioClinica.obtenerPropietarioPorId(new ObjectId(parametro));
    }

    private ObjectId procesarGuardarPropietario(ModeloPropietario propietario) {
        return servicioClinica.guardarPropietario(propietario);
    }   
} 

