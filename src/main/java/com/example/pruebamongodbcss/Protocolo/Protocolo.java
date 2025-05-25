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
    static final int ACTUALIZARPROPIETARIO=1009;
    static final int ACTUALIZARPROPIETARIO_RESPONSE=1010;
    static final int ERRORACTUALIZARPROPIETARIO=1011;


    static final int CREARPACIENTE=1012;
    static final int CREARPACIENTE_RESPONSE=1013;
    static final int MODIFICARPACIENTE=1014;
    static final int MODIFICARPACIENTE_RESPONSE=1015;
    static final int ELIMINARPACIENTE=1016;
    static final int ELIMINARPACIENTE_RESPONSE=1017;
    static final int ERRORCREARPACIENTE=1018;
    static final int ERRORELIMINARPACIENTE=1019;

    static final int CREARDIAGNOSTICO=1020;
    static final int CREARDIAGNOSTICO_RESPONSE=1021;
    static final int MODIFICARDIAGNOSTICO=1022;
    static final int MODIFICARDIAGNOSTICO_RESPONSE=1023;
    static final int ELIMINARDIAGNOSTICO=1024;
    static final int ELIMINARDIAGNOSTICO_RESPONSE=1025;
    static final int BUSCARDIAGNOSTICO=1026;
    static final int BUSCARDIAGNOSTICO_RESPONSE=1027;
    static final int ERRODIAGNOSTICO=1028;

    static final int ACTUALIZAREVENTOS=1029;
    static final int ACTUALIZAREVENTOS_RESPONSE=1030;
    static final int ERRORACTUALIZAREVENTOS=1031;
    


    static final int GET_USER_REQUEST=1032;
    static final int GET_USER_RESPONSE=1033;
    static final int ERRORGET_USER=1034;


    static final int OBTENERPACIENTE_POR_ID=1035;
    static final int OBTENERPACIENTE_POR_ID_RESPONSE=1036;
    static final int ERROROBTENERPACIENTE_POR_ID=1037;
    static final int ACTUALIZARPACIENTE=1038;
    static final int ACTUALIZARPACIENTE_RESPONSE=1039;
    static final int ERRORACTUALIZARPACIENTE=1040;

    static final int OBTENERPROPIETARIO_POR_ID=1041;
    static final int OBTENERPROPIETARIO_POR_ID_RESPONSE=1042;
    static final int ERROROBTENERPROPIETARIO_POR_ID=1043;


    static final int OBTENER_TODOS_PACIENTES=1044;
    static final int OBTENER_TODOS_PACIENTES_RESPONSE=1045;
    static final int ERROROBTENER_TODOS_PACIENTES=1046;

    static final int OBTENER_TODOS_PROPIETARIOS=1047;
    static final int OBTENER_TODOS_PROPIETARIOS_RESPONSE=1048;
    static final int ERROROBTENER_TODOS_PROPIETARIOS=1049;

    static final int BUSCAR_DIAGNOSTICOS_POR_FECHA=1050;
    static final int BUSCAR_DIAGNOSTICOS_POR_FECHA_RESPONSE=1051;
    static final int ERRORBUSCAR_DIAGNOSTICOS_POR_FECHA=1052;

    static final int BUSCAR_PACIENTES_POR_NOMBRE=1053;
    static final int BUSCAR_PACIENTES_POR_NOMBRE_RESPONSE=1054;
    static final int ERRORBUSCAR_PACIENTES_POR_NOMBRE=1055;

    static final int BUSCAR_PROPIETARIOS_POR_NOMBRE=1056;
    static final int BUSCAR_PROPIETARIOS_POR_NOMBRE_RESPONSE=1057;
    static final int ERRORBUSCAR_PROPIETARIOS_POR_NOMBRE=1058;

    static final int BUSCAR_DIAGNOSTICOS_POR_PACIENTE=1059;
    static final int BUSCAR_DIAGNOSTICOS_POR_PACIENTE_RESPONSE=1060;
    static final int ERRORBUSCAR_DIAGNOSTICOS_POR_PACIENTE=1061;

    static final int BUSCAR_PACIENTES_POR_PROPIETARIO=1062;
    static final int BUSCAR_PACIENTES_POR_PROPIETARIO_RESPONSE=1063;
    static final int ERRORBUSCAR_PACIENTES_POR_PROPIETARIO=1064;

    static final int CREARPACIENTE_DEVUELVEPACIENTE=1065;
    static final int CREARPACIENTE_DEVUELVEPACIENTE_RESPONSE=1066;
    static final int ERRORCREARPACIENTE_DEVUELVEPACIENTE=1067;


    static final int SETUSERCONECTADO=1068;
    static final int SETUSERCONECTADO_RESPONSE=1069;
    static final int ERRORSETUSERCONECTADO=1070;

    static final int GETALLUSERS=1071;
    static final int GETALLUSERS_RESPONSE=1072;
    static final int ERRORGETALLUSERS=1073;

    static final int GETALLVETERINARIOS=1074;
    static final int GETALLVETERINARIOS_RESPONSE=1075;
    static final int ERRORGETALLVETERINARIOS=1076;

    static final int DELETEUSER=1077;
    static final int DELETEUSER_RESPONSE=1078;
    static final int ERRORDELETEUSER=1079;

    static final int RESETPASSWORD=1080;
    static final int RESETPASSWORD_RESPONSE=1081;
    static final int ERRORRESETPASSWORD=1082;

    static final int CARGAR_DATOS_PRUEBA=1083;
    static final int CARGAR_DATOS_PRUEBA_RESPONSE=1084;
    static final int ERROR_CARGAR_DATOS_PRUEBA=1085;

    static final int RECONECTAR_DB=1086;
    static final int RECONECTAR_DB_RESPONSE=1087;
    static final int ERROR_RECONECTAR_DB=1088;

    static final int GUARDAR_USUARIO=1089;
    static final int GUARDAR_USUARIO_RESPONSE=1090;
    static final int ERROR_GUARDAR_USUARIO=1091;

    static final int VERIFICAR_USUARIO_EXISTE=1092;
    static final int VERIFICAR_USUARIO_EXISTE_RESPONSE=1093;
    static final int ERROR_VERIFICAR_USUARIO_EXISTE=1094;

    static final int DAMETODASLASCITAS=1095;
    static final int DAMETODASLASCITAS_RESPONSE=1096;
    static final int ERROR_DAMETODASLASCITAS=1097;

    // Códigos para operaciones del calendario
    static final int GUARDAR_EVENTO_CALENDARIO=1098;
    static final int GUARDAR_EVENTO_CALENDARIO_RESPONSE=1099;
    static final int ERROR_GUARDAR_EVENTO_CALENDARIO=1100;

    static final int ACTUALIZAR_EVENTO_CALENDARIO=1101;
    static final int ACTUALIZAR_EVENTO_CALENDARIO_RESPONSE=1102;
    static final int ERROR_ACTUALIZAR_EVENTO_CALENDARIO=1103;

    static final int ELIMINAR_EVENTO_CALENDARIO=1104;
    static final int ELIMINAR_EVENTO_CALENDARIO_RESPONSE=1105;
    static final int ERROR_ELIMINAR_EVENTO_CALENDARIO=1106;

    static final int OBTENER_EVENTO_POR_ID=1107;
    static final int OBTENER_EVENTO_POR_ID_RESPONSE=1108;
    static final int ERROR_OBTENER_EVENTO_POR_ID=1109;

    static final int OBTENER_EVENTOS_POR_USUARIO=1110;
    static final int OBTENER_EVENTOS_POR_USUARIO_RESPONSE=1111;
    static final int ERROR_OBTENER_EVENTOS_POR_USUARIO=1112;

} 