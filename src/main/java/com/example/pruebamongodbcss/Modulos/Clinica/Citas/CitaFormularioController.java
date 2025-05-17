package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import com.example.pruebamongodbcss.Data.EstadoCita;
import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica;

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
    @FXML private DatePicker dpFecha;
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
    private ObservableList<Usuario> veterinariosObservable;
    private java.util.Map<String, ObjectId> mapaVeterinariosId = new java.util.HashMap<>();
    
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
        
        System.out.println("Configurando cita para edición: " + cita.getId().toString());
        
        // Asegurarse que los datos necesarios estén cargados antes de popular el formulario
        if (pacientesObservable.isEmpty()) {
            cargarPacientes();
        }
        
        if (cmbVeterinario.getItems().isEmpty()) {
            cargarVeterinarios();
        }
        
        // Cargar datos de la cita después de un breve retraso para asegurar que los datos estén cargados
        javafx.application.Platform.runLater(() -> {
            cargarDatosCita();
        });
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
            mapaVeterinariosId.clear();
            
            // Cargar veterinarios desde la base de datos
            ServicioUsuarios servicioUsuarios = new ServicioUsuarios();
            List<Usuario> listaVeterinarios = servicioUsuarios.buscarVeterinarios();
            
            // Convertir la lista de objetos Usuario a una lista de nombres
            for (Usuario veterinario : listaVeterinarios) {
                String nombreCompleto = "Dr. " + veterinario.getNombre() + " " + veterinario.getApellido();
                veterinarios.add(nombreCompleto);
                mapaVeterinariosId.put(nombreCompleto, veterinario.getId());
            }
            
            // Si no hay veterinarios en la base de datos, usar datos de ejemplo
            if (veterinarios.isEmpty()) {
                veterinarios.add("Dr. Juan Pérez");
                veterinarios.add("Dra. María González");
                veterinarios.add("Dr. Carlos Rodríguez");
            }
            
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
            try {
                System.out.println("Cargando datos de cita: " + cita.getId());
                
                // Seleccionar paciente
                boolean pacienteEncontrado = false;
                if (cita.getPacienteId() != null) {
                    System.out.println("Buscando paciente con ID: " + cita.getPacienteId());
                    for (ModeloPaciente paciente : pacientesObservable) {
                        if (paciente.getId().equals(cita.getPacienteId())) {
                            System.out.println("Paciente encontrado: " + paciente.getNombre());
                            cmbPaciente.getSelectionModel().select(paciente);
                            pacienteEncontrado = true;
                            break;
                        }
                    }
                    
                    if (!pacienteEncontrado) {
                        System.out.println("Advertencia: No se encontró el paciente con ID: " + cita.getPacienteId());
                    }
                }
                
                // Seleccionar veterinario
                if (cita.getNombreVeterinario() != null && !cita.getNombreVeterinario().isEmpty()) {
                    System.out.println("Seleccionando veterinario: " + cita.getNombreVeterinario());
                    
                    // Verificar si el nombre del veterinario ya incluye el prefijo "Dr." o "Dra."
                    String nombreVeterinario = cita.getNombreVeterinario();
                    if (!nombreVeterinario.startsWith("Dr.") && !nombreVeterinario.startsWith("Dra.")) {
                        nombreVeterinario = "Dr. " + nombreVeterinario;
                    }
                    
                    // Imprimir los veterinarios disponibles para diagnóstico
                    System.out.println("Veterinarios disponibles:");
                    for (String vet : cmbVeterinario.getItems()) {
                        System.out.println(" - " + vet);
                    }
                    
                    // Seleccionar el veterinario por el nombre exacto o buscando coincidencia parcial
                    if (cmbVeterinario.getItems().contains(nombreVeterinario)) {
                        cmbVeterinario.getSelectionModel().select(nombreVeterinario);
                        System.out.println("Veterinario seleccionado: " + nombreVeterinario);
                    } else {
                        // Buscar por coincidencia parcial
                        boolean encontrado = false;
                        for (String vet : cmbVeterinario.getItems()) {
                            if (vet.contains(nombreVeterinario) || nombreVeterinario.contains(vet)) {
                                cmbVeterinario.getSelectionModel().select(vet);
                                System.out.println("Veterinario seleccionado por coincidencia parcial: " + vet);
                                encontrado = true;
                                break;
                            }
                        }
                        
                        if (!encontrado && !cmbVeterinario.getItems().isEmpty()) {
                            // Si no encuentra coincidencia, seleccionar el primero
                            cmbVeterinario.getSelectionModel().selectFirst();
                            System.out.println("No se encontró el veterinario, se seleccionó el primero por defecto: " + cmbVeterinario.getValue());
                        }
                    }
                } else if (!cmbVeterinario.getItems().isEmpty()) {
                    // Si no hay veterinario asignado, seleccionar el primero
                    cmbVeterinario.getSelectionModel().selectFirst();
                    System.out.println("No hay veterinario asignado, se seleccionó el primero por defecto: " + cmbVeterinario.getValue());
                }
                
                // Configurar fecha y hora
                if (cita.getFechaHora() != null) {
                    LocalDateTime fechaHora = cita.getFechaHora();
                    System.out.println("Configurando fecha: " + fechaHora);
                    
                    // Asegurar que el datepicker tenga un valor
                    dpFecha.setValue(fechaHora.toLocalDate());
                    
                    // Formatear hora y minuto con dos dígitos
                    String horaStr = String.format("%02d", fechaHora.getHour());
                    String minutoStr = String.format("%02d", fechaHora.getMinute());
                    
                    System.out.println("Hora: " + horaStr + ", Minuto: " + minutoStr);
                    
                    // Seleccionar hora y minuto si están en las listas
                    if (cmbHora.getItems().contains(horaStr)) {
                        cmbHora.getSelectionModel().select(horaStr);
                    } else if (!cmbHora.getItems().isEmpty()) {
                        cmbHora.getSelectionModel().selectFirst();
                    }
                    
                    if (cmbMinuto.getItems().contains(minutoStr)) {
                        cmbMinuto.getSelectionModel().select(minutoStr);
                    } else if (!cmbMinuto.getItems().isEmpty()) {
                        cmbMinuto.getSelectionModel().selectFirst();
                    }
                } else {
                    System.out.println("No hay fecha y hora en la cita");
                    dpFecha.setValue(LocalDate.now());
                    if (!cmbHora.getItems().isEmpty()) cmbHora.getSelectionModel().selectFirst();
                    if (!cmbMinuto.getItems().isEmpty()) cmbMinuto.getSelectionModel().selectFirst();
                }
                
                // Establecer motivo
                if (cita.getMotivo() != null) {
                    txtMotivo.setText(cita.getMotivo());
                    System.out.println("Motivo: " + cita.getMotivo());
                }
                
                // Seleccionar estado
                if (cita.getEstado() != null) {
                    cmbEstado.getSelectionModel().select(cita.getEstado());
                    System.out.println("Estado: " + cita.getEstado());
                } else if (!cmbEstado.getItems().isEmpty()) {
                    cmbEstado.getSelectionModel().selectFirst();
                }
                
                // Establecer observaciones
                if (cita.getObservaciones() != null) {
                    txtObservaciones.setText(cita.getObservaciones());
                    System.out.println("Observaciones configuradas");
                }
                
                System.out.println("Carga de datos completada");
            } catch (Exception e) {
                System.err.println("Error al cargar datos de la cita: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No hay cita para cargar");
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
            
            // Obtener el veterinario seleccionado
            String nombreVeterinario = cmbVeterinario.getValue();
            cita.setNombreVeterinario(nombreVeterinario);
            
            // Si existe el ID del veterinario en el mapa, asignarlo
            if (mapaVeterinariosId.containsKey(nombreVeterinario)) {
                cita.setVeterinarioId(mapaVeterinariosId.get(nombreVeterinario));
            }
            
            // IMPORTANTE: Extraer username del veterinario para asignar la cita
            // Quitar el "Dr. " o "Dra. " si existe
            String nombreSinTitulo = nombreVeterinario;
            if (nombreVeterinario.startsWith("Dr. ") || nombreVeterinario.startsWith("Dra. ")) {
                nombreSinTitulo = nombreVeterinario.substring(4);
            }
            
            // Buscar el usuario del veterinario seleccionado
            ServicioUsuarios servicioUsuarios = new ServicioUsuarios();
            try {
                // Dividir nombre y apellido
                String[] partes = nombreSinTitulo.split(" ", 2);
                String nombre = partes[0].trim();
                String apellido = partes.length > 1 ? partes[1].trim() : "";
                
                // Buscar usuario que coincida con el nombre o apellido
                List<Usuario> posiblesUsuarios = servicioUsuarios.buscarUsuariosPorTexto(nombre);
                Usuario veterinario = null;
                
                // Buscar coincidencia exacta de nombre y apellido
                for (Usuario u : posiblesUsuarios) {
                    if (u.getNombre().equalsIgnoreCase(nombre) && 
                        u.getApellido().toLowerCase().contains(apellido.toLowerCase())) {
                        veterinario = u;
                        break;
                    }
                }
                
                if (veterinario != null) {
                    // Si encontramos el usuario, usar su username
                    cita.setUsuarioAsignado(veterinario.getUsuario());
                    System.out.println("Usuario asignado a la cita: " + veterinario.getUsuario());
                } else {
                    // Si no lo encontramos, intentamos construir un username típico (primera letra + apellido)
                    if (!nombre.isEmpty() && !apellido.isEmpty()) {
                        String usuarioGenerado = (nombre.substring(0, 1) + apellido).toLowerCase();
                        cita.setUsuarioAsignado(usuarioGenerado);
                        System.out.println("Usuario generado para la cita: " + usuarioGenerado);
                    } else {
                        // Fallback a sistema si no podemos generar nada
                        cita.setUsuarioAsignado("sistema");
                        System.out.println("Usuario por defecto para la cita: sistema");
                    }
                }
            } catch (Exception e) {
                // Si algo falla, usar un valor por defecto
                cita.setUsuarioAsignado("sistema");
                System.out.println("Error al buscar usuario para veterinario: " + e.getMessage());
            }
            
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