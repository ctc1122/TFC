package com.example.pruebamongodbcss.Utilidades;

/**
 * Protocolo de comunicación específico para el servidor de inventario (puerto 50005)
 * Compatible con los códigos que ya maneja el servidor existente
 */
public class ProtocoloInventarioVeterinaria {
    
    // Separadores para el protocolo
    public static final String SEPARADOR_CODIGO = "|";
    public static final String SEPARADOR_PARAMETROS = ":";
    
    // Códigos de petición al servidor de inventario
    public static final int LOGIN_REQUEST = 1;
    public static final int BUSCAR_FARMACIA_REQUEST = 2;
    public static final int OBTENER_FARMACIA_REQUEST = 3;
    public static final int ACTUALIZAR_UNIDADES_REQUEST = 4;
    public static final int ELIMINAR_PRODUCTO_REQUEST = 5;
    
    // Códigos de respuesta del servidor
    public static final int LOGIN_RESPONSE = 100;
    public static final int BUSCAR_FARMACIA_RESPONSE = 101;
    public static final int OBTENER_FARMACIA_RESPONSE = 102;
    public static final int ACTUALIZAR_UNIDADES_RESPONSE = 103;
    public static final int ELIMINAR_PRODUCTO_RESPONSE = 104;
    
    // Códigos de estado
    public static final int SUCCESS = 200;
    public static final int LOGIN_SUCCESS = 201;
    public static final int LOGIN_FAILED = 401;
    public static final int INVALID_CREDENTIALS = 402;
    public static final int NOT_FOUND = 404;
    public static final int SERVER_ERROR = 500;
    public static final int DATABASE_ERROR = 501;
    
    // Códigos adicionales para medicamentos con stock
    public static final int OBTENER_MEDICAMENTOS_INVENTARIO = 9001;
    public static final int OBTENER_MEDICAMENTOS_INVENTARIO_RESPONSE = 9002;
    public static final int BUSCAR_MEDICAMENTOS_INVENTARIO = 9003;
    public static final int BUSCAR_MEDICAMENTOS_INVENTARIO_RESPONSE = 9004;
    public static final int ERROR_INVENTARIO = 9999;
    
    /**
     * Construye un mensaje siguiendo el protocolo del servidor
     * Formato: CODIGO|PARAM1:PARAM2:PARAM3...
     */
    public static String construirMensaje(int codigo, String... parametros) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append(codigo).append(SEPARADOR_CODIGO);
        
        if (parametros != null && parametros.length > 0) {
            for (int i = 0; i < parametros.length; i++) {
                if (i > 0) {
                    mensaje.append(SEPARADOR_PARAMETROS);
                }
                mensaje.append(parametros[i] != null ? parametros[i] : "");
            }
        }
        
        return mensaje.toString();
    }
    
    /**
     * Parsea un mensaje recibido del servidor
     */
    public static String[] parsearMensaje(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return new String[0];
        }
        
        String[] partes = mensaje.split("\\" + SEPARADOR_CODIGO, 2);
        if (partes.length < 2) {
            return new String[]{partes[0]};
        }
        
        String[] parametros = partes[1].split(SEPARADOR_PARAMETROS);
        String[] resultado = new String[parametros.length + 1];
        resultado[0] = partes[0]; // código
        System.arraycopy(parametros, 0, resultado, 1, parametros.length);
        
        return resultado;
    }
    
    /**
     * Obtiene el código de un mensaje
     */
    public static int obtenerCodigo(String mensaje) {
        try {
            String[] partes = parsearMensaje(mensaje);
            return partes.length > 0 ? Integer.parseInt(partes[0]) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Construye mensaje de login para el servidor de inventario
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
     * Construye mensaje para obtener medicamentos con stock > 0
     */
    public static String construirObtenerMedicamentosConStock() {
        return construirMensaje(OBTENER_MEDICAMENTOS_INVENTARIO);
    }
    
    /**
     * Construye mensaje para buscar medicamentos por término
     */
    public static String construirBuscarMedicamentos(String termino) {
        return construirMensaje(BUSCAR_MEDICAMENTOS_INVENTARIO, termino);
    }
    
    /**
     * Construye mensaje para actualizar unidades de un producto
     */
    public static String construirActualizarUnidades(String codigo, int unidades) {
        return construirMensaje(ACTUALIZAR_UNIDADES_REQUEST, codigo, String.valueOf(unidades));
    }
    
    /**
     * Verifica si una respuesta indica éxito
     */
    public static boolean esExitoso(int codigoRespuesta) {
        return codigoRespuesta == OBTENER_FARMACIA_RESPONSE || 
               codigoRespuesta == BUSCAR_FARMACIA_RESPONSE ||
               codigoRespuesta == OBTENER_MEDICAMENTOS_INVENTARIO_RESPONSE ||
               codigoRespuesta == BUSCAR_MEDICAMENTOS_INVENTARIO_RESPONSE ||
               codigoRespuesta == ACTUALIZAR_UNIDADES_RESPONSE ||
               codigoRespuesta == LOGIN_RESPONSE;
    }
    
    /**
     * Verifica si una respuesta indica error
     */
    public static boolean esError(int codigoRespuesta) {
        return codigoRespuesta >= 400 || codigoRespuesta == ERROR_INVENTARIO;
    }
} 