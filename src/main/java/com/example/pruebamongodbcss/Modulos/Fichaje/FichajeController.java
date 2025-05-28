package com.example.pruebamongodbcss.Modulos.Fichaje;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.PanelInicioController;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;
import com.example.pruebamongodbcss.Modulos.Fichaje.ExportadorFichajes;
import com.example.pruebamongodbcss.Modulos.Fichaje.EstadisticasFichaje;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FichajeController implements Initializable {

    // Elementos de la interfaz
    @FXML private Label lblRelojTiempoReal;
    @FXML private Label lblFechaActual;
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblEstadoFichaje;
    @FXML private Label lblHoraEntrada;
    @FXML private Label lblTiempoTrabajado;
    @FXML private Label lblEstadoActual;
    @FXML private Label lblConexionEstado;
    
    // Paneles de navegación
    @FXML private VBox panelSeleccionModulos;
    @FXML private VBox cardFichaje;
    @FXML private VBox cardAdministrador;
    @FXML private VBox panelFichajePrincipal;
    @FXML private Button btnVolverSeleccion;
    @FXML private Button btnVolverSeleccionAdmin;
    
    // Controles de fichaje
    @FXML private ComboBox<ModeloFichaje.TipoFichaje> cmbTipoFichaje;
    @FXML private TextField txtMotivoIncidencia;
    @FXML private Button btnFicharEntrada;
    @FXML private Button btnFicharSalida;
    @FXML private Button btnRefrescar;
    
    // Panel de fichaje actual
    @FXML private VBox panelFichajeActual;
    
    // Controles de historial
    @FXML private TableView<ModeloFichaje> tablaHistorial;
    @FXML private TableColumn<ModeloFichaje, String> colFecha;
    @FXML private TableColumn<ModeloFichaje, String> colHoraEntrada;
    @FXML private TableColumn<ModeloFichaje, String> colHoraSalida;
    @FXML private TableColumn<ModeloFichaje, String> colTiempoTotal;
    @FXML private TableColumn<ModeloFichaje, String> colTipoEntrada;
    @FXML private TableColumn<ModeloFichaje, String> colTipoSalida;
    @FXML private TableColumn<ModeloFichaje, String> colEstado;
    @FXML private TableColumn<ModeloFichaje, String> colAcciones;
    
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpiarFiltros;
    @FXML private Button btnExportarHistorial;
    @FXML private Button btnGenerarInforme;
    
    // Panel de administración
    @FXML private VBox panelAdministracion;
    @FXML private ComboBox<String> cmbEmpleados;
    @FXML private Button btnVerTodosFichajes;
    @FXML private Button btnEstadisticas;
    @FXML private Button btnGestionarIncidencias;
    @FXML private GridPane gridEstadisticas;
    @FXML private Label lblTotalEmpleados;
    @FXML private Label lblFichajesHoy;
    @FXML private Label lblIncidenciasHoy;
    @FXML private Label lblPromedioHoras;
    
    // Controles administrativos adicionales
    @FXML private DatePicker dpFechaInicioAdmin;
    @FXML private DatePicker dpFechaFinAdmin;
    @FXML private Button btnFiltrarAdmin;
    @FXML private Button btnLimpiarFiltrosAdmin;
    @FXML private Button btnExportarTodosExcel;
    @FXML private Button btnExportarTodosPDF;
    @FXML private Button btnEstadisticasCompletas;
    @FXML private Button btnGenerarInformeAdmin;
    @FXML private TableView<ModeloFichaje> tablaTodosFichajes;
    @FXML private TableColumn<ModeloFichaje, String> colEmpleado;
    @FXML private TableColumn<ModeloFichaje, String> colFechaAdmin;
    @FXML private TableColumn<ModeloFichaje, String> colHoraEntradaAdmin;
    @FXML private TableColumn<ModeloFichaje, String> colHoraSalidaAdmin;
    @FXML private TableColumn<ModeloFichaje, String> colTiempoTotalAdmin;
    @FXML private TableColumn<ModeloFichaje, String> colTipoEntradaAdmin;
    @FXML private TableColumn<ModeloFichaje, String> colTipoSalidaAdmin;
    @FXML private TableColumn<ModeloFichaje, String> colEstadoAdmin;
    @FXML private TableColumn<ModeloFichaje, String> colAccionesAdmin;
    
    // Variables de control
    private Usuario usuarioActual;
    private GestorSocket gestorSocket;
    private Timer relojTimer;
    private Timer actualizacionTimer;
    private ModeloFichaje fichajeActual;
    private ObservableList<ModeloFichaje> listaFichajes;
    private ObservableList<ModeloFichaje> listaTodosFichajes;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Inicializar gestor de socket
            gestorSocket = GestorSocket.getInstance();
            
            // Configurar tabla de historial
            configurarTablaHistorial();
            
            // Configurar tabla administrativa
            configurarTablaTodosFichajes();
            
            // Configurar combo de tipos de fichaje
            configurarComboTipoFichaje();
            
            // Configurar eventos de botones
            configurarEventosBotones();
            
            // Inicializar reloj en tiempo real
            iniciarRelojTiempoReal();
            
            // Obtener usuario actual
            usuarioActual = PanelInicioController.getUsuarioSesion();
            if (usuarioActual != null) {
                lblUsuarioActual.setText(usuarioActual.getNombre());
                configurarInterfazSegunRol();
                configurarInterfazInicial();
                cargarDatosIniciales();
            }
            
            // Inicializar listas de fichajes
            listaFichajes = FXCollections.observableArrayList();
            tablaHistorial.setItems(listaFichajes);
            
            listaTodosFichajes = FXCollections.observableArrayList();
            if (tablaTodosFichajes != null) {
                tablaTodosFichajes.setItems(listaTodosFichajes);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de Inicialización", "No se pudo inicializar el módulo de fichaje: " + e.getMessage());
        }
    }
    
    private void configurarTablaHistorial() {
        // Configurar política de redimensionamiento
        tablaHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Configurar cell value factories
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFormateada"));
        colHoraEntrada.setCellValueFactory(new PropertyValueFactory<>("horaEntradaFormateada"));
        colHoraSalida.setCellValueFactory(new PropertyValueFactory<>("horaSalidaFormateada"));
        colTiempoTotal.setCellValueFactory(new PropertyValueFactory<>("horasTrabajadasFormateadas"));
        colTipoEntrada.setCellValueFactory(cellData -> {
            ModeloFichaje fichaje = cellData.getValue();
            String tipo = fichaje.getTipoEntrada() != null ? fichaje.getTipoEntrada().getDescripcion() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(tipo);
        });
        colTipoSalida.setCellValueFactory(cellData -> {
            ModeloFichaje fichaje = cellData.getValue();
            String tipo = fichaje.getTipoSalida() != null ? fichaje.getTipoSalida().getDescripcion() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(tipo);
        });
        colEstado.setCellValueFactory(cellData -> {
            ModeloFichaje fichaje = cellData.getValue();
            String estado = fichaje.getEstado() != null ? fichaje.getEstado().getDescripcion() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(estado);
        });
        
        // Configurar anchos de columnas responsive
        configurarAnchoColumnasResponsive();
        
        // Configurar columna de acciones con botones
        colAcciones.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<ModeloFichaje, String>() {
                private final Button btnEditar = new Button("✏️");
                private final Button btnEliminar = new Button("🗑️");
                
                {
                    btnEditar.getStyleClass().addAll("secondary-button");
                    btnEliminar.getStyleClass().addAll("secondary-button");
                    btnEditar.setTooltip(new javafx.scene.control.Tooltip("Editar fichaje"));
                    btnEliminar.setTooltip(new javafx.scene.control.Tooltip("Eliminar fichaje"));
                    
                    // Hacer botones más pequeños para móviles
                    btnEditar.setPrefSize(30, 25);
                    btnEliminar.setPrefSize(30, 25);
                    
                    btnEditar.setOnAction(event -> {
                        ModeloFichaje fichaje = getTableView().getItems().get(getIndex());
                        editarFichaje(fichaje);
                    });
                    
                    btnEliminar.setOnAction(event -> {
                        ModeloFichaje fichaje = getTableView().getItems().get(getIndex());
                        eliminarFichaje(fichaje);
                    });
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(3);
                        hbox.setAlignment(javafx.geometry.Pos.CENTER);
                        hbox.getChildren().addAll(btnEditar, btnEliminar);
                        setGraphic(hbox);
                    }
                }
            };
        });
        
        // Configurar comportamiento responsive de la tabla
        configurarComportamientoResponsiveTabla(tablaHistorial);
    }
    
    private void configurarAnchoColumnasResponsive() {
        // Configurar anchos de columnas que se adapten al tamaño de la ventana
        colFecha.prefWidthProperty().bind(tablaHistorial.widthProperty().multiply(0.12));
        colHoraEntrada.prefWidthProperty().bind(tablaHistorial.widthProperty().multiply(0.10));
        colHoraSalida.prefWidthProperty().bind(tablaHistorial.widthProperty().multiply(0.10));
        colTiempoTotal.prefWidthProperty().bind(tablaHistorial.widthProperty().multiply(0.10));
        colTipoEntrada.prefWidthProperty().bind(tablaHistorial.widthProperty().multiply(0.15));
        colTipoSalida.prefWidthProperty().bind(tablaHistorial.widthProperty().multiply(0.15));
        colEstado.prefWidthProperty().bind(tablaHistorial.widthProperty().multiply(0.12));
        colAcciones.prefWidthProperty().bind(tablaHistorial.widthProperty().multiply(0.16));
    }
    
    private void configurarComportamientoResponsiveTabla(TableView<?> tabla) {
        // Agregar listener para cambios de tamaño de ventana
        tabla.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> {
                            ajustarColumnasSegunTamano(tabla, newWidth.doubleValue());
                        });
                    }
                });
            }
        });
    }
    
    private void ajustarColumnasSegunTamano(TableView<?> tabla, double anchoVentana) {
        // Ocultar/mostrar columnas según el tamaño de la ventana
        if (tabla == tablaHistorial) {
            if (anchoVentana < 800) {
                // Pantallas pequeñas: ocultar columnas menos importantes
                colTipoEntrada.setVisible(false);
                colTipoSalida.setVisible(false);
            } else if (anchoVentana < 1000) {
                // Pantallas medianas: mostrar columnas importantes
                colTipoEntrada.setVisible(true);
                colTipoSalida.setVisible(false);
            } else {
                // Pantallas grandes: mostrar todas las columnas
                colTipoEntrada.setVisible(true);
                colTipoSalida.setVisible(true);
            }
        } else if (tabla == tablaTodosFichajes) {
            if (anchoVentana < 900) {
                // Pantallas pequeñas: ocultar columnas menos importantes
                colTipoEntradaAdmin.setVisible(false);
                colTipoSalidaAdmin.setVisible(false);
            } else if (anchoVentana < 1100) {
                // Pantallas medianas: mostrar columnas importantes
                colTipoEntradaAdmin.setVisible(true);
                colTipoSalidaAdmin.setVisible(false);
            } else {
                // Pantallas grandes: mostrar todas las columnas
                colTipoEntradaAdmin.setVisible(true);
                colTipoSalidaAdmin.setVisible(true);
            }
        }
    }
    
    private void configurarComboTipoFichaje() {
        cmbTipoFichaje.setItems(FXCollections.observableArrayList(ModeloFichaje.TipoFichaje.values()));
        cmbTipoFichaje.setValue(ModeloFichaje.TipoFichaje.NORMAL);
        
        // Mostrar/ocultar campo de motivo según el tipo seleccionado
        cmbTipoFichaje.setOnAction(event -> {
            ModeloFichaje.TipoFichaje tipoSeleccionado = cmbTipoFichaje.getValue();
            boolean esIncidencia = tipoSeleccionado != ModeloFichaje.TipoFichaje.NORMAL;
            txtMotivoIncidencia.setVisible(esIncidencia);
            txtMotivoIncidencia.setManaged(esIncidencia);
            
            if (!esIncidencia) {
                txtMotivoIncidencia.clear();
            }
        });
    }
    
    private void configurarEventosBotones() {
        btnFicharEntrada.setOnAction(event -> ficharEntrada());
        btnFicharSalida.setOnAction(event -> ficharSalida());
        btnRefrescar.setOnAction(event -> actualizarDatos());
        
        btnFiltrar.setOnAction(event -> aplicarFiltros());
        btnLimpiarFiltros.setOnAction(event -> limpiarFiltros());
        btnExportarHistorial.setOnAction(event -> exportarHistorial());
        btnGenerarInforme.setOnAction(event -> generarInforme());
        
        // Botones de administración
        btnVerTodosFichajes.setOnAction(event -> verTodosFichajes());
        btnEstadisticas.setOnAction(event -> mostrarEstadisticas());
        btnGestionarIncidencias.setOnAction(event -> gestionarIncidencias());
        
        // Botones de navegación entre tarjetas
        if (btnVolverSeleccion != null) {
            btnVolverSeleccion.setOnAction(event -> volverASeleccion());
        }
        if (btnVolverSeleccionAdmin != null) {
            btnVolverSeleccionAdmin.setOnAction(event -> volverASeleccionAdmin());
        }
        
        // Botones administrativos adicionales
        if (btnFiltrarAdmin != null) {
            btnFiltrarAdmin.setOnAction(event -> aplicarFiltrosAdmin());
        }
        if (btnLimpiarFiltrosAdmin != null) {
            btnLimpiarFiltrosAdmin.setOnAction(event -> limpiarFiltrosAdmin());
        }
        if (btnExportarTodosExcel != null) {
            btnExportarTodosExcel.setOnAction(event -> exportarTodosExcel());
        }
        if (btnExportarTodosPDF != null) {
            btnExportarTodosPDF.setOnAction(event -> exportarTodosPDF());
        }
        if (btnEstadisticasCompletas != null) {
            btnEstadisticasCompletas.setOnAction(event -> mostrarEstadisticasCompletas());
        }
        if (btnGenerarInformeAdmin != null) {
            btnGenerarInformeAdmin.setOnAction(event -> generarInformeAdministrativo());
        }
    }
    
    private void iniciarRelojTiempoReal() {
        relojTimer = new Timer(true);
        relojTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    LocalDateTime ahora = LocalDateTime.now();
                    lblRelojTiempoReal.setText(ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    lblFechaActual.setText(ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    
                    // Actualizar tiempo trabajado si hay fichaje activo
                    if (fichajeActual != null && fichajeActual.getEstado() == ModeloFichaje.EstadoFichaje.ABIERTO) {
                        actualizarTiempoTrabajado();
                    }
                });
            }
        }, 0, 1000); // Actualizar cada segundo
        
        // Timer para actualización periódica de datos
        actualizacionTimer = new Timer(true);
        actualizacionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> verificarFichajeActual());
            }
        }, 0, 30000); // Actualizar cada 30 segundos
    }
    
    private void configurarInterfazSegunRol() {
        if (usuarioActual != null && usuarioActual.esAdmin()) {
            panelAdministracion.setVisible(true);
            panelAdministracion.setManaged(true);
            cargarEmpleados();
        }
    }
    
    private void cargarDatosIniciales() {
        verificarFichajeActual();
        cargarHistorialFichajes();
        if (usuarioActual.esAdmin()) {
            cargarEstadisticasGenerales();
        }
    }
    
    private void ficharEntrada() {
        try {
            if (fichajeActual != null && fichajeActual.getEstado() == ModeloFichaje.EstadoFichaje.ABIERTO) {
                mostrarError("Error", "Ya tienes un fichaje abierto. Debes fichar la salida primero.");
                return;
            }
            
            ModeloFichaje.TipoFichaje tipo = cmbTipoFichaje.getValue();
            String motivo = txtMotivoIncidencia.getText().trim();
            
            if (tipo != ModeloFichaje.TipoFichaje.NORMAL && motivo.isEmpty()) {
                mostrarError("Error", "Debes especificar un motivo para las incidencias.");
                return;
            }
            
            // Enviar petición al servidor
            String peticion = Protocolo.FICHAR_ENTRADA + Protocolo.SEPARADOR_CODIGO + 
                            usuarioActual.getId().toString() + Protocolo.SEPARADOR_PARAMETROS +
                            usuarioActual.getNombre() + Protocolo.SEPARADOR_PARAMETROS +
                            usuarioActual.getUsuario() + Protocolo.SEPARADOR_PARAMETROS +
                            tipo.name() + 
                            (motivo.isEmpty() ? "" : Protocolo.SEPARADOR_PARAMETROS + motivo);
            
            gestorSocket.enviarPeticion(peticion);
            
            // Leer respuesta
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.FICHAR_ENTRADA_RESPONSE) {
                fichajeActual = (ModeloFichaje) entrada.readObject();
                mostrarExito("Fichaje de Entrada", "Entrada registrada correctamente a las " + 
                           fichajeActual.getHoraEntradaFormateada());
                actualizarInterfazFichajeActual();
                cargarHistorialFichajes();
            } else if (codigoRespuesta == Protocolo.ERROR_FICHAR_ENTRADA) {
                String error = entrada.readUTF();
                mostrarError("Error al Fichar Entrada", error);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de Comunicación", "No se pudo conectar con el servidor: " + e.getMessage());
        }
    }
    
    private void ficharSalida() {
        try {
            if (fichajeActual == null || fichajeActual.getEstado() != ModeloFichaje.EstadoFichaje.ABIERTO) {
                mostrarError("Error", "No tienes un fichaje de entrada abierto.");
                return;
            }
            
            ModeloFichaje.TipoFichaje tipo = cmbTipoFichaje.getValue();
            String motivo = txtMotivoIncidencia.getText().trim();
            
            if (tipo != ModeloFichaje.TipoFichaje.NORMAL && motivo.isEmpty()) {
                mostrarError("Error", "Debes especificar un motivo para las incidencias.");
                return;
            }
            
            // Enviar petición al servidor
            String peticion = Protocolo.FICHAR_SALIDA + Protocolo.SEPARADOR_CODIGO + 
                            usuarioActual.getId().toString() + Protocolo.SEPARADOR_PARAMETROS +
                            tipo.name() + 
                            (motivo.isEmpty() ? "" : Protocolo.SEPARADOR_PARAMETROS + motivo);
            
            gestorSocket.enviarPeticion(peticion);
            
            // Leer respuesta
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.FICHAR_SALIDA_RESPONSE) {
                boolean exito = entrada.readBoolean();
                if (exito) {
                    mostrarExito("Fichaje de Salida", "Salida registrada correctamente.");
                    fichajeActual = null;
                    actualizarInterfazFichajeActual();
                    cargarHistorialFichajes();
                } else {
                    mostrarError("Error", "No se pudo registrar la salida.");
                }
            } else if (codigoRespuesta == Protocolo.ERROR_FICHAR_SALIDA) {
                String error = entrada.readUTF();
                mostrarError("Error al Fichar Salida", error);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de Comunicación", "No se pudo conectar con el servidor: " + e.getMessage());
        }
    }
    
    private void verificarFichajeActual() {
        try {
            if (usuarioActual == null) return;
            
            // Enviar petición al servidor
            String peticion = Protocolo.OBTENER_FICHAJE_ABIERTO_HOY + Protocolo.SEPARADOR_CODIGO + 
                            usuarioActual.getId().toString();
            
            gestorSocket.enviarPeticion(peticion);
            
            // Leer respuesta
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_FICHAJE_ABIERTO_HOY_RESPONSE) {
                fichajeActual = (ModeloFichaje) entrada.readObject();
                actualizarInterfazFichajeActual();
            } else {
                fichajeActual = null;
                actualizarInterfazFichajeActual();
            }
            
        } catch (Exception e) {
            System.err.println("Error al verificar fichaje actual: " + e.getMessage());
        }
    }
    
    private void actualizarInterfazFichajeActual() {
        // El panel de fichaje actual siempre debe estar visible
        panelFichajeActual.setVisible(true);
        panelFichajeActual.setManaged(true);
        
        if (fichajeActual != null && fichajeActual.getEstado() == ModeloFichaje.EstadoFichaje.ABIERTO) {
            lblHoraEntrada.setText(fichajeActual.getHoraEntradaFormateada());
            lblEstadoActual.setText(fichajeActual.getEstado().getDescripcion());
            lblEstadoFichaje.setText("Fichado - Trabajando");
            
            btnFicharEntrada.setDisable(true);
            btnFicharSalida.setDisable(false);
            
            actualizarTiempoTrabajado();
        } else {
            lblHoraEntrada.setText("--:--");
            lblEstadoActual.setText("Sin fichar");
            lblEstadoFichaje.setText("Sin fichar");
            lblTiempoTrabajado.setText("00:00");
            
            btnFicharEntrada.setDisable(false);
            btnFicharSalida.setDisable(true);
        }
    }
    
    private void actualizarTiempoTrabajado() {
        if (fichajeActual != null && fichajeActual.getFechaHoraEntrada() != null) {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime entrada = fichajeActual.getFechaHoraEntrada();
            
            long minutos = java.time.Duration.between(entrada, ahora).toMinutes();
            long horas = minutos / 60;
            long minutosRestantes = minutos % 60;
            
            lblTiempoTrabajado.setText(String.format("%02d:%02d", horas, minutosRestantes));
        }
    }
    
    private void cargarHistorialFichajes() {
        try {
            if (usuarioActual == null) return;
            
            // Enviar petición al servidor
            String peticion = Protocolo.OBTENER_HISTORIAL_FICHAJES + Protocolo.SEPARADOR_CODIGO + 
                            usuarioActual.getId().toString() + Protocolo.SEPARADOR_PARAMETROS + "50";
            
            gestorSocket.enviarPeticion(peticion);
            
            // Leer respuesta
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_HISTORIAL_FICHAJES_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloFichaje> fichajes = (List<ModeloFichaje>) entrada.readObject();
                
                Platform.runLater(() -> {
                    listaFichajes.clear();
                    listaFichajes.addAll(fichajes);
                });
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar historial: " + e.getMessage());
        }
    }
    
    private void cargarEmpleados() {
        // TODO: Implementar carga de empleados para administradores
        // Por ahora, agregar algunos empleados de ejemplo
        Platform.runLater(() -> {
            cmbEmpleados.setItems(FXCollections.observableArrayList(
                "Todos los empleados",
                usuarioActual.getNombre()
            ));
            cmbEmpleados.setValue("Todos los empleados");
        });
    }
    
    private void cargarEstadisticasGenerales() {
        try {
            LocalDate hoy = LocalDate.now();
            
            // Enviar petición al servidor
            String peticion = Protocolo.OBTENER_ESTADISTICAS_FICHAJES + Protocolo.SEPARADOR_CODIGO;
            gestorSocket.enviarPeticion(peticion);
            gestorSocket.getSalida().writeObject(hoy);
            gestorSocket.getSalida().writeObject(hoy);
            gestorSocket.getSalida().flush();
            
            // Leer respuesta
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_ESTADISTICAS_FICHAJES_RESPONSE) {
                Document estadisticas = (Document) entrada.readObject();
                
                Platform.runLater(() -> {
                    lblTotalEmpleados.setText(String.valueOf(estadisticas.getInteger("totalEmpleados", 0)));
                    lblFichajesHoy.setText(String.valueOf(estadisticas.getInteger("fichajesHoy", 0)));
                    lblIncidenciasHoy.setText(String.valueOf(estadisticas.getInteger("incidenciasHoy", 0)));
                    Double promedioHoras = estadisticas.getDouble("promedioHoras");
                    lblPromedioHoras.setText(String.format("%.1fh", promedioHoras != null ? promedioHoras : 0.0));
                });
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar estadísticas: " + e.getMessage());
        }
    }
    
    private void actualizarDatos() {
        verificarFichajeActual();
        cargarHistorialFichajes();
        if (usuarioActual != null && usuarioActual.esAdmin()) {
            cargarEstadisticasGenerales();
        }
    }
    
    private void aplicarFiltros() {
        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();
        
        if (fechaInicio == null || fechaFin == null) {
            mostrarError("Error", "Debes seleccionar ambas fechas para filtrar.");
            return;
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            mostrarError("Error", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }
        
        try {
            // Enviar petición al servidor
            String peticion = Protocolo.OBTENER_FICHAJES_EMPLEADO_POR_FECHA + Protocolo.SEPARADOR_CODIGO + 
                            usuarioActual.getId().toString();
            
            gestorSocket.enviarPeticion(peticion);
            gestorSocket.getSalida().writeObject(fechaInicio);
            gestorSocket.getSalida().writeObject(fechaFin);
            gestorSocket.getSalida().flush();
            
            // Leer respuesta
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_FICHAJES_EMPLEADO_POR_FECHA_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloFichaje> fichajes = (List<ModeloFichaje>) entrada.readObject();
                
                Platform.runLater(() -> {
                    listaFichajes.clear();
                    listaFichajes.addAll(fichajes);
                });
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudieron aplicar los filtros: " + e.getMessage());
        }
    }
    
    private void limpiarFiltros() {
        dpFechaInicio.setValue(null);
        dpFechaFin.setValue(null);
        cargarHistorialFichajes();
    }
    
    private void exportarHistorial() {
        try {
            // Obtener datos filtrados o todos los datos
            List<ModeloFichaje> fichajes = new ArrayList<>(listaFichajes);
            
            if (fichajes.isEmpty()) {
                mostrarInfo("Sin datos", "No hay fichajes para exportar.");
                return;
            }
            
            // Mostrar diálogo de selección de formato
            javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>("Excel", "Excel", "PDF");
            dialog.setTitle("Exportar Historial");
            dialog.setHeaderText("Seleccione el formato de exportación");
            dialog.setContentText("Formato:");
            
            dialog.showAndWait().ifPresent(formato -> {
                String nombreEmpleado = usuarioActual != null ? usuarioActual.getNombre() : "Todos";
                LocalDate fechaInicio = dpFechaInicio.getValue();
                LocalDate fechaFin = dpFechaFin.getValue();
                
                Stage stage = (Stage) btnExportarHistorial.getScene().getWindow();
                
                if ("Excel".equals(formato)) {
                    ExportadorFichajes.exportarAExcel(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
                } else if ("PDF".equals(formato)) {
                    ExportadorFichajes.exportarAPDF(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al exportar", "No se pudo exportar el historial: " + e.getMessage());
        }
    }
    
    private void generarInforme() {
        try {
            // Obtener datos para el informe
            List<ModeloFichaje> fichajes = new ArrayList<>(listaFichajes);
            
            if (fichajes.isEmpty()) {
                mostrarInfo("Sin datos", "No hay fichajes para generar el informe.");
                return;
            }
            
            // Mostrar diálogo de selección de tipo de informe
            javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(
                "Resumen Estadístico", 
                "Resumen Estadístico", 
                "Informe Detallado Excel", 
                "Informe Detallado PDF"
            );
            dialog.setTitle("Generar Informe");
            dialog.setHeaderText("Seleccione el tipo de informe");
            dialog.setContentText("Tipo:");
            
            dialog.showAndWait().ifPresent(tipo -> {
                String nombreEmpleado = usuarioActual != null ? usuarioActual.getNombre() : "Todos los empleados";
                LocalDate fechaInicio = dpFechaInicio.getValue();
                LocalDate fechaFin = dpFechaFin.getValue();
                Stage stage = (Stage) btnGenerarInforme.getScene().getWindow();
                
                switch (tipo) {
                    case "Resumen Estadístico":
                        // Mostrar estadísticas en ventana modal
                        EstadisticasFichaje.mostrarEstadisticasCompletas(fichajes, 
                            "Estadísticas de Fichaje - " + nombreEmpleado);
                        break;
                    case "Informe Detallado Excel":
                        ExportadorFichajes.exportarAExcel(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
                        break;
                    case "Informe Detallado PDF":
                        ExportadorFichajes.exportarAPDF(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
                        break;
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al generar informe", "No se pudo generar el informe: " + e.getMessage());
        }
    }
    
    private void verTodosFichajes() {
        if (!usuarioActual.esAdmin()) {
            mostrarError("Acceso Denegado", "Solo los administradores pueden ver todos los fichajes.");
            return;
        }
        
        try {
            // Enviar petición al servidor
            String peticion = Protocolo.OBTENER_TODOS_FICHAJES + Protocolo.SEPARADOR_CODIGO + "100";
            
            gestorSocket.enviarPeticion(peticion);
            
            // Leer respuesta
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_TODOS_FICHAJES_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloFichaje> fichajes = (List<ModeloFichaje>) entrada.readObject();
                
                Platform.runLater(() -> {
                    listaFichajes.clear();
                    listaFichajes.addAll(fichajes);
                });
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudieron cargar todos los fichajes: " + e.getMessage());
        }
    }
    
    private void mostrarEstadisticas() {
        try {
            // Obtener datos para estadísticas
            List<ModeloFichaje> fichajes = new ArrayList<>(listaTodosFichajes);
            
            if (fichajes.isEmpty()) {
                mostrarInfo("Sin datos", "No hay fichajes para mostrar estadísticas.");
                return;
            }
            
            // Mostrar ventana de estadísticas completas
            EstadisticasFichaje.mostrarEstadisticasCompletas(fichajes, 
                "Estadísticas Generales de Fichaje");
                
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al mostrar estadísticas", "No se pudieron cargar las estadísticas: " + e.getMessage());
        }
    }
    
    private void gestionarIncidencias() {
        // TODO: Implementar gestión de incidencias
        mostrarInfo("Gestión de Incidencias", "Funcionalidad en desarrollo");
    }
    
    private void editarFichaje(ModeloFichaje fichaje) {
        if (!usuarioActual.esAdmin()) {
            mostrarError("Acceso Denegado", "Solo los administradores pueden editar fichajes.");
            return;
        }
        
        // TODO: Implementar edición de fichajes
        mostrarInfo("Editar", "Funcionalidad de edición en desarrollo.");
    }
    
    private void eliminarFichaje(ModeloFichaje fichaje) {
        if (!usuarioActual.esAdmin()) {
            mostrarError("Acceso Denegado", "Solo los administradores pueden eliminar fichajes.");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar fichaje?");
        confirmacion.setContentText("¿Estás seguro de que quieres eliminar este fichaje? Esta acción no se puede deshacer.");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Enviar petición al servidor
                    String peticion = Protocolo.ELIMINAR_FICHAJE + Protocolo.SEPARADOR_CODIGO + 
                                    fichaje.getId().toString();
                    
                    gestorSocket.enviarPeticion(peticion);
                    
                    // Leer respuesta
                    ObjectInputStream entrada = gestorSocket.getEntrada();
                    int codigoRespuesta = entrada.readInt();
                    
                    if (codigoRespuesta == Protocolo.ELIMINAR_FICHAJE_RESPONSE) {
                        boolean eliminado = entrada.readBoolean();
                        if (eliminado) {
                            mostrarExito("Eliminación", "Fichaje eliminado correctamente.");
                            cargarHistorialFichajes();
                        } else {
                            mostrarError("Error", "No se pudo eliminar el fichaje.");
                        }
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarError("Error", "No se pudo eliminar el fichaje: " + e.getMessage());
                }
            }
        });
    }
    
    // Métodos de utilidad para mostrar mensajes
    private void mostrarError(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(titulo);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    private void mostrarExito(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(titulo);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Abre el módulo de fichaje desde la tarjeta
     */
    @FXML
    private void abrirModuloFichaje() {
        try {
            // Ocultar panel de selección y mostrar panel de fichaje
            panelSeleccionModulos.setVisible(false);
            panelSeleccionModulos.setManaged(false);
            
            panelFichajePrincipal.setVisible(true);
            panelFichajePrincipal.setManaged(true);
            
            panelAdministracion.setVisible(false);
            panelAdministracion.setManaged(false);
            
            // Aplicar tema después de mostrar el panel
            aplicarTemaActual();
            
            // Cargar datos del usuario actual
            verificarFichajeActual();
            cargarHistorialFichajes();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el módulo de fichaje: " + e.getMessage());
        }
    }
    
    /**
     * Abre el módulo de administración desde la tarjeta
     */
    @FXML
    private void abrirModuloAdministrador() {
        try {
            // Verificar permisos de administrador
            if (usuarioActual == null || !usuarioActual.esAdmin()) {
                mostrarError("Acceso Denegado", "Solo los administradores pueden acceder a este módulo.");
                return;
            }
            
            // Ocultar panel de selección y mostrar panel de administración
            panelSeleccionModulos.setVisible(false);
            panelSeleccionModulos.setManaged(false);
            
            panelFichajePrincipal.setVisible(false);
            panelFichajePrincipal.setManaged(false);
            
            panelAdministracion.setVisible(true);
            panelAdministracion.setManaged(true);
            
            // Aplicar tema después de mostrar el panel
            aplicarTemaActual();
            
            // Cargar datos administrativos
            cargarEmpleados();
            cargarEstadisticasGenerales();
            cargarTodosFichajes();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo abrir el módulo de administración: " + e.getMessage());
        }
    }
    
    /**
     * Vuelve al panel de selección desde el módulo de fichaje
     */
    @FXML
    private void volverASeleccion() {
        try {
            // Mostrar panel de selección y ocultar panel de fichaje
            panelSeleccionModulos.setVisible(true);
            panelSeleccionModulos.setManaged(true);
            
            panelFichajePrincipal.setVisible(false);
            panelFichajePrincipal.setManaged(false);
            
            panelAdministracion.setVisible(false);
            panelAdministracion.setManaged(false);
            
            // Aplicar tema después de mostrar el panel
            aplicarTemaActual();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo volver a la selección: " + e.getMessage());
        }
    }
    
    /**
     * Vuelve al panel de selección desde el módulo de administración
     */
    @FXML
    private void volverASeleccionAdmin() {
        try {
            // Mostrar panel de selección y ocultar panel de administración
            panelSeleccionModulos.setVisible(true);
            panelSeleccionModulos.setManaged(true);
            
            panelFichajePrincipal.setVisible(false);
            panelFichajePrincipal.setManaged(false);
            
            panelAdministracion.setVisible(false);
            panelAdministracion.setManaged(false);
            
            // Aplicar tema después de mostrar el panel
            aplicarTemaActual();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo volver a la selección: " + e.getMessage());
        }
    }
    
    /**
     * Configura la interfaz inicial según el rol del usuario
     */
    private void configurarInterfazInicial() {
        // Siempre mostrar el panel de selección de módulos al inicio
        panelSeleccionModulos.setVisible(true);
        panelSeleccionModulos.setManaged(true);
        
        // Ocultar todos los paneles de contenido inicialmente
        panelFichajePrincipal.setVisible(false);
        panelFichajePrincipal.setManaged(false);
        panelAdministracion.setVisible(false);
        panelAdministracion.setManaged(false);
        
        if (usuarioActual != null && usuarioActual.esAdmin()) {
            // Mostrar tarjeta de administrador para admins
            cardAdministrador.setVisible(true);
            cardAdministrador.setManaged(true);
        } else {
            // Ocultar tarjeta de administrador para usuarios normales
            cardAdministrador.setVisible(false);
            cardAdministrador.setManaged(false);
            
            // Para usuarios normales, también mostrar las tarjetas inicialmente
            // pero solo la de fichaje estará disponible
        }
    }
    
    /**
     * Carga todos los fichajes para el panel administrativo
     */
    private void cargarTodosFichajes() {
        try {
            String peticion = Protocolo.OBTENER_TODOS_FICHAJES + Protocolo.SEPARADOR_CODIGO + "100";
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_TODOS_FICHAJES_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloFichaje> fichajes = (List<ModeloFichaje>) entrada.readObject();
                
                Platform.runLater(() -> {
                    if (listaTodosFichajes == null) {
                        listaTodosFichajes = FXCollections.observableArrayList();
                        tablaTodosFichajes.setItems(listaTodosFichajes);
                    }
                    listaTodosFichajes.clear();
                    listaTodosFichajes.addAll(fichajes);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> mostrarError("Error", "No se pudieron cargar todos los fichajes: " + e.getMessage()));
        }
    }
    
    /**
     * Configura la tabla de todos los fichajes para administradores
     */
    private void configurarTablaTodosFichajes() {
        if (tablaTodosFichajes != null) {
            // Configurar política de redimensionamiento
            tablaTodosFichajes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            // Configurar cell value factories
            colEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
            colFechaAdmin.setCellValueFactory(new PropertyValueFactory<>("fechaFormateada"));
            colHoraEntradaAdmin.setCellValueFactory(new PropertyValueFactory<>("horaEntradaFormateada"));
            colHoraSalidaAdmin.setCellValueFactory(new PropertyValueFactory<>("horaSalidaFormateada"));
            colTiempoTotalAdmin.setCellValueFactory(new PropertyValueFactory<>("horasTrabajadasFormateadas"));
            colTipoEntradaAdmin.setCellValueFactory(cellData -> {
                ModeloFichaje fichaje = cellData.getValue();
                String tipo = fichaje.getTipoEntrada() != null ? fichaje.getTipoEntrada().getDescripcion() : "N/A";
                return new javafx.beans.property.SimpleStringProperty(tipo);
            });
            colTipoSalidaAdmin.setCellValueFactory(cellData -> {
                ModeloFichaje fichaje = cellData.getValue();
                String tipo = fichaje.getTipoSalida() != null ? fichaje.getTipoSalida().getDescripcion() : "N/A";
                return new javafx.beans.property.SimpleStringProperty(tipo);
            });
            colEstadoAdmin.setCellValueFactory(cellData -> {
                ModeloFichaje fichaje = cellData.getValue();
                String estado = fichaje.getEstado() != null ? fichaje.getEstado().getDescripcion() : "N/A";
                return new javafx.beans.property.SimpleStringProperty(estado);
            });
            
            // Configurar anchos de columnas responsive para tabla admin
            configurarAnchoColumnasResponsiveAdmin();
            
            // Configurar columna de acciones administrativas
            colAccionesAdmin.setCellFactory(column -> {
                return new javafx.scene.control.TableCell<ModeloFichaje, String>() {
                    private final Button btnEditarAdmin = new Button("✏️");
                    private final Button btnEliminarAdmin = new Button("🗑️");
                    
                    {
                        btnEditarAdmin.getStyleClass().addAll("secondary-button");
                        btnEliminarAdmin.getStyleClass().addAll("secondary-button");
                        btnEditarAdmin.setTooltip(new javafx.scene.control.Tooltip("Editar fichaje"));
                        btnEliminarAdmin.setTooltip(new javafx.scene.control.Tooltip("Eliminar fichaje"));
                        
                        // Hacer botones más pequeños para móviles
                        btnEditarAdmin.setPrefSize(30, 25);
                        btnEliminarAdmin.setPrefSize(30, 25);
                        
                        btnEditarAdmin.setOnAction(event -> {
                            ModeloFichaje fichaje = getTableView().getItems().get(getIndex());
                            editarFichaje(fichaje);
                        });
                        
                        btnEliminarAdmin.setOnAction(event -> {
                            ModeloFichaje fichaje = getTableView().getItems().get(getIndex());
                            eliminarFichaje(fichaje);
                        });
                    }
                    
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(3);
                            hbox.setAlignment(javafx.geometry.Pos.CENTER);
                            hbox.getChildren().addAll(btnEditarAdmin, btnEliminarAdmin);
                            setGraphic(hbox);
                        }
                    }
                };
            });
            
            // Configurar comportamiento responsive de la tabla admin
            configurarComportamientoResponsiveTabla(tablaTodosFichajes);
        }
    }
    
    private void configurarAnchoColumnasResponsiveAdmin() {
        // Configurar anchos de columnas que se adapten al tamaño de la ventana para tabla admin
        colEmpleado.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.15));
        colFechaAdmin.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.10));
        colHoraEntradaAdmin.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.10));
        colHoraSalidaAdmin.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.10));
        colTiempoTotalAdmin.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.10));
        colTipoEntradaAdmin.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.12));
        colTipoSalidaAdmin.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.12));
        colEstadoAdmin.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.10));
        colAccionesAdmin.prefWidthProperty().bind(tablaTodosFichajes.widthProperty().multiply(0.11));
    }

    /**
     * Cierra todos los recursos utilizados por el controlador
     */
    public void cerrarRecursos() {
        if (relojTimer != null) {
            relojTimer.cancel();
        }
        if (actualizacionTimer != null) {
            actualizacionTimer.cancel();
        }
    }

    /**
     * Aplica filtros en el panel administrativo
     */
    private void aplicarFiltrosAdmin() {
        try {
            LocalDate fechaInicio = dpFechaInicioAdmin.getValue();
            LocalDate fechaFin = dpFechaFinAdmin.getValue();
            String empleadoSeleccionado = cmbEmpleados.getValue();
            
            if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
                mostrarError("Error en Fechas", "La fecha de inicio no puede ser posterior a la fecha de fin.");
                return;
            }
            
            // Construir petición con filtros
            StringBuilder peticion = new StringBuilder();
            peticion.append(Protocolo.OBTENER_TODOS_FICHAJES).append(Protocolo.SEPARADOR_CODIGO).append("100");
            
            if (fechaInicio != null) {
                peticion.append(Protocolo.SEPARADOR_PARAMETROS).append("fechaInicio:").append(fechaInicio.toString());
            }
            if (fechaFin != null) {
                peticion.append(Protocolo.SEPARADOR_PARAMETROS).append("fechaFin:").append(fechaFin.toString());
            }
            if (empleadoSeleccionado != null && !empleadoSeleccionado.isEmpty()) {
                peticion.append(Protocolo.SEPARADOR_PARAMETROS).append("empleado:").append(empleadoSeleccionado);
            }
            
            gestorSocket.enviarPeticion(peticion.toString());
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_TODOS_FICHAJES_RESPONSE) {
                @SuppressWarnings("unchecked")
                List<ModeloFichaje> fichajes = (List<ModeloFichaje>) entrada.readObject();
                
                Platform.runLater(() -> {
                    listaTodosFichajes.clear();
                    listaTodosFichajes.addAll(fichajes);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudieron aplicar los filtros: " + e.getMessage());
        }
    }
    
    /**
     * Limpia los filtros del panel administrativo
     */
    private void limpiarFiltrosAdmin() {
        dpFechaInicioAdmin.setValue(null);
        dpFechaFinAdmin.setValue(null);
        cmbEmpleados.setValue(null);
        cargarTodosFichajes();
    }

    /**
     * Aplica el tema actual (claro u oscuro) a todos los elementos del módulo
     */
    private void aplicarTemaActual() {
        Platform.runLater(() -> {
            try {
                // Buscar el nodo raíz
                javafx.scene.Node rootNode = null;
                
                // Intentar obtener el nodo raíz desde diferentes elementos
                if (panelSeleccionModulos != null && panelSeleccionModulos.getScene() != null) {
                    rootNode = panelSeleccionModulos.getScene().getRoot();
                } else if (panelFichajePrincipal != null && panelFichajePrincipal.getScene() != null) {
                    rootNode = panelFichajePrincipal.getScene().getRoot();
                } else if (panelAdministracion != null && panelAdministracion.getScene() != null) {
                    rootNode = panelAdministracion.getScene().getRoot();
                }
                
                // Si no se encontró por scene, buscar por parent
                if (rootNode == null) {
                    javafx.scene.Node currentNode = panelSeleccionModulos;
                    if (currentNode != null && currentNode.getParent() != null) {
                        javafx.scene.Parent parent = currentNode.getParent();
                        while (parent.getParent() != null) {
                            parent = parent.getParent();
                        }
                        rootNode = parent;
                    }
                }
                
                // Aplicar o quitar tema oscuro según corresponda
                if (rootNode != null) {
                    boolean isDarkTheme = com.example.pruebamongodbcss.theme.ThemeManager.getInstance().isDarkTheme();
                    
                    if (isDarkTheme) {
                        if (!rootNode.getStyleClass().contains("dark-theme")) {
                            rootNode.getStyleClass().add("dark-theme");
                        }
                    } else {
                        rootNode.getStyleClass().remove("dark-theme");
                    }
                    
                    System.out.println("Tema " + (isDarkTheme ? "oscuro" : "claro") + " aplicado al módulo de fichaje");
                }
                
            } catch (Exception e) {
                System.err.println("Error al aplicar tema: " + e.getMessage());
            }
        });
    }

    /**
     * Exporta todos los fichajes filtrados a Excel (solo administradores)
     */
    private void exportarTodosExcel() {
        if (!usuarioActual.esAdmin()) {
            mostrarError("Acceso Denegado", "Solo los administradores pueden exportar todos los fichajes.");
            return;
        }
        
        try {
            List<ModeloFichaje> fichajes = new ArrayList<>(listaTodosFichajes);
            
            if (fichajes.isEmpty()) {
                mostrarInfo("Sin datos", "No hay fichajes para exportar.");
                return;
            }
            
            String nombreEmpleado = cmbEmpleados.getValue();
            if (nombreEmpleado == null || nombreEmpleado.equals("Todos los empleados")) {
                nombreEmpleado = "Todos_los_empleados";
            }
            
            LocalDate fechaInicio = dpFechaInicioAdmin.getValue();
            LocalDate fechaFin = dpFechaFinAdmin.getValue();
            Stage stage = (Stage) btnExportarTodosExcel.getScene().getWindow();
            
            ExportadorFichajes.exportarAExcel(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al exportar", "No se pudo exportar a Excel: " + e.getMessage());
        }
    }
    
    /**
     * Exporta todos los fichajes filtrados a PDF (solo administradores)
     */
    private void exportarTodosPDF() {
        if (!usuarioActual.esAdmin()) {
            mostrarError("Acceso Denegado", "Solo los administradores pueden exportar todos los fichajes.");
            return;
        }
        
        try {
            List<ModeloFichaje> fichajes = new ArrayList<>(listaTodosFichajes);
            
            if (fichajes.isEmpty()) {
                mostrarInfo("Sin datos", "No hay fichajes para exportar.");
                return;
            }
            
            String nombreEmpleado = cmbEmpleados.getValue();
            if (nombreEmpleado == null || nombreEmpleado.equals("Todos los empleados")) {
                nombreEmpleado = "Todos_los_empleados";
            }
            
            LocalDate fechaInicio = dpFechaInicioAdmin.getValue();
            LocalDate fechaFin = dpFechaFinAdmin.getValue();
            Stage stage = (Stage) btnExportarTodosPDF.getScene().getWindow();
            
            ExportadorFichajes.exportarAPDF(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al exportar", "No se pudo exportar a PDF: " + e.getMessage());
        }
    }
    
    /**
     * Muestra estadísticas completas de todos los empleados (solo administradores)
     */
    private void mostrarEstadisticasCompletas() {
        if (!usuarioActual.esAdmin()) {
            mostrarError("Acceso Denegado", "Solo los administradores pueden ver estadísticas completas.");
            return;
        }
        
        try {
            List<ModeloFichaje> fichajes = new ArrayList<>(listaTodosFichajes);
            
            if (fichajes.isEmpty()) {
                mostrarInfo("Sin datos", "No hay fichajes para mostrar estadísticas.");
                return;
            }
            
            String titulo = "Estadísticas Administrativas - Todos los Empleados";
            String empleadoSeleccionado = cmbEmpleados.getValue();
            
            if (empleadoSeleccionado != null && !empleadoSeleccionado.equals("Todos los empleados")) {
                titulo = "Estadísticas Administrativas - " + empleadoSeleccionado;
            }
            
            LocalDate fechaInicio = dpFechaInicioAdmin.getValue();
            LocalDate fechaFin = dpFechaFinAdmin.getValue();
            
            if (fechaInicio != null && fechaFin != null) {
                titulo += " (" + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                         " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
            }
            
            EstadisticasFichaje.mostrarEstadisticasCompletas(fichajes, titulo);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al mostrar estadísticas", "No se pudieron cargar las estadísticas: " + e.getMessage());
        }
    }
    
    /**
     * Genera un informe administrativo completo con opciones avanzadas
     */
    private void generarInformeAdministrativo() {
        if (!usuarioActual.esAdmin()) {
            mostrarError("Acceso Denegado", "Solo los administradores pueden generar informes administrativos.");
            return;
        }
        
        try {
            List<ModeloFichaje> fichajes = new ArrayList<>(listaTodosFichajes);
            
            if (fichajes.isEmpty()) {
                mostrarInfo("Sin datos", "No hay fichajes para generar el informe.");
                return;
            }
            
            // Mostrar diálogo de selección de tipo de informe administrativo
            javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(
                "Informe Completo Excel", 
                "Informe Completo Excel", 
                "Informe Completo PDF",
                "Estadísticas Detalladas",
                "Resumen Ejecutivo PDF",
                "Análisis de Productividad"
            );
            dialog.setTitle("Generar Informe Administrativo");
            dialog.setHeaderText("Seleccione el tipo de informe administrativo");
            dialog.setContentText("Tipo de informe:");
            
            dialog.showAndWait().ifPresent(tipo -> {
                String nombreEmpleado = cmbEmpleados.getValue();
                if (nombreEmpleado == null || nombreEmpleado.equals("Todos los empleados")) {
                    nombreEmpleado = "Informe_Administrativo_Completo";
                } else {
                    nombreEmpleado = "Informe_Admin_" + nombreEmpleado.replaceAll("\\s+", "_");
                }
                
                LocalDate fechaInicio = dpFechaInicioAdmin.getValue();
                LocalDate fechaFin = dpFechaFinAdmin.getValue();
                Stage stage = (Stage) btnGenerarInformeAdmin.getScene().getWindow();
                
                switch (tipo) {
                    case "Informe Completo Excel":
                        ExportadorFichajes.exportarAExcel(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
                        break;
                    case "Informe Completo PDF":
                        ExportadorFichajes.exportarAPDF(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
                        break;
                    case "Estadísticas Detalladas":
                        mostrarEstadisticasCompletas();
                        break;
                    case "Resumen Ejecutivo PDF":
                        // Generar un PDF con resumen ejecutivo
                        generarResumenEjecutivoPDF(fichajes, nombreEmpleado, fechaInicio, fechaFin, stage);
                        break;
                    case "Análisis de Productividad":
                        // Mostrar análisis de productividad
                        mostrarAnalisisProductividad(fichajes);
                        break;
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al generar informe", "No se pudo generar el informe administrativo: " + e.getMessage());
        }
    }
    
    /**
     * Genera un resumen ejecutivo en PDF para administradores
     */
    private void generarResumenEjecutivoPDF(List<ModeloFichaje> fichajes, String nombreArchivo, 
                                          LocalDate fechaInicio, LocalDate fechaFin, Stage stage) {
        try {
            // Por ahora, usar el exportador normal de PDF
            // En el futuro se puede crear un formato específico de resumen ejecutivo
            ExportadorFichajes.exportarAPDF(fichajes, nombreArchivo + "_Resumen_Ejecutivo", 
                                          fechaInicio, fechaFin, stage);
            
            mostrarInfo("Resumen Ejecutivo", 
                       "Se ha generado el resumen ejecutivo. En futuras versiones incluirá " +
                       "gráficos y análisis avanzados específicos para la dirección.");
                       
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo generar el resumen ejecutivo: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un análisis de productividad avanzado
     */
    private void mostrarAnalisisProductividad(List<ModeloFichaje> fichajes) {
        try {
            // Crear ventana de análisis de productividad
            Alert analisis = new Alert(Alert.AlertType.INFORMATION);
            analisis.setTitle("Análisis de Productividad");
            analisis.setHeaderText("Análisis Avanzado de Productividad");
            
            // Calcular métricas básicas
            long totalFichajes = fichajes.size();
            long fichajesCompletos = fichajes.stream()
                .filter(f -> f.getEstado() == ModeloFichaje.EstadoFichaje.CERRADO)
                .count();
            long incidencias = fichajes.stream()
                .filter(f -> f.getTipoEntrada() != ModeloFichaje.TipoFichaje.NORMAL || 
                           f.getTipoSalida() != ModeloFichaje.TipoFichaje.NORMAL)
                .count();
            
            double porcentajeCompletitud = totalFichajes > 0 ? 
                (fichajesCompletos * 100.0 / totalFichajes) : 0;
            double porcentajeIncidencias = totalFichajes > 0 ? 
                (incidencias * 100.0 / totalFichajes) : 0;
            
            StringBuilder contenido = new StringBuilder();
            contenido.append("📊 MÉTRICAS GENERALES:\n");
            contenido.append("• Total de fichajes: ").append(totalFichajes).append("\n");
            contenido.append("• Fichajes completos: ").append(fichajesCompletos)
                     .append(" (").append(String.format("%.1f", porcentajeCompletitud)).append("%)\n");
            contenido.append("• Incidencias: ").append(incidencias)
                     .append(" (").append(String.format("%.1f", porcentajeIncidencias)).append("%)\n\n");
            
            contenido.append("📈 INDICADORES DE PRODUCTIVIDAD:\n");
            if (porcentajeCompletitud >= 95) {
                contenido.append("✅ Excelente completitud de fichajes\n");
            } else if (porcentajeCompletitud >= 85) {
                contenido.append("⚠️ Completitud de fichajes mejorable\n");
            } else {
                contenido.append("❌ Completitud de fichajes deficiente\n");
            }
            
            if (porcentajeIncidencias <= 5) {
                contenido.append("✅ Bajo nivel de incidencias\n");
            } else if (porcentajeIncidencias <= 15) {
                contenido.append("⚠️ Nivel moderado de incidencias\n");
            } else {
                contenido.append("❌ Alto nivel de incidencias\n");
            }
            
            contenido.append("\n💡 RECOMENDACIONES:\n");
            if (porcentajeCompletitud < 90) {
                contenido.append("• Implementar recordatorios automáticos de fichaje\n");
            }
            if (porcentajeIncidencias > 10) {
                contenido.append("• Revisar causas frecuentes de incidencias\n");
            }
            contenido.append("• Considerar análisis por departamentos\n");
            contenido.append("• Evaluar patrones de horarios de trabajo\n");
            
            analisis.setContentText(contenido.toString());
            
            // Hacer el diálogo más grande para mostrar toda la información
            analisis.getDialogPane().setPrefWidth(500);
            analisis.getDialogPane().setPrefHeight(400);
            
            analisis.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo generar el análisis de productividad: " + e.getMessage());
        }
    }
} 