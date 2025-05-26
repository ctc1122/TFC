package com.example.pruebamongodbcss.Modulos.Clinica.Citas;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        citasObservable.clear();
        
        if (dpFechaInicio.getValue() != null && dpFechaFin.getValue() != null) {
            List<ModeloCita> citas = buscarCitasPorRangoFechas(
                    dpFechaInicio.getValue(), 
                    dpFechaFin.getValue());
            
            // Aplicar filtro por búsqueda de texto si existe
            String textoBusqueda = txtBuscarCita.getText().trim().toLowerCase();
            if (!textoBusqueda.isEmpty()) {
                citas.removeIf(cita -> 
                    !cita.getNombrePaciente().toLowerCase().contains(textoBusqueda) && 
                    !cita.getNombreVeterinario().toLowerCase().contains(textoBusqueda) &&
                    !cita.getMotivo().toLowerCase().contains(textoBusqueda));
            }
            
            // Aplicar filtro por estado si está seleccionado
            String estadoSeleccionado = cmbEstadoFiltro.getSelectionModel().getSelectedItem();
            if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos")) {
                citas.removeIf(cita -> !cita.getEstadoAsString().equals(estadoSeleccionado));
            }
            
            citasObservable.addAll(citas);
        }
    }
    
    /**
     * Métodos privados para comunicación con el servidor
     */
    private List<ModeloCita> buscarCitasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            gestorSocket.enviarPeticion(Protocolo.BUSCAR_CITAS_POR_RANGO_FECHAS + Protocolo.SEPARADOR_CODIGO);
            gestorSocket.getSalida().writeObject(fechaInicio);
            gestorSocket.getSalida().writeObject(fechaFin);
            gestorSocket.getSalida().flush();
            
            int codigo = gestorSocket.getEntrada().readInt();
            if (codigo == Protocolo.BUSCAR_CITAS_POR_RANGO_FECHAS_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloCita> citas = (List<ModeloCita>) gestorSocket.getEntrada().readObject();
                return citas;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al buscar citas", 
                "No se pudieron cargar las citas: " + e.getMessage());
        }
        return new java.util.ArrayList<>();
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
            
            // Configurar callback para actualizar después de guardar
            controller.setCitaGuardadaCallback(() -> cargarCitas());
            
            Stage stage = new Stage();
            stage.setTitle(cita == null ? "Nueva Cita" : "Editar Cita");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir formulario", 
                "Ha ocurrido un error al intentar abrir el formulario de cita: " + e.getMessage());
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
} 