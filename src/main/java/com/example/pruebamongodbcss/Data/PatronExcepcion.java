package com.example.pruebamongodbcss.Data;

public class PatronExcepcion extends Exception {
    public PatronExcepcion(String mensaje) {
        super("Error en el patron: "+mensaje);
    }
}

