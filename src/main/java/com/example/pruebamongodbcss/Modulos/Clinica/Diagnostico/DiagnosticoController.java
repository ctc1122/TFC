package com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloDiagnostico;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPropietario;
import com.itextpdf.text.DocumentException;

import Utilidades1.GestorSocket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controlador para la pantalla de diagnósticos médicos.
 */
public class DiagnosticoController implements Initializable {

    // Datos del paciente
    @FXML private Label lblNombrePaciente;
    @FXML private Label lblEspecie;
    @FXML private Label lblRaza;
    @FXML private Label lblEdad;
    @FXML private Label lblSexo;
    @FXML private Label lblPeso;
    
    // Datos del diagnóstico
    // @FXML private DatePicker dpFecha;
    @FXML private TextField txtMotivo;
    @FXML private TextArea txtAnamnesis;
    @FXML private TextArea txtExamenFisico;
    
    // Búsqueda de diagnósticos
    @FXML private TextField txtBusquedaDiagnostico;
    @FXML private TableView<ModeloDiagnosticoUMLS> tblDiagnosticos;
    @FXML private TableColumn<ModeloDiagnosticoUMLS, String> colDescripcion;
    @FXML private TableColumn<ModeloDiagnosticoUMLS, String> colCodigo;
    @FXML private TableColumn<ModeloDiagnosticoUMLS, String> colFuente;
    
    // Lista de diagnósticos seleccionados
    @FXML private ListView<ModeloDiagnosticoUMLS> lstDiagnosticosSeleccionados;
    @FXML private Button btnAgregarDiagnostico;
    @FXML private Button btnQuitarDiagnostico;
    
    // Exportación
    @FXML private Button btnExportarPDF;
    @FXML private Button btnExportarCSV;
    
    @FXML private TextArea txtTratamiento;
    @FXML private TextArea txtObservaciones;
    @FXML private DatePicker dpProximaVisita;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private VBox contenedorPrincipal;
    
    // Datos seleccionados
    private ModeloDiagnosticoUMLS diagnosticoSeleccionado;
    private ModeloPaciente paciente;
    private ModeloDiagnostico diagnosticoActual;
    private ModeloCita citaActual;
    
    // Servicios
    private GestorSocket gestorSocket;
    private final ServicioDiagnosticoUMLS servicioDiagnosticoUMLS = new ServicioDiagnosticoUMLS();
    
    // Lista observable para almacenar los diagnósticos seleccionados
    private ObservableList<ModeloDiagnosticoUMLS> diagnosticosSeleccionados = FXCollections.observableArrayList();
    
    // Executor para búsqueda en tiempo real
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private Runnable onGuardarCallback;
    private Runnable onCancelarCallback;
    
    @FXML private Label lblFechaCita;
    @FXML private Label lblHoraCita;
    @FXML private Label lblMotivoCita;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializar gestor de socket
        gestorSocket = GestorSocket.getInstance();
        
        // Configurar las partes esenciales de la interfaz
        configurarTabla();
        configurarControlesDiagnosticos();
        configurarBusquedaEnTiempoReal();
        
        // Configurar lista de diagnósticos seleccionados
        lstDiagnosticosSeleccionados.setItems(diagnosticosSeleccionados);
        
        // Inicializar lista vacía para mostrar la interfaz inmediatamente
        tblDiagnosticos.setItems(FXCollections.observableArrayList());
        
