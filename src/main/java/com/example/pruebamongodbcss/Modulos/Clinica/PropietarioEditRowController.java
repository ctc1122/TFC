package com.example.pruebamongodbcss.Modulos.Clinica;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Controlador para la edición en línea de un propietario
 */
public class PropietarioEditRowController implements Initializable {

    @FXML private HBox rowContainer;
    @FXML private MFXTextField txtNombre;
    @FXML private MFXTextField txtApellidos;
    @FXML private MFXTextField txtDni;
    @FXML private MFXTextField txtDireccion;
    @FXML private MFXTextField txtTelefono;
    @FXML private MFXTextField txtEmail;
    @FXML private MFXButton btnGuardar;
    @FXML private MFXButton btnCancelar;
    
    private ModeloPropietario propietario;
    private ServicioClinica servicio;
    private Consumer<ModeloPropietario> onGuardarCallback;
    private Runnable onCancelarCallback;
    private boolean esNuevo;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicio
        servicio = new ServicioClinica();
        
        // Configurar botones
        btnGuardar.setOnAction(this::onGuardar);
        btnCancelar.setOnAction(this::onCancelar);
    }
    
    /**
     * Configura el controlador con los valores necesarios
     * @param servicio Servicio de clínica
     * @param propietario Propietario a editar
     * @param esNuevo Indica si es un nuevo propietario
     * @param callback Callback para notificar el resultado
     */
    public void configurar(ServicioClinica servicio, ModeloPropietario propietario, boolean esNuevo, 
                          BiConsumer<ModeloPropietario, Boolean> callback) {
        this.servicio = servicio;
        this.propietario = propietario;
        this.esNuevo = esNuevo;
        
        // Cargar los datos del propietario
        cargarDatosPropietario();
        
        // Configurar los callbacks
        this.onGuardarCallback = propietarioEditado -> {
            callback.accept(propietarioEditado, true);
        };
        
        this.onCancelarCallback = () -> {
            callback.accept(propietario, false);
        };
    }
    
    /**
     * Carga los datos del propietario en los controles
     */
    private void cargarDatosPropietario() {
        txtNombre.setText(propietario.getNombre());
        txtApellidos.setText(propietario.getApellidos());
        txtDni.setText(propietario.getDni());
        txtDireccion.setText(propietario.getDireccion());
        txtTelefono.setText(propietario.getTelefono());
        txtEmail.setText(propietario.getEmail());
    }
    
    /**
     * Maneja el evento de guardar
     */
    @FXML
    private void onGuardar(ActionEvent event) {
        if (validarCampos()) {
            // Actualizar propietario con los datos del formulario
            propietario.setNombre(txtNombre.getText().trim());
            propietario.setApellidos(txtApellidos.getText().trim());
            propietario.setDni(txtDni.getText().trim());
            propietario.setDireccion(txtDireccion.getText().trim());
            propietario.setTelefono(txtTelefono.getText().trim());
            propietario.setEmail(txtEmail.getText().trim());
            
            // Guardar cambios
            boolean guardado = false;
            if (esNuevo) {
                try {
                    // Para un nuevo propietario, intentamos guardarlo y obtener su ID
                    propietario.setId(servicio.guardarPropietario(propietario));
                    guardado = propietario.getId() != null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Para un propietario existente, actualizamos sus datos
                guardado = servicio.actualizarPropietario(propietario);
            }
            
            if (guardado) {
                if (onGuardarCallback != null) {
                    onGuardarCallback.accept(propietario);
                }
                // Cerrar ventana si no se maneja externamente
                Stage stage = (Stage) btnGuardar.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
            } else {
                mostrarAlerta("Error", "Error al guardar", 
                    "Ha ocurrido un error al intentar guardar el propietario.");
            }
        }
    }
    
    /**
     * Maneja el evento de cancelar
     */
    @FXML
    private void onCancelar(ActionEvent event) {
        if (onCancelarCallback != null) {
            onCancelarCallback.run();
        } else {
            // Cerrar ventana si no se maneja externamente
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }
    
    /**
     * Valida que todos los campos obligatorios estén completos
     */
    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "El nombre es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }
        
        if (txtApellidos.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "Los apellidos son obligatorios.");
            txtApellidos.requestFocus();
            return false;
        }
        
        if (txtDni.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "El DNI es obligatorio.");
            txtDni.requestFocus();
            return false;
        }
        
        if (txtTelefono.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "El teléfono es obligatorio.");
            txtTelefono.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Establece el callback para cuando se guarda un propietario
     */
    public void setOnGuardarCallback(Consumer<ModeloPropietario> callback) {
        this.onGuardarCallback = callback;
    }
    
    /**
     * Establece el callback para cuando se cancela la edición
     */
    public void setOnCancelarCallback(Runnable callback) {
        this.onCancelarCallback = callback;
    }
    
    /**
     * Configura el propietario a editar (para compatibilidad)
     */
    public void setPropietario(ModeloPropietario propietario, boolean esNuevo) {
        this.propietario = propietario;
        this.esNuevo = esNuevo;
        
        if (propietario != null) {
            cargarDatosPropietario();
        }
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