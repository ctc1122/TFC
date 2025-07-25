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

import com.example.pruebamongodbcss.Protocolo.Protocolo;

import Utilidades1.GestorSocket;
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
import javafx.scene.shape.SVGPath;
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
    @FXML private Button btnEliminarPaciente;
    @FXML private Button btnVerHistorial;
    
    // Tab de Propietarios
    @FXML private Tab tabPropietarios;
    @FXML private TableView<ModeloPropietario> tablaPropietarios;
    @FXML private TableColumn<ModeloPropietario, String> colNombre;
    @FXML private TableColumn<ModeloPropietario, String> colApellidos;
    @FXML private TableColumn<ModeloPropietario, String> colDNI;
    @FXML private TableColumn<ModeloPropietario, String> colTelefono;
    @FXML private TableColumn<ModeloPropietario, String> colEmail;
    @FXML private TextField txtBuscarPropietario;
    @FXML private Button btnNuevoPropietario;
    @FXML private Button btnEliminarPropietario;
    
    // Tab de Diagnósticos
    @FXML private Tab tabDiagnosticos;
    @FXML private TableView<ModeloDiagnostico> tablaDiagnosticos;
    @FXML private TableColumn<ModeloDiagnostico, String> colFechaDiagnostico;
    @FXML private TableColumn<ModeloDiagnostico, String> colPacienteDiagnostico;
    @FXML private TableColumn<ModeloDiagnostico, String> colMotivo;
    @FXML private TableColumn<ModeloDiagnostico, String> colDiagnostico;
    @FXML private TableColumn<ModeloDiagnostico, String> colVeterinario;
    @FXML private TableColumn<ModeloDiagnostico, Void> colAcciones;
    @FXML private MFXDatePicker dpFechaInicio;
    @FXML private MFXDatePicker dpFechaFin;
    @FXML private TextField txtBuscarDiagnostico;
    @FXML private Button btnBuscarDiagnostico;
    @FXML private Button btnEliminarDiagnostico;
    @FXML private ComboBox<ModeloPaciente> cmbPacientesDiagnostico;
    @FXML private Button btnLimpiarFiltro;
    @FXML private Button btnExportarPDFDiagnostico;
    @FXML private Button btnExportarCSVDiagnostico;
    
    // Servicio clínico
    //private ServicioClinica servicioClinica;
    
    // Listas observables para las tablas
    private ObservableList<ModeloPaciente> pacientesObservable;
    private ObservableList<ModeloPropietario> propietariosObservable;
    private ObservableList<ModeloDiagnostico> diagnosticosObservable;
    
    // Formato de fecha
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    
    // Controlador de citas
    //private CitasController citasController;

    //Lanza peticiones al servidor
    private GestorSocket gestorPeticiones;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar el servicio clínico
        //servicioClinica = new ServicioClinica();
        try {
            gestorPeticiones = GestorSocket.getInstance();
            
            // Verificar que la conexión esté establecida
            if (gestorPeticiones == null || !gestorPeticiones.isConectado()) {
                System.err.println("⚠️ Advertencia: No se pudo establecer conexión inicial con el servidor");
                Platform.runLater(() -> {
                    mostrarAlerta("Advertencia de conexión", "Conexión no disponible",
                            "No se pudo establecer conexión con el servidor al inicializar.\n" +
                            "Puede intentar recargar los datos usando los botones de la interfaz.");
                });
            }
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar gestor de peticiones: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                mostrarAlerta("Error de inicialización", "Error al conectar con servidor",
                        "Error al establecer conexión con el servidor: " + e.getMessage() + 
                        "\n\nAlgunas funciones pueden no estar disponibles.");
            });
        }
        
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
        
        // Cargar datos iniciales de forma asíncrona para no bloquear la UI
        Platform.runLater(() -> {
            cargarDatosIniciales();
        });
        
        // Configurar eventos de búsqueda
        configurarEventosBusqueda();
        
        /*// Cargar la vista de citas de forma proactiva
        // Esto asegura que esté listo antes de que el usuario haga clic en la pestaña
        cargarVistaCitas();
        
        // Configurar cambio de pestañas
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabCitas) {
                // Asegurarse de que la vista de citas esté cargada cuando se selecciona la pestaña
                cargarVistaCitas();
            }
        });*/
    }
    
    /**
     * Carga los datos iniciales de forma controlada
     */
    private void cargarDatosIniciales() {
        // Cargar datos iniciales solo si hay conexión
        if (gestorPeticiones != null && gestorPeticiones.isConectado()) {
            cargarPacientes();
            cargarPropietarios();
            
            // Inicializar fechas de búsqueda de diagnósticos
            LocalDate hoy = LocalDate.now();
            dpFechaFin.setValue(hoy);
            dpFechaInicio.setValue(hoy.minusDays(30));
            
            // Buscar diagnósticos iniciales
            buscarDiagnosticos();
        } else {
            System.out.println("⚠️ Saltando carga de datos iniciales por falta de conexión");
        }
    }
    
    /**
     * Carga la vista de citas
     */
    /*private void cargarVistaCitas() {
        try {
            // Solo cargar si no está ya cargada
            if (citasContainer.getCenter() == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/citas-view.fxml"));
                Parent root = loader.load();
                //citasController = loader.getController();
                
                citasContainer.setCenter(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar vista de citas", 
                    "Ha ocurrido un error al intentar cargar la vista de citas: " + e.getMessage());
        }
    }*/
    
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
                
                // Crear y añadir icono SVG blanco de "más"
                SVGPath iconoMas = new SVGPath();
                iconoMas.setContent("M12 5v6m0 0v6m0-6h6m-6 0H6");
                iconoMas.setFill(javafx.scene.paint.Color.WHITE);
                iconoMas.setStroke(javafx.scene.paint.Color.WHITE);
                iconoMas.setStrokeWidth(2);
                iconoMas.setScaleX(0.6);
                iconoMas.setScaleY(0.6);
                btnSeleccionar.setGraphic(iconoMas);
                btnSeleccionar.setText(""); // Quitar el texto y solo mostrar el icono
                
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
                        
                        try {
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
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                            mostrarAlerta("Error", "Error al guardar paciente",
                                    "Ha ocurrido un error al intentar guardar el paciente: " + e.getMessage());
                        }
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
                            } catch (ClassNotFoundException | IOException e) {
                               
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
        tablaPropietarios.setEditable(true);
        tablaPropietarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        configurarColumnaCabecera(colNombre, "Nombre", 0.15, tablaPropietarios);
        configurarColumnaCabecera(colApellidos, "Apellidos", 0.15, tablaPropietarios);
        configurarColumnaCabecera(colDNI, "DNI/NIF", 0.15, tablaPropietarios);
        configurarColumnaCabecera(colTelefono, "Teléfono", 0.15, tablaPropietarios);
        configurarColumnaCabecera(colEmail, "Email", 0.15, tablaPropietarios);

        // Configurar las celdas para mostrar los datos
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colApellidos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApellidos()));
        colDNI.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDni()));
        colTelefono.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefono()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        // Configurar las celdas para ser editables
        colNombre.setCellFactory(column -> new TableCell<ModeloPropietario, String>() {
            private TextField textField;
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    if (textField == null) {
                        createTextField();
                    }
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
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
                    }
                }
            }
            private void createTextField() {
                textField = new TextField(getString());
                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                textField.focusedProperty().addListener((arg0, arg1, arg2) -> {
                    if (!arg2) {
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
                propietario.setNombre(newValue);
            }
        });

        colApellidos.setCellFactory(column -> new TableCell<ModeloPropietario, String>() {
            private TextField textField;
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    if (textField == null) {
                        createTextField();
                    }
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
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
                    }
                }
            }
            private void createTextField() {
                textField = new TextField(getString());
                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                textField.focusedProperty().addListener((arg0, arg1, arg2) -> {
                    if (!arg2) {
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
                propietario.setApellidos(newValue);
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
            tablaPropietarios.edit(0, colNombre);
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
    
    // ********** CARGA DE DATOS **********
    private void cargarPacientes() {
        pacientesObservable.clear();
        try {
            // Verificar la conexión primero
            if (gestorPeticiones == null) {
                mostrarAlerta("Error de conexión", "Sin conexión al servidor",
                        "No se ha establecido conexión con el servidor. Inténtelo más tarde.");
                return;
            }
            
            // Verificar que el gestor esté conectado
            if (!gestorPeticiones.isConectado()) {
                mostrarAlerta("Error de conexión", "Conexión perdida",
                        "Se ha perdido la conexión con el servidor. Verificando reconexión...");
                
                // Intentar reconectar
                try {
                    gestorPeticiones = GestorSocket.getInstance();
                    Thread.sleep(1000); // Esperar un poco para la reconexión
                    
                    if (!gestorPeticiones.isConectado()) {
                        mostrarAlerta("Error de conexión", "No se pudo reconectar",
                                "No se pudo reestablecer la conexión con el servidor.");
                        return;
                    }
                } catch (Exception reconectEx) {
                    mostrarAlerta("Error de reconexión", "Error al intentar reconectar",
                            "Error al intentar reconectar: " + reconectEx.getMessage());
                    return;
                }
            }
            
            //Hacemos una peticion al servidor para obtener todos los pacientes
            gestorPeticiones.enviarPeticion(Protocolo.OBTENER_TODOS_PACIENTES + Protocolo.SEPARADOR_CODIGO);

            ObjectInputStream entrada = gestorPeticiones.getEntrada();
            if (entrada == null) {
                mostrarAlerta("Error de comunicación", "Stream de entrada nulo",
                        "No se pudo obtener el stream de entrada del servidor.");
                return;
            }
            
            int codigoRespuesta = entrada.readInt();
            if (codigoRespuesta == Protocolo.OBTENER_TODOS_PACIENTES_RESPONSE) {
                List<ModeloPaciente> pacientes = (List<ModeloPaciente>) entrada.readObject();
                if (pacientes != null) {
                    pacientesObservable.addAll(pacientes);
                    System.out.println("✅ Pacientes cargados exitosamente: " + pacientes.size());
                } else {
                    mostrarAlerta("Advertencia", "Lista de pacientes vacía",
                            "El servidor devolvió una lista vacía de pacientes.");
                }
            } else {
                mostrarAlerta("Error", "Error al obtener los pacientes",
                        "El servidor respondió con código de error: " + codigoRespuesta + 
                        ". No se pudieron obtener los pacientes. Inténtelo de nuevo.");
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
            tablaPropietarios.edit(0, colNombre);
        });
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
        try {
            // Cargar la vista de diagnóstico
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Diagnostico/diagnostico-view.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador
            com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico.DiagnosticoController controller = loader.getController();
            
            // Hacer las variables finales para usar en las lambdas
            final ModeloDiagnostico diagnosticoFinal = diagnostico;
            
            // Crear una nueva ventana
            Stage nuevaVentana = new Stage();
            nuevaVentana.setTitle("Diagnóstico - " + diagnostico.getNombrePaciente());
            nuevaVentana.initModality(Modality.NONE); // Ventana independiente, no modal
            nuevaVentana.setResizable(true);
            
            // Establecer el icono de la ventana
            try {
                Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
                nuevaVentana.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono de la ventana: " + e.getMessage());
            }
            
            // Crear la escena
            Scene scene = new Scene(root, 1200, 800); // Tamaño inicial de la ventana
            nuevaVentana.setScene(scene);
            
            // Buscar los datos del paciente y cita en un hilo separado
            new Thread(() -> {
                try {
                    // Buscar el paciente asociado al diagnóstico
                    gestorPeticiones.enviarPeticion(Protocolo.OBTENERPACIENTE_POR_ID + Protocolo.SEPARADOR_CODIGO + diagnosticoFinal.getPacienteId());
                    
                    ObjectInputStream entrada = gestorPeticiones.getEntrada();
                    if (entrada.readInt() == Protocolo.OBTENERPACIENTE_POR_ID_RESPONSE) {
                        ModeloPaciente paciente = (ModeloPaciente) entrada.readObject();
                        
                        if (paciente != null) {
                            // Buscar las citas del paciente para obtener la más reciente o relacionada
                            ModeloCita cita = null;
                            try {
                                gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_CITAS_POR_PACIENTE + Protocolo.SEPARADOR_CODIGO + paciente.getId());
                                
                                ObjectInputStream entradaCitas = gestorPeticiones.getEntrada();
                                if (entradaCitas.readInt() == Protocolo.BUSCAR_CITAS_POR_PACIENTE_RESPONSE) {
                                    List<ModeloCita> citas = (List<ModeloCita>) entradaCitas.readObject();
                                    
                                    // Si el diagnóstico tiene una cita asociada, buscarla específicamente
                                    if (diagnosticoFinal.getCitaId() != null && !citas.isEmpty()) {
                                        cita = citas.stream()
                                            .filter(c -> c.getId().equals(diagnosticoFinal.getCitaId()))
                                            .findFirst()
                                            .orElse(citas.get(0)); // Si no se encuentra, usar la primera
                                    } else if (!citas.isEmpty()) {
                                        cita = citas.get(0); // Usar la cita más reciente
                                    }
                                }
                            } catch (Exception ex) {
                                System.err.println("No se pudieron obtener las citas del paciente: " + ex.getMessage());
                            }
                            
                            // Hacer las variables finales para las lambdas
                            final ModeloPaciente pacienteFinal = paciente;
                            final ModeloCita citaFinal = cita;
                            
                            // Ejecutar en el hilo de la interfaz
                            Platform.runLater(() -> {
                                try {
                                    // Configurar el controlador con los datos
                                    controller.setPaciente(pacienteFinal, citaFinal);
                                    controller.setDiagnostico(diagnosticoFinal);
                                    
                                    // Configurar callback para cerrar la ventana al guardar/cancelar
                                    Runnable cerrarVentana = () -> {
                                        nuevaVentana.close();
                                        // Refrescar la tabla de diagnósticos en la ventana principal
                                        buscarDiagnosticos();
                                    };
                                    
                                    controller.setOnGuardarCallback(cerrarVentana);
                                    controller.setOnCancelarCallback(() -> nuevaVentana.close());
                                    
                                    // Actualizar el título de la ventana con el nombre del paciente
                                    nuevaVentana.setTitle("Diagnóstico - " + pacienteFinal.getNombre() + 
                                        (citaFinal != null ? " (" + citaFinal.getMotivo() + ")" : ""));
                                    
                                    System.out.println("✅ Diagnóstico cargado correctamente para paciente: " + pacienteFinal.getNombre());
                                    
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    mostrarAlerta("Error", "Error al configurar diagnóstico", 
                                            "Ha ocurrido un error al configurar la vista del diagnóstico: " + ex.getMessage());
                                    nuevaVentana.close();
                                }
                            });
                            
                        } else {
                            Platform.runLater(() -> {
                                mostrarAlerta("Error", "Paciente no encontrado",
                                        "No se pudo encontrar el paciente asociado a este diagnóstico.");
                                nuevaVentana.close();
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            mostrarAlerta("Error", "Error al obtener el paciente",
                                    "No se pudo obtener el paciente. Inténtelo de nuevo.");
                            nuevaVentana.close();
                        });
                    }
                    
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        mostrarAlerta("Error", "Error al cargar diagnóstico",
                                "Ha ocurrido un error al cargar la vista del diagnóstico: " + e.getMessage());
                        nuevaVentana.close();
                    });
                }
            }).start();
            
            // Mostrar la ventana inmediatamente (los datos se cargarán en segundo plano)
            nuevaVentana.show();
            
            // Centrar la ventana en pantalla
            nuevaVentana.centerOnScreen();
            
            System.out.println("✅ Nueva ventana de diagnóstico abierta");
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error inesperado", 
                    "Ha ocurrido un error inesperado al abrir el diagnóstico: " + e.getMessage());
        }
    }
    
    // ********** MÉTODOS DE UTILIDAD **********
    
    /**
     * Selecciona la pestaña de citas programáticamente
     */
    /*public void seleccionarTabCitas() {
        // Seleccionar la pestaña de citas (índice 3 - es la cuarta pestaña)
        tabPane.getSelectionModel().select(tabCitas);
    }*/
    
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
            try {
                // Buscar la ventana principal de la aplicación
                javafx.stage.Window ventanaPrincipal = mainPane.getScene().getWindow();
                javafx.scene.Scene scene = ventanaPrincipal.getScene();
                
                if (scene != null && scene.getRoot() instanceof BorderPane) {
                    BorderPane mainRoot = (BorderPane) scene.getRoot();
                    
                    // Buscar el BorderPane central donde se cargan los módulos
                    BorderPane centerPane = (BorderPane) mainRoot.getCenter();
                    if (centerPane != null) {
                        // Cargar directamente el módulo de citas (no el standalone)
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Clinica/Citas/citas-view.fxml"));
                        Parent citasView = loader.load();
                        
                        // Obtener directamente el CitasController
                        com.example.pruebamongodbcss.Modulos.Clinica.Citas.CitasController citasController = loader.getController();
                        
                        // Reemplazar el contenido central con el módulo de citas
                        centerPane.setCenter(citasView);
                        
                        // Actualizar el título si existe
                        javafx.scene.control.Label lblClinica = (javafx.scene.control.Label) mainRoot.lookup("#lblClinica");
                        if (lblClinica != null) {
                            lblClinica.setText("Gestión de Citas - " + paciente.getNombre());
                        }
                        
                        // Filtrar por paciente después de que la vista se haya cargado completamente
                        if (citasController != null) {
                            Platform.runLater(() -> {
                                try {
                                    System.out.println("🔍 Filtrando citas para paciente: " + paciente.getNombre() + " (ID: " + paciente.getId() + ")");
                                    citasController.filtrarPorPaciente(paciente.getId());
                                    mostrarMensaje("Filtro aplicado", 
                                            "Citas de " + paciente.getNombre(), 
                                            "Se han filtrado las citas para mostrar solo las de: " + paciente.getNombre());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println("❌ Error al filtrar por paciente: " + e.getMessage());
                                    mostrarAlerta("Error al filtrar", 
                                            "No se pudo filtrar automáticamente", 
                                            "Se ha navegado al módulo de citas, pero no se pudo filtrar automáticamente por el paciente.\n" +
                                            "Busque manualmente las citas de: " + paciente.getNombre());
                                }
                            });
                        } else {
                            mostrarAlerta("Error", 
                                    "Controlador no encontrado", 
                                    "Se cargó el módulo de citas pero no se pudo acceder al controlador para filtrar.");
                        }
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error al navegar a citas", 
                        "No se pudo navegar al módulo de citas: " + e.getMessage());
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
            
            // Ya no es necesario setServicio porque el controller usa GestorSocket directamente

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
        configurarColumnaCabecera(colFechaDiagnostico, "Fecha", 0.12, tablaDiagnosticos);
        configurarColumnaCabecera(colPacienteDiagnostico, "Paciente", 0.18, tablaDiagnosticos);
        configurarColumnaCabecera(colMotivo, "Motivo\nConsulta", 0.18, tablaDiagnosticos);
        configurarColumnaCabecera(colDiagnostico, "Diagnóstico", 0.25, tablaDiagnosticos);
        configurarColumnaCabecera(colVeterinario, "Veterinario", 0.15, tablaDiagnosticos);
        configurarColumnaCabecera(colAcciones, "Acciones", 0.12, tablaDiagnosticos);
        
        colFechaDiagnostico.setCellValueFactory(data -> {
            Date fecha = data.getValue().getFecha();
            return new SimpleStringProperty(fecha != null ? formatoFecha.format(fecha) : "");
        });
        colPacienteDiagnostico.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombrePaciente()));
        colMotivo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMotivo()));
        colDiagnostico.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDiagnostico()));
        colVeterinario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVeterinario()));
        
        // Configurar columna de acciones con botón "Ver"
        colAcciones.setCellFactory(col -> new TableCell<ModeloDiagnostico, Void>() {
            private final Button btnVer = new Button("Ver");
            
            {
                btnVer.setOnAction(event -> {
                    ModeloDiagnostico diagnostico = getTableView().getItems().get(getIndex());
                    if (diagnostico != null) {
                        abrirDetallesDiagnostico(diagnostico);
                    }
                });
                btnVer.getStyleClass().add("btn-info");
                btnVer.setPrefWidth(60);
                btnVer.setMaxWidth(60);
                btnVer.setMinWidth(60);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVer);
                }
            }
        });
        
        tablaDiagnosticos.setItems(diagnosticosObservable);
        
        // Manejar doble clic en un diagnóstico
        tablaDiagnosticos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaDiagnosticos.getSelectionModel().getSelectedItem() != null) {
                abrirDetallesDiagnostico(tablaDiagnosticos.getSelectionModel().getSelectedItem());
            }
        });
        
        // Configurar listener para habilitar/deshabilitar botones de exportación
        tablaDiagnosticos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean haySeleccion = newSelection != null;
            if (btnExportarPDFDiagnostico != null) {
                btnExportarPDFDiagnostico.setDisable(!haySeleccion);
            }
            if (btnExportarCSVDiagnostico != null) {
                btnExportarCSVDiagnostico.setDisable(!haySeleccion);
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
     * Exporta el diagnóstico seleccionado a PDF
     */
    @FXML
    private void onExportarPDFDiagnostico() {
        ModeloDiagnostico diagnosticoSeleccionado = tablaDiagnosticos.getSelectionModel().getSelectedItem();
        if (diagnosticoSeleccionado != null) {
            exportarDiagnosticoPDF(diagnosticoSeleccionado);
        } else {
            mostrarAlerta("Selección requerida", "No hay diagnóstico seleccionado",
                    "Por favor, seleccione un diagnóstico de la tabla para exportar a PDF.");
        }
    }
    
    /**
     * Exporta el diagnóstico seleccionado a CSV
     */
    @FXML
    private void onExportarCSVDiagnostico() {
        ModeloDiagnostico diagnosticoSeleccionado = tablaDiagnosticos.getSelectionModel().getSelectedItem();
        if (diagnosticoSeleccionado != null) {
            exportarDiagnosticoCSV(diagnosticoSeleccionado);
        } else {
            mostrarAlerta("Selección requerida", "No hay diagnóstico seleccionado",
                    "Por favor, seleccione un diagnóstico de la tabla para exportar a CSV.");
        }
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

    /**
     * Método público para recargar todos los datos desde la interfaz
     */
    @FXML
    public void recargarDatos() {
        System.out.println("🔄 Recargando datos manualmente...");
        
        // Reinicializar conexión si es necesario
        try {
            if (gestorPeticiones == null || !gestorPeticiones.isConectado()) {
                System.out.println("🔄 Reinicializando conexión...");
                gestorPeticiones = GestorSocket.getInstance();
                
                // Esperar un momento para que se establezca la conexión
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al reinicializar conexión: " + e.getMessage());
            mostrarAlerta("Error de conexión", "No se pudo reestablecer la conexión",
                    "Error al intentar reconectar con el servidor: " + e.getMessage());
            return;
        }
        
        // Limpiar listas
        Platform.runLater(() -> {
            pacientesObservable.clear();
            propietariosObservable.clear();
            diagnosticosObservable.clear();
        });
        
        // Recargar datos
        cargarDatosIniciales();
        
        System.out.println("✅ Recarga manual completada");
    }
    
    /**
     * Método específico para recargar solo pacientes
     */
    @FXML 
    public void recargarPacientes() {
        System.out.println("🔄 Recargando pacientes...");
        cargarPacientes();
    }
    
    /**
     * Método específico para recargar solo propietarios
     */
    @FXML
    public void recargarPropietarios() {
        System.out.println("🔄 Recargando propietarios...");
        cargarPropietarios();
    }
    
    /**
     * Refresca solo la tabla de propietarios
     */
    private void refreshTablaPropietarios() {
        Platform.runLater(() -> {
            try {
                cargarPropietarios();
                System.out.println("✅ Tabla de propietarios actualizada");
            } catch (Exception e) {
                System.err.println("❌ Error al refrescar tabla de propietarios: " + e.getMessage());
            }
        });
    }
    
    /**
     * Exporta un diagnóstico específico a PDF usando el DiagnosticoController
     */
    private void exportarDiagnosticoPDF(ModeloDiagnostico diagnostico) {
        try {
            // Crear una instancia del DiagnosticoController para usar sus métodos de exportación
            com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico.DiagnosticoController diagController = 
                new com.example.pruebamongodbcss.Modulos.Clinica.Diagnostico.DiagnosticoController();
            
            // Buscar el paciente asociado al diagnóstico
            ModeloPaciente paciente = null;
            for (ModeloPaciente p : pacientesObservable) {
                if (p.getId().equals(diagnostico.getPacienteId())) {
                    paciente = p;
                    break;
                }
            }
            
            if (paciente == null) {
                // Si no está en la lista observable, buscarlo en el servidor
                try {
                    gestorPeticiones.enviarPeticion(Protocolo.OBTENERPACIENTE_POR_ID + Protocolo.SEPARADOR_CODIGO + diagnostico.getPacienteId());
                    ObjectInputStream entrada = gestorPeticiones.getEntrada();
                    if (entrada.readInt() == Protocolo.OBTENERPACIENTE_POR_ID_RESPONSE) {
                        paciente = (ModeloPaciente) entrada.readObject();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            
            if (paciente == null) {
                mostrarAlerta("Error", "Paciente no encontrado", 
                        "No se pudo encontrar el paciente asociado a este diagnóstico.");
                return;
            }
            
            // Buscar la cita asociada si existe
            ModeloCita cita = null;
            if (diagnostico.getCitaId() != null) {
                try {
                    gestorPeticiones.enviarPeticion(Protocolo.BUSCAR_CITAS_POR_PACIENTE + Protocolo.SEPARADOR_CODIGO + paciente.getId());
                    ObjectInputStream entradaCitas = gestorPeticiones.getEntrada();
                    if (entradaCitas.readInt() == Protocolo.BUSCAR_CITAS_POR_PACIENTE_RESPONSE) {
                        List<ModeloCita> citas = (List<ModeloCita>) entradaCitas.readObject();
                        cita = citas.stream()
                            .filter(c -> c.getId().equals(diagnostico.getCitaId()))
                            .findFirst()
                            .orElse(null);
                    }
                } catch (Exception e) {
                    System.err.println("No se pudieron obtener las citas: " + e.getMessage());
                }
            }
            
            // Llamar al método de exportación del DiagnosticoController
            diagController.exportarPDFDesdeLista(diagnostico, paciente, cita);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al exportar PDF", 
                    "Ha ocurrido un error al exportar el diagnóstico a PDF: " + e.getMessage());
        }
    }
    
    /**
     * Exporta un diagnóstico específico a CSV
     */
    private void exportarDiagnosticoCSV(ModeloDiagnostico diagnostico) {
                 try {
             // Buscar el paciente asociado al diagnóstico
             ModeloPaciente pacienteTemporal = null;
             for (ModeloPaciente p : pacientesObservable) {
                 if (p.getId().equals(diagnostico.getPacienteId())) {
                     pacienteTemporal = p;
                     break;
                 }
             }
             
             if (pacienteTemporal == null) {
                 // Si no está en la lista observable, buscarlo en el servidor
                 try {
                     gestorPeticiones.enviarPeticion(Protocolo.OBTENERPACIENTE_POR_ID + Protocolo.SEPARADOR_CODIGO + diagnostico.getPacienteId());
                     ObjectInputStream entrada = gestorPeticiones.getEntrada();
                     if (entrada.readInt() == Protocolo.OBTENERPACIENTE_POR_ID_RESPONSE) {
                         pacienteTemporal = (ModeloPaciente) entrada.readObject();
                     }
                 } catch (IOException | ClassNotFoundException e) {
                     e.printStackTrace();
                 }
             }
             
             final ModeloPaciente paciente = pacienteTemporal; // Hacer final para usar en lambda
             
             if (paciente == null) {
                 mostrarAlerta("Error", "Paciente no encontrado", 
                         "No se pudo encontrar el paciente asociado a este diagnóstico.");
                 return;
             }
             
             // Crear el FileChooser para guardar el CSV
             FileChooser fileChooser = new FileChooser();
             fileChooser.setTitle("Guardar Diagnóstico CSV");
             fileChooser.getExtensionFilters().add(
                 new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
             );
             fileChooser.setInitialFileName("diagnostico_" + paciente.getNombre().replaceAll("[^a-zA-Z0-9]", "_") + ".csv");
             
             // Obtener ventana actual para mostrar el diálogo
             Stage stage = (Stage) mainPane.getScene().getWindow();
             File file = fileChooser.showSaveDialog(stage);
             
             if (file != null) {
                 // Ejecutar en un hilo separado para no bloquear la interfaz
                 new Thread(() -> {
                     try (FileWriter writer = new FileWriter(file)) {
                         // Escribir encabezados CSV
                         writer.append("Paciente,Especie,Raza,Fecha,Motivo,Diagnóstico,Veterinario,Anamnesis,Examen Físico,Tratamiento,Observaciones,Próxima Visita\n");
                         
                         // Formatear fecha
                         String fecha = diagnostico.getFecha() != null ? 
                                 formatoFecha.format(diagnostico.getFecha()) : "No especificada";
                         
                         // Formatear próxima visita
                         String proximaVisita = diagnostico.getProximaVisita() != null ? 
                                 formatoFecha.format(diagnostico.getProximaVisita()) : "No programada";
                         
                         // Escapar comas y comillas en los campos de texto
                         String nombre = escaparCSV(paciente.getNombre());
                         String especie = escaparCSV(paciente.getEspecie());
                         String raza = escaparCSV(paciente.getRaza());
                         String motivo = escaparCSV(diagnostico.getMotivo());
                         String diagnosticoTexto = escaparCSV(diagnostico.getDiagnostico());
                         String veterinario = escaparCSV(diagnostico.getVeterinario());
                         String anamnesis = escaparCSV(diagnostico.getAnamnesis());
                         String examenFisico = escaparCSV(diagnostico.getExamenFisico());
                         String tratamiento = escaparCSV(diagnostico.getTratamiento());
                         String observaciones = escaparCSV(diagnostico.getObservaciones());
                         
                         // Escribir línea de datos
                         writer.append(nombre).append(",")
                               .append(especie).append(",")
                               .append(raza).append(",")
                               .append(fecha).append(",")
                               .append(motivo).append(",")
                               .append(diagnosticoTexto).append(",")
                               .append(veterinario).append(",")
                               .append(anamnesis).append(",")
                               .append(examenFisico).append(",")
                               .append(tratamiento).append(",")
                               .append(observaciones).append(",")
                               .append(proximaVisita).append("\n");
                         
                         Platform.runLater(() -> {
                             mostrarMensaje("Exportación exitosa", "CSV generado", 
                                     "El diagnóstico ha sido exportado correctamente a CSV:\n" + file.getAbsolutePath());
                         });
                         
                     } catch (IOException e) {
                         e.printStackTrace();
                         Platform.runLater(() -> {
                             mostrarAlerta("Error al exportar", "Error de escritura", 
                                     "No se pudo exportar el diagnóstico a CSV. Error: " + e.getMessage());
                         });
                     }
                 }).start();
             }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al exportar CSV", 
                    "Ha ocurrido un error al exportar el diagnóstico a CSV: " + e.getMessage());
        }
    }
    
    /**
     * Escapa caracteres especiales para CSV
     */
    private String escaparCSV(String texto) {
        if (texto == null) {
            return "";
        }
        
        // Si contiene comas, comillas o saltos de línea, envolverlo en comillas
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            // Escapar comillas dobles duplicándolas
            texto = texto.replace("\"", "\"\"");
            // Envolver en comillas
            return "\"" + texto + "\"";
        }
        
        return texto;
    }
} 