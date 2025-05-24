package com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Modulos.Clinica.ModeloDiagnostico;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloPaciente;
import com.example.pruebamongodbcss.Modulos.Clinica.ModeloCita;
import com.example.pruebamongodbcss.Modulos.Clinica.ServicioClinica;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
import javafx.beans.property.SimpleStringProperty;
import java.time.format.DateTimeFormatter;

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
    
    // Filtrado por paciente
    @FXML private ComboBox<ModeloPaciente> cmbPacientes;
    @FXML private Button btnFiltrarPaciente;
    @FXML private Button btnLimpiarFiltro;
    
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
    
    // Servicios
    private final ServicioClinica servicioClinica = new ServicioClinica();
    private final ServicioDiagnosticoUMLS servicioDiagnosticoUMLS = new ServicioDiagnosticoUMLS();
    
    // Lista observable para almacenar los diagnósticos seleccionados
    private ObservableList<ModeloDiagnosticoUMLS> diagnosticosSeleccionados = FXCollections.observableArrayList();
    
    // Executor para búsqueda en tiempo real
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private Runnable onGuardarCallback;
    private Runnable onCancelarCallback;
    
    @FXML private TableView<ModeloCita> tblConsultas;
    @FXML private TableColumn<ModeloCita, String> colFecha;
    @FXML private TableColumn<ModeloCita, String> colHora;
    @FXML private TableColumn<ModeloCita, String> colMotivo;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurar las partes esenciales de la interfaz primero
        configurarTabla();
        
        // Inicializar fechas inmediatamente
        // dpFecha.setValue(LocalDate.now());
        
        // Configurar eventos de la interfaz de usuario
        btnAgregarDiagnostico.setOnAction(event -> agregarDiagnostico());
        btnQuitarDiagnostico.setOnAction(event -> quitarDiagnostico());
        btnGuardar.setOnAction(event -> guardarDiagnostico());
        btnCancelar.setOnAction(event -> cancelar());
        
        // Configurar lista de diagnósticos seleccionados
        lstDiagnosticosSeleccionados.setItems(diagnosticosSeleccionados);
        
        // Configurar selección de diagnóstico
        tblDiagnosticos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            diagnosticoSeleccionado = newVal;
        });
        
        // Inicializar lista vacía para mostrar la interfaz inmediatamente
        tblDiagnosticos.setItems(FXCollections.observableArrayList());
        
        // Retrasar las operaciones más pesadas
        Platform.runLater(() -> {
            // Configuraciones que no son críticas para la visualización inicial
            configurarBusquedaEnTiempoReal();
            configurarComboBoxPacientes();
            
            // Carga de datos en segundo plano
            executorService.submit(() -> {
                // Cargar pacientes
                List<ModeloPaciente> listaPacientes = servicioClinica.obtenerTodosPacientes();
                Platform.runLater(() -> {
                    cmbPacientes.setItems(FXCollections.observableArrayList(listaPacientes));
                });
                
                // Cargar diagnósticos limitados (solo 50 para que sea más rápido)
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

        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

        colFecha.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFechaHora().format(formatoFecha)));
        colHora.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFechaHora().format(formatoHora)));
        colMotivo.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getMotivo()));
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
        // Configurar botones para agregar/quitar diagnósticos
        btnAgregarDiagnostico.setOnAction(event -> agregarDiagnostico());
        btnQuitarDiagnostico.setOnAction(event -> quitarDiagnostico());
        
        // Configurar selección en la lista de diagnósticos seleccionados
        lstDiagnosticosSeleccionados.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                // Puedes hacer algo cuando se selecciona un diagnóstico de la lista
            });
    }
    
    /**
     * Configura el ComboBox de pacientes.
     */
    private void configurarComboBoxPacientes() {
        // Personalizar la forma en que se muestran los pacientes en el ComboBox
        cmbPacientes.setCellFactory(lv -> {
            return new ListCell<ModeloPaciente>() {
                @Override
                protected void updateItem(ModeloPaciente item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNombre() + " (" + item.getEspecie() + " - " + item.getRaza() + ")");
                    }
                }
            };
        });
        
        cmbPacientes.setButtonCell(new javafx.scene.control.ListCell<ModeloPaciente>() {
            @Override
            protected void updateItem(ModeloPaciente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre() + " (" + item.getEspecie() + " - " + item.getRaza() + ")");
                }
            }
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
     * Filtra los diagnósticos por el paciente seleccionado.
     */
    @FXML
    private void filtrarPorPaciente() {
        ModeloPaciente pacienteSeleccionado = cmbPacientes.getSelectionModel().getSelectedItem();
        if (pacienteSeleccionado != null) {
            this.paciente = pacienteSeleccionado;
            actualizarDatosPaciente();
            
            // Cargar diagnósticos del paciente seleccionado
            executorService.submit(() -> {
                List<ModeloDiagnostico> diagnosticosDelPaciente = 
                    servicioClinica.buscarDiagnosticosPorPaciente(pacienteSeleccionado.getId());
                
                Platform.runLater(() -> {
                    if (diagnosticosDelPaciente.isEmpty()) {
                        mostrarInformacion("Información", 
                            "No hay diagnósticos registrados para este paciente. Puede crear uno nuevo.");
                    } else {
                        // Aquí puedes mostrar los diagnósticos en alguna vista adicional o en una tabla
                        // Por ahora, solo mostramos un mensaje informativo
                        mostrarInformacion("Diagnósticos encontrados", 
                            "Se encontraron " + diagnosticosDelPaciente.size() + 
                            " diagnósticos para el paciente " + pacienteSeleccionado.getNombre());
                    }
                });
            });
        }
    }
    
    /**
     * Limpia el filtro de paciente y muestra todos los diagnósticos.
     */
    @FXML
    private void limpiarFiltro() {
        cmbPacientes.getSelectionModel().clearSelection();
        cargarDatosDiagnosticosIniciales();
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
    public void setPaciente(ModeloPaciente paciente) {
        this.paciente = paciente;
        actualizarDatosPaciente();
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
        
        // if (txtMotivo.getText().isBlank()) {
        //     mostrarError("Datos incompletos", "El motivo de la consulta es obligatorio");
        //     return;
        // }
        
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
        // diagnostico.setFecha(Date.from(dpFecha.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        // diagnostico.setMotivo(txtMotivo.getText());
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
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        
        // Si no hay veterinario asignado, usar uno genérico (esto debe adaptarse según tu sistema)
        if (diagnostico.getVeterinario() == null || diagnostico.getVeterinario().isBlank()) {
            diagnostico.setVeterinario("Dr. Veterinario");
        }
        
        // Guardar diagnóstico
        ObjectId id = servicioClinica.guardarDiagnostico(diagnostico);
        if (id != null) {
            diagnostico.setId(id);
            mostrarInformacion("Diagnóstico guardado", 
                    "El diagnóstico ha sido guardado correctamente");
            
            // Llamar al callback si está configurado
            if (onGuardarCallback != null) {
                onGuardarCallback.run();
            }
        } else {
            mostrarError("Error al guardar", 
                    "No se pudo guardar el diagnóstico. Intente nuevamente");
        }
    }
    
    /**
     * Exporta los diagnósticos a un archivo en formato texto (como alternativa a PDF).
     */
    @FXML
    private void exportarPDF() {
        if (paciente == null) {
            mostrarError("Error", "No hay paciente seleccionado para exportar");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Informe");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos de texto", "*.txt")
        );
        fileChooser.setInitialFileName("diagnostico_" + paciente.getNombre() + ".txt");
        
        // Obtener ventana actual para mostrar el diálogo
        Stage stage = (Stage) contenedorPrincipal.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            executorService.submit(() -> {
                try (FileWriter writer = new FileWriter(file)) {
                    // Título
                    writer.write("==========================================\n");
                    writer.write("      INFORME DE DIAGNÓSTICO VETERINARIO\n");
                    writer.write("==========================================\n\n");
                    
                    // Datos del paciente
                    writer.write("PACIENTE: " + paciente.getNombre() + "\n");
                    writer.write("Especie: " + paciente.getEspecie() + "\n");
                    writer.write("Raza: " + paciente.getRaza() + "\n");
                    writer.write("Sexo: " + paciente.getSexo() + "\n");
                    writer.write("Peso: " + paciente.getPeso() + " kg\n\n");
                    
                    // Fecha
                    // LocalDate fecha = dpFecha.getValue();
                    // writer.write("Fecha: " + fecha + "\n\n");
                    
                    // Motivo
                    // writer.write("MOTIVO DE CONSULTA:\n");
                    // writer.write(txtMotivo.getText() + "\n\n");
                    
                    // Anamnesis
                    writer.write("ANAMNESIS:\n");
                    writer.write(txtAnamnesis.getText() + "\n\n");
                    
                    // Examen físico
                    writer.write("EXAMEN FÍSICO:\n");
                    writer.write(txtExamenFisico.getText() + "\n\n");
                    
                    // Diagnósticos
                    writer.write("DIAGNÓSTICOS:\n");
                    for (ModeloDiagnosticoUMLS diag : diagnosticosSeleccionados) {
                        writer.write("- " + diag.getStr() + " (Código: " + diag.getCui() + 
                                ", Fuente: " + diag.getSab() + ")\n");
                    }
                    writer.write("\n");
                    
                    // Tratamiento
                    writer.write("TRATAMIENTO:\n");
                    writer.write(txtTratamiento.getText() + "\n\n");
                    
                    // Observaciones
                    writer.write("OBSERVACIONES:\n");
                    writer.write(txtObservaciones.getText() + "\n\n");
                    
                    // Próxima visita
                    if (dpProximaVisita.getValue() != null) {
                        writer.write("Próxima visita programada: " + dpProximaVisita.getValue() + "\n\n");
                    }
                    
                    // Firma
                    writer.write("\n\n");
                    writer.write("Firma: _______________________________\n");
                    writer.write("       Dr. Veterinario\n");
                    
                    Platform.runLater(() -> {
                        mostrarInformacion("Exportación exitosa", 
                                "El diagnóstico ha sido exportado correctamente como archivo de texto");
                    });
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        mostrarError("Error al exportar", 
                                "No se pudo exportar el diagnóstico. Error: " + e.getMessage());
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