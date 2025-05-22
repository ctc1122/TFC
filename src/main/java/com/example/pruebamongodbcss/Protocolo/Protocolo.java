package com.example.pruebamongodbcss.Protocolo;

public interface Protocolo {

    
    // Separadores para el protocolo
    String SEPARADOR_CODIGO = "|";  // Separa el código de operación de los parámetros
    String SEPARADOR_PARAMETROS = ":";  // Separa los parámetros entre sí
    
    // Códigos de operación (enteros)
    int LOGIN_REQUEST = 1;
    int LOGIN_RESPONSE = 2;
    
    // Códigos de respuesta (enteros)
    int LOGIN_SUCCESS = 100;
    int LOGIN_FAILED = 101;
    int INVALID_CREDENTIALS = 102;
    int SERVER_ERROR = 500;

    static final int REGISTRO_REQUEST=200;
    static final int REGISTRO_RESPONSE=201;
    static final int REGISTRO_SUCCESS=202;
    static final int REGISTRO_FAILED=203;
    static final int REGISTRO_OK=200;
    static final int REGISTRO_ERROR=201;

    static final int CREARPROPIETARIO=1000;
    static final int CREARPROPIETARIO_RESPONSE=1001;
    static final int MODIFICARPROPIETARIO=1002;
    static final int MODIFICARPROPIETARIO_RESPONSE=1003;
    static final int ELIMINARPROPIETARIO=1004;
    static final int ELIMINARPROPIETARIO_RESPONSE=1005;
    static final int BUSCARPROPIETARIO=1006;
    static final int BUSCARPROPIETARIO_RESPONSE=1007;
    static final int ERROPROPIETARIO=1008;
    


    static final int CREARPACIENTE=1009;
    static final int CREARPACIENTE_RESPONSE=1010;
    static final int MODIFICARPACIENTE=1011;
    static final int MODIFICARPACIENTE_RESPONSE=1012;
    static final int ELIMINARPACIENTE=1013;
    static final int ELIMINARPACIENTE_RESPONSE=1014;
    static final int BUSCARPACIENTE=1015;
    static final int BUSCARPACIENTE_RESPONSE=1016;
    static final int ERROPACIENTE=1017;


    static final int CREARDIAGNOSTICO=1018;
    static final int CREARDIAGNOSTICO_RESPONSE=1019;
    static final int MODIFICARDIAGNOSTICO=1020;
    static final int MODIFICARDIAGNOSTICO_RESPONSE=1021;
    static final int ELIMINARDIAGNOSTICO=1022;
    static final int ELIMINARDIAGNOSTICO_RESPONSE=1023;
    static final int BUSCARDIAGNOSTICO=1024;
    static final int BUSCARDIAGNOSTICO_RESPONSE=1025;
    static final int ERRODIAGNOSTICO=1026;

    static final int ACTUALIZAREVENTOS=1027;
    static final int ACTUALIZAREVENTOS_RESPONSE=1028;
    static final int ERRORACTUALIZAREVENTOS=1029;
    





} 