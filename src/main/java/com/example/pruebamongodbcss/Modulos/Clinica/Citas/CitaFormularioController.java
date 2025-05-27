package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import com.example.pruebamongodbcss.Data.EstadoCita;
import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica;
import com.example.pruebamongodbcss.Modulos.Clinica.PacienteEditRowController;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
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
import java.util.Map;
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
    private GestorSocket gestorSocket;
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
     * Establece el gestor de socket
     */
    public void setGestorSocket(GestorSocket gestorSocket) {
        this.gestorSocket = gestorSocket;
        
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
        pacientesObservable.clear();
        
        if (gestorSocket != null) {
            // Usar gestorSocket
            try {
                gestorSocket.enviarPeticion(Protocolo.OBTENER_TODOS_PACIENTES + Protocolo.SEPARADOR_CODIGO);
                
                int codigo = gestorSocket.getEntrada().readInt();
                if (codigo == Protocolo.OBTENER_TODOS_PACIENTES_RESPONSE) {
                    @SuppressWarnings("unchecked")
                    List<ModeloPaciente> pacientes = (List<ModeloPaciente>) gestorSocket.getEntrada().readObject();
                    pacientesObservable.addAll(pacientes);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar pacientes: " + e.getMessage());
            }
        } else if (servicio != null) {
            // Usar servicio directo (compatibilidad)
            List<ModeloPaciente> pacientes = servicio.obtenerTodosPacientes();
            pacientesObservable.addAll(pacientes);
        }
    }
    
    /**
     * Carga la lista de veterinarios
     */
    private void cargarVeterinarios() {
        List<String> veterinarios = new ArrayList<>();
        mapaVeterinariosId.clear();
        
        if (gestorSocket != null) {
            // Usar gestorSocket
            try {
                gestorSocket.enviarPeticion(Protocolo.GETALLVETERINARIOS + Protocolo.SEPARADOR_CODIGO);
                gestorSocket.getSalida().writeObject(Usuario.Rol.VETERINARIO);
                gestorSocket.getSalida().flush();
                
                int codigo = gestorSocket.getEntrada().readInt();
                if (codigo == Protocolo.GETALLVETERINARIOS_RESPONSE) {
                    @SuppressWarnings("unchecked")
                    List<Usuario> listaVeterinarios = (List<Usuario>) gestorSocket.getEntrada().readObject();
                    
                    // Convertir la lista de objetos Usuario a una lista de nombres
                    for (Usuario veterinario : listaVeterinarios) {
                        String nombreCompleto = "Dr. " + veterinario.getNombre() + " " + veterinario.getApellido();
                        veterinarios.add(nombreCompleto);
                        mapaVeterinariosId.put(nombreCompleto, veterinario.getId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar veterinarios: " + e.getMessage());
            }
        } else if (servicio != null) {
            // Usar servicio directo (compatibilidad)
            ServicioUsuarios servicioUsuarios = new ServicioUsuarios();
            List<Usuario> listaVeterinarios = servicioUsuarios.buscarVeterinarios();
            
            // Convertir la lista de objetos Usuario a una lista de nombres
            for (Usuario veterinario : listaVeterinarios) {
                String nombreCompleto = "Dr. " + veterinario.getNombre() + " " + veterinario.getApellido();
                veterinarios.add(nombreCompleto);
                mapaVeterinariosId.put(nombreCompleto, veterinario.getId());
            }
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
    
    /**
     * Carga los datos de la cita en el formulario
     */
    private void cargarDatosCita() {
        if (cita != null) {
            try {
                // Seleccionar paciente
                boolean pacienteEncontrado = false;
                if (cita.getPacienteId() != null) {
                    for (ModeloPaciente paciente : pacientesObservable) {
                        if (paciente.getId().equals(cita.getPacienteId())) {
                            cmbPaciente.getSelectionModel().select(paciente);
                            pacienteEncontrado = true;
                            break;
                        }
                    }
                }
                
                // Asegurar que los veterinarios estén cargados
                if (cmbVeterinario.getItems().isEmpty()) {
                    cargarVeterinarios();
                }
                
                // SOLUCIÓN SIMPLIFICADA: Buscar el veterinario por nombre exacto
                boolean veterinarioEncontrado = false;
                String nombreVeterinario = cita.getNombreVeterinario();
                
                if (nombreVeterinario != null && !nombreVeterinario.isEmpty()) {
                    // Asegurarse que tiene el prefijo "Dr." o "Dra."
                    if (!nombreVeterinario.startsWith("Dr.") && !nombreVeterinario.startsWith("Dra.")) {
                        nombreVeterinario = "Dr. " + nombreVeterinario;
                    }
                    
                    // Buscar coincidencia exacta en la lista
                    for (String vet : cmbVeterinario.getItems()) {
                        if (vet.equals(nombreVeterinario)) {
                            cmbVeterinario.getSelectionModel().select(vet);
                            veterinarioEncontrado = true;
                            break;
                        }
                    }
                    
                    // Si no hay coincidencia exacta, buscar coincidencia parcial
                    if (!veterinarioEncontrado) {
                        for (String vet : cmbVeterinario.getItems()) {
                            // Quitar "Dr." o "Dra." para comparación
                            String nombreVetLimpio = nombreVeterinario.replace("Dr. ", "").replace("Dra. ", "");
                            String vetLimpio = vet.replace("Dr. ", "").replace("Dra. ", "");
                            
                            if (vetLimpio.contains(nombreVetLimpio) || nombreVetLimpio.contains(vetLimpio)) {
                                cmbVeterinario.getSelectionModel().select(vet);
                                veterinarioEncontrado = true;
                                break;
                            }
                        }
                    }
                }
                
                // Si no se encontró el veterinario por nombre, usar el primero disponible
                if (!veterinarioEncontrado && !cmbVeterinario.getItems().isEmpty()) {
                    cmbVeterinario.getSelectionModel().selectFirst();
                }
                
                // Configurar fecha y hora
                if (cita.getFechaHora() != null) {
                    LocalDateTime fechaHora = cita.getFechaHora();
                    
                    // Configurar fecha
                    dpFecha.setValue(fechaHora.toLocalDate());
                    
                    // Configurar hora y minuto
                    String horaStr = String.format("%02d", fechaHora.getHour());
                    String minutoStr = String.format("%02d", fechaHora.getMinute());
                    
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
                    dpFecha.setValue(LocalDate.now());
                    if (!cmbHora.getItems().isEmpty()) cmbHora.getSelectionModel().selectFirst();
                    if (!cmbMinuto.getItems().isEmpty()) cmbMinuto.getSelectionModel().selectFirst();
                }
                
                // Establecer motivo
                if (cita.getMotivo() != null) {
                    txtMotivo.setText(cita.getMotivo());
                }
                
                // Seleccionar estado
                if (cita.getEstado() != null) {
                    cmbEstado.getSelectionModel().select(cita.getEstado());
                } else if (!cmbEstado.getItems().isEmpty()) {
                    cmbEstado.getSelectionModel().selectFirst();
                }
                
                // Establecer observaciones
                if (cita.getObservaciones() != null) {
                    txtObservaciones.setText(cita.getObservaciones());
                }
                
            } catch (Exception e) {
                System.err.println("Error al cargar datos de la cita: " + e.getMessage());
                e.printStackTrace();
            }
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
        boolean hayConflicto = false;
        if (gestorSocket != null) {
            // Usar gestorSocket
            try {
                gestorSocket.enviarPeticion(Protocolo.HAY_CONFLICTO_HORARIO + Protocolo.SEPARADOR_CODIGO);
                gestorSocket.getSalida().writeObject(fechaHora);
                gestorSocket.getSalida().writeInt(DURACION_CITA_MINUTOS);
                gestorSocket.getSalida().writeObject(citaId);
                gestorSocket.getSalida().flush();
                
                int codigo = gestorSocket.getEntrada().readInt();
                if (codigo == Protocolo.HAY_CONFLICTO_HORARIO_RESPONSE) {
                    hayConflicto = gestorSocket.getEntrada().readBoolean();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError("Error al validar conflicto de horarios: " + e.getMessage());
                return false;
            }
        } else if (servicio != null) {
            // Usar servicio directo (compatibilidad)
            hayConflicto = servicio.hayConflictoHorario(fechaHora, DURACION_CITA_MINUTOS, citaId);
        }
        
        if (hayConflicto) {
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
            try {
                ServicioUsuarios servicioUsuarios = new ServicioUsuarios();
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
                    cita.setUsuarioAsignado(veterinario.getUsuario());
                } else {
                    cita.setUsuarioAsignado("sistema");
                }
            } catch (Exception e) {
                cita.setUsuarioAsignado("sistema");
                System.out.println("Error al buscar usuario para veterinario: " + e.getMessage());
            }
            
            cita.setFechaHora(obtenerFechaHoraSeleccionada());
            cita.setMotivo(txtMotivo.getText().trim());
            cita.setEstado(cmbEstado.getValue());
            cita.setObservaciones(txtObservaciones.getText().trim());
            
            // Guardar en la base de datos
            ObjectId id = null;
            if (gestorSocket != null) {
                // Usar gestorSocket
                try {
                    gestorSocket.enviarPeticion(Protocolo.GUARDAR_CITA + Protocolo.SEPARADOR_CODIGO);
                    gestorSocket.getSalida().writeObject(cita);
                    gestorSocket.getSalida().flush();
                    
                    int codigo = gestorSocket.getEntrada().readInt();
                    if (codigo == Protocolo.GUARDAR_CITA_RESPONSE) {
                        id = (ObjectId) gestorSocket.getEntrada().readObject();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarError("Error al guardar la cita: " + e.getMessage());
                    return;
                }
            } else if (servicio != null) {
                // Usar servicio directo (compatibilidad)
                id = servicio.guardarCita(cita);
            }
            
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
                    String[] razas = null;
                    
                    if (gestorSocket != null) {
                        // Usar gestorSocket
                        try {
                            gestorSocket.enviarPeticion(Protocolo.BUSCAR_RAZAS_POR_TIPO_ANIMAL + Protocolo.SEPARADOR_CODIGO + tipoAnimal);
                            
                            int codigo = gestorSocket.getEntrada().readInt();
                            if (codigo == Protocolo.BUSCAR_RAZAS_POR_TIPO_ANIMAL_RESPONSE) {
                                razas = (String[]) gestorSocket.getEntrada().readObject();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mostrarError("Error al buscar razas: " + e.getMessage());
                            return;
                        }
                    } else if (servicio != null) {
                        // Usar servicio directo (compatibilidad)
                        razas = servicio.buscarRazasPorTipoAnimal(tipoAnimal);
                    }
                    
                    if (razas != null) {
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
                        mostrarError("No se encontraron razas para el tipo de animal: " + tipoAnimal);
                    }
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

    @FXML
    private void onNuevoPaciente(ActionEvent event) {
        try {
            // Cargar el formulario de nuevo paciente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/paciente-edit-row.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador
            PacienteEditRowController controller = loader.getController();
            controller.setServicio(servicio);
            
            // Crear un nuevo paciente vacío
            ModeloPaciente nuevoPaciente = new ModeloPaciente();
            controller.configurar(nuevoPaciente, true, (pacienteCreado, confirmado) -> {
                if (confirmado && pacienteCreado != null) {
                    pacientesObservable.add(pacienteCreado);
                    cmbPaciente.getSelectionModel().select(pacienteCreado);
                    
                    // Ejecutar callback si existe para actualizar el calendario
                    if (citaGuardadaCallback != null) {
                        citaGuardadaCallback.run();
                    }
                }
            });
            
            // Mostrar el formulario en una ventana modal
            Stage stage = new Stage();
            stage.setTitle("Nuevo Paciente");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir el formulario de nuevo paciente: " + e.getMessage());
        }
    }
} 