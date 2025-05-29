package com.example.pruebamongodbcss.Modulos.Informes;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;

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
    private TableView<ServicioVendidoRow> tableServicios;
    
    @FXML
    private TableColumn<ServicioVendidoRow, String> colServicio;
    
    @FXML
    private TableColumn<ServicioVendidoRow, Integer> colCantidad;
    
    @FXML
    private TableColumn<ServicioVendidoRow, String> colTotal;
    
    private ServicioInformes servicioInformes;
    private Usuario usuarioActual;
    private DecimalFormat formatoMoneda = new DecimalFormat("€#,##0.00");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        servicioInformes = new ServicioInformes();
        
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
        colServicio.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalFormateado"));
        
        tableServicios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void generarReporte() {
        try {
            // Obtener datos del servicio
            ServicioInformes.DashboardMetricas metricas = servicioInformes.obtenerMetricasDashboard();
            List<ServicioInformes.ServicioVendido> topServicios = servicioInformes.obtenerTopServicios(10);
            List<ServicioInformes.DatoGrafico> ventasMensuales = servicioInformes.obtenerVentasMensuales(12);
            
            // Actualizar métricas
            lblTotalVentas.setText(formatoMoneda.format(metricas.getVentasMesActual()));
            lblNumeroFacturas.setText(String.valueOf(metricas.getNumeroFacturasMes()));
            lblPromedioVenta.setText(formatoMoneda.format(metricas.getPromedioVentasDiarias()));
            
            // Actualizar gráfico
            actualizarGrafico(ventasMensuales);
            
            // Actualizar tabla
            actualizarTabla(topServicios);
            
        } catch (Exception e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void actualizarGrafico(List<ServicioInformes.DatoGrafico> datos) {
        chartContainer.getChildren().clear();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes");
        yAxis.setLabel("Ventas (€)");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Evolución de Ventas - Últimos 12 Meses");
        chart.setPrefHeight(400);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas");
        
        for (ServicioInformes.DatoGrafico dato : datos) {
            series.getData().add(new XYChart.Data<>(dato.getEtiqueta(), dato.getValor()));
        }
        
        chart.getData().add(series);
        chartContainer.getChildren().add(chart);
    }
    
    private void actualizarTabla(List<ServicioInformes.ServicioVendido> servicios) {
        tableServicios.getItems().clear();
        
        for (ServicioInformes.ServicioVendido servicio : servicios) {
            ServicioVendidoRow row = new ServicioVendidoRow(
                servicio.getNombre(),
                servicio.getCantidad(),
                servicio.getTotal(),
                formatoMoneda.format(servicio.getTotal())
            );
            tableServicios.getItems().add(row);
        }
    }
    
    private void exportarReporte() {
        // Implementar exportación a PDF/Excel
        System.out.println("Exportando reporte de ventas...");
        // Aquí podrías usar librerías como iText para PDF o Apache POI para Excel
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
    
    // Clase para las filas de la tabla
    public static class ServicioVendidoRow {
        private String nombre;
        private Integer cantidad;
        private Double total;
        private String totalFormateado;
        
        public ServicioVendidoRow(String nombre, Integer cantidad, Double total, String totalFormateado) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.total = total;
            this.totalFormateado = totalFormateado;
        }
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
        
        public String getTotalFormateado() { return totalFormateado; }
        public void setTotalFormateado(String totalFormateado) { this.totalFormateado = totalFormateado; }
    }
} 