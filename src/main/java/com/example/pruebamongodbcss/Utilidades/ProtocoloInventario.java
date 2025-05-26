package com.example.pruebamongodbcss.Utilidades;

import java.util.UUID;

/**
 * Protocolo de comunicación con el servidor de inventario
 * Define los códigos y métodos para la comunicación
 */
public class ProtocoloInventario {
    
    // Códigos de petición
    public static final int LOGIN_REQUEST = 1001;
    public static final int BUSCAR_FARMACIA_REQUEST = 1002;
    public static final int OBTENER_FARMACIA_REQUEST = 1003;
    public static final int ACTUALIZAR_UNIDADES_REQUEST = 1004;
    public static final int OBTENER_MEDICAMENTOS_DISPONIBLES_REQUEST = 1005; // Nuevo para nuestro caso
    
    // Códigos de respuesta
    public static final int LOGIN_RESPONSE = 2001;
    public static final int BUSCAR_FARMACIA_RESPONSE = 2002;
    public static final int OBTENER_FARMACIA_RESPONSE = 2003;
    public static final int ACTUALIZAR_UNIDADES_RESPONSE = 2004;
    public static final int OBTENER_MEDICAMENTOS_DISPONIBLES_RESPONSE = 2005; // Nuevo para nuestro caso
    
    // Códigos de estado
    public static final int SUCCESS = 200;
    public static final int LOGIN_SUCCESS = 201;
    public static final int LOGIN_FAILED = 401;
    public static final int INVALID_CREDENTIALS = 402;
    public static final int SERVER_ERROR = 500;
    public static final int DATABASE_ERROR = 501;
    
    // Separador de campos
    public static final String SEPARADOR = "|";
    
    /**
     * Construye un mensaje siguiendo el protocolo del servidor
     * Formato: CODIGO|PARAM1|PARAM2|...|ID_MENSAJE
     */
    public static String construirMensaje(int codigo, String... parametros) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append(codigo);
        
        for (String parametro : parametros) {
            mensaje.append(SEPARADOR).append(parametro != null ? parametro : "");
        }
        
        // Agregar ID único del mensaje
        mensaje.append(SEPARADOR).append(UUID.randomUUID().toString());
        
        return mensaje.toString();
    }
    
    /**
     * Parsea un mensaje recibido del servidor
     */
    public static String[] parsearMensaje(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return new String[0];
        }
        return mensaje.split("\\" + SEPARADOR);
    }
    
    /**
     * Construye mensaje de login
     */
    public static String construirLogin(String usuario, String contrasena) {
        return construirMensaje(LOGIN_REQUEST, usuario, contrasena);
    }
    
    /**
     * Construye mensaje para buscar farmacia por nombre
     */
    public static String construirBuscarFarmacia(String nombre) {
        return construirMensaje(BUSCAR_FARMACIA_REQUEST, nombre);
    }
    
    /**
     * Construye mensaje para obtener toda la farmacia
     */
    public static String construirObtenerFarmacia() {
        return construirMensaje(OBTENER_FARMACIA_REQUEST);
    }
    
    /**
     * Construye mensaje para obtener medicamentos disponibles (con stock > 0)
     */
    public static String construirObtenerMedicamentosDisponibles() {
        return construirMensaje(OBTENER_MEDICAMENTOS_DISPONIBLES_REQUEST);
    }
    
    /**
     * Construye mensaje para actualizar unidades de un producto
     */
    public static String construirActualizarUnidades(String codigo, int unidades) {
        return construirMensaje(ACTUALIZAR_UNIDADES_REQUEST, codigo, String.valueOf(unidades));
    }
    
    /**
     * Extrae el código de respuesta de un mensaje
     */
    public static int extraerCodigoRespuesta(String mensaje) {
        String[] partes = parsearMensaje(mensaje);
        if (partes.length > 0) {
            try {
                return Integer.parseInt(partes[0]);
            } catch (NumberFormatException e) {
                return SERVER_ERROR;
            }
        }
        return SERVER_ERROR;
    }
    
    /**
     * Extrae el estado de una respuesta
     */
    public static int extraerEstado(String mensaje) {
        String[] partes = parsearMensaje(mensaje);
        if (partes.length > 2) {
            try {
                return Integer.parseInt(partes[2]);
            } catch (NumberFormatException e) {
                return SERVER_ERROR;
            }
        }
        return SERVER_ERROR;
    }
    
    /**
     * Extrae los datos de una respuesta
     */
    public static String extraerDatos(String mensaje) {
        String[] partes = parsearMensaje(mensaje);
        if (partes.length > 3) {
            return partes[3];
        }
        return "";
    }
    
    /**
     * Extrae el ID del mensaje
     */
    public static String extraerIdMensaje(String mensaje) {
        String[] partes = parsearMensaje(mensaje);
        if (partes.length > 1) {
            return partes[partes.length - 1]; // El ID siempre es el último
        }
        return "";
    }
    
    /**
     * Verifica si una respuesta indica éxito
     */
    public static boolean esExitoso(String mensaje) {
        int estado = extraerEstado(mensaje);
        return estado == SUCCESS || estado == LOGIN_SUCCESS;
    }
    
    /**
     * Verifica si una respuesta indica error
     */
    public static boolean esError(String mensaje) {
        int estado = extraerEstado(mensaje);
        return estado >= 400;
    }
} 