package com.example.pruebamongodbcss.Modulos.Clinica;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;

import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Modulos.Clinica.Citas.CitasController;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;

import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.control.ListView;
import javafx.scene.text.TextAlignment;

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
    @FXML private TableColumn<ModeloPaciente, String> colSexoPaciente;
    @FXML private TableColumn<ModeloPaciente, String> colPesoPaciente;
    @FXML private TableColumn<ModeloPaciente, String> colFechaNacPaciente;
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
    @FXML private TableColumn<ModeloDiagnostico, String> colDiagnostico;
    @FXML private TableColumn<ModeloDiagnostico, String> colVeterinario;
    @FXML private MFXDatePicker dpFechaInicio;
    @FXML private MFXDatePicker dpFechaFin;
    @FXML private TextField txtBuscarDiagnostico;
    @FXML private Button btnBuscarDiagnostico;
    @FXML private Button btnNuevoDiagnostico;
    @FXML private Button btnVerDiagnostico;
    @FXML private Button btnEliminarDiagnostico;
    @FXML private ComboBox<ModeloPaciente> cmbPacientesDiagnostico;
    @FXML private Button btnLimpiarFiltro;
    @FXML private Button btnExportarPDF;
    @FXML private Button btnExportarCSV;
    
    // Tab de Citas
    @FXML private Tab tabCitas;
    @FXML private BorderPane citasContainer;
    
    // Servicio clínico
    //private ServicioClinica servicioClinica;
    
    // Listas observables para las tablas
    private ObservableList<ModeloPaciente> pacientesObservable;
    private ObservableList<ModeloPropietario> propietariosObservable;
    private ObservableList<ModeloDiagnostico> diagnosticosObservable;
    
    // Formato de fecha
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    
    // Controlador de citas
    private CitasController citasController;

    //Lanza peticiones al servidor
    private GestorSocket gestorPeticiones;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar el servicio clínico
        //servicioClinica = new ServicioClinica();
        gestorPeticiones = GestorSocket.getInstance();
        
        // Configurar las listas observables
        pacientesObservable = FXCollections.observableArrayList();
        propietariosObservable = FXCollections.observableArrayList();
        diagnosticosObservable = FXCollections.observableArrayList();
        
        // Configurar las tablas
        configurarTablaPacientes();
        configurarTablaPropietarios();
        configurarTablaDiagnosticos();
        
        // Hacer que las tablas se ajusten al tamaño del contenedor padre
        Platform.runLater(() -> {
            // Aseguramos que los controles ya están renderizados para acceder a sus padres
            if (tablaPacientes.getParent() != null) {
                tablaPacientes.prefWidthProperty().bind(((Region)tablaPacientes.getParent()).widthProperty().subtract(20));
            }
            
            if (tablaPropietarios.getParent() != null) {
                tablaPropietarios.prefWidthProperty().bind(((Region)tablaPropietarios.getParent()).widthProperty().subtract(20));
            }
            
            if (tablaDiagnosticos.getParent() != null) {
                tablaDiagnosticos.prefWidthProperty().bind(((Region)tablaDiagnosticos.getParent()).widthProperty().subtract(20));
            }
            
            // Aplicar listener para redimensionar columnas en cambio de tamaño de ventana
            Scene scene = mainPane.getScene();
            if (scene != null) {
                scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                    ajustarTablasResponsivas();
                });
                scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                    ajustarTablasResponsivas();
                });
            } else {
                // Si la escena aún no está disponible, esperar a que se establezca
                mainPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.widthProperty().addListener((obs2, oldVal, newVal) -> {
                            ajustarTablasResponsivas();
                        });
                        newScene.heightProperty().addListener((obs2, oldVal, newVal) -> {
                            ajustarTablasResponsivas();
                        });
                    }
                });
            }
        });
        
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
        
        // Cargar la vista de citas de forma proactiva
        // Esto asegura que esté listo antes de que el usuario haga clic en la pestaña
        cargarVistaCitas();
        
        // Configurar cambio de pestañas
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabCitas) {
                // Asegurarse de que la vista de citas esté cargada cuando se selecciona la pestaña
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/citas-view.fxml"));
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
    
    /**
     * Configura una columna con cabecera que permite múltiples líneas de texto
     * @param columna La columna a configurar
     * @param titulo El título que se mostrará en la cabecera
     * @param porcentajeAncho Porcentaje del ancho de la tabla que ocupará
     * @param tabla La tabla a la que pertenece la columna
     */
    private <T> void configurarColumnaCabecera(TableColumn<T, ?> columna, String titulo, double porcentajeAncho, TableView<T> tabla) {
        // Configurar ancho mínimo y máximo
        columna.setMinWidth(80);
        columna.setMaxWidth(5000);
        
        // Configurar ancho proporcional
        columna.prefWidthProperty().bind(
                tabla.widthProperty().multiply(porcentajeAncho));
        
        // Limpiar el texto original para evitar duplicados
        columna.setText("");
        
        // Crear un label que permita múltiples líneas
        Label labelCabecera = new Label(titulo);
        labelCabecera.setWrapText(true); // Permitir ajuste de texto
        labelCabecera.setAlignment(Pos.CENTER);
        labelCabecera.setTextAlignment(TextAlignment.CENTER);
        labelCabecera.setMaxWidth(Double.MAX_VALUE);
        
        // Establecer el gráfico de la cabecera
        columna.setGraphic(labelCabecera);
    }
    
    /**
     * Versión simplificada que utiliza la tabla de pacientes por defecto
     */
    private void configurarColumnaCabecera(TableColumn<ModeloPaciente, ?> columna, String titulo, double porcentajeAncho) {
        configurarColumnaCabecera(columna, titulo, porcentajeAncho, tablaPacientes);
    }
    
    private void configurarTablaPacientes() {
        // Configurar la tabla para ser editable
        tablaPacientes.setEditable(true);
        
        // Hacer que la tabla sea responsive
        tablaPacientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Configurar propiedades de columnas para que sean responsive
        // Definir porcentajes de ancho para las columnas y ajustar cabeceras para múltiples líneas
        configurarColumnaCabecera(colNombrePaciente, "Nombre", 0.15);
        configurarColumnaCabecera(colEspecie, "Especie", 0.12);
        configurarColumnaCabecera(colRaza, "Raza", 0.12);
        configurarColumnaCabecera(colPropietario, "Propietario", 0.15);
        configurarColumnaCabecera(colSexoPaciente, "Sexo", 0.1);
        configurarColumnaCabecera(colPesoPaciente, "Peso (kg)", 0.1);
        configurarColumnaCabecera(colFechaNacPaciente, "Fecha de\nNacimiento", 0.12);
        
        // Columna Nombre (editable con TextField)
        colNombrePaciente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colNombrePaciente.setCellFactory(tc -> {
            TableCell<ModeloPaciente, String> cell = new TableCell<>() {
                private TextField textField;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (textField == null) {
                        createTextField();
                    }
                    
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (textField != null) {
                                textField.setText(getString());
                            }
                            setText(null);
                            setGraphic(textField);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                            if (paciente.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createTextField() {
                    textField = new TextField(getString());
                    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    textField.setOnAction(e -> {
                        commitEdit(textField.getText());
                    });
                    
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commitEdit(textField.getText());
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                    paciente.setNombre(newValue);
                }
            };
            
            return cell;
        });
        
        // Columna Especie (editable con ComboBox)
        colEspecie.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEspecie()));
        colEspecie.setCellFactory(tc -> {
            TableCell<ModeloPaciente, String> cell = new TableCell<>() {
                private ComboBox<String> comboBox;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (comboBox == null) {
                        createComboBox();
                    }
                    
                    setText(null);
                    setGraphic(comboBox);
                    comboBox.requestFocus();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (comboBox != null) {
                                comboBox.setValue(getString());
                            }
                            setText(null);
                            setGraphic(comboBox);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                            if (paciente.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createComboBox() {
                    comboBox = new ComboBox<>();
                    comboBox.getItems().addAll("Perro", "Gato", "Ave", "Conejo", "Reptil", "Otro");
                    // Permitir edición para ingresar valores personalizados
                    comboBox.setEditable(true);
                    comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    comboBox.setOnAction(e -> {
                        String value = comboBox.getValue();
                        if (value != null && !value.isEmpty()) {
                            commitEdit(value);
                        }
                    });
                    
                    comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            String value = comboBox.getValue();
                            if (value != null && !value.isEmpty()) {
                                commitEdit(value);
                            }
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                    paciente.setEspecie(newValue);
                    // Forzar actualización de la vista
                    getTableView().refresh();
                }
            };
            
            return cell;
        });
        
        // Columna Raza (editable con TextField)
        colRaza.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRaza()));
        colRaza.setCellFactory(tc -> {
            TableCell<ModeloPaciente, String> cell = new TableCell<>() {
                private TextField textField;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (textField == null) {
                        createTextField();
                    }
                    
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (textField != null) {
                                textField.setText(getString());
                            }
                            setText(null);
                            setGraphic(textField);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                            if (paciente.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createTextField() {
                    textField = new TextField(getString());
                    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    textField.setOnAction(e -> {
                        commitEdit(textField.getText());
                    });
                    
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commitEdit(textField.getText());
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                    paciente.setRaza(newValue);
                }
            };
            
            return cell;
        });
        
        // Columna Propietario (editable con botón selector)
        colPropietario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombrePropietario()));
        colPropietario.setCellFactory(tc -> new TableCell<ModeloPaciente, String>() {
            private final TextField textField = new TextField();
            private final Button btnSeleccionar = new Button("+");
            private final HBox hbox = new HBox(5);
            
            {
                textField.setEditable(false);
                btnSeleccionar.setMinWidth(30);
                btnSeleccionar.getStyleClass().add("btn-secondary");
                
                btnSeleccionar.setOnAction(e -> {
                    ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                    seleccionarPropietario(paciente);
                });
                
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.getChildren().addAll(textField, btnSeleccionar);
                textField.setMaxWidth(Double.MAX_VALUE);
            }
            
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                    
                    if (paciente.isEditando()) {
                        textField.setText(paciente.getNombrePropietario());
                        setGraphic(hbox);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    } else {
                        setText(paciente.getNombrePropietario());
                        setGraphic(null);
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                    }
                }
            }
        });
        
        // Columna Sexo del Paciente
        colSexoPaciente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSexo()));
        colSexoPaciente.setCellFactory(tc -> {
            TableCell<ModeloPaciente, String> cell = new TableCell<>() {
                private ComboBox<String> comboBox;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (comboBox == null) {
                        createComboBox();
                    }
                    
                    setText(null);
                    setGraphic(comboBox);
                    comboBox.requestFocus();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (comboBox != null) {
                                comboBox.setValue(getString());
                            }
                            setText(null);
                            setGraphic(comboBox);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                            if (paciente.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createComboBox() {
                    comboBox = new ComboBox<>();
                    comboBox.getItems().addAll("Macho", "Hembra");
                    comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    comboBox.setOnAction(e -> {
                        commitEdit(comboBox.getSelectionModel().getSelectedItem());
                    });
                    
                    comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commitEdit(comboBox.getSelectionModel().getSelectedItem());
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                    paciente.setSexo(newValue);
                    // Forzar actualización de la vista
                    getTableView().refresh();
                }
            };
            
            return cell;
        });
        
        // Columna Peso del Paciente
        colPesoPaciente.setCellValueFactory(data -> {
            double peso = data.getValue().getPeso();
            return new SimpleStringProperty(peso > 0 ? String.valueOf(peso) : "");
        });
        colPesoPaciente.setCellFactory(tc -> {
            TableCell<ModeloPaciente, String> cell = new TableCell<>() {
                private TextField textField;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (textField == null) {
                        createTextField();
                    }
                    
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (textField != null) {
                                textField.setText(getString());
                            }
                            setText(null);
                            setGraphic(textField);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                            if (paciente.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createTextField() {
                    textField = new TextField(getString());
                    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    // Solo permitir valores numéricos
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("\\d*\\.?\\d*")) {
                            textField.setText(oldValue);
                        }
                    });
                    
                    textField.setOnAction(e -> {
                        commitEdit(textField.getText());
                    });
                    
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commitEdit(textField.getText());
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    try {
                        double peso = newValue.isEmpty() ? 0.0 : Double.parseDouble(newValue);
                        ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                        paciente.setPeso(peso);
                        // Forzar actualización de la vista
                        getTableView().refresh();
                    } catch (NumberFormatException e) {
                        // Mostrar error si el texto no es un número válido
                        mostrarAlerta("Error de formato", "Peso inválido", 
                                "El peso debe ser un número decimal válido.");
                    }
                }
            };
            
            return cell;
        });
        
        // Columna Fecha de Nacimiento del Paciente
        colFechaNacPaciente.setCellValueFactory(data -> {
            Date fecha = data.getValue().getFechaNacimiento();
            return new SimpleStringProperty(fecha != null ? formatoFecha.format(fecha) : "");
        });
        colFechaNacPaciente.setCellFactory(tc -> {
            TableCell<ModeloPaciente, String> cell = new TableCell<>() {
                private MFXDatePicker datePicker;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (datePicker == null) {
                        createDatePicker();
                    }
                    
                    setText(null);
                    setGraphic(datePicker);
                    datePicker.requestFocus();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (datePicker != null) {
                                // Intentar configurar la fecha actual en el DatePicker
                                if (getItem() != null && !getItem().isEmpty()) {
                                    try {
                                        Date date = formatoFecha.parse(getItem());
                                        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                        datePicker.setValue(localDate);
                                    } catch (Exception e) {
                                        // En caso de error, dejar el datepicker vacío
                                        datePicker.setValue(null);
                                    }
                                }
                            }
                            setText(null);
                            setGraphic(datePicker);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                            if (paciente.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createDatePicker() {
                    datePicker = new MFXDatePicker();
                    datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    // Actualizar el valor cuando se selecciona una fecha
                    datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            // Convertir LocalDate a Date y luego a String
                            Date date = Date.from(newVal.atStartOfDay(ZoneId.systemDefault()).toInstant());
                            String fechaStr = formatoFecha.format(date);
                            commitEdit(fechaStr);
                        }
                    });
                    
                    // Capturar el evento cuando se pierde el foco
                    datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            // Solo actualizar si hay una fecha seleccionada
                            if (datePicker.getValue() != null) {
                                Date date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                String fechaStr = formatoFecha.format(date);
                                commitEdit(fechaStr);
                            }
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    try {
                        ModeloPaciente paciente = getTableView().getItems().get(getIndex());
                        if (newValue != null && !newValue.isEmpty()) {
                            // Convertir el string a Date
                            Date fecha = formatoFecha.parse(newValue);
                            paciente.setFechaNacimiento(fecha);
                        } else {
                            paciente.setFechaNacimiento(null);
                        }
                        // Forzar actualización de la vista
                        getTableView().refresh();
                    } catch (Exception e) {
                        // Mostrar error si hay un problema con el formato
                        mostrarAlerta("Error de formato", "Fecha inválida", 
                                "La fecha debe tener el formato dd/MM/yyyy.");
                    }
                }
            };
            
            return cell;
        });
        
        tablaPacientes.setItems(pacientesObservable);
        
        // Añadir columna para botones de acciones
        TableColumn<ModeloPaciente, Void> colAcciones = new TableColumn<>();
        
        // Configurar la cabecera para permitir ajuste de texto
        Label labelAcciones = new Label("Acciones\nDisponibles");
        labelAcciones.setWrapText(true);
        labelAcciones.setAlignment(Pos.CENTER);
        labelAcciones.setTextAlignment(TextAlignment.CENTER);
        labelAcciones.setMaxWidth(Double.MAX_VALUE);
        colAcciones.setGraphic(labelAcciones);
        
        colAcciones.setMinWidth(150);
        colAcciones.setMaxWidth(5000);
        colAcciones.prefWidthProperty().bind(
                tablaPacientes.widthProperty().multiply(0.20)); // Aumentado a 20%
        
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnGuardar = new Button("Guardar");
            private final Button btnCancelar = new Button("Cancelar");
            private final Button btnCitas = new Button("Citas");
            private final Button btnVacunas = new Button("Vacunas");
            private final Button btnAlergias = new Button("Alergias");
            
            // Separar los botones en dos filas para mejor visualización
            private final HBox botonesFilaSuperior = new HBox(5);
            private final HBox botonesFilaInferior = new HBox(5);
            private final VBox botonesNormales = new VBox(5);
            private final HBox botonesEdicion = new HBox(5);
            
            {
                // Configurar estilos y propiedades
                btnEditar.getStyleClass().add("btn-secondary");
                btnEditar.setMinWidth(70);
                btnEditar.setPrefWidth(90);
                
                btnCitas.getStyleClass().add("btn-info");
                btnCitas.setMinWidth(70);
                btnCitas.setPrefWidth(90);
                
                btnVacunas.getStyleClass().add("btn-success");
                btnVacunas.setMinWidth(70);
                btnVacunas.setPrefWidth(90);
                
                btnAlergias.getStyleClass().add("btn-warning");
                btnAlergias.setMinWidth(70);
                btnAlergias.setPrefWidth(90);
                
                btnGuardar.getStyleClass().add("btn-primary");
                btnGuardar.setMinWidth(70);
                btnGuardar.setPrefWidth(90);
                
                btnCancelar.getStyleClass().add("btn-danger");
                btnCancelar.setMinWidth(70);
                btnCancelar.setPrefWidth(90);
                
                // Configurar el layout de dos filas
                botonesFilaSuperior.getChildren().addAll(btnEditar, btnCitas);
                botonesFilaInferior.getChildren().addAll(btnVacunas, btnAlergias);
                
                botonesFilaSuperior.setAlignment(Pos.CENTER);
                botonesFilaInferior.setAlignment(Pos.CENTER);
                
                botonesNormales.getChildren().addAll(botonesFilaSuperior, botonesFilaInferior);
                botonesNormales.setAlignment(Pos.CENTER);
                
                botonesEdicion.getChildren().addAll(btnGuardar, btnCancelar);
                botonesEdicion.setAlignment(Pos.CENTER);
                
                // Configurar eventos
                btnEditar.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        ModeloPaciente paciente = getTableView().getItems().get(index);
                        paciente.setEditando(true);
                        getTableView().refresh();
                    }
                });
                
                btnCitas.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        ModeloPaciente paciente = getTableView().getItems().get(index);
                        verCitasPaciente(paciente);
                    }
                });
                
                btnVacunas.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        ModeloPaciente paciente = getTableView().getItems().get(index);
                        mostrarVacunasPaciente(paciente);
                    }
                });
                
                btnAlergias.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        ModeloPaciente paciente = getTableView().getItems().get(index);
                        mostrarAlergiasPaciente(paciente);
                    }
                });

                btnGuardar.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        ModeloPaciente paciente = getTableView().getItems().get(index);

                        // Validar campos obligatorios antes de guardar
                        if (paciente.getNombre() == null || paciente.getNombre().trim().isEmpty()) {
                            mostrarAlerta("Error de validación", "Nombre requerido",
                                    "El nombre del paciente es obligatorio.");
                            return;
                        }

                        if (paciente.getEspecie() == null || paciente.getEspecie().trim().isEmpty()) {
                            mostrarAlerta("Error de validación", "Especie requerida",
                                    "La especie del paciente es obligatoria.");
                            return;
                        }

                        if (paciente.getPropietarioId() == null) {
                            mostrarAlerta("Error de validación", "Propietario requerido",
                                    "Debe asignar un propietario al paciente.");
                            return;
                        }

                        // Guardar el paciente
                        guardarPaciente(paciente);

                        // Desactivar modo edición
                        paciente.setEditando(false);
                        getTableView().refresh();
                    }
                });

                btnCancelar.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        ModeloPaciente paciente = getTableView().getItems().get(index);
                        if (paciente.getId() == null) {
                            // Si es nuevo, eliminarlo de la lista
                            getTableView().getItems().remove(index);
                        } else {
                            try {
                                // Si es existente, recargar sus datos
                                //Hacemos una peticion al servidor para obtener los datos del paciente
                                gestorPeticiones.enviarPeticion(Protocolo.OBTENERPACIENTE_POR_ID + Protocolo.SEPARADOR_CODIGO + paciente.getId());
                            } catch (IOException ex) {
                            }

                            ObjectInputStream entrada = gestorPeticiones.getEntrada();
                            try {
                                if (entrada.readInt() == Protocolo.OBTENERPACIENTE_POR_ID_RESPONSE) {

                                    ModeloPaciente pacienteOriginal = (ModeloPaciente) entrada.readObject();
                                    if (pacienteOriginal != null) {
                                        pacientesObservable.set(index, pacienteOriginal);
                                    }
                                    paciente.setEditando(false);
                                } else {
                                    mostrarAlerta("Error", "Error al obtener el paciente", "No se pudo obtener el paciente. Inténtelo de nuevo.");
                                }
                            } catch (ClassNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                        getTableView().refresh();
                    }
                });
            }
            
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        ModeloPaciente paciente = getTableView().getItems().get(index);
                        setGraphic(paciente.isEditando() ? botonesEdicion : botonesNormales);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        tablaPacientes.getColumns().add(colAcciones);
        
        // Añadir botón para nuevo paciente
        Button btnAgregar = new Button("+");
        btnAgregar.getStyleClass().add("btn-success");
        btnAgregar.setOnAction(e -> onNuevoPaciente(new ActionEvent()));
        
        VBox headerBox = new VBox(btnAgregar);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10));
        tablaPacientes.setPlaceholder(headerBox);
        
        // Añadir detección de doble clic para activar edición
        tablaPacientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaPacientes.getSelectionModel().getSelectedItem() != null) {
                ModeloPaciente paciente = tablaPacientes.getSelectionModel().getSelectedItem();
                if (!paciente.isEditando()) {
                    paciente.setEditando(true);
                    tablaPacientes.refresh();
                }
            }
        });
    }
    
    /**
     * Crea una nueva fila para un paciente y activa el modo de edición
     */
    private void crearNuevoPaciente() {
        ModeloPaciente nuevoPaciente = new ModeloPaciente();
        nuevoPaciente.setEditando(true);
        pacientesObservable.add(0, nuevoPaciente);
        tablaPacientes.setEditable(true);
        tablaPacientes.refresh();
        tablaPacientes.getSelectionModel().select(0);
        tablaPacientes.scrollTo(0);
    }

    /**
     * Guarda un paciente
     */
    private void guardarPaciente(ModeloPaciente paciente) {
        try {
            // Validar datos requeridos
            if (paciente.getNombre() == null || paciente.getNombre().trim().isEmpty()) {
                mostrarAlerta("Error de validación", "Nombre requerido",
                        "El nombre del paciente es obligatorio.");
                return;
            }

            if (paciente.getEspecie() == null || paciente.getEspecie().trim().isEmpty()) {
                mostrarAlerta("Error de validación", "Especie requerida",
                        "La especie del paciente es obligatoria.");
                return;
            }

            if (paciente.getPropietarioId() == null) {
                mostrarAlerta("Error de validación", "Propietario requerido",
                        "Debe asignar un propietario al paciente.");
                return;
            }

            if (paciente.getSexo() == null || paciente.getSexo().trim().isEmpty()) {
                mostrarAlerta("Error de validación", "Sexo requerido",
                        "El sexo del paciente es obligatorio.");
                return;
            }

            if (paciente.getId() == null) {
                // Nuevo paciente
                //Hacemos una peticion al servidor para agregar el paciente
                gestorPeticiones.enviarPeticion(Protocolo.CREARPACIENTE + Protocolo.SEPARADOR_CODIGO);
                ObjectOutputStream salida = gestorPeticiones.getSalida();
                salida.writeObject(paciente);
                salida.flush();

                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.CREARPACIENTE_RESPONSE) {
                    mostrarMensaje("Éxito", "Paciente agregado",
                            "El paciente ha sido agregado correctamente.");
                } else {
                    mostrarAlerta("Error", "Error al agregar paciente",
                            "No se pudo agregar el paciente. Inténtelo de nuevo.");
                }

            } else {
                // Actualizar paciente existente
                //Hacemos una peticion al servidor para actualizar el paciente
                gestorPeticiones.enviarPeticion(Protocolo.ACTUALIZARPACIENTE + Protocolo.SEPARADOR_CODIGO);
                ObjectOutputStream salida = gestorPeticiones.getSalida();
                salida.writeObject(paciente);
                salida.flush();

                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.ACTUALIZAREVENTOS_RESPONSE) {
                    mostrarMensaje("Éxito", "Paciente agregado",
                            "El paciente ha sido agregado correctamente.");
                } else {
                    mostrarAlerta("Error", "Error al agregar paciente",
                            "No se pudo agregar el paciente. Inténtelo de nuevo.");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al guardar paciente",
                    "Ha ocurrido un error al intentar guardar el paciente: " + e.getMessage());
        }
    }
    /**
     * Actualiza el modo de edición de un paciente
     */
    private void actualizarModoPaciente(ModeloPaciente paciente) {
        pacientesObservable.set(pacientesObservable.indexOf(paciente), paciente);
        tablaPacientes.refresh();
    }
    
    private void configurarTablaPropietarios() {
        // Configurar la tabla para ser editable
        tablaPropietarios.setEditable(true);
        
        // Hacer que la tabla sea responsive
        tablaPropietarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Configurar propiedades de columnas para que sean responsive
        configurarColumnaCabecera(colNombrePropietario, "Nombre\nCompleto", 0.3, tablaPropietarios);
        configurarColumnaCabecera(colDNI, "DNI/NIF", 0.2, tablaPropietarios);
        configurarColumnaCabecera(colTelefono, "Teléfono", 0.2, tablaPropietarios);
        configurarColumnaCabecera(colEmail, "Correo\nElectrónico", 0.3, tablaPropietarios);
        
        // Añadir celdas editables para cada columna
        colNombrePropietario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colNombrePropietario.setCellFactory(column -> new TableCell<ModeloPropietario, String>() {
            private TextField textField;
            private final TextField tfNombre = new TextField();
            private final TextField tfApellidos = new TextField();
            private final HBox hbox = new HBox(5, new Label("Nombre:"), tfNombre, new Label("Apellidos:"), tfApellidos);
            
            {
                hbox.setAlignment(Pos.CENTER_LEFT);
            }
            
            @Override
            public void startEdit() {
                ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                
                if (!propietario.isEditando()) {
                    return;
                }
                
                super.startEdit();
                
                tfNombre.setText(propietario.getNombre());
                tfApellidos.setText(propietario.getApellidos());
                
                setGraphic(hbox);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                tfNombre.requestFocus();
            }
            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
            
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    
                    if (propietario.isEditando()) {
                        if (isEditing()) {
                            tfNombre.setText(propietario.getNombre());
                            tfApellidos.setText(propietario.getApellidos());
                            setGraphic(hbox);
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        } else {
                            setText(propietario.getNombreCompleto());
                            setGraphic(null);
                            setContentDisplay(ContentDisplay.TEXT_ONLY);
                            setOnMouseClicked(e -> startEdit());
                        }
                    } else {
                        setText(propietario.getNombreCompleto());
                        setGraphic(null);
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        setOnMouseClicked(null);
                    }
                }
            }
            
            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                
                ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                propietario.setNombre(tfNombre.getText());
                propietario.setApellidos(tfApellidos.getText());
                
                tablaPropietarios.refresh();
            }
        });
        
        colDNI.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDni()));
        colDNI.setCellFactory(tc -> {
            TableCell<ModeloPropietario, String> cell = new TableCell<>() {
                private TextField textField;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (textField == null) {
                        createTextField();
                    }
                    
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (textField != null) {
                                textField.setText(getString());
                            }
                            setText(null);
                            setGraphic(textField);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                            if (propietario.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createTextField() {
                    textField = new TextField(getString());
                    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    textField.setOnAction(e -> {
                        commitEdit(textField.getText());
                    });
                    
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commitEdit(textField.getText());
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    propietario.setDni(newValue);
                }
            };
            
            return cell;
        });
        
        colTelefono.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefono()));
        colTelefono.setCellFactory(tc -> {
            TableCell<ModeloPropietario, String> cell = new TableCell<>() {
                private TextField textField;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (textField == null) {
                        createTextField();
                    }
                    
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (textField != null) {
                                textField.setText(getString());
                            }
                            setText(null);
                            setGraphic(textField);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                            if (propietario.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createTextField() {
                    textField = new TextField(getString());
                    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    textField.setOnAction(e -> {
                        commitEdit(textField.getText());
                    });
                    
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commitEdit(textField.getText());
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    propietario.setTelefono(newValue);
                }
            };
            
            return cell;
        });
        
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colEmail.setCellFactory(tc -> {
            TableCell<ModeloPropietario, String> cell = new TableCell<>() {
                private TextField textField;
                
                @Override
                public void startEdit() {
                    if (isEmpty() || !getTableView().getItems().get(getIndex()).isEditando()) {
                        return;
                    }
                    
                    super.startEdit();
                    
                    if (textField == null) {
                        createTextField();
                    }
                    
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }
                
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            if (textField != null) {
                                textField.setText(getString());
                            }
                            setText(null);
                            setGraphic(textField);
                        } else {
                            setText(getString());
                            setGraphic(null);
                            
                            // Si está en modo edición, permitir doble clic para editar
                            ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                            if (propietario.isEditando()) {
                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() == 2) {
                                        startEdit();
                                    }
                                });
                            } else {
                                setOnMouseClicked(null);
                            }
                        }
                    }
                }
                
                private void createTextField() {
                    textField = new TextField(getString());
                    textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                    
                    textField.setOnAction(e -> {
                        commitEdit(textField.getText());
                    });
                    
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commitEdit(textField.getText());
                        }
                    });
                }
                
                private String getString() {
                    return getItem() == null ? "" : getItem();
                }
                
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    propietario.setEmail(newValue);
                }
            };
            
            return cell;
        });
        
        tablaPropietarios.setItems(propietariosObservable);
        
        // Añadir columna para botones de acciones
        TableColumn<ModeloPropietario, Void> colAcciones = new TableColumn<>();
        
        // Configurar la cabecera para permitir ajuste de texto
        Label labelAcciones = new Label("Acciones\nDisponibles");
        labelAcciones.setWrapText(true);
        labelAcciones.setAlignment(Pos.CENTER);
        labelAcciones.setTextAlignment(TextAlignment.CENTER);
        labelAcciones.setMaxWidth(Double.MAX_VALUE);
        colAcciones.setGraphic(labelAcciones);
        
        colAcciones.setMinWidth(120);
        colAcciones.setMaxWidth(5000);
        colAcciones.prefWidthProperty().bind(
                tablaPropietarios.widthProperty().multiply(0.2)); // 20%
        
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnVerMascotas = new Button("Mascotas");
            private final Button btnGuardar = new Button("Guardar");
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox botonesEdicion = new HBox(5);
            private final HBox botonesNormales = new HBox(5);
            
            {
                // Configurar estilos y propiedades
                btnEditar.getStyleClass().add("btn-secondary");
                btnEditar.setMinWidth(60);

                btnVerMascotas.getStyleClass().add("btn-info");
                btnVerMascotas.setMinWidth(60);

                btnGuardar.getStyleClass().add("btn-primary");
                btnGuardar.setMinWidth(60);

                btnCancelar.getStyleClass().add("btn-danger");
                btnCancelar.setMinWidth(60);

                botonesNormales.getChildren().addAll(btnEditar, btnVerMascotas);
                botonesEdicion.getChildren().addAll(btnGuardar, btnCancelar);

                // Configurar eventos
                btnEditar.setOnAction(event -> {
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    tablaPropietarios.setEditable(true);
                    propietario.setEditando(true);
                    actualizarModoPropietario(propietario);
                });

                btnVerMascotas.setOnAction(event -> {
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    verMascotasPropietario(propietario);
                });

                btnGuardar.setOnAction(event -> {
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    guardarPropietario(propietario);
                    propietario.setEditando(false);
                    actualizarModoPropietario(propietario);
                    tablaPropietarios.setEditable(false);
                    tablaPropietarios.refresh();
                });

                btnCancelar.setOnAction(event -> {
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    if (propietario.getId() == null) {
                        // Si es nuevo, eliminarlo de la lista
                        propietariosObservable.remove(propietario);
                    } else {
                        try {
                            // Si es existente, recargar sus datos
                            //Hacemos una peticion al servidor para obtener los datos del propietario
                            gestorPeticiones.enviarPeticion(Protocolo.OBTENERPROPIETARIO_POR_ID + Protocolo.SEPARADOR_CODIGO + propietario.getId());

                            ObjectInputStream entrada = gestorPeticiones.getEntrada();

                            if (entrada.readInt() == Protocolo.OBTENERPROPIETARIO_POR_ID_RESPONSE) {
                                ModeloPropietario propietarioOriginal = (ModeloPropietario) entrada.readObject();
                                int index = propietariosObservable.indexOf(propietario);
                                if (index >= 0 && propietarioOriginal != null) {
                                    propietariosObservable.set(index, propietarioOriginal);
                                }
                            } else {
                                mostrarAlerta("Error", "Error al obtener el propietario", "No se pudo obtener el propietario. Inténtelo de nuevo.");
                            }
                        } catch (IOException ex) {
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    tablaPropietarios.setEditable(false);
                    tablaPropietarios.refresh();
                });
            }
            
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ModeloPropietario propietario = getTableView().getItems().get(getIndex());
                    setGraphic(propietario.isEditando() ? botonesEdicion : botonesNormales);
                }
            }
        });
        
        tablaPropietarios.getColumns().add(colAcciones);
        
        // Añadir botón para nuevo propietario como primera fila
        Button btnAgregar = new Button("+");
        btnAgregar.getStyleClass().add("btn-success");
        btnAgregar.setOnAction(e -> crearNuevoPropietario());
        
        VBox headerBox = new VBox(btnAgregar);
        headerBox.setAlignment(Pos.CENTER);
        tablaPropietarios.setPlaceholder(headerBox);
    }
    
    /**
     * Crea una nueva fila para un propietario y activa el modo de edición
     */
    private void crearNuevoPropietario() {
        ModeloPropietario nuevoPropietario = new ModeloPropietario();
        nuevoPropietario.setEditando(true);
        nuevoPropietario.setFechaAlta(new Date());
        
        // Añadir al inicio de la lista y seleccionarlo
        propietariosObservable.add(0, nuevoPropietario);
        tablaPropietarios.setEditable(true);
        tablaPropietarios.refresh();
        tablaPropietarios.getSelectionModel().select(0);
        tablaPropietarios.scrollTo(0);
        
        // Activar la celda del nombre para empezar a editar inmediatamente
        Platform.runLater(() -> {
            tablaPropietarios.edit(0, colNombrePropietario);
        });
    }
    
    /**
     * Guarda un propietario
     */
    private void guardarPropietario(ModeloPropietario propietario) {
        try {
            // Validar datos requeridos
            if (propietario.getNombre() == null || propietario.getNombre().trim().isEmpty()) {
                mostrarAlerta("Error de validación", "Nombre requerido", 
                        "El nombre del propietario es obligatorio.");
                return;
            }
            
            if (propietario.getDni() == null || propietario.getDni().trim().isEmpty()) {
                mostrarAlerta("Error de validación", "DNI requerido", 
                        "El DNI del propietario es obligatorio.");
                return;
            }
            
            if (propietario.getTelefono() == null || propietario.getTelefono().trim().isEmpty()) {
                mostrarAlerta("Error de validación", "Teléfono requerido", 
                        "El teléfono del propietario es obligatorio.");
                return;
            }
            
            if (propietario.getId() == null) {
                // Nuevo propietario
                //Hacemos una peticion al servidor para guardar el propietario
                gestorPeticiones.enviarPeticion(Protocolo.CREARPROPIETARIO + Protocolo.SEPARADOR_CODIGO);
                ObjectOutputStream salida = gestorPeticiones.getSalida();
                salida.writeObject(propietario);
                salida.flush();

                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.CREARPROPIETARIO_RESPONSE) {
                    mostrarMensaje("Éxito", "Propietario agregado", 
                            "El propietario ha sido agregado correctamente.");
                } else {
                    mostrarAlerta("Error", "Error al agregar propietario", 
                            "No se pudo agregar el propietario. Inténtelo de nuevo.");
                }

            } else {
                // Actualizar propietario existente
                //Hacemos una peticion al servidor para actualizar el propietario
                gestorPeticiones.enviarPeticion(Protocolo.ACTUALIZARPROPIETARIO + Protocolo.SEPARADOR_CODIGO);

                ObjectOutputStream salida = gestorPeticiones.getSalida();
                salida.writeObject(propietario);
                salida.flush();

                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.ACTUALIZARPROPIETARIO_RESPONSE) {
                    mostrarMensaje("Éxito", "Propietario actualizado", 
                            "El propietario ha sido actualizado correctamente.");
                } else {
                    mostrarAlerta("Error", "Error al actualizar propietario", 
                            "No se pudo actualizar el propietario. Inténtelo de nuevo.");
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al guardar propietario", 
                    "Ha ocurrido un error al intentar guardar el propietario: " + e.getMessage());
        }
    }
    
    /**
     * Exporta los diagnósticos seleccionados o visibles a un archivo CSV
     */
    @FXML
    private void onExportarCSVDiagnostico() {
        // Si no hay diagnósticos para exportar
        if (diagnosticosObservable.isEmpty()) {
            mostrarAlerta("Sin datos", "No hay diagnósticos para exportar", 
                    "No hay diagnósticos disponibles para exportar a CSV.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        fileChooser.setInitialFileName("diagnosticos.csv");
        
        File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Escribir encabezados
                writer.write("Fecha,Paciente,Motivo,Diagnóstico,Veterinario\n");
                
                // Escribir cada diagnóstico
                for (ModeloDiagnostico diag : diagnosticosObservable) {
                    String fecha = diag.getFecha() != null ? formatoFecha.format(diag.getFecha()) : "";
                    String paciente = diag.getNombrePaciente() != null ? diag.getNombrePaciente().replace(",", ";") : "";
                    String motivo = diag.getMotivo() != null ? diag.getMotivo().replace(",", ";") : "";
                    String diagnostico = diag.getDiagnostico() != null ? diag.getDiagnostico().replace(",", ";") : "";
                    String veterinario = diag.getVeterinario() != null ? diag.getVeterinario().replace(",", ";") : "";
                    
                    writer.write(fecha + "," +
                                paciente + "," +
                                motivo + "," +
                                diagnostico + "," +
                                veterinario + "\n");
                }
                
                mostrarMensaje("Exportación exitosa", "Diagnósticos exportados", 
                        "Los diagnósticos han sido exportados correctamente a CSV.");
                
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error al exportar", 
                        "Ha ocurrido un error al intentar exportar los diagnósticos a CSV: " + e.getMessage());
            }
        }
    }
    
    /**
     * Exporta los diagnósticos seleccionados o visibles a un archivo PDF
     */
    @FXML
    private void onExportarPDFDiagnostico(ActionEvent event) {
        ModeloDiagnostico diagnostico = tablaDiagnosticos.getSelectionModel().getSelectedItem();
        if (diagnostico == null) {
            mostrarAlerta("Selección requerida", "No hay diagnóstico seleccionado",
                    "Por favor, seleccione un diagnóstico para exportar a PDF.");
            return;
        }

        try {
            // Abrir la vista de diagnóstico para exportación
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Diagnostico/diagnostico-view.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y configurarlo con el diagnóstico seleccionado
            com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico.DiagnosticoController controller = loader.getController();

            // Buscar el paciente asociado al diagnóstico
            //Hacemos una peticion al servidor para obtener el paciente asociado al diagnóstico
            gestorPeticiones.enviarPeticion(Protocolo.OBTENERPACIENTE_POR_ID + Protocolo.SEPARADOR_CODIGO + diagnostico.getPacienteId());

            ObjectInputStream entrada = gestorPeticiones.getEntrada();
            if (entrada.readInt() == Protocolo.OBTENERPACIENTE_POR_ID_RESPONSE) {
                ModeloPaciente paciente = (ModeloPaciente) entrada.readObject();
                if (paciente != null) {
                    // Obtener la cita más reciente del paciente
                    try {
                        gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_CITAS_POR_PACIENTE + Protocolo.SEPARADOR_CODIGO + paciente.getId());
                        
                        ObjectInputStream entradaCitas = gestorPeticiones.getEntrada();
                        if (entradaCitas.readInt() == Protocolo.BUSCAR_CITAS_POR_PACIENTE_RESPONSE) {
                            List<ModeloCita> citas = (List<ModeloCita>) entradaCitas.readObject();
                            ModeloCita cita = citas.isEmpty() ? null : citas.get(0); // La primera cita es la más reciente porque están ordenadas
                            controller.setPaciente(paciente, cita);
                            controller.setDiagnostico(diagnostico);

                            // Llamar al método de exportación a PDF
                            controller.exportarPDFDesdeLista(diagnostico, paciente, cita);
                        } else {
                            mostrarAlerta("Error", "Error al obtener citas",
                                    "No se pudieron obtener las citas del paciente.");
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                        mostrarAlerta("Error", "Error al obtener citas",
                                "Ha ocurrido un error al obtener las citas: " + ex.getMessage());
                    }
                } else {
                    mostrarAlerta("Error", "Paciente no encontrado",
                            "No se pudo encontrar el paciente asociado a este diagnóstico.");
                }
            } else {
                mostrarAlerta("Error", "Error al obtener el paciente",
                        "No se pudo obtener el paciente. Inténtelo de nuevo.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al exportar",
                    "Ha ocurrido un error al intentar exportar el diagnóstico a PDF: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    // ********** CARGA DE DATOS **********
    private void cargarPacientes() {
        pacientesObservable.clear();
        try {
            //Hacemos una peticion al servidor para obtener todos los pacientes
            gestorPeticiones.enviarPeticion(Protocolo.OBTENER_TODOS_PACIENTES + Protocolo.SEPARADOR_CODIGO);

            ObjectInputStream entrada = gestorPeticiones.getEntrada();
            if (entrada.readInt() == Protocolo.OBTENER_TODOS_PACIENTES_RESPONSE) {
                List<ModeloPaciente> pacientes = (List<ModeloPaciente>) entrada.readObject();
                pacientesObservable.addAll(pacientes);
            } else {
                mostrarAlerta("Error", "Error al obtener los pacientes",
                        "No se pudo obtener los pacientes. Inténtelo de nuevo.");
            }

        } catch (IOException ex) {
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 
    }
    
    private void cargarPropietarios() {
        try {
            propietariosObservable.clear();

            //Hacemos una peticion al servidor para obtener todos los propietarios
            gestorPeticiones.enviarPeticion(Protocolo.OBTENER_TODOS_PROPIETARIOS + Protocolo.SEPARADOR_CODIGO);

            ObjectInputStream entrada = gestorPeticiones.getEntrada();
            if (entrada.readInt() == Protocolo.OBTENER_TODOS_PROPIETARIOS_RESPONSE) {
                List<ModeloPropietario> propietarios = (List<ModeloPropietario>) entrada.readObject();
                propietariosObservable.addAll(propietarios);
            }
        } catch (IOException ex) {
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    @FXML
    public void buscarDiagnosticos() {
        diagnosticosObservable.clear();
        
        if (dpFechaInicio.getValue() != null && dpFechaFin.getValue() != null) {
            try {
                Date fechaInicio = Date.from(dpFechaInicio.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date fechaFin = Date.from(dpFechaFin.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                
                //Hacemos una peticion al servidor para obtener los diagnosticos por fecha
                gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_DIAGNOSTICOS_POR_FECHA + Protocolo.SEPARADOR_CODIGO);

                //Enviamos los datos al servidor
                ObjectOutputStream salida = gestorPeticiones.getSalida();
                salida.writeObject(fechaInicio);
                salida.writeObject(fechaFin);
                salida.flush();

                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.BUSCAR_DIAGNOSTICOS_POR_FECHA_RESPONSE) {
                    List<ModeloDiagnostico> diagnosticos = (List<ModeloDiagnostico>) entrada.readObject();
                    diagnosticosObservable.addAll(diagnosticos);
                } else {
                    mostrarAlerta("Error", "Error al obtener los diagnosticos",
                            "No se pudo obtener los diagnosticos. Inténtelo de nuevo.");
                }

            } catch (IOException ex) {
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
        try {
            pacientesObservable.clear();
            //Hacemos una peticion al servidor para obtener los pacientes por nombre
            gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_PACIENTES_POR_NOMBRE + Protocolo.SEPARADOR_CODIGO);
            
            //Enviamos el nombre al servidor
            ObjectOutputStream salida = gestorPeticiones.getSalida();
            salida.writeObject(nombre);
            salida.flush();
            
            ObjectInputStream entrada = gestorPeticiones.getEntrada();
            if (entrada.readInt() == Protocolo.BUSCAR_PACIENTES_POR_NOMBRE_RESPONSE) {
                List<ModeloPaciente> pacientes = (List<ModeloPaciente>) entrada.readObject();
                pacientesObservable.addAll(pacientes);
            } else {
                mostrarAlerta("Error", "Error al obtener los pacientes",
                        "No se pudo obtener los pacientes. Inténtelo de nuevo.");
            }
        } catch (IOException | ClassNotFoundException ex) {
        }

    }
    
    private void buscarPropietariosPorNombre(String nombre) {
        try {
            propietariosObservable.clear();
            
            //Hacemos una peticion al servidor para obtener los propietarios por nombre
            gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_PROPIETARIOS_POR_NOMBRE + Protocolo.SEPARADOR_CODIGO + nombre);
     
            //Recibimos la respuesta del servidor
            ObjectInputStream entrada = gestorPeticiones.getEntrada();
            if (entrada.readInt() == Protocolo.BUSCAR_PROPIETARIOS_POR_NOMBRE_RESPONSE) {
                List<ModeloPropietario> propietarios = (List<ModeloPropietario>) entrada.readObject();
                propietariosObservable.addAll(propietarios);
            } else {
                mostrarAlerta("Error", "Error al obtener los propietarios",
                        "No se pudo obtener los propietarios. Inténtelo de nuevo.");
            }            

        } catch (IOException ex) {
        } catch (ClassNotFoundException ex) {
        }
    }
    
    // ********** ACCIONES DE PACIENTES **********
    
    @FXML
    private void onNuevoPaciente(ActionEvent event) {
        // Crear un nuevo paciente vacío y añadirlo al final de la tabla
        ModeloPaciente nuevoPaciente = new ModeloPaciente();
        nuevoPaciente.setEditando(true);
        
        // Añadir al inicio de la lista y seleccionarlo
        pacientesObservable.add(0, nuevoPaciente);
        tablaPacientes.setEditable(true);
        tablaPacientes.refresh();
        tablaPacientes.getSelectionModel().select(0);
        tablaPacientes.scrollTo(0);
        
        // Activar la celda del nombre para empezar a editar inmediatamente
        Platform.runLater(() -> {
            tablaPacientes.edit(0, colNombrePaciente);
        });
    }
    
    @FXML
    private void onEditarPaciente(ActionEvent event) {
        ModeloPaciente pacienteSeleccionado = tablaPacientes.getSelectionModel().getSelectedItem();
        
        if (pacienteSeleccionado != null) {
            habilitarEdicionPaciente(pacienteSeleccionado, false);
        } else {
            mostrarAlerta("Selección requerida", "Seleccione un paciente", 
                    "Debe seleccionar un paciente de la tabla para editarlo.");
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
                try {
                    //Hacemos una peticion al servidor para eliminar el paciente
                    gestorPeticiones.enviarPeticion(Protocolo.ELIMINARPACIENTE + Protocolo.SEPARADOR_CODIGO + paciente.getId());
                    
                    ObjectInputStream entrada = gestorPeticiones.getEntrada();
                    if (entrada.readInt() == Protocolo.ELIMINARPACIENTE_RESPONSE) {
                        boolean eliminado = true;
                        cargarPacientes();
                        mostrarMensaje("Paciente eliminado", "El paciente ha sido eliminado",
                                "El paciente y sus diagnósticos asociados han sido eliminados exitosamente.");
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el paciente",
                                "Ha ocurrido un error al intentar eliminar el paciente.");
                    }
                    
                } catch (IOException ex) {
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
            //Hacemos una peticion al servidor para obtener los diagnosticos por paciente
            try {
                gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_DIAGNOSTICOS_POR_PACIENTE + Protocolo.SEPARADOR_CODIGO + paciente.getId());

                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.BUSCAR_DIAGNOSTICOS_POR_PACIENTE_RESPONSE) {
                    List<ModeloDiagnostico> diagnosticos = (List<ModeloDiagnostico>) entrada.readObject();
                    // Si no hay diagnósticos, mostrar mensaje
                    if (diagnosticos.isEmpty()) {
                        mostrarMensaje("Sin diagnósticos", "No hay diagnósticos para este paciente",
                                "El paciente " + paciente.getNombre() + " no tiene diagnósticos registrados.");
                    }
                    diagnosticosObservable.addAll(diagnosticos);
                } else {
                    mostrarAlerta("Error", "Error al obtener los diagnosticos",
                            "No se pudo obtener los diagnosticos. Inténtelo de nuevo.");
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            mostrarAlerta("Selección requerida", "No hay paciente seleccionado",
                    "Por favor, seleccione un paciente para ver su historial.");
        }
    }
    
    // ********** ACCIONES DE PROPIETARIOS **********
    
    @FXML
    private void onNuevoPropietario(ActionEvent event) {
        // Crear un nuevo propietario vacío y añadirlo al inicio de la tabla
        ModeloPropietario nuevoPropietario = new ModeloPropietario();
        nuevoPropietario.setEditando(true);
        nuevoPropietario.setFechaAlta(new Date());
        
        // Añadir al inicio de la lista y seleccionarlo
        propietariosObservable.add(0, nuevoPropietario);
        tablaPropietarios.setEditable(true);
        tablaPropietarios.refresh();
        tablaPropietarios.getSelectionModel().select(0);
        tablaPropietarios.scrollTo(0);
        
        // Activar la celda del nombre para empezar a editar inmediatamente
        Platform.runLater(() -> {
            tablaPropietarios.edit(0, colNombrePropietario);
        });
    }
    
    @FXML
    private void onEditarPropietario(ActionEvent event) {
        ModeloPropietario propietarioSeleccionado = tablaPropietarios.getSelectionModel().getSelectedItem();
        
        if (propietarioSeleccionado != null) {
            habilitarEdicionPropietario(propietarioSeleccionado, false);
        } else {
            mostrarAlerta("Selección requerida", "Seleccione un propietario", 
                    "Debe seleccionar un propietario de la tabla para editarlo.");
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
                //Hacemos una peticion al servidor para eliminar el propietario
                try {
                    gestorPeticiones.enviarPeticion(Protocolo.ELIMINARPROPIETARIO + Protocolo.SEPARADOR_CODIGO + propietario.getId());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                try {
                    if (entrada.readInt() == Protocolo.ELIMINARPROPIETARIO_RESPONSE) {
                        boolean eliminado = true;
                        cargarPropietarios();
                        mostrarMensaje("Propietario eliminado", "El propietario ha sido eliminado",
                                "El propietario ha sido eliminado exitosamente.");
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el propietario",
                                "No se puede eliminar el propietario porque tiene mascotas asociadas.");
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
            // Navegar a la pestaña de pacientes
            tabPane.getSelectionModel().select(tabPacientes);
            
            // Buscar los pacientes de este propietario
            pacientesObservable.clear();
            try {
                gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_PACIENTES_POR_PROPIETARIO + Protocolo.SEPARADOR_CODIGO + propietario.getId());
                
                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.BUSCAR_PACIENTES_POR_PROPIETARIO_RESPONSE) {
                    List<ModeloPaciente> mascotas = (List<ModeloPaciente>) entrada.readObject();
                    pacientesObservable.addAll(mascotas);
                    
                    // Si no hay mascotas, mostrar mensaje
                    if (mascotas.isEmpty()) {
                        mostrarMensaje("Sin mascotas", "No hay mascotas para este propietario", 
                                "El propietario " + propietario.getNombreCompleto() + " no tiene mascotas registradas.");
                    }
                } else {
                    mostrarAlerta("Error", "Error al obtener mascotas",
                            "No se pudieron obtener las mascotas del propietario.");
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                mostrarAlerta("Error", "Error al obtener mascotas",
                        "Ha ocurrido un error al obtener las mascotas: " + ex.getMessage());
            }


        }
    }
    
    // ********** ACCIONES DE DIAGNÓSTICOS **********
    
    @FXML
    private void onNuevoDiagnostico(ActionEvent event) {
        try {
            // Crear un contenedor para el spinner y la imagen
            StackPane loadingPane = new StackPane();
            // Configurar para que use todo el espacio disponible
            loadingPane.setMinSize(200, 200);
            loadingPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            loadingPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            loadingPane.setStyle("-fx-background-color: transparent;");
            
            // Imagen de fondo para el área de carga
            ImageView backgroundImage = new ImageView();
            try {
                Image image = new Image(getClass().getResourceAsStream("/ImagenCarga/carga1.png"));
                backgroundImage.setImage(image);
                
                // Vinculamos las dimensiones de la imagen al contenedor para que se ajuste automáticamente
                backgroundImage.fitWidthProperty().bind(loadingPane.widthProperty());
                backgroundImage.fitHeightProperty().bind(loadingPane.heightProperty());
                backgroundImage.setPreserveRatio(false); // Para que ocupe todo el espacio
                backgroundImage.setSmooth(true); // Para mejor calidad
                
                // Centrar en el panel
                StackPane.setAlignment(backgroundImage, Pos.CENTER);
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen de fondo: " + e.getMessage());
            }
            
            // Crear el spinner circular con tamaño proporcional
            MFXProgressSpinner spinner = new MFXProgressSpinner();
            spinner.minWidthProperty().bind(loadingPane.widthProperty().multiply(0.10));  // 10% del ancho
            spinner.minHeightProperty().bind(loadingPane.heightProperty().multiply(0.10)); // 10% del alto
            spinner.prefWidthProperty().bind(loadingPane.widthProperty().multiply(0.15));  // 15% del ancho
            spinner.prefHeightProperty().bind(loadingPane.heightProperty().multiply(0.15)); // 15% del alto
            spinner.setProgress(-1); // Animación continua
            spinner.setStyle("-fx-stroke: white;"); // Color blanco para mejor visibilidad
            
            // Etiqueta de "Cargando..." con tamaño proporcional y color verde
            Label cargandoLabel = new Label("Cargando...");
            cargandoLabel.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ", loadingPane.widthProperty().multiply(0.035).asString(), "px; ",
                "-fx-font-weight: bold; -fx-text-fill: #0F9D58;"
            ));
            
            // Añadir un fondo semi-transparente detrás del spinner y texto para mejor visibilidad
            StackPane spinnerBackground = new StackPane();
            spinnerBackground.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // Quitar el borde redondeado
            // El contenedor del spinner también se redimensiona proporcionalmente
            spinnerBackground.prefWidthProperty().bind(loadingPane.widthProperty().multiply(0.3));  // 30% del ancho
            spinnerBackground.prefHeightProperty().bind(loadingPane.heightProperty().multiply(0.3)); // 30% del alto
            
            // Organizar elementos verticalmente
            VBox spinnerBox = new VBox();
            spinnerBox.setAlignment(Pos.CENTER);
            spinnerBox.spacingProperty().bind(loadingPane.heightProperty().multiply(0.02)); // 2% del alto
            spinnerBox.setPadding(new Insets(20));
            spinnerBox.getChildren().addAll(spinner, cargandoLabel);
            
            // Añadir el panel de spinner al panel con fondo semi-transparente
            spinnerBackground.getChildren().add(spinnerBox);
            
            // Añadir todos los elementos al panel de carga
            loadingPane.getChildren().addAll(backgroundImage, spinnerBackground);
            
            // Asegurar que el spinner esté en el centro
            StackPane.setAlignment(spinnerBackground, Pos.CENTER);
            
            // Guardar el contenido original del tab
            Node contenidoOriginal = tabDiagnosticos.getContent();
            
            // Mostrar el panel de carga
            tabDiagnosticos.setContent(loadingPane);
            
            // Cargar el formulario en un hilo separado para no bloquear la interfaz
            new Thread(() -> {
                try {
                    // Cargar la vista de diagnóstico
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Diagnostico/diagnostico-view.fxml"));
                    Parent root = loader.load();
                    
                    // Obtener el controlador
                    com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico.DiagnosticoController controller = loader.getController();
                    
                    // Verificar si hay un paciente seleccionado en la pestaña de pacientes
                    ModeloPaciente pacienteSeleccionado = tablaPacientes.getSelectionModel().getSelectedItem();
                    
                    // Configurar un manejador para volver a la vista original
                    Runnable volverAVistaPrincipal = () -> {
                        // Restaurar el contenido original y actualizar la tabla
                        tabDiagnosticos.setContent(contenidoOriginal);
                        buscarDiagnosticos();
                    };
                    
                    // Ejecutar en el hilo de la interfaz de usuario
                    Platform.runLater(() -> {
                        try {
                            // Si hay un paciente seleccionado, asignarlo
                            if (pacienteSeleccionado != null) {
                                // Obtener la cita más reciente del paciente
                                try {
                                    gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_CITAS_POR_PACIENTE + Protocolo.SEPARADOR_CODIGO + pacienteSeleccionado.getId());
                                    
                                    ObjectInputStream entradaCitasPaciente = gestorPeticiones.getEntrada();
                                    if (entradaCitasPaciente.readInt() == Protocolo.BUSCAR_CITAS_POR_PACIENTE_RESPONSE) {
                                        List<ModeloCita> citas = (List<ModeloCita>) entradaCitasPaciente.readObject();
                                        ModeloCita cita = citas.isEmpty() ? null : citas.get(0);
                                        controller.setPaciente(pacienteSeleccionado, cita);
                                    } else {
                                        // Si no se pueden obtener las citas, continuar sin cita
                                        controller.setPaciente(pacienteSeleccionado, null);
                                    }
                                } catch (IOException | ClassNotFoundException ex) {
                                    ex.printStackTrace();
                                    // Si hay error, continuar sin cita
                                    controller.setPaciente(pacienteSeleccionado, null);
                                }
                            }
                            
                            // Configurar callbacks
                            controller.setOnGuardarCallback(volverAVistaPrincipal);
                            controller.setOnCancelarCallback(volverAVistaPrincipal);
                            
                            // Reemplazar el contenido del tab con el formulario
                            tabDiagnosticos.setContent(root);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            mostrarAlerta("Error", "Error al configurar formulario", 
                                    "Ha ocurrido un error al configurar el formulario: " + ex.getMessage());
                            volverAVistaPrincipal.run();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        mostrarAlerta("Error", "Error al cargar formulario", 
                                "Ha ocurrido un error al cargar el formulario: " + e.getMessage());
                        tabDiagnosticos.setContent(contenidoOriginal);
                    });
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error inesperado", 
                    "Ha ocurrido un error inesperado: " + e.getMessage());
        }
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
                //Hacemos una peticion al servidor para eliminar el diagnostico
                try {
                    gestorPeticiones.enviarPeticion(Protocolo.ELIMINARDIAGNOSTICO + Protocolo.SEPARADOR_CODIGO + diagnostico.getId());

                    ObjectInputStream entrada = gestorPeticiones.getEntrada();
                    if (entrada.readInt() == Protocolo.ELIMINARDIAGNOSTICO_RESPONSE) {
                        boolean eliminado = true;
                        buscarDiagnosticos();
                        mostrarMensaje("Diagnóstico eliminado", "El diagnóstico ha sido eliminado", 
                                "El diagnóstico ha sido eliminado exitosamente.");
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el diagnóstico",
                                "Ha ocurrido un error al intentar eliminar el diagnóstico.");
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
    
    /**
     * Selecciona la pestaña de citas programáticamente
     */
    public void seleccionarTabCitas() {
        // Seleccionar la pestaña de citas (índice 3 - es la cuarta pestaña)
        tabPane.getSelectionModel().select(tabCitas);
    }
    
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
     * Habilita o deshabilita la edición en línea de un paciente
     * @param paciente El paciente a editar
     * @param esNuevo Indica si es un registro nuevo o existente
     */
    private void habilitarEdicionPaciente(ModeloPaciente paciente, boolean esNuevo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/paciente-edit-row.fxml"));
            Parent contenido = loader.load();
            
            PacienteEditRowController controlador = loader.getController();
            controlador.configurar( paciente, esNuevo, (pacienteEditado, confirmado) -> {
                if (confirmado) {
                    // Si se confirmó la edición, guardar el paciente
                    try {
                        //Pedir al servidor guardar el paciente
                        gestorPeticiones.enviarPeticion(Protocolo.CREARPACIENTE + Protocolo.SEPARADOR_CODIGO);
                        ObjectOutputStream salida = gestorPeticiones.getSalida();
                        salida.writeObject(pacienteEditado);
                        salida.flush();

                        ObjectInputStream entrada = gestorPeticiones.getEntrada();
                        ObjectId pacienteId = (ObjectId) entrada.readObject();
                        if (pacienteId != null) {
                            // Refrescar datos
                            cargarPacientes();
                            mostrarMensaje("Éxito", "Paciente guardado", 
                                "El paciente ha sido guardado correctamente.");
                        }
                        else {
                            mostrarAlerta("Error", "Error al guardar", 
                                "Ha ocurrido un error al guardar el paciente: " );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Error al guardar", 
                            "Ha ocurrido un error al guardar el paciente: " + e.getMessage());
                    }
                } else {
                    // Si se canceló, eliminar de la lista si era nuevo
                    if (esNuevo) {
                        pacientesObservable.remove(paciente);
                    } else {
                        // Si no era nuevo, refrescar para descartar cambios
                        cargarPacientes();
                    }
                }
            });
            
            // Crear y mostrar diálogo
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(esNuevo ? "Nuevo Paciente" : "Editar Paciente");
            dialogStage.setScene(new Scene(contenido));
            
            // Mostrar y esperar
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir editor", 
                "Ha ocurrido un error al abrir el editor de paciente: " + e.getMessage());
        }
    }
    
    /**
     * Habilita o deshabilita la edición en línea de un propietario
     * @param propietario El propietario a editar
     * @param esNuevo Indica si es un registro nuevo o existente
     */
    private void habilitarEdicionPropietario(ModeloPropietario propietario, boolean esNuevo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/propietario-edit-row.fxml"));
            Parent contenido = loader.load();
            
            PropietarioEditRowController controlador = loader.getController();
            controlador.configurar( propietario, esNuevo, (propietarioEditado, confirmado) -> {
                if (confirmado) {
                    // Si se confirmó la edición, guardar el propietario
                    try {
                        //Pedir al servidor guardar el propietario
                        gestorPeticiones.enviarPeticion(Protocolo.CREARPROPIETARIO + Protocolo.SEPARADOR_CODIGO);
                        ObjectOutputStream salida = gestorPeticiones.getSalida();
                        salida.writeObject(propietarioEditado);
                        salida.flush();

                        ObjectInputStream entrada = gestorPeticiones.getEntrada();
                        if (entrada.readInt() == Protocolo.CREARPROPIETARIO_RESPONSE) {
                            ObjectId propietarioId = (ObjectId) entrada.readObject();
                            if (propietarioId != null) {
                                // Refrescar datos
                                cargarPropietarios();
                                mostrarMensaje("Éxito", "Propietario guardado", 
                                    "El propietario ha sido guardado correctamente.");
                            }
                        }
                        else {
                            mostrarAlerta("Error", "Error al guardar", 
                                "Ha ocurrido un error al guardar el propietario: " );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Error al guardar", 
                            "Ha ocurrido un error al guardar el propietario: " + e.getMessage());
                    }
                } else {
                    // Si se canceló, eliminar de la lista si era nuevo
                    if (esNuevo) {
                        propietariosObservable.remove(propietario);
                    } else {
                        // Si no era nuevo, refrescar para descartar cambios
                        cargarPropietarios();
                    }
                }
            });
            
            // Crear y mostrar diálogo
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(esNuevo ? "Nuevo Propietario" : "Editar Propietario");
            dialogStage.setScene(new Scene(contenido));
            
            // Mostrar y esperar
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir editor", 
                "Ha ocurrido un error al abrir el editor de propietario: " + e.getMessage());
        }
    }
    
    /**
     * Habilita la edición directamente en la fila de la tabla para un paciente
     * @param paciente El paciente a editar
     * @param esNuevo Indica si es un registro nuevo o existente
     */
    private void habilitarEdicionFilaPaciente(ModeloPaciente paciente, boolean esNuevo) {
        try {
            // Aquí implementaremos la edición en la fila directamente
            // en lugar de abrir un diálogo
            
            // Identificar la fila en la tabla
            int index = -1;
            for (int i = 0; i < pacientesObservable.size(); i++) {
                if (pacientesObservable.get(i) == paciente) {
                    index = i;
                    break;
                }
            }
            
            if (index >= 0) {
                // Seleccionar la fila para edición
                tablaPacientes.getSelectionModel().select(index);
                tablaPacientes.scrollTo(index);
                
                // Idealmente, aquí activaríamos un modo de edición especial en la fila
                // Por ahora, seguimos usando el formulario como solución temporal
                habilitarEdicionPaciente(paciente, esNuevo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al habilitar edición", 
                "Ha ocurrido un error al intentar habilitar la edición: " + e.getMessage());
        }
    }
    
    /**
     * Habilita la edición directamente en la fila de la tabla para un propietario
     * @param propietario El propietario a editar
     * @param esNuevo Indica si es un registro nuevo o existente
     */
    private void habilitarEdicionFilaPropietario(ModeloPropietario propietario, boolean esNuevo) {
        try {
            // Aquí implementaremos la edición en la fila directamente
            // en lugar de abrir un diálogo
            
            // Identificar la fila en la tabla
            int index = -1;
            for (int i = 0; i < propietariosObservable.size(); i++) {
                if (propietariosObservable.get(i) == propietario) {
                    index = i;
                    break;
                }
            }
            
            if (index >= 0) {
                // Seleccionar la fila para edición
                tablaPropietarios.getSelectionModel().select(index);
                tablaPropietarios.scrollTo(index);
                
                // Idealmente, aquí activaríamos un modo de edición especial en la fila
                // Por ahora, seguimos usando el formulario como solución temporal
                habilitarEdicionPropietario(propietario, esNuevo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al habilitar edición", 
                "Ha ocurrido un error al intentar habilitar la edición: " + e.getMessage());
        }
    }

    /**
     * Muestra las citas de un paciente
     */
    private void verCitasPaciente(ModeloPaciente paciente) {
        if (paciente != null) {
            // Navegar a la pestaña de citas
            tabPane.getSelectionModel().select(tabCitas);
            
            // Filtrar citas por paciente (implementar esta función en CitasController)
            if (citasController != null) {
                citasController.filtrarPorPaciente(paciente.getId());
            } else {
                // Mostrar mensaje si el controlador no está disponible
                mostrarMensaje("Citas del paciente", "Filtrar citas", 
                        "Mostrando citas para el paciente: " + paciente.getNombre());
            }
        }
    }

    /**
     * Muestra las mascotas de un propietario
     */
    private void verMascotasPropietario(ModeloPropietario propietario) {
        if (propietario != null) {
            // Navegar a la pestaña de pacientes
            tabPane.getSelectionModel().select(tabPacientes);
            
            // Buscar los pacientes de este propietario
            pacientesObservable.clear();
            try {
                gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_PACIENTES_POR_PROPIETARIO + Protocolo.SEPARADOR_CODIGO + propietario.getId());
                
                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.BUSCAR_PACIENTES_POR_PROPIETARIO_RESPONSE) {
                    List<ModeloPaciente> mascotas = (List<ModeloPaciente>) entrada.readObject();
                    pacientesObservable.addAll(mascotas);
                    
                    // Si no hay mascotas, mostrar mensaje
                    if (mascotas.isEmpty()) {
                        mostrarMensaje("Sin mascotas", "No hay mascotas para este propietario", 
                                "El propietario " + propietario.getNombreCompleto() + " no tiene mascotas registradas.");
                    }
                } else {
                    mostrarAlerta("Error", "Error al obtener mascotas",
                            "No se pudieron obtener las mascotas del propietario.");
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                mostrarAlerta("Error", "Error al obtener mascotas",
                        "Ha ocurrido un error al obtener las mascotas: " + ex.getMessage());
            }
        }
    }

    /**
     * Abre el selector de propietarios para asignar a un paciente
     */
    private void seleccionarPropietario(ModeloPaciente paciente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/propietario-selector.fxml"));
            Parent root = loader.load();
            PropietarioSelectorController controller = loader.getController();
            // TODO: Implementar setServicio con gestorPeticiones en lugar de servicioClinica
            // controller.setServicio(servicioClinica);

            controller.setPropietarioSeleccionadoCallback(propietario -> {
                // Asignar el propietario al paciente
                paciente.setPropietarioId(propietario.getId());
                paciente.setNombrePropietario(propietario.getNombreCompleto());
                tablaPacientes.refresh();
            });

            Stage stage = new Stage();
            stage.setTitle("Seleccionar Propietario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir selector de propietarios", 
                    "Ha ocurrido un error al intentar abrir el selector de propietarios: " + e.getMessage());
        }
    }

    /**
     * Actualiza el modo de edición de un propietario
     */
    private void actualizarModoPropietario(ModeloPropietario propietario) {
        propietariosObservable.set(propietariosObservable.indexOf(propietario), propietario);
        tablaPropietarios.refresh();
    }
    
    /**
     * Configura la tabla de diagnósticos
     */
    private void configurarTablaDiagnosticos() {
        // Hacer que la tabla sea responsive
        tablaDiagnosticos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Configurar propiedades de columnas para que sean responsive
        configurarColumnaCabecera(colFechaDiagnostico, "Fecha", 0.15, tablaDiagnosticos);
        configurarColumnaCabecera(colPacienteDiagnostico, "Paciente", 0.2, tablaDiagnosticos);
        configurarColumnaCabecera(colMotivo, "Motivo\nConsulta", 0.2, tablaDiagnosticos);
        configurarColumnaCabecera(colDiagnostico, "Diagnóstico", 0.3, tablaDiagnosticos);
        configurarColumnaCabecera(colVeterinario, "Veterinario", 0.15, tablaDiagnosticos);
        
        colFechaDiagnostico.setCellValueFactory(data -> {
            Date fecha = data.getValue().getFecha();
            return new SimpleStringProperty(fecha != null ? formatoFecha.format(fecha) : "");
        });
        colPacienteDiagnostico.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombrePaciente()));
        colMotivo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMotivo()));
        colDiagnostico.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDiagnostico()));
        colVeterinario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVeterinario()));
        
        tablaDiagnosticos.setItems(diagnosticosObservable);
        
        // Manejar doble clic en un diagnóstico
        tablaDiagnosticos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaDiagnosticos.getSelectionModel().getSelectedItem() != null) {
                abrirDetallesDiagnostico(tablaDiagnosticos.getSelectionModel().getSelectedItem());
            }
        });
        
        // Configurar ComboBox de pacientes
        configurarComboBoxPacientes();
    }
    
    /**
     * Configura el ComboBox de pacientes para filtrar diagnósticos
     */
    private void configurarComboBoxPacientes() {
        try {
            // Personalizar la visualización de los pacientes en el ComboBox
            cmbPacientesDiagnostico.setCellFactory(lv -> new ListCell<ModeloPaciente>() {
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
            
            // Configurar celda del botón
            cmbPacientesDiagnostico.setButtonCell(new ListCell<ModeloPaciente>() {
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
            
            // Cargar todos los pacientes en el ComboBox
            //Pedir al servidor todos los pacientes
            gestorPeticiones.enviarPeticion(Protocolo.OBTENER_TODOS_PACIENTES + Protocolo.SEPARADOR_CODIGO);
            ObjectInputStream entrada = gestorPeticiones.getEntrada();
            if (entrada.readInt() == Protocolo.OBTENER_TODOS_PACIENTES_RESPONSE) {
                List<ModeloPaciente> pacientes = (List<ModeloPaciente>) entrada.readObject();
                cmbPacientesDiagnostico.setItems(FXCollections.observableArrayList(pacientes));
            }
            else {
                mostrarAlerta("Error", "Error al obtener pacientes", "No se pudieron obtener los pacientes.");
            }
            // Manejar cambio de selección en el ComboBox
            cmbPacientesDiagnostico.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    filtrarDiagnosticosPorPaciente(newVal.getId());
                }
            });
        } catch (IOException | ClassNotFoundException ex) {
        }
    }
    
    /**
     * Limpia el filtro de pacientes y muestra todos los diagnósticos
     */
    @FXML
    private void onLimpiarFiltroDiagnostico() {
        cmbPacientesDiagnostico.getSelectionModel().clearSelection();
        buscarDiagnosticos();
    }
    
    /**
     * Filtra los diagnósticos por el paciente seleccionado
     * @param pacienteId ID del paciente para filtrar
     */
    private void filtrarDiagnosticosPorPaciente(ObjectId pacienteId) {
        if (pacienteId != null) {
            try {
                diagnosticosObservable.clear();
                //Pedir al servidor los diagnosticos del paciente
                gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_DIAGNOSTICOS_POR_PACIENTE + Protocolo.SEPARADOR_CODIGO + pacienteId);
                ObjectInputStream entrada = gestorPeticiones.getEntrada();
                if (entrada.readInt() == Protocolo.BUSCAR_DIAGNOSTICOS_POR_PACIENTE_RESPONSE) {
                    List<ModeloDiagnostico> diagnosticos = (List<ModeloDiagnostico>) entrada.readObject();
                    diagnosticosObservable.addAll(diagnosticos);
                }
                else {
                    mostrarAlerta("Error", "Error al filtrar diagnosticos",
                            "No se pudieron filtrar los diagnosticos del paciente.");
                }
            } catch (IOException | ClassNotFoundException ex) {
            }
        }
    }

    /**
     * Ajusta las tablas cuando cambia el tamaño de la ventana para garantizar 
     * que sean responsivas.
     */
    private void ajustarTablasResponsivas() {
        // Aplicar el policy de redimensionamiento a todas las tablas
        tablaPacientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaPropietarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDiagnosticos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Forzar actualización de las tablas
        tablaPacientes.refresh();
        tablaPropietarios.refresh();
        tablaDiagnosticos.refresh();
    }

    /**
     * Muestra un diálogo para gestionar las vacunas de un paciente
     */
    private void mostrarVacunasPaciente(ModeloPaciente paciente) {
        try {
            // Crear una lista observable de las vacunas
            ObservableList<String> vacunasObservable = FXCollections.observableArrayList(paciente.getVacunas());
            
            // Crear el diálogo
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Vacunas de " + paciente.getNombre());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            
            // Crear los componentes de la interfaz
            ListView<String> listView = new ListView<>(vacunasObservable);
            listView.setEditable(true);
            listView.setPrefHeight(300);
            listView.setPrefWidth(400);
            
            TextField txtNuevaVacuna = new TextField();
            txtNuevaVacuna.setPromptText("Nueva vacuna");
            txtNuevaVacuna.setPrefWidth(300);
            
            Button btnAgregar = new Button("Agregar");
            btnAgregar.getStyleClass().add("btn-primary");
            Button btnEliminar = new Button("Eliminar seleccionada");
            btnEliminar.getStyleClass().add("btn-danger");
            Button btnGuardar = new Button("Guardar cambios");
            btnGuardar.getStyleClass().add("btn-success");
            
            // Configurar eventos
            btnAgregar.setOnAction(e -> {
                String nuevaVacuna = txtNuevaVacuna.getText().trim();
                if (!nuevaVacuna.isEmpty() && !vacunasObservable.contains(nuevaVacuna)) {
                    vacunasObservable.add(nuevaVacuna);
                    txtNuevaVacuna.clear();
                }
            });
            
            btnEliminar.setOnAction(e -> {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    vacunasObservable.remove(selectedIndex);
                }
            });
            
            btnGuardar.setOnAction(e -> {
                try {
                    // Actualizar la lista de vacunas del paciente
                    paciente.setVacunas(new ArrayList<>(vacunasObservable));
                    
                    // Guardar el paciente en la base de datos
                    //Pedir al servidor actualizar el paciente
                    gestorPeticiones.enviarPeticion(Protocolo.ACTUALIZARPACIENTE + Protocolo.SEPARADOR_CODIGO);
                    ObjectOutputStream salida = gestorPeticiones.getSalida();
                    salida.writeObject(paciente);
                    salida.flush();
                    
                    boolean actualizado = false;
                    
                    ObjectInputStream entrada = gestorPeticiones.getEntrada();
                    if (entrada.readInt() == Protocolo.ACTUALIZARPACIENTE_RESPONSE) {
                        actualizado = true;
                    }
                    else {
                        actualizado = false;
                    }
                    
                    
                    if (actualizado) {
                        mostrarMensaje("Éxito", "Vacunas guardadas",
                                "Las vacunas del paciente han sido actualizadas correctamente.");
                        // Actualizar la vista
                        cargarPacientes();
                    } else {
                        mostrarAlerta("Error", "Error al guardar vacunas",
                                "No se pudieron guardar las vacunas del paciente.");
                    }
                    
                    dialogStage.close();
                } catch (IOException ex) {
                }
            });
            
            // Crear el layout
            HBox hboxInput = new HBox(10, txtNuevaVacuna, btnAgregar);
            hboxInput.setPadding(new Insets(10));
            hboxInput.setAlignment(Pos.CENTER_LEFT);
            
            HBox hboxButtons = new HBox(10, btnEliminar, btnGuardar);
            hboxButtons.setPadding(new Insets(10));
            hboxButtons.setAlignment(Pos.CENTER_RIGHT);
            
            VBox vbox = new VBox(10, new Label("Vacunas"), listView, hboxInput, hboxButtons);
            vbox.setPadding(new Insets(15));
            
            // Configurar y mostrar el diálogo
            Scene scene = new Scene(vbox);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al mostrar vacunas", 
                    "Ha ocurrido un error al intentar mostrar las vacunas: " + e.getMessage());
        }
    }

    /**
     * Muestra un diálogo para gestionar las alergias de un paciente
     */
    private void mostrarAlergiasPaciente(ModeloPaciente paciente) {
        try {
            // Crear una lista observable de las alergias
            ObservableList<String> alergiasObservable = FXCollections.observableArrayList(paciente.getAlergias());
            
            // Crear el diálogo
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Alergias de " + paciente.getNombre());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            
            // Crear los componentes de la interfaz
            ListView<String> listView = new ListView<>(alergiasObservable);
            listView.setEditable(true);
            listView.setPrefHeight(300);
            listView.setPrefWidth(400);
            
            TextField txtNuevaAlergia = new TextField();
            txtNuevaAlergia.setPromptText("Nueva alergia");
            txtNuevaAlergia.setPrefWidth(300);
            
            Button btnAgregar = new Button("Agregar");
            btnAgregar.getStyleClass().add("btn-primary");
            Button btnEliminar = new Button("Eliminar seleccionada");
            btnEliminar.getStyleClass().add("btn-danger");
            Button btnGuardar = new Button("Guardar cambios");
            btnGuardar.getStyleClass().add("btn-success");
            
            // Configurar eventos
            btnAgregar.setOnAction(e -> {
                String nuevaAlergia = txtNuevaAlergia.getText().trim();
                if (!nuevaAlergia.isEmpty() && !alergiasObservable.contains(nuevaAlergia)) {
                    alergiasObservable.add(nuevaAlergia);
                    txtNuevaAlergia.clear();
                }
            });
            
            btnEliminar.setOnAction(e -> {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    alergiasObservable.remove(selectedIndex);
                }
            });
            
            btnGuardar.setOnAction(e -> {
                try {
                    // Actualizar la lista de alergias del paciente
                    paciente.setAlergias(new ArrayList<>(alergiasObservable));
                    
                    // Guardar el paciente en la base de datos
                    
                    //Pedir al servidor actualizar el paciente
                    gestorPeticiones.enviarPeticion(Protocolo.ACTUALIZARPACIENTE + Protocolo.SEPARADOR_CODIGO);
                    ObjectOutputStream salida = gestorPeticiones.getSalida();
                    salida.writeObject(paciente);
                    salida.flush();
                    
                    boolean actualizado = false;
                    
                    ObjectInputStream entrada = gestorPeticiones.getEntrada();
                    if (entrada.readInt() == Protocolo.ACTUALIZARPACIENTE_RESPONSE) {
                        actualizado = true;
                    }
                    else {
                        actualizado = false;
                    }
                    
                    
                    if (actualizado) {
                        mostrarMensaje("Éxito", "Alergias guardadas",
                                "Las alergias del paciente han sido actualizadas correctamente.");
                        // Actualizar la vista
                        cargarPacientes();
                    } else {
                        mostrarAlerta("Error", "Error al guardar alergias",
                                "No se pudieron guardar las alergias del paciente.");
                    }
                    
                    dialogStage.close();
                } catch (IOException ex) {
                }
            });
            
            // Crear el layout
            HBox hboxInput = new HBox(10, txtNuevaAlergia, btnAgregar);
            hboxInput.setPadding(new Insets(10));
            hboxInput.setAlignment(Pos.CENTER_LEFT);
            
            HBox hboxButtons = new HBox(10, btnEliminar, btnGuardar);
            hboxButtons.setPadding(new Insets(10));
            hboxButtons.setAlignment(Pos.CENTER_RIGHT);
            
            VBox vbox = new VBox(10, new Label("Alergias"), listView, hboxInput, hboxButtons);
            vbox.setPadding(new Insets(15));
            
            // Configurar y mostrar el diálogo
            Scene scene = new Scene(vbox);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al mostrar alergias", 
                    "Ha ocurrido un error al intentar mostrar las alergias: " + e.getMessage());
        }
    }
} 