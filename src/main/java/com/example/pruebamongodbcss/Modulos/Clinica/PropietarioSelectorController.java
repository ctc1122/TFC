package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.example.pruebamongodbcss.Protocolo.Protocolo;

import Utilidades1.GestorSocket;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador para la selección de propietarios
 */
public class PropietarioSelectorController implements Initializable {

    @FXML private TableView<ModeloPropietario> tablaPropietarios;
    @FXML private TableColumn<ModeloPropietario, String> colNombre;
    @FXML private TableColumn<ModeloPropietario, String> colApellidos;
    @FXML private TableColumn<ModeloPropietario, String> colDni;
    @FXML private TableColumn<ModeloPropietario, String> colTelefono;
    @FXML private MFXTextField txtBuscar;
    @FXML private MFXButton btnSeleccionar;
    @FXML private MFXButton btnNuevoPropietario;
    @FXML private MFXButton btnCancelar;
    
    private GestorSocket gestorSocket;
    private ObservableList<ModeloPropietario> propietariosObservable = FXCollections.observableArrayList();
    private Consumer<ModeloPropietario> propietarioSeleccionadoCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestorSocket = GestorSocket.getInstance();
        
        // Configurar la tabla
        configurarTabla();
        
        // Configurar eventos de botones
        btnSeleccionar.setOnAction(this::onSeleccionar);
        btnNuevoPropietario.setOnAction(this::onNuevoPropietario);
        btnCancelar.setOnAction(this::onCancelar);
        
        // Configurar búsqueda en tiempo real
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            filtrarPropietarios(newVal);
        });
        
        // Cargar propietarios iniciales
        cargarPropietarios();
    }
    
    /**
     * Configura la tabla de propietarios
     */
    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        
        tablaPropietarios.setItems(propietariosObservable);
        
        // Doble clic para seleccionar
        tablaPropietarios.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaPropietarios.getSelectionModel().getSelectedItem() != null) {
                onSeleccionar(new ActionEvent());
            }
        });
    }
    
    /**
     * Carga la lista de propietarios desde el servidor
     */
    private void cargarPropietarios() {
        try {
            propietariosObservable.clear();
            
            // Hacer petición al servidor para obtener todos los propietarios
            gestorSocket.enviarPeticion(Protocolo.OBTENER_TODOS_PROPIETARIOS + Protocolo.SEPARADOR_CODIGO);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_TODOS_PROPIETARIOS_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloPropietario> propietarios = (List<ModeloPropietario>) entrada.readObject();
                if (propietarios != null) {
                    propietariosObservable.addAll(propietarios);
                    System.out.println("✅ Propietarios cargados: " + propietarios.size());
                } else {
                    System.out.println("⚠️ Lista de propietarios vacía recibida del servidor");
                }
            } else {
                System.err.println("❌ Error al obtener propietarios del servidor. Código: " + codigoRespuesta);
                mostrarAlerta("Error", "Error al cargar propietarios", 
                        "No se pudieron cargar los propietarios desde el servidor.");
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error de comunicación al cargar propietarios: " + e.getMessage());
            mostrarAlerta("Error de conexión", "Error al conectar con el servidor", 
                    "No se pudo establecer comunicación con el servidor: " + e.getMessage());
        }
    }
    
    /**
     * Filtra los propietarios según el texto de búsqueda
     */
    private void filtrarPropietarios(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            cargarPropietarios();
            return;
        }
        
        try {
            propietariosObservable.clear();
            
            // Hacer petición al servidor para buscar propietarios por nombre
            gestorSocket.enviarPeticion(Protocolo.BUSCAR_PROPIETARIOS_POR_NOMBRE + Protocolo.SEPARADOR_CODIGO + texto.trim());
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.BUSCAR_PROPIETARIOS_POR_NOMBRE_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloPropietario> propietarios = (List<ModeloPropietario>) entrada.readObject();
                if (propietarios != null) {
                    propietariosObservable.addAll(propietarios);
                    System.out.println("✅ Propietarios filtrados: " + propietarios.size());
                }
            } else {
                System.err.println("❌ Error al filtrar propietarios. Código: " + codigoRespuesta);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error de comunicación al filtrar propietarios: " + e.getMessage());
            // En caso de error, mostrar todos los propietarios
            cargarPropietarios();
        }
    }
    
    /**
     * Maneja el evento de seleccionar propietario
     */
    @FXML
    private void onSeleccionar(ActionEvent event) {
        ModeloPropietario seleccionado = tablaPropietarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            if (propietarioSeleccionadoCallback != null) {
                propietarioSeleccionadoCallback.accept(seleccionado);
            }
            cerrarVentana();
        } else {
            mostrarAlerta("Selección requerida", "No hay propietario seleccionado", 
                "Por favor, seleccione un propietario de la lista.");
        }
    }
    
    /**
     * Maneja el evento de crear nuevo propietario
     */
    @FXML
    private void onNuevoPropietario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/propietario-edit-row.fxml"));
            Parent root = loader.load();
            
            PropietarioEditRowController controller = loader.getController();
            ModeloPropietario nuevoPropietario = new ModeloPropietario();
            controller.configurar(nuevoPropietario, true, (propietario, confirmado) -> {
                if (confirmado) {
                    // Recargar la lista de propietarios
                    cargarPropietarios();
                    // Seleccionar el nuevo propietario
                    tablaPropietarios.getSelectionModel().select(propietario);
                }
            });
            
            Stage stage = new Stage();
            stage.setTitle("Nuevo Propietario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir formulario", 
                "Ha ocurrido un error al intentar abrir el formulario de propietario: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de cancelar
     */
    @FXML
    private void onCancelar(ActionEvent event) {
        cerrarVentana();
    }
    
    /**
     * Establece el callback para cuando se selecciona un propietario
     */
    public void setPropietarioSeleccionadoCallback(Consumer<ModeloPropietario> callback) {
        this.propietarioSeleccionadoCallback = callback;
    }
    
    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Método de compatibilidad - ya no se usa ServicioClinica
     * @deprecated Usar GestorSocket en su lugar
     */
    @Deprecated
    public void setServicio(ServicioClinica servicio) {
        // Este método se mantiene por compatibilidad pero ya no se usa
        System.out.println("⚠️ setServicio() está deprecated. PropietarioSelectorController ahora usa GestorSocket directamente.");
    }
    
    private void mostrarAlerta(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
} 