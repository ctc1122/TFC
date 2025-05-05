package com.example.pruebamongodbcss.Data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    }

    /* Métodos */
    public String toString() {
        return "Clinica{" +
                "CIF='" + CIF + '\'' +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                '}';
    }

    public void registrarUsuario(Usuario usuario) throws PatronExcepcion {
        if (usuario == null) {
            throw new PatronExcepcion("Usuario no válido");
        }
        usuario.registrar();
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