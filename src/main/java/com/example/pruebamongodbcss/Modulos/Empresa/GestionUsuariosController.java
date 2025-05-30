package com.example.pruebamongodbcss.Modulos.Empresa;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Data.Usuario.Rol;
import com.example.pruebamongodbcss.Protocolo.Protocolo;

import Utilidades1.GestorSocket;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador principal para la gestión de usuarios y veterinarios.
 * Esta clase unifica la gestión de todos los tipos de usuarios en una sola interfaz.
 */
public class GestionUsuariosController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private TabPane tabPane;
    
    // Pestaña de usuarios
    @FXML private Tab tabUsuarios;
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
    @FXML private Tab tabVeterinarios;
    @FXML private TableView<Usuario> tablaVeterinarios;
    @FXML private TableColumn<Usuario, String> colNombreVet;
    @FXML private TableColumn<Usuario, String> colApellidoVet;
    @FXML private TableColumn<Usuario, String> colEspecialidad;
    @FXML private TableColumn<Usuario, String> colColegiado;
    @FXML private TableColumn<Usuario, String> colHorario;
    @FXML private TableColumn<Usuario, Boolean> colDisponible;
    @FXML private TextField txtBuscarVeterinario;
    
    // Pestaña de configuración
    @FXML private Tab tabConfiguracion;
    @FXML private VBox vboxConfiguracion;
    @FXML private Button btnCargarDatos;
    @FXML private Button btnReconectarDB;
    
    // Botones de acción para usuarios
    @FXML private Button btnNuevoUsuario;
    @FXML private Button btnEditarUsuario;
    @FXML private Button btnEliminarUsuario;
    @FXML private Button btnResetPassword;
    
    // Botones de acción para veterinarios
    @FXML private Button btnNuevoVeterinario;
    @FXML private Button btnEditarVeterinario;
    @FXML private Button btnEliminarVeterinario;
    

    
    // Listas observables
    private ObservableList<Usuario> usuariosObservable;
    private FilteredList<Usuario> usuariosFiltrados;
    
    private ObservableList<Usuario> veterinariosObservable;
    private FilteredList<Usuario> veterinariosFiltrados;
    
    // Formato para fechas
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

    // Add a new field for the current user
    private Usuario usuarioActual;

    private GestorSocket gestorPeticiones;
    
    /**
     * Establece el usuario actual (generalmente un administrador)
     * @param usuario Usuario actual de la sesión
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if ( usuario != null) {
            try {
                //Pedir al servidor setear el usuario actual
                gestorPeticiones.enviarPeticion(Protocolo.SETUSERCONECTADO + Protocolo.SEPARADOR_CODIGO);
                gestorPeticiones.getSalida().writeObject(usuarioActual);
                gestorPeticiones.getSalida().flush();
                
                //Recibir la respuesta del servidor
                int codigo = gestorPeticiones.getEntrada().readInt();
                if (codigo == Protocolo.SETUSERCONECTADO_RESPONSE) {
                    System.out.println("Usuario actual establecido: " + usuarioActual.getUsuario()
                            + " (Rol: " + usuarioActual.getRol().getDescripcion() + ")");
                } else {
                    System.out.println("ADVERTENCIA: No hay usuario actual establecido para GestionUsuariosController");
                }
                System.out.println("Usuario actual establecido: " + usuario.getUsuario() + " (Rol: " + usuario.getRol() + ")");
            } catch (IOException ex) {
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("Inicializando GestionUsuariosController...");
            
            gestorPeticiones = GestorSocket.getInstance();
            
            // Verificar si hay un usuario actual
            if (usuarioActual != null) {
                //Pedir al servidor setear el usuario actual
                gestorPeticiones.enviarPeticion(Protocolo.SETUSERCONECTADO + Protocolo.SEPARADOR_CODIGO);
                gestorPeticiones.getSalida().writeObject(usuarioActual);
                gestorPeticiones.getSalida().flush();
                
                //Recibir la respuesta del servidor
                int codigo = gestorPeticiones.getEntrada().readInt();
                if (codigo == Protocolo.SETUSERCONECTADO_RESPONSE) {
                    System.out.println("Usuario actual establecido: " + usuarioActual.getUsuario() + 
                                    " (Rol: " + usuarioActual.getRol().getDescripcion() + ")");
                } else {
                    System.out.println("ADVERTENCIA: No hay usuario actual establecido para GestionUsuariosController");
                }
            } else {
                System.out.println("ADVERTENCIA: No hay usuario actual establecido para GestionUsuariosController");
            }
            
            // Configurar listas observables
            usuariosObservable = FXCollections.observableArrayList();
            usuariosFiltrados = new FilteredList<>(usuariosObservable, p -> true);
            
            veterinariosObservable = FXCollections.observableArrayList();
            veterinariosFiltrados = new FilteredList<>(veterinariosObservable, p -> true);
            
            // Configurar tablas
            configurarTablaUsuarios();
            configurarTablaVeterinarios();
            
            // Configurar botones de la pestaña de configuración
            configurarBotonesConfiguracion();
            
            // Cargar datos iniciales
            cargarUsuarios();
            cargarVeterinarios();
            
            // Configurar filtros de búsqueda
            configurarFiltrosUsuarios();
            configurarFiltrosVeterinarios();
            
            System.out.println("GestionUsuariosController inicializado correctamente");
        } catch (Exception e) {
            System.err.println("Error al inicializar GestionUsuariosController: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al inicializar", 
                "Se produjo un error al inicializar el controlador: " + e.getMessage());
        }
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
        
        // Personalizar la celda de activo para mostrar un texto verde/rojo
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
     * Configura los botones de la pestaña de configuración
     */
    private void configurarBotonesConfiguracion() {
        // Verificar que los botones existan antes de configurarlos
        if (btnCargarDatos != null) {
            btnCargarDatos.setOnAction(event -> cargarDatosPrueba());
        } else {
            System.err.println("Error: btnCargarDatos es null");
        }
        
        if (btnReconectarDB != null) {
            btnReconectarDB.setOnAction(event -> reconectarBaseDatos());
        } else {
            System.err.println("Error: btnReconectarDB es null");
        }
    }
    
    /**
     * Configura los filtros de búsqueda para usuarios
     */
    private void configurarFiltrosUsuarios() {
        txtBuscarUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
            usuariosFiltrados.setPredicate(usuario -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true; // Mostrar todos los usuarios, incluyendo veterinarios
                }
                
                String lowerCaseFilter = newVal.toLowerCase();
                
                return (usuario.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                        usuario.getApellido().toLowerCase().contains(lowerCaseFilter) ||
                        usuario.getUsuario().toLowerCase().contains(lowerCaseFilter));
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
        try {
            usuariosObservable.clear();
            //Pedir al servidor obtener todos los usuarios
            gestorPeticiones.enviarPeticion(Protocolo.GETALLUSERS + Protocolo.SEPARADOR_CODIGO);
            gestorPeticiones.getSalida().flush();

            int codigo = gestorPeticiones.getEntrada().readInt();
            if (codigo == Protocolo.GETALLUSERS_RESPONSE) {
                List<Usuario> usuarios = (List<Usuario>) gestorPeticiones.getEntrada().readObject();
                usuariosObservable.addAll(usuarios);
            } else {
                System.err.println("Error: No se encontraron usuarios");
            }
        } catch (IOException | ClassNotFoundException ex) {
        }
    }
    
    /**
     * Carga todos los veterinarios
     */
    private void cargarVeterinarios() {
        try {
            veterinariosObservable.clear();
            //Pedir al servidor obtener todos los veterinarios
            gestorPeticiones.enviarPeticion(Protocolo.GETALLVETERINARIOS + Protocolo.SEPARADOR_CODIGO);
            gestorPeticiones.getSalida().flush();
            gestorPeticiones.getSalida().writeObject(Rol.VETERINARIO);
            gestorPeticiones.getSalida().flush();

            int codigo = gestorPeticiones.getEntrada().readInt();
            if (codigo == Protocolo.GETALLVETERINARIOS_RESPONSE) {
                List<Usuario> veterinarios = (List<Usuario>) gestorPeticiones.getEntrada().readObject();
                veterinariosObservable.addAll(veterinarios);
            } else {
                System.err.println("Error: No se encontraron veterinarios");
            }
        } catch (IOException | ClassNotFoundException ex) {
        }
    }
    
    /**
     * Recarga los datos de ambas tablas
     */
    private void recargarTablas() {
        cargarUsuarios();
        cargarVeterinarios();
    }
    
    /**
     * Carga datos de prueba en la base de datos usando el protocolo
     */
    private void cargarDatosPrueba() {
        Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar carga", 
            "¿Está seguro de cargar datos de prueba?", 
            "Esta acción cargará datos de prueba en el sistema. No se sobrescribirán datos existentes.");
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                System.out.println("Iniciando carga de datos de prueba...");
                
                // Enviar petición al servidor para cargar datos de prueba
                gestorPeticiones.enviarPeticion(Protocolo.CARGAR_DATOS_PRUEBA + Protocolo.SEPARADOR_CODIGO);
                gestorPeticiones.getSalida().flush();
                
                // Recibir respuesta del servidor
                int codigo = gestorPeticiones.getEntrada().readInt();
                if (codigo == Protocolo.CARGAR_DATOS_PRUEBA_RESPONSE) {
                    mostrarMensaje("Datos cargados", "Datos de prueba cargados con éxito", 
                        "Los datos de prueba han sido cargados correctamente en el sistema.");
                    
                    // Recargar los datos en las tablas
                    recargarTablas();
                } else {
                    mostrarAlerta("Error", "Error al cargar datos", 
                        "Se produjo un error al cargar los datos de prueba.");
                }
                
            } catch (Exception e) {
                System.err.println("Error al cargar datos: " + e.getMessage());
                e.printStackTrace();
                mostrarAlerta("Error", "Error al cargar datos", 
                    "Se produjo un error al cargar los datos de prueba: " + e.getMessage());
            }
        }
    }
    
    /**
     * Reconecta a la base de datos usando el protocolo
     */
    private void reconectarBaseDatos() {
        try {
            System.out.println("Forzando reconexión a MongoDB...");
            
            // Enviar petición al servidor para reconectar la base de datos
            gestorPeticiones.enviarPeticion(Protocolo.RECONECTAR_DB + Protocolo.SEPARADOR_CODIGO);
            gestorPeticiones.getSalida().flush();
            
            // Recibir respuesta del servidor
            int codigo = gestorPeticiones.getEntrada().readInt();
            if (codigo == Protocolo.RECONECTAR_DB_RESPONSE) {
                // Recargar datos
                recargarTablas();
                
                mostrarMensaje("Conexión restablecida", "Reconexión exitosa", 
                    "La conexión a la base de datos ha sido restablecida correctamente.");
            } else {
                mostrarAlerta("Error", "Error al reconectar", 
                    "Se produjo un error al intentar reconectar a la base de datos.");
            }
            
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al reconectar", 
                "Se produjo un error al intentar reconectar a la base de datos: " + e.getMessage());
        }
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
            // No permitir eliminar al último administrador
            if (usuario.getRol() == Usuario.Rol.ADMINISTRADOR && 
                usuariosObservable.stream()
                    .filter(u -> u.getRol() == Usuario.Rol.ADMINISTRADOR)
                    .count() <= 1) {
                mostrarAlerta("Operación no permitida", "No se puede eliminar el último administrador", 
                    "El sistema requiere al menos un administrador activo.");
                return;
            }
            
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                "¿Está seguro que desea eliminar este usuario?", 
                "Esta acción no se puede deshacer.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {

                try {
                    //Pedir al servidor eliminar el usuario
                    gestorPeticiones.enviarPeticion(Protocolo.DELETEUSER + Protocolo.SEPARADOR_CODIGO + usuario.getId());
                    
                    gestorPeticiones.getSalida().flush();
                    
                    boolean eliminado = false;
                    int codigo = gestorPeticiones.getEntrada().readInt();
                    if (codigo == Protocolo.DELETEUSER_RESPONSE) {
                        eliminado = true;
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el usuario",
                                "Ocurrió un error al intentar eliminar el usuario.");
                    }
                    
                    if (eliminado) {
                        recargarTablas();
                        mostrarMensaje("Usuario eliminado", "Usuario eliminado correctamente",
                                "El usuario ha sido eliminado de la base de datos.");
                    } 
                } catch (IOException ex) {
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
                try {
                    String nuevaContraseña = resultado.get();
                    
                    // Resetear contraseña
                    //Pedir al servidor resetear la contraseña
                    gestorPeticiones.enviarPeticion(Protocolo.RESETPASSWORD + Protocolo.SEPARADOR_CODIGO + usuario.getId() + Protocolo.SEPARADOR_CODIGO + nuevaContraseña);
                    gestorPeticiones.getSalida().flush();
                    
                    boolean reseteo = false;
                    int codigo = gestorPeticiones.getEntrada().readInt();
                    if (codigo == Protocolo.RESETPASSWORD_RESPONSE) {
                        reseteo = true;
                        mostrarMensaje("Contraseña reseteada", "Contraseña cambiada correctamente", 
                            "La contraseña del usuario ha sido actualizada correctamente.");
                    } else {
                        mostrarAlerta("Error", "No se pudo resetear la contraseña",
                                "Ocurrió un error al intentar resetear la contraseña.");
                    }

                } catch (IOException ex) {
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay usuario seleccionado", 
                "Por favor, seleccione un usuario para resetear su contraseña.");
        }
    }
    
    // ********** ACCIONES DE VETERINARIOS **********
    
    @FXML
    public void crearNuevoVeterinario() {
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
                try {
                    //Pedir al servidor eliminar el veterinario
                    gestorPeticiones.enviarPeticion(Protocolo.DELETEUSER + Protocolo.SEPARADOR_CODIGO + veterinario.getId());
                    gestorPeticiones.getSalida().flush();

                    boolean eliminado = false;
                    int codigo = gestorPeticiones.getEntrada().readInt();
                    if (codigo == Protocolo.DELETEUSER_RESPONSE) {
                        eliminado = true;
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el veterinario",
                                "Ocurrió un error al intentar eliminar el veterinario.");
                    }
                
                if (eliminado) {
                    recargarTablas();
                    mostrarMensaje("Veterinario eliminado", "Veterinario eliminado correctamente", 
                        "El veterinario ha sido eliminado de la base de datos.");
                } 
                
                } catch (IOException ex) {
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
            
            // Cargar el formulario
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            // Obtener y configurar el controlador
            RegistroUsuarioController controller = loader.getController();
            if (controller == null) {
                System.err.println("Error: No se pudo obtener el controlador del formulario");
                mostrarAlerta("Error", "Error al inicializar", 
                    "No se pudo inicializar el controlador del formulario.");
                return;
            }
            
            // Preparar usuario para edición/creación
            Usuario usuarioEdicion = null;
            
            if (esVeterinario && usuario == null) {
                // Nuevo veterinario - crear un objeto vacío con rol VETERINARIO
                usuarioEdicion = new Usuario();
                usuarioEdicion.setRol(Rol.VETERINARIO);
            } else if (usuario == null) {
                // Nuevo usuario normal - dejamos que el controlador cree el objeto
                usuarioEdicion = null;
            } else {
                // Editar usuario existente
                usuarioEdicion = usuario;
            }
            
            // Configurar el controlador con el usuario a editar
            controller.setUsuarioParaEditar(usuarioEdicion);
            
            // Configurar la ventana modal
            Stage stage = new Stage();
            stage.setTitle(usuario == null ? (esVeterinario ? "Nuevo Veterinario" : "Nuevo Usuario") : 
                                           ("Editar " + (esVeterinario ? "Veterinario" : "Usuario")));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Configurar y mostrar la escena
            Scene scene = new Scene(root);
            // Agregar el archivo CSS al formulario
            String cssPath = "/com/example/pruebamongodbcss/css/form-styles.css";
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            stage.setScene(scene);
            
            // Mostrar la ventana y esperar a que se cierre
            stage.showAndWait();
            
            // Recargar datos después de cerrar el formulario
            recargarTablas();
            
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