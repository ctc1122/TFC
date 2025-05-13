package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

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
    
    private ServicioClinica servicio;
    private ObservableList<ModeloPropietario> propietariosObservable = FXCollections.observableArrayList();
    private Consumer<ModeloPropietario> propietarioSeleccionadoCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
     * Establece el servicio y carga los propietarios
     */
    public void setServicio(ServicioClinica servicio) {
        this.servicio = servicio;
        cargarPropietarios();
    }
    
    /**
     * Carga la lista de propietarios desde el servicio
     */
    private void cargarPropietarios() {
        propietariosObservable.clear();
        List<ModeloPropietario> propietarios = servicio.obtenerTodosPropietarios();
        propietariosObservable.addAll(propietarios);
    }
    
    /**
     * Filtra los propietarios según el texto de búsqueda
     */
    private void filtrarPropietarios(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            cargarPropietarios();
            return;
        }
        
        String busqueda = texto.toLowerCase().trim();
        propietariosObservable.clear();
        
        List<ModeloPropietario> propietariosFiltrados = servicio.obtenerTodosPropietarios().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(busqueda) || 
                             p.getApellidos().toLowerCase().contains(busqueda) ||
                             p.getDni().toLowerCase().contains(busqueda) ||
                             p.getTelefono().toLowerCase().contains(busqueda))
                .toList();
        
        propietariosObservable.addAll(propietariosFiltrados);
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
            controller.setPropietario(nuevoPropietario, true);
            
            // Callback para cuando se guarda el nuevo propietario
            controller.setOnGuardarCallback(propietario -> {
                // Actualizar la lista
                cargarPropietarios();
                // Seleccionar el nuevo propietario
                tablaPropietarios.getSelectionModel().select(propietario);
                // Cerrar el diálogo
                Stage stage = (Stage) root.getScene().getWindow();
                stage.close();
            });
            
            // Callback para cuando se cancela
            controller.setOnCancelarCallback(() -> {
                Stage stage = (Stage) root.getScene().getWindow();
                stage.close();
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
        stage.close();
    }
    
    // Métodos de utilidad para mostrar diálogos
    
    private void mostrarAlerta(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
} 