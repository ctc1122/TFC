package com.example.pruebamongodbcss.Modulos.Informes;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.example.pruebamongodbcss.Data.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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