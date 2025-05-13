package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador para la edición en línea de un paciente.
 */
public class PacienteEditRowController implements Initializable {
    
    @FXML private HBox rowContainer;
    @FXML private MFXTextField txtNombre;
    @FXML private MFXTextField txtEspecie;
    @FXML private MFXTextField txtRaza;
    @FXML private MFXTextField txtColor;
    @FXML private MFXComboBox<String> cmbSexo;
    @FXML private MFXDatePicker dpFechaNacimiento;
    @FXML private MFXComboBox<String> cmbEstado;
    @FXML private MFXTextField txtPeso;
    @FXML private Label lblPropietario;
    @FXML private MFXButton btnBuscarPropietario;
    @FXML private MFXButton btnGuardar;
    @FXML private MFXButton btnCancelar;
    
    private ModeloPaciente paciente;
    private ModeloPropietario propietario;
    private ServicioClinica servicio;
    private Consumer<ModeloPaciente> onGuardarCallback;
    private Runnable onCancelarCallback;
    private boolean esNuevo;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicio
        servicio = new ServicioClinica();
        
        // Configurar combos
        cmbSexo.setItems(FXCollections.observableArrayList("Macho", "Hembra"));
        
        ObservableList<String> estadosString = FXCollections.observableArrayList();
        estadosString.addAll("Activo", "Inactivo", "Fallecido");
        cmbEstado.setItems(estadosString);
        
