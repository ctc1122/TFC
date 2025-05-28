package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.EstadoCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.example.pruebamongodbcss.Modulos.Facturacion.FacturacionController;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import com.example.pruebamongodbcss.calendar.google.GoogleCalendarWebView;

import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador para la gestión de citas
 */
public class CitasController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private TabPane tabPane;
    
    // Tab de Lista de Citas
    @FXML private Tab tabListaCitas;
    @FXML private TableView<ModeloCita> tablaCitas;
    @FXML private TableColumn<ModeloCita, String> colFecha;
    @FXML private TableColumn<ModeloCita, String> colHora;
    @FXML private TableColumn<ModeloCita, String> colPaciente;
    @FXML private TableColumn<ModeloCita, String> colTipoAnimal;
    @FXML private TableColumn<ModeloCita, String> colVeterinario;
    @FXML private TableColumn<ModeloCita, String> colMotivo;
    @FXML private TableColumn<ModeloCita, String> colEstado;
    
    @FXML private TextField txtBuscarCita;
    @FXML private MFXDatePicker dpFechaInicio;
    @FXML private MFXDatePicker dpFechaFin;
    @FXML private ComboBox<String> cmbEstadoFiltro;
    @FXML private Button btnFacturarCita;
    
    // Tab de Calendario
    @FXML private Tab tabCalendario;
    @FXML private BorderPane calendarContainer;
    @FXML private Label lblMesActual;
    @FXML private Button btnMesAnterior;
    @FXML private Button btnMesSiguiente;
    
    // Servicio de clínica
    private GestorSocket gestorSocket;
    
    // Lista observable para la tabla de citas
    private ObservableList<ModeloCita> citasObservable;
    
    private GoogleCalendarWebView calendarView;
    
    // Formato para la fecha y hora
    private DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Inicializar el gestor de socket
            gestorSocket = GestorSocket.getInstance();
            
            // Configurar listas observables
            citasObservable = FXCollections.observableArrayList();
            
            // Configurar tabla de citas
            configurarTablaCitas();
            
            // Configurar campo de filtro por estado
            configurarFiltroEstado();
            
            // Inicializar fechas de búsqueda
            LocalDate hoy = LocalDate.now();
            dpFechaInicio.setValue(hoy.withDayOfMonth(1));
            dpFechaFin.setValue(hoy.withDayOfMonth(hoy.lengthOfMonth()));
            
            // Configurar calendario
            configurarCalendario();
            
            // Agregar listener para búsqueda en tiempo real
            txtBuscarCita.textProperty().addListener((obs, oldVal, newVal) -> {
                cargarCitas();
            });
            
            // Cargar citas iniciales
            Platform.runLater(() -> {
                cargarCitas();
            });
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de inicialización", "Error al inicializar el controlador de citas", 
                "Ha ocurrido un error al inicializar el controlador: " + e.getMessage());
        }
    }
    
    /**
     * Configura la tabla de citas
     */
    private void configurarTablaCitas() {
        colFecha.setCellValueFactory(data -> {
            LocalDateTime fechaHora = data.getValue().getFechaHora();
            return new SimpleStringProperty(fechaHora.format(formatoFecha));
        });
        
        colHora.setCellValueFactory(data -> {
            LocalDateTime fechaHora = data.getValue().getFechaHora();
            return new SimpleStringProperty(fechaHora.format(formatoHora));
        });
        
        colPaciente.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNombrePaciente()));
        
        colTipoAnimal.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTipoAnimal()));
        
        colVeterinario.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNombreVeterinario()));
        
        colMotivo.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getMotivo()));
        
        colEstado.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEstadoAsString()));
        
        tablaCitas.setItems(citasObservable);
        
        // Manejar doble clic en una cita
        tablaCitas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaCitas.getSelectionModel().getSelectedItem() != null) {
                onEditarCita(new ActionEvent());
            }
        });
        
        // Personalizar celdas de estado para mostrar colores
        colEstado.setCellFactory(col -> new TableCell<ModeloCita, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    ModeloCita cita = getTableView().getItems().get(getIndex());
                    
                    switch (cita.getEstado()) {
                        case PENDIENTE:
                            setStyle("-fx-background-color: #fff9c4;"); // Amarillo claro
                            break;
                        case EN_CURSO:
                            setStyle("-fx-background-color: #bbdefb;"); // Azul claro
                            break;
                        case COMPLETADA:
                            setStyle("-fx-background-color: #c8e6c9;"); // Verde claro
                            break;
                        case CANCELADA:
                            setStyle("-fx-background-color: #ffcdd2;"); // Rojo claro
                            break;
                        case REPROGRAMADA:
                            setStyle("-fx-background-color: #e1bee7;"); // Morado claro
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }
    
    /**
     * Configura el combo box de filtro por estado
     */
    private void configurarFiltroEstado() {
        cmbEstadoFiltro.getItems().add("Todos");
        for (EstadoCita estado : EstadoCita.values()) {
            cmbEstadoFiltro.getItems().add(estado.getDescripcion());
        }
        cmbEstadoFiltro.getSelectionModel().select(0);
    }
    
    /**
     * Carga las citas desde el servicio
     */
    private void cargarCitas() {
        System.out.println("🔄 Iniciando carga de citas en CitasController...");
        citasObservable.clear();
        
        // Usar una conexión independiente para evitar conflictos
        GestorSocket gestorSocketCitas = null;
        try {
            gestorSocketCitas = GestorSocket.crearConexionIndependiente();
            
            // Primero intentar obtener todas las citas
            List<ModeloCita> todasLasCitas = obtenerTodasLasCitas(gestorSocketCitas);
            System.out.println("📋 Total de citas obtenidas: " + todasLasCitas.size());
            
            if (todasLasCitas.isEmpty()) {
                System.out.println("⚠️ No se encontraron citas en la base de datos");
                return;
            }
            
            List<ModeloCita> citasFiltradas = new ArrayList<>(todasLasCitas);
            
            // Aplicar filtro por rango de fechas si están definidas
            if (dpFechaInicio.getValue() != null && dpFechaFin.getValue() != null) {
                LocalDate fechaInicio = dpFechaInicio.getValue();
                LocalDate fechaFin = dpFechaFin.getValue();
                
                citasFiltradas.removeIf(cita -> {
                    LocalDate fechaCita = cita.getFechaHora().toLocalDate();
                    return fechaCita.isBefore(fechaInicio) || fechaCita.isAfter(fechaFin);
                });
                
                System.out.println("📅 Citas después de filtro por fechas (" + fechaInicio + " - " + fechaFin + "): " + citasFiltradas.size());
            }
            
            // Aplicar filtro por búsqueda de texto si existe
            String textoBusqueda = txtBuscarCita.getText().trim().toLowerCase();
            if (!textoBusqueda.isEmpty()) {
                citasFiltradas.removeIf(cita -> 
                    !cita.getNombrePaciente().toLowerCase().contains(textoBusqueda) && 
                    !cita.getNombreVeterinario().toLowerCase().contains(textoBusqueda) &&
                    !cita.getMotivo().toLowerCase().contains(textoBusqueda));
                
                System.out.println("🔍 Citas después de filtro por texto '" + textoBusqueda + "': " + citasFiltradas.size());
            }
            
            // Aplicar filtro por estado si está seleccionado
            String estadoSeleccionado = cmbEstadoFiltro.getSelectionModel().getSelectedItem();
            if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos")) {
                citasFiltradas.removeIf(cita -> !cita.getEstadoAsString().equals(estadoSeleccionado));
                System.out.println("📊 Citas después de filtro por estado '" + estadoSeleccionado + "': " + citasFiltradas.size());
            }
            
            // Agregar las citas filtradas a la tabla
            citasObservable.addAll(citasFiltradas);
            System.out.println("✅ Citas cargadas en la tabla: " + citasObservable.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar citas: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar citas", 
                "No se pudieron cargar las citas: " + e.getMessage());
        } finally {
            // Cerrar la conexión independiente
            if (gestorSocketCitas != null) {
                try {
                    gestorSocketCitas.cerrarConexion();
                    System.out.println("🔌 Conexión de citas cerrada correctamente");
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión de citas: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Obtiene todas las citas desde el servidor usando una conexión específica
     */
    private List<ModeloCita> obtenerTodasLasCitas(GestorSocket gestorSocketEspecifico) {
        try {
            System.out.println("📡 Solicitando todas las citas al servidor...");
            gestorSocketEspecifico.enviarPeticion(Protocolo.DAMETODASLASCITAS + Protocolo.SEPARADOR_CODIGO);
            
            int codigo = gestorSocketEspecifico.getEntrada().readInt();
            if (codigo == Protocolo.DAMETODASLASCITAS_RESPONSE) {
                // El servidor devuelve CalendarEvent, necesitamos convertir a ModeloCita
                @SuppressWarnings("unchecked")
                List<com.example.pruebamongodbcss.calendar.CalendarEvent> eventos = 
                    (List<com.example.pruebamongodbcss.calendar.CalendarEvent>) gestorSocketEspecifico.getEntrada().readObject();
                
                System.out.println("📦 Eventos recibidos del servidor: " + eventos.size());
                
                // Convertir CalendarEvent a ModeloCita solo para citas médicas
                List<ModeloCita> citas = new ArrayList<>();
                for (com.example.pruebamongodbcss.calendar.CalendarEvent evento : eventos) {
                    if (evento.getTipoEvento() == com.example.pruebamongodbcss.calendar.CalendarEvent.EventoTipo.CITA_MEDICA) {
                        ModeloCita cita = convertirEventoACita(evento);
                        if (cita != null) {
                            citas.add(cita);
                        }
                    }
                }
                
                System.out.println("🏥 Citas médicas convertidas: " + citas.size());
                return citas;
                
            } else if (codigo == Protocolo.ERROR_DAMETODASLASCITAS) {
                String errorMsg = gestorSocketEspecifico.getEntrada().readUTF();
                System.err.println("❌ Error del servidor: " + errorMsg);
                throw new RuntimeException("Error del servidor: " + errorMsg);
            } else {
                System.err.println("❌ Código de respuesta inesperado: " + codigo);
                throw new RuntimeException("Respuesta inesperada del servidor (código: " + codigo + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener todas las citas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error de comunicación: " + e.getMessage());
        }
    }
    
    /**
     * Convierte un CalendarEvent a ModeloCita
     */
    private ModeloCita convertirEventoACita(com.example.pruebamongodbcss.calendar.CalendarEvent evento) {
        try {
            ModeloCita cita = new ModeloCita();
            
            System.out.println("🔄 Convirtiendo evento: " + evento.getId() + " - " + evento.getTitle());
            
            // ID
            if (evento.getId() != null && !evento.getId().isEmpty()) {
                cita.setId(new org.bson.types.ObjectId(evento.getId()));
            }
            
            // Título y motivo
            cita.setMotivo(evento.getTitle() != null ? evento.getTitle() : "Sin motivo");
            
            // Fecha y hora
            if (evento.getStart() != null) {
                LocalDateTime fechaHora = parseDateTime(evento.getStart());
                cita.setFechaHora(fechaHora);
            }
            
            // Estado
            if (evento.getEstado() != null) {
                try {
                    EstadoCita estado = EstadoCita.valueOf(evento.getEstado().toUpperCase());
                    cita.setEstado(estado);
                } catch (IllegalArgumentException e) {
                    cita.setEstado(EstadoCita.PENDIENTE);
                }
            } else {
                cita.setEstado(EstadoCita.PENDIENTE);
            }
            
            // Veterinario
            if (evento.getUsuario() != null) {
                cita.setUsuarioAsignado(evento.getUsuario());
                cita.setNombreVeterinario("Dr. " + evento.getUsuario());
            }
            
            // Ubicación
            if (evento.getLocation() != null) {
                cita.setObservaciones("Ubicación: " + evento.getLocation());
            }
            
            // MÉTODO ALTERNATIVO: Obtener la cita completa desde la base de datos
            // En lugar de intentar reconstruir desde CalendarEvent, obtenemos la cita original
            if (evento.getId() != null && !evento.getId().isEmpty()) {
                try {
                    ModeloCita citaCompleta = obtenerCitaCompletaPorId(evento.getId());
                    if (citaCompleta != null) {
                        System.out.println("✅ Cita completa obtenida para: " + evento.getId());
                        return citaCompleta;
                    } else {
                        System.out.println("⚠️ No se pudo obtener cita completa para: " + evento.getId());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al obtener cita completa: " + e.getMessage());
                }
            }
            
            // Si no pudimos obtener la cita completa, intentar obtener el paciente por separado
            System.out.println("🔍 Intentando obtener paciente por separado...");
            System.out.println("   PacienteId del evento: " + evento.getPacienteId());
            
            if (evento.getPacienteId() != null && !evento.getPacienteId().isEmpty()) {
                try {
                    org.bson.types.ObjectId pacienteId = new org.bson.types.ObjectId(evento.getPacienteId());
                    cita.setPacienteId(pacienteId);
                    
                    // Obtener datos del paciente
                    ModeloPaciente paciente = obtenerPacientePorIdDirecto(pacienteId);
                    if (paciente != null) {
                        cita.setNombrePaciente(paciente.getNombre());
                        cita.setTipoAnimal(paciente.getEspecie());
                        cita.setRazaAnimal(paciente.getRaza());
                        System.out.println("✅ Paciente obtenido: " + paciente.getNombre());
                    } else {
                        System.out.println("⚠️ No se pudo obtener paciente con ID: " + pacienteId);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al procesar paciente para cita " + evento.getId() + ": " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ Evento sin pacienteId: " + evento.getId());
            }
            
            return cita;
            
        } catch (Exception e) {
            System.err.println("❌ Error al convertir evento a cita: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene una cita completa por ID desde la base de datos
     */
    private ModeloCita obtenerCitaCompletaPorId(String citaId) {
        GestorSocket gestorSocketCita = null;
        try {
            gestorSocketCita = GestorSocket.crearConexionIndependiente();
            gestorSocketCita.enviarPeticion(Protocolo.OBTENER_CITA_POR_ID + Protocolo.SEPARADOR_CODIGO + citaId);
            
            int codigo = gestorSocketCita.getEntrada().readInt();
            if (codigo == Protocolo.OBTENER_CITA_POR_ID_RESPONSE) {
                ModeloCita cita = (ModeloCita) gestorSocketCita.getEntrada().readObject();
                
                // Si la cita tiene pacienteId pero no nombre, obtener el paciente
                if (cita != null && cita.getPacienteId() != null && 
                    (cita.getNombrePaciente() == null || cita.getNombrePaciente().isEmpty())) {
                    
                    ModeloPaciente paciente = obtenerPacientePorIdDirecto(cita.getPacienteId());
                    if (paciente != null) {
                        cita.setNombrePaciente(paciente.getNombre());
                        cita.setTipoAnimal(paciente.getEspecie());
                        cita.setRazaAnimal(paciente.getRaza());
                    }
                }
                
                return cita;
            } else if (codigo == Protocolo.ERROR_OBTENER_CITA_POR_ID) {
                String errorMsg = gestorSocketCita.getEntrada().readUTF();
                System.err.println("Error del servidor al obtener cita: " + errorMsg);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener cita completa " + citaId + ": " + e.getMessage());
        } finally {
            if (gestorSocketCita != null) {
                try {
                    gestorSocketCita.cerrarConexion();
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión de cita: " + e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * Obtiene un paciente por ID usando una conexión directa
     */
    private ModeloPaciente obtenerPacientePorIdDirecto(org.bson.types.ObjectId pacienteId) {
        GestorSocket gestorSocketPaciente = null;
        try {
            System.out.println("🔍 Obteniendo paciente con ID: " + pacienteId);
            gestorSocketPaciente = GestorSocket.crearConexionIndependiente();
            gestorSocketPaciente.enviarPeticion(Protocolo.OBTENERPACIENTE_POR_ID + Protocolo.SEPARADOR_CODIGO + pacienteId.toString());
            
            int codigo = gestorSocketPaciente.getEntrada().readInt();
            if (codigo == Protocolo.OBTENERPACIENTE_POR_ID_RESPONSE) {
                ModeloPaciente paciente = (ModeloPaciente) gestorSocketPaciente.getEntrada().readObject();
                if (paciente != null) {
                    System.out.println("✅ Paciente encontrado: " + paciente.getNombre() + " (" + paciente.getEspecie() + ")");
                } else {
                    System.out.println("⚠️ Paciente es null para ID: " + pacienteId);
                }
                return paciente;
            } else if (codigo == Protocolo.ERROROBTENERPACIENTE_POR_ID) {
                String errorMsg = gestorSocketPaciente.getEntrada().readUTF();
                System.err.println("❌ Error del servidor al obtener paciente: " + errorMsg);
            } else {
                System.err.println("❌ Código de respuesta inesperado: " + codigo);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener paciente " + pacienteId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (gestorSocketPaciente != null) {
                try {
                    gestorSocketPaciente.cerrarConexion();
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión de paciente: " + e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * Convierte una cadena de fecha ISO a LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // Manejar tanto formato ISO completo como sin zona horaria
            String cleanedStr = dateTimeStr.replace("Z", "");
            if (cleanedStr.contains("T")) {
                return LocalDateTime.parse(cleanedStr);
            } else {
                // Si no tiene formato ISO con T, intentar interpretar como fecha local
                return LocalDateTime.parse(cleanedStr);
            }
        } catch (Exception e) {
            System.err.println("Error al parsear fecha: " + dateTimeStr + " - " + e.getMessage());
            return LocalDateTime.now();
        }
    }
    
    private boolean eliminarCita(org.bson.types.ObjectId citaId) {
        try {
            gestorSocket.enviarPeticion(Protocolo.ELIMINAR_CITA + Protocolo.SEPARADOR_CODIGO + citaId.toString());
            
            int codigo = gestorSocket.getEntrada().readInt();
            if (codigo == Protocolo.ELIMINAR_CITA_RESPONSE) {
                return gestorSocket.getEntrada().readBoolean();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al eliminar cita", 
                "No se pudo eliminar la cita: " + e.getMessage());
        }
        return false;
    }
    
    private ModeloPaciente obtenerPacientePorId(org.bson.types.ObjectId pacienteId) {
        try {
            gestorSocket.enviarPeticion(Protocolo.OBTENERPACIENTE_POR_ID + Protocolo.SEPARADOR_CODIGO + pacienteId.toString());
            
            int codigo = gestorSocket.getEntrada().readInt();
            if (codigo == Protocolo.OBTENERPACIENTE_POR_ID_RESPONSE) {
                return (ModeloPaciente) gestorSocket.getEntrada().readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al obtener paciente", 
                "No se pudo obtener el paciente: " + e.getMessage());
        }
        return null;
    }
    
    private ModeloPropietario obtenerPropietarioPorId(org.bson.types.ObjectId propietarioId) {
        try {
            gestorSocket.enviarPeticion(Protocolo.OBTENERPROPIETARIO_POR_ID + Protocolo.SEPARADOR_CODIGO + propietarioId.toString());
            
            int codigo = gestorSocket.getEntrada().readInt();
            if (codigo == Protocolo.OBTENERPROPIETARIO_POR_ID_RESPONSE) {
                return (ModeloPropietario) gestorSocket.getEntrada().readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al obtener propietario", 
                "No se pudo obtener el propietario: " + e.getMessage());
        }
        return null;
    }
    
    private boolean cambiarEstadoCita(org.bson.types.ObjectId citaId, EstadoCita nuevoEstado) {
        try {
            gestorSocket.enviarPeticion(Protocolo.CAMBIAR_ESTADO_CITA + Protocolo.SEPARADOR_CODIGO + 
                citaId.toString() + Protocolo.SEPARADOR_PARAMETROS + nuevoEstado.name());
            
            int codigo = gestorSocket.getEntrada().readInt();
            if (codigo == Protocolo.CAMBIAR_ESTADO_CITA_RESPONSE) {
                return gestorSocket.getEntrada().readBoolean();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cambiar estado", 
                "No se pudo cambiar el estado de la cita: " + e.getMessage());
        }
        return false;
    }
    
    private List<ModeloCita> buscarCitasPorPaciente(org.bson.types.ObjectId pacienteId) {
        try {
            gestorSocket.enviarPeticion(Protocolo.BUSCAR_CITAS_POR_PACIENTE + Protocolo.SEPARADOR_CODIGO + pacienteId.toString());
            
            int codigo = gestorSocket.getEntrada().readInt();
            if (codigo == Protocolo.BUSCAR_CITAS_POR_PACIENTE_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloCita> citas = (List<ModeloCita>) gestorSocket.getEntrada().readObject();
                return citas;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al buscar citas del paciente", 
                "No se pudieron cargar las citas del paciente: " + e.getMessage());
        }
        return new java.util.ArrayList<>();
    }
    
    /**
     * Maneja el clic en el botón Buscar
     */
    @FXML
    private void onBuscarCitas(ActionEvent event) {
        cargarCitas();
    }
    
    /**
     * Maneja el clic en el botón Filtrar por Estado
     */
    @FXML
    private void onFiltrarPorEstado(ActionEvent event) {
        cargarCitas();
    }
    
    /**
     * Maneja el clic en el botón Nueva Cita
     */
    @FXML
    private void onNuevaCita(ActionEvent event) {
        abrirFormularioCita(null);
    }
    
    /**
     * Maneja el clic en el botón Editar Cita
     */
    @FXML
    private void onEditarCita(ActionEvent event) {
        ModeloCita citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            abrirFormularioCita(citaSeleccionada);
        } else {
            mostrarAlerta("Selección requerida", "No hay cita seleccionada", 
                "Por favor, seleccione una cita para editar.");
        }
    }
    
    /**
     * Maneja el clic en el botón Eliminar Cita
     */
    @FXML
    private void onEliminarCita(ActionEvent event) {
        ModeloCita citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                "¿Está seguro que desea eliminar esta cita?", 
                "Esta acción no se puede deshacer.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = eliminarCita(citaSeleccionada.getId());
                if (eliminado) {
                    cargarCitas();
                    mostrarMensaje("Cita eliminada", "La cita ha sido eliminada", 
                        "La cita ha sido eliminada exitosamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la cita", 
                        "Ha ocurrido un error al intentar eliminar la cita.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay cita seleccionada", 
                "Por favor, seleccione una cita para eliminar.");
        }
    }
    
    /**
     * Maneja el clic en el botón Facturar Cita
     */
    @FXML
    private void onFacturarCita(ActionEvent event) {
        ModeloCita citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            try {
                // Obtener datos del paciente y propietario
                ModeloPaciente paciente = obtenerPacientePorId(citaSeleccionada.getPacienteId());
                if (paciente == null) {
                    mostrarAlerta("Error", "Paciente no encontrado", 
                        "No se pudo encontrar el paciente asociado a esta cita.");
                    return;
                }
                
                ModeloPropietario propietario = obtenerPropietarioPorId(paciente.getPropietarioId());
                if (propietario == null) {
                    mostrarAlerta("Error", "Propietario no encontrado", 
                        "No se pudo encontrar el propietario del paciente.");
                    return;
                }
                
                // Cargar el módulo de facturación
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/facturacion-view.fxml"));
                Parent root = loader.load();
                
                FacturacionController facturacionController = loader.getController();
                
                // Crear nueva factura desde la cita
                facturacionController.crearFacturaDesdeCita(citaSeleccionada, paciente, propietario);
                
                Stage stage = new Stage();
                stage.setTitle("Facturación - Cita: " + citaSeleccionada.getNombrePaciente());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.showAndWait();
                
                // Actualizar la lista de citas después de facturar
                cargarCitas();
                
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error al abrir facturación", 
                    "No se pudo abrir el módulo de facturación: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay cita seleccionada", 
                "Por favor, seleccione una cita para facturar.");
        }
    }
    
    /**
     * Maneja el clic en el botón Cambiar Estado Cita
     */
    @FXML
    private void onCambiarEstadoCita(ActionEvent event) {
        ModeloCita citaSeleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            // Crear un diálogo para seleccionar el nuevo estado
            Dialog<EstadoCita> dialog = new Dialog<>();
            dialog.setTitle("Cambiar Estado");
            dialog.setHeaderText("Seleccione el nuevo estado para la cita");
            
            // Configurar botones
            ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, ButtonType.CANCEL);
            
            // Crear y configurar combo box
            ComboBox<EstadoCita> comboEstados = new ComboBox<>();
            comboEstados.getItems().addAll(EstadoCita.values());
            comboEstados.setValue(citaSeleccionada.getEstado());
            
            // Agregar el combo al contenido del diálogo
            dialog.getDialogPane().setContent(comboEstados);
            
            // Convertir resultado
            dialog.setResultConverter(boton -> {
                if (boton == btnAceptar) {
                    return comboEstados.getValue();
                }
                return null;
            });
            
            // Mostrar el diálogo y procesar resultado
            Optional<EstadoCita> resultado = dialog.showAndWait();
            if (resultado.isPresent()) {
                EstadoCita nuevoEstado = resultado.get();
                if (nuevoEstado != citaSeleccionada.getEstado()) {
                    boolean actualizado = cambiarEstadoCita(citaSeleccionada.getId(), nuevoEstado);
                    if (actualizado) {
                        cargarCitas();
                        mostrarMensaje("Estado actualizado", "Estado de la cita actualizado", 
                            "El estado de la cita ha sido actualizado exitosamente.");
                    } else {
                        mostrarAlerta("Error", "No se pudo actualizar el estado", 
                            "Ha ocurrido un error al intentar actualizar el estado de la cita.");
                    }
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay cita seleccionada", 
                "Por favor, seleccione una cita para cambiar su estado.");
        }
    }
    
    /**
     * Maneja el clic en el botón Mes Anterior
     */
    @FXML
    private void onMesAnterior(ActionEvent event) {
        // Implementar la lógica para cambiar al mes anterior
    }
    
    /**
     * Maneja el clic en el botón Mes Siguiente
     */
    @FXML
    private void onMesSiguiente(ActionEvent event) {
        // Implementar la lógica para cambiar al mes siguiente
    }
    
    /**
     * Actualiza la etiqueta del mes actual
     */
    private void actualizarLabelMes() {
        // Implementar la lógica para actualizar la etiqueta del mes actual
    }
    
    /**
     * Filtra las citas para mostrar solo las de un paciente específico
     * @param pacienteId ID del paciente
     */
    public void filtrarPorPaciente(org.bson.types.ObjectId pacienteId) {
        if (pacienteId == null) {
            return;
        }
        
        try {
            // Cambiar a la pestaña de lista de citas
            tabPane.getSelectionModel().select(tabListaCitas);
            
            // Buscar las citas del paciente
            List<ModeloCita> citasPaciente = buscarCitasPorPaciente(pacienteId);
            
            // Actualizar la tabla
            citasObservable.clear();
            citasObservable.addAll(citasPaciente);
            
            // Mostrar mensaje informativo
            if (citasPaciente.isEmpty()) {
                mostrarMensaje("Sin citas", "No hay citas", 
                        "No hay citas registradas para este paciente.");
            } else {
                // Obtener el nombre del paciente de la primera cita
                String nombrePaciente = citasPaciente.get(0).getNombrePaciente();
                mostrarMensaje("Citas filtradas", "Mostrando citas del paciente", 
                        "Se muestran las citas para el paciente: " + nombrePaciente);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al filtrar citas", 
                    "Ha ocurrido un error al intentar filtrar las citas: " + e.getMessage());
        }
    }
    
    /**
     * Configura el calendario
     */
    private void configurarCalendario() {
        try {
            // Crear y configurar el calendario
            calendarView = new GoogleCalendarWebView();
            
            // Asignar el calendario al contenedor
            if (calendarContainer != null) {
                calendarContainer.setCenter(calendarView);
            } else {
                System.err.println("Error: calendarContainer es null");
            }
            
            // Ocultar los botones de navegación ya que el calendario web tiene los suyos
            if (btnMesAnterior != null) btnMesAnterior.setVisible(false);
            if (btnMesSiguiente != null) btnMesSiguiente.setVisible(false);
            if (lblMesActual != null) lblMesActual.setVisible(false);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al configurar calendario", 
                         "No se pudo configurar el calendario web: " + e.getMessage());
        }
    }
    
    /**
     * Abre el formulario para crear o editar una cita
     */
    private void abrirFormularioCita(ModeloCita cita) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/cita-formulario.fxml"));
            Parent root = loader.load();
            
            CitaFormularioController controller = loader.getController();
            controller.setGestorSocket(gestorSocket);
            
            if (cita != null) {
                controller.setCita(cita);
            }
            
            // Configurar callback para refrescar la tabla cuando se guarde la cita
            controller.setCitaGuardadaCallback(() -> {
                System.out.println("🔄 Cita guardada desde formulario, refrescando tabla...");
                Platform.runLater(() -> {
                    cargarCitas();
                    System.out.println("✅ Tabla de citas refrescada");
                });
            });
            
            Stage stage = new Stage();
            stage.setTitle(cita == null ? "Nueva Cita" : "Editar Cita");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refrescar la tabla después de cerrar el formulario
            cargarCitas();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir formulario", 
                "No se pudo abrir el formulario de citas: " + e.getMessage());
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
    
    private void mostrarMensaje(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    private Optional<ButtonType> mostrarConfirmacion(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        return alert.showAndWait();
    }
    
    /**
     * Método público para refrescar las citas desde otros controladores
     */
    public void refrescarCitas() {
        System.out.println("🔄 Refrescando citas desde controlador externo...");
        Platform.runLater(() -> {
            cargarCitas();
        });
    }
} 