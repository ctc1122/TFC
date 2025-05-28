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
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloDiagnostico;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica;
import com.example.pruebamongodbcss.Modulos.Fichaje.ModeloFichaje;
import com.example.pruebamongodbcss.Modulos.Fichaje.ResumenFichaje;
import com.example.pruebamongodbcss.Modulos.Fichaje.ServicioFichaje;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.calendar.CalendarService;
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
    private CalendarService calendarService;
    private com.example.pruebamongodbcss.Modulos.Facturacion.ServicioFacturacion servicioFacturacion;
    private ServicioFichaje servicioFichaje;
    
    //Declaro el constructor
    public ClienteHandler(Socket socket) {
        this.clientSocket = socket;
        this.servicioUsuarios = new ServicioUsuarios();
        this.servicioClinica = new ServicioClinica();
        this.calendarService = new CalendarService();
        this.servicioFacturacion = new com.example.pruebamongodbcss.Modulos.Facturacion.ServicioFacturacion();
        this.servicioFichaje = new ServicioFichaje();
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
                        parametros = partes[1].split(":");  // Usar SEPARADOR_PARAMETROS (:) para separar parámetros
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
                                    salida.writeObject(id); // Enviar el ID de vuelta al cliente
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
                        case Protocolo.DAMETODASLASCITAS:
                            System.out.println("Procesando solicitud de obtener todas las citas...");
                            List<com.example.pruebamongodbcss.calendar.CalendarEvent> citas = calendarService.getAllAppointments();
                            if(citas != null){
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.DAMETODASLASCITAS_RESPONSE);
                                    salida.writeObject(citas);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: No se encontraron citas");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_DAMETODASLASCITAS);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.GUARDAR_EVENTO_CALENDARIO:
                            System.out.println("Procesando solicitud de guardar evento de calendario...");
                            try {
                                com.example.pruebamongodbcss.calendar.CalendarEvent eventoAGuardar = 
                                    (com.example.pruebamongodbcss.calendar.CalendarEvent) entrada.readObject();
                                com.example.pruebamongodbcss.calendar.CalendarEvent eventoGuardado = 
                                    calendarService.saveAppointment(eventoAGuardar);
                                if (eventoGuardado != null) {
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.GUARDAR_EVENTO_CALENDARIO_RESPONSE);
                                        salida.writeObject(eventoGuardado);
                                        salida.flush();
                                    }
                                } else {
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_GUARDAR_EVENTO_CALENDARIO);
                                        salida.flush();
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error al guardar evento: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_GUARDAR_EVENTO_CALENDARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.ACTUALIZAR_EVENTO_CALENDARIO:
                            System.out.println("Procesando solicitud de actualizar evento de calendario...");
                            try {
                                com.example.pruebamongodbcss.calendar.CalendarEvent eventoAActualizar = 
                                    (com.example.pruebamongodbcss.calendar.CalendarEvent) entrada.readObject();
                                boolean actualizado = calendarService.updateAppointment(eventoAActualizar);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ACTUALIZAR_EVENTO_CALENDARIO_RESPONSE);
                                    salida.writeBoolean(actualizado);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al actualizar evento: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_ACTUALIZAR_EVENTO_CALENDARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.ELIMINAR_EVENTO_CALENDARIO:
                            System.out.println("Procesando solicitud de eliminar evento de calendario...");
                            if (parametros.length >= 1) {
                                String idEvento = parametros[0];
                                boolean eliminadoEvento = calendarService.deleteAppointment(idEvento);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ELIMINAR_EVENTO_CALENDARIO_RESPONSE);
                                    salida.writeBoolean(eliminadoEvento);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud ELIMINAR_EVENTO_CALENDARIO");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_ELIMINAR_EVENTO_CALENDARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENER_EVENTO_POR_ID:
                            System.out.println("Procesando solicitud de obtener evento por ID...");
                            if (parametros.length >= 1) {
                                String idEvento = parametros[0];
                                com.example.pruebamongodbcss.calendar.CalendarEvent evento = calendarService.getEventById(idEvento);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_EVENTO_POR_ID_RESPONSE);
                                    salida.writeObject(evento);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENER_EVENTO_POR_ID");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_EVENTO_POR_ID);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENER_EVENTOS_POR_USUARIO:
                            System.out.println("Procesando solicitud de obtener eventos por usuario...");
                            if (parametros.length >= 1) {
                                String nombreUsuario = parametros[0];
                                List<com.example.pruebamongodbcss.calendar.CalendarEvent> eventosUsuario = 
                                    calendarService.getAppointmentsByUser(nombreUsuario);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_EVENTOS_POR_USUARIO_RESPONSE);
                                    salida.writeObject(eventosUsuario);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENER_EVENTOS_POR_USUARIO");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_EVENTOS_POR_USUARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENER_CITAS_POR_USUARIO:
                            System.out.println("Procesando solicitud de obtener citas médicas por usuario...");
                            if (parametros.length >= 1) {
                                try {
                                    String nombreUsuarioCitas = parametros[0];
                                    System.out.println("Obteniendo citas para usuario: " + nombreUsuarioCitas);
                                    List<com.example.pruebamongodbcss.calendar.CalendarEvent> citasUsuario = 
                                        calendarService.getsoloCitasporUsuario(nombreUsuarioCitas);
                                    System.out.println("Se encontraron " + citasUsuario.size() + " citas para el usuario: " + nombreUsuarioCitas);
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.OBTENER_CITAS_POR_USUARIO_RESPONSE);
                                        salida.writeObject(citasUsuario);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al obtener citas por usuario: " + e.getMessage());
                                    e.printStackTrace();
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_OBTENER_CITAS_POR_USUARIO);
                                        salida.writeUTF("Error al obtener citas: " + e.getMessage());
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENER_CITAS_POR_USUARIO");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_CITAS_POR_USUARIO);
                                    salida.writeUTF("Faltan parámetros en la solicitud");
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.PROBAR_VERIFICACION_AUTOMATICA:
                            System.out.println("🧪 Procesando solicitud de prueba de verificación automática...");
                            try {
                                int citasActualizadas = calendarService.probarVerificacionAutomatica();
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.PROBAR_VERIFICACION_AUTOMATICA_RESPONSE);
                                    salida.writeInt(citasActualizadas);
                                    salida.flush();
                                }
                                System.out.println("✅ Prueba de verificación automática completada. " + citasActualizadas + " citas actualizadas.");
                            } catch (Exception e) {
                                System.err.println("❌ Error en prueba de verificación automática: " + e.getMessage());
                                e.printStackTrace();
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_PROBAR_VERIFICACION_AUTOMATICA);
                                    salida.writeUTF("Error: " + e.getMessage());
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENER_RESUMEN_EVENTOS_USUARIO:
                            System.out.println("Procesando solicitud de obtener resumen de eventos por usuario...");
                            if (parametros.length >= 1) {
                                String nombreUsuarioResumen = parametros[0];
                                try {
                                    // Obtener el resumen del servicio
                                    com.example.pruebamongodbcss.calendar.CalendarService.EventSummary summaryInterno = 
                                        calendarService.getEventSummaryForUser(nombreUsuarioResumen);
                                    
                                    // Convertir a la clase serializable
                                    com.example.pruebamongodbcss.calendar.EventSummary summarySerializable = 
                                        new com.example.pruebamongodbcss.calendar.EventSummary(
                                            summaryInterno.getMeetings(),
                                            summaryInterno.getReminders(),
                                            summaryInterno.getAppointments()
                                        );
                                    
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.OBTENER_RESUMEN_EVENTOS_USUARIO_RESPONSE);
                                        salida.writeObject(summarySerializable);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al obtener resumen de eventos: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_OBTENER_RESUMEN_EVENTOS_USUARIO);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENER_RESUMEN_EVENTOS_USUARIO");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_RESUMEN_EVENTOS_USUARIO);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.BUSCAR_CITAS_POR_PACIENTE:
                            System.out.println("Procesando solicitud de buscar citas por paciente...");
                            if (parametros.length >= 1) {
                                String idPacienteCitas = parametros[0];
                                List<ModeloCita> citasPaciente = servicioClinica.buscarCitasPorPaciente(new ObjectId(idPacienteCitas));
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.BUSCAR_CITAS_POR_PACIENTE_RESPONSE);
                                    salida.writeObject(citasPaciente);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud BUSCAR_CITAS_POR_PACIENTE");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_BUSCAR_CITAS_POR_PACIENTE);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.OBTENER_CITA_POR_ID:
                            System.out.println("Procesando solicitud de obtener cita por ID...");
                            if (parametros.length >= 1) {
                                String idCita = parametros[0];
                                ModeloCita cita = servicioClinica.obtenerCitaPorId(new ObjectId(idCita));
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_CITA_POR_ID_RESPONSE);
                                    salida.writeObject(cita);
                                    salida.flush();
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENER_CITA_POR_ID");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_CITA_POR_ID);
                                    salida.flush();
                                }
                            }
                            break;
                        case Protocolo.GUARDAR_DIAGNOSTICO:
                            System.out.println("Procesando solicitud de guardar diagnóstico...");
                            try {
                                ModeloDiagnostico diagnosticoAGuardar = (ModeloDiagnostico) entrada.readObject();
                                ObjectId idDiagnosticoGuardado = servicioClinica.guardarDiagnostico(diagnosticoAGuardar);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.GUARDAR_DIAGNOSTICO_RESPONSE);
                                    salida.writeObject(idDiagnosticoGuardado);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al guardar diagnóstico: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_GUARDAR_DIAGNOSTICO);
                                    salida.flush();
                                }
                            }
                            break;
                        
                        // Casos de facturación
                        case Protocolo.CREAR_FACTURA:
                            System.out.println("Procesando solicitud de crear factura...");
                            try {
                                com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura facturaACrear = 
                                    (com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura) entrada.readObject();
                                ObjectId idFacturaCreada = servicioFacturacion.guardarFactura(facturaACrear);
                                facturaACrear.setId(idFacturaCreada);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.CREAR_FACTURA_RESPONSE);
                                    salida.writeObject(facturaACrear);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al crear factura: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_CREAR_FACTURA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_TODAS_FACTURAS:
                            System.out.println("Procesando solicitud de obtener todas las facturas...");
                            try {
                                List<com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura> facturas = 
                                    servicioFacturacion.obtenerTodasFacturas();
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_TODAS_FACTURAS_RESPONSE);
                                    salida.writeObject(facturas);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener facturas: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_TODAS_FACTURAS);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_FACTURA_POR_ID:
                            System.out.println("Procesando solicitud de obtener factura por ID...");
                            if (parametros.length >= 1) {
                                try {
                                    String idFactura = parametros[0];
                                    com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura factura = 
                                        servicioFacturacion.obtenerFacturaPorId(new ObjectId(idFactura));
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.OBTENER_FACTURA_POR_ID_RESPONSE);
                                        salida.writeObject(factura);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al obtener factura por ID: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_OBTENER_FACTURA_POR_ID);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud OBTENER_FACTURA_POR_ID");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_FACTURA_POR_ID);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.ACTUALIZAR_FACTURA:
                            System.out.println("Procesando solicitud de actualizar factura...");
                            try {
                                com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura facturaAActualizar = 
                                    (com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura) entrada.readObject();
                                boolean actualizada = servicioFacturacion.actualizarFactura(facturaAActualizar);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ACTUALIZAR_FACTURA_RESPONSE);
                                    salida.writeBoolean(actualizada);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al actualizar factura: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_ACTUALIZAR_FACTURA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.ELIMINAR_FACTURA:
                            System.out.println("Procesando solicitud de eliminar factura...");
                            if (parametros.length >= 1) {
                                try {
                                    String idFactura = parametros[0];
                                    boolean eliminada = servicioFacturacion.eliminarFactura(new ObjectId(idFactura));
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ELIMINAR_FACTURA_RESPONSE);
                                        salida.writeBoolean(eliminada);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al eliminar factura: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_ELIMINAR_FACTURA);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud ELIMINAR_FACTURA");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_ELIMINAR_FACTURA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.BUSCAR_FACTURAS_POR_CLIENTE:
                            System.out.println("Procesando solicitud de buscar facturas por cliente...");
                            if (parametros.length >= 1) {
                                try {
                                    String idCliente = parametros[0];
                                    List<com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura> facturas = 
                                        servicioFacturacion.buscarFacturasPorCliente(new ObjectId(idCliente));
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.BUSCAR_FACTURAS_POR_CLIENTE_RESPONSE);
                                        salida.writeObject(facturas);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al buscar facturas por cliente: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_BUSCAR_FACTURAS_POR_CLIENTE);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud BUSCAR_FACTURAS_POR_CLIENTE");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_BUSCAR_FACTURAS_POR_CLIENTE);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.BUSCAR_FACTURAS_POR_FECHA:
                            System.out.println("Procesando solicitud de buscar facturas por fecha...");
                            try {
                                Date fechaInicioFacturas = (Date) entrada.readObject();
                                Date fechaFinFacturas = (Date) entrada.readObject();
                                List<com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura> facturas = 
                                    servicioFacturacion.buscarFacturasPorFecha(fechaInicioFacturas, fechaFinFacturas);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.BUSCAR_FACTURAS_POR_FECHA_RESPONSE);
                                    salida.writeObject(facturas);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al buscar facturas por fecha: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_BUSCAR_FACTURAS_POR_FECHA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.FINALIZAR_FACTURA:
                            System.out.println("Procesando solicitud de finalizar factura...");
                            if (parametros.length >= 1) {
                                try {
                                    String idFactura = parametros[0];
                                    boolean finalizada = servicioFacturacion.finalizarFactura(new ObjectId(idFactura));
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.FINALIZAR_FACTURA_RESPONSE);
                                        salida.writeBoolean(finalizada);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al finalizar factura: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_FINALIZAR_FACTURA);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud FINALIZAR_FACTURA");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_FINALIZAR_FACTURA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_FACTURAS_BORRADOR:
                            System.out.println("Procesando solicitud de obtener facturas borrador...");
                            try {
                                List<com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura> borradores = 
                                    servicioFacturacion.obtenerFacturasBorrador();
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_FACTURAS_BORRADOR_RESPONSE);
                                    salida.writeObject(borradores);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener facturas borrador: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_FACTURAS_BORRADOR);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR:
                            System.out.println("Procesando solicitud de cambiar estado de cita a pendiente facturar...");
                            if (parametros.length >= 1) {
                                try {
                                    String idCita = parametros[0];
                                    boolean cambiado = servicioFacturacion.cambiarEstadoCitaPendienteFacturar(new ObjectId(idCita));
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR_RESPONSE);
                                        salida.writeBoolean(cambiado);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al cambiar estado de cita: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.CAMBIAR_ESTADO_CITA:
                            System.out.println("Procesando solicitud de cambiar estado de cita...");
                            if (parametros.length >= 2) {
                                try {
                                    String idCita = parametros[0];
                                    String nuevoEstado = parametros[1];
                                    System.out.println("Cambiando estado de cita " + idCita + " a " + nuevoEstado);
                                    
                                    // 1. Cambiar estado en la colección de citas
                                    boolean cambiado = servicioClinica.cambiarEstadoCita(idCita, nuevoEstado);
                                    
                                    // 2. Actualizar el evento correspondiente en el calendario si existe
                                    if (cambiado) {
                                        try {
                                            // Buscar el evento del calendario correspondiente
                                            com.example.pruebamongodbcss.calendar.CalendarEvent evento = calendarService.getEventById(idCita);
                                            if (evento != null) {
                                                // Actualizar el estado en el título del evento
                                                String tituloActual = evento.getTitle();
                                                if (tituloActual != null) {
                                                    // Extraer el motivo (parte antes del " - ")
                                                    String motivo = tituloActual.contains(" - ") ? 
                                                        tituloActual.substring(0, tituloActual.indexOf(" - ")) : 
                                                        tituloActual;
                                                    
                                                    // Crear nuevo título con el estado actualizado
                                                    String nuevoTitulo = motivo + " - " + nuevoEstado;
                                                    evento.setTitle(nuevoTitulo);
                                                    
                                                    // Actualizar el estado en el evento
                                                    evento.setEstado(nuevoEstado);
                                                    
                                                    // Guardar el evento actualizado
                                                    boolean eventoActualizado = calendarService.updateAppointment(evento);
                                                    if (eventoActualizado) {
                                                        System.out.println("Evento del calendario actualizado correctamente");
                                                    } else {
                                                        System.out.println("Advertencia: No se pudo actualizar el evento del calendario");
                                                    }
                                                }
                                            } else {
                                                System.out.println("No se encontró evento del calendario correspondiente para la cita: " + idCita);
                                            }
                                        } catch (Exception eventoException) {
                                            System.err.println("Error al actualizar evento del calendario: " + eventoException.getMessage());
                                            // No fallar la operación completa por esto
                                        }
                                    }
                                    
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.CAMBIAR_ESTADO_CITA_RESPONSE);
                                        salida.writeBoolean(cambiado);
                                        salida.flush();
                                    }
                                    System.out.println("Estado de cita cambiado: " + cambiado);
                                } catch (Exception e) {
                                    System.err.println("Error al cambiar estado de cita: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_CAMBIAR_ESTADO_CITA);
                                        salida.writeUTF("Error al cambiar estado: " + e.getMessage());
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud CAMBIAR_ESTADO_CITA");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_CAMBIAR_ESTADO_CITA);
                                    salida.writeUTF("Faltan parámetros: se requiere ID de cita y nuevo estado");
                                    salida.flush();
                                }
                            }
                            break;
                            
                        // Casos de fichaje
                        case Protocolo.FICHAR_ENTRADA:
                            System.out.println("Procesando solicitud de fichar entrada...");
                            try {
                                ObjectId empleadoId = new ObjectId(parametros[0]);
                                String nombreEmpleado = parametros[1];
                                String usuarioEmpleado = parametros[2];
                                ModeloFichaje.TipoFichaje tipo = ModeloFichaje.TipoFichaje.valueOf(parametros[3]);
                                String motivoIncidencia = parametros.length > 4 ? parametros[4] : null;
                                
                                ModeloFichaje fichaje = servicioFichaje.ficharEntrada(empleadoId, nombreEmpleado, usuarioEmpleado, tipo, motivoIncidencia);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.FICHAR_ENTRADA_RESPONSE);
                                    salida.writeObject(fichaje);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al fichar entrada: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_FICHAR_ENTRADA);
                                    salida.writeUTF(e.getMessage());
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.FICHAR_SALIDA:
                            System.out.println("Procesando solicitud de fichar salida...");
                            try {
                                ObjectId empleadoId = new ObjectId(parametros[0]);
                                ModeloFichaje.TipoFichaje tipo = ModeloFichaje.TipoFichaje.valueOf(parametros[1]);
                                String motivoIncidencia = parametros.length > 2 ? parametros[2] : null;
                                
                                boolean exito = servicioFichaje.ficharSalida(empleadoId, tipo, motivoIncidencia);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.FICHAR_SALIDA_RESPONSE);
                                    salida.writeBoolean(exito);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al fichar salida: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_FICHAR_SALIDA);
                                    salida.writeUTF(e.getMessage());
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_FICHAJE_ABIERTO_HOY:
                            System.out.println("Procesando solicitud de obtener fichaje abierto hoy...");
                            try {
                                ObjectId empleadoId = new ObjectId(parametros[0]);
                                ModeloFichaje fichaje = servicioFichaje.obtenerFichajeAbiertoHoy(empleadoId);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_FICHAJE_ABIERTO_HOY_RESPONSE);
                                    salida.writeObject(fichaje);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener fichaje abierto: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_FICHAJE_ABIERTO_HOY);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_HISTORIAL_FICHAJES:
                            System.out.println("Procesando solicitud de obtener historial de fichajes...");
                            try {
                                ObjectId empleadoId = new ObjectId(parametros[0]);
                                int limite = Integer.parseInt(parametros[1]);
                                List<ModeloFichaje> historial = servicioFichaje.obtenerHistorialEmpleado(empleadoId, limite);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_HISTORIAL_FICHAJES_RESPONSE);
                                    salida.writeObject(historial);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener historial de fichajes: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_HISTORIAL_FICHAJES);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_TODOS_FICHAJES:
                            System.out.println("Procesando solicitud de obtener todos los fichajes...");
                            try {
                                int limite = Integer.parseInt(parametros[0]);
                                List<ModeloFichaje> fichajes = servicioFichaje.obtenerTodosFichajes(limite);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_TODOS_FICHAJES_RESPONSE);
                                    salida.writeObject(fichajes);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener todos los fichajes: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_TODOS_FICHAJES);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_FICHAJES_POR_FECHA:
                            System.out.println("Procesando solicitud de obtener fichajes por fecha...");
                            try {
                                java.time.LocalDate fechaInicioFichajes = (java.time.LocalDate) entrada.readObject();
                                java.time.LocalDate fechaFinFichajes = (java.time.LocalDate) entrada.readObject();
                                List<ModeloFichaje> fichajes = servicioFichaje.obtenerFichajesPorFecha(fechaInicioFichajes, fechaFinFichajes);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_FICHAJES_POR_FECHA_RESPONSE);
                                    salida.writeObject(fichajes);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener fichajes por fecha: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_FICHAJES_POR_FECHA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_FICHAJES_EMPLEADO_POR_FECHA:
                            System.out.println("Procesando solicitud de obtener fichajes de empleado por fecha...");
                            try {
                                ObjectId empleadoId = new ObjectId(parametros[0]);
                                java.time.LocalDate fechaInicioEmpleado = (java.time.LocalDate) entrada.readObject();
                                java.time.LocalDate fechaFinEmpleado = (java.time.LocalDate) entrada.readObject();
                                List<ModeloFichaje> fichajes = servicioFichaje.obtenerFichajesEmpleadoPorFecha(empleadoId, fechaInicioEmpleado, fechaFinEmpleado);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_FICHAJES_EMPLEADO_POR_FECHA_RESPONSE);
                                    salida.writeObject(fichajes);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener fichajes de empleado por fecha: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_FICHAJES_EMPLEADO_POR_FECHA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.GENERAR_RESUMEN_FICHAJES:
                            System.out.println("Procesando solicitud de generar resumen de fichajes...");
                            try {
                                String usuarioEmpleado = parametros[0];
                                java.time.LocalDate fechaInicioResumen = (java.time.LocalDate) entrada.readObject();
                                java.time.LocalDate fechaFinResumen = (java.time.LocalDate) entrada.readObject();
                                ResumenFichaje resumen = servicioFichaje.generarResumenEmpleado(usuarioEmpleado, fechaInicioResumen, fechaFinResumen);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.GENERAR_RESUMEN_FICHAJES_RESPONSE);
                                    salida.writeObject(resumen);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al generar resumen de fichajes: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_GENERAR_RESUMEN_FICHAJES);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_FICHAJES_POR_DIA:
                            System.out.println("Procesando solicitud de obtener fichajes por día...");
                            try {
                                java.time.LocalDate fechaDia = (java.time.LocalDate) entrada.readObject();
                                List<ModeloFichaje> fichajes = servicioFichaje.obtenerFichajesPorDia(fechaDia);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_FICHAJES_POR_DIA_RESPONSE);
                                    salida.writeObject(fichajes);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener fichajes por día: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_FICHAJES_POR_DIA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_ESTADISTICAS_FICHAJES:
                            System.out.println("Procesando solicitud de obtener estadísticas de fichajes...");
                            try {
                                java.time.LocalDate fechaInicioEstadisticas = (java.time.LocalDate) entrada.readObject();
                                java.time.LocalDate fechaFinEstadisticas = (java.time.LocalDate) entrada.readObject();
                                Document estadisticas = servicioFichaje.obtenerEstadisticasGenerales(fechaInicioEstadisticas, fechaFinEstadisticas);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_ESTADISTICAS_FICHAJES_RESPONSE);
                                    salida.writeObject(estadisticas);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener estadísticas de fichajes: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_ESTADISTICAS_FICHAJES);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.ELIMINAR_FICHAJE:
                            System.out.println("Procesando solicitud de eliminar fichaje...");
                            try {
                                ObjectId fichajeId = new ObjectId(parametros[0]);
                                boolean eliminadoFichaje = servicioFichaje.eliminarFichaje(fichajeId);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ELIMINAR_FICHAJE_RESPONSE);
                                    salida.writeBoolean(eliminadoFichaje);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al eliminar fichaje: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_ELIMINAR_FICHAJE);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.ACTUALIZAR_FICHAJE:
                            System.out.println("Procesando solicitud de actualizar fichaje...");
                            try {
                                ModeloFichaje fichaje = (ModeloFichaje) entrada.readObject();
                                boolean actualizado = servicioFichaje.actualizarFichaje(fichaje);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ACTUALIZAR_FICHAJE_RESPONSE);
                                    salida.writeBoolean(actualizado);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al actualizar fichaje: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_ACTUALIZAR_FICHAJE);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        // Casos para operaciones CRUD de citas
                        case Protocolo.GUARDAR_CITA:
                            System.out.println("Procesando solicitud de guardar cita...");
                            try {
                                ModeloCita citaAGuardar = (ModeloCita) entrada.readObject();
                                ObjectId idCitaGuardada = servicioClinica.guardarCita(citaAGuardar);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.GUARDAR_CITA_RESPONSE);
                                    salida.writeObject(idCitaGuardada);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al guardar cita: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_GUARDAR_CITA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.ACTUALIZAR_CITA:
                            System.out.println("Procesando solicitud de actualizar cita...");
                            try {
                                ModeloCita citaAActualizar = (ModeloCita) entrada.readObject();
                                ObjectId idActualizado = servicioClinica.guardarCita(citaAActualizar);
                                boolean actualizada = (idActualizado != null);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ACTUALIZAR_CITA_RESPONSE);
                                    salida.writeBoolean(actualizada);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al actualizar cita: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_ACTUALIZAR_CITA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.ELIMINAR_CITA:
                            System.out.println("Procesando solicitud de eliminar cita...");
                            if (parametros.length >= 1) {
                                try {
                                    String idCita = parametros[0];
                                    boolean eliminada = servicioClinica.eliminarCita(new ObjectId(idCita));
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ELIMINAR_CITA_RESPONSE);
                                        salida.writeBoolean(eliminada);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al eliminar cita: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_ELIMINAR_CITA);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud ELIMINAR_CITA");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_ELIMINAR_CITA);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.BUSCAR_CITAS_POR_RANGO_FECHAS:
                            System.out.println("Procesando solicitud de buscar citas por rango de fechas...");
                            try {
                                java.time.LocalDate fechaInicioCitas = (java.time.LocalDate) entrada.readObject();
                                java.time.LocalDate fechaFinCitas = (java.time.LocalDate) entrada.readObject();
                                List<ModeloCita> citasRango = servicioClinica.buscarCitasPorRangoFechas(fechaInicioCitas, fechaFinCitas);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.BUSCAR_CITAS_POR_RANGO_FECHAS_RESPONSE);
                                    salida.writeObject(citasRango);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al buscar citas por rango de fechas: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_BUSCAR_CITAS_POR_RANGO_FECHAS);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.HAY_CONFLICTO_HORARIO:
                            System.out.println("Procesando solicitud de verificar conflicto horario...");
                            try {
                                java.time.LocalDateTime fechaHora = (java.time.LocalDateTime) entrada.readObject();
                                int duracionMinutos = entrada.readInt();
                                ObjectId citaId = (ObjectId) entrada.readObject(); // Puede ser null
                                boolean hayConflicto = servicioClinica.hayConflictoHorario(fechaHora, duracionMinutos, citaId);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.HAY_CONFLICTO_HORARIO_RESPONSE);
                                    salida.writeBoolean(hayConflicto);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al verificar conflicto horario: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_HAY_CONFLICTO_HORARIO);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.BUSCAR_RAZAS_POR_TIPO_ANIMAL:
                            System.out.println("Procesando solicitud de buscar razas por tipo de animal...");
                            if (parametros.length >= 1) {
                                try {
                                    String tipoAnimal = parametros[0];
                                    String[] razas = servicioClinica.buscarRazasPorTipoAnimal(tipoAnimal);
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.BUSCAR_RAZAS_POR_TIPO_ANIMAL_RESPONSE);
                                        salida.writeObject(razas);
                                        salida.flush();
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al buscar razas por tipo de animal: " + e.getMessage());
                                    synchronized (salida) {
                                        salida.writeInt(Protocolo.ERROR_BUSCAR_RAZAS_POR_TIPO_ANIMAL);
                                        salida.flush();
                                    }
                                }
                            } else {
                                System.err.println("Error: Faltan parámetros en la solicitud BUSCAR_RAZAS_POR_TIPO_ANIMAL");
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_BUSCAR_RAZAS_POR_TIPO_ANIMAL);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_FACTURAS_POR_ESTADO:
                            System.out.println("Procesando solicitud de obtener facturas por estado...");
                            try {
                                com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura.EstadoFactura estado = 
                                    (com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura.EstadoFactura) entrada.readObject();
                                List<com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura> facturas = 
                                    servicioFacturacion.obtenerFacturasPorEstado(estado);
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_FACTURAS_POR_ESTADO_RESPONSE);
                                    salida.writeObject(facturas);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener facturas por estado: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_FACTURAS_POR_ESTADO);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.OBTENER_FACTURAS_FINALIZADAS:
                            System.out.println("Procesando solicitud de obtener facturas finalizadas...");
                            try {
                                List<com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura> facturas = 
                                    servicioFacturacion.obtenerFacturasFinalizadas();
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.OBTENER_FACTURAS_FINALIZADAS_RESPONSE);
                                    salida.writeObject(facturas);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al obtener facturas finalizadas: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_OBTENER_FACTURAS_FINALIZADAS);
                                    salida.flush();
                                }
                            }
                            break;
                            
                        case Protocolo.CAMBIAR_ESTADO_FACTURA:
                            System.out.println("Procesando solicitud de cambiar estado de factura...");
                            try {
                                ObjectId facturaId = (ObjectId) entrada.readObject();
                                com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura.EstadoFactura nuevoEstado = 
                                    (com.example.pruebamongodbcss.Modulos.Facturacion.ModeloFactura.EstadoFactura) entrada.readObject();
                                
                                boolean cambiado = servicioFacturacion.cambiarEstadoFactura(facturaId, nuevoEstado);
                                
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.CAMBIAR_ESTADO_FACTURA_RESPONSE);
                                    salida.writeBoolean(cambiado);
                                    salida.flush();
                                }
                            } catch (Exception e) {
                                System.err.println("Error al cambiar estado de factura: " + e.getMessage());
                                synchronized (salida) {
                                    salida.writeInt(Protocolo.ERROR_CAMBIAR_ESTADO_FACTURA);
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
                    
                    // Intentar enviar un mensaje de error al cliente antes de cerrar
                    try {
                        synchronized (salida) {
                            salida.writeInt(Protocolo.ERROR_GENERICO);
                            salida.writeUTF("Error interno del servidor: " + e.getMessage());
                            salida.flush();
                        }
                    } catch (IOException ex) {
                        System.err.println("No se pudo enviar mensaje de error al cliente: " + ex.getMessage());
                    }
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

