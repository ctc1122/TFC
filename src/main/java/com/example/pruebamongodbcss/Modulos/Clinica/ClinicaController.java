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
    
    // Servicio clínico
    private ServicioClinica servicioClinica;
    
    // Listas observables para las tablas
    private ObservableList<ModeloPaciente> pacientesObservable;
    private ObservableList<ModeloPropietario> propietariosObservable;
    private ObservableList<ModeloDiagnostico> diagnosticosObservable;
    
    // Formato de fecha
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

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
    
    private void buscarDiagnosticos() {
        diagnosticosObservable.clear();
        
        if (dpFechaInicio.getValue() != null && dpFechaFin.getValue() != null) {
            Date fechaInicio = Date.from(dpFechaInicio.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date fechaFin = Date.from(dpFechaFin.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<ModeloDiagnostico> diagnosticos = servicioClinica.buscarDiagnosticosPorFecha(fechaInicio, fechaFin);
            diagnosticosObservable.addAll(diagnosticos);
        }
    }
    
    // ********** EVENTOS DE BÚSQUEDA **********
    
    private void configurarEventosBusqueda() {
        // Búsqueda de pacientes
        txtBuscarPaciente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                cargarPacientes();
            } else {
                pacientesObservable.clear();
                List<ModeloPaciente> pacientes = servicioClinica.buscarPacientesPorNombre(newValue);
                pacientesObservable.addAll(pacientes);
            }
        });
        
        // Búsqueda de propietarios
        txtBuscarPropietario.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                cargarPropietarios();
            } else {
                propietariosObservable.clear();
                List<ModeloPropietario> propietarios = servicioClinica.buscarPropietariosPorNombre(newValue);
                propietariosObservable.addAll(propietarios);
            }
        });
        
        // Configurar botón de búsqueda de diagnósticos
        btnBuscarDiagnostico.setOnAction(event -> buscarDiagnosticos());
    }
    
    // ********** ACCIONES DE BOTONES **********
    
    @FXML
    private void onNuevoPaciente(ActionEvent event) {
        // Todo: Implementar la creación de un nuevo paciente
        // Esto se implementará en un formulario separado
        mostrarMensaje("Información", "Funcionalidad pendiente de implementar", 
                "La creación de nuevos pacientes se implementará en la siguiente fase.");
    }
    
    @FXML
    private void onEditarPaciente(ActionEvent event) {
        ModeloPaciente paciente = tablaPacientes.getSelectionModel().getSelectedItem();
        if (paciente != null) {
            abrirDetallesPaciente(paciente);
        } else {
            mostrarAlerta("Error", "Seleccione un paciente", 
                    "Debe seleccionar un paciente para editar sus datos.");
        }
    }
    
    @FXML
    private void onEliminarPaciente(ActionEvent event) {
        ModeloPaciente paciente = tablaPacientes.getSelectionModel().getSelectedItem();
        if (paciente != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Eliminar paciente", 
                    "¿Está seguro de eliminar al paciente " + paciente.getNombre() + "?",
                    "Esta acción no puede deshacerse y eliminará todo el historial clínico asociado.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicioClinica.eliminarPaciente(paciente.getId());
                if (eliminado) {
                    mostrarMensaje("Éxito", "Paciente eliminado", 
                            "El paciente y su historial clínico se han eliminado correctamente.");
                    cargarPacientes();
                } else {
                    mostrarAlerta("Error", "Error al eliminar", 
                            "No se pudo eliminar el paciente. Inténtelo nuevamente.");
                }
            }
        } else {
            mostrarAlerta("Error", "Seleccione un paciente", 
                    "Debe seleccionar un paciente para eliminarlo.");
        }
    }
    
    @FXML
    private void onVerHistorial(ActionEvent event) {
        ModeloPaciente paciente = tablaPacientes.getSelectionModel().getSelectedItem();
        if (paciente != null) {
            // Todo: Implementar la visualización del historial clínico
            mostrarMensaje("Información", "Funcionalidad pendiente de implementar", 
                    "La visualización del historial clínico completo se implementará en la siguiente fase.");
        } else {
            mostrarAlerta("Error", "Seleccione un paciente", 
                    "Debe seleccionar un paciente para ver su historial clínico.");
        }
    }
    
    @FXML
    private void onNuevoPropietario(ActionEvent event) {
        // Todo: Implementar la creación de un nuevo propietario
        mostrarMensaje("Información", "Funcionalidad pendiente de implementar", 
                "La creación de nuevos propietarios se implementará en la siguiente fase.");
    }
    
    @FXML
    private void onEditarPropietario(ActionEvent event) {
        ModeloPropietario propietario = tablaPropietarios.getSelectionModel().getSelectedItem();
        if (propietario != null) {
            abrirDetallesPropietario(propietario);
        } else {
            mostrarAlerta("Error", "Seleccione un propietario", 
                    "Debe seleccionar un propietario para editar sus datos.");
        }
    }
    
    @FXML
    private void onEliminarPropietario(ActionEvent event) {
        ModeloPropietario propietario = tablaPropietarios.getSelectionModel().getSelectedItem();
        if (propietario != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Eliminar propietario", 
                    "¿Está seguro de eliminar al propietario " + propietario.getNombreCompleto() + "?",
                    "Esta acción no puede deshacerse.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicioClinica.eliminarPropietario(propietario.getId());
                if (eliminado) {
                    mostrarMensaje("Éxito", "Propietario eliminado", 
                            "El propietario se ha eliminado correctamente.");
                    cargarPropietarios();
                } else {
                    mostrarAlerta("Error", "Error al eliminar", 
                            "No se pudo eliminar el propietario. Inténtelo nuevamente.");
                }
            }
        } else {
            mostrarAlerta("Error", "Seleccione un propietario", 
                    "Debe seleccionar un propietario para eliminarlo.");
        }
    }
    
    @FXML
    private void onVerMascotas(ActionEvent event) {
        ModeloPropietario propietario = tablaPropietarios.getSelectionModel().getSelectedItem();
        if (propietario != null) {
            // Todo: Implementar la visualización de mascotas del propietario
            mostrarMensaje("Información", "Funcionalidad pendiente de implementar", 
                    "La visualización de mascotas por propietario se implementará en la siguiente fase.");
        } else {
            mostrarAlerta("Error", "Seleccione un propietario", 
                    "Debe seleccionar un propietario para ver sus mascotas.");
        }
    }
    
    @FXML
    private void buscarDiagnosticos(ActionEvent event) {
        buscarDiagnosticos();
    }
    
    @FXML
    private void onNuevoDiagnostico(ActionEvent event) {
        // Todo: Implementar la creación de un nuevo diagnóstico
        mostrarMensaje("Información", "Funcionalidad pendiente de implementar", 
                "La creación de nuevos diagnósticos se implementará en la siguiente fase.");
    }
    
    @FXML
    private void onVerDiagnostico(ActionEvent event) {
        ModeloDiagnostico diagnostico = tablaDiagnosticos.getSelectionModel().getSelectedItem();
        if (diagnostico != null) {
            abrirDetallesDiagnostico(diagnostico);
        } else {
            mostrarAlerta("Error", "Seleccione un diagnóstico", 
                    "Debe seleccionar un diagnóstico para ver sus detalles.");
        }
    }
    
    @FXML
    private void onEliminarDiagnostico(ActionEvent event) {
        ModeloDiagnostico diagnostico = tablaDiagnosticos.getSelectionModel().getSelectedItem();
        if (diagnostico != null) {
            Optional<ButtonType> resultado = mostrarConfirmacion("Eliminar diagnóstico", 
                    "¿Está seguro de eliminar el diagnóstico seleccionado?",
                    "Esta acción no puede deshacerse.");
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean eliminado = servicioClinica.eliminarDiagnostico(diagnostico.getId());
                if (eliminado) {
                    mostrarMensaje("Éxito", "Diagnóstico eliminado", 
                            "El diagnóstico se ha eliminado correctamente.");
                    buscarDiagnosticos();
                } else {
                    mostrarAlerta("Error", "Error al eliminar", 
                            "No se pudo eliminar el diagnóstico. Inténtelo nuevamente.");
                }
            }
        } else {
            mostrarAlerta("Error", "Seleccione un diagnóstico", 
                    "Debe seleccionar un diagnóstico para eliminarlo.");
        }
    }
    
    // ********** FORMULARIOS DE DETALLE **********
    
    private void abrirDetallesPaciente(ModeloPaciente paciente) {
        // Todo: Implementar la apertura del formulario de detalles de paciente
        mostrarMensaje("Información", "Funcionalidad pendiente de implementar", 
                "El formulario de detalles de paciente se implementará en la siguiente fase.");
    }
    
    private void abrirDetallesPropietario(ModeloPropietario propietario) {
        // Todo: Implementar la apertura del formulario de detalles de propietario
        mostrarMensaje("Información", "Funcionalidad pendiente de implementar", 
                "El formulario de detalles de propietario se implementará en la siguiente fase.");
    }
    
    private void abrirDetallesDiagnostico(ModeloDiagnostico diagnostico) {
        // Todo: Implementar la apertura del formulario de detalles de diagnóstico
        mostrarMensaje("Información", "Funcionalidad pendiente de implementar", 
                "El formulario de detalles de diagnóstico se implementará en la siguiente fase.");
    }
    
    // ********** UTILIDADES **********
    
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