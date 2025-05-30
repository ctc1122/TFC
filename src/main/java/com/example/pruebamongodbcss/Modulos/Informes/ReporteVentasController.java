package com.example.pruebamongodbcss.Modulos.Informes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;
import com.example.pruebamongodbcss.Protocolo.Protocolo;
import com.example.pruebamongodbcss.Utilidades.GestorSocket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ReporteVentasController implements Initializable {

    @FXML
    private DatePicker fechaInicio;
    
    @FXML
    private DatePicker fechaFin;
    
    @FXML
    private Button btnGenerar;
    
    @FXML
    private Button btnExportar;
    
    @FXML
    private Label lblTotalVentas;
    
    @FXML
    private Label lblNumeroFacturas;
    
    @FXML
    private Label lblPromedioVenta;
    
    @FXML
    private VBox chartContainer;
    
    @FXML
    private TableView<FacturaTopRow> tableFacturas;
    
    @FXML
    private TableColumn<FacturaTopRow, String> colNumeroFactura;
    
    @FXML
    private TableColumn<FacturaTopRow, String> colCliente;
    
    @FXML
    private TableColumn<FacturaTopRow, String> colTotalFactura;
    
    @FXML
    private TableColumn<FacturaTopRow, String> colFecha;
    
    @FXML
    private TableColumn<FacturaTopRow, Integer> colServicios;
    
    private GestorSocket gestorSocket;
    private Usuario usuarioActual;
    private DecimalFormat formatoMoneda = new DecimalFormat("€#,##0.00");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gestorSocket = GestorSocket.getInstance();
        
        // Configurar fechas por defecto (último mes)
        fechaFin.setValue(LocalDate.now());
        fechaInicio.setValue(LocalDate.now().minusMonths(1));
        
        // Configurar tabla
        configurarTabla();
        
        // Configurar eventos
        btnGenerar.setOnAction(e -> generarReporte());
        btnExportar.setOnAction(e -> exportarReporte());
        
        // IMPORTANTE: Aplicar tema correcto desde el inicio
        aplicarTemaCorreecto();
        
        // Generar reporte inicial
        generarReporte();
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    private void configurarTabla() {
        colNumeroFactura.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colTotalFactura.setCellValueFactory(new PropertyValueFactory<>("totalFormateado"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colServicios.setCellValueFactory(new PropertyValueFactory<>("numeroServicios"));
        
        tableFacturas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void generarReporte() {
        try {
            // Obtener las fechas seleccionadas
            LocalDate inicio = fechaInicio.getValue();
            LocalDate fin = fechaFin.getValue();
            
            if (inicio == null || fin == null) {
                mostrarAlert("Error", "Fechas requeridas", "Debe seleccionar un rango de fechas válido.");
                return;
            }
            
            if (inicio.isAfter(fin)) {
                mostrarAlert("Error", "Rango de fechas inválido", "La fecha de inicio debe ser anterior a la fecha de fin.");
                return;
            }
            
            // Obtener análisis de ventas del servidor
            AnalisisVentasData analisis = obtenerAnalisisVentas(inicio, fin);
            
            if (analisis != null) {
                // Actualizar métricas
                lblTotalVentas.setText(formatoMoneda.format(analisis.getTotalVentas()));
                lblNumeroFacturas.setText(String.valueOf(analisis.getNumeroFacturas()));
                lblPromedioVenta.setText(formatoMoneda.format(analisis.getPromedioVenta()));
                
                // Actualizar gráfico de evolución
                actualizarGraficoEvolucion();
                
                // Obtener top facturas por importe
                List<FacturaTopData> topFacturas = obtenerTopFacturasPorImporte(10, inicio, fin);
            
            // Actualizar tabla
                actualizarTablaTopFacturas(topFacturas);
            }
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte de ventas: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al generar reporte", 
                        "Ha ocurrido un error al generar el reporte de ventas: " + e.getMessage());
        }
    }
    
    private void actualizarGraficoEvolucion() {
        chartContainer.getChildren().clear();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes");
        yAxis.setLabel("Ventas (€)");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Evolución de Ventas - Últimos 12 Meses");
        chart.setPrefHeight(400);
        chart.setLegendVisible(true);
        chart.setCreateSymbols(true);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas");
        
        // Obtener datos de evolución de ventas
        List<DatoGraficoData> datos = obtenerEvolucionVentasPorPeriodo(12);
        for (DatoGraficoData dato : datos) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(dato.getEtiqueta(), dato.getValor());
            series.getData().add(dataPoint);
        }
        
        chart.getData().add(series);
        
        // Configurar tooltips dinámicos después de que el gráfico se haya renderizado
        Platform.runLater(() -> {
            configurarTooltipsGrafico(chart, series);
        });
        
        chartContainer.getChildren().add(chart);
    }
    
    /**
     * Configura tooltips dinámicos para el gráfico de ventas
     */
    private void configurarTooltipsGrafico(LineChart<String, Number> chart, XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                // Crear tooltip personalizado
                Tooltip tooltip = new Tooltip();
                
                String periodo = data.getXValue();
                double valor = data.getYValue().doubleValue();
                String valorFormateado = formatoMoneda.format(valor);
                
                tooltip.setText(periodo + "\nVentas: " + valorFormateado);
                tooltip.setStyle("-fx-background-color: rgba(0,0,0,0.8); " +
                               "-fx-text-fill: white; " +
                               "-fx-background-radius: 6px; " +
                               "-fx-padding: 8px; " +
                               "-fx-font-size: 12px;");
                
                // Instalar el tooltip en el nodo
                Tooltip.install(data.getNode(), tooltip);
                
                // Efectos visuales al pasar el mouse
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setStyle("-fx-background-color: #28a745; -fx-background-radius: 6px;");
                    data.getNode().setScaleX(1.3);
                    data.getNode().setScaleY(1.3);
                });
                
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setStyle("-fx-background-color: #2196F3; -fx-background-radius: 4px;");
                    data.getNode().setScaleX(1.0);
                    data.getNode().setScaleY(1.0);
                });
            }
        }
    }
    
    private void actualizarTablaTopFacturas(List<FacturaTopData> topFacturas) {
        tableFacturas.getItems().clear();
        
        for (FacturaTopData factura : topFacturas) {
            FacturaTopRow row = new FacturaTopRow(
                factura.getNumeroFactura(),
                factura.getNombreCliente(),
                factura.getTotal(),
                formatoMoneda.format(factura.getTotal()),
                factura.getFechaCreacion().toString(),
                factura.getNumeroServicios()
            );
            tableFacturas.getItems().add(row);
        }
    }
    
    private void exportarReporte() {
        // TODO: Implementar exportación a PDF/Excel
        System.out.println("Exportando reporte de ventas...");
        mostrarAlert("Información", "Funcionalidad en desarrollo", "La exportación se implementará próximamente.");
    }
    
    @FXML
    private void volverAlDashboard() {
        try {
            // Buscar la ventana principal y obtener la escena
            javafx.stage.Window ventanaPrincipal = btnExportar.getScene().getWindow();
            if (ventanaPrincipal != null) {
                javafx.scene.Scene scene = ventanaPrincipal.getScene();
                if (scene != null) {
                    javafx.scene.Node root = scene.getRoot();
                    
                    if (root instanceof BorderPane) {
                        BorderPane mainRoot = (BorderPane) root;
                        
                        // Cargar la vista completa de informes (con filtros)
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pruebamongodbcss/Modulos/Informes/informes-view.fxml"));
                        Parent informesView = com.example.pruebamongodbcss.theme.ThemeUtil.loadWithTheme(loader);
                        
                        // Establecer el usuario en el controlador
                        InformesController controller = loader.getController();
                        if (controller != null && usuarioActual != null) {
                            controller.setUsuarioActual(usuarioActual);
                        }
                        
                        // Buscar el BorderPane central (donde se cargan los módulos)
                        if (mainRoot.getCenter() instanceof BorderPane) {
                            BorderPane centerPane = (BorderPane) mainRoot.getCenter();
                            // IMPORTANTE: Restaurar la vista completa del dashboard
                            centerPane.setTop(null);     // Limpiar primero
                            centerPane.setBottom(null);  
                            centerPane.setLeft(null);    
                            centerPane.setRight(null);   
                            centerPane.setCenter(informesView); // Restaurar vista completa con filtros
                        } else {
                            // Fallback: reemplazar directamente en el centro
                            mainRoot.setCenter(informesView);
                        }
                        
                        // Aplicar temas para asegurar consistencia
                        javafx.application.Platform.runLater(() -> {
                            com.example.pruebamongodbcss.theme.ThemeUtil.applyThemeToAllOpenWindows();
                        });
                        
                    } else {
                        System.err.println("Root no es BorderPane: " + root.getClass().getSimpleName());
                        mostrarAlert("Error", "Error de navegación", 
                                    "No se pudo identificar la estructura de navegación.");
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al cargar la vista de informes: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error al cargar dashboard", 
                        "Ha ocurrido un error al volver al dashboard: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado al volver al dashboard: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert("Error", "Error inesperado", 
                        "Ha ocurrido un error inesperado: " + e.getMessage());
        }
    }
    
    private void mostrarAlert(String titulo, String header, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Aplica el tema correcto al reporte basado en el estado actual del ThemeManager
     */
    private void aplicarTemaCorreecto() {
        javafx.application.Platform.runLater(() -> {
            try {
                // Obtener la escena actual
                if (btnGenerar != null && btnGenerar.getScene() != null) {
                    javafx.scene.Scene scene = btnGenerar.getScene();
                    javafx.scene.Parent root = scene.getRoot();
                    
                    // Verificar si el tema oscuro está activo
                    boolean temaOscuroActivo = com.example.pruebamongodbcss.theme.ThemeManager.getInstance().isDarkTheme();
                    
                    // Aplicar o quitar la clase dark-theme del root
                    if (temaOscuroActivo) {
                        if (!root.getStyleClass().contains("dark-theme")) {
                            root.getStyleClass().add("dark-theme");
                        }
                    } else {
                        root.getStyleClass().remove("dark-theme");
                    }
                    
                    // Asegurar que los temas estén aplicados correctamente
                    com.example.pruebamongodbcss.theme.ThemeUtil.applyThemeToAllOpenWindows();
                }
            } catch (Exception e) {
                System.err.println("Error al aplicar tema al reporte de ventas: " + e.getMessage());
            }
        });
    }
    
    // Métodos para peticiones al servidor
    
    private AnalisisVentasData obtenerAnalisisVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            String peticion = Protocolo.OBTENER_ANALISIS_VENTAS + Protocolo.SEPARADOR_CODIGO + 
                             fechaInicio.toString() + Protocolo.SEPARADOR_PARAMETROS + fechaFin.toString();
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_ANALISIS_VENTAS_RESPONSE) {
                return (AnalisisVentasData) entrada.readObject();
            } else {
                System.err.println("Error al obtener análisis de ventas");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerAnalisisVentas: " + e.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<DatoGraficoData> obtenerEvolucionVentasPorPeriodo(int meses) {
        try {
            String peticion = Protocolo.OBTENER_EVOLUCION_VENTAS_POR_PERIODO + Protocolo.SEPARADOR_CODIGO + meses;
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_EVOLUCION_VENTAS_POR_PERIODO_RESPONSE) {
                return (List<DatoGraficoData>) entrada.readObject();
            } else {
                System.err.println("Error al obtener evolución de ventas por período");
                return List.of(); // Lista vacía
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerEvolucionVentasPorPeriodo: " + e.getMessage());
            return List.of();
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<FacturaTopData> obtenerTopFacturasPorImporte(int limite, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            String peticion = Protocolo.OBTENER_TOP_FACTURAS_POR_IMPORTE + Protocolo.SEPARADOR_CODIGO + 
                             limite + Protocolo.SEPARADOR_PARAMETROS + 
                             fechaInicio.toString() + Protocolo.SEPARADOR_PARAMETROS + fechaFin.toString();
            gestorSocket.enviarPeticion(peticion);
            
            ObjectInputStream entrada = gestorSocket.getEntrada();
            int codigoRespuesta = entrada.readInt();
            
            if (codigoRespuesta == Protocolo.OBTENER_TOP_FACTURAS_POR_IMPORTE_RESPONSE) {
                return (List<FacturaTopData>) entrada.readObject();
            } else {
                System.err.println("Error al obtener top facturas por importe");
                return List.of(); // Lista vacía
            }
        } catch (Exception e) {
            System.err.println("Error en petición obtenerTopFacturasPorImporte: " + e.getMessage());
            return List.of();
        }
    }
    
    // Clases de datos para la comunicación cliente-servidor
    
    public static class AnalisisVentasData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private double totalVentas;
        private int numeroFacturas;
        private double promedioVenta;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        
        public double getTotalVentas() { return totalVentas; }
        public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }
        
        public int getNumeroFacturas() { return numeroFacturas; }
        public void setNumeroFacturas(int numeroFacturas) { this.numeroFacturas = numeroFacturas; }
        
        public double getPromedioVenta() { return promedioVenta; }
        public void setPromedioVenta(double promedioVenta) { this.promedioVenta = promedioVenta; }
        
        public LocalDate getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
        
        public LocalDate getFechaFin() { return fechaFin; }
        public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    }
    
    public static class DatoGraficoData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String etiqueta;
        private double valor;
        
        public DatoGraficoData(String etiqueta, double valor) {
            this.etiqueta = etiqueta;
            this.valor = valor;
        }
        
        public String getEtiqueta() { return etiqueta; }
        public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
        
        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
    }
    
    public static class FacturaTopData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String numeroFactura;
        private String nombreCliente;
        private double total;
        private LocalDate fechaCreacion;
        private int numeroServicios;
        
        public String getNumeroFactura() { return numeroFactura; }
        public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
        
        public String getNombreCliente() { return nombreCliente; }
        public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
        
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        
        public LocalDate getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
        
        public int getNumeroServicios() { return numeroServicios; }
        public void setNumeroServicios(int numeroServicios) { this.numeroServicios = numeroServicios; }
    }
    
    // Clase para las filas de la tabla de servicios vendidos
    public static class FacturaTopRow {
        private String numeroFactura;
        private String nombreCliente;
        private Double total;
        private String totalFormateado;
        private String fechaCreacion;
        private Integer numeroServicios;
        
        public FacturaTopRow(String numeroFactura, String nombreCliente, Double total, 
                           String totalFormateado, String fechaCreacion, Integer numeroServicios) {
            this.numeroFactura = numeroFactura;
            this.nombreCliente = nombreCliente;
            this.total = total;
            this.totalFormateado = totalFormateado;
            this.fechaCreacion = fechaCreacion;
            this.numeroServicios = numeroServicios;
        }
        
        // Getters y setters
        public String getNumeroFactura() { return numeroFactura; }
        public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
        
        public String getNombreCliente() { return nombreCliente; }
        public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
        
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
        
        public String getTotalFormateado() { return totalFormateado; }
        public void setTotalFormateado(String totalFormateado) { this.totalFormateado = totalFormateado; }
        
        public String getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
        
        public Integer getNumeroServicios() { return numeroServicios; }
        public void setNumeroServicios(Integer numeroServicios) { this.numeroServicios = numeroServicios; }
    }
    
    // Clase para servicios vendidos en comunicación cliente-servidor
    public static class ServicioVendidoData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String nombre;
        private int cantidad;
        private double total;
        
        public ServicioVendidoData() {}
        
        public ServicioVendidoData(String nombre, int cantidad, double total) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.total = total;
        }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
    }
} 