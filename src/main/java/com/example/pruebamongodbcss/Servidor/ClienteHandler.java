package com.example.pruebamongodbcss.Servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Data.Usuario.Rol;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloDiagnostico;
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
                                    salida.writeInt(Protocolo.ACTUALIZARPACIENTE_RESPONSE);
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
                                System.out.println("Pacientes obtenidos: " + pacientes.size());
                                
                                // Verificar si los pacientes son serializables
                                for (int i = 0; i < pacientes.size(); i++) {
                                    ModeloPaciente p = pacientes.get(i);
                                    System.out.println("Paciente " + i + ": " + (p != null ? p.getNombre() : "null"));
                                    
                                    // Verificar campos problemáticos
                                    if (p != null) {
                                        System.out.println("  - ID: " + p.getId());
                                        System.out.println("  - PropietarioId: " + p.getPropietarioId());
                                        System.out.println("  - Vacunas: " + (p.getVacunas() != null ? p.getVacunas().size() : "null"));
                                        System.out.println("  - Alergias: " + (p.getAlergias() != null ? p.getAlergias().size() : "null"));
                                    }
                                }
                                
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_TODOS_PACIENTES_RESPONSE);
                                    System.out.println("Enviando lista de pacientes...");
                                    salida.writeObject(pacientes);
                                    salida.flush();
                                    System.out.println("Todos los pacientes enviados correctamente");
                                }
                            } else {
                                System.err.println("Error: Lista de pacientes es null");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROROBTENER_TODOS_PACIENTES);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENER_TODOS_PROPIETARIOS:
                            System.out.println("Procesando solicitud de obtener todos los propietarios...");
                            List<ModeloPropietario> propietarios = servicioClinica.obtenerTodosPropietarios();
                            if (propietarios != null) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_TODOS_PROPIETARIOS_RESPONSE);
                                    salida.writeObject(propietarios);
                                    salida.flush();
                                    System.out.println("Todos los propietarios enviados");
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENER_TODOS_PROPIETARIOS");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROROBTENER_TODOS_PROPIETARIOS);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.BUSCAR_DIAGNOSTICOS_POR_FECHA:
                            System.out.println("Procesando solicitud de obtener todos los diagnosticos...");
                            Date fechaInicio = (Date) entrada.readObject();
                            Date fechaFin = (Date) entrada.readObject();
                            List<ModeloDiagnostico> diagnosticos = servicioClinica.buscarDiagnosticosPorFecha(fechaInicio, fechaFin);
                            if (diagnosticos != null) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.BUSCAR_DIAGNOSTICOS_POR_FECHA_RESPONSE);
                                    salida.writeObject(diagnosticos);
                                    salida.flush();
                                    System.out.println("Todos los diagnosticos enviados");
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud BUSCAR_DIAGNOSTICOS_POR_FECHA");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORBUSCAR_DIAGNOSTICOS_POR_FECHA);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.BUSCAR_PACIENTES_POR_NOMBRE:
                            System.out.println("Procesando solicitud de obtener pacientes por nombre...");
                            String nombre = (String) entrada.readObject();
                            List<ModeloPaciente> pacientes2 = servicioClinica.buscarPacientesPorNombre(nombre);
                            if (pacientes2 != null) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.BUSCAR_PACIENTES_POR_NOMBRE_RESPONSE);
                                    salida.writeObject(pacientes2);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se encontraron pacientes con el nombre: " + nombre);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORBUSCAR_PACIENTES_POR_NOMBRE);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.BUSCAR_PROPIETARIOS_POR_NOMBRE:
                            System.out.println("Procesando solicitud de obtener propietarios por nombre...");
                            String nombrePropietario = parametros[0];
                            List<ModeloPropietario> propietarios2 = servicioClinica.buscarPropietariosPorNombre(nombrePropietario);
                            if (propietarios2 != null) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.BUSCAR_PROPIETARIOS_POR_NOMBRE_RESPONSE);
                                    salida.writeObject(propietarios2);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se encontraron propietarios con el nombre: " + nombrePropietario);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORBUSCAR_PROPIETARIOS_POR_NOMBRE);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.ELIMINARPACIENTE:
                            System.out.println("Procesando solicitud de eliminar paciente...");
                            String idPaciente = parametros[0];
                            boolean eliminado = servicioClinica.eliminarPaciente(new ObjectId(idPaciente));
                            if (eliminado) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ELIMINARPACIENTE_RESPONSE);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se pudo eliminar el paciente");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORELIMINARPACIENTE);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.BUSCAR_DIAGNOSTICOS_POR_PACIENTE:
                            System.out.println("Procesando solicitud de obtener diagnosticos por paciente...");
                            String idPaciente2 = parametros[0];
                            List<ModeloDiagnostico> diagnosticos2 = servicioClinica.buscarDiagnosticosPorPaciente(new ObjectId(idPaciente2));
                            if (diagnosticos2 != null) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.BUSCAR_DIAGNOSTICOS_POR_PACIENTE_RESPONSE);
                                    salida.writeObject(diagnosticos2);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se encontraron diagnosticos para el paciente: " + idPaciente2);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORBUSCAR_DIAGNOSTICOS_POR_PACIENTE);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.ELIMINARPROPIETARIO:
                            System.out.println("Procesando solicitud de eliminar propietario...");
                            String idPropietario = parametros[0];
                            boolean eliminadoPropietario = servicioClinica.eliminarPropietario(new ObjectId(idPropietario));
                            if (eliminadoPropietario) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ELIMINARPROPIETARIO_RESPONSE);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se pudo eliminar el propietario");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROPROPIETARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.BUSCAR_PACIENTES_POR_PROPIETARIO:
                            System.out.println("Procesando solicitud de obtener pacientes por propietario...");
                            String idPropietario2 = parametros[0];
                            List<ModeloPaciente> pacientes3 = servicioClinica.buscarPacientesPorPropietario(new ObjectId(idPropietario2));
                            if (pacientes3 != null) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.BUSCAR_PACIENTES_POR_PROPIETARIO_RESPONSE);
                                    salida.writeObject(pacientes3);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se encontraron pacientes para el propietario: " + idPropietario2);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORBUSCAR_PACIENTES_POR_PROPIETARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.ELIMINARDIAGNOSTICO:
                            System.out.println("Procesando solicitud de eliminar diagnostico...");
                            String idDiagnostico = parametros[0];
                            boolean eliminadoDiagnostico = servicioClinica.eliminarDiagnostico(new ObjectId(idDiagnostico));
                            if (eliminadoDiagnostico) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ELIMINARDIAGNOSTICO_RESPONSE);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se pudo eliminar el diagnostico");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRODIAGNOSTICO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.CREARPACIENTE_DEVUELVEPACIENTE:
                            System.out.println("Procesando solicitud de crear paciente y devolver paciente...");
                            ModeloPaciente pacienteDevuelto = (ModeloPaciente) entrada.readObject();
                            if (pacienteDevuelto != null) {
                                boolean guardado = procesarGuardarPaciente(pacienteDevuelto);
                                if (guardado) {
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.CREARPACIENTE_DEVUELVEPACIENTE_RESPONSE);
                                        salida.writeObject(pacienteDevuelto);
                                        salida.flush();
                                    }
                                }
                                else {
                                    salida.writeInt(Protocolo.ERRORCREARPACIENTE_DEVUELVEPACIENTE);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.SETUSERCONECTADO:
                            System.out.println("Procesando solicitud de establecer usuario conectado...");
                            Usuario usuario = (Usuario) entrada.readObject();
                            if (usuario != null) {
                                servicioUsuarios.setUsuarioActual(usuario);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.SETUSERCONECTADO_RESPONSE);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud SETUSERCONECTADO");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORSETUSERCONECTADO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.GETALLUSERS:
                            System.out.println("Procesando solicitud de obtener todos los usuarios...");
                            List<Usuario> usuarios = servicioUsuarios.obtenerTodosUsuarios();
                            if (usuarios != null) { 
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.GETALLUSERS_RESPONSE);
                                    salida.writeObject(usuarios);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se encontraron usuarios");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORGETALLUSERS);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.GETALLVETERINARIOS:
                            System.out.println("Procesando solicitud de obtener todos los veterinarios...");
                            Rol rol = (Rol) entrada.readObject();
                            System.out.println("Rol: " + rol);
                            List<Usuario> veterinarios = servicioUsuarios.buscarUsuariosPorRol(rol);
                            if (veterinarios != null) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.GETALLVETERINARIOS_RESPONSE);
                                    salida.writeObject(veterinarios);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se encontraron veterinarios");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORGETALLVETERINARIOS);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.DELETEUSER:
                            System.out.println("Procesando solicitud de eliminar usuario...");
                            String idUsuario = parametros[0];
                            boolean eliminadoUsuario = servicioUsuarios.eliminarUsuario(new ObjectId(idUsuario));
                            if (eliminadoUsuario) {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.DELETEUSER_RESPONSE);
                                    salida.flush();
                                }
                            }
                            else {
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORDELETEUSER);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.RESETPASSWORD:
                            System.out.println("Procesando solicitud de resetear contraseña...");
                            if (parametros.length >= 2) {
                                String idUsuarioReset = parametros[0];
                                String nuevaContrasena = parametros[1];
                                boolean reseteado = servicioUsuarios.resetearContrasena(new ObjectId(idUsuarioReset), nuevaContrasena);
                                if (reseteado) {
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.RESETPASSWORD_RESPONSE);
                                        salida.flush();
                                    }
                                } else {
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERRORRESETPASSWORD);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud RESETPASSWORD");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERRORRESETPASSWORD);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.CARGAR_DATOS_PRUEBA:
                            System.out.println("Procesando solicitud de cargar datos de prueba...");
                            try {
                                servicioUsuarios.cargarDatosPrueba();
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.CARGAR_DATOS_PRUEBA_RESPONSE);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al cargar datos de prueba: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_CARGAR_DATOS_PRUEBA);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.RECONECTAR_DB:
                            System.out.println("Procesando solicitud de reconectar base de datos...");
                            try {
                                servicioUsuarios.reiniciarConexion();
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.RECONECTAR_DB_RESPONSE);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al reconectar base de datos: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_RECONECTAR_DB);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.GUARDAR_USUARIO:
                            System.out.println("Procesando solicitud de guardar usuario...");
                            try {
                                Usuario usuarioAGuardar = (Usuario) entrada.readObject();
                                if (usuarioAGuardar != null) {
                                    servicioUsuarios.guardarUsuario(usuarioAGuardar);
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.GUARDAR_USUARIO_RESPONSE);
                                        salida.flush();
                                    }
                                } else {
                                    System.err.println("Error: Usuario recibido es null");
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_GUARDAR_USUARIO);
                                        salida.flush();
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error al guardar usuario: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_GUARDAR_USUARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.VERIFICAR_USUARIO_EXISTE:
                            System.out.println("Procesando solicitud de verificar si usuario existe...");
                            if (parametros.length >= 1) {
                                String nombreUsuario = parametros[0];
                                try {
                                    boolean existe = servicioUsuarios.existeUsuario(nombreUsuario);
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.VERIFICAR_USUARIO_EXISTE_RESPONSE);
                                        salida.writeBoolean(existe);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al verificar si usuario existe: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_VERIFICAR_USUARIO_EXISTE);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud VERIFICAR_USUARIO_EXISTE");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_VERIFICAR_USUARIO_EXISTE);
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

    private boolean  procesarGuardarPaciente(ModeloPaciente pacienteDevuelto) {
        return servicioClinica.agregarPaciente(pacienteDevuelto);
    }
} 

