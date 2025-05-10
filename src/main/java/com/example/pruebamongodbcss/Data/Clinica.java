package com.example.pruebamongodbcss.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import Utilidades.GestorConexion;

public class Clinica {
    private String CIF;
    private String nombre;
    private String direccion;

    private List<Usuario> usuarios;
    private List<Paciente> pacientes;
    private Map<String, Cita> citas; // Id de cita -> Cita
    private Map<String, SeguimientoClinico> seguimientosClinicos; // Id de paciente -> Seguimiento clínico
    
    private static final int MAX_USUARIOS = 100;
    static final String CONTRASENA_ADMIN = "admin12345";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /* Constructor */
    public Clinica(String id, String nombre, String direccion) throws PatronExcepcion {
        this.setCIF(id);
        this.setNombre(nombre);
        this.setDireccion(direccion);
        this.usuarios = new ArrayList<>();
        this.pacientes = new ArrayList<>();
        this.citas = new HashMap<>();
        this.seguimientosClinicos = new HashMap<>();
        
        // Carga los datos de la base de datos en las listas
        cargarUsuariosaLista();
        cargarPacientesaLista();
        cargarCitasaLista();
        cargarSeguimientosaLista();
    }

    /* Métodos */
    @Override
    public String toString() {
        return "Clinica{" +
                "CIF='" + CIF + '\'' +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                '}';
    }

    public void cargarUsuariosaLista() throws PatronExcepcion {
        MongoDatabase empresaDB = GestorConexion.conectarEmpresa();
        MongoCollection<Document> usuariosCollection = empresaDB.getCollection("usuarios");

        FindIterable<Document> listaUsuarios = usuariosCollection.find();
        Iterator<Document> iterador = listaUsuarios.iterator();
        while (iterador.hasNext()) {
            Document usuarioDoc = iterador.next();
            try {
                // Crear el usuario basado en el rol
                String rol = usuarioDoc.getString("rol");
                Usuario usuario;
                
                if (rol != null && rol.equals("ADMINISTRADOR")) {
                    // Crear usuario administrador - la contraseña admin ya está validada en la BD
                    usuario = new Usuario(
                        usuarioDoc.getString("nombre"),
                        usuarioDoc.getString("apellido"),
                        usuarioDoc.getString("usuario"),
                        usuarioDoc.getString("password"),
                        usuarioDoc.getString("email"),
                        usuarioDoc.getString("telefono"),
                        CONTRASENA_ADMIN
                    );
                } else {
                    // Crear usuario normal
                    usuario = new Usuario(
                        usuarioDoc.getString("nombre"),
                        usuarioDoc.getString("apellido"),
                        usuarioDoc.getString("usuario"),
                        usuarioDoc.getString("password"),
                        usuarioDoc.getString("email"),
                        usuarioDoc.getString("telefono")
                    );
                }
                
                // Asignar el ID de MongoDB si existe
                if (usuarioDoc.containsKey("_id")) {
                    usuario.setId(usuarioDoc.getObjectId("_id"));
                }
                
                // Asignar fecha de creación si existe
                if (usuarioDoc.containsKey("fechaCreacion")) {
                    usuario.setFechaCreacion(usuarioDoc.getDate("fechaCreacion"));
                }
                
                this.usuarios.add(usuario);
            } catch (Exception e) {
                System.err.println("Error al cargar usuario: " + e.getMessage());
            }
        }
    }
    
    public void cargarPacientesaLista() throws PatronExcepcion {
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> pacientesCollection = clinicaDB.getCollection("pacientes");

        FindIterable<Document> listaPacientes = pacientesCollection.find();
        Iterator<Document> iterador = listaPacientes.iterator();
        while (iterador.hasNext()) {
            Document pacienteDoc = iterador.next();
            Paciente paciente = new Paciente(
                pacienteDoc.getString("nombre"),
                pacienteDoc.getString("raza"),
                pacienteDoc.getString("fechaNacimiento"),
                pacienteDoc.getString("sexo"),
                pacienteDoc.getString("color"),
                pacienteDoc.getString("nombrePropietario"),
                pacienteDoc.getString("telefonoPropietario")
            );
            
            // Establecer el tipo de animal si existe
            if (pacienteDoc.containsKey("tipoAnimal")) {
                paciente.setTipoAnimal(pacienteDoc.getString("tipoAnimal"));
            }
            
            this.pacientes.add(paciente);
        }
    }
    