        // Cargar datos iniciales de diagnósticos en segundo plano
        Platform.runLater(() -> {
            executorService.submit(() -> {
                try {
                    ObservableList<ModeloDiagnosticoUMLS> diagnosticos = 
                        servicioDiagnosticoUMLS.obtenerDiagnosticosLimitados(50);
                    Platform.runLater(() -> {
                        tblDiagnosticos.setItems(diagnosticos);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarError("Error al cargar diagnósticos", 
                            "No se pudieron cargar los diagnósticos. " +
                            "Puede intentar buscar un diagnóstico específico.");
                }
            });
        });
    }
    
    /**
     * Configura las columnas de la tabla de diagnósticos.
     */
    private void configurarTabla() {
        colDescripcion.setCellValueFactory(cellData -> cellData.getValue().strProperty());
        colCodigo.setCellValueFactory(cellData -> cellData.getValue().cuiProperty());
        colFuente.setCellValueFactory(cellData -> cellData.getValue().sabProperty());
        
        // Ajustar anchos de columnas
        colDescripcion.prefWidthProperty().bind(tblDiagnosticos.widthProperty().multiply(0.6));
        colCodigo.prefWidthProperty().bind(tblDiagnosticos.widthProperty().multiply(0.2));
        colFuente.prefWidthProperty().bind(tblDiagnosticos.widthProperty().multiply(0.2));
    }
    
    /**
     * Configura los controles relacionados con la selección de diagnósticos.
     */
    private void configurarControlesDiagnosticos() {
        // Configurar eventos de la interfaz de usuario
        btnAgregarDiagnostico.setOnAction(event -> agregarDiagnostico());
        btnQuitarDiagnostico.setOnAction(event -> quitarDiagnostico());
        btnGuardar.setOnAction(event -> guardarDiagnostico());
        btnCancelar.setOnAction(event -> cancelar());
        
        // Configurar selección de diagnóstico en la tabla
        tblDiagnosticos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            diagnosticoSeleccionado = newVal;
        });
        
