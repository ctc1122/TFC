package com.example.pruebamongodbcss.Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final int MAX_USUARIOS = 100;
    static final String CONTRASENA_ADMIN = "adminVeterinaria";

    /* Constructor */
    public Clinica(String id, String nombre, String direccion) throws PatronExcepcion {
        this.setCIF(id);
        this.setNombre(nombre);
        this.setDireccion(direccion);
        this.usuarios = new ArrayList<>();
        //Carga los usuarios de la base de datos en la lista
        cargarUsuariosaLista();
    }

    /* Métodos */
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
            Document usuario = iterador.next();
            this.usuarios.add(new Usuario(usuario.getString("nombre"), usuario.getString("email"), usuario.getString("contraseña"), usuario.getString("telefono")));
        }
 
    }

    public void registrarUsuario(Usuario usuario) throws PatronExcepcion {
        if (usuario == null) {
            throw new PatronExcepcion("Usuario no válido");
        }
        usuarios.add(usuario);
    }

    public Usuario iniciarSesion(String usuario, String contraseña) throws PatronExcepcion {

        MongoDatabase empresaDB = GestorConexion.conectarEmpresa();
        MongoCollection<Document> usuariosCollection = empresaDB.getCollection("usuarios");

        Document query = new Document("nombre", usuario);
        // Busca el usuario en la base de datos por nombre y contraseña
        Document resultado = usuariosCollection.find(query).first().append(contraseña, contraseña);

        if (resultado != null) {
            return new Usuario(resultado.getString("nombre"), resultado.getString("email"),
                    resultado.getString("contraseña"), resultado.getString("telefono"));
        }

        throw new PatronExcepcion("Usuario no encontrado");
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
        Pattern patronCIF = Pattern.compile("^[A-Z]{1}[0-9]{7}[A-Z0-9]{1}$");
        Matcher matcher = patronCIF.matcher(CIF);
        if (matcher.matches()) {
            this.CIF = CIF;
        } else {
            throw new PatronExcepcion("CIF no válido");
        }
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