package com.example.pruebamongodbcss.Modulos.Empresa;

import com.example.pruebamongodbcss.Data.Usuario;

import Utilidades1.GestorSocket;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Punto de entrada principal para el módulo de Empresa.
 * Este módulo permite gestionar usuarios y veterinarios del sistema.
 * Solo los administradores pueden acceder a este módulo.
 */
public class EmpresaMain {
    
    private GestorSocket gestorSocket;
    private Usuario usuarioActual;
    
    /**
     * Constructor que inicializa el módulo con GestorSocket y el usuario actual.
     * @param gestorSocket Gestor de socket para comunicación con el servidor
     * @param usuarioActual Usuario actual de la sesión
     */
    public EmpresaMain(GestorSocket gestorSocket, Usuario usuarioActual) {
        this.gestorSocket = gestorSocket;
        this.usuarioActual = usuarioActual;
    }
    
    /**
     * Constructor alternativo para compatibilidad (deprecated).
     * @param servicioUsuarios Servicio de usuarios (no utilizado)
     * @deprecated Usar el constructor con GestorSocket y Usuario
     */
    @Deprecated
    public EmpresaMain(com.example.pruebamongodbcss.Data.ServicioUsuarios servicioUsuarios) {
        // Constructor mantenido por compatibilidad
        this.gestorSocket = GestorSocket.getInstance();
        // El usuario actual se establecerá mediante setUsuarioActual
    }
    
    /**
     * Establece el usuario actual (para compatibilidad con el constructor deprecated)
     * @param usuario Usuario actual
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    /**
     * Inicia el módulo de Empresa si el usuario actual es administrador.
     * @return true si el módulo se inició correctamente
     */
    public boolean iniciar() {
        try {
            // Verificar si el usuario actual es administrador
            if (usuarioActual == null || !usuarioActual.esAdmin()) {
                mostrarAlerta("Acceso denegado", "Acceso denegado", 
                    "Solo los administradores pueden acceder al módulo de Empresa.");
                return false;
            }
            
            return iniciarIntegrado(null);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al iniciar módulo de Empresa", 
                "Se produjo un error al iniciar el módulo de Empresa: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Inicia el módulo de Empresa integrado en el panel principal.
     * 
     * @param panelPrincipal BorderPane donde se cargará el contenido (si es null, se abrirá en una ventana nueva)
     * @return true si el módulo se inició correctamente
     */
    public boolean iniciarIntegrado(BorderPane panelPrincipal) {
        try {
            // Verificar si el usuario actual es administrador
            if (usuarioActual == null || !usuarioActual.esAdmin()) {
                mostrarAlerta("Acceso denegado", "Acceso denegado", 
                    "Solo los administradores pueden acceder al módulo de Empresa.");
                return false;
            }
            
            // Cargar la interfaz de gestión de usuarios
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Empresa/gestion-usuarios.fxml"));
            Parent root = loader.load();
            
            // Configurar el controlador
            GestionUsuariosController controller = loader.getController();
            
            // Establecer el usuario actual en el controlador
            if (controller != null && usuarioActual != null) {
                controller.setUsuarioActual(usuarioActual);
            }
            
            if (panelPrincipal != null) {
                // Cargar en el panel principal existente
                panelPrincipal.setCenter(root);
            } else {
                // Configurar y mostrar la ventana independiente
                Stage stage = new Stage();
                stage.setTitle("Gestión de Empresa - " + usuarioActual.getNombreCompleto());
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
                stage.show();
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al iniciar módulo de Empresa", 
                "Se produjo un error al iniciar el módulo de Empresa: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Muestra una alerta al usuario.
     */
    private void mostrarAlerta(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
} 