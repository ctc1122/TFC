package com.example.pruebamongodbcss.Modulos.Empresa;

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
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
    @FXML private TableView<ModeloUsuario> tablaUsuarios;
    @FXML private TableColumn<ModeloUsuario, String> colUsuario;
    @FXML private TableColumn<ModeloUsuario, String> colNombreUsuario;
    @FXML private TableColumn<ModeloUsuario, String> colRol;
    @FXML private TableColumn<ModeloUsuario, String> colEmailUsuario;
    @FXML private TableColumn<ModeloUsuario, String> colTelefonoUsuario;
    @FXML private TableColumn<ModeloUsuario, Boolean> colActivo;
    @FXML private TextField txtBuscarUsuario;
    
    // Tab de Configuración
    @FXML private Tab tabConfiguracion;
    @FXML private Button btnCargarDatos;
    @FXML private Button btnBackup;
    @FXML private Button btnRestore;
    
    // Servicio
    private ServicioEmpresa servicio;
    
    // Listas observables
    private ObservableList<ModeloVeterinario> veterinariosObservable;
    private ObservableList<ModeloUsuario> usuariosObservable;
    
    // Formato para fechas
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicio
        servicio = new ServicioEmpresa();
        
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
        colActivo.setCellFactory(col -> new TableCell<ModeloUsuario, Boolean>() {
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
        List<ModeloUsuario> usuarios = servicio.obtenerTodosUsuarios();
        usuariosObservable.addAll(usuarios);
    }
    
    /**
     * Busca usuarios por texto (nombre, apellido o usuario)
     */
    private void buscarUsuariosPorTexto(String texto) {
        usuariosObservable.clear();
        List<ModeloUsuario> usuarios = servicio.obtenerTodosUsuarios();
        
        // Filtrar localmente por nombre, apellido o usuario
        usuarios.removeIf(u -> 
            !u.getNombre().toLowerCase().contains(texto.toLowerCase()) && 
            !u.getApellido().toLowerCase().contains(texto.toLowerCase()) && 
            !u.getUsuario().toLowerCase().contains(texto.toLowerCase()));
        
        usuariosObservable.addAll(usuarios);
    }
    
    // ********** ACCIONES DE VETERINARIOS **********
    
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
                "Por favor, seleccione un veterinario para editar.");
        }
    }
    
    @FXML
    private void onEliminarVeterinario(ActionEvent event) {
        ModeloVeterinario veterinarioSeleccionado = tablaVeterinarios.getSelectionModel().getSelectedItem();
        if (veterinarioSeleccionado != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                "¿Está seguro que desea eliminar este veterinario?", 
                "Esta acción no se puede deshacer y podría fallar si hay usuarios asociados.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicio.eliminarVeterinario(veterinarioSeleccionado.getId());
                if (eliminado) {
                    cargarVeterinarios();
                    mostrarMensaje("Veterinario eliminado", "Eliminación exitosa", 
                        "El veterinario ha sido eliminado correctamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el veterinario", 
                        "Hay usuarios asociados a este veterinario o se produjo un error en la eliminación.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay veterinario seleccionado", 
                "Por favor, seleccione un veterinario para eliminar.");
        }
    }
    
    /**
     * Abre el formulario de veterinario para crear o editar
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
            
            controller.setOnSaveCallback(() -> cargarVeterinarios());
            
            Stage stage = new Stage();
            stage.setTitle(veterinario == null ? "Nuevo Veterinario" : "Editar Veterinario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir formulario", 
                "Ha ocurrido un error al intentar abrir el formulario: " + e.getMessage());
        }
    }
    
    // ********** ACCIONES DE USUARIOS **********
    
    @FXML
    private void onNuevoUsuario(ActionEvent event) {
        abrirFormularioUsuario(null);
    }
    
    @FXML
    private void onEditarUsuario(ActionEvent event) {
        ModeloUsuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            abrirFormularioUsuario(usuarioSeleccionado);
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario para editar.");
        }
    }
    
    @FXML
    private void onEliminarUsuario(ActionEvent event) {
        ModeloUsuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            // No permitir eliminar al admin si es el único
            if (usuarioSeleccionado.esAdmin() && 
                usuariosObservable.stream().filter(ModeloUsuario::esAdmin).count() <= 1) {
                mostrarAlerta("Operación no permitida", "No se puede eliminar el único administrador", 
                    "Debe existir al menos un administrador en el sistema.");
                return;
            }
            
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                "¿Está seguro que desea eliminar este usuario?", 
                "Esta acción no se puede deshacer.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicio.eliminarUsuario(usuarioSeleccionado.getId());
                if (eliminado) {
                    cargarUsuarios();
                    mostrarMensaje("Usuario eliminado", "Eliminación exitosa", 
                        "El usuario ha sido eliminado correctamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el usuario", 
                        "Se produjo un error en la eliminación.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario para eliminar.");
        }
    }
    
    @FXML
    private void onResetPassword(ActionEvent event) {
        ModeloUsuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar reset de contraseña", 
                "¿Está seguro que desea resetear la contraseña?", 
                "La contraseña se establecerá a 'password'.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                usuarioSeleccionado.setPassword("password");
                servicio.guardarUsuario(usuarioSeleccionado);
                mostrarMensaje("Contraseña reseteada", "Reset exitoso", 
                    "La contraseña ha sido reseteada correctamente a 'password'.");
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario para resetear su contraseña.");
        }
    }
    
    /**
     * Abre el formulario de usuario para crear o editar
     */
    private void abrirFormularioUsuario(ModeloUsuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Empresa/usuario-form.fxml"));
            Parent root = loader.load();
            
            UsuarioFormController controller = loader.getController();
            controller.setServicio(servicio);
            
            if (usuario != null) {
                controller.setUsuario(usuario);
            }
            
            controller.setOnSaveCallback(() -> cargarUsuarios());
            
            Stage stage = new Stage();
            stage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir formulario", 
                "Ha ocurrido un error al intentar abrir el formulario: " + e.getMessage());
        }
    }
    
    // ********** ACCIONES DE CONFIGURACIÓN **********
    
    @FXML
    private void onCargarDatos(ActionEvent event) {
        Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar carga de datos", 
            "¿Está seguro que desea cargar los datos de prueba?", 
            "Esto agregará veterinarios y usuarios de ejemplo si no existen.");
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            servicio.cargarDatosPrueba();
            cargarVeterinarios();
            cargarUsuarios();
            mostrarMensaje("Datos cargados", "Carga exitosa", 
                "Los datos de prueba han sido cargados correctamente.");
        }
    }
    
    @FXML
    private void onCrearBackup(ActionEvent event) {
        mostrarMensaje("Función no implementada", "Operación pendiente", 
            "La funcionalidad de backup aún no está implementada.");
    }
    
    @FXML
    private void onRestaurarBackup(ActionEvent event) {
        mostrarMensaje("Función no implementada", "Operación pendiente", 
            "La funcionalidad de restauración aún no está implementada.");
    }
    
    // ********** MÉTODOS DE UTILIDAD **********
    
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