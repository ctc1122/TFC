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

    // Nuevas constantes para obtener resumen de eventos
    static final int OBTENER_RESUMEN_EVENTOS_USUARIO=1113;
    static final int OBTENER_RESUMEN_EVENTOS_USUARIO_RESPONSE=1114;
    static final int ERROR_OBTENER_RESUMEN_EVENTOS_USUARIO=1115;

    // Códigos para operaciones de citas
    static final int BUSCAR_CITAS_POR_PACIENTE=1116;
    static final int BUSCAR_CITAS_POR_PACIENTE_RESPONSE=1117;
    static final int ERROR_BUSCAR_CITAS_POR_PACIENTE=1118;

    static final int OBTENER_CITA_POR_ID=1119;
    static final int OBTENER_CITA_POR_ID_RESPONSE=1120;
    static final int ERROR_OBTENER_CITA_POR_ID=1121;

    static final int GUARDAR_DIAGNOSTICO=1122;
    static final int GUARDAR_DIAGNOSTICO_RESPONSE=1123;
    static final int ERROR_GUARDAR_DIAGNOSTICO=1124;

    // Códigos para operaciones de facturación
    static final int CREAR_FACTURA=1125;
    static final int CREAR_FACTURA_RESPONSE=1126;
    static final int ERROR_CREAR_FACTURA=1127;

    static final int OBTENER_TODAS_FACTURAS=1128;
    static final int OBTENER_TODAS_FACTURAS_RESPONSE=1129;
    static final int ERROR_OBTENER_TODAS_FACTURAS=1130;

    static final int OBTENER_FACTURA_POR_ID=1131;
    static final int OBTENER_FACTURA_POR_ID_RESPONSE=1132;
    static final int ERROR_OBTENER_FACTURA_POR_ID=1133;

    static final int ACTUALIZAR_FACTURA=1134;
    static final int ACTUALIZAR_FACTURA_RESPONSE=1135;
    static final int ERROR_ACTUALIZAR_FACTURA=1136;

    static final int ELIMINAR_FACTURA=1137;
    static final int ELIMINAR_FACTURA_RESPONSE=1138;
    static final int ERROR_ELIMINAR_FACTURA=1139;

    static final int BUSCAR_FACTURAS_POR_CLIENTE=1140;
    static final int BUSCAR_FACTURAS_POR_CLIENTE_RESPONSE=1141;
    static final int ERROR_BUSCAR_FACTURAS_POR_CLIENTE=1142;

    static final int BUSCAR_FACTURAS_POR_FECHA=1143;
    static final int BUSCAR_FACTURAS_POR_FECHA_RESPONSE=1144;
    static final int ERROR_BUSCAR_FACTURAS_POR_FECHA=1145;

    static final int FINALIZAR_FACTURA=1146;
    static final int FINALIZAR_FACTURA_RESPONSE=1147;
    static final int ERROR_FINALIZAR_FACTURA=1148;

    static final int OBTENER_FACTURAS_BORRADOR=1149;
    static final int OBTENER_FACTURAS_BORRADOR_RESPONSE=1150;
    static final int ERROR_OBTENER_FACTURAS_BORRADOR=1151;

    static final int CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR=1152;
    static final int CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR_RESPONSE=1153;
    static final int ERROR_CAMBIAR_ESTADO_CITA_PENDIENTE_FACTURAR=1154;

    // Códigos para cambiar estado de cita
    static final int CAMBIAR_ESTADO_CITA=1155;
    static final int CAMBIAR_ESTADO_CITA_RESPONSE=1156;
    static final int ERROR_CAMBIAR_ESTADO_CITA=1157;

    // Códigos para operaciones de fichaje
    static final int FICHAR_ENTRADA=1158;
    static final int FICHAR_ENTRADA_RESPONSE=1159;
    static final int ERROR_FICHAR_ENTRADA=1160;

    static final int FICHAR_SALIDA=1161;
    static final int FICHAR_SALIDA_RESPONSE=1162;
    static final int ERROR_FICHAR_SALIDA=1163;

    static final int OBTENER_FICHAJE_ABIERTO_HOY=1164;
    static final int OBTENER_FICHAJE_ABIERTO_HOY_RESPONSE=1165;
    static final int ERROR_OBTENER_FICHAJE_ABIERTO_HOY=1166;

    static final int OBTENER_HISTORIAL_FICHAJES=1167;
    static final int OBTENER_HISTORIAL_FICHAJES_RESPONSE=1168;
    static final int ERROR_OBTENER_HISTORIAL_FICHAJES=1169;

    static final int OBTENER_TODOS_FICHAJES=1170;
    static final int OBTENER_TODOS_FICHAJES_RESPONSE=1171;
    static final int ERROR_OBTENER_TODOS_FICHAJES=1172;

    static final int OBTENER_FICHAJES_POR_FECHA=1173;
    static final int OBTENER_FICHAJES_POR_FECHA_RESPONSE=1174;
    static final int ERROR_OBTENER_FICHAJES_POR_FECHA=1175;

    static final int OBTENER_FICHAJES_EMPLEADO_POR_FECHA=1176;
    static final int OBTENER_FICHAJES_EMPLEADO_POR_FECHA_RESPONSE=1177;
    static final int ERROR_OBTENER_FICHAJES_EMPLEADO_POR_FECHA=1178;

    static final int GENERAR_RESUMEN_FICHAJES=1179;
    static final int GENERAR_RESUMEN_FICHAJES_RESPONSE=1180;
    static final int ERROR_GENERAR_RESUMEN_FICHAJES=1181;

    static final int OBTENER_FICHAJES_POR_DIA=1182;
    static final int OBTENER_FICHAJES_POR_DIA_RESPONSE=1183;
    static final int ERROR_OBTENER_FICHAJES_POR_DIA=1184;

    static final int OBTENER_ESTADISTICAS_FICHAJES=1185;
    static final int OBTENER_ESTADISTICAS_FICHAJES_RESPONSE=1186;
    static final int ERROR_OBTENER_ESTADISTICAS_FICHAJES=1187;

    static final int ELIMINAR_FICHAJE=1188;
    static final int ELIMINAR_FICHAJE_RESPONSE=1189;
    static final int ERROR_ELIMINAR_FICHAJE=1190;

    static final int ACTUALIZAR_FICHAJE=1191;
    static final int ACTUALIZAR_FICHAJE_RESPONSE=1192;
    static final int ERROR_ACTUALIZAR_FICHAJE=1193;

    // Constantes para citas
    public static final int GUARDAR_CITA = 1210;
    public static final int GUARDAR_CITA_RESPONSE = 1211;
    public static final int ERROR_GUARDAR_CITA = 1212;
    public static final int ACTUALIZAR_CITA = 1213;
    public static final int ACTUALIZAR_CITA_RESPONSE = 1214;
    public static final int ERROR_ACTUALIZAR_CITA = 1215;
    public static final int ELIMINAR_CITA = 1216;
    public static final int ELIMINAR_CITA_RESPONSE = 1217;
    public static final int ERROR_ELIMINAR_CITA = 1218;

    // Constantes para búsqueda de citas por rango de fechas
    public static final int BUSCAR_CITAS_POR_RANGO_FECHAS = 1200;
    public static final int BUSCAR_CITAS_POR_RANGO_FECHAS_RESPONSE = 1201;
    public static final int ERROR_BUSCAR_CITAS_POR_RANGO_FECHAS = 1202;

    // Constantes para verificación de conflictos horarios
    public static final int HAY_CONFLICTO_HORARIO = 1203;
    public static final int HAY_CONFLICTO_HORARIO_RESPONSE = 1204;
    public static final int ERROR_HAY_CONFLICTO_HORARIO = 1205;

    // Constantes para búsqueda de razas
    public static final int BUSCAR_RAZAS_POR_TIPO_ANIMAL = 1206;
    public static final int BUSCAR_RAZAS_POR_TIPO_ANIMAL_RESPONSE = 1207;
    public static final int ERROR_BUSCAR_RAZAS_POR_TIPO_ANIMAL = 1208;

    // Constante para error genérico
    public static final int ERROR_GENERICO = 1209;

    // Constantes para obtener citas médicas por usuario
    public static final int OBTENER_CITAS_POR_USUARIO = 1232;
    public static final int OBTENER_CITAS_POR_USUARIO_RESPONSE = 1233;
    public static final int ERROR_OBTENER_CITAS_POR_USUARIO = 1234;

    // Constantes para verificación automática de estados
    public static final int PROBAR_VERIFICACION_AUTOMATICA = 1235;
    public static final int PROBAR_VERIFICACION_AUTOMATICA_RESPONSE = 1236;
    public static final int ERROR_PROBAR_VERIFICACION_AUTOMATICA = 1237;

    // Códigos para obtener facturas por estado
    static final int OBTENER_FACTURAS_POR_ESTADO = 1238;
    static final int OBTENER_FACTURAS_POR_ESTADO_RESPONSE = 1239;
    static final int ERROR_OBTENER_FACTURAS_POR_ESTADO = 1240;

    // Códigos para cambiar estado de factura
    static final int CAMBIAR_ESTADO_FACTURA = 1241;
    static final int CAMBIAR_ESTADO_FACTURA_RESPONSE = 1242;
    static final int ERROR_CAMBIAR_ESTADO_FACTURA = 1243;

    // Códigos para obtener facturas finalizadas
    static final int OBTENER_FACTURAS_FINALIZADAS = 1244;
    static final int OBTENER_FACTURAS_FINALIZADAS_RESPONSE = 1245;
    static final int ERROR_OBTENER_FACTURAS_FINALIZADAS = 1246;

    // Códigos para contadores de diagnósticos y facturas en el calendario
    static final int ACTUALIZAR_CONTADOR_DIAGNOSTICOS = 1247;
    static final int ACTUALIZAR_CONTADOR_DIAGNOSTICOS_RESPONSE = 1248;
    static final int ERROR_ACTUALIZAR_CONTADOR_DIAGNOSTICOS = 1249;

    static final int ACTUALIZAR_CONTADOR_FACTURAS = 1250;
    static final int ACTUALIZAR_CONTADOR_FACTURAS_RESPONSE = 1251;
    static final int ERROR_ACTUALIZAR_CONTADOR_FACTURAS = 1252;

    static final int PUEDE_AGREGAR_FACTURA = 1253;
    static final int PUEDE_AGREGAR_FACTURA_RESPONSE = 1254;
    static final int ERROR_PUEDE_AGREGAR_FACTURA = 1255;

    static final int OBTENER_CONTADOR_FACTURAS = 1256;
    static final int OBTENER_CONTADOR_FACTURAS_RESPONSE = 1257;
    static final int ERROR_OBTENER_CONTADOR_FACTURAS = 1258;

    static final int OBTENER_CONTADOR_DIAGNOSTICOS = 1259;
    static final int OBTENER_CONTADOR_DIAGNOSTICOS_RESPONSE = 1260;
    static final int ERROR_OBTENER_CONTADOR_DIAGNOSTICOS = 1261;

} 