        // Configurar validaciones
        txtPeso.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^\\d*\\.?\\d*$")) {
                return change;
            }
            return null;
        }));
        
        // Configurar botones
        configurarBotones();
    }
    
    /**
     * Configura el controlador con los valores necesarios
     * @param servicio Servicio de clínica
     * @param paciente Paciente a editar
     * @param esNuevo Indica si es un nuevo paciente
     * @param callback Callback para notificar el resultado
     */
    public void configurar(ServicioClinica servicio, ModeloPaciente paciente, boolean esNuevo, 
                          BiConsumer<ModeloPaciente, Boolean> callback) {
        this.servicio = servicio;
        this.paciente = paciente;
        this.esNuevo = esNuevo;
        
        if (paciente != null) {
            cargarDatosPaciente();
        }
        
        // Si es nuevo, establecer valores predeterminados
        if (esNuevo) {
            cmbEstado.setValue("Activo");
            dpFechaNacimiento.setValue(LocalDate.now());
        }
        
        // Configurar los callbacks
        this.onGuardarCallback = pacienteEditado -> {
            callback.accept(pacienteEditado, true);
        };
        
        this.onCancelarCallback = () -> {
            callback.accept(paciente, false);
        };
    }
    
    /**
     * Establece el propietario para este paciente
     */
    public void setPropietario(ModeloPropietario propietario) {
        this.propietario = propietario;
        actualizarLabelPropietario();
    }
    
    /**
     * Carga los datos del paciente en los controles
     */
    private void cargarDatosPaciente() {
        txtNombre.setText(paciente.getNombre());
        txtEspecie.setText(paciente.getEspecie());
        txtRaza.setText(paciente.getRaza());
        txtColor.setText(paciente.getColor());
        cmbSexo.setValue(paciente.getSexo());
        
        if (paciente.getFechaNacimiento() != null) {
            dpFechaNacimiento.setValue(LocalDate.from(paciente.getFechaNacimiento().toInstant().atZone(java.time.ZoneId.systemDefault())));
        }
        
        // Estado - asumiendo que el paciente tiene un campo de estado como String
        if (paciente.getEstadoPaciente() != null) {
            cmbEstado.setValue(paciente.getEstadoPaciente());
        }
        
        if (paciente.getPeso() > 0) {
            txtPeso.setText(String.valueOf(paciente.getPeso()));
        }
        
        // Cargar propietario si existe
        if (paciente.getPropietarioId() != null) {
            propietario = servicio.obtenerPropietarioPorId(paciente.getPropietarioId());
            actualizarLabelPropietario();
        }
    }
    
    /**
     * Actualiza el label del propietario seleccionado
     */
    private void actualizarLabelPropietario() {
        if (propietario != null) {
            lblPropietario.setText(propietario.getNombre() + " " + propietario.getApellidos());
        } else {
            lblPropietario.setText("Sin propietario");
        }
    }
    
    /**
     * Configura los botones de la fila
     */
    private void configurarBotones() {
        // Configurar botón de buscar propietario
        btnBuscarPropietario.setOnAction(event -> buscarPropietario());
        
        // Configurar botón guardar
        btnGuardar.setOnAction(this::onGuardar);
        
        // Configurar botón cancelar
        btnCancelar.setOnAction(this::onCancelar);
    }
    
    /**
     * Abre el diálogo para buscar un propietario
     */
    private void buscarPropietario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/propietario-selector.fxml"));
            Parent root = loader.load();
            
            PropietarioSelectorController controller = loader.getController();
            controller.setServicio(servicio);
            
            // Configurar callback para recibir el propietario seleccionado
            controller.setPropietarioSeleccionadoCallback(propietario -> {
                this.propietario = propietario;
                actualizarLabelPropietario();
            });
            
            Stage stage = new Stage();
            stage.setTitle("Seleccionar Propietario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir selector", 
                "Ha ocurrido un error al intentar abrir el selector de propietarios: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el clic en el botón Guardar
     */
    @FXML
    private void onGuardar(ActionEvent event) {
        if (validarCampos()) {
            // Actualizar paciente con los datos del formulario
            paciente.setNombre(txtNombre.getText().trim());
            paciente.setEspecie(txtEspecie.getText().trim());
            paciente.setRaza(txtRaza.getText().trim());
            paciente.setColor(txtColor.getText().trim());
            paciente.setSexo(cmbSexo.getValue());
            
            if (dpFechaNacimiento.getValue() != null) {
                // Convertir LocalDate a Date
                java.util.Date fecha = java.util.Date.from(dpFechaNacimiento.getValue().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                paciente.setFechaNacimiento(fecha);
            }
            
            // Asumiendo que hay un método setEstadoPaciente
            paciente.setEstadoPaciente(cmbEstado.getValue());
            
            try {
                double peso = Double.parseDouble(txtPeso.getText().replace(",", "."));
                paciente.setPeso(peso);
            } catch (NumberFormatException e) {
                paciente.setPeso(0);
            }
            
            // Asignar propietario
            if (propietario != null) {
                paciente.setPropietarioId(propietario.getId());
                paciente.setNombrePropietario(propietario.getNombre() + " " + propietario.getApellidos());
            }
            
            // Guardar cambios
            boolean guardado;
            if (esNuevo) {
                guardado = servicio.agregarPaciente(paciente);
            } else {
                guardado = servicio.actualizarPaciente(paciente);
            }
            
            if (guardado) {
                if (onGuardarCallback != null) {
                    onGuardarCallback.accept(paciente);
                }
            } else {
                mostrarAlerta("Error", "Error al guardar", 
                    "Ha ocurrido un error al intentar guardar el paciente.");
            }
        }
    }
    
    /**
     * Maneja el clic en el botón Cancelar
     */
    @FXML
    private void onCancelar(ActionEvent event) {
        if (onCancelarCallback != null) {
            onCancelarCallback.run();
        }
    }
    
    /**
     * Valida que todos los campos obligatorios estén completos
     */
    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "El nombre del paciente es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }
        
        if (txtEspecie.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo obligatorio", "La especie del paciente es obligatoria.");
            txtEspecie.requestFocus();
            return false;
        }
        
        if (cmbSexo.getValue() == null) {
            mostrarAlerta("Validación", "Campo obligatorio", "El sexo del paciente es obligatorio.");
            cmbSexo.requestFocus();
            return false;
        }
        
        if (dpFechaNacimiento.getValue() == null) {
            mostrarAlerta("Validación", "Campo obligatorio", "La fecha de nacimiento es obligatoria.");
            dpFechaNacimiento.requestFocus();
            return false;
        }
        
        if (propietario == null) {
            mostrarAlerta("Validación", "Campo obligatorio", "Debe seleccionar un propietario para el paciente.");
            btnBuscarPropietario.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Configura el paciente a editar (para compatibilidad)
     */
    public void setPaciente(ModeloPaciente paciente, boolean esNuevo) {
        this.paciente = paciente;
        this.esNuevo = esNuevo;
        
        if (paciente != null) {
            cargarDatosPaciente();
        }
        
        // Si es nuevo, establecer valores predeterminados
        if (esNuevo) {
            cmbEstado.setValue("Activo");
            dpFechaNacimiento.setValue(LocalDate.now());
        }
    }

    /**
     * Establece el callback para cuando se guarda un paciente
     */
    public void setOnGuardarCallback(Consumer<ModeloPaciente> callback) {
        this.onGuardarCallback = callback;
    }
    
    /**
     * Establece el callback para cuando se cancela la edición
     */
    public void setOnCancelarCallback(Runnable callback) {
        this.onCancelarCallback = callback;
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