package com.example.pruebamongodbcss.calendar;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloDiagnostico;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import com.example.pruebamongodbcss.Utils.SplashUtils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;



/**
 * Componente de calendario basado en CalendarFX.
 * Esta clase envuelve la funcionalidad de CalendarFX para su uso en la aplicación.
 */
public class CalendarFXComponent extends BorderPane {
    
    // Variables para el calendario
    private CalendarView calendarView;
    private List<Calendar> calendars = new ArrayList<>();
    private CalendarSource calendarSource;
    
    // Usuario actual
    private com.example.pruebamongodbcss.Data.Usuario usuarioActual;
    
    // Ruta al archivo CSS del calendario
    private static final String CALENDAR_DEFAULT_CSS = CalendarFXComponent.class.getResource("/com/example/pruebamongodbcss/theme/jfx-calendar-styles.css").toExternalForm();
    
    // CSS personalizado para forzar texto negro en todo el calendario
    private static final String BLACK_TEXT_CSS = 
        ".calendar-view * { -fx-text-fill: black !important; }" +
        ".date-time-label { -fx-text-fill: black !important; }" +
        ".date-time-header { -fx-text-fill: black !important; }" +
        ".weekday-label { -fx-text-fill: black !important; }" +
        ".date-label { -fx-text-fill: black !important; }" +
        ".month-year-label { -fx-text-fill: black !important; }" +
        ".time-label { -fx-text-fill: black !important; }" +
        ".detail-label { -fx-text-fill: black !important; }" +
        ".calendar-header { -fx-text-fill: black !important; }" +
        ".entry-title { -fx-text-fill: black !important; }" +
        ".agenda-view .time-label { -fx-text-fill: black !important; }" +
        ".day-of-week-label { -fx-text-fill: black !important; }" +
        ".day-of-month-label { -fx-text-fill: black !important; }";
    
    private Map<String, String> entryDescriptions = new java.util.HashMap<>();

    
    private GestorSocket gestorSocket;

    /**
     * Constructor que inicializa el componente del calendario con el usuario actual.
     * @param usuario El usuario actual logueado en el sistema
     */
    public CalendarFXComponent() {
        initialize();
    }
    
    /**
     * Establece el usuario actual para el componente de calendario.
     * @param usuario El usuario actual de la sesión
     */
    public void setUsuarioActual(com.example.pruebamongodbcss.Data.Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            System.out.println("Usuario establecido en CalendarFXComponent: " + usuario.getUsuario() + 
                               ", Rol: " + (usuario.getRol() != null ? usuario.getRol().name() : "null"));
        }

