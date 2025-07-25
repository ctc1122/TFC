package com.example.pruebamongodbcss.calendar;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.PanelInicioController;
import com.example.pruebamongodbcss.Data.ServicioUsuarios;
import com.example.pruebamongodbcss.calendar.CalendarEvent.EventoTipo;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import Utilidades1.GestorSocket;
import java.io.ObjectInputStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.example.pruebamongodbcss.Modulos.InicioSesion.PanelInicioSesionController;

/**
 * Controlador para el formulario de eventos (reuniones y recordatorios)
 */
public class EventoFormularioController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtUbicacion;
    @FXML private ComboBox<String> cmbParticipantes;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbHora;
    @FXML private ComboBox<String> cmbMinuto;
    @FXML private ComboBox<String> cmbHoraFin;
    @FXML private ComboBox<String> cmbMinutoFin;
    @FXML private ComboBox<EventoTipo> cmbTipoEvento;
    @FXML private ColorPicker colorPicker;
    @FXML private Label lblError;
    @FXML private CheckBox chkTodoElDia;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    
    private GestorSocket gestorSocket;
    private CalendarEvent evento;
    private boolean esEdicion = false;
    private Runnable eventoGuardadoCallback;
    
    // Listas observables
    private ObservableList<String> participantesObservable;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar listas observables
        participantesObservable = FXCollections.observableArrayList();
        
        // Configurar controles
        configurarComboHoras();
        configurarComboMinutos();
        configurarComboTipoEvento();
        configurarParticipantes();
        
        // Valores predeterminados
        dpFecha.setValue(LocalDate.now());
        cmbHora.getSelectionModel().select("09");
        cmbMinuto.getSelectionModel().select("00");
        cmbHoraFin.getSelectionModel().select("10");
        cmbMinutoFin.getSelectionModel().select("00");
        cmbTipoEvento.getSelectionModel().select(EventoTipo.REUNION);
        colorPicker.setValue(javafx.scene.paint.Color.web("#4285f4"));
        
        // Configurar handler para el checkbox de todo el día
        chkTodoElDia.selectedProperty().addListener((obs, oldVal, newVal) -> {
            cmbHora.setDisable(newVal);
            cmbMinuto.setDisable(newVal);
            cmbHoraFin.setDisable(newVal);
            cmbMinutoFin.setDisable(newVal);
        });
    }
    
    /**
     * Configura el combo box de horas
     */
    private void configurarComboHoras() {
        List<String> horas = IntStream.rangeClosed(0, 23)
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
     * Configura el combo box de tipos de evento
     */
    private void configurarComboTipoEvento() {
        cmbTipoEvento.setItems(FXCollections.observableArrayList(
            EventoTipo.REUNION, EventoTipo.RECORDATORIO
        ));
        
        cmbTipoEvento.setConverter(new StringConverter<EventoTipo>() {
            @Override
            public String toString(EventoTipo tipo) {
                return tipo != null ? tipo.getDescripcion() : "";
            }
            
            @Override
            public EventoTipo fromString(String string) {
                return Arrays.stream(EventoTipo.values())
                        .filter(tipo -> tipo.getDescripcion().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        
        // Cambiar colores según el tipo de evento
        cmbTipoEvento.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == EventoTipo.REUNION) {
                colorPicker.setValue(javafx.scene.paint.Color.web("#9c27b0")); // Morado para reuniones
            } else if (newVal == EventoTipo.RECORDATORIO) {
                colorPicker.setValue(javafx.scene.paint.Color.web("#ff9800")); // Naranja para recordatorios
            }
        });
    }
    
    /**
     * Configura el combo box de participantes
     */
    private void configurarParticipantes() {
        cmbParticipantes.setItems(participantesObservable);
        cmbParticipantes.setEditable(true);
    }
    
    /**
     * Establece el gestor de socket para comunicación cliente-servidor
     */
    public void setGestorSocket(GestorSocket gestorSocket) {
        this.gestorSocket = gestorSocket;
        
        // Cargar datos iniciales
        cargarParticipantes();
    }
    
    /**
     * Establece el callback a ejecutar cuando se guarda un evento
     */
    public void setEventoGuardadoCallback(Runnable callback) {
        this.eventoGuardadoCallback = callback;
    }
    
    /**
     * Establece el evento a editar
     */
    public void setEvento(CalendarEvent evento) {
        this.evento = evento;
        this.esEdicion = true;
        
        // Actualizar título
        lblTitulo.setText("Editar " + (evento.getTipoEvento() == EventoTipo.REUNION ? "Reunión" : "Recordatorio"));
        
        // Cargar datos del evento
        cargarDatosEvento();
    }
    
    /**
     * Carga la lista de participantes usando el protocolo cliente-servidor
     */
    private void cargarParticipantes() {
        if (gestorSocket != null) {
            participantesObservable.clear();
            
            try {
                // Solicitar todos los usuarios al servidor
                gestorSocket.enviarPeticion(Protocolo.GETALLUSERS + Protocolo.SEPARADOR_CODIGO);
                ObjectInputStream ois = gestorSocket.getEntrada();
                int codigo = ois.readInt();
                
                if (codigo == Protocolo.GETALLUSERS_RESPONSE) {
                    @SuppressWarnings("unchecked")
                    List<Usuario> listaUsuarios = (List<Usuario>) ois.readObject();
                    
                    // Convertir la lista de objetos Usuario a una lista de nombres
                    for (Usuario usuario : listaUsuarios) {
                        String nombreCompleto = usuario.getNombre() + " " + usuario.getApellido();
                        participantesObservable.add(nombreCompleto);
                    }
                } else {
                    System.err.println("Error al cargar usuarios para participantes");
                }
            } catch (Exception e) {
                System.err.println("Error al cargar usuarios: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Si no hay usuarios en la base de datos, usar datos de ejemplo
            if (participantesObservable.isEmpty()) {
                participantesObservable.add("Juan Pérez");
                participantesObservable.add("María González");
                participantesObservable.add("Carlos Rodríguez");
            }
        }
    }
    
    /**
     * Carga los datos del evento en el formulario
     */
    private void cargarDatosEvento() {
        if (evento != null) {
            // Establecer título y descripción
            txtTitulo.setText(evento.getTitle());
            txtDescripcion.setText(evento.getDescription());
            txtUbicacion.setText(evento.getLocation());
            
            // Configurar fecha y hora con manejo de diferentes formatos
            LocalDateTime fechaHoraInicio;
            LocalDateTime fechaHoraFin;
            
            try {
                // Intentar parsear como ISO_LOCAL_DATE_TIME primero
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                fechaHoraInicio = LocalDateTime.parse(evento.getStart(), formatter);
                fechaHoraFin = LocalDateTime.parse(evento.getEnd(), formatter);
            } catch (Exception e) {
                try {
                    // Si falla, intentar parsear solo como fecha (ISO_LOCAL_DATE)
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
                    LocalDate fechaInicio = LocalDate.parse(evento.getStart(), dateFormatter);
                    LocalDate fechaFin = LocalDate.parse(evento.getEnd(), dateFormatter);
                    
                    // Asignar horas por defecto
                    fechaHoraInicio = fechaInicio.atTime(9, 0);  // 9:00 AM por defecto
                    fechaHoraFin = fechaFin.atTime(10, 0);      // 10:00 AM por defecto
                    
                    System.out.println("Fecha parseada como DATE, asignando horas por defecto");
                } catch (Exception ex) {
                    // Si también falla, usar fecha y hora actual
                    System.err.println("Error al parsear fechas del evento: " + ex.getMessage());
                    fechaHoraInicio = LocalDateTime.now();
                    fechaHoraFin = LocalDateTime.now().plusHours(1);
                }
            }
            
            dpFecha.setValue(fechaHoraInicio.toLocalDate());
            cmbHora.getSelectionModel().select(String.format("%02d", fechaHoraInicio.getHour()));
            cmbMinuto.getSelectionModel().select(String.format("%02d", fechaHoraInicio.getMinute()));
            cmbHoraFin.getSelectionModel().select(String.format("%02d", fechaHoraFin.getHour()));
            cmbMinutoFin.getSelectionModel().select(String.format("%02d", fechaHoraFin.getMinute()));
            
            // Establecer tipo de evento
            cmbTipoEvento.getSelectionModel().select(evento.getTipoEvento());
            
            // Establecer color
            if (evento.getColor() != null && !evento.getColor().isEmpty()) {
                try {
                    colorPicker.setValue(javafx.scene.paint.Color.web(evento.getColor()));
                } catch (Exception e) {
                    System.err.println("Error al convertir color: " + evento.getColor());
                }
            }
            
            // Establecer todo el día
            chkTodoElDia.setSelected(evento.isAllDay());
        }
    }
    
    /**
     * Valida los datos del formulario
     */
    private boolean validarFormulario() {
        // Validar que todos los campos requeridos estén completos
        if (txtTitulo.getText().trim().isEmpty()) {
            mostrarError("Debe ingresar un título para el evento.");
            return false;
        }
        
        if (dpFecha.getValue() == null) {
            mostrarError("Debe seleccionar una fecha.");
            return false;
        }
        
        if (!chkTodoElDia.isSelected() && 
            (cmbHora.getSelectionModel().isEmpty() || cmbMinuto.getSelectionModel().isEmpty() || 
             cmbHoraFin.getSelectionModel().isEmpty() || cmbMinutoFin.getSelectionModel().isEmpty())) {
            mostrarError("Debe seleccionar la hora de inicio y fin.");
            return false;
        }
        
        if (cmbTipoEvento.getSelectionModel().isEmpty()) {
            mostrarError("Debe seleccionar un tipo de evento.");
            return false;
        }
        
        // Validar que la hora de fin sea posterior a la de inicio
        if (!chkTodoElDia.isSelected()) {
            LocalDateTime inicio = obtenerFechaHoraInicio();
            LocalDateTime fin = obtenerFechaHoraFin();
            
            if (fin.isBefore(inicio) || fin.isEqual(inicio)) {
                mostrarError("La hora de fin debe ser posterior a la hora de inicio.");
                return false;
            }
        }
        
        ocultarError();
        return true;
    }
    
    /**
     * Obtiene la fecha y hora de inicio seleccionadas en el formulario
     */
    private LocalDateTime obtenerFechaHoraInicio() {
        LocalDate fecha = dpFecha.getValue();
        
        if (chkTodoElDia.isSelected()) {
            // Si es todo el día, hora 0:00
            return LocalDateTime.of(fecha, LocalTime.of(0, 0));
        } else {
            int hora = Integer.parseInt(cmbHora.getValue());
            int minuto = Integer.parseInt(cmbMinuto.getValue());
            return LocalDateTime.of(fecha, LocalTime.of(hora, minuto));
        }
    }
    
    /**
     * Obtiene la fecha y hora de fin seleccionadas en el formulario
     */
    private LocalDateTime obtenerFechaHoraFin() {
        LocalDate fecha = dpFecha.getValue();
        
        if (chkTodoElDia.isSelected()) {
            // Si es todo el día, hora 23:59
            return LocalDateTime.of(fecha, LocalTime.of(23, 59));
        } else {
            int hora = Integer.parseInt(cmbHoraFin.getValue());
            int minuto = Integer.parseInt(cmbMinutoFin.getValue());
            return LocalDateTime.of(fecha, LocalTime.of(hora, minuto));
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
            // Crear o actualizar objeto de evento
            if (evento == null) {
                evento = new CalendarEvent();
            }
            
            // Establecer datos del evento
            evento.setTitle(txtTitulo.getText().trim());
            
            // Manejar descripción de forma segura (evitar NullPointerException)
            String descripcion = txtDescripcion.getText();
            evento.setDescription(descripcion != null ? descripcion.trim() : "");
            
            // Manejar ubicación de forma segura
            String ubicacion = txtUbicacion.getText();
            evento.setLocation(ubicacion != null ? ubicacion.trim() : "");
            
            // Fechas y horas
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime inicio = obtenerFechaHoraInicio();
            LocalDateTime fin = obtenerFechaHoraFin();
            
            evento.setStart(inicio.format(formatter));
            evento.setEnd(fin.format(formatter));
            
            // Tipo de evento
            evento.setTipoEvento(cmbTipoEvento.getValue());
            
            // Color
            evento.setColor(colorPicker.getValue().toString().replace("0x", "#"));
            
            // Todo el día
            evento.setAllDay(chkTodoElDia.isSelected());
            
            // Estado (pendiente por defecto)
            evento.setEstado("PENDIENTE");



            evento.setUsuario(PanelInicioController.getUsuarioSesion().getUsuario());
            
            // Guardar en la base de datos usando el protocolo cliente-servidor
            try {
                if (esEdicion) {
                    // Actualizar evento existente
                    gestorSocket.enviarPeticion(Protocolo.ACTUALIZAR_EVENTO_CALENDARIO + Protocolo.SEPARADOR_CODIGO);
                    gestorSocket.getSalida().writeObject(evento);
                    gestorSocket.getSalida().flush();
                    
                    ObjectInputStream ois = gestorSocket.getEntrada();
                    int codigo = ois.readInt();
                    
                    if (codigo == Protocolo.ACTUALIZAR_EVENTO_CALENDARIO_RESPONSE) {
                        boolean actualizado = ois.readBoolean();
                        if (actualizado) {
                            // Ejecutar callback si existe
                            if (eventoGuardadoCallback != null) {
                                eventoGuardadoCallback.run();
                            }
                            // Cerrar ventana
                            cerrarVentana();
                        } else {
                            mostrarError("No se pudo actualizar el evento.");
                        }
                    } else {
                        mostrarError("Error del servidor al actualizar el evento.");
                    }
                } else {
                    // Crear nuevo evento
                    gestorSocket.enviarPeticion(Protocolo.GUARDAR_EVENTO_CALENDARIO + Protocolo.SEPARADOR_CODIGO);
                    gestorSocket.getSalida().writeObject(evento);
                    gestorSocket.getSalida().flush();
                    
                    ObjectInputStream ois = gestorSocket.getEntrada();
                    int codigo = ois.readInt();
                    
                    if (codigo == Protocolo.GUARDAR_EVENTO_CALENDARIO_RESPONSE) {
                        CalendarEvent guardado = (CalendarEvent) ois.readObject();
                        if (guardado != null) {
                            // Ejecutar callback si existe
                            if (eventoGuardadoCallback != null) {
                                eventoGuardadoCallback.run();
                            }
                            // Cerrar ventana
                            cerrarVentana();
                        } else {
                            mostrarError("No se pudo guardar el evento.");
                        }
                    } else {
                        mostrarError("Error del servidor al guardar el evento.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al comunicarse con el servidor: " + e.getMessage());
                e.printStackTrace();
                mostrarError("Error de comunicación con el servidor.");
            }
        }
    }
    
    /**
     * Maneja el clic en el botón Cancelar
     */
    @FXML
    private void onCancelar(ActionEvent event) {
        // Si estamos creando un evento nuevo (no edición) y ya se creó la entrada visual,
        // necesitamos informar que se canceló para que se elimine del calendario visual
        if (!esEdicion && evento != null && evento.getId() != null && !evento.getId().isEmpty()) {
            try {
                // Si el evento ya tiene ID (UUID temporal), eliminar del calendario visual
                if (eventoGuardadoCallback != null) {
                    // El callback se ejecuta para refrescar el calendario y eliminar entradas temporales
                    eventoGuardadoCallback.run();
                }
            } catch (Exception e) {
                System.err.println("Error al cancelar evento: " + e.getMessage());
            }
        }
        
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