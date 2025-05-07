package com.example.pruebamongodbcss.Modulos.Clinica;

import com.example.pruebamongodbcss.Data.EstadoCita;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.bson.types.ObjectId;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controlador para el formulario de citas
 */
public class CitaFormularioController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private ComboBox<ModeloPaciente> cmbPaciente;
    @FXML private ComboBox<String> cmbVeterinario;
    @FXML private MFXDatePicker dpFecha;
    @FXML private ComboBox<String> cmbHora;
    @FXML private ComboBox<String> cmbMinuto;
    @FXML private TextField txtMotivo;
    @FXML private ComboBox<EstadoCita> cmbEstado;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblError;
    @FXML private Button btnBuscarPaciente;
    @FXML private Button btnRazas;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    
    private ServicioClinica servicio;
    private ModeloCita cita;
    private boolean esEdicion = false;
    private Runnable citaGuardadaCallback;
    
    // Listas observables
    private ObservableList<ModeloPaciente> pacientesObservable;
    
    // Constantes
    private static final int DURACION_CITA_MINUTOS = 30;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar listas observables
        pacientesObservable = FXCollections.observableArrayList();
        
        // Configurar controles
        configurarComboHoras();
        configurarComboMinutos();
        configurarComboEstado();
        configurarComboPaciente();
        
        // Valores predeterminados
        dpFecha.setValue(LocalDate.now());
        cmbHora.getSelectionModel().select("09");
        cmbMinuto.getSelectionModel().select("00");
        cmbEstado.getSelectionModel().select(EstadoCita.PENDIENTE);
    }
    
    /**
     * Configura el combo box de horas
     */
    private void configurarComboHoras() {
        List<String> horas = IntStream.rangeClosed(8, 20)
                .mapToObj(hora -> String.format("%02d", hora))
                .collect(Collectors.toList());
        cmbHora.setItems(FXCollections.observableArrayList(horas));
    }
    
    /**
     * Configura el combo box de minutos
     */
    private void configurarComboMinutos() {
        List<String> minutos = Arrays.asList("00", "15", "30", "45");
        cmbMinuto.setItems(FXCollections.observableArrayList(minutos));
    }
    
    /**
     * Configura el combo box de estados
     */
    private void configurarComboEstado() {
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoCita.values()));
        cmbEstado.setConverter(new StringConverter<EstadoCita>() {
            @Override
            public String toString(EstadoCita estado) {
                return estado != null ? estado.getDescripcion() : "";
            }
            
            @Override
            public EstadoCita fromString(String string) {
                return Arrays.stream(EstadoCita.values())
                        .filter(estado -> estado.getDescripcion().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }
    
    /**
     * Configura el combo box de pacientes
     */
    private void configurarComboPaciente() {
        cmbPaciente.setItems(pacientesObservable);
        cmbPaciente.setConverter(new StringConverter<ModeloPaciente>() {
            @Override
            public String toString(ModeloPaciente paciente) {
                return paciente != null ? 
                       paciente.getNombre() + " (" + paciente.getEspecie() + " - " + paciente.getRaza() + ")" : 
                       "";
            }
            
            @Override
            public ModeloPaciente fromString(String string) {
                return null; // No se usa
            }
        });
        
        // Actualizar tipo de animal cuando se selecciona un paciente
        cmbPaciente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Si necesitamos hacer algo al seleccionar un paciente
            }
        });
    }
    
    /**
     * Establece el servicio de clínica
     */
    public void setServicio(ServicioClinica servicio) {
        this.servicio = servicio;
        
        // Cargar datos iniciales
        cargarPacientes();
        cargarVeterinarios();
    }
    
    /**
     * Establece el callback a ejecutar cuando se guarda una cita
     */
    public void setCitaGuardadaCallback(Runnable callback) {
        this.citaGuardadaCallback = callback;
    }
    
    /**
     * Establece la cita a editar
     */
    public void setCita(ModeloCita cita) {
        this.cita = cita;
        this.esEdicion = true;
        
        // Actualizar título
        lblTitulo.setText("Editar Cita");
        
        // Cargar datos de la cita
        cargarDatosCita();
    }
    
    /**
     * Carga la lista de pacientes
     */
    private void cargarPacientes() {
        if (servicio != null) {
            pacientesObservable.clear();
            
            List<ModeloPaciente> pacientes = servicio.obtenerTodosPacientes();
            pacientesObservable.addAll(pacientes);
        }
    }
    
    /**
     * Carga la lista de veterinarios
     */
    private void cargarVeterinarios() {
        if (servicio != null) {
            List<String> veterinarios = new ArrayList<>();
            
            // Aquí deberíamos cargar los veterinarios desde el servicio
            // Por ahora, usamos algunos nombres de ejemplo
            veterinarios.add("Dr. Juan Pérez");
            veterinarios.add("Dra. María González");
            veterinarios.add("Dr. Carlos Rodríguez");
            
            cmbVeterinario.setItems(FXCollections.observableArrayList(veterinarios));
            
            if (!veterinarios.isEmpty()) {
                cmbVeterinario.getSelectionModel().select(0);
            }
        }
    }
    
    /**
     * Carga los datos de la cita en el formulario
     */
    private void cargarDatosCita() {
        if (cita != null) {
            // Seleccionar paciente
            for (ModeloPaciente paciente : pacientesObservable) {
                if (paciente.getId().equals(cita.getPacienteId())) {
                    cmbPaciente.getSelectionModel().select(paciente);
                    break;
                }
            }
            
            // Seleccionar veterinario
            cmbVeterinario.getSelectionModel().select(cita.getNombreVeterinario());
            
            // Configurar fecha y hora
            LocalDateTime fechaHora = cita.getFechaHora();
            dpFecha.setValue(fechaHora.toLocalDate());
            cmbHora.getSelectionModel().select(String.format("%02d", fechaHora.getHour()));
            cmbMinuto.getSelectionModel().select(String.format("%02d", fechaHora.getMinute()));
            
            // Establecer motivo
            txtMotivo.setText(cita.getMotivo());
            
            // Seleccionar estado
            cmbEstado.getSelectionModel().select(cita.getEstado());
            
            // Establecer observaciones
            txtObservaciones.setText(cita.getObservaciones());
        }
    }
    
    /**
     * Valida los datos del formulario
     */
    private boolean validarFormulario() {
        // Validar que todos los campos requeridos estén completos
        if (cmbPaciente.getSelectionModel().isEmpty()) {
            mostrarError("Debe seleccionar un paciente.");
            return false;
        }
        
        if (cmbVeterinario.getSelectionModel().isEmpty()) {
            mostrarError("Debe seleccionar un veterinario.");
            return false;
        }
        
        if (dpFecha.getValue() == null) {
            mostrarError("Debe seleccionar una fecha.");
            return false;
        }
        
        if (cmbHora.getSelectionModel().isEmpty() || cmbMinuto.getSelectionModel().isEmpty()) {
            mostrarError("Debe seleccionar una hora válida.");
            return false;
        }
        
        if (txtMotivo.getText().trim().isEmpty()) {
            mostrarError("Debe especificar el motivo de la cita.");
            return false;
        }
        
        LocalDateTime fechaHora = obtenerFechaHoraSeleccionada();
        ObjectId citaId = (cita != null) ? cita.getId() : null;
        
        // Validar que la fecha no sea anterior a hoy
        if (fechaHora.isBefore(LocalDateTime.now())) {
            mostrarError("La fecha y hora de la cita no puede ser anterior a la actual.");
            return false;
        }
        
        // Validar que no haya conflicto de horarios
        if (servicio.hayConflictoHorario(fechaHora, DURACION_CITA_MINUTOS, citaId)) {
            mostrarError("Ya existe una cita programada para esa fecha y hora.");
            return false;
        }
        
        ocultarError();
        return true;
    }
    
    /**
     * Obtiene la fecha y hora seleccionadas en el formulario
     */
    private LocalDateTime obtenerFechaHoraSeleccionada() {
        LocalDate fecha = dpFecha.getValue();
        int hora = Integer.parseInt(cmbHora.getValue());
        int minuto = Integer.parseInt(cmbMinuto.getValue());
        
        return LocalDateTime.of(fecha, LocalTime.of(hora, minuto));
    }
    
    /**
     * Muestra un mensaje de error en el formulario
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
    
    /**
     * Oculta el mensaje de error en el formulario
     */
    private void ocultarError() {
        lblError.setVisible(false);
    }
    
    /**
     * Maneja el clic en el botón Guardar
     */
    @FXML
    private void onGuardar(ActionEvent event) {
        if (validarFormulario()) {
            // Crear o actualizar objeto de cita
            if (cita == null) {
                cita = new ModeloCita();
            }
            
            // Establecer datos de la cita
            ModeloPaciente paciente = cmbPaciente.getValue();
            cita.setPacienteId(paciente.getId());
            cita.setNombrePaciente(paciente.getNombre());
            cita.setTipoAnimal(paciente.getEspecie());
            cita.setRazaAnimal(paciente.getRaza());
            
            // El veterinario debería ser un objeto con ID, pero por simplicidad usamos solo el nombre
            cita.setNombreVeterinario(cmbVeterinario.getValue());
            
            cita.setFechaHora(obtenerFechaHoraSeleccionada());
            cita.setMotivo(txtMotivo.getText().trim());
            cita.setEstado(cmbEstado.getValue());
            cita.setObservaciones(txtObservaciones.getText().trim());
            
            // Guardar en la base de datos
            ObjectId id = servicio.guardarCita(cita);
            
            if (id != null) {
                // Ejecutar callback si existe
                if (citaGuardadaCallback != null) {
                    citaGuardadaCallback.run();
                }
                
                // Cerrar ventana
                cerrarVentana();
            } else {
                mostrarError("Ha ocurrido un error al guardar la cita.");
            }
        }
    }
    
    /**
     * Maneja el clic en el botón Buscar Paciente
     */
    @FXML
    private void onBuscarPaciente(ActionEvent event) {
        // Aquí se abriría un diálogo de búsqueda de pacientes
        // Por simplicidad, no lo implementamos en este ejemplo
    }
    
    /**
     * Maneja el clic en el botón Buscar Razas
     */
    @FXML
    private void onBuscarRazas(ActionEvent event) {
        ModeloPaciente paciente = cmbPaciente.getValue();
        if (paciente != null) {
            try {
                String tipoAnimal = paciente.getEspecie();
                if (tipoAnimal != null && !tipoAnimal.isEmpty()) {
                    String[] razas = servicio.buscarRazasPorTipoAnimal(tipoAnimal);
                    
                    // Crear diálogo para mostrar las razas
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Razas de " + tipoAnimal);
                    alert.setHeaderText("Razas disponibles para: " + tipoAnimal);
                    
                    TextArea textArea = new TextArea();
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setText(String.join("\n", razas));
                    
                    alert.getDialogPane().setContent(textArea);
                    alert.showAndWait();
                } else {
                    mostrarError("El paciente seleccionado no tiene un tipo de animal definido.");
                }
            } catch (Exception e) {
                mostrarError("Error al buscar razas: " + e.getMessage());
            }
        } else {
            mostrarError("Debe seleccionar un paciente.");
        }
    }
    
    /**
     * Maneja el clic en el botón Cancelar
     */
    @FXML
    private void onCancelar(ActionEvent event) {
        cerrarVentana();
    }
    
    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
} 