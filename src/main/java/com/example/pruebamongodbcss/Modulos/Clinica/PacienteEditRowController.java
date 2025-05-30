package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.example.pruebamongodbcss.Protocolo.Protocolo;

import Utilidades1.GestorSocket;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
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
    
    // =========================
    // 1. ATRIBUTOS FXML Y DE CLASE
    // =========================
    @FXML private HBox rowContainer;
    @FXML private MFXTextField txtNombre, txtRaza, txtPeso;
    @FXML private MFXComboBox<String> cmbSexo, cmbEspecie;
    @FXML private MFXDatePicker dpFechaNacimiento;
    @FXML private Label lblPropietario;
    @FXML private MFXButton btnBuscarPropietario, btnGuardar, btnCancelar;
    
    private ModeloPaciente paciente;
    private ModeloPropietario propietario;
    private Consumer<ModeloPaciente> onGuardarCallback;
    private Runnable onCancelarCallback;
    private boolean esNuevo;
    private GestorSocket gestorServidor;
    
    // =========================
    // 2. INICIALIZACIÓN Y CONFIGURACIÓN
    // =========================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestorServidor = GestorSocket.getInstance();
        configurarCombos();
        configurarValidaciones();
        configurarBotones();
        configurarCamposTexto();
    }
    
    private void configurarCombos() {
        cmbSexo.setItems(FXCollections.observableArrayList("Macho", "Hembra"));
        cmbEspecie.setItems(FXCollections.observableArrayList("Perro", "Gato", "Ave", "Reptil", "Otro"));
    }
    
    private void configurarValidaciones() {
        txtPeso.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return newText.matches("^\\d*\\.?\\d*$") ? change : null;
        }));
    }
    
    /**
     * Configura los campos de texto para evitar problemas de visualización
     */
    private void configurarCamposTexto() {
        // Asegurar que los MFXTextField mantengan su texto visible
        txtNombre.setPromptText("Ingrese el nombre del paciente");
        txtRaza.setPromptText("Ingrese la raza");
        txtPeso.setPromptText("Peso en kg");
        
        // Forzar actualización visual cuando pierden el foco
        txtNombre.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Cuando pierde el foco
                String texto = txtNombre.getText();
                if (texto != null && !texto.isEmpty()) {
                    // Forzar redibujado
                    txtNombre.applyCss();
                }
            }
        });
        
        txtRaza.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String texto = txtRaza.getText();
                if (texto != null && !texto.isEmpty()) {
                    txtRaza.applyCss();
                }
            }
        });
        
        txtPeso.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String texto = txtPeso.getText();
                if (texto != null && !texto.isEmpty()) {
                    txtPeso.applyCss();
                }
            }
        });
    }
    
    private void configurarBotones() {
        btnBuscarPropietario.setOnAction(event -> buscarPropietario());
        btnGuardar.setOnAction(this::onGuardar);
        btnCancelar.setOnAction(this::onCancelar);
    }
    
    // =========================
    // 3. CARGA Y ACTUALIZACIÓN DE DATOS
    // =========================
    /**
     * Configura el controlador con los valores necesarios
     * @param servicio Servicio de clínica
     * @param paciente Paciente a editar
     * @param esNuevo Indica si es un nuevo paciente
     * @param callback Callback para notificar el resultado
     */
    public void configurar( ModeloPaciente paciente, boolean esNuevo, 
                          BiConsumer<ModeloPaciente, Boolean> callback) {
        this.paciente = paciente;
        this.esNuevo = esNuevo;
        
        if (paciente != null) {
            cargarDatosPaciente();
        }
        
        // Si es nuevo, establecer valores predeterminados
        if (esNuevo) {
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
     * Carga los datos del paciente en los controles
     */
    private void cargarDatosPaciente() {
        txtNombre.setText(paciente.getNombre());
        cmbEspecie.setValue(paciente.getEspecie());
        txtRaza.setText(paciente.getRaza());
        cmbSexo.setValue(paciente.getSexo());

        if (paciente.getFechaNacimiento() != null) {
            dpFechaNacimiento.setValue(LocalDate.from(paciente.getFechaNacimiento().toInstant().atZone(java.time.ZoneId.systemDefault())));
        }

        if (paciente.getPeso() > 0) {
            txtPeso.setText(String.valueOf(paciente.getPeso()));
        }

        // Cargar propietario si existe
        if (paciente.getPropietarioId() != null) {
            cargarPropietario();
        }
    }
    
    private void cargarPropietario() {
        try {
            //Pedir al servidor el propietario
            gestorServidor.enviarPeticion(Protocolo.OBTENERPROPIETARIO_POR_ID + Protocolo.SEPARADOR_CODIGO + paciente.getPropietarioId());
            ObjectInputStream entrada = gestorServidor.getEntrada();
            if (entrada.readInt() == Protocolo.OBTENERPROPIETARIO_POR_ID_RESPONSE) {
                propietario = (ModeloPropietario) entrada.readObject();
                actualizarLabelPropietario();
            }
            else {
                mostrarAlerta("Error", "Error al obtener el propietario",
                        "Ha ocurrido un error al intentar obtener el propietario.");
            }
        } catch (IOException | ClassNotFoundException ex) {
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
    
    // =========================
    // 4. GESTIÓN DE EVENTOS (BOTONES)
    // =========================
    /**
     * Abre el diálogo para buscar un propietario
     */
    private void buscarPropietario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/propietario-selector.fxml"));
            Parent root = loader.load();
            
            PropietarioSelectorController controller = loader.getController();
            
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
            actualizarPacienteDesdeFormulario();
            guardarPaciente();
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
    
    // =========================
    // 5. VALIDACIÓN
    // =========================
    /**
     * Valida que todos los campos obligatorios estén completos
     */
    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarYFoco("El nombre del paciente es obligatorio.", txtNombre);
            return false;
        }
        
        if (cmbEspecie.getValue() == null || cmbEspecie.getValue().trim().isEmpty()) {
            mostrarYFoco("La especie del paciente es obligatoria.", cmbEspecie);
            return false;
        }
        
        if (cmbSexo.getValue() == null) {
            mostrarYFoco("El sexo del paciente es obligatorio.", cmbSexo);
            return false;
        }
        
        if (dpFechaNacimiento.getValue() == null) {
            mostrarYFoco("La fecha de nacimiento es obligatoria.", dpFechaNacimiento);
            return false;
        }
        
        if (propietario == null) {
            mostrarYFoco("Debe seleccionar un propietario para el paciente.", btnBuscarPropietario);
            return false;
        }
        
        return true;
    }
    
    private boolean mostrarYFoco(String mensaje, javafx.scene.Node nodo) {
        mostrarAlerta("Validación", "Campo obligatorio", mensaje);
        nodo.requestFocus();
        return false;
    }
    
    // =========================
    // 6. UTILIDADES
    // =========================
    private void mostrarAlerta(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    private void actualizarPacienteDesdeFormulario() {
        paciente.setNombre(txtNombre.getText().trim());
        paciente.setEspecie(cmbEspecie.getValue());
        paciente.setRaza(txtRaza.getText().trim());
        paciente.setSexo(cmbSexo.getValue());

        if (dpFechaNacimiento.getValue() != null) {
            paciente.setFechaNacimiento(java.util.Date.from(dpFechaNacimiento.getValue().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        }

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
    }
    
    private void guardarPaciente() {
        boolean guardado = false;
        if (esNuevo) {
            try {
                gestorServidor.enviarPeticion(Protocolo.CREARPACIENTE_DEVUELVEPACIENTE + Protocolo.SEPARADOR_CODIGO);
                ObjectOutputStream salida = gestorServidor.getSalida();
                salida.writeObject(paciente);
                salida.flush();

                ObjectInputStream entrada = gestorServidor.getEntrada();
                if (entrada.readInt() == Protocolo.CREARPACIENTE_DEVUELVEPACIENTE_RESPONSE) {
                    paciente = (ModeloPaciente) entrada.readObject();
                    guardado = true;
                } else {
                    guardado = false;
                }
                if (guardado) {
                    if (onGuardarCallback != null) {
                        onGuardarCallback.accept(paciente);
                    }
                    cerrarVentana();
                } else {
                    mostrarAlerta("Error", "Error al guardar",
                            "Ha ocurrido un error al intentar guardar el paciente.");
                }
            } catch (IOException | ClassNotFoundException ex) {
            }
        } else {
            try {
                //Pedir al servidor actualizar el paciente
                gestorServidor.enviarPeticion(Protocolo.ACTUALIZARPACIENTE + Protocolo.SEPARADOR_CODIGO);
                ObjectOutputStream salida = gestorServidor.getSalida();
                salida.writeObject(paciente);
                salida.flush();
                
                ObjectInputStream entrada = gestorServidor.getEntrada();
                if (entrada.readInt() == Protocolo.ACTUALIZARPACIENTE_RESPONSE) {
                    guardado = true;
                }
                else {
                    guardado = false;
                }
                
                if (guardado) {
                    if (onGuardarCallback != null) {
                        onGuardarCallback.accept(paciente);
                    }
                    cerrarVentana();
                } else {
                    mostrarAlerta("Error", "Error al guardar",
                            "Ha ocurrido un error al intentar guardar el paciente.");
                }
            } catch (IOException ex) {
            }
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) rowContainer.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }


} 