        loadAppointmentsFromDatabase();
        
    }
    
    /**
     * Inicializa el componente del calendario.
     */
    private void initialize() {
        try {
            // Inicializar el servicio de calendario
            gestorSocket = GestorSocket.getInstance();
            
            // Crear el componente principal de la vista
            calendarView = new CalendarView();
            
            // DESACTIVAR COMPLETAMENTE LOS ESTILOS DEL THEME MANAGER
            // Esto es una técnica radical pero efectiva
            calendarView.getStylesheets().clear();
            
            // Establecer un ID único para este componente y sus hijos
            // para evitar que los selectores CSS del ThemeManager lo afecten
            this.setId("calendar-fx-isolated-component");
            calendarView.setId("calendar-view-isolated");
            
            // Forzar el estilo predeterminado de CalendarFX SOLAMENTE
            if (!calendarView.getStylesheets().contains(CALENDAR_DEFAULT_CSS)) {
                calendarView.getStylesheets().add(CALENDAR_DEFAULT_CSS);
            }
            
            // Agregar el CSS para forzar texto negro
            calendarView.getStylesheets().add("data:text/css," + BLACK_TEXT_CSS.replace(" ", "%20"));
            
            // Aplicar tema básico
            applyTheme();
            
            // Desconectar este componente del ThemeManager
            desconectarDelThemeManager(this);
            
            // Crear los calendarios por tipo de cita con colores predeterminados
            Calendar citasPendientes = new Calendar("Citas pendientes");
            citasPendientes.setStyle(Calendar.Style.STYLE1); // Azul
            
            Calendar citasEnCurso = new Calendar("Citas en curso");
            citasEnCurso.setStyle(Calendar.Style.STYLE2); // Naranja
            
            Calendar citasPendientesDiagnostico = new Calendar("Pendientes de diagnóstico");
            citasPendientesDiagnostico.setStyle(Calendar.Style.STYLE5); // Morado
            
            Calendar citasCompletadas = new Calendar("Citas completadas");
            citasCompletadas.setStyle(Calendar.Style.STYLE3); // Verde
            
            Calendar citasCanceladas = new Calendar("Citas canceladas");
            citasCanceladas.setStyle(Calendar.Style.STYLE7); // Rojo
            
            Calendar eventosReuniones = new Calendar("Reuniones");
            eventosReuniones.setStyle(Calendar.Style.STYLE5); // Morado
            
            Calendar eventosRecordatorios = new Calendar("Recordatorios");
            eventosRecordatorios.setStyle(Calendar.Style.STYLE4); // Naranja claro
            
            // Configurar nombres cortos
            citasPendientes.setShortName("CP");
            citasEnCurso.setShortName("CEC");
            citasPendientesDiagnostico.setShortName("PDI");
            citasCompletadas.setShortName("CC");
            citasCanceladas.setShortName("CX");
            eventosReuniones.setShortName("REU");
            eventosRecordatorios.setShortName("REC");
            
            // Habilitar la edición de los calendarios
            citasPendientes.setReadOnly(false);
            citasEnCurso.setReadOnly(false);
            citasPendientesDiagnostico.setReadOnly(false);
            citasCompletadas.setReadOnly(false);
            citasCanceladas.setReadOnly(false);
            eventosReuniones.setReadOnly(false);
            eventosRecordatorios.setReadOnly(false);
            
            // Agregar a la lista de calendarios
            calendars.add(citasPendientes);
            calendars.add(citasEnCurso);
            calendars.add(citasPendientesDiagnostico);
            calendars.add(citasCompletadas);
            calendars.add(citasCanceladas);
            calendars.add(eventosReuniones);
            calendars.add(eventosRecordatorios);
            
            // Agregar los calendarios a una fuente
            calendarSource = new CalendarSource("Clínica Veterinaria");
            calendarSource.getCalendars().addAll(calendars);
            
            // Registrar la fuente del calendario
            calendarView.getCalendarSources().add(calendarSource);
            
            // Configurar fecha y hora actual
            calendarView.setToday(LocalDate.now());
            calendarView.setTime(LocalTime.now());
            
            // Permitir la creación y edición de entradas
            calendarView.setEntryEditPolicy(param -> true);
            calendarView.setEntryFactory(param -> createNewEntry(param));
            
            // Configurar el manejo de eventos del calendario
            configureCalendarHandlers();
            
            // Mostrar páginas relevantes
            calendarView.showDayPage();
            calendarView.showMonthPage();
            calendarView.showWeekPage(); // La última vista mostrada será la predeterminada
            
            // Configuraciones adicionales de estilo visual
            configureVisualSettings();
            
            // Cargar citas reales desde la base de datos
            loadAppointmentsFromDatabase();
            
            // Hilo para actualizar la hora
            Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
                @Override
                public void run() {
                    while (true) {
                        Platform.runLater(() -> {
                            calendarView.setToday(LocalDate.now());
                            calendarView.setTime(LocalTime.now());
                        });
                        
                        try {
                            // Actualizar cada 10 segundos
                            sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
            updateTimeThread.setDaemon(true);
            updateTimeThread.start();
            
            // Hilo para refrescar el calendario cada 2 minutos (sincronización automática)
            Thread refreshThread = new Thread("Calendar: Auto Refresh Thread") {
                @Override
                public void run() {
                    while (true) {
                        try {
                            // Esperar 2 minutos antes del siguiente refresh
                            sleep(120000); // 2 minutos
                            
                            // Refrescar el calendario en el hilo de JavaFX
                            Platform.runLater(() -> {
                                System.out.println("🔄 Refresh automático del calendario cada 2 minutos...");
                                refreshCalendarFromDatabase();
                                System.out.println("✅ Refresh automático completado");
                            });
                            
                        } catch (InterruptedException e) {
                            System.out.println("Hilo de refresh automático interrumpido");
                            break;
                        } catch (Exception e) {
                            System.err.println("Error en refresh automático: " + e.getMessage());
                            e.printStackTrace();
                            // Continuar ejecutándose a pesar del error
                        }
                    }
                }
            };
            
            refreshThread.setDaemon(true);
            refreshThread.start();
            
            // Crear barra de herramientas personalizada
            createCustomToolbar();
            
            // Añadir el calendario a este BorderPane
            setCenter(calendarView);
            
            // Buscar y modificar el botón de impresión una vez que todo esté configurado
            Platform.runLater(() -> {
                // Programar múltiples intentos para asegurar que capturamos el botón
                for (int i = 0; i < 5; i++) {
                    int delay = i * 1000;
                    new Thread(() -> {
                        try {
                            Thread.sleep(delay);
                            Platform.runLater(() -> {
                                buscarYCambiarColorBoton(calendarView);
                                aplicarTextoNegro(calendarView);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al inicializar el calendario", e.getMessage());
        }
    }
    
    /**
     * Crea una barra de herramientas personalizada para el calendario
     */
    private void createCustomToolbar() {
        // Barra de herramientas deshabilitada para maximizar el espacio visual
        setTop(null);
    }
    
    /**
     * Muestra un diálogo para crear una nueva cita
     */
    private void showNewAppointmentDialog() {
        try {
            // Cargar el formulario de citas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/cita-formulario.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador
            com.example.pruebamongodbcss.Modulos.Clinica.Citas.CitaFormularioController controller = loader.getController();
            
            // Configurar el controlador con el servicio de clínica
            com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica servicioClinica = new com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica();
            controller.setServicio(servicioClinica);
            
            // Obtener la fecha y hora seleccionada del calendario
            LocalDate fechaSeleccionada = calendarView.getToday();
            LocalTime horaSeleccionada = calendarView.getTime();
            LocalDateTime fechaHoraSeleccionada = fechaSeleccionada.atTime(horaSeleccionada);
            
            // Crear una nueva cita con la fecha y hora seleccionada
            ModeloCita nuevaCita = new ModeloCita();
            nuevaCita.setFechaHora(fechaHoraSeleccionada);
            controller.setCita(nuevaCita);
            
            // Configurar callback para refrescar el calendario
            controller.setCitaGuardadaCallback(() -> {
                System.out.println("🔄 Cita guardada, refrescando calendario inmediatamente...");
                Platform.runLater(() -> {
                    refreshCalendarFromDatabase();
                    System.out.println("✅ Calendario refrescado después de guardar cita");
                });
            });
            
            // Mostrar el formulario en una ventana modal
            Stage stage = new Stage();
            stage.setTitle("Nueva Cita");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refrescar el calendario después de cerrar
            refreshCalendarFromDatabase();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo abrir el formulario de citas: " + e.getMessage());
        }
    }
    
    /**
     * Carga las citas desde la base de datos aplicando los filtros adecuados según el rol del usuario
     */
    private void loadAppointmentsFromDatabase() {
        // Usar un hilo separado para no bloquear la UI
        new Thread(() -> {
            GestorSocket gestorSocketIndependiente = null;
            try {
                System.out.println("🔄 Cargando citas desde la base de datos...");
                
                // Crear una conexión independiente para evitar conflictos
                gestorSocketIndependiente = GestorSocket.crearConexionIndependiente();
                
                // Limpiar todas las entradas existentes en el hilo de JavaFX
                Platform.runLater(() -> {
                    for (Calendar calendar : calendars) {
                        calendar.clear();
                    }
                });
                
                List<CalendarEvent> events = new ArrayList<>();
                
                // Cargar todas las citas de forma simple
                System.out.println("📤 Solicitando todas las citas al servidor...");
                gestorSocketIndependiente.enviarPeticion(Protocolo.DAMETODASLASCITAS + Protocolo.SEPARADOR_CODIGO);
                
                ObjectInputStream ois = gestorSocketIndependiente.getEntrada();
                int codigo = ois.readInt();
                
                if (codigo == Protocolo.DAMETODASLASCITAS_RESPONSE) {
                    events = (List<CalendarEvent>) ois.readObject();
                    System.out.println("✅ Cargadas " + events.size() + " citas correctamente");
                } else if (codigo == Protocolo.ERROR_DAMETODASLASCITAS) {
                    System.err.println("❌ Error del servidor al cargar citas");
                    Platform.runLater(() -> showErrorMessage("Error", "No se pudieron cargar las citas del servidor"));
                    return;
                } else {
                    System.err.println("❌ Código de respuesta inesperado: " + codigo);
                    Platform.runLater(() -> showErrorMessage("Error", "Respuesta inesperada del servidor"));
                    return;
                }
                
                // Filtrar eventos según el rol del usuario
                List<CalendarEvent> eventosFinales = filtrarEventosPorRol(events);
                
                System.out.println("📊 Se mostrarán " + eventosFinales.size() + " eventos después del filtrado");
                
                // Convertir eventos a entradas del calendario en el hilo de JavaFX
                Platform.runLater(() -> {
                    try {
                        for (CalendarEvent event : eventosFinales) {
                            agregarEventoAlCalendario(event);
                        }
                        System.out.println("✅ Eventos cargados con éxito en el calendario visual");
                        refreshCalendar();
                    } catch (Exception e) {
                        System.err.println("❌ Error al agregar eventos al calendario: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                System.err.println("❌ Error al cargar citas: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> showErrorMessage("Error al cargar citas", "Error: " + e.getMessage()));
            } finally {
                // Cerrar la conexión independiente
                if (gestorSocketIndependiente != null) {
                    try {
                        gestorSocketIndependiente.cerrarConexion();
                        System.out.println("🔌 Conexión independiente cerrada correctamente");
                    } catch (Exception e) {
                        System.err.println("⚠️ Error al cerrar conexión: " + e.getMessage());
                    }
                }
            }
        }).start();
    }
    
    /**
     * Filtra los eventos según el rol del usuario actual
     */
    private List<CalendarEvent> filtrarEventosPorRol(List<CalendarEvent> todosLosEventos) {
        if (usuarioActual == null) {
            System.out.println("⚠️ No hay usuario actual, mostrando todos los eventos");
            return todosLosEventos;
        }
        
        String username = usuarioActual.getUsuario();
        com.example.pruebamongodbcss.Data.Usuario.Rol rol = usuarioActual.getRol();
        
        System.out.println("🔍 Filtrando eventos para usuario: " + username + ", Rol: " + (rol != null ? rol.name() : "null"));
        
        if (rol == com.example.pruebamongodbcss.Data.Usuario.Rol.ADMINISTRADOR) {
            // Administradores ven todo
            System.out.println("👑 Administrador: mostrando todos los eventos");
            return todosLosEventos;
        } else if (rol == com.example.pruebamongodbcss.Data.Usuario.Rol.AUXILIAR) {
            // Auxiliares ven todas las citas médicas
            List<CalendarEvent> citasMedicas = todosLosEventos.stream()
                .filter(this::esCitaMedica)
                .collect(Collectors.toList());
            System.out.println("🏥 Auxiliar: mostrando " + citasMedicas.size() + " citas médicas");
            return citasMedicas;
        } else if (rol == com.example.pruebamongodbcss.Data.Usuario.Rol.VETERINARIO) {
            // Veterinarios ven sus propias citas y eventos
            List<CalendarEvent> eventosVeterinario = todosLosEventos.stream()
                .filter(event -> {
                    boolean esMio = username.equals(event.getUsuario());
                    boolean esCita = esCitaMedica(event);
                    boolean esEvento = esReunion(event) || esRecordatorio(event);
                    return esMio && (esCita || esEvento);
                })
                .collect(Collectors.toList());
            System.out.println("👨‍⚕️ Veterinario: mostrando " + eventosVeterinario.size() + " eventos propios");
            return eventosVeterinario;
        } else {
            // Otros usuarios ven solo sus eventos
            List<CalendarEvent> eventosUsuario = todosLosEventos.stream()
                .filter(event -> username.equals(event.getUsuario()))
                .collect(Collectors.toList());
            System.out.println("👤 Usuario estándar: mostrando " + eventosUsuario.size() + " eventos propios");
            return eventosUsuario;
        }
    }
    
    /**
     * Agrega un evento individual al calendario visual
     */
    private void agregarEventoAlCalendario(CalendarEvent event) {
        try {
            Calendar targetCalendar = getTargetCalendar(event);
            String motivo = event.getTitle() != null ? event.getTitle().toUpperCase() : "CITA";
            String estadoStr = event.getEstado() != null ? event.getEstado().toUpperCase() : "PENDIENTE";
            String titulo = motivo + " - " + estadoStr;
            
            Entry<String> entry = new Entry<>(titulo);
            entry.setId(event.getId());
            entry.setLocation(event.getLocation());
            entry.setCalendar(targetCalendar);
            
            // Convertir fechas de String a LocalDateTime
            LocalDateTime startDateTime = parseDateTime(event.getStart());
            LocalDateTime endDateTime = parseDateTime(event.getEnd());
            
            if (startDateTime != null && endDateTime != null) {
                entry.changeStartDate(startDateTime.toLocalDate());
                entry.changeStartTime(startDateTime.toLocalTime());
                entry.changeEndDate(endDateTime.toLocalDate());
                entry.changeEndTime(endDateTime.toLocalTime());
                
                // Agregar notas si hay descripción
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    entryDescriptions.put(entry.getId(), event.getDescription());
                }
                
                // Agregar al calendario correspondiente
                targetCalendar.addEntry(entry);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al procesar evento individual: " + e.getMessage());
            // Continuar con el siguiente evento en lugar de fallar completamente
        }
    }
    
    /**
     * Determina el calendario destino para un evento según su estado o tipo
     */
    private Calendar getTargetCalendar(CalendarEvent event) {
        // Verificar eventType primero (reuniones y recordatorios)
        if (event.getEventType() != null) {
            String eventType = event.getEventType().toLowerCase();
            if (eventType.contains("meeting") || eventType.contains("reunion")) {
                return calendars.get(4); // Reuniones
            } else if (eventType.contains("reminder") || eventType.contains("recordatorio")) {
                return calendars.get(5); // Recordatorios
            }
        }
        
        // Si no es un evento, usar el estado
        if (event.getEstado() != null) {
            String estado = event.getEstado().toUpperCase();
            switch (estado) {
                case "PENDIENTE":
                    return calendars.get(0); // Citas pendientes
                case "EN_CURSO":
                    return calendars.get(1); // Citas en curso
                case "COMPLETADA":
                case "PENDIENTE_DE_FACTURAR":
                    return calendars.get(2); // Citas completadas (incluye pendientes de facturar)
                case "CANCELADA":
                case "ABSENTISMO":
                    return calendars.get(3); // Citas canceladas (incluye absentismo)
                default:
                    return calendars.get(0); // Por defecto, pendientes
            }
        } else if (event.getType() != null) {
            // Usar el tipo como respaldo
            String type = event.getType().toLowerCase();
            if (type.contains("urgent")) {
                return calendars.get(1); // Citas en curso
            } else if (type.contains("completed")) {
                return calendars.get(2); // Citas completadas
            } else if (type.contains("cancelled")) {
                return calendars.get(3); // Citas canceladas
            }
        }
        
        // Por defecto, pendientes
        return calendars.get(0);
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
    
    /**
     * Refresca los datos del calendario desde la base de datos
     */
    public void refreshCalendarFromDatabase() {
        System.out.println("🔄 Iniciando refresco del calendario...");
        
        // Simplemente llamar al método de carga principal
        loadAppointmentsFromDatabase();
    }
    
    /**
     * Aplica los estilos CSS por defecto del calendario
     */
    private void applyCalendarStyles() {
        // Asegurarse de que estamos usando los estilos por defecto de CalendarFX
        if (!calendarView.getStylesheets().contains(CALENDAR_DEFAULT_CSS)) {
            calendarView.getStylesheets().add(CALENDAR_DEFAULT_CSS);
        }
        
        // Aplicar estilos básicos
        this.setStyle("-fx-background-color: white;");
    }
    
    /**
     * Aplica un tema básico independiente del ThemeManager
     */
    private void applyTheme() {
        // Aplicar un tema claro fijo, sin usar el ThemeManager
        setStyle("-fx-background-color: white;");
        calendarView.setStyle("-fx-background-color: white;");
        
        // Aplicar estilo de texto negro directamente a través de CSS inline
        String blackTextStyle = "-fx-text-fill: black !important;";
        calendarView.setStyle(calendarView.getStyle() + "; " + blackTextStyle);
        
        // Programar múltiples intentos para buscar y modificar los componentes
        programarMultiplesIntentos();
    }
    
    /**
     * Programa múltiples intentos para modificar los componentes críticos
     */
    private void programarMultiplesIntentos() {
        // Ejecutar varias veces con diferentes retrasos para asegurar que capturamos todos los componentes
        for (int i = 0; i < 10; i++) {
            final int delay = 500 * (i + 1);
            new Thread(() -> {
                try {
                    Thread.sleep(delay);
                    Platform.runLater(() -> {
                        buscarYModificarComponentes(calendarView);
                        aplicarTextoNegro(calendarView);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Busca y modifica botones y otros componentes 
     */
    private void buscarYModificarComponentes(Node nodo) {
        try {
            // Modificar según el tipo de componente
            if (nodo instanceof Button) {
                Button btn = (Button) nodo;
                
                // Aplicar texto negro a todos los botones
                btn.setTextFill(Color.BLACK);
                
                // Si es botón de impresión, modificar directamente
                if (btn.getText() != null && 
                    (btn.getText().equals("Print") || btn.getText().equals("Imprimir"))) {
                    
                    // Crear un fondo rojo sólido
                    javafx.scene.layout.Background redBackground = new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(
                            javafx.scene.paint.Color.web("#EA4335"), 
                            new javafx.scene.layout.CornerRadii(3), 
                            javafx.geometry.Insets.EMPTY
                        )
                    );
                    
                    // Aplicar el fondo y otros estilos directamente
                    btn.setBackground(redBackground);
                    btn.setTextFill(javafx.scene.paint.Color.WHITE);
                    btn.setBorder(null);
                    btn.setEffect(null);
                    
                    System.out.println("🖨️ Botón de impresión modificado: " + btn.getText());
                }
            } 
            // Si es la barra de búsqueda
            else if (nodo instanceof TextField) {
                TextField searchField = (TextField) nodo;
                searchField.setStyle(searchField.getStyle() + "; -fx-text-fill: black;");
                
                if (searchField.getPromptText() != null && 
                    (searchField.getPromptText().contains("Search") || 
                     searchField.getPromptText().contains("Buscar"))) {
                    
                    searchField.setBackground(new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(
                            javafx.scene.paint.Color.WHITE, 
                            new javafx.scene.layout.CornerRadii(3), 
                            javafx.geometry.Insets.EMPTY
                        )
                    ));
                    searchField.setBorder(new javafx.scene.layout.Border(
                        new javafx.scene.layout.BorderStroke(
                            javafx.scene.paint.Color.LIGHTGRAY,
                            javafx.scene.layout.BorderStrokeStyle.SOLID,
                            new javafx.scene.layout.CornerRadii(3),
                            new javafx.scene.layout.BorderWidths(1)
                        )
                    ));
                    
                    System.out.println("🔍 Barra de búsqueda modificada");
                }
            }
            // Si es cualquier etiqueta, aplicar texto negro
            else if (nodo instanceof Label) {
                ((Label) nodo).setTextFill(Color.BLACK);
            }
            // Si es un texto, aplicar color negro
            else if (nodo instanceof Text) {
                ((Text) nodo).setFill(Color.BLACK);
            }
            
            // Buscar recursivamente en todos los hijos
            if (nodo instanceof Parent) {
                Parent parent = (Parent) nodo;
                for (Node hijo : parent.getChildrenUnmodifiable()) {
                    buscarYModificarComponentes(hijo);
                }
            }
        } catch (Exception e) {
 
        }
    }
    
    /**
     * Configura aspectos visuales adicionales del calendario
     */
    private void configureVisualSettings() {
        // IDs únicos para cada página
        calendarView.getDayPage().setId("calendar-day-page");
        calendarView.getWeekPage().setId("calendar-week-page");
        calendarView.getMonthPage().setId("calendar-month-page");
        calendarView.getYearPage().setId("calendar-year-page");
        
        // Deshabilitar botones predeterminados que reemplazamos con los nuestros
        calendarView.setShowAddCalendarButton(false);
        
        // Asegurarnos de que el botón de impresión está habilitado y visible
        calendarView.setShowPrintButton(true);
        //calendarView.getCalendarSources().get(0).getCalendars().get(0).getStyle();
        
        // Mantener controles útiles
        calendarView.setShowPageToolBarControls(true);
        calendarView.setShowSourceTrayButton(true);
        
        // Configurar colores personalizados para algunas vistas
        calendarView.getWeekPage().setStyle("-fx-background-color: #f8f9fa;");
        calendarView.getDayPage().setStyle("-fx-background-color: #f8f9fa;");
        
        // Aplicar estilo de texto negro a las páginas principales
        String blackTextStyle = "-fx-text-fill: black !important;";
        calendarView.getDayPage().setStyle(calendarView.getDayPage().getStyle() + "; " + blackTextStyle);
        calendarView.getWeekPage().setStyle(calendarView.getWeekPage().getStyle() + "; " + blackTextStyle);
        calendarView.getMonthPage().setStyle(calendarView.getMonthPage().getStyle() + "; " + blackTextStyle);
        calendarView.getYearPage().setStyle(calendarView.getYearPage().getStyle() + "; " + blackTextStyle);
        
        // Programar búsqueda del botón de impresión y forzar su color
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {  // 10 intentos
                try {
                    Thread.sleep(500 * (i + 1));  // Incrementar el tiempo entre intentos
                    Platform.runLater(() -> {
                        buscarYCambiarColorBoton(calendarView);
                        aplicarTextoNegro(calendarView);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    /**
     * Método simple para cambiar el color del botón de impresión
     */
    private void buscarYCambiarColorBoton(Node node) {
        if (node instanceof Button) {
            Button boton = (Button) node;
            // Verificar si es el botón de impresión
            if (boton.getText() != null && 
                (boton.getText().equals("Print") || boton.getText().equals("Imprimir"))) {
                
                // Cambiar el color directamente (sin CSS)
                try {
                    boton.setBackground(new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(
                            javafx.scene.paint.Color.web("#EA4335"), 
                            new javafx.scene.layout.CornerRadii(3), 
                            javafx.geometry.Insets.EMPTY
                        )
                    ));
                    
                    // Cambiar color del texto
                    boton.setTextFill(javafx.scene.paint.Color.WHITE);
                    
                    System.out.println("Botón de impresión modificado");
                } catch (Exception e) {
                    // Si falla, probar con setStyle
                    boton.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white;");
                }
            }
        }
        
        // Revisar hijos recursivamente
        if (node instanceof Parent) {
            for (Node hijo : ((Parent) node).getChildrenUnmodifiable()) {
                buscarYCambiarColorBoton(hijo);
            }
        }
    }
    
    /**
     * Muestra el diálogo de detalles según el tipo de entrada
     */
    private void showEntryDetailsDialog(Entry<?> entry) {
        try {
            // Convertir Entry a CalendarEvent
            CalendarEvent event = entryToCalendarEvent(entry);
            
            // Si el evento es una cita médica y está en curso, abrir la vista de diagnóstico
            if (event.getTipoEvento() == CalendarEvent.EventoTipo.CITA_MEDICA && 
                "EN_CURSO".equals(event.getEstado())) {
                abrirVistaDiagnostico(event);
                return;
            }
            // Si el evento es una cita médica y está pendiente de facturar, abrir facturación
            if (event.getTipoEvento() == CalendarEvent.EventoTipo.CITA_MEDICA && 
                "PENDIENTE_DE_FACTURAR".equalsIgnoreCase(event.getEstado())) {
                abrirFormularioFacturacionDesdeCita(event);
                return;
            }
            // Para otros tipos de eventos o citas no en curso, mostrar el formulario normal
            if (event.getTipoEvento() == CalendarEvent.EventoTipo.CITA_MEDICA) {
                showCitaFormulario(event, entry);
            } else {
                showEventoFormulario(event, entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error al mostrar detalles", "No se pudieron mostrar los detalles: " + e.getMessage());
        }
    }
    
    /**
     * Abre la vista de diagnóstico para una cita en curso
     */
    private void abrirVistaDiagnostico(CalendarEvent event) {
        // 1. Mostrar splash
        Stage splashStage = SplashUtils.mostrarSplashDiagnostico();

        // 2. Cargar el formulario en un hilo aparte
        new Thread(() -> {
            try {
                // Primero buscar si ya existe un diagnóstico para esta cita
                com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica servicioClinica = 
                    new com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica();
                ModeloDiagnostico diagnosticoExistente = servicioClinica.obtenerDiagnosticoPorCita(
                    new org.bson.types.ObjectId(event.getId())
                );

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Diagnostico/diagnostico-view.fxml"));
                Parent root = loader.load();

                // Configura el controlador, paciente, etc.
                com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico.DiagnosticoController controller = loader.getController();
                com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente paciente = 
                    servicioClinica.obtenerPacientePorId(new org.bson.types.ObjectId(event.getPacienteId()));
                ModeloCita cita = servicioClinica.obtenerCitaPorId(new org.bson.types.ObjectId(event.getId()));
                
                // Si existe un diagnóstico, cargarlo
                if (diagnosticoExistente != null) {
                    controller.setDiagnostico(diagnosticoExistente);
                }
                
                controller.setPaciente(paciente, cita);
                
                // Configurar callback para refrescar inmediatamente el calendario
                controller.setOnGuardarCallback(() -> {
                    System.out.println("🔄 Refrescando calendario después de guardar diagnóstico...");
                    Platform.runLater(() -> {
                        refreshCalendarFromDatabase();
                        System.out.println("✅ Calendario refrescado");
                    });
                });

                // 3. Cuando termine, mostrar el formulario y cerrar el splash
                Platform.runLater(() -> {
                    Stage stage = new Stage();
                    stage.setTitle("Diagnóstico Médico");
                    Scene scene = new Scene(root, 950, 760); // 5% menos que 1000x800
                    stage.setScene(scene);
                    stage.initModality(Modality.NONE);
                    stage.show();

                    splashStage.close();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    splashStage.close();
                    showErrorMessage("Error", "No se pudo abrir la vista de diagnóstico: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Convierte un Entry a CalendarEvent
     */
    private CalendarEvent entryToCalendarEvent(Entry<?> entry) {
        CalendarEvent event = new CalendarEvent();
        
        // Establecer ID
        event.setId(entry.getId());
        
        // Establecer título
        event.setTitle(entry.getTitle());
        
        // Establecer fechas
        event.setStart(entry.getStartDate().toString());
        event.setEnd(entry.getEndDate().toString());
        
        // Establecer ubicación si existe
        if (entry.getLocation() != null) {
            event.setLocation(entry.getLocation());
        }
        
        // Establecer si es todo el día
        event.setAllDay(entry.isFullDay());
        
        // Establecer tipo de evento según el calendario
        if (entry.getCalendar() == calendars.get(0)) {
            event.setTipoEvento(CalendarEvent.EventoTipo.CITA_MEDICA);
            event.setEstado("PENDIENTE");
            event.setType("default");
        } else if (entry.getCalendar() == calendars.get(1)) {
            event.setTipoEvento(CalendarEvent.EventoTipo.CITA_MEDICA);
            event.setEstado("EN_CURSO");
            event.setType("urgent");
        } else if (entry.getCalendar() == calendars.get(2)) {
            event.setTipoEvento(CalendarEvent.EventoTipo.CITA_MEDICA);
            event.setEstado("COMPLETADA");
            event.setType("completed");
        } else if (entry.getCalendar() == calendars.get(3)) {
            event.setTipoEvento(CalendarEvent.EventoTipo.CITA_MEDICA);
            event.setEstado("CANCELADA");
            event.setType("cancelled");
        } else if (entry.getCalendar() == calendars.get(4)) {
            event.setTipoEvento(CalendarEvent.EventoTipo.REUNION);
        } else if (entry.getCalendar() == calendars.get(5)) {
            event.setTipoEvento(CalendarEvent.EventoTipo.RECORDATORIO);
        }
        
        // Si es una cita médica, buscar el ID del paciente
        if (event.getTipoEvento() == CalendarEvent.EventoTipo.CITA_MEDICA && event.getId() != null && !event.getId().isEmpty()) {
            try {
                // Convertir el ID a ObjectId
                String idStr = event.getId();
                if (idStr.startsWith("_")) {
                    idStr = idStr.substring(1);
                }
                org.bson.types.ObjectId citaId = new org.bson.types.ObjectId(idStr);
                
                // Buscar la cita en la base de datos
                com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica servicioClinica = new com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica();
                com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita cita = servicioClinica.obtenerCitaPorId(citaId);
                
                if (cita != null && cita.getPacienteId() != null) {
                    event.setPacienteId(cita.getPacienteId().toString());
                    event.setEstado(cita.getEstado().name()); // <-- Esto es lo importante
                }
            } catch (Exception e) {
                System.err.println("Error al obtener el ID del paciente: " + e.getMessage());
            }
        }
        
        return event;
    }
    
    /**
     * Muestra el formulario para citas médicas
     */
    private void showCitaFormulario(CalendarEvent event, Entry<?> entry) {
        try {
            // Si la entrada ya está en un calendario, eliminarla temporalmente
            Calendar originalCalendar = entry.getCalendar();
            if (originalCalendar != null) {
                originalCalendar.removeEntry(entry);
            }
            
            // Cargar el formulario de citas desde la ubicación correcta
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/cita-formulario.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador
            com.example.pruebamongodbcss.Modulos.Clinica.Citas.CitaFormularioController controller = loader.getController();
            
            // Configurar el controlador con el servicio de clínica
            com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica servicioClinica = new com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica();
            controller.setServicio(servicioClinica);
            
            // Si es una cita existente, cargar sus datos
            if (event.getId() != null && !event.getId().isEmpty()) {
                try {
                    // Convertir el ID a ObjectId
                    String idStr = event.getId();
                    if (idStr.startsWith("_")) {
                        idStr = idStr.substring(1);
                    }
                    
                    // Verificar que el ID sea válido para MongoDB ObjectId
                    if (idStr.length() == 24 && idStr.matches("[0-9a-fA-F]+")) {
                        org.bson.types.ObjectId citaId = new org.bson.types.ObjectId(idStr);
                        
                        // Buscar la cita en la base de datos usando el protocolo
                        GestorSocket gestorSocketCita = null;
                        try {
                            gestorSocketCita = GestorSocket.crearConexionIndependiente();
                            gestorSocketCita.enviarPeticion(Protocolo.OBTENER_CITA_POR_ID + Protocolo.SEPARADOR_CODIGO + citaId.toString());
                            ObjectInputStream ois = gestorSocketCita.getEntrada();
                            int codigo = ois.readInt();
                            
                            if (codigo == Protocolo.OBTENER_CITA_POR_ID_RESPONSE) {
                                com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita cita = (com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita) ois.readObject();
                                if (cita != null) {
                                    System.out.println("Cita encontrada para edición: " + cita.getId());
                                    // Sobrescribir la fecha/hora con la del Entry visual
                                    cita.setFechaHora(LocalDateTime.of(entry.getStartDate(), entry.getStartTime()));
                                    controller.setCita(cita);
                                } else {
                                    System.out.println("No se encontró la cita con ID: " + citaId);
                                }
                            } else if (codigo == Protocolo.ERROR_OBTENER_CITA_POR_ID) {
                                String errorMsg = ois.readUTF();
                                System.err.println("Error del servidor al obtener cita: " + errorMsg);
                            } else {
                                System.err.println("Código de respuesta inesperado al obtener cita: " + codigo);
                            }
                        } catch (Exception dbException) {
                            System.err.println("Error al comunicarse con la base de datos para obtener la cita: " + dbException.getMessage());
                            dbException.printStackTrace();
                        } finally {
                            if (gestorSocketCita != null) {
                                gestorSocketCita.cerrarConexion();
                                System.out.println("🔒 Conexión de obtener cita cerrada");
                            }
                        }
                    } else {
                        System.out.println("ID de cita no válido para MongoDB: " + idStr + " (longitud: " + idStr.length() + ")");
                    }
                } catch (Exception idException) {
                    System.err.println("Error al procesar el ID de la cita: " + idException.getMessage());
                    idException.printStackTrace();
                }
            } else {
                // Es una nueva cita - crear una cita con la fecha/hora del Entry
                com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita nuevaCita = new com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita();
                
                // Obtener fecha y hora del Entry
                LocalDateTime fechaHoraEntry = LocalDateTime.of(entry.getStartDate(), entry.getStartTime());
                LocalDateTime fechaFinEntry = LocalDateTime.of(entry.getEndDate(), entry.getEndTime());
                
                // Calcular duración en minutos
                long duracionMinutos = java.time.Duration.between(fechaHoraEntry, fechaFinEntry).toMinutes();
                
                // Configurar la nueva cita
                nuevaCita.setFechaHora(fechaHoraEntry);
                nuevaCita.setEstado(com.example.pruebamongodbcss.Data.EstadoCita.PENDIENTE);
                nuevaCita.setDuracionMinutos((int) duracionMinutos);
                nuevaCita.setMotivo("Nueva cita médica");
                
                System.out.println("✅ Nueva cita creada con fecha/hora: " + fechaHoraEntry + " (duración: " + duracionMinutos + " min)");
                
                controller.setCita(nuevaCita);
            }
            
            // Configurar callback para refrescar el calendario
            controller.setCitaGuardadaCallback(() -> {
                System.out.println("🔄 Cita guardada, refrescando calendario inmediatamente...");
                Platform.runLater(() -> {
                    refreshCalendarFromDatabase();
                    System.out.println("✅ Calendario refrescado después de guardar cita");
                });
            });
            
            // Mostrar el formulario en una ventana modal
            Stage stage = new Stage();
            stage.setTitle(event.getId() != null && !event.getId().isEmpty() ? "Editar Cita" : "Nueva Cita");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refrescar el calendario después de cerrar
            refreshCalendarFromDatabase();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo abrir el formulario de citas: " + e.getMessage());
        }
    }
    
    /**
     * Muestra el formulario para reuniones y recordatorios
     */
    private void showEventoFormulario(CalendarEvent event, Entry<?> entry) {
        try {
            // Si la entrada ya está en un calendario, eliminarla temporalmente
            Calendar originalCalendar = entry.getCalendar();
            if (originalCalendar != null) {
                originalCalendar.removeEntry(entry);
            }
            
            // Cargar el formulario de eventos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/calendar/evento-formulario.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador
            EventoFormularioController controller = loader.getController();
            
            // Configurar el controlador
            controller.setServicio(null);
            
            // Configurar callback para refrescar el calendario
            controller.setEventoGuardadoCallback(() -> {
                System.out.println("🔄 Evento guardado, refrescando calendario inmediatamente...");
                Platform.runLater(() -> {
                    refreshCalendarFromDatabase();
                    System.out.println("✅ Calendario refrescado después de guardar evento");
                });
            });
            
            // Si es edición, pasar el evento
            if (event.getId() != null && !event.getId().isEmpty()) {
                controller.setEvento(event);
            }
            
            // Mostrar el formulario en una ventana modal
            Stage stage = new Stage();
            stage.setTitle(event.getTipoEvento() == CalendarEvent.EventoTipo.REUNION ? "Reunión" : "Recordatorio");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refrescar el calendario después de cerrar, independientemente del resultado
            refreshCalendarFromDatabase();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo abrir el formulario de eventos: " + e.getMessage());
            // Refrescar de todos modos para asegurar consistencia
            refreshCalendarFromDatabase();
        }
    }
    
    /**
     * Muestra el diálogo para cambiar el estado de una cita
     */
    private void showChangeStatusDialog(Entry<?> entry) {
        try {
            // Crear el diálogo
            Alert dialog = new Alert(AlertType.CONFIRMATION);
            dialog.setTitle("Cambiar Estado de Cita");
            dialog.setHeaderText("Seleccione el nuevo estado para la cita:");
            dialog.setContentText(entry.getTitle());
            
            // Crear el contenido personalizado con radio buttons
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Crear grupo de radio buttons
            javafx.scene.control.ToggleGroup toggleGroup = new javafx.scene.control.ToggleGroup();
            
            javafx.scene.control.RadioButton pendienteRadio = new javafx.scene.control.RadioButton("Pendiente");
            javafx.scene.control.RadioButton enCursoRadio = new javafx.scene.control.RadioButton("En Curso");
            javafx.scene.control.RadioButton completadaRadio = new javafx.scene.control.RadioButton("Completada");
            javafx.scene.control.RadioButton pendienteFacturarRadio = new javafx.scene.control.RadioButton("Pendiente de Facturar");
            javafx.scene.control.RadioButton canceladaRadio = new javafx.scene.control.RadioButton("Cancelada");
            javafx.scene.control.RadioButton absentismoRadio = new javafx.scene.control.RadioButton("Absentismo");
            
            pendienteRadio.setToggleGroup(toggleGroup);
            enCursoRadio.setToggleGroup(toggleGroup);
            completadaRadio.setToggleGroup(toggleGroup);
            pendienteFacturarRadio.setToggleGroup(toggleGroup);
            canceladaRadio.setToggleGroup(toggleGroup);
            absentismoRadio.setToggleGroup(toggleGroup);
            
            // Establecer el estado actual como seleccionado
            String currentStatus = getCurrentStatusFromCalendar(entry.getCalendar());
            switch (currentStatus) {
                case "PENDIENTE":
                    pendienteRadio.setSelected(true);
                    break;
                case "EN_CURSO":
                    enCursoRadio.setSelected(true);
                    break;
                case "COMPLETADA":
                    completadaRadio.setSelected(true);
                    break;
                case "PENDIENTE_DE_FACTURAR":
                    pendienteFacturarRadio.setSelected(true);
                    break;
                case "CANCELADA":
                    canceladaRadio.setSelected(true);
                    break;
                case "ABSENTISMO":
                    absentismoRadio.setSelected(true);
                    break;
                default:
                    pendienteRadio.setSelected(true);
                    break;
            }
            
            // Añadir radio buttons al grid
            grid.add(new Label("Estados disponibles:"), 0, 0);
            grid.add(pendienteRadio, 0, 1);
            grid.add(enCursoRadio, 0, 2);
            grid.add(completadaRadio, 0, 3);
            grid.add(pendienteFacturarRadio, 0, 4);
            grid.add(canceladaRadio, 0, 5);
            grid.add(absentismoRadio, 0, 6);
            
            // Establecer el contenido expandible del diálogo
            dialog.getDialogPane().setExpandableContent(grid);
            dialog.getDialogPane().setExpanded(true);
            
            // Configurar botones
            dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Mostrar el diálogo y procesar la respuesta
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Obtener el estado seleccionado
                javafx.scene.control.RadioButton selectedRadio = (javafx.scene.control.RadioButton) toggleGroup.getSelectedToggle();
                if (selectedRadio != null) {
                    String newStatus = getStatusFromRadioText(selectedRadio.getText());
                    
                    // Cambiar el estado en la base de datos
                    changeAppointmentStatus(entry, newStatus);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo mostrar el diálogo de cambio de estado: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el estado actual basado en el calendario de la entrada
     */
    private String getCurrentStatusFromCalendar(Calendar calendar) {
        if (calendar == calendars.get(0)) return "PENDIENTE";
        if (calendar == calendars.get(1)) return "EN_CURSO";
        if (calendar == calendars.get(2)) {
            // Para el calendario de completadas, necesitamos verificar el título para distinguir
            // entre COMPLETADA y PENDIENTE_DE_FACTURAR, pero por defecto devolvemos COMPLETADA
            return "COMPLETADA";
        }
        if (calendar == calendars.get(3)) {
            // Para el calendario de canceladas, necesitamos verificar el título para distinguir
            // entre CANCELADA y ABSENTISMO, pero por defecto devolvemos CANCELADA
            return "CANCELADA";
        }
        return "PENDIENTE";
    }
    
    /**
     * Convierte el texto del radio button al estado correspondiente
     */
    private String getStatusFromRadioText(String radioText) {
        switch (radioText) {
            case "Pendiente":
                return "PENDIENTE";
            case "En Curso":
                return "EN_CURSO";
            case "Completada":
                return "COMPLETADA";
            case "Pendiente de Facturar":
                return "PENDIENTE_DE_FACTURAR";
            case "Cancelada":
                return "CANCELADA";
            case "Absentismo":
                return "ABSENTISMO";
            default:
                return "PENDIENTE";
        }
    }
    
    /**
     * Cambia el estado de una cita en la base de datos
     */
    private void changeAppointmentStatus(Entry<?> entry, String newStatus) {
        // Usar una conexión independiente para evitar conflictos
        GestorSocket gestorSocketCambio = null;
        try {
            String entryId = entry.getId();
            if (entryId == null || entryId.isEmpty()) {
                showErrorMessage("Error", "No se puede cambiar el estado: ID de cita no válido.");
                return;
            }
            
            // Remover prefijo si existe
            if (entryId.startsWith("_")) {
                entryId = entryId.substring(1);
            }
            
            System.out.println("🔄 Cambiando estado de cita " + entryId + " a " + newStatus);
            
            // Crear conexión independiente
            gestorSocketCambio = GestorSocket.crearConexionIndependiente();
            
            // Enviar petición al servidor para cambiar el estado
            gestorSocketCambio.enviarPeticion(Protocolo.CAMBIAR_ESTADO_CITA + Protocolo.SEPARADOR_CODIGO + entryId + Protocolo.SEPARADOR_PARAMETROS + newStatus);
            ObjectInputStream ois = gestorSocketCambio.getEntrada();
            int codigo = ois.readInt();
            
            if (codigo == Protocolo.CAMBIAR_ESTADO_CITA_RESPONSE) {
                boolean success = ois.readBoolean();
                if (success) {
                    // Éxito: refrescar el calendario inmediatamente
                    System.out.println("✅ Estado cambiado exitosamente, refrescando calendario...");
                    refreshCalendarFromDatabase();
                    
                    // Mostrar mensaje de éxito
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("Éxito");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("El estado de la cita ha sido cambiado correctamente a: " + getStatusDisplayName(newStatus));
                    successAlert.showAndWait();
                } else {
                    showErrorMessage("Error", "No se pudo cambiar el estado de la cita. Verifique que la cita existe.");
                }
            } else if (codigo == Protocolo.ERROR_CAMBIAR_ESTADO_CITA) {
                String errorMsg = ois.readUTF();
                showErrorMessage("Error del Servidor", "Error al cambiar estado: " + errorMsg);
            } else {
                showErrorMessage("Error", "Respuesta inesperada del servidor (código: " + codigo + ")");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error de Comunicación", "No se pudo comunicar con el servidor: " + e.getMessage());
        } finally {
            // Cerrar la conexión independiente
            if (gestorSocketCambio != null) {
                gestorSocketCambio.cerrarConexion();
                System.out.println("🔒 Conexión de cambio de estado cerrada");
            }
        }
    }
    
    /**
     * Obtiene el nombre de visualización para un estado
     */
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "PENDIENTE":
                return "Pendiente";
            case "EN_CURSO":
                return "En Curso";
            case "COMPLETADA":
                return "Completada";
            case "PENDIENTE_DE_FACTURAR":
                return "Pendiente de Facturar";
            case "CANCELADA":
                return "Cancelada";
            case "ABSENTISMO":
                return "Absentismo";
            default:
                return status;
        }
    }
    
    /**
     * Crea una nueva entrada para el calendario
     */
    private Entry<?> createNewEntry(DateControl.CreateEntryParameter param) {
        String motivo = "NUEVA CITA";
        String estadoStr = "PENDIENTE";
        String titulo = motivo + " - " + estadoStr;
        Entry<String> entry = new Entry<>(titulo);
        
        // Usar la fecha y hora del parámetro
        ZonedDateTime startTime = param.getZonedDateTime();
        ZonedDateTime endTime = startTime.plusMinutes(30); // Duración estándar de 30 minutos
        
        entry.setInterval(startTime, endTime);
        entry.setCalendar(calendars.get(0));
        entry.setId("");
        return entry;
    }
    
    /**
     * Configura los manejadores de eventos para el calendario
     */
    private void configureCalendarHandlers() {
        // Manejar evento de creación de nueva entrada
        calendarView.setEntryDetailsCallback(entryDetailsParameter -> {
            Entry<?> entry = entryDetailsParameter.getEntry();
            
            // Asegurarse de que la entrada ya esté agregada a un calendario
            if (entry.getCalendar() == null) {
                // Agregar a calendario por defecto (primer calendario)
                entry.setCalendar(calendars.get(0));
            }
            
            showEntryDetailsDialog(entry);
            return null;
        });
        
        // Configurar tooltips personalizados para los elementos del calendario
        setupCustomTooltips();
        
        // Menú contextual para clic derecho sobre una entrada (cita)
        calendarView.setEntryContextMenuCallback(param -> {
            Entry<?> entry = param.getEntry();
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editItem = new MenuItem("Editar cita");
            MenuItem deleteItem = new MenuItem("Eliminar cita");
            MenuItem changeStatusItem = new MenuItem("Cambiar estado");
            
            // Verificar si la cita ya ha pasado
            LocalDateTime fechaHoraCita = LocalDateTime.of(entry.getStartDate(), entry.getStartTime());
            LocalDateTime ahora = LocalDateTime.now();
            boolean citaPasada = fechaHoraCita.isBefore(ahora);
            
            // Configurar acción de edición
            editItem.setOnAction(e -> {
                showEntryDetailsDialog(entry);
            });
            
            // Configurar acción de eliminación (siempre disponible pero con nombre diferente para citas pasadas)
            if (citaPasada) {
                deleteItem.setText("Cancelar cita");
            }
            deleteItem.setOnAction(e -> {
                // Confirmar eliminación/cancelación
                Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
                if (citaPasada) {
                    confirmDialog.setTitle("Cancelar cita");
                    confirmDialog.setHeaderText("¿Está seguro que desea cancelar esta cita?");
                    confirmDialog.setContentText("Esta cita ya ha pasado su fecha programada.");
                } else {
                    confirmDialog.setTitle("Eliminar cita");
                    confirmDialog.setHeaderText("¿Está seguro que desea eliminar esta cita?");
                    confirmDialog.setContentText("Esta acción no se puede deshacer.");
                }
                
                Optional<ButtonType> result = confirmDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Obtener ID de la cita
                    String entryId = entry.getId();
                    
                    // Eliminar usando conexión independiente y protocolo correcto
                    boolean deleted = false;
                    GestorSocket gestorSocketEliminar = null;
                    
                    try {
                        System.out.println("🗑️ Eliminando cita con ID: " + entryId);
                        
                        // Crear conexión independiente para evitar conflictos
                        gestorSocketEliminar = GestorSocket.crearConexionIndependiente();
                        
                        // Usar el protocolo correcto para eliminar citas médicas
                        gestorSocketEliminar.enviarPeticion(Protocolo.ELIMINAR_CITA + Protocolo.SEPARADOR_CODIGO + entryId);
                        
                        // Leer respuesta
                        ObjectInputStream ois = gestorSocketEliminar.getEntrada();
                        int codigo = ois.readInt();
                        
                        if (codigo == Protocolo.ELIMINAR_CITA_RESPONSE) {
                            deleted = ois.readBoolean();
                            System.out.println(deleted ? "✅ Cita eliminada exitosamente" : "❌ No se pudo eliminar la cita");
                        } else if (codigo == Protocolo.ERROR_ELIMINAR_CITA) {
                            System.err.println("❌ Error del servidor al eliminar cita");
                            deleted = false;
                        } else {
                            System.err.println("❌ Código de respuesta inesperado: " + codigo);
                            deleted = false;
                        }
                        
                    } catch (Exception ex) {
                        System.err.println("❌ Error al eliminar cita: " + ex.getMessage());
                        ex.printStackTrace();
                        deleted = false;
                    } finally {
                        // Cerrar la conexión independiente
                        if (gestorSocketEliminar != null) {
                            try {
                                gestorSocketEliminar.cerrarConexion();
                                System.out.println("🔌 Conexión de eliminación cerrada correctamente");
                            } catch (Exception ex) {
                                System.err.println("Error al cerrar conexión de eliminación: " + ex.getMessage());
                            }
                        }
                    }
                    
                    if (deleted) {
                        // Eliminar del calendario visual
                        Calendar calendar = entry.getCalendar();
                        if (calendar != null) {
                            calendar.removeEntry(entry);
                        }
                        
                        // Refrescar la vista inmediatamente
                        System.out.println("🔄 Cita eliminada exitosamente, refrescando calendario...");
                        refreshCalendarFromDatabase();
                        
                        // Mostrar mensaje de éxito
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Éxito");
                        alert.setHeaderText(null);
                        alert.setContentText(citaPasada ? "La cita ha sido cancelada correctamente." : "La cita ha sido eliminada correctamente.");
                        alert.showAndWait();
                    } else {
                        // Mostrar error
                        showErrorMessage("Error", citaPasada ? "No se pudo cancelar la cita. Por favor, inténtelo de nuevo." : "No se pudo eliminar la cita. Por favor, inténtelo de nuevo.");
                        // Refrescar de todos modos para asegurar consistencia
                        refreshCalendarFromDatabase();
                    }
                }
            });
            
            // Configurar acción de cambio de estado
            changeStatusItem.setOnAction(e -> {
                showChangeStatusDialog(entry);
            });
            
            // Solo mostrar "Cambiar estado" para citas médicas (no para reuniones o recordatorios)
            boolean isMedicalAppointment = entry.getCalendar() == calendars.get(0) || // Pendientes
                                         entry.getCalendar() == calendars.get(1) || // En curso
                                         entry.getCalendar() == calendars.get(2) || // Completadas
                                         entry.getCalendar() == calendars.get(3);   // Canceladas
            
            // Agregar opciones al menú
            contextMenu.getItems().addAll(editItem, deleteItem);
            if (isMedicalAppointment) {
                contextMenu.getItems().add(changeStatusItem);
            }
            
            return contextMenu;
        });
        
        // Agregar menú contextual para clic derecho en un día
        calendarView.getDayPage().setContextMenuCallback(new Callback<DateControl.ContextMenuParameter, ContextMenu>() {
            @Override
            public ContextMenu call(DateControl.ContextMenuParameter param) {
                ContextMenu contextMenu = new ContextMenu();
                
                MenuItem newAppointmentItem = new MenuItem("Nueva cita médica");
                MenuItem newMeetingItem = new MenuItem("Nueva reunión");
                MenuItem newReminderItem = new MenuItem("Nuevo recordatorio");
                
                // Configurar acciones
                newAppointmentItem.setOnAction(e -> {
                    // Obtener la fecha y hora exacta donde se hizo clic
                    ZonedDateTime fechaHoraClick = param.getZonedDateTime();
                    
                    // Logs de depuración detallados
                    System.out.println("🔍 DEPURACIÓN - Clic detectado:");
                    System.out.println("   📅 Fecha/hora del parámetro: " + fechaHoraClick);
                    System.out.println("   📅 Fecha/hora local: " + fechaHoraClick.toLocalDateTime());
                    System.out.println("   📅 Solo fecha: " + fechaHoraClick.toLocalDate());
                    System.out.println("   🕐 Solo hora: " + fechaHoraClick.toLocalTime());
                    
                    // Crear la entrada con la fecha/hora exacta
                    Entry<String> entry = new Entry<>("Nueva cita médica");
                    
                    // Establecer intervalo: desde la hora de clic hasta 30 minutos después (duración estándar)
                    ZonedDateTime horaInicio = fechaHoraClick;
                    ZonedDateTime horaFin = fechaHoraClick.plusMinutes(30);
                    
                    entry.setInterval(horaInicio, horaFin);
                    entry.setCalendar(calendars.get(0)); // Pendientes
                    entry.setId(""); // Asegurar que sea cadena vacía para nuevas citas
                    
                    System.out.println("🕒 Nueva cita creada en: " + horaInicio.toLocalDateTime() + " - " + horaFin.toLocalDateTime());
                    
                    showEntryDetailsDialog(entry);
                });
                
                newMeetingItem.setOnAction(e -> {
                    // Obtener la fecha y hora exacta donde se hizo clic
                    ZonedDateTime fechaHoraClick = param.getZonedDateTime();
                    
                    Entry<String> entry = new Entry<>("Nueva reunión");
                    
                    // Establecer intervalo: desde la hora de clic hasta 1 hora después
                    ZonedDateTime horaInicio = fechaHoraClick;
                    ZonedDateTime horaFin = fechaHoraClick.plusHours(1);
                    
                    entry.setInterval(horaInicio, horaFin);
                    entry.setCalendar(calendars.get(5)); // Reuniones (índice correcto)
                    entry.setId(""); // Asegurar que sea cadena vacía para nuevas reuniones
                    
                    System.out.println("🕒 Nueva reunión creada en: " + horaInicio.toLocalDateTime() + " - " + horaFin.toLocalDateTime());
                    
                    showEntryDetailsDialog(entry);
                });
                
                newReminderItem.setOnAction(e -> {
                    // Obtener la fecha y hora exacta donde se hizo clic
                    ZonedDateTime fechaHoraClick = param.getZonedDateTime();
                    
                    Entry<String> entry = new Entry<>("Nuevo recordatorio");
                    
                    // Establecer intervalo: desde la hora de clic hasta 1 hora después
                    ZonedDateTime horaInicio = fechaHoraClick;
                    ZonedDateTime horaFin = fechaHoraClick.plusHours(1);
                    
                    entry.setInterval(horaInicio, horaFin);
                    entry.setCalendar(calendars.get(6)); // Recordatorios (índice correcto)
                    entry.setId(""); // Asegurar que sea cadena vacía para nuevos recordatorios
                    
                    System.out.println("🕒 Nuevo recordatorio creado en: " + horaInicio.toLocalDateTime() + " - " + horaFin.toLocalDateTime());
                    
                    showEntryDetailsDialog(entry);
                });
                
                // Agregar items al menú
                contextMenu.getItems().addAll(newAppointmentItem, newMeetingItem, newReminderItem);
                
                return contextMenu;
            }
        });
    }
    
    /**
     * Configura tooltips personalizados para las entradas del calendario
     */
    private void setupCustomTooltips() {
        System.out.println("Iniciando configuración de tooltips personalizados...");
        
        // Programar múltiples intentos para asegurar que capturamos todas las entradas
        for (int i = 0; i < 5; i++) {
            final int delay = 1000 * (i + 1); // Mayores retrasos para asegurar que el calendario esté completamente renderizado
            new Thread(() -> {
                try {
                    Thread.sleep(delay);
                    Platform.runLater(() -> {
                        System.out.println("Intentando aplicar tooltips después de " + delay + "ms");
                        
                        // Configurar listeners para detectar nuevas entradas
                        if (calendarView.getDayPage() != null && calendarView.getDayPage().getDetailedDayView() != null) {
                            System.out.println("Configurando tooltips para vista de día");
                            setupMouseListeners(calendarView.getDayPage().getDetailedDayView().getDayView());
                        }
                        
                        if (calendarView.getWeekPage() != null && calendarView.getWeekPage().getDetailedWeekView() != null) {
                            System.out.println("Configurando tooltips para vista de semana");
                            setupMouseListeners(calendarView.getWeekPage().getDetailedWeekView().getWeekView());
                        }
                        
                        if (calendarView.getMonthPage() != null) {
                            System.out.println("Configurando tooltips para vista de mes");
                            setupMouseListeners(calendarView.getMonthPage().getMonthView());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Configura listeners de ratón para detectar entradas del calendario
     */
    private void setupMouseListeners(DateControl view) {
        if (view == null) {
            return;
        }
        
        // Tratamiento especial para la vista de mes
        boolean isMonthView = view.getClass().getSimpleName().contains("MonthView");
        
        // Nodo actual con tooltip para evitar reinstalaciones
        final Node[] lastNodeWithTooltip = {null};
        final Entry<?>[] lastEntryWithTooltip = {null};
        
        // Configurar un listener para eventos de movimiento del ratón con limitación de frecuencia
        view.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            // Limitar la frecuencia de procesamiento para evitar parpadeos
            Node node = e.getPickResult().getIntersectedNode();
            if (node != null) {
                // Si el nodo o uno de sus padres ya tiene un tooltip instalado, no hacer nada
                Node current = node;
                while (current != null && current != view) {
                    if (current == lastNodeWithTooltip[0]) {
                        return; // Ya tiene tooltip, no hacer nada
                    }
                    current = current.getParent();
                }
                
                // Buscar entrada para este nodo
                Entry<?> entry = null;
                Node targetNode = null;
                
                // Buscar en los padres hasta encontrar un nodo de entrada
                current = node;
                int currentDepth = 0;
                int maxDepth = 15;
                
                while (current != null && current != view && currentDepth < maxDepth) {
                    boolean foundEntry = false;
                    
                    // Para vista de mes, necesitamos técnicas más agresivas
                    if (isMonthView) {
                        // Verificar si tiene clases específicas de la vista de mes
                        if (isMonthEntryNode(current)) {
                            // Intentar obtener la información exacta de la celda
                            entry = findExactEntryForDay(current, e.getX(), e.getY());
                            if (entry != null) {
                                targetNode = current;
                                foundEntry = true;
                            }
                        }
                        
                        if (!foundEntry) {
                            // Intentar buscar en la información de texto
                            String nodeText = extractTextFromNode(current);
                            if (nodeText != null && !nodeText.isEmpty() && nodeText.length() > 3) {
                                // Buscar entrada que coincida con este texto exacto
                                entry = findExactEntryByText(nodeText, current);
                                if (entry != null) {
                                    targetNode = current;
                                    foundEntry = true;
                                }
                            }
                        }
                    }
                    
                    // Método general para todas las vistas
                    if (!foundEntry && isEntryNode(current)) {
                        entry = findEntryForNode(current);
                        if (entry != null) {
                            targetNode = current;
                            foundEntry = true;
                        }
                    }
                    
                    if (foundEntry) {
                        break;
                    }
                    
                    current = current.getParent();
                    currentDepth++;
                }
                
                // Si se encontró una entrada y es diferente a la última, actualizar el tooltip
                if (entry != null && targetNode != null) {
                    if (lastEntryWithTooltip[0] == null || !entry.equals(lastEntryWithTooltip[0])) {
                        // Quitar el tooltip anterior si existe
                        if (lastNodeWithTooltip[0] != null) {
                            Tooltip.uninstall(lastNodeWithTooltip[0], null);
                        }
                        
                        // Instalar el nuevo tooltip
                        createTooltipForEntry(targetNode, entry);
                        
                        // Actualizar referencias
                        lastNodeWithTooltip[0] = targetNode;
                        lastEntryWithTooltip[0] = entry;
                    }
                }
            }
        });
        
        // Limpiar el tooltip cuando el mouse sale de la vista
        view.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            if (lastNodeWithTooltip[0] != null) {
                Tooltip.uninstall(lastNodeWithTooltip[0], null);
                lastNodeWithTooltip[0] = null;
                lastEntryWithTooltip[0] = null;
            }
        });
    }
    
    /**
     * Crea un tooltip para una entrada específica
     */
    private void createTooltipForEntry(Node node, Entry<?> entry) {
        try {
            // Verificar si ya tiene un tooltip instalado
            Tooltip existingTooltip = null;
            if (node instanceof Control) {
                existingTooltip = ((Control) node).getTooltip();
            }
            
            if (existingTooltip != null) {
                // Ya tiene tooltip, no hacer nada
                return;
            }
            
            // Buscar información detallada
            String description = "";
            String usuario = "No asignado";
            
            if (entry.getId() != null && entryDescriptions.containsKey(entry.getId())) {
                description = entryDescriptions.get(entry.getId());
            }
            
            // Obtener el usuario asignado a este evento
            if (entry.getId() != null && !entry.getId().isEmpty()) {
                String entryId = entry.getId();
                
                // Remover prefijo si existe
                if (entryId.startsWith("_")) {
                    entryId = entryId.substring(1);
                }
                
                // Verificar si es un UUID (formato no compatible con MongoDB ObjectId)
                boolean isUUID = entryId.contains("-") && entryId.length() == 36;
                
                if (!isUUID) {
                    // Solo buscar en la base de datos si NO es UUID
                    GestorSocket gestorSocketEvento = null;
                    try {
                        gestorSocketEvento = GestorSocket.crearConexionIndependiente();
                        gestorSocketEvento.enviarPeticion(Protocolo.OBTENER_EVENTO_POR_ID+Protocolo.SEPARADOR_CODIGO+entry.getId());
                        ObjectInputStream ois = gestorSocketEvento.getEntrada();
                        int codigo = ois.readInt();
                        if(codigo == Protocolo.OBTENER_EVENTO_POR_ID_RESPONSE){
                            CalendarEvent calEvent = (CalendarEvent) ois.readObject();
                            if (calEvent != null && calEvent.getUsuario() != null) {
                                usuario = calEvent.getUsuario();
                            }
                        }
                    } catch (Exception e) {
                        // Si hay error al buscar, usar valor por defecto
                        System.out.println("Error al obtener usuario para tooltip: " + e.getMessage());
                    } finally {
                        if (gestorSocketEvento != null) {
                            gestorSocketEvento.cerrarConexion();
                        }
                    }
                } else {
                    // Si es UUID, es una entrada nueva, usar valor por defecto
                    usuario = "Nueva cita";
                }
            }
            
            // Crear un tooltip elaborado
            Tooltip tooltip = new Tooltip();
            
            // Estilo avanzado con CSS - forzar texto blanco con !important
            tooltip.setStyle("-fx-background-color: #333333; " +
                          "-fx-text-fill: white !important; " +
                          "-fx-font-size: 12px; " +
                          "-fx-padding: 10 15 10 15; " +
                          "-fx-background-radius: 6; " +
                          "-fx-font-weight: normal; " +
                          "-fx-font-family: 'System'; " +
                          "-fx-text-alignment: left; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);");
            
            // Construir contenido
            StringBuilder content = new StringBuilder();
            content.append(entry.getTitle()).append("\n\n");
            content.append("📅 ").append(entry.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
            content.append("⏰ ").append(entry.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                  .append(" - ").append(entry.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
            
            if (entry.getLocation() != null && !entry.getLocation().isEmpty()) {
                content.append("📍 ").append(entry.getLocation()).append("\n");
            }
            
            content.append("👤 ").append(usuario).append("\n");
            
            if (!description.isEmpty()) {
                content.append("\n📝 ").append(description);
            }
            
            tooltip.setText(content.toString());
            
            // Configurar el tooltip con tiempos ajustados para reducir parpadeo
            tooltip.setShowDelay(Duration.millis(100));  // Mostrar más rápido
            tooltip.setShowDuration(Duration.seconds(60)); // Duración larga
            tooltip.setHideDelay(Duration.seconds(1));  // Retraso al ocultar para reducir parpadeo
            
            // Aplicar el tooltip al nodo
            Tooltip.install(node, tooltip);
        } catch (Exception e) {
            // Error silencioso
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void showErrorMessage(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Método para desconectar este componente del ThemeManager
     * recorriendo todos los nodos y aplicando estilos directos
     */
    private void desconectarDelThemeManager(Node nodo) {
        try {
            // Desconectar nodo actual
            if (nodo != null) {
                // Aplicar estilos directos según el tipo de nodo
                if (nodo instanceof Button) {
                    Button btn = (Button) nodo;
                    
                    // Aplicar texto negro por defecto a botones
                    btn.setTextFill(Color.BLACK);
                    
                    // Si es botón de impresión, forzar estilo rojo
                    if (btn.getText() != null && 
                        (btn.getText().equals("Print") || btn.getText().equals("Imprimir"))) {
                        
                        btn.setBackground(new javafx.scene.layout.Background(
                            new javafx.scene.layout.BackgroundFill(
                                javafx.scene.paint.Color.web("#EA4335"), 
                                new javafx.scene.layout.CornerRadii(3), 
                                javafx.geometry.Insets.EMPTY
                            )
                        ));
                        btn.setTextFill(javafx.scene.paint.Color.WHITE);
                    }
                } 
                // Si es barra de búsqueda
                else if (nodo instanceof TextField) {
                    TextField searchField = (TextField) nodo;
                    searchField.setStyle(searchField.getStyle() + "; -fx-text-fill: black;");
                    
                    if (searchField.getPromptText() != null && 
                        (searchField.getPromptText().contains("Search") || 
                         searchField.getPromptText().contains("Buscar"))) {
                        
                        searchField.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-prompt-text-fill: #999; -fx-text-fill: black;");
                    }
                }
                // Si es una etiqueta o texto, forzar color negro
                else if (nodo instanceof Label) {
                    ((Label) nodo).setTextFill(Color.BLACK);
                }
                else if (nodo instanceof Text) {
                    ((Text) nodo).setFill(Color.BLACK);
                }
            }
            
            // Recursivamente desconectar todos los hijos
            if (nodo instanceof Parent) {
                Parent parent = (Parent) nodo;
                for (Node hijo : parent.getChildrenUnmodifiable()) {
                    desconectarDelThemeManager(hijo);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en desconectarDelThemeManager: " + e.getMessage());
        }
    }
    
    /**
     * Aplica color negro a todos los textos del calendario
     */
    private void aplicarTextoNegro(Node nodo) {
        try {
            // Aplicar estilo de texto negro a todos los nodos de texto
            if (nodo instanceof Text) {
                ((Text) nodo).setFill(Color.BLACK);
            } else if (nodo instanceof Label) {
                ((Label) nodo).setTextFill(Color.BLACK);
            } else if (nodo instanceof Button) {
                ((Button) nodo).setTextFill(Color.BLACK);
            } else if (nodo instanceof TextField) {
                ((TextField) nodo).setStyle(((TextField) nodo).getStyle() + "; -fx-text-fill: black;");
            } else if (nodo instanceof Control) {
                ((Control) nodo).setStyle(((Control) nodo).getStyle() + "; -fx-text-fill: black;");
            }
            
            // Recorrer recursivamente todos los nodos hijos
            if (nodo instanceof Parent) {
                for (Node hijo : ((Parent) nodo).getChildrenUnmodifiable()) {
                    aplicarTextoNegro(hijo);
                }
            }
        } catch (Exception e) {

        }
    }
    
    /**
     * Refresca el calendario
     */
    public void refreshCalendar() {
        if (calendarView != null) {
            calendarView.refreshData();
        }
    }
    
    /**
     * Obtiene la vista principal del calendario
     * 
     * @return La vista del calendario
     */
    public CalendarView getCalendarView() {
        return calendarView;
    }
    
    /**
     * Extrae texto de un nodo (útil para vista de mes)
     */
    private String extractTextFromNode(Node node) {
        if (node instanceof Text) {
            return ((Text) node).getText();
        } else if (node instanceof Label) {
            return ((Label) node).getText();
        }
        
        // Buscar textos en hijos
        if (node instanceof Parent) {
            StringBuilder result = new StringBuilder();
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                String childText = extractTextFromNode(child);
                if (childText != null && !childText.isEmpty()) {
                    if (result.length() > 0) {
                        result.append(" ");
                    }
                    result.append(childText);
                }
            }
            return result.toString();
        }
        
        return "";
    }
    
    /**
     * Verifica si un nodo es un nodo de entrada de calendario
     */
    private boolean isEntryNode(Node node) {
        if (node == null) return false;
        
        // Verificar por estructura o clase específica
        if (node.getStyleClass() != null) {
            for (String styleClass : node.getStyleClass()) {
                if (styleClass.contains("entry") || 
                    styleClass.contains("calendar-entry") || 
                    styleClass.contains("day-entry") ||
                    styleClass.contains("month-entry") ||
                    styleClass.contains("all-day-entry")) {
                    return true;
                }
            }
        }
        
        // Verificar si el userData es una entrada
        if (node.getUserData() instanceof Entry) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Intenta encontrar la entrada asociada a un nodo
     */
    private Entry<?> findEntryForNode(Node node) {
        // Intentar obtener el Entry del userData
        if (node.getUserData() instanceof Entry) {
            return (Entry<?>) node.getUserData();
        }
        
        // Intentar obtener el Entry mediante propiedades
        for (Calendar calendar : calendars) {
            // Obtener las entradas del calendario para un periodo amplio
            Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                LocalDate.now().minusDays(30), 
                LocalDate.now().plusDays(365), 
                ZoneId.systemDefault()
            );
            
            // Iterar por cada día y sus entradas
            for (List<Entry<?>> entries : entriesMap.values()) {
                for (Entry<?> entry : entries) {
                    // Intentar comparar el título del entry con algún texto en el nodo
                    if (containsEntryInfo(node, entry)) {
                        return entry;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Verifica si un nodo contiene información sobre una entrada específica
     */
    private boolean containsEntryInfo(Node node, Entry<?> entry) {
        // Buscar todos los textos dentro del nodo
        List<Text> textNodes = findTextNodes(node);
        
        for (Text text : textNodes) {
            // Si el texto contiene el título del entry, probablemente es este entry
            if (text.getText() != null && 
                entry.getTitle() != null && 
                text.getText().contains(entry.getTitle())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Encuentra todos los nodos de texto dentro de un nodo padre
     */
    private List<Text> findTextNodes(Node node) {
        List<Text> result = new ArrayList<>();
        
        if (node instanceof Text) {
            result.add((Text) node);
        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                result.addAll(findTextNodes(child));
            }
        }
        
        return result;
    }
    
    /**
     * Verifica si un nodo es específicamente un nodo de entrada en la vista de mes
     */
    private boolean isMonthEntryNode(Node node) {
        if (node == null) return false;
        
        // Clases específicas de la vista de mes
        if (node.getStyleClass() != null) {
            for (String styleClass : node.getStyleClass()) {
                if (styleClass.contains("month-day-entry") || 
                    styleClass.contains("month-entry") ||
                    styleClass.contains("month-view-entry") ||
                    styleClass.contains("badge") ||
                    styleClass.contains("pill") ||
                    styleClass.contains("event")) {
                    return true;
                }
            }
        }
        
        // Verificar por el tipo de nodo
        String className = node.getClass().getSimpleName();
        if (className.contains("EntryView") || 
            className.contains("MonthEntryView") ||
            className.contains("DayEntryView")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Encuentra la entrada exacta para un día en particular basado en la posición del cursor
     */
    private Entry<?> findExactEntryForDay(Node node, double x, double y) {
        // Intentar obtener el día del mes
        LocalDate day = extractDayFromNode(node);
        if (day == null) {
            return null;
        }
        
        // Obtener entradas para este día específico
        List<Entry<?>> entriesForDay = new ArrayList<>();
        for (Calendar calendar : calendars) {
            Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                day, day, ZoneId.systemDefault()
            );
            
            if (entriesMap.containsKey(day)) {
                entriesForDay.addAll(entriesMap.get(day));
            }
        }
        
        if (entriesForDay.isEmpty()) {
            return null;
        }
        
        // Si solo hay una entrada, devolver esa
        if (entriesForDay.size() == 1) {
            return entriesForDay.get(0);
        }
        
        // Si hay múltiples entradas, intentar con el texto
        String nodeText = extractTextFromNode(node);
        if (nodeText != null && !nodeText.isEmpty()) {
            for (Entry<?> entry : entriesForDay) {
                if (entry.getTitle() != null && nodeText.contains(entry.getTitle())) {
                    return entry;
                }
            }
        }
        
        // Fallback: devolver la primera entrada
        return entriesForDay.get(0);
    }
    
    /**
     * Extrae la fecha del día de un nodo de la vista de mes
     */
    private LocalDate extractDayFromNode(Node node) {
        // Intentar encontrar la fecha en el nodo o en sus padres
        Node current = node;
        while (current != null) {
            if (current.getUserData() instanceof LocalDate) {
                return (LocalDate) current.getUserData();
            }
            
            // También buscar texto que sea una fecha
            String text = extractTextFromNode(current);
            if (text != null && !text.isEmpty()) {
                try {
                    // Buscar patrones de fecha
                    if (text.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        return LocalDate.parse(text, formatter);
                    }
                } catch (Exception e) {
                    // Ignorar errores de parseo
                }
            }
            
            current = current.getParent();
        }
        
        return null;
    }
    
    /**
     * Encuentra una entrada exacta por texto
     */
    private Entry<?> findExactEntryByText(String text, Node node) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        // Buscar en todos los calendarios las entradas exactas
        for (Calendar calendar : calendars) {
            // Obtener las entradas para un período amplio
            Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                LocalDate.now().minusDays(30), 
                LocalDate.now().plusDays(365), 
                ZoneId.systemDefault()
            );
            
            // Revisar todas las entradas por coincidencia exacta
            for (List<Entry<?>> entries : entriesMap.values()) {
                for (Entry<?> entry : entries) {
                    if (entry.getTitle() != null && 
                        text.contains(entry.getTitle())) {
                        return entry;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Determina si un evento es una cita médica
     * @param event Evento a evaluar
     * @return true si es una cita médica
     */
    private boolean esCitaMedica(CalendarEvent event) {
        return event.getTipoEvento() == CalendarEvent.EventoTipo.CITA_MEDICA;
    }
    
    /**
     * Determina si un evento es una reunión
     * @param event Evento a evaluar
     * @return true si es una reunión
     */
    private boolean esReunion(CalendarEvent event) {
        return event.getTipoEvento() == CalendarEvent.EventoTipo.REUNION;
    }
    
    /**
     * Determina si un evento es un recordatorio
     * @param event Evento a evaluar
     * @return true si es un recordatorio
     */
    private boolean esRecordatorio(CalendarEvent event) {
        return event.getTipoEvento() == CalendarEvent.EventoTipo.RECORDATORIO;
    }

    // Agrego el método para abrir el formulario de facturación desde una cita
    private void abrirFormularioFacturacionDesdeCita(CalendarEvent event) {
        try {
            // Obtener la cita, paciente y propietario
            com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica servicioClinica = new com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica();
            org.bson.types.ObjectId citaId = new org.bson.types.ObjectId(event.getId());
            com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita cita = servicioClinica.obtenerCitaPorId(citaId);
            com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente paciente = servicioClinica.obtenerPacientePorId(cita.getPacienteId());
            com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario propietario = servicioClinica.obtenerPropietarioPorId(paciente.getPropietarioId());

            // Cargar el formulario de facturación
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Facturacion/factura-form.fxml"));
            Parent root = loader.load();
            com.example.pruebamongodbcss.Modulos.Facturacion.FacturaFormController controller = loader.getController();
            controller.cargarDatosDesdeCita(cita, paciente, propietario);

            Stage stage = new Stage();
            stage.setTitle("Facturación - Cita: " + cita.getNombrePaciente());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();

            // Refrescar el calendario después de cerrar
            refreshCalendarFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error", "No se pudo abrir el formulario de facturación: " + e.getMessage());
        }
    }
    
    /**
     * Verifica y actualiza automáticamente los estados de las citas
     */
    private void verificarYActualizarEstadosAutomaticos() {
        // Usar una conexión independiente para evitar conflictos
        GestorSocket gestorSocketAuto = null;
        try {
            gestorSocketAuto = GestorSocket.crearConexionIndependiente();
            
            System.out.println("🔄 Verificando estados automáticos de citas...");
            
            // Solicitar verificación automática al servidor
            gestorSocketAuto.enviarPeticion(Protocolo.DAMETODASLASCITAS + Protocolo.SEPARADOR_CODIGO);
            ObjectInputStream ois = gestorSocketAuto.getEntrada();
            int codigo = ois.readInt();
            
            if (codigo != Protocolo.DAMETODASLASCITAS_RESPONSE) {
                System.err.println("❌ Error al obtener citas para verificación automática");
                return;
            }
            
            List<CalendarEvent> todasLasCitas = (List<CalendarEvent>) ois.readObject();
            System.out.println("📋 Verificando " + todasLasCitas.size() + " citas para actualización automática");
            
            // Aquí puedes agregar lógica para verificar y actualizar estados automáticamente
            // Por ejemplo, cambiar citas vencidas a "PERDIDA" o similar
            
        } catch (Exception e) {
            System.err.println("❌ Error en verificación automática: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (gestorSocketAuto != null) {
                try {
                    gestorSocketAuto.cerrarConexion();
                } catch (Exception e) {
                    System.err.println("⚠️ Error al cerrar conexión automática: " + e.getMessage());
                }
            }
        }
    }
}