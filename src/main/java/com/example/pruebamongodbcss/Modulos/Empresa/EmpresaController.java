package com.example.pruebamongodbcss.Modulos.Empresa;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador para la gestión de empresa (usuarios y veterinarios).
 */
public class EmpresaController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private TabPane tabPane;
    
    // Tab de Veterinarios
    @FXML private Tab tabVeterinarios;
    @FXML private TableView<ModeloVeterinario> tablaVeterinarios;
    @FXML private TableColumn<ModeloVeterinario, String> colNombreVeterinario;
    @FXML private TableColumn<ModeloVeterinario, String> colDNI;
    @FXML private TableColumn<ModeloVeterinario, String> colNumeroTitulo;
    @FXML private TableColumn<ModeloVeterinario, String> colEspecialidad;
    @FXML private TableColumn<ModeloVeterinario, String> colTelefono;
    @FXML private TableColumn<ModeloVeterinario, String> colEmail;
    @FXML private TextField txtBuscarVeterinario;
    
    // Tab de Usuarios
    @FXML private Tab tabUsuarios;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colNombreUsuario;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEmailUsuario;
    @FXML private TableColumn<Usuario, String> colTelefonoUsuario;
    @FXML private TableColumn<Usuario, Boolean> colActivo;
    @FXML private TextField txtBuscarUsuario;
    
    // Tab de Configuración
    @FXML private Tab tabConfiguracion;
    @FXML private Button btnCargarDatos;
    @FXML private Button btnBackup;
    @FXML private Button btnRestore;
    
    // Servicio
    private ServicioUsuarios servicio;
    
    // Listas observables
    private ObservableList<ModeloVeterinario> veterinariosObservable;
    private ObservableList<Usuario> usuariosObservable;
    
    // Formato para fechas
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicio
        servicio = new ServicioUsuarios();
        
        // Configurar listas observables
        veterinariosObservable = FXCollections.observableArrayList();
        usuariosObservable = FXCollections.observableArrayList();
        
        // Configurar tablas
        configurarTablaVeterinarios();
        configurarTablaUsuarios();
        
        // Cargar datos iniciales
        cargarVeterinarios();
        cargarUsuarios();
        
        // Configurar filtros de búsqueda
        configurarFiltros();
    }
    
    /**
     * Configura la tabla de veterinarios
     */
    private void configurarTablaVeterinarios() {
        colNombreVeterinario.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colDNI.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDni()));
        colNumeroTitulo.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNumeroTitulo()));
        colEspecialidad.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEspecialidad()));
        colTelefono.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTelefono()));
        colEmail.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEmail()));
        
        tablaVeterinarios.setItems(veterinariosObservable);
        
        // Doble clic para editar
        tablaVeterinarios.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaVeterinarios.getSelectionModel().getSelectedItem() != null) {
                onEditarVeterinario(new ActionEvent());
            }
        });
    }
    
    /**
     * Configura la tabla de usuarios
     */
    private void configurarTablaUsuarios() {
        colUsuario.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUsuario()));
        colNombreUsuario.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colRol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getRol().getDescripcion()));
        colEmailUsuario.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEmail()));
        colTelefonoUsuario.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTelefono()));
        colActivo.setCellValueFactory(data -> 
            new SimpleBooleanProperty(data.getValue().isActivo()));
        
        // Personalizar la celda de activo para mostrar un círculo verde/rojo
        colActivo.setCellFactory(col -> new TableCell<Usuario, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Sí" : "No");
                    setStyle(item ? 
                        "-fx-text-fill: green; -fx-font-weight: bold;" : 
                        "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        
        tablaUsuarios.setItems(usuariosObservable);
        
        // Doble clic para editar
        tablaUsuarios.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaUsuarios.getSelectionModel().getSelectedItem() != null) {
                onEditarUsuario(new ActionEvent());
            }
        });
    }
    
    /**
     * Configura los filtros de búsqueda
     */
    private void configurarFiltros() {
        // Filtro para veterinarios
        txtBuscarVeterinario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cargarVeterinarios();
            } else {
                veterinariosObservable.clear();
                List<ModeloVeterinario> veterinarios = servicio.buscarVeterinariosPorNombre(newVal);
                veterinariosObservable.addAll(veterinarios);
            }
        });
        
        // Filtro para usuarios
        txtBuscarUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cargarUsuarios();
            } else {
                buscarUsuariosPorTexto(newVal);
            }
        });
    }
    
    /**
     * Carga todos los veterinarios
     */
    private void cargarVeterinarios() {
        veterinariosObservable.clear();
        List<ModeloVeterinario> veterinarios = servicio.obtenerTodosVeterinarios();
        veterinariosObservable.addAll(veterinarios);
    }
    
    /**
     * Carga todos los usuarios
     */
    private void cargarUsuarios() {
        usuariosObservable.clear();
        List<Usuario> usuarios = servicio.obtenerTodosUsuarios();
        usuariosObservable.addAll(usuarios);
    }
    
    /**
     * Busca usuarios por texto (nombre o correo)
     */
    private void buscarUsuariosPorTexto(String texto) {
        usuariosObservable.clear();
        List<Usuario> usuarios = servicio.obtenerTodosUsuarios();
        
        for (Usuario usuario : usuarios) {
            if (usuario.getNombreCompleto().toLowerCase().contains(texto.toLowerCase()) ||
                usuario.getEmail().toLowerCase().contains(texto.toLowerCase()) ||
                usuario.getUsuario().toLowerCase().contains(texto.toLowerCase())) {
                usuariosObservable.add(usuario);
            }
        }
    }
    
    @FXML
    private void onNuevoVeterinario(ActionEvent event) {
        abrirFormularioVeterinario(null);
    }
    
    @FXML
    private void onEditarVeterinario(ActionEvent event) {
        ModeloVeterinario veterinarioSeleccionado = tablaVeterinarios.getSelectionModel().getSelectedItem();
        
        if (veterinarioSeleccionado != null) {
            abrirFormularioVeterinario(veterinarioSeleccionado);
        } else {
            mostrarAlerta("Selección requerida", "No hay veterinario seleccionado", 
                "Por favor, seleccione un veterinario de la tabla para editar.");
        }
    }
    
    @FXML
    private void onEliminarVeterinario(ActionEvent event) {
        ModeloVeterinario veterinarioSeleccionado = tablaVeterinarios.getSelectionModel().getSelectedItem();
        
        if (veterinarioSeleccionado != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                "¿Está seguro de eliminar este veterinario?", 
                "Se eliminará al veterinario " + veterinarioSeleccionado.getNombreCompleto() + " del sistema.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    boolean eliminado = servicio.eliminarVeterinario(veterinarioSeleccionado.getId());
                    
                    if (eliminado) {
                        mostrarMensaje("Veterinario eliminado", "Veterinario eliminado con éxito", 
                            "El veterinario " + veterinarioSeleccionado.getNombreCompleto() + " ha sido eliminado del sistema.");
                        cargarVeterinarios();
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar", 
                            "No se pudo eliminar al veterinario. Es posible que tenga registros asociados.");
                    }
                } catch (Exception e) {
                    mostrarAlerta("Error", "Error al eliminar", 
                        "Se produjo un error al intentar eliminar al veterinario: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay veterinario seleccionado", 
                "Por favor, seleccione un veterinario de la tabla para eliminar.");
        }
    }
    
    /**
     * Muestra el formulario de veterinario para crear o editar
     */
    private void abrirFormularioVeterinario(ModeloVeterinario veterinario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Empresa/veterinario-form.fxml"));
            Parent root = loader.load();
            
            VeterinarioFormController controller = loader.getController();
            controller.setServicio(servicio);
            
            if (veterinario != null) {
                controller.setVeterinario(veterinario);
            }
            
            Stage stage = new Stage();
            stage.setTitle(veterinario == null ? "Nuevo Veterinario" : "Editar Veterinario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
            
            // Recargar datos
            cargarVeterinarios();
            
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al abrir formulario", 
                "No se pudo abrir el formulario de veterinario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onNuevoUsuario(ActionEvent event) {
        abrirFormularioUsuario(null);
    }
    
    @FXML
    private void onEditarUsuario(ActionEvent event) {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        
        if (usuarioSeleccionado != null) {
            abrirFormularioUsuario(usuarioSeleccionado);
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario de la tabla para editar.");
        }
    }
    
    @FXML
    private void onEliminarUsuario(ActionEvent event) {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        
        if (usuarioSeleccionado != null) {
            // No permitir eliminar al último administrador
            if (usuarioSeleccionado.esAdmin() && 
                usuariosObservable.stream().filter(Usuario::esAdmin).count() <= 1) {
                mostrarAlerta("Operación no permitida", "No se puede eliminar el último administrador", 
                    "El sistema requiere al menos un administrador activo.");
                return;
            }
            
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                "¿Está seguro de eliminar este usuario?", 
                "Se eliminará al usuario " + usuarioSeleccionado.getNombreCompleto() + " del sistema.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    boolean eliminado = servicio.eliminarUsuario(usuarioSeleccionado.getId());
                    
                    if (eliminado) {
                        mostrarMensaje("Usuario eliminado", "Usuario eliminado con éxito", 
                            "El usuario " + usuarioSeleccionado.getNombreCompleto() + " ha sido eliminado del sistema.");
                        cargarUsuarios();
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar", 
                            "No se pudo eliminar al usuario. Es posible que tenga registros asociados.");
                    }
                } catch (Exception e) {
                    mostrarAlerta("Error", "Error al eliminar", 
                        "Se produjo un error al intentar eliminar al usuario: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario de la tabla para eliminar.");
        }
    }
    
    @FXML
    private void onResetPassword(ActionEvent event) {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        
        if (usuarioSeleccionado != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Resetear contraseña");
            dialog.setHeaderText("Introduzca la nueva contraseña para " + usuarioSeleccionado.getNombreCompleto());
            dialog.setContentText("Nueva contraseña:");
            
            Optional<String> resultado = dialog.showAndWait();
            
            if (resultado.isPresent() && !resultado.get().isEmpty()) {
                try {
                    String nuevaPassword = resultado.get();
                    usuarioSeleccionado.setPassword(nuevaPassword);
                    servicio.guardarUsuario(usuarioSeleccionado);
                    
                    mostrarMensaje("Contraseña actualizada", "Contraseña actualizada con éxito", 
                        "La contraseña para " + usuarioSeleccionado.getNombreCompleto() + " ha sido actualizada.");
                } catch (Exception e) {
                    mostrarAlerta("Error", "Error al actualizar contraseña", 
                        "Se produjo un error al actualizar la contraseña: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario de la tabla para resetear la contraseña.");
        }
    }
    
    private void abrirFormularioUsuario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Empresa/usuario-form.fxml"));
            Parent root = loader.load();
            
            UsuarioFormController controller = loader.getController();
            controller.setServicio(servicio);
            
            if (usuario != null) {
                controller.setUsuario(usuario);
            }
            
            Stage stage = new Stage();
            stage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
            
            // Recargar datos
            cargarUsuarios();
            
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al abrir formulario", 
                "No se pudo abrir el formulario de usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onCargarDatos(ActionEvent event) {
        Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar carga", 
            "¿Está seguro de cargar datos de prueba?", 
            "Esta acción cargará datos de prueba en el sistema. No se sobrescribirán datos existentes.");
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                servicio.cargarDatosPrueba();
                
                mostrarMensaje("Datos cargados", "Datos de prueba cargados con éxito", 
                    "Los datos de prueba han sido cargados correctamente en el sistema.");
                
                cargarVeterinarios();
                cargarUsuarios();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al cargar datos", 
                    "Se produjo un error al cargar los datos de prueba: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void onCrearBackup(ActionEvent event) {
        mostrarMensaje("Funcionalidad no implementada", "Backup no implementado", 
            "La funcionalidad de backup aún no está implementada.");
    }
    
    @FXML
    private void onRestaurarBackup(ActionEvent event) {
        mostrarMensaje("Funcionalidad no implementada", "Restauración no implementada", 
            "La funcionalidad de restauración de backup aún no está implementada.");
    }
    
    private void mostrarAlerta(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    private void mostrarMensaje(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    private Optional<ButtonType> mostrarConfirmacion(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        return alert.showAndWait();
    }
} 