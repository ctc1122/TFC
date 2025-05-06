package com.example.pruebamongodbcss.Data;

public class Paciente {
    private String nombre;
    private String raza;
    private String fechaNacimiento;
    private String sexo;
    private String color;
    private String nombrePropietario;
    private String telefonoPropietario;

    /* Constructor */
    public Paciente(String nombre, String raza, String fechaNacimiento, String sexo, String color, String nombrePropietario, String telefonoPropietario) {
        this.nombre = nombre;
        this.raza = raza;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.color = color;
        this.nombrePropietario = nombrePropietario;
        this.telefonoPropietario = telefonoPropietario;
    }
    
    
}
