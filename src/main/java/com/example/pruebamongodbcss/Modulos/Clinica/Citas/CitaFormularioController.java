package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import com.example.pruebamongodbcss.Data.EstadoCita;
import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica;
import com.example.pruebamongodbcss.Modulos.Clinica.PacienteEditRowController;
import com.example.pruebamongodbcss.Protocolo.Protocolo;

import Utilidades1.GestorSocket;
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
    @FXML private CheckBox chkDuracionManual;
    @FXML private ComboBox<Integer> cmbDuracion;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<String> cmbHoraFin;
    @FXML private ComboBox<String> cmbMinutoFin;
    @FXML private ComboBox<String> txtMotivo;
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
        configurarComboDuracion();
        configurarComboMotivo();
        configurarEventosDuracionManual();
        
        // Valores predeterminados
        dpFecha.setValue(LocalDate.now());
        cmbHora.getSelectionModel().select("09");
        cmbMinuto.getSelectionModel().select("00");
        cmbEstado.getSelectionModel().select(EstadoCita.PENDIENTE);
        cmbDuracion.getSelectionModel().select(Integer.valueOf(30)); // 30 minutos por defecto
        
        // Configurar fecha fin automática inicialmente
        actualizarFechaFinAutomatica();
    }
    
    /**
     * Configura el combo box de horas
     */
    private void configurarComboHoras() {
        List<String> horas = IntStream.rangeClosed(8, 20)
                .mapToObj(hora -> String.format("%02d", hora))
                .collect(Collectors.toList());
        cmbHora.setItems(FXCollections.observableArrayList(horas));
        cmbHoraFin.setItems(FXCollections.observableArrayList(horas));
    }
    
    /**
     * Configura el combo box de minutos
     */
    private void configurarComboMinutos() {
        List<String> minutos = Arrays.asList("00", "15", "30", "45");
        cmbMinuto.setItems(FXCollections.observableArrayList(minutos));
        cmbMinutoFin.setItems(FXCollections.observableArrayList(minutos));
    }
    
    /**
     * Configura el combo box de duración
     */
    private void configurarComboDuracion() {
        List<Integer> duraciones = Arrays.asList(15, 20, 30, 40, 45, 60, 70, 90, 120, 180, 240);
        cmbDuracion.setItems(FXCollections.observableArrayList(duraciones));
        cmbDuracion.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer duracion) {
                return duracion != null ? duracion + " min" : "";
            }
            
            @Override
            public Integer fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                try {
                    return Integer.parseInt(string.replace(" min", ""));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
    }
    
    /**
     * Configura el combo box de motivo
     */
    private void configurarComboMotivo() {
        // Configurar las opciones del ComboBox
        List<String> motivos = Arrays.asList(
            "Consulta", 
            "Revision", 
            "Cirugia básica", 
            "Cirugia compleja", 
            "Limpieza y uñas"
        );
        txtMotivo.setItems(FXCollections.observableArrayList(motivos));
        
        // Listener para cambiar la duración automáticamente según el motivo
        txtMotivo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Integer nuevaDuracion = obtenerDuracionPorMotivo(newVal);
                if (nuevaDuracion != null) {
                    System.out.println("🔄 Motivo cambiado a: " + newVal + " -> Duración: " + nuevaDuracion + " minutos");
                    
                    // Siempre actualizar la duración (incluso en modo manual)
                    cmbDuracion.getSelectionModel().select(nuevaDuracion);
                    
                    // Solo actualizar fecha fin automáticamente si no está en modo manual
                    if (!chkDuracionManual.isSelected()) {
                        actualizarFechaFinAutomatica();
                    }
                    
                    System.out.println("✅ Duración actualizada correctamente");
                }
            }
        });
    }
    
    /**
     * Obtiene la duración en minutos según el motivo seleccionado
     * @param motivo El motivo de la cita
     * @return La duración en minutos
     */
    private Integer obtenerDuracionPorMotivo(String motivo) {
        if (motivo == null) return null;
        
        switch (motivo) {
            case "Consulta":
                return 40;
            case "Revision":
                return 30;
            case "Cirugia básica":
                return 70;
            case "Cirugia compleja":
                return 90;
            case "Limpieza y uñas":
                return 20;
            default:
                return 30; // Duración por defecto
        }
    }
    
    /**
     * Configura los eventos para la duración manual
     */
    private void configurarEventosDuracionManual() {
        // Listener para el checkbox de duración manual
        chkDuracionManual.selectedProperty().addListener((obs, oldVal, newVal) -> {
            cmbDuracion.setDisable(!newVal);
            dpFechaFin.setDisable(!newVal);
            cmbHoraFin.setDisable(!newVal);
            cmbMinutoFin.setDisable(!newVal);
            
            if (newVal) {
                // Modo manual: habilitar controles de fecha fin
                dpFechaFin.setPromptText("Seleccionar fecha fin");
                actualizarFechaFinManual();
            } else {
                // Modo automático: calcular fecha fin basada en duración
                dpFechaFin.setPromptText("Fecha fin automática");
                actualizarFechaFinAutomatica();
            }
        });
        
        // Listeners para actualizar fecha fin automáticamente cuando cambie la fecha/hora de inicio
        dpFecha.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!chkDuracionManual.isSelected()) {
                actualizarFechaFinAutomatica();
            }
        });
        
        cmbHora.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!chkDuracionManual.isSelected()) {
                actualizarFechaFinAutomatica();
            }
        });
        
        cmbMinuto.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!chkDuracionManual.isSelected()) {
                actualizarFechaFinAutomatica();
            }
        });
        
        // Listener para actualizar fecha fin cuando cambie la duración
        cmbDuracion.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!chkDuracionManual.isSelected()) {
                actualizarFechaFinAutomatica();
            }
        });
    }
    
    /**
     * Actualiza la fecha fin automáticamente basada en la fecha/hora de inicio y duración
     */
    private void actualizarFechaFinAutomatica() {
        if (dpFecha.getValue() != null && 
            cmbHora.getValue() != null && 
            cmbMinuto.getValue() != null) {
            
            try {
                LocalDateTime fechaHoraInicio = obtenerFechaHoraSeleccionada();
                Integer duracion = cmbDuracion.getValue();
                if (duracion == null) {
                    duracion = DURACION_CITA_MINUTOS;
                }
                
                LocalDateTime fechaHoraFin = fechaHoraInicio.plusMinutes(duracion);
                
                dpFechaFin.setValue(fechaHoraFin.toLocalDate());
                cmbHoraFin.setValue(String.format("%02d", fechaHoraFin.getHour()));
                cmbMinutoFin.setValue(String.format("%02d", fechaHoraFin.getMinute()));
                
            } catch (Exception e) {
                // Ignorar errores de conversión
            }
        }
    }
    
    /**
     * Actualiza la fecha fin manualmente cuando está en modo manual
     */
    private void actualizarFechaFinManual() {
        if (dpFecha.getValue() != null) {
            // Por defecto, establecer la fecha fin igual a la fecha de inicio
            dpFechaFin.setValue(dpFecha.getValue());
            
            if (cmbHora.getValue() != null && cmbMinuto.getValue() != null) {
                try {
                    LocalDateTime fechaHoraInicio = obtenerFechaHoraSeleccionada();
                    LocalDateTime fechaHoraFin = fechaHoraInicio.plusMinutes(30); // 30 min por defecto
                    
                    cmbHoraFin.setValue(String.format("%02d", fechaHoraFin.getHour()));
                    cmbMinutoFin.setValue(String.format("%02d", fechaHoraFin.getMinute()));
                } catch (Exception e) {
                    // Establecer valores por defecto
                    cmbHoraFin.setValue("10");
                    cmbMinutoFin.setValue("00");
                }
            }
        }
    }
    
    /**
     * Maneja el cambio del checkbox de duración manual
     */
    @FXML
    private void onDuracionManualChanged() {
        // La lógica ya está manejada en configurarEventosDuracionManual()
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
        this.esEdicion = cita.getId() != null; // Solo es edición si tiene ID
        
        // Actualizar título
        if (esEdicion) {
            lblTitulo.setText("Editar Cita");
            System.out.println("Configurando cita para edición: " + cita.getId().toString());
        } else {
            lblTitulo.setText("Nueva Cita");
            System.out.println("Configurando nueva cita");
        }
        
        // Asegurarse que los datos necesarios estén cargados antes de popular el formulario
        if (pacientesObservable.isEmpty()) {
            cargarPacientes();
        }
        
        if (cmbVeterinario.getItems().isEmpty()) {
            cargarVeterinarios();
        }
        
        // Cargar datos de la cita en el formulario
        cargarDatosCita();
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
                    
                    // NUEVO: Configurar fecha fin y duración
                    if (cita.getFechaHoraFin() != null) {
                        LocalDateTime fechaHoraFin = cita.getFechaHoraFin();
                        
                        // Calcular duración
                        int duracionCalculada = (int) java.time.Duration.between(fechaHora, fechaHoraFin).toMinutes();
                        
                        // Verificar si es una duración estándar o personalizada
                        List<Integer> duracionesEstandar = Arrays.asList(15, 30, 45, 60, 90, 120, 180, 240);
                        boolean esDuracionEstandar = duracionesEstandar.contains(duracionCalculada);
                        
                        if (esDuracionEstandar && fechaHoraFin.toLocalDate().equals(fechaHora.toLocalDate())) {
                            // Modo automático: duración estándar en el mismo día
                            chkDuracionManual.setSelected(false);
                            cmbDuracion.setValue(duracionCalculada);
                        } else {
                            // Modo manual: duración personalizada o fecha fin diferente
                            chkDuracionManual.setSelected(true);
                            cmbDuracion.setValue(duracionCalculada);
                            
                            // Configurar fecha fin
                            dpFechaFin.setValue(fechaHoraFin.toLocalDate());
                            
                            // Configurar hora fin
                            String horaFinStr = String.format("%02d", fechaHoraFin.getHour());
                            String minutoFinStr = String.format("%02d", fechaHoraFin.getMinute());
                            
                            if (cmbHoraFin.getItems().contains(horaFinStr)) {
                                cmbHoraFin.getSelectionModel().select(horaFinStr);
                            }
                            
                            if (cmbMinutoFin.getItems().contains(minutoFinStr)) {
                                cmbMinutoFin.getSelectionModel().select(minutoFinStr);
                            }
                        }
                    } else {
                        // Si no hay fecha fin, usar duración por defecto
                        chkDuracionManual.setSelected(false);
                        int duracionCita = cita.getDuracionMinutos() > 0 ? cita.getDuracionMinutos() : DURACION_CITA_MINUTOS;
                        cmbDuracion.setValue(duracionCita);
                    }
                } else {
                    dpFecha.setValue(LocalDate.now());
                    if (!cmbHora.getItems().isEmpty()) cmbHora.getSelectionModel().selectFirst();
                    if (!cmbMinuto.getItems().isEmpty()) cmbMinuto.getSelectionModel().selectFirst();
                    chkDuracionManual.setSelected(false);
                    cmbDuracion.setValue(DURACION_CITA_MINUTOS);
                }
                
                // Establecer motivo
                if (cita.getMotivo() != null) {
                    txtMotivo.getSelectionModel().select(cita.getMotivo());
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
                
                // Verificar y corregir duración según el motivo (para citas existentes)
                verificarYCorregirDuracion();
                
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
        
        // Validar fecha fin si está en modo manual
        if (chkDuracionManual.isSelected()) {
            if (dpFechaFin.getValue() == null) {
                mostrarError("Debe seleccionar una fecha fin.");
                return false;
            }
            
            if (cmbHoraFin.getSelectionModel().isEmpty() || cmbMinutoFin.getSelectionModel().isEmpty()) {
                mostrarError("Debe seleccionar una hora fin válida.");
                return false;
            }
            
            // Validar que la fecha fin sea posterior a la fecha inicio
            LocalDateTime fechaHoraInicio = obtenerFechaHoraSeleccionada();
            LocalDateTime fechaHoraFin = obtenerFechaHoraFinSeleccionada();
            
            if (fechaHoraFin.isBefore(fechaHoraInicio) || fechaHoraFin.isEqual(fechaHoraInicio)) {
                mostrarError("La fecha y hora de fin debe ser posterior a la fecha y hora de inicio.");
                return false;
            }
        }
        
        if (txtMotivo.getSelectionModel().isEmpty()) {
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
                
                // Calcular duración para la validación
                int duracionValidacion;
                if (chkDuracionManual.isSelected()) {
                    LocalDateTime fechaHoraFin = obtenerFechaHoraFinSeleccionada();
                    duracionValidacion = (int) java.time.Duration.between(fechaHora, fechaHoraFin).toMinutes();
                } else {
                    Integer duracionSeleccionada = cmbDuracion.getValue();
                    duracionValidacion = (duracionSeleccionada != null) ? duracionSeleccionada : DURACION_CITA_MINUTOS;
                }
                
                gestorSocket.getSalida().writeInt(duracionValidacion);
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
            int duracionValidacion;
            if (chkDuracionManual.isSelected()) {
                LocalDateTime fechaHoraFin = obtenerFechaHoraFinSeleccionada();
                duracionValidacion = (int) java.time.Duration.between(fechaHora, fechaHoraFin).toMinutes();
            } else {
                Integer duracionSeleccionada = cmbDuracion.getValue();
                duracionValidacion = (duracionSeleccionada != null) ? duracionSeleccionada : DURACION_CITA_MINUTOS;
            }
            hayConflicto = servicio.hayConflictoHorario(fechaHora, duracionValidacion, citaId);
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
     * Obtiene la fecha y hora fin seleccionadas en el formulario
     */
    private LocalDateTime obtenerFechaHoraFinSeleccionada() {
        if (chkDuracionManual.isSelected()) {
            LocalDate fechaFin = dpFechaFin.getValue();
            int horaFin = Integer.parseInt(cmbHoraFin.getValue());
            int minutoFin = Integer.parseInt(cmbMinutoFin.getValue());
            
            return LocalDateTime.of(fechaFin, LocalTime.of(horaFin, minutoFin));
        } else {
            // Calcular automáticamente basado en duración
            LocalDateTime fechaHoraInicio = obtenerFechaHoraSeleccionada();
            Integer duracion = cmbDuracion.getValue();
            if (duracion == null) {
                duracion = DURACION_CITA_MINUTOS;
            }
            return fechaHoraInicio.plusMinutes(duracion);
        }
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
            
            // NUEVO: Establecer fecha fin y duración
            LocalDateTime fechaHoraFin = obtenerFechaHoraFinSeleccionada();
            cita.setFechaHoraFin(fechaHoraFin);
            
            // Calcular duración en minutos
            int duracionMinutos = (int) java.time.Duration.between(
                cita.getFechaHora(), fechaHoraFin).toMinutes();
            cita.setDuracionMinutos(duracionMinutos);
            
            // Agregar logs para debuggear
            String motivoSeleccionado = txtMotivo.getSelectionModel().getSelectedItem();
            Integer duracionSeleccionada = cmbDuracion.getValue();
            
            System.out.println("📝 DATOS DE GUARDADO:");
            System.out.println("   Motivo: " + motivoSeleccionado);
            System.out.println("   Duración del ComboBox: " + duracionSeleccionada + " min");
            System.out.println("   Fecha inicio: " + cita.getFechaHora());
            System.out.println("   Fecha fin: " + fechaHoraFin);
            System.out.println("   Duración calculada: " + duracionMinutos + " min");
            
            cita.setMotivo(motivoSeleccionado);
            cita.setEstado(cmbEstado.getValue());
            cita.setObservaciones(txtObservaciones.getText().trim());
            
            // Guardar en la base de datos usando una conexión independiente
            ObjectId id = null;
            GestorSocket gestorSocketIndependiente = null;
            
            try {
                if (gestorSocket != null) {
                    // Crear una nueva conexión independiente para evitar conflictos
                    gestorSocketIndependiente = GestorSocket.crearConexionIndependiente();
                    
                    System.out.println("🔄 Guardando cita con conexión independiente...");
                    
                    // Enviar petición de guardar cita
                    gestorSocketIndependiente.enviarPeticion(Protocolo.GUARDAR_CITA + Protocolo.SEPARADOR_CODIGO);
                    gestorSocketIndependiente.getSalida().writeObject(cita);
                    gestorSocketIndependiente.getSalida().flush();
                    
                    // Leer respuesta
                    int codigo = gestorSocketIndependiente.getEntrada().readInt();
                    if (codigo == Protocolo.GUARDAR_CITA_RESPONSE) {
                        id = (ObjectId) gestorSocketIndependiente.getEntrada().readObject();
                        System.out.println("✅ Cita guardada exitosamente con ID: " + id);
                    } else if (codigo == Protocolo.ERROR_GUARDAR_CITA) {
                        System.err.println("❌ Error del servidor al guardar cita");
                        mostrarError("Error del servidor al guardar la cita.");
                        return;
                    } else {
                        System.err.println("❌ Código de respuesta inesperado: " + codigo);
                        mostrarError("Respuesta inesperada del servidor.");
                        return;
                    }
                } else if (servicio != null) {
                    // Usar servicio directo (compatibilidad)
                    id = servicio.guardarCita(cita);
                    System.out.println("✅ Cita guardada directamente con servicio, ID: " + id);
                }
                
                if (id != null) {
                    System.out.println("🎉 Cita guardada correctamente, ejecutando callback...");
                    
                    // Ejecutar callback si existe (esto refrescará el calendario)
                    if (citaGuardadaCallback != null) {
                        // Ejecutar el callback en un hilo separado con un pequeño retraso
                        // para asegurar que la transacción se complete
                        new Thread(() -> {
                            try {
                                Thread.sleep(500); // Esperar 500ms para asegurar consistencia
                                javafx.application.Platform.runLater(() -> {
                                    citaGuardadaCallback.run();
                                });
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
                    
                    // Cerrar ventana
                    cerrarVentana();
                } else {
                    mostrarError("Ha ocurrido un error al guardar la cita.");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error al guardar la cita: " + e.getMessage());
                e.printStackTrace();
                mostrarError("Error al guardar la cita: " + e.getMessage());
            } finally {
                // Cerrar la conexión independiente
                if (gestorSocketIndependiente != null) {
                    try {
                        gestorSocketIndependiente.cerrarConexion();
                        System.out.println("🔌 Conexión independiente cerrada correctamente");
                    } catch (Exception e) {
                        System.err.println("Error al cerrar conexión independiente: " + e.getMessage());
                    }
                }
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

    /**
     * Verifica y corrige la duración de una cita según su motivo
     * Útil para citas existentes que no tienen la duración correcta
     */
    private void verificarYCorregirDuracion() {
        String motivo = txtMotivo.getSelectionModel().getSelectedItem();
        if (motivo != null) {
            Integer duracionEsperada = obtenerDuracionPorMotivo(motivo);
            Integer duracionActual = cmbDuracion.getValue();
            
            if (duracionEsperada != null && !duracionEsperada.equals(duracionActual)) {
                System.out.println("⚠️ Corrigiendo duración: " + duracionActual + " -> " + duracionEsperada + " min para motivo: " + motivo);
                cmbDuracion.getSelectionModel().select(duracionEsperada);
                
                // Actualizar fecha fin si no está en modo manual
                if (!chkDuracionManual.isSelected()) {
                    actualizarFechaFinAutomatica();
                }
            }
        }
    }
} 