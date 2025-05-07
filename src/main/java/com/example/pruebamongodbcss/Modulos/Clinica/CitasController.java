package com.example.pruebamongodbcss.Modulos.Clinica;

import com.example.pruebamongodbcss.Data.EstadoCita;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;

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
    
    // Tab de Calendario
    @FXML private Tab tabCalendario;
    @FXML private GridPane gridCalendario;
    @FXML private Label lblMesActual;
    
    // Servicio de clínica
    private ServicioClinica servicio;
    
    // Lista observable para la tabla de citas
    private ObservableList<ModeloCita> citasObservable;
    
    // Estado actual del calendario
    private YearMonth mesActual;
    private DateTimeFormatter formateadorMes = DateTimeFormatter.ofPattern("MMMM yyyy");
    
    // Formato para la fecha y hora
    private DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Inicializar el servicio
            servicio = new ServicioClinica();
            
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
            mesActual = YearMonth.now();
            actualizarLabelMes();
            
            // Agregar listener para búsqueda en tiempo real
            txtBuscarCita.textProperty().addListener((obs, oldVal, newVal) -> {
                cargarCitas();
            });
            
            // Cargar citas iniciales (hacerlo después de configurar todos los componentes)
            Platform.runLater(() -> {
                cargarCitas();
                generarCalendario();
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
            List<ModeloCita> citas = servicio.buscarCitasPorRangoFechas(
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
        
        // Actualizar el calendario
        generarCalendario();
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
                boolean eliminado = servicio.eliminarCita(citaSeleccionada.getId());
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
                    boolean actualizado = servicio.cambiarEstadoCita(citaSeleccionada.getId(), nuevoEstado);
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
        mesActual = mesActual.minusMonths(1);
        actualizarLabelMes();
        generarCalendario();
    }
    
    /**
     * Maneja el clic en el botón Mes Siguiente
     */
    @FXML
    private void onMesSiguiente(ActionEvent event) {
        mesActual = mesActual.plusMonths(1);
        actualizarLabelMes();
        generarCalendario();
    }
    
    /**
     * Actualiza la etiqueta del mes actual
     */
    private void actualizarLabelMes() {
        lblMesActual.setText(mesActual.format(formateadorMes));
    }
    
    /**
     * Genera el calendario del mes actual
     */
    private void generarCalendario() {
        // Limpiar el grid
        gridCalendario.getChildren().clear();
        
        // Configurar el grid
        int numColumnas = 7; // 7 días de la semana
        int numFilas = 6;    // Máximo 6 semanas en un mes
        
        // Agregar etiquetas de días de la semana
        String[] diasSemana = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label lblDia = new Label(diasSemana[i]);
            lblDia.setStyle("-fx-font-weight: bold;");
            gridCalendario.add(lblDia, i, 0);
        }
        
        // Obtener el primer día del mes y ajustar para que lunes sea 0
        LocalDate primerDia = mesActual.atDay(1);
        int diaSemana = primerDia.getDayOfWeek().getValue() - 1; // Lunes = 0, Domingo = 6
        
        // Obtener las citas para este mes
        List<ModeloCita> citasDelMes = servicio.buscarCitasPorRangoFechas(
            mesActual.atDay(1), 
            mesActual.atEndOfMonth());
        
        try {
            // Cargar el componente de celda
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/celda-calendario.fxml"));
            
            // Generar celdas para cada día del mes
            int dia = 1;
            int ultimoDia = mesActual.lengthOfMonth();
            
            for (int semana = 1; semana <= numFilas; semana++) {
                for (int columna = 0; columna < numColumnas; columna++) {
                    if ((semana == 1 && columna < diaSemana) || dia > ultimoDia) {
                        // Celda vacía (antes del primer día o después del último)
                        continue;
                    }
                    
                    // Crear celda para este día
                    FXMLLoader celdaLoader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/celda-calendario.fxml"));
                    Parent celdaRoot = celdaLoader.load();
                    CeldaCalendarioController celdaController = celdaLoader.getController();
                    
                    // Configurar la celda con el día y las citas
                    LocalDate fechaCelda = mesActual.atDay(dia);
                    celdaController.setDia(dia);
                    
                    // Agregar citas del día a la celda
                    for (ModeloCita cita : citasDelMes) {
                        LocalDate fechaCita = cita.getFechaHora().toLocalDate();
                        if (fechaCita.equals(fechaCelda)) {
                            celdaController.agregarCita(cita);
                        }
                    }
                    
                    // Agregar la celda al grid
                    gridCalendario.add(celdaRoot, columna, semana);
                    
                    dia++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al generar calendario", 
                "Ha ocurrido un error al intentar generar el calendario: " + e.getMessage());
        }
    }
    
    /**
     * Abre el formulario para crear o editar una cita
     */
    private void abrirFormularioCita(ModeloCita cita) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/cita-formulario.fxml"));
            Parent root = loader.load();
            
            CitaFormularioController controller = loader.getController();
            controller.setServicio(servicio);
            
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