    public void cargarCitasaLista() throws PatronExcepcion {
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> citasCollection = clinicaDB.getCollection("citas");

        FindIterable<Document> listaCitas = citasCollection.find();
        Iterator<Document> iterador = listaCitas.iterator();
        while (iterador.hasNext()) {
            Document citaDoc = iterador.next();
            
            // Buscar el paciente por nombre
            String nombrePaciente = citaDoc.getString("nombrePaciente");
            Paciente paciente = buscarPacientePorNombre(nombrePaciente);
            
            // Buscar el veterinario por nombre
            String nombreVeterinario = citaDoc.getString("nombreVeterinario");
            Usuario veterinario = buscarUsuarioPorNombre(nombreVeterinario);
            
            if (paciente != null && veterinario != null) {
                // Convertir la fecha y hora desde String
                String fechaHoraStr = citaDoc.getString("fechaHora");
                LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr, FORMATTER);
                
                // Obtener el estado de la cita
                String estadoStr = citaDoc.getString("estado");
                EstadoCita estado = EstadoCita.valueOf(estadoStr);
                
                // Crear la cita
                Cita cita = new Cita(
                    citaDoc.getString("id"),
                    paciente,
                    veterinario,
                    fechaHora,
                    citaDoc.getString("motivo"),
                    estado,
                    citaDoc.getString("observaciones")
                );
                
                this.citas.put(cita.getId(), cita);
            }
        }
    }
    
    public void cargarSeguimientosaLista() throws PatronExcepcion {
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> seguimientosCollection = clinicaDB.getCollection("seguimientos");

        FindIterable<Document> listaSeguimientos = seguimientosCollection.find();
        Iterator<Document> iterador = listaSeguimientos.iterator();
        while (iterador.hasNext()) {
            Document seguimientoDoc = iterador.next();
            
            // Buscar el paciente por nombre
            String nombrePaciente = seguimientoDoc.getString("nombrePaciente");
            Paciente paciente = buscarPacientePorNombre(nombrePaciente);
            
            if (paciente != null) {
                // Crear la lista de registros médicos
                List<SeguimientoClinico.RegistroMedico> registros = new ArrayList<>();
                
                // Obtener la lista de registros del documento
                List<Document> registrosDoc = (List<Document>) seguimientoDoc.get("registros");
                
                if (registrosDoc != null) {
                    for (Document registroDoc : registrosDoc) {
                        // Obtener la cita asociada
                        String idCita = registroDoc.getString("idCita");
                        Cita cita = this.citas.get(idCita);
                        
                        if (cita != null) {
                            // Convertir la fecha desde String
                            String fechaStr = registroDoc.getString("fecha");
                            LocalDateTime fecha = LocalDateTime.parse(fechaStr, FORMATTER);
                            
                            // Obtener la lista de medicamentos
                            List<String> medicamentos = (List<String>) registroDoc.get("medicamentos");
                            
                            // Crear el registro médico
                            SeguimientoClinico.RegistroMedico registro = new SeguimientoClinico.RegistroMedico(
                                registroDoc.getString("id"),
                                cita,
                                fecha,
                                registroDoc.getString("diagnostico"),
                                registroDoc.getString("tratamiento"),
                                medicamentos
                            );
                            
                            registros.add(registro);
                        }
                    }
                }
                
                // Crear el seguimiento clínico
                SeguimientoClinico seguimiento = new SeguimientoClinico(
                    seguimientoDoc.getString("id"),
                    paciente,
                    registros
                );
                
                this.seguimientosClinicos.put(paciente.toString(), seguimiento);
            }
        }
    }

    public void registrarUsuario(Usuario usuario) throws PatronExcepcion {
        if (usuario == null) {
            throw new PatronExcepcion("Usuario no válido");
        }
        
        // Verificar si ya existe un usuario con el mismo nombre de usuario
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(usuario.getUsuario())) {
                throw new PatronExcepcion("Ya existe un usuario con ese nombre de usuario");
            }
        }
        
        // Agregar el usuario a la lista
        usuarios.add(usuario);
        
        // Guardar el usuario en la base de datos
        MongoDatabase empresaDB = GestorConexion.conectarEmpresa();
        MongoCollection<Document> usuariosCollection = empresaDB.getCollection("usuarios");
        
        Document usuarioDoc = new Document()
                .append("nombre", usuario.getNombre())
                .append("apellido", usuario.getApellido())
                .append("usuario", usuario.getUsuario())
                .append("password", usuario.getPassword())
                .append("email", usuario.getEmail())
                .append("telefono", usuario.getTelefono())
                .append("rol", usuario.getRol().toString())
                .append("fechaCreacion", usuario.getFechaCreacion());
        
        usuariosCollection.insertOne(usuarioDoc);
    }

    public Usuario iniciarSesion(String usuario, String contraseña) throws PatronExcepcion {
        System.out.println("Intentando iniciar sesión en MongoDB con: " + usuario);
        
        MongoDatabase empresaDB = GestorConexion.conectarEmpresa();
        MongoCollection<Document> usuariosCollection = empresaDB.getCollection("usuarios");

        Document query = new Document("usuario", usuario);
        // Busca el usuario en la base de datos por nombre de usuario
        Document resultado = usuariosCollection.find(query).first();

        if (resultado == null) {
            System.out.println("No se encontró ningún usuario con el nombre: " + usuario);
            throw new PatronExcepcion("Usuario no encontrado");
        }
        
        System.out.println("Usuario encontrado: " + resultado.toJson());
        
        // Verificar si la contraseña coincide
        String contraseñaAlmacenada = resultado.getString("password");
        if (contraseñaAlmacenada.equals(contraseña)) {
            System.out.println("Contraseña correcta para el usuario: " + usuario);
            try {
                String rol = resultado.getString("rol");
                if (rol != null && rol.equals("ADMINISTRADOR")) {
                    return new Usuario(
                        resultado.getString("nombre"),
                        resultado.getString("apellido"),
                        resultado.getString("usuario"),
                        resultado.getString("password"),
                        resultado.getString("email"),
                        resultado.getString("telefono"),
                        CONTRASENA_ADMIN
                    );
                } else {
                    return new Usuario(
                        resultado.getString("nombre"),
                        resultado.getString("apellido"),
                        resultado.getString("usuario"),
                        resultado.getString("password"),
                        resultado.getString("email"),
                        resultado.getString("telefono")
                    );
                }
            } catch (Exception e) {
                throw new PatronExcepcion("Error al cargar el usuario: " + e.getMessage());
            }
        } else {
            System.out.println("Contraseña incorrecta para el usuario: " + usuario);
            throw new PatronExcepcion("Contraseña incorrecta");
        }
    }
    
    // Métodos para gestión de Pacientes
    
    /**
     * Registra un nuevo paciente en la clínica
     */
    public void registrarPaciente(Paciente paciente) throws PatronExcepcion {
        if (paciente == null) {
            throw new PatronExcepcion("Paciente no válido");
        }
        
        // Verificar si ya existe un paciente con el mismo nombre y propietario
        for (Paciente p : pacientes) {
            if (p.toString().equals(paciente.toString())) {
                throw new PatronExcepcion("Ya existe un paciente con ese nombre y propietario");
            }
        }
        
        // Agregar el paciente a la lista
        pacientes.add(paciente);
        
        // Guardar el paciente en la base de datos
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> pacientesCollection = clinicaDB.getCollection("pacientes");
        
        Document pacienteDoc = new Document()
                .append("nombre", paciente.toString())
                .append("raza", paciente.getRaza())
                .append("fechaNacimiento", paciente.getFechaNacimiento())
                .append("sexo", paciente.getSexo())
                .append("color", paciente.getColor())
                .append("nombrePropietario", paciente.getNombrePropietario())
                .append("telefonoPropietario", paciente.getTelefonoPropietario());
        
        // Agregar el tipo de animal si está disponible
        if (paciente.getTipoAnimal() != null && !paciente.getTipoAnimal().isEmpty()) {
            pacienteDoc.append("tipoAnimal", paciente.getTipoAnimal());
        }
        
        pacientesCollection.insertOne(pacienteDoc);
        
        // Crear un nuevo seguimiento clínico vacío para el paciente
        SeguimientoClinico seguimiento = new SeguimientoClinico(paciente);
        seguimientosClinicos.put(paciente.toString(), seguimiento);
        
        // Guardar el seguimiento en la base de datos
        guardarSeguimientoClinico(seguimiento);
    }
    
    /**
     * Modifica los datos de un paciente existente
     */
    public void modificarPaciente(Paciente pacienteAntiguo, Paciente pacienteNuevo) throws PatronExcepcion {
        if (pacienteAntiguo == null || pacienteNuevo == null) {
            throw new PatronExcepcion("Paciente no válido");
        }
        
        // Buscar el paciente en la lista
        int indice = -1;
        for (int i = 0; i < pacientes.size(); i++) {
            if (pacientes.get(i).toString().equals(pacienteAntiguo.toString())) {
                indice = i;
                break;
            }
        }
        
        if (indice == -1) {
            throw new PatronExcepcion("Paciente no encontrado");
        }
        
        // Actualizar el paciente en la lista
        pacientes.set(indice, pacienteNuevo);
        
        // Actualizar el paciente en la base de datos
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> pacientesCollection = clinicaDB.getCollection("pacientes");
        
        Document filtro = new Document("nombre", pacienteAntiguo.toString());
        
        Document pacienteDoc = new Document()
                .append("nombre", pacienteNuevo.toString())
                .append("raza", pacienteNuevo.getRaza())
                .append("fechaNacimiento", pacienteNuevo.getFechaNacimiento())
                .append("sexo", pacienteNuevo.getSexo())
                .append("color", pacienteNuevo.getColor())
                .append("nombrePropietario", pacienteNuevo.getNombrePropietario())
                .append("telefonoPropietario", pacienteNuevo.getTelefonoPropietario());
        
        // Agregar el tipo de animal si está disponible
        if (pacienteNuevo.getTipoAnimal() != null && !pacienteNuevo.getTipoAnimal().isEmpty()) {
            pacienteDoc.append("tipoAnimal", pacienteNuevo.getTipoAnimal());
        }
        
        Document actualizacion = new Document("$set", pacienteDoc);
        pacientesCollection.updateOne(filtro, actualizacion);
        
        // Actualizar el seguimiento clínico del paciente
        SeguimientoClinico seguimiento = seguimientosClinicos.get(pacienteAntiguo.toString());
        if (seguimiento != null) {
            seguimientosClinicos.remove(pacienteAntiguo.toString());
            seguimientosClinicos.put(pacienteNuevo.toString(), seguimiento);
            
            // Actualizar el seguimiento en la base de datos
            MongoCollection<Document> seguimientosCollection = clinicaDB.getCollection("seguimientos");
            Document filtroSeguimiento = new Document("nombrePaciente", pacienteAntiguo.toString());
            Document actualizacionSeguimiento = new Document("$set", new Document("nombrePaciente", pacienteNuevo.toString()));
            seguimientosCollection.updateOne(filtroSeguimiento, actualizacionSeguimiento);
        }
    }
    
    /**
     * Elimina un paciente de la clínica
     */
    public void eliminarPaciente(Paciente paciente) throws PatronExcepcion {
        if (paciente == null) {
            throw new PatronExcepcion("Paciente no válido");
        }
        
        // Verificar si existen citas para el paciente
        for (Cita cita : citas.values()) {
            if (cita.getPaciente().toString().equals(paciente.toString())) {
                throw new PatronExcepcion("No se puede eliminar el paciente porque tiene citas programadas");
            }
        }
        
        // Eliminar el paciente de la lista
        pacientes.removeIf(p -> p.toString().equals(paciente.toString()));
        
        // Eliminar el paciente de la base de datos
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> pacientesCollection = clinicaDB.getCollection("pacientes");
        
        pacientesCollection.deleteOne(new Document("nombre", paciente.toString()));
        
        // Eliminar el seguimiento clínico del paciente
        seguimientosClinicos.remove(paciente.toString());
        
        // Eliminar el seguimiento de la base de datos
        MongoCollection<Document> seguimientosCollection = clinicaDB.getCollection("seguimientos");
        seguimientosCollection.deleteOne(new Document("nombrePaciente", paciente.toString()));
    }
    
    /**
     * Busca un paciente por su nombre
     */
    public Paciente buscarPacientePorNombre(String nombre) {
        for (Paciente paciente : pacientes) {
            if (paciente.toString().equals(nombre)) {
                return paciente;
            }
        }
        return null;
    }
    
    /**
     * Busca un usuario por su nombre
     */
    public Usuario buscarUsuarioPorNombre(String nombreUsuario) {
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(nombreUsuario)) {
                return u;
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los pacientes de la clínica
     */
    public List<Paciente> getPacientes() {
        return new ArrayList<>(pacientes);
    }
    
    /**
     * Busca las razas disponibles para un tipo de animal
     */
    public String[] buscarRazasPorTipoAnimal(String tipoAnimal) throws Exception {
        return Paciente.buscarRazasPorTipoAnimal(tipoAnimal);
    }
    
    // Métodos para gestión de Citas
    
    /**
     * Registra una nueva cita en la clínica
     */
    public void registrarCita(Cita cita) throws PatronExcepcion {
        if (cita == null) {
            throw new PatronExcepcion("Cita no válida");
        }
        
        // Verificar si hay conflicto con otras citas
        for (Cita c : citas.values()) {
            if (c.hayConflictoCon(cita)) {
                throw new PatronExcepcion("Ya existe una cita para esa fecha y hora");
            }
        }
        
        // Agregar la cita al mapa
        citas.put(cita.getId(), cita);
        
        // Guardar la cita en la base de datos
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> citasCollection = clinicaDB.getCollection("citas");
        
        Document citaDoc = new Document()
                .append("id", cita.getId())
                .append("nombrePaciente", cita.getPaciente().toString())
                .append("nombreVeterinario", cita.getVeterinario().getNombre())
                .append("fechaHora", cita.getFechaHora().format(FORMATTER))
                .append("motivo", cita.getMotivo())
                .append("estado", cita.getEstado().name())
                .append("observaciones", cita.getObservaciones());
        
        citasCollection.insertOne(citaDoc);
    }
    
    /**
     * Modifica una cita existente
     */
    public void modificarCita(String idCita, LocalDateTime nuevaFecha, String nuevoMotivo, Usuario nuevoVeterinario) throws PatronExcepcion {
        Cita cita = citas.get(idCita);
        if (cita == null) {
            throw new PatronExcepcion("Cita no encontrada");
        }
        
        // Actualizar los datos de la cita
        cita.setFechaHora(nuevaFecha);
        cita.setMotivo(nuevoMotivo);
        cita.setVeterinario(nuevoVeterinario);
        cita.setEstado(EstadoCita.REPROGRAMADA);
        
        // Actualizar la cita en la base de datos
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> citasCollection = clinicaDB.getCollection("citas");
        
        Document filtro = new Document("id", idCita);
        Document actualizacion = new Document("$set", new Document()
                .append("nombreVeterinario", nuevoVeterinario.getNombre())
                .append("fechaHora", nuevaFecha.format(FORMATTER))
                .append("motivo", nuevoMotivo)
                .append("estado", cita.getEstado().name()));
        
        citasCollection.updateOne(filtro, actualizacion);
    }
    
    /**
     * Cancela una cita
     */
    public void cancelarCita(String idCita) throws PatronExcepcion {
        Cita cita = citas.get(idCita);
        if (cita == null) {
            throw new PatronExcepcion("Cita no encontrada");
        }
        
        // Marcar la cita como cancelada
        cita.cancelar();
        
        // Actualizar la cita en la base de datos
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> citasCollection = clinicaDB.getCollection("citas");
        
        Document filtro = new Document("id", idCita);
        Document actualizacion = new Document("$set", new Document("estado", cita.getEstado().name()));
        
        citasCollection.updateOne(filtro, actualizacion);
    }
    
    /**
     * Elimina una cita
     */
    public void eliminarCita(String idCita) throws PatronExcepcion {
        Cita cita = citas.get(idCita);
        if (cita == null) {
            throw new PatronExcepcion("Cita no encontrada");
        }
        
        // Eliminar la cita del mapa
        citas.remove(idCita);
        
        // Eliminar la cita de la base de datos
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> citasCollection = clinicaDB.getCollection("citas");
        
        citasCollection.deleteOne(new Document("id", idCita));
    }
    
    /**
     * Obtiene todas las citas
     */
    public List<Cita> getCitas() {
        return new ArrayList<>(citas.values());
    }
    
    /**
     * Obtiene las citas para una fecha
     */
    public List<Cita> getCitasPorFecha(LocalDateTime fecha) {
        // Convertir la fecha a inicio y fin del día
        LocalDateTime inicioDia = fecha.toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1).minusNanos(1);
        
        return citas.values().stream()
                .filter(cita -> cita.estaEnRango(inicioDia, finDia))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las citas para un paciente
     */
    public List<Cita> getCitasPorPaciente(Paciente paciente) {
        return citas.values().stream()
                .filter(cita -> cita.getPaciente().toString().equals(paciente.toString()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las citas para un veterinario
     */
    public List<Cita> getCitasPorVeterinario(Usuario veterinario) {
        return citas.values().stream()
                .filter(cita -> cita.getVeterinario().getNombre().equals(veterinario.getNombre()))
                .collect(Collectors.toList());
    }
    
    // Métodos para gestión de Seguimientos Clínicos
    
    /**
     * Obtiene el seguimiento clínico para un paciente
     */
    public SeguimientoClinico getSeguimientoClinico(Paciente paciente) {
        return seguimientosClinicos.get(paciente.toString());
    }
    
    /**
     * Agrega un nuevo registro médico al seguimiento de un paciente
     */
    public void agregarRegistroMedico(Cita cita, String diagnostico, String tratamiento, List<String> medicamentos) throws PatronExcepcion {
        Paciente paciente = cita.getPaciente();
        SeguimientoClinico seguimiento = seguimientosClinicos.get(paciente.toString());
        
        if (seguimiento == null) {
            // Crear un nuevo seguimiento si no existe
            seguimiento = new SeguimientoClinico(paciente);
            seguimientosClinicos.put(paciente.toString(), seguimiento);
        }
        
        // Agregar el registro al seguimiento
        seguimiento.agregarRegistro(cita, diagnostico, tratamiento, medicamentos);
        
        // Marcar la cita como completada
        cita.marcarCompletada();
        
        // Actualizar el estado de la cita en la base de datos
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> citasCollection = clinicaDB.getCollection("citas");
        
        Document filtro = new Document("id", cita.getId());
        Document actualizacion = new Document("$set", new Document("estado", cita.getEstado().name()));
        
        citasCollection.updateOne(filtro, actualizacion);
        
        // Guardar el seguimiento en la base de datos
        guardarSeguimientoClinico(seguimiento);
    }
    
    /**
     * Guarda un seguimiento clínico en la base de datos
     */
    private void guardarSeguimientoClinico(SeguimientoClinico seguimiento) {
        MongoDatabase clinicaDB = GestorConexion.conectarClinica();
        MongoCollection<Document> seguimientosCollection = clinicaDB.getCollection("seguimientos");
        
        // Preparar la lista de registros médicos
        List<Document> registrosDoc = new ArrayList<>();
        for (SeguimientoClinico.RegistroMedico registro : seguimiento.getHistorial()) {
            Document registroDoc = new Document()
                    .append("id", registro.getId())
                    .append("idCita", registro.getCita().getId())
                    .append("fecha", registro.getFecha().format(FORMATTER))
                    .append("diagnostico", registro.getDiagnostico())
                    .append("tratamiento", registro.getTratamiento())
                    .append("medicamentos", registro.getMedicamentos());
            
            registrosDoc.add(registroDoc);
        }
        
        // Verificar si el seguimiento ya existe en la base de datos
        Document filtro = new Document("id", seguimiento.getId());
        Document seguimientoDoc = seguimientosCollection.find(filtro).first();
        
        if (seguimientoDoc != null) {
            // Actualizar el seguimiento existente
            Document actualizacion = new Document("$set", new Document("registros", registrosDoc));
            seguimientosCollection.updateOne(filtro, actualizacion);
        } else {
            // Crear un nuevo seguimiento
            seguimientoDoc = new Document()
                    .append("id", seguimiento.getId())
                    .append("nombrePaciente", seguimiento.getPaciente().toString())
                    .append("registros", registrosDoc);
            
            seguimientosCollection.insertOne(seguimientoDoc);
        }
    }

    /* Setters y Getters */
    public void setNombre(String nombre) throws PatronExcepcion {
        if (nombre.isEmpty() || nombre.length() < 3) {
            throw new PatronExcepcion("Nombre no válido");
        }
        this.nombre = nombre;
    }

    public void setDireccion(String direccion) throws PatronExcepcion {
        if (direccion.isEmpty()) {
            throw new PatronExcepcion("Dirección no válida");
        }
        this.direccion = direccion;
    }

    public void setCIF(String CIF) throws PatronExcepcion {
        this.CIF = CIF;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCIF() {
        return CIF;
    }
}