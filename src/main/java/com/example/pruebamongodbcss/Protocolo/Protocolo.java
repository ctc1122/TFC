package com.example.pruebamongodbcss.Protocolo;

public interface Protocolo {
    // Tipos de mensajes
    int LOGIN_REQUEST = 1;
    int LOGIN_RESPONSE = 2;
    
    // Códigos de respuesta
    int LOGIN_SUCCESS = 100;
    int LOGIN_FAILED = 101;
    int INVALID_CREDENTIALS = 102;
    int SERVER_ERROR = 500;
} 