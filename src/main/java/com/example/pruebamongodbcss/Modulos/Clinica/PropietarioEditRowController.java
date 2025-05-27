package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;

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
    private Consumer<ModeloPropietario> onGuardarCallback;
    private Runnable onCancelarCallback;
    private boolean esNuevo;
    private GestorSocket gestorServidor;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestorServidor = GestorSocket.getInstance();
        
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
    public void configurar( ModeloPropietario propietario, boolean esNuevo, 
                          BiConsumer<ModeloPropietario, Boolean> callback) {

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
            try {
                // Actualizar propietario con los datos del formulario
                propietario.setNombre(txtNombre.getText() == null ? "" : txtNombre.getText().trim());
                propietario.setApellidos(txtApellidos.getText() == null ? "" : txtApellidos.getText().trim());
                propietario.setDni(txtDni.getText() == null ? "" : txtDni.getText().trim());
                propietario.setDireccion(txtDireccion.getText() == null ? "" : txtDireccion.getText().trim());
                propietario.setTelefono(txtTelefono.getText() == null ? "" : txtTelefono.getText().trim());
                propietario.setEmail(txtEmail.getText() == null ? "" : txtEmail.getText().trim());
                
                boolean guardado = false;
                
                if (esNuevo) {
                    // Para un nuevo propietario
                    gestorServidor.enviarPeticion(Protocolo.CREARPROPIETARIO + Protocolo.SEPARADOR_CODIGO);
                    ObjectOutputStream salida = gestorServidor.getSalida();
                    salida.writeObject(propietario);
                    salida.flush();

                    ObjectInputStream entrada = gestorServidor.getEntrada();
                    int respuesta = entrada.readInt();
                    
                    if (respuesta == Protocolo.CREARPROPIETARIO_RESPONSE) {
                        // Leer el ID asignado por el servidor
                        Object idObj = entrada.readObject();
                        if (idObj != null && idObj instanceof org.bson.types.ObjectId) {
                            // El servidor ya envía un ObjectId directamente
                            propietario.setId((org.bson.types.ObjectId) idObj);
                            guardado = true;
                        } else {
                            mostrarAlerta("Error", "Error al procesar el ID", 
                                "El ID recibido del servidor no es válido.");
                            guardado = false;
                        }
                    }
                } else {
                    // Para un propietario existente
                    gestorServidor.enviarPeticion(Protocolo.ACTUALIZARPROPIETARIO + Protocolo.SEPARADOR_CODIGO);
                    ObjectOutputStream salida = gestorServidor.getSalida();
                    salida.writeObject(propietario);
                    salida.flush();

                    ObjectInputStream entrada = gestorServidor.getEntrada();
                    int respuesta = entrada.readInt();
                    guardado = (respuesta == Protocolo.ACTUALIZARPROPIETARIO_RESPONSE);
                }
                
                if (guardado) {
                    if (onGuardarCallback != null) {
                        onGuardarCallback.accept(propietario);
                    }
                    Stage stage = (Stage) btnGuardar.getScene().getWindow();
                    if (stage != null) {
                        stage.close();
                    }
                } else {
                    mostrarAlerta("Error", "Error al guardar", 
                        "No se pudo guardar el propietario. Por favor, inténtelo de nuevo.");
                }
                
            } catch (IOException e) {
                e.printStackTrace();
                mostrarAlerta("Error de conexión", "Error al comunicarse con el servidor", 
                    "No se pudo establecer comunicación con el servidor: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error al procesar la respuesta", 
                    "Error al procesar la respuesta del servidor: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error inesperado", 
                    "Ha ocurrido un error inesperado: " + e.getMessage());
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
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "El nombre es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }
        
        if (txtApellidos.getText() == null || txtApellidos.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "Los apellidos son obligatorios.");
            txtApellidos.requestFocus();
            return false;
        }
        
        if (txtDni.getText() == null || txtDni.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "El DNI es obligatorio.");
            txtDni.requestFocus();
            return false;
        }
        
        if (txtTelefono.getText() == null || txtTelefono.getText().trim().isEmpty()) {
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