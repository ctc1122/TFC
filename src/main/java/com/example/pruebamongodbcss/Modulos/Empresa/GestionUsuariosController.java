package com.example.pruebamongodbcss.Modulos.Empresa;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Data.Usuario.Rol;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
 * Controlador para la gestión de usuarios y veterinarios
 */
public class GestionUsuariosController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private TabPane tabPane;
    
    // Pestaña de usuarios
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colTelefono;
    @FXML private TableColumn<Usuario, Boolean> colActivo;
    @FXML private TextField txtBuscarUsuario;
    
    // Pestaña de veterinarios
    @FXML private TableView<Usuario> tablaVeterinarios;
    @FXML private TableColumn<Usuario, String> colNombreVet;
    @FXML private TableColumn<Usuario, String> colApellidoVet;
    @FXML private TableColumn<Usuario, String> colEspecialidad;
    @FXML private TableColumn<Usuario, String> colColegiado;
    @FXML private TableColumn<Usuario, String> colHorario;
    @FXML private TableColumn<Usuario, Boolean> colDisponible;
    @FXML private TextField txtBuscarVeterinario;
    
    // Botones de acción
    @FXML private Button btnNuevoUsuario;
    @FXML private Button btnEditarUsuario;
    @FXML private Button btnEliminarUsuario;
    @FXML private Button btnResetPassword;
    @FXML private Button btnNuevoVeterinario;
    @FXML private Button btnEditarVeterinario;
    @FXML private Button btnEliminarVeterinario;
    
    // Servicio
    private ServicioUsuarios servicio;
    
    // Listas observables
    private ObservableList<Usuario> usuariosObservable;
    private FilteredList<Usuario> usuariosFiltrados;
    
    private ObservableList<Usuario> veterinariosObservable;
    private FilteredList<Usuario> veterinariosFiltrados;
    
    // Formato para fechas
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicio
        servicio = new ServicioUsuarios();
        
        // Configurar listas observables
        usuariosObservable = FXCollections.observableArrayList();
        usuariosFiltrados = new FilteredList<>(usuariosObservable, p -> true);
        
        veterinariosObservable = FXCollections.observableArrayList();
        veterinariosFiltrados = new FilteredList<>(veterinariosObservable, p -> true);
        
        // Configurar tablas
        configurarTablaUsuarios();
        configurarTablaVeterinarios();
        
        // Cargar datos de prueba explícitamente
        try {
            servicio.cargarDatosPrueba();
            System.out.println("Intentando cargar datos de prueba...");
        } catch (Exception e) {
            System.err.println("Error al cargar datos de prueba: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Cargar datos iniciales
        cargarUsuarios();
        cargarVeterinarios();
        
        // Configurar filtros de búsqueda
        configurarFiltrosUsuarios();
        configurarFiltrosVeterinarios();
    }
    
    /**
     * Configura la tabla de usuarios
     */
    private void configurarTablaUsuarios() {
        colUsuario.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUsuario()));
        
        colNombre.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNombre()));
        
        colApellido.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getApellido()));
        
        colRol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getRol().getDescripcion()));
        
        colEmail.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEmail()));
        
        colTelefono.setCellValueFactory(data -> 
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
        
        tablaUsuarios.setItems(usuariosFiltrados);
        
        // Doble clic para editar
        tablaUsuarios.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaUsuarios.getSelectionModel().getSelectedItem() != null) {
                editarUsuarioSeleccionado();
            }
        });
    }
    
    /**
     * Configura la tabla de veterinarios
     */
    private void configurarTablaVeterinarios() {
        colNombreVet.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNombre()));
        
        colApellidoVet.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getApellido()));
        
        colEspecialidad.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEspecialidad()));
        
        colColegiado.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNumeroColegiado()));
        
        colHorario.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getHoraInicio() + " - " + data.getValue().getHoraFin()));
        
        colDisponible.setCellValueFactory(data -> 
            new SimpleBooleanProperty(data.getValue().isDisponible()));
        
        // Personalizar la celda de disponible
        colDisponible.setCellFactory(col -> new TableCell<Usuario, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Disponible" : "No disponible");
                    setStyle(item ? 
                        "-fx-text-fill: green; -fx-font-weight: bold;" : 
                        "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        
        tablaVeterinarios.setItems(veterinariosFiltrados);
        
        // Doble clic para editar
        tablaVeterinarios.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaVeterinarios.getSelectionModel().getSelectedItem() != null) {
                editarVeterinarioSeleccionado();
            }
        });
    }
    
    /**
     * Configura los filtros de búsqueda para usuarios
     */
    private void configurarFiltrosUsuarios() {
        txtBuscarUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
            usuariosFiltrados.setPredicate(usuario -> {
                if (newVal == null || newVal.isEmpty()) {
                    return usuario.getRol() != Rol.VETERINARIO; // Mostrar todos excepto veterinarios
                }
                
                String lowerCaseFilter = newVal.toLowerCase();
                
                return (usuario.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                        usuario.getApellido().toLowerCase().contains(lowerCaseFilter) ||
                        usuario.getUsuario().toLowerCase().contains(lowerCaseFilter)) &&
                        usuario.getRol() != Rol.VETERINARIO;
            });
        });
    }
    
    /**
     * Configura los filtros de búsqueda para veterinarios
     */
    private void configurarFiltrosVeterinarios() {
        txtBuscarVeterinario.textProperty().addListener((obs, oldVal, newVal) -> {
            veterinariosFiltrados.setPredicate(usuario -> {
                if (newVal == null || newVal.isEmpty()) {
                    return usuario.getRol() == Rol.VETERINARIO; // Mostrar solo veterinarios
                }
                
                String lowerCaseFilter = newVal.toLowerCase();
                
                return (usuario.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                        usuario.getApellido().toLowerCase().contains(lowerCaseFilter) ||
                        (usuario.getEspecialidad() != null && 
                         usuario.getEspecialidad().toLowerCase().contains(lowerCaseFilter))) &&
                        usuario.getRol() == Rol.VETERINARIO;
            });
        });
    }
    
    /**
     * Carga todos los usuarios
     */
    private void cargarUsuarios() {
        usuariosObservable.clear();
        List<Usuario> usuarios = servicio.obtenerTodosUsuarios();
        
        // Filtrar los que no son veterinarios
        usuarios.removeIf(u -> u.getRol() == Rol.VETERINARIO);
        
        usuariosObservable.addAll(usuarios);
    }
    
    /**
     * Carga todos los veterinarios
     */
    private void cargarVeterinarios() {
        veterinariosObservable.clear();
        List<Usuario> veterinarios = servicio.buscarUsuariosPorRol(Rol.VETERINARIO);
        veterinariosObservable.addAll(veterinarios);
    }
    
    // ********** ACCIONES DE USUARIOS **********
    
    @FXML
    private void crearNuevoUsuario() {
        abrirFormularioUsuario(null, false);
    }
    
    @FXML
    private void editarUsuarioSeleccionado() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuario != null) {
            abrirFormularioUsuario(usuario, false);
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario para editar.");
        }
    }
    
    @FXML
    private void eliminarUsuarioSeleccionado() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuario != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                "¿Está seguro que desea eliminar este usuario?", 
                "Esta acción no se puede deshacer.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicio.eliminarUsuario(usuario.getId());
                if (eliminado) {
                    usuariosObservable.remove(usuario);
                    mostrarMensaje("Usuario eliminado", "Usuario eliminado correctamente", 
                        "El usuario ha sido eliminado de la base de datos.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el usuario", 
                        "Ocurrió un error al intentar eliminar el usuario.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario para eliminar.");
        }
    }
    
    @FXML
    private void resetearContraseña() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuario != null) {
            // Pedir nueva contraseña
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Resetear Contraseña");
            dialog.setHeaderText("Introduzca la nueva contraseña para el usuario: " + usuario.getUsuario());
            dialog.setContentText("Nueva contraseña:");
            
            Optional<String> resultado = dialog.showAndWait();
            if (resultado.isPresent() && !resultado.get().isEmpty()) {
                String nuevaContraseña = resultado.get();
                
                // Validar longitud mínima
                if (nuevaContraseña.length() < 8) {
                    mostrarAlerta("Error", "Contraseña no válida", 
                        "La contraseña debe tener al menos 8 caracteres.");
                    return;
                }
                
                // Resetear contraseña
                boolean reseteo = servicio.resetearContrasena(usuario.getId(), nuevaContraseña);
                if (reseteo) {
                    mostrarMensaje("Contraseña reseteada", "Contraseña reseteada correctamente", 
                        "La contraseña del usuario ha sido actualizada.");
                } else {
                    mostrarAlerta("Error", "No se pudo resetear la contraseña", 
                        "Ocurrió un error al intentar resetear la contraseña.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario para resetear su contraseña.");
        }
    }
    
    // ********** ACCIONES DE VETERINARIOS **********
    
    @FXML
    private void crearNuevoVeterinario() {
        abrirFormularioUsuario(null, true);
    }
    
    @FXML
    private void editarVeterinarioSeleccionado() {
        Usuario veterinario = tablaVeterinarios.getSelectionModel().getSelectedItem();
        if (veterinario != null) {
            abrirFormularioUsuario(veterinario, true);
        } else {
            mostrarAlerta("Selección requerida", "No hay veterinario seleccionado", 
                "Por favor, seleccione un veterinario para editar.");
        }
    }
    
    @FXML
    private void eliminarVeterinarioSeleccionado() {
        Usuario veterinario = tablaVeterinarios.getSelectionModel().getSelectedItem();
        if (veterinario != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                "¿Está seguro que desea eliminar este veterinario?", 
                "Esta acción no se puede deshacer.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicio.eliminarUsuario(veterinario.getId());
                if (eliminado) {
                    veterinariosObservable.remove(veterinario);
                    mostrarMensaje("Veterinario eliminado", "Veterinario eliminado correctamente", 
                        "El veterinario ha sido eliminado de la base de datos.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el veterinario", 
                        "Ocurrió un error al intentar eliminar el veterinario.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay veterinario seleccionado", 
                "Por favor, seleccione un veterinario para eliminar.");
        }
    }
    
    /**
     * Abre el formulario para crear o editar un usuario/veterinario
     */
    private void abrirFormularioUsuario(Usuario usuario, boolean esVeterinario) {
        try {
            System.out.println("Abriendo formulario de usuario: " + (usuario != null ? usuario.getUsuario() : "nuevo") + 
                               (esVeterinario ? " (veterinario)" : ""));
            
            // Verificar que el recurso existe
            String rutaFXML = "/com/example/pruebamongodbcss/Empresa/registro-usuario.fxml";
            URL url = getClass().getResource(rutaFXML);
            if (url == null) {
                System.err.println("No se pudo encontrar el archivo FXML en: " + rutaFXML);
                mostrarAlerta("Error", "Archivo no encontrado", 
                    "No se pudo encontrar el archivo del formulario. Ruta: " + rutaFXML);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            RegistroUsuarioController controller = loader.getController();
            
            // Si es un veterinario, configurar el rol
            if (esVeterinario && usuario == null) {
                // Nuevo veterinario
                try {
                    Usuario nuevoVeterinario = new Usuario();
                    nuevoVeterinario.setRol(Rol.VETERINARIO);
                    controller.setUsuarioParaEditar(nuevoVeterinario);
                } catch (Exception e) {
                    System.err.println("Error al crear nuevo veterinario: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (usuario != null) {
                // Editar existente
                controller.setUsuarioParaEditar(usuario);
            }
            
            // Configurar la ventana modal
            Stage stage = new Stage();
            stage.setTitle(usuario == null ? (esVeterinario ? "Nuevo Veterinario" : "Nuevo Usuario") : 
                                           ("Editar " + (esVeterinario ? "Veterinario" : "Usuario")));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
            
            // Recargar datos después de cerrar el formulario
            cargarUsuarios();
            cargarVeterinarios();
            
        } catch (IOException e) {
            System.err.println("Error al abrir formulario: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir formulario", 
                "No se pudo abrir el formulario: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error inesperado", 
                "Ocurrió un error inesperado: " + e.getMessage());
        }
    }
    
    // ********** UTILIDADES **********
    
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