        // Configurar selección en la lista de diagnósticos seleccionados
        lstDiagnosticosSeleccionados.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                // Puedes hacer algo cuando se selecciona un diagnóstico de la lista
            });
    }
    
    /**
     * Configura la búsqueda en tiempo real mientras el usuario escribe.
     */
    private void configurarBusquedaEnTiempoReal() {
        // Añadir listener para actualizar la tabla mientras se escribe
        txtBusquedaDiagnostico.textProperty().addListener((observable, oldValue, newValue) -> {
            // Cancelar cualquier búsqueda previa programada
            scheduledExecutorService.schedule(() -> {
                executorService.submit(() -> {
                    // Realizar búsqueda en hilo separado
                    ObservableList<ModeloDiagnosticoUMLS> resultados = servicioDiagnosticoUMLS.buscarDiagnosticosPorTexto(newValue);
                    
                    // Actualizar UI en hilo de JavaFX
                    Platform.runLater(() -> {
                        tblDiagnosticos.setItems(resultados);
                    });
                });
            }, 300, TimeUnit.MILLISECONDS); // Retraso para evitar búsquedas excesivas
        });
        
        // También permitir buscar al presionar Enter
        txtBusquedaDiagnostico.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                buscarDiagnosticos();
            }
        });
    }
    
    /**
     * Carga los datos iniciales para la tabla de diagnósticos.
     */
    private void cargarDatosDiagnosticosIniciales() {
        try {
            // Usamos el método obtenerDiagnosticosLimitados para mejorar el rendimiento
            ObservableList<ModeloDiagnosticoUMLS> diagnosticos = servicioDiagnosticoUMLS.obtenerDiagnosticosLimitados(50);
            
            Platform.runLater(() -> {
                tblDiagnosticos.setItems(diagnosticos);
            });
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al cargar diagnósticos", 
                     "No se pudieron cargar los diagnósticos desde la base de datos. " +
                     "Verifique la conexión con el servidor MariaDB.");
        }
    }
    
    /**
     * Busca diagnósticos según el texto ingresado.
     */
    @FXML
    private void buscarDiagnosticos() {
        String texto = txtBusquedaDiagnostico.getText();
        if (texto == null || texto.isBlank()) {
            cargarDatosDiagnosticosIniciales();
            return;
        }
        
        executorService.submit(() -> {
            ObservableList<ModeloDiagnosticoUMLS> resultados = servicioDiagnosticoUMLS.buscarDiagnosticosPorTexto(texto);
            Platform.runLater(() -> {
                tblDiagnosticos.setItems(resultados);
            });
        });
    }
    
    /**
     * Agrega el diagnóstico seleccionado a la lista de diagnósticos seleccionados.
     */
    @FXML
    private void agregarDiagnostico() {
        ModeloDiagnosticoUMLS diagnostico = tblDiagnosticos.getSelectionModel().getSelectedItem();
        if (diagnostico != null && !diagnosticosSeleccionados.contains(diagnostico)) {
            diagnosticosSeleccionados.add(diagnostico);
        }
    }
    
    /**
     * Quita el diagnóstico seleccionado de la lista de diagnósticos seleccionados.
     */
    @FXML
    private void quitarDiagnostico() {
        ModeloDiagnosticoUMLS diagnostico = lstDiagnosticosSeleccionados.getSelectionModel().getSelectedItem();
        if (diagnostico != null) {
            diagnosticosSeleccionados.remove(diagnostico);
        }
    }
    
    /**
     * Actualiza la interfaz con el diagnóstico seleccionado.
     */
    private void actualizarDiagnosticoSeleccionado() {
        if (diagnosticoSeleccionado != null) {
            // Aquí puedes mostrar detalles adicionales del diagnóstico seleccionado
            // o preparar los datos para guardar
            txtAnamnesis.appendText("\n\nDiagnóstico seleccionado: " + diagnosticoSeleccionado.getStr());
        }
    }
    
    /**
     * Establece el paciente para el diagnóstico.
     * @param paciente Paciente seleccionado
     */
    public void setPaciente(ModeloPaciente paciente, ModeloCita citaSeleccionada) {
        this.paciente = paciente;
        this.citaActual = citaSeleccionada;
        actualizarDatosPaciente();
        if (citaSeleccionada != null) {
            String fecha = citaSeleccionada.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String hora = citaSeleccionada.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String motivo = citaSeleccionada.getMotivo();
            // Rellenar los labels del FXML
            lblFechaCita.setText(fecha);
            lblHoraCita.setText(hora);
            lblMotivoCita.setText(motivo);
        } else {
            lblFechaCita.setText("");
            lblHoraCita.setText("");
            lblMotivoCita.setText("");
        }
    }
    
    /**
     * Establece un diagnóstico existente para editarlo.
     * @param diagnostico Diagnóstico a editar
     */
    public void setDiagnostico(ModeloDiagnostico diagnostico) {
        this.diagnosticoActual = diagnostico;
        cargarDatosDiagnostico();
    }
    
    /**
     * Actualiza la interfaz con los datos del paciente.
     */
    private void actualizarDatosPaciente() {
        if (paciente != null) {
            lblNombrePaciente.setText(paciente.getNombre());
            lblEspecie.setText(paciente.getEspecie());
            lblRaza.setText(paciente.getRaza());
            
            // Calcular edad a partir de la fecha de nacimiento
            String edadTexto = "No disponible";
            if (paciente.getFechaNacimiento() != null) {
                Date fechaNacimiento = paciente.getFechaNacimiento();
                Date fechaActual = new Date();
                long diferenciaMilisegundos = fechaActual.getTime() - fechaNacimiento.getTime();
                long edadAnios = diferenciaMilisegundos / (1000L * 60 * 60 * 24 * 365);
                edadTexto = edadAnios + " años";
            }
            lblEdad.setText(edadTexto);
            
            lblSexo.setText(paciente.getSexo());
            lblPeso.setText(paciente.getPeso() + " kg");
        }
    }
    
    /**
     * Carga los datos de un diagnóstico existente en la interfaz.
     */
    private void cargarDatosDiagnostico() {
        if (diagnosticoActual != null) {
            // if (diagnosticoActual.getFecha() != null) {
            //     dpFecha.setValue(diagnosticoActual.getFecha().toInstant()
            //             .atZone(ZoneId.systemDefault())
            //             .toLocalDate());
            // }
            
            // txtMotivo.setText(diagnosticoActual.getMotivo());
            txtAnamnesis.setText(diagnosticoActual.getAnamnesis());
            txtExamenFisico.setText(diagnosticoActual.getExamenFisico());
            txtTratamiento.setText(diagnosticoActual.getTratamiento());
            txtObservaciones.setText(diagnosticoActual.getObservaciones());
            
            if (diagnosticoActual.getProximaVisita() != null) {
                dpProximaVisita.setValue(diagnosticoActual.getProximaVisita().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
            }
            
            // Si hay diagnóstico, intentar cargar en la lista de seleccionados
            if (diagnosticoActual.getDiagnostico() != null && !diagnosticoActual.getDiagnostico().isBlank()) {
                // Este es un proceso simplificado. En una aplicación real, habría que parsear 
                // el diagnóstico almacenado y buscar los objetos correspondientes en la base de datos
                String diagnosticoTexto = diagnosticoActual.getDiagnostico();
                
                // Ejemplo básico: extraer CUI y buscar en la base de datos
                int cuiIndex = diagnosticoTexto.indexOf("CUI: ");
                if (cuiIndex > 0) {
                    int endIndex = diagnosticoTexto.indexOf(",", cuiIndex);
                    if (endIndex > 0) {
                        String cui = diagnosticoTexto.substring(cuiIndex + 5, endIndex);
                        executorService.submit(() -> {
                            ModeloDiagnosticoUMLS diag = servicioDiagnosticoUMLS.buscarDiagnosticoPorCUI(cui);
                            if (diag != null) {
                                Platform.runLater(() -> diagnosticosSeleccionados.add(diag));
                            }
                        });
                    }
                }
            }
        }
    }
    
    /**
     * Guarda el diagnóstico actual.
     */
    @FXML
    public void guardarDiagnostico() {
        if (paciente == null) {
            mostrarError("Error", "No hay paciente seleccionado");
            return;
        }
        
        if (diagnosticosSeleccionados.isEmpty()) {
            mostrarError("Datos incompletos", "Debe seleccionar al menos un diagnóstico");
            return;
        }
        
        // Crear o actualizar el diagnóstico
        ModeloDiagnostico diagnostico = (diagnosticoActual != null) 
                ? diagnosticoActual : new ModeloDiagnostico();
        
        // Establecer datos básicos
        diagnostico.setPacienteId(paciente.getId());
        diagnostico.setNombrePaciente(paciente.getNombre());
        
        // CORREGIDO: Establecer fecha y motivo desde la cita actual
        if (citaActual != null) {
            // Convertir LocalDateTime de la cita a Date para el diagnóstico
            Date fechaDiagnostico = Date.from(citaActual.getFechaHora()
                .atZone(java.time.ZoneId.systemDefault()).toInstant());
            diagnostico.setFecha(fechaDiagnostico);
            
            // Establecer el motivo desde la cita
            diagnostico.setMotivo(citaActual.getMotivo());
            
            // Establecer ID de la cita asociada
            diagnostico.setCitaId(citaActual.getId());
        } else {
            // Si no hay cita, usar fecha actual
            diagnostico.setFecha(new Date());
            diagnostico.setMotivo("Consulta general");
        }
        
        diagnostico.setAnamnesis(txtAnamnesis.getText());
        diagnostico.setExamenFisico(txtExamenFisico.getText());
        
        // Establecer diagnósticos seleccionados
        StringBuilder diagnosticoTexto = new StringBuilder();
        for (ModeloDiagnosticoUMLS diag : diagnosticosSeleccionados) {
            if (diagnosticoTexto.length() > 0) {
                diagnosticoTexto.append("; ");
            }
            diagnosticoTexto.append(diag.getStr())
                    .append(" (CUI: ").append(diag.getCui())
                    .append(", Fuente: ").append(diag.getSab()).append(")");
        }
        diagnostico.setDiagnostico(diagnosticoTexto.toString());
        
        diagnostico.setTratamiento(txtTratamiento.getText());
        diagnostico.setObservaciones(txtObservaciones.getText());
        
        if (dpProximaVisita.getValue() != null) {
            diagnostico.setProximaVisita(Date.from(dpProximaVisita.getValue()
                    .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        }
        
        // Establecer veterinario desde el usuario actual o desde la cita
        if (diagnostico.getVeterinario() == null || diagnostico.getVeterinario().isBlank()) {
            if (citaActual != null && citaActual.getNombreVeterinario() != null) {
                diagnostico.setVeterinario(citaActual.getNombreVeterinario());
            } else {
                diagnostico.setVeterinario("Dr. Veterinario");
            }
        }
        
        // Guardar diagnóstico usando protocolo
        try {
            gestorSocket.enviarPeticion(com.example.pruebamongodbcss.Protocolo.Protocolo.GUARDAR_DIAGNOSTICO + com.example.pruebamongodbcss.Protocolo.Protocolo.SEPARADOR_CODIGO);
            gestorSocket.getSalida().writeObject(diagnostico);
            gestorSocket.getSalida().flush();
            
            int codigo = gestorSocket.getEntrada().readInt();
            if (codigo == com.example.pruebamongodbcss.Protocolo.Protocolo.GUARDAR_DIAGNOSTICO_RESPONSE) {
                ObjectId id = (ObjectId) gestorSocket.getEntrada().readObject();
                if (id != null) {
                    diagnostico.setId(id);
                    
                    // Mostrar mensaje de éxito inmediato
                    mostrarInformacion("Diagnóstico guardado", "El diagnóstico ha sido guardado correctamente.");
                    
                    // Actualizar contador de diagnósticos en el calendario si hay cita asociada
                    if (citaActual != null && citaActual.getId() != null) {
                        try {
                            // Incrementar contador de diagnósticos en el calendario
                            com.example.pruebamongodbcss.calendar.CalendarService calendarService = 
                                new com.example.pruebamongodbcss.calendar.CalendarService();
                            boolean contadorActualizado = calendarService.actualizarContadorDiagnosticos(
                                citaActual.getId().toString(), true);
                            
                            if (contadorActualizado) {
                                System.out.println("✅ Contador de diagnósticos actualizado en el calendario");
                            } else {
                                System.out.println("⚠️ No se pudo actualizar el contador de diagnósticos en el calendario");
                            }
                        } catch (Exception e) {
                            System.err.println("Error al actualizar contador de diagnósticos: " + e.getMessage());
                        }
                    }
                    
                    // Cambiar estado de la cita a 'PENDIENTE_DE_FACTURAR' si hay cita asociada
                    if (citaActual != null && citaActual.getId() != null) {
                        // Usar Platform.runLater para evitar problemas de concurrencia
                        Platform.runLater(() -> {
                            try {
                                // Cambiar estado de la cita
                                gestorSocket.enviarPeticion(com.example.pruebamongodbcss.Protocolo.Protocolo.CAMBIAR_ESTADO_CITA + 
                                    com.example.pruebamongodbcss.Protocolo.Protocolo.SEPARADOR_CODIGO + 
                                    citaActual.getId().toString() + ":" + "PENDIENTE_DE_FACTURAR");
                                
                                int codigoEstado = gestorSocket.getEntrada().readInt();
                                if (codigoEstado == com.example.pruebamongodbcss.Protocolo.Protocolo.CAMBIAR_ESTADO_CITA_RESPONSE) {
                                    boolean cambiado = gestorSocket.getEntrada().readBoolean();
                                    if (cambiado) {
                                        System.out.println("✅ Estado de cita cambiado a PENDIENTE_DE_FACTURAR");
                                    }
                                }
                                
                                // Ejecutar callback y cerrar
                                if (onGuardarCallback != null) {
                                    onGuardarCallback.run();
                                }
                                
                                Stage stage = (Stage) btnGuardar.getScene().getWindow();
                                stage.close();
                                
                            } catch (Exception e) {
                                System.err.println("Error al cambiar estado de cita: " + e.getMessage());
                                // Aún así ejecutar callback y cerrar
                                if (onGuardarCallback != null) {
                                    onGuardarCallback.run();
                                }
                                Stage stage = (Stage) btnGuardar.getScene().getWindow();
                                stage.close();
                            }
                        });
                    } else {
                        // Si no hay cita asociada, ejecutar callback y cerrar de todos modos
                        if (onGuardarCallback != null) {
                            onGuardarCallback.run();
                        }
                        
                        // Cerrar el formulario
                        Platform.runLater(() -> {
                            Stage stage = (Stage) btnGuardar.getScene().getWindow();
                            stage.close();
                        });
                    }
                } else {
                    mostrarError("Error", "No se pudo guardar el diagnóstico.");
                }
            } else if (codigo == com.example.pruebamongodbcss.Protocolo.Protocolo.ERROR_GUARDAR_DIAGNOSTICO) {
                String errorMsg = gestorSocket.getEntrada().readUTF();
                mostrarError("Error del servidor", "Error al guardar diagnóstico: " + errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de comunicación", "No se pudo comunicar con el servidor: " + e.getMessage());
        }
    }
    
    /**
     * Exporta el diagnóstico a un archivo PDF profesional.
     */
    @FXML
    private void exportarPDF() {
        if (paciente == null) {
            mostrarError("Error", "No hay paciente seleccionado para exportar");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Informe PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("diagnostico_" + paciente.getNombre().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
        
        // Obtener ventana actual para mostrar el diálogo
        Stage stage = (Stage) contenedorPrincipal.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            executorService.submit(() -> {
                try {
                    // Obtener propietario usando protocolo
                    ModeloPropietario propietario = null;
                    if (paciente.getPropietarioId() != null) {
                        try {
                            gestorSocket.enviarPeticion(com.example.pruebamongodbcss.Protocolo.Protocolo.OBTENERPROPIETARIO_POR_ID + 
                                com.example.pruebamongodbcss.Protocolo.Protocolo.SEPARADOR_CODIGO + 
                                paciente.getPropietarioId().toString());
                            
                            int codigo = gestorSocket.getEntrada().readInt();
                            if (codigo == com.example.pruebamongodbcss.Protocolo.Protocolo.OBTENERPROPIETARIO_POR_ID_RESPONSE) {
                                propietario = (ModeloPropietario) gestorSocket.getEntrada().readObject();
                            }
                        } catch (Exception e) {
                            System.err.println("Error al obtener propietario: " + e.getMessage());
                        }
                    }
                    
                    // Obtener fecha de próxima visita
                    Date proximaVisita = null;
                    if (dpProximaVisita.getValue() != null) {
                        proximaVisita = Date.from(dpProximaVisita.getValue()
                                .atStartOfDay(ZoneId.systemDefault()).toInstant());
                    }
                    
                    // Generar el PDF usando la clase utilitaria
                    GeneradorPDFDiagnostico.generarPDFDiagnostico(
                        file,
                        paciente,
                        propietario,
                        null, // cita - se puede pasar si está disponible
                        diagnosticoActual,
                        diagnosticosSeleccionados,
                        txtAnamnesis.getText(),
                        txtExamenFisico.getText(),
                        txtTratamiento.getText(),
                        txtObservaciones.getText(),
                        proximaVisita
                    );
                    
                    Platform.runLater(() -> {
                        mostrarInformacion("Exportación exitosa", 
                                "El diagnóstico ha sido exportado correctamente como PDF");
                    });
                    
                } catch (DocumentException | IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        mostrarError("Error al exportar", 
                                "No se pudo exportar el diagnóstico a PDF. Error: " + e.getMessage());
                    });
                }
            });
        }
    }
    
    /**
     * Método público para exportar PDF desde el listado de diagnósticos.
     * Este método puede ser llamado desde ClinicaController.
     */
    public void exportarPDFDesdeLista(ModeloDiagnostico diagnostico, ModeloPaciente pacienteParam, ModeloCita cita) {
        if (diagnostico == null || pacienteParam == null) {
            mostrarError("Error", "Datos insuficientes para exportar el diagnóstico");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Informe PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("diagnostico_" + pacienteParam.getNombre().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
        
        // Usar la ventana principal si no hay contenedor disponible
        Stage stage = contenedorPrincipal != null && contenedorPrincipal.getScene() != null ? 
                     (Stage) contenedorPrincipal.getScene().getWindow() : null;
        
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            executorService.submit(() -> {
                try {
                    // Obtener propietario usando protocolo
                    ModeloPropietario propietario = null;
                    if (pacienteParam.getPropietarioId() != null) {
                        try {
                            gestorSocket.enviarPeticion(com.example.pruebamongodbcss.Protocolo.Protocolo.OBTENERPROPIETARIO_POR_ID + 
                                com.example.pruebamongodbcss.Protocolo.Protocolo.SEPARADOR_CODIGO + 
                                pacienteParam.getPropietarioId().toString());
                            
                            int codigo = gestorSocket.getEntrada().readInt();
                            if (codigo == com.example.pruebamongodbcss.Protocolo.Protocolo.OBTENERPROPIETARIO_POR_ID_RESPONSE) {
                                propietario = (ModeloPropietario) gestorSocket.getEntrada().readObject();
                            }
                        } catch (Exception e) {
                            System.err.println("Error al obtener propietario: " + e.getMessage());
                        }
                    }
                    
                    // Parsear diagnósticos UMLS del texto almacenado (simplificado)
                    java.util.List<ModeloDiagnosticoUMLS> diagnosticosUMLS = new java.util.ArrayList<>();
                    if (diagnostico.getDiagnostico() != null && !diagnostico.getDiagnostico().isEmpty()) {
                        // Crear un diagnóstico UMLS básico a partir del texto
                        ModeloDiagnosticoUMLS diagUMLS = new ModeloDiagnosticoUMLS();
                        diagUMLS.setStr(diagnostico.getDiagnostico());
                        diagUMLS.setCui("N/A");
                        diagUMLS.setSab("Diagnóstico clínico");
                        diagnosticosUMLS.add(diagUMLS);
                    }
                    
                    // Generar el PDF
                    GeneradorPDFDiagnostico.generarPDFDiagnostico(
                        file,
                        pacienteParam,
                        propietario,
                        cita,
                        diagnostico,
                        diagnosticosUMLS,
                        diagnostico.getAnamnesis(),
                        diagnostico.getExamenFisico(),
                        diagnostico.getTratamiento(),
                        diagnostico.getObservaciones(),
                        diagnostico.getProximaVisita()
                    );
                    
                    Platform.runLater(() -> {
                        mostrarInformacion("Exportación exitosa", 
                                "El diagnóstico ha sido exportado correctamente como PDF");
                    });
                    
                } catch (DocumentException | IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        mostrarError("Error al exportar", 
                                "No se pudo exportar el diagnóstico a PDF. Error: " + e.getMessage());
                    });
                }
            });
        }
    }
    
    /**
     * Exporta los diagnósticos a un archivo CSV.
     */
    @FXML
    private void exportarCSV() {
        if (paciente == null) {
            mostrarError("Error", "No hay paciente seleccionado para exportar");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        fileChooser.setInitialFileName("diagnostico_" + paciente.getNombre() + ".csv");
        
        // Obtener ventana actual para mostrar el diálogo
        Stage stage = (Stage) contenedorPrincipal.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            executorService.submit(() -> {
                try (FileWriter writer = new FileWriter(file)) {
                    // Escribir encabezados CSV
                    writer.append("Paciente,Fecha,Motivo,Diagnóstico,Tratamiento\n");
                    
                    // Fecha formateada
                    // String fecha = dpFecha.getValue().toString();
                    
                    // Diagnósticos concatenados
                    StringBuilder diagnosticos = new StringBuilder();
                    for (ModeloDiagnosticoUMLS diag : diagnosticosSeleccionados) {
                        if (diagnosticos.length() > 0) {
                            diagnosticos.append("; ");
                        }
                        diagnosticos.append(diag.getStr()).append(" (").append(diag.getCui()).append(")");
                    }
                    
                    // Escapar comas en campos de texto
                    // String motivo = txtMotivo.getText().replace(",", ";");
                    String tratamiento = txtTratamiento.getText().replace(",", ";");
                    
                    // Escribir línea de datos
                    writer.append(paciente.getNombre()).append(",")
                          // .append(fecha).append(",")
                          // .append(motivo).append(",")
                          .append(diagnosticos.toString()).append(",")
                          .append(tratamiento).append("\n");
                    
                    Platform.runLater(() -> {
                        mostrarInformacion("Exportación exitosa", 
                                "El diagnóstico ha sido exportado correctamente a CSV");
                    });
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        mostrarError("Error al exportar", 
                                "No se pudo exportar el diagnóstico a CSV. Error: " + e.getMessage());
                    });
                }
            });
        }
    }
    
    /**
     * Establece un callback para cuando se guarde el diagnóstico.
     * @param callback Callback a ejecutar después de guardar
     */
    public void setOnGuardarCallback(Runnable callback) {
        this.onGuardarCallback = callback;
        
        // En caso de que este método sea llamado después de initialize(),
        // no sobreescribimos la acción del botón, solo actualizamos el callback
        // que será llamado después de guardar
    }
    
    /**
     * Establece un callback para cuando se cancele el diagnóstico.
     * @param callback Callback a ejecutar al cancelar
     */
    public void setOnCancelarCallback(Runnable callback) {
        this.onCancelarCallback = callback;
        
        // En caso de que este método sea llamado después de initialize(),
        // no sobreescribimos la acción del botón, solo actualizamos el callback
        // que será llamado después de cancelar
    }
    
    /**
     * Muestra un diálogo de error.
     */
    private void mostrarError(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    /**
     * Muestra un diálogo de información.
     */
    private void mostrarInformacion(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    /**
     * Limpia los recursos cuando se cierra la ventana.
     */
    public void onClose() {
        scheduledExecutorService.shutdown();
        executorService.shutdown();
    }
    
    /**
     * Cancela la creación/edición del diagnóstico.
     */
    @FXML
    public void cancelar() {
        if (onCancelarCallback != null) {
            onCancelarCallback.run();
        }
    }
} 