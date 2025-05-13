package com.example.pruebamongodbcss.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Clase de utilidades genéricas para la aplicación
 */
public class Utilidades {
    
    /**
     * Formatea una fecha a String en formato dd/MM/yyyy
     * @param fecha La fecha a formatear
     * @return String con la fecha formateada
     */
    public static String formatearFecha(Date fecha) {
        if (fecha == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(fecha);
    }
    
    /**
     * Muestra un mensaje de error
     * @param titulo Título de la ventana
     * @param encabezado Encabezado del mensaje
     * @param contenido Texto del mensaje
     */
    public static void mostrarError(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    /**
     * Muestra un mensaje informativo
     * @param titulo Título de la ventana
     * @param encabezado Encabezado del mensaje
     * @param contenido Texto del mensaje
     */
    public static void mostrarInfo(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    /**
     * Muestra un diálogo de confirmación
     * @param titulo Título de la ventana
     * @param encabezado Encabezado del mensaje
     * @param contenido Texto del mensaje
     * @return Optional con el ButtonType seleccionado
     */
    public static Optional<ButtonType> mostrarConfirmacion(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        return alert.showAndWait();
    }
} 