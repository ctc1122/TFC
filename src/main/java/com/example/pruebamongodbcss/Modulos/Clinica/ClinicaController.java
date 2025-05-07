package com.example.pruebamongodbcss.Modulos.Clinica;

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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador principal para la gestión clínica veterinaria.
 */
public class ClinicaController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private TabPane tabPane;
    
    // Tab de Pacientes
    @FXML private Tab tabPacientes;
    @FXML private TableView<ModeloPaciente> tablaPacientes;
    @FXML private TableColumn<ModeloPaciente, String> colNombrePaciente;
    @FXML private TableColumn<ModeloPaciente, String> colEspecie;
    @FXML private TableColumn<ModeloPaciente, String> colRaza;
    @FXML private TableColumn<ModeloPaciente, String> colPropietario;
    @FXML private TextField txtBuscarPaciente;
    @FXML private Button btnNuevoPaciente;
    @FXML private Button btnEditarPaciente;
    @FXML private Button btnEliminarPaciente;
    @FXML private Button btnVerHistorial;
    
    // Tab de Propietarios
    @FXML private Tab tabPropietarios;
    @FXML private TableView<ModeloPropietario> tablaPropietarios;
    @FXML private TableColumn<ModeloPropietario, String> colNombrePropietario;
    @FXML private TableColumn<ModeloPropietario, String> colDNI;
    @FXML private TableColumn<ModeloPropietario, String> colTelefono;
    @FXML private TableColumn<ModeloPropietario, String> colEmail;
    @FXML private TextField txtBuscarPropietario;
    @FXML private Button btnNuevoPropietario;
    @FXML private Button btnEditarPropietario;
    @FXML private Button btnEliminarPropietario;
    @FXML private Button btnVerMascotas;
    
    // Tab de Diagnósticos
    @FXML private Tab tabDiagnosticos;
    @FXML private TableView<ModeloDiagnostico> tablaDiagnosticos;
    @FXML private TableColumn<ModeloDiagnostico, String> colFechaDiagnostico;
    @FXML private TableColumn<ModeloDiagnostico, String> colPacienteDiagnostico;
    @FXML private TableColumn<ModeloDiagnostico, String> colMotivo;
    @FXML private TableColumn<ModeloDiagnostico, String> colVeterinario;
    @FXML private MFXDatePicker dpFechaInicio;
    @FXML private MFXDatePicker dpFechaFin;
    @FXML private TextField txtBuscarDiagnostico;
    @FXML private Button btnBuscarDiagnostico;
    @FXML private Button btnNuevoDiagnostico;
    @FXML private Button btnVerDiagnostico;
    @FXML private Button btnEliminarDiagnostico;
    
    // Tab de Citas
    @FXML private Tab tabCitas;
    @FXML private BorderPane citasContainer;
    
    // Servicio clínico
    private ServicioClinica servicioClinica;
    
    // Listas observables para las tablas
    private ObservableList<ModeloPaciente> pacientesObservable;
    private ObservableList<ModeloPropietario> propietariosObservable;
    private ObservableList<ModeloDiagnostico> diagnosticosObservable;
    
    // Formato de fecha
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    
    // Controlador de citas
    private CitasController citasController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar el servicio clínico
        servicioClinica = new ServicioClinica();
        
        // Configurar las listas observables
        pacientesObservable = FXCollections.observableArrayList();
        propietariosObservable = FXCollections.observableArrayList();
        diagnosticosObservable = FXCollections.observableArrayList();
        
        // Configurar las tablas
        configurarTablaPacientes();
        configurarTablaPropietarios();
        configurarTablaDiagnosticos();
        
        // Cargar datos iniciales
        cargarPacientes();
        cargarPropietarios();
        
        // Inicializar fechas de búsqueda de diagnósticos
        LocalDate hoy = LocalDate.now();
        dpFechaFin.setValue(hoy);
        dpFechaInicio.setValue(hoy.minusDays(30));
        
        // Buscar diagnósticos iniciales
        buscarDiagnosticos();
        
        // Configurar eventos de búsqueda
        configurarEventosBusqueda();
        
        // Configurar cambio de pestañas
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabCitas) {
                cargarVistaCitas();
            }
        });
    }
    
    /**
     * Carga la vista de citas
     */
    private void cargarVistaCitas() {
        try {
            // Solo cargar si no está ya cargada
            if (citasContainer.getCenter() == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/citas-view.fxml"));
                Parent root = loader.load();
                citasController = loader.getController();
                
                citasContainer.setCenter(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar vista de citas", 
                    "Ha ocurrido un error al intentar cargar la vista de citas: " + e.getMessage());
        }
    }
    
    // ********** CONFIGURACIÓN DE TABLAS **********
    
    private void configurarTablaPacientes() {
        colNombrePaciente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colEspecie.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEspecie()));
        colRaza.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRaza()));
        colPropietario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombrePropietario()));
        
        tablaPacientes.setItems(pacientesObservable);
        
        // Manejar doble clic en un paciente
        tablaPacientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaPacientes.getSelectionModel().getSelectedItem() != null) {
                abrirDetallesPaciente(tablaPacientes.getSelectionModel().getSelectedItem());
            }
        });
    }
    
    private void configurarTablaPropietarios() {
        colNombrePropietario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colDNI.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDni()));
        colTelefono.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefono()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        
        tablaPropietarios.setItems(propietariosObservable);
        
        // Manejar doble clic en un propietario
        tablaPropietarios.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaPropietarios.getSelectionModel().getSelectedItem() != null) {
                abrirDetallesPropietario(tablaPropietarios.getSelectionModel().getSelectedItem());
            }
        });
    }
    
    private void configurarTablaDiagnosticos() {
        colFechaDiagnostico.setCellValueFactory(data -> {
            Date fecha = data.getValue().getFecha();
            return new SimpleStringProperty(fecha != null ? formatoFecha.format(fecha) : "");
        });
        colPacienteDiagnostico.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombrePaciente()));
        colMotivo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMotivo()));
        colVeterinario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVeterinario()));
        
        tablaDiagnosticos.setItems(diagnosticosObservable);
        
        // Manejar doble clic en un diagnóstico
        tablaDiagnosticos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaDiagnosticos.getSelectionModel().getSelectedItem() != null) {
                abrirDetallesDiagnostico(tablaDiagnosticos.getSelectionModel().getSelectedItem());
            }
        });
    }
    
    // ********** CARGA DE DATOS **********
    
    private void cargarPacientes() {
        pacientesObservable.clear();
        List<ModeloPaciente> pacientes = servicioClinica.obtenerTodosPacientes();
        pacientesObservable.addAll(pacientes);
    }
    
    private void cargarPropietarios() {
        propietariosObservable.clear();
        List<ModeloPropietario> propietarios = servicioClinica.obtenerTodosPropietarios();
        propietariosObservable.addAll(propietarios);
    }
    
    @FXML
    public void buscarDiagnosticos() {
        diagnosticosObservable.clear();
        
        if (dpFechaInicio.getValue() != null && dpFechaFin.getValue() != null) {
            Date fechaInicio = Date.from(dpFechaInicio.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(dpFechaFin.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<ModeloDiagnostico> diagnosticos = servicioClinica.buscarDiagnosticosPorFecha(fechaInicio, fechaFin);
            diagnosticosObservable.addAll(diagnosticos);
        }
    }
    
    @FXML
    public void buscarDiagnosticos(ActionEvent event) {
        buscarDiagnosticos();
    }
    
    // ********** EVENTOS DE BÚSQUEDA **********
    
    private void configurarEventosBusqueda() {
        // Filtro en tiempo real para pacientes
        txtBuscarPaciente.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cargarPacientes();
            } else {
                buscarPacientesPorNombre(newVal);
            }
        });
        
        // Filtro en tiempo real para propietarios
        txtBuscarPropietario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cargarPropietarios();
            } else {
                buscarPropietariosPorNombre(newVal);
            }
        });
    }
    
    private void buscarPacientesPorNombre(String nombre) {
        pacientesObservable.clear();
        List<ModeloPaciente> pacientes = servicioClinica.buscarPacientesPorNombre(nombre);
        pacientesObservable.addAll(pacientes);
    }
    
    private void buscarPropietariosPorNombre(String nombre) {
        propietariosObservable.clear();
        List<ModeloPropietario> propietarios = servicioClinica.buscarPropietariosPorNombre(nombre);
        propietariosObservable.addAll(propietarios);
    }
    
    // ********** ACCIONES DE PACIENTES **********
    
    @FXML
    private void onNuevoPaciente(ActionEvent event) {
        // Implementar apertura de formulario de nuevo paciente
    }
    
    @FXML
    private void onEditarPaciente(ActionEvent event) {
        ModeloPaciente paciente = tablaPacientes.getSelectionModel().getSelectedItem();
        if (paciente != null) {
            abrirDetallesPaciente(paciente);
        } else {
            mostrarAlerta("Selección requerida", "No hay paciente seleccionado", 
                    "Por favor, seleccione un paciente para editar.");
        }
    }
    
    @FXML
    private void onEliminarPaciente(ActionEvent event) {
        ModeloPaciente paciente = tablaPacientes.getSelectionModel().getSelectedItem();
        if (paciente != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                    "¿Está seguro que desea eliminar este paciente?", 
                    "Esta acción eliminará el paciente y todos sus diagnósticos asociados.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicioClinica.eliminarPaciente(paciente.getId());
                if (eliminado) {
                    cargarPacientes();
                    mostrarMensaje("Paciente eliminado", "El paciente ha sido eliminado", 
                            "El paciente y sus diagnósticos asociados han sido eliminados exitosamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el paciente", 
                            "Ha ocurrido un error al intentar eliminar el paciente.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay paciente seleccionado", 
                    "Por favor, seleccione un paciente para eliminar.");
        }
    }
    
    @FXML
    private void onVerHistorial(ActionEvent event) {
        ModeloPaciente paciente = tablaPacientes.getSelectionModel().getSelectedItem();
        if (paciente != null) {
            // Navegar a la pestaña de diagnósticos y filtrar por este paciente
            tabPane.getSelectionModel().select(tabDiagnosticos);
            
            // Buscar los diagnósticos del paciente
            diagnosticosObservable.clear();
            List<ModeloDiagnostico> diagnosticos = servicioClinica.buscarDiagnosticosPorPaciente(paciente.getId());
            diagnosticosObservable.addAll(diagnosticos);
            
            // Si no hay diagnósticos, mostrar mensaje
            if (diagnosticos.isEmpty()) {
                mostrarMensaje("Sin diagnósticos", "No hay diagnósticos para este paciente", 
                        "El paciente " + paciente.getNombre() + " no tiene diagnósticos registrados.");
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay paciente seleccionado", 
                    "Por favor, seleccione un paciente para ver su historial.");
        }
    }
    
    // ********** ACCIONES DE PROPIETARIOS **********
    
    @FXML
    private void onNuevoPropietario(ActionEvent event) {
        // Implementar apertura de formulario de nuevo propietario
    }
    
    @FXML
    private void onEditarPropietario(ActionEvent event) {
        ModeloPropietario propietario = tablaPropietarios.getSelectionModel().getSelectedItem();
        if (propietario != null) {
            abrirDetallesPropietario(propietario);
        } else {
            mostrarAlerta("Selección requerida", "No hay propietario seleccionado", 
                    "Por favor, seleccione un propietario para editar.");
        }
    }
    
    @FXML
    private void onEliminarPropietario(ActionEvent event) {
        ModeloPropietario propietario = tablaPropietarios.getSelectionModel().getSelectedItem();
        if (propietario != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                    "¿Está seguro que desea eliminar este propietario?", 
                    "Esta acción eliminará el propietario. No se podrá eliminar si tiene mascotas asociadas.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicioClinica.eliminarPropietario(propietario.getId());
                if (eliminado) {
                    cargarPropietarios();
                    mostrarMensaje("Propietario eliminado", "El propietario ha sido eliminado", 
                            "El propietario ha sido eliminado exitosamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el propietario", 
                            "No se puede eliminar el propietario porque tiene mascotas asociadas.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay propietario seleccionado", 
                    "Por favor, seleccione un propietario para eliminar.");
        }
    }
    
    @FXML
    private void onVerMascotas(ActionEvent event) {
        ModeloPropietario propietario = tablaPropietarios.getSelectionModel().getSelectedItem();
        if (propietario != null) {
            // Navegar a la pestaña de pacientes y filtrar por este propietario
            tabPane.getSelectionModel().select(tabPacientes);
            
            // Buscar los pacientes de este propietario
            pacientesObservable.clear();
            List<ModeloPaciente> mascotas = servicioClinica.buscarPacientesPorPropietario(propietario.getId());
            pacientesObservable.addAll(mascotas);
            
            // Si no hay mascotas, mostrar mensaje
            if (mascotas.isEmpty()) {
                mostrarMensaje("Sin mascotas", "No hay mascotas para este propietario", 
                        "El propietario " + propietario.getNombreCompleto() + " no tiene mascotas registradas.");
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay propietario seleccionado", 
                    "Por favor, seleccione un propietario para ver sus mascotas.");
        }
    }
    
    // ********** ACCIONES DE DIAGNÓSTICOS **********
    
    @FXML
    private void onNuevoDiagnostico(ActionEvent event) {
        // Implementar apertura de formulario de nuevo diagnóstico
    }
    
    @FXML
    private void onVerDiagnostico(ActionEvent event) {
        ModeloDiagnostico diagnostico = tablaDiagnosticos.getSelectionModel().getSelectedItem();
        if (diagnostico != null) {
            abrirDetallesDiagnostico(diagnostico);
        } else {
            mostrarAlerta("Selección requerida", "No hay diagnóstico seleccionado", 
                    "Por favor, seleccione un diagnóstico para ver detalles.");
        }
    }
    
    @FXML
    private void onEliminarDiagnostico(ActionEvent event) {
        ModeloDiagnostico diagnostico = tablaDiagnosticos.getSelectionModel().getSelectedItem();
        if (diagnostico != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Confirmar eliminación", 
                    "¿Está seguro que desea eliminar este diagnóstico?", 
                    "Esta acción eliminará el diagnóstico. Esta operación no se puede deshacer.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicioClinica.eliminarDiagnostico(diagnostico.getId());
                if (eliminado) {
                    buscarDiagnosticos();
                    mostrarMensaje("Diagnóstico eliminado", "El diagnóstico ha sido eliminado", 
                            "El diagnóstico ha sido eliminado exitosamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el diagnóstico", 
                            "Ha ocurrido un error al intentar eliminar el diagnóstico.");
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "No hay diagnóstico seleccionado", 
                    "Por favor, seleccione un diagnóstico para eliminar.");
        }
    }
    
    // ********** APERTURA DE DETALLES **********
    
    private void abrirDetallesPaciente(ModeloPaciente paciente) {
        // Implementar apertura de detalles de paciente
    }
    
    private void abrirDetallesPropietario(ModeloPropietario propietario) {
        // Implementar apertura de detalles de propietario
    }
    
    private void abrirDetallesDiagnostico(ModeloDiagnostico diagnostico) {
        // Implementar apertura de detalles de diagnóstico
    }
    
    // ********** MÉTODOS DE UTILIDAD **********
    
